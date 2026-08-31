package jadx.gui.cache.code.disk;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jadx.api.ICodeCache;
import jadx.api.ICodeInfo;
import jadx.api.JadxArgs;
import jadx.api.JadxDecompiler;
import jadx.core.dex.nodes.ClassNode;
import jadx.core.dex.nodes.RootNode;
import jadx.core.utils.AnalysisFingerprint;
import jadx.core.utils.Utils;
import jadx.core.utils.exceptions.JadxRuntimeException;
import jadx.core.utils.files.FileUtils;

public class DiskCodeCache implements ICodeCache {
	private static final Logger LOG = LoggerFactory.getLogger(DiskCodeCache.class);

	private static final int DATA_FORMAT_VERSION = 18;
	private static final String ENTRY_SUFFIX = ".jadxbc";
	private static final Map<Path, ProcessLockCoordinator> PROCESS_LOCKS = new ConcurrentHashMap<>();

	private final Path baseDir;
	private final Path entriesDir;
	private final Path codeVersionFile;
	private final Path processLockFile;
	private final ProcessLockCoordinator processLockCoordinator;
	private final Path stagingRoot;
	private final String codeVersion;
	private final CodeMetadataAdapter codeMetadataAdapter;
	private final ExecutorService writePool;
	private final Map<String, CacheData> clsDataMap;

	public DiskCodeCache(RootNode root, Path projectCacheDir) {
		baseDir = projectCacheDir.resolve("code");
		entriesDir = baseDir.resolve("entries");
		codeVersionFile = baseDir.resolve("code-version");
		processLockFile = projectCacheDir.resolve("code-cache.lock").toAbsolutePath().normalize();
		processLockCoordinator = PROCESS_LOCKS.computeIfAbsent(
				processLockFile, ignored -> new ProcessLockCoordinator());
		stagingRoot = projectCacheDir.resolve("code-staging");
		JadxArgs args = root.getArgs();
		codeVersion = buildCodeVersion(args, root.getDecompiler());
		writePool = buildWritePool(args.getThreadsCount());
		codeMetadataAdapter = new CodeMetadataAdapter(root);
		clsDataMap = buildClassDataMap(root.getClasses());
		try {
			withProcessLock(() -> {
				if (checkCodeVersion()) {
					loadCachedSet();
				} else {
					reset();
				}
				return null;
			});
		} catch (Exception e) {
			throw new JadxRuntimeException("Failed to initialize disk code cache", e);
		}
		FileUtils.makeDirs(stagingRoot);
	}

	private static ExecutorService buildWritePool(int threads) {
		// Keep decompiler workers independent from short NTFS latency bursts. Entries stay bounded
		// because every queued item retains generated code and metadata until publication completes.
		int queueCapacity = Math.max(256, threads * 128);
		ThreadPoolExecutor executor = new ThreadPoolExecutor(
				threads,
				threads,
				0L,
				TimeUnit.MILLISECONDS,
				new ArrayBlockingQueue<>(queueCapacity),
				Utils.simpleThreadFactory("disk-code-cache-write"));
		executor.setRejectedExecutionHandler((runnable, pool) -> {
			try {
				while (true) {
					if (pool.isShutdown()) {
						throw new RejectedExecutionException("Disk code cache is closed");
					}
					if (pool.getQueue().offer(runnable, 100, TimeUnit.MILLISECONDS)) {
						// shutdown can win between the state check and direct queue offer. If the
						// worker did not claim this task, remove it and report rejection so the
						// class generation can restore its previous published state.
						if (pool.isShutdown() && pool.remove(runnable)) {
							throw new RejectedExecutionException("Disk code cache is closed");
						}
						return;
					}
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new RejectedExecutionException("Interrupted while queuing disk cache write", e);
			}
		});
		return executor;
	}

	private boolean checkCodeVersion() {
		try {
			if (!Files.exists(codeVersionFile)) {
				return false;
			}
			String currentCodeVer = FileUtils.readFile(codeVersionFile);
			return currentCodeVer.equals(codeVersion);
		} catch (Exception e) {
			LOG.warn("Failed to load code version file", e);
			return false;
		}
	}

	private void reset() {
		try {
			long start = System.currentTimeMillis();
			LOG.info("Resetting disk code cache, base dir: {}", baseDir.toAbsolutePath());
			FileUtils.deleteDirIfExists(baseDir);
			Path legacyVersionFile = baseDir.getParent().resolve(codeVersionFile.getFileName());
			if (Files.exists(legacyVersionFile)) {
				// remove old version cache files
				Files.deleteIfExists(legacyVersionFile);
				FileUtils.deleteDirIfExists(baseDir.getParent().resolve("sources"));
				FileUtils.deleteDirIfExists(baseDir.getParent().resolve("metadata"));
			}
			FileUtils.makeDirs(entriesDir);
			FileUtils.writeFile(codeVersionFile, codeVersion);
			if (LOG.isDebugEnabled()) {
				LOG.info("Reset done in: {}ms", System.currentTimeMillis() - start);
			}
		} catch (Exception e) {
			throw new JadxRuntimeException("Failed to reset code cache", e);
		} finally {
			clsDataMap.values().forEach(CacheData::reset);
		}
	}

	/**
	 * Async writes backed by in-memory store
	 */
	@Override
	public void add(String clsFullName, ICodeInfo codeInfo) {
		CacheData clsData = getClsData(clsFullName);
		WriteRequest write = clsData.beginWrite(codeInfo);
		try {
			writePool.execute(() -> writeEntry(clsFullName, clsData, write, codeInfo));
		} catch (RejectedExecutionException e) {
			clsData.failWrite(write.generation);
			throw e;
		}
	}

	private void writeEntry(String clsFullName, CacheData clsData, WriteRequest write, ICodeInfo codeInfo) {
		Path stagingFile = null;
		try {
			int clsId = clsData.getClsId();
			Path entryFile = getEntryFile(clsId);
			stagingFile = stagingRoot.resolve(Integer.toHexString(clsId) + '-' + write.fileToken + ".tmp");
			codeMetadataAdapter.writeBundle(stagingFile, codeInfo);
			Path completedStagingFile = stagingFile;
			boolean published = withProcessLock(() -> {
				synchronized (clsData) {
					if (!clsData.isCurrentWrite(write.generation) || !checkCodeVersion()) {
						return false;
					}
					FileUtils.makeDirs(entryFile.getParent());
					moveAtomically(completedStagingFile, entryFile);
					clsData.finishWrite(write.generation);
					return true;
				}
			});
			if (published) {
				// Atomic move consumed the staging file.
				stagingFile = null;
			} else {
				clsData.failWrite(write.generation);
			}
		} catch (Exception e) {
			LOG.error("Failed to write code cache for " + clsFullName, e);
			clsData.failWrite(write.generation);
		} finally {
			deleteTemporary(stagingFile);
		}
	}

	private static void moveAtomically(Path source, Path target) throws IOException {
		try {
			Files.move(source, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
		} catch (AtomicMoveNotSupportedException e) {
			Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
		}
	}

	private static void deleteTemporary(@Nullable Path path) {
		if (path == null) {
			return;
		}
		try {
			Files.deleteIfExists(path);
		} catch (IOException e) {
			LOG.debug("Failed to delete temporary code cache file: {}", path, e);
		}
	}

	@Override
	public @Nullable String getCode(String clsFullName) {
		try {
			CacheData clsData = getClsData(clsFullName);
			ICodeInfo pendingCodeInfo = clsData.getTmpCodeInfo();
			if (pendingCodeInfo != null) {
				return pendingCodeInfo.getCodeStr();
			}
			if (isUnpublishedMiss(clsData)) {
				return null;
			}
			synchronized (clsData) {
				ICodeInfo tmpCodeInfo = clsData.getTmpCodeInfo();
				if (tmpCodeInfo != null) {
					return tmpCodeInfo.getCodeStr();
				}
				Path entryFile = getEntryFile(clsData.getClsId());
				refreshPublishedEntry(clsData, entryFile);
				if (!clsData.isCached()) {
					return null;
				}
				return codeMetadataAdapter.readCode(entryFile);
			}
		} catch (Exception e) {
			LOG.error("Failed to read class code for {}", clsFullName, e);
			invalidateEntry(clsFullName);
			return null;
		}
	}

	@Override
	public @NotNull ICodeInfo get(String clsFullName) {
		return getWithKnownCode(clsFullName, null);
	}

	@Override
	public @NotNull ICodeInfo getWithKnownCode(String clsFullName, @Nullable String knownCode) {
		try {
			CacheData clsData = getClsData(clsFullName);
			ICodeInfo pendingCodeInfo = clsData.getTmpCodeInfo();
			if (pendingCodeInfo != null) {
				return pendingCodeInfo;
			}
			if (isUnpublishedMiss(clsData)) {
				return ICodeInfo.EMPTY;
			}
			synchronized (clsData) {
				ICodeInfo tmpCodeInfo = clsData.getTmpCodeInfo();
				if (tmpCodeInfo != null) {
					return tmpCodeInfo;
				}
				Path entryFile = getEntryFile(clsData.getClsId());
				refreshPublishedEntry(clsData, entryFile);
				if (!clsData.isCached()) {
					return ICodeInfo.EMPTY;
				}
				return codeMetadataAdapter.readAndBuild(entryFile, knownCode);
			}
		} catch (Exception e) {
			LOG.error("Failed to read code cache for {}", clsFullName, e);
			invalidateEntry(clsFullName);
			return ICodeInfo.EMPTY;
		}
	}

	/**
	 * Cold-cache misses are the overwhelmingly common path during the first decompilation. Avoid
	 * touching the bundle unless this process already knows about an entry or another process has
	 * installed its atomic file. A publisher can win immediately after the check; that only causes
	 * one harmless local cache miss and the next lookup observes it.
	 */
	private boolean isUnpublishedMiss(CacheData clsData) {
		return !clsData.isCached()
				&& !Files.isRegularFile(getEntryFile(clsData.getClsId()));
	}

	private void invalidateEntry(String clsFullName) {
		try {
			remove(clsFullName);
		} catch (Exception removeError) {
			LOG.warn("Failed to remove invalid code cache entry for {}", clsFullName, removeError);
		}
	}

	@Override
	public boolean contains(String clsFullName) {
		return getClsData(clsFullName).isCached();
	}

	@Override
	public void remove(String clsFullName) {
		try {
			CacheData clsData = getClsData(clsFullName);
			withProcessLock(() -> {
				synchronized (clsData) {
					Path entryFile = getEntryFile(clsData.getClsId());
					if (clsData.invalidate() || Files.isRegularFile(entryFile)) {
						LOG.debug("Removing class info from disk: {}", clsFullName);
						Files.deleteIfExists(entryFile);
					}
					return null;
				}
			});
		} catch (Exception e) {
			throw new JadxRuntimeException("Failed to remove code cache for " + clsFullName, e);
		}
	}

	private String buildCodeVersion(JadxArgs args, @Nullable JadxDecompiler decompiler) {
		String fingerprint = decompiler == null
				? AnalysisFingerprint.build(args, null)
				: decompiler.getAnalysisFingerprint();
		return DATA_FORMAT_VERSION + ":" + fingerprint;
	}

	private CacheData getClsData(String clsFullName) {
		CacheData clsData = clsDataMap.get(clsFullName);
		if (clsData == null) {
			throw new JadxRuntimeException("Unknown class name: " + clsFullName);
		}
		return clsData;
	}

	private void loadCachedSet() {
		long start = System.currentTimeMillis();
		Map<Integer, CacheData> dataById = new HashMap<>(clsDataMap.size());
		clsDataMap.values().forEach(data -> dataById.put(data.getClsId(), data));
		List<Path> entries;
		try (Stream<Path> stream = Files.walk(entriesDir)) {
			entries = stream.filter(Files::isRegularFile)
					.filter(file -> file.getFileName().toString().endsWith(ENTRY_SUFFIX))
					.toList();
		} catch (Exception e) {
			throw new JadxRuntimeException("Failed to enumerate cached classes", e);
		}
		int count = 0;
		for (Path entry : entries) {
			try {
				String fileName = entry.getFileName().toString();
				int clsId = Integer.parseInt(fileName.substring(0, fileName.length() - ENTRY_SUFFIX.length()), 16);
				CacheData data = dataById.get(clsId);
				if (data == null) {
					continue;
				}
				data.loadPublished();
				count++;
			} catch (Exception e) {
				LOG.warn("Ignoring invalid code cache entry: {}", entry, e);
				deleteTemporary(entry);
			}
		}
		LOG.info("Found {} classes in disk cache, time: {}ms, dir: {}",
				count, System.currentTimeMillis() - start, entriesDir.getParent());
	}

	private void refreshPublishedEntry(CacheData clsData, Path entryFile) {
		if (!Files.isRegularFile(entryFile)) {
			clsData.clearPublished();
			return;
		}
		clsData.loadPublished();
	}

	private <T> T withProcessLock(LockedAction<T> action) throws Exception {
		return processLockCoordinator.execute(processLockFile, action);
	}

	private Path getEntryFile(int clsId) {
		// all classes divided between 256 top level folders
		String firstByte = FileUtils.byteToHex(clsId);
		return entriesDir.resolve(firstByte).resolve(FileUtils.intToHex(clsId) + ENTRY_SUFFIX);
	}

	private Map<String, CacheData> buildClassDataMap(List<ClassNode> classes) {
		int clsCount = classes.size();
		Map<String, CacheData> map = new HashMap<>(clsCount);
		for (int i = 0; i < clsCount; i++) {
			ClassNode cls = classes.get(i);
			map.put(cls.getRawName(), new CacheData(i));
		}
		return map;
	}

	@Override
	public void close() throws IOException {
		boolean interrupted = false;
		writePool.shutdown();
		long gracefulDeadline = System.nanoTime() + TimeUnit.MINUTES.toNanos(1);
		try {
			while (!writePool.isTerminated()) {
				long remaining = gracefulDeadline - System.nanoTime();
				if (remaining <= 0) {
					LOG.warn("Disk code cache closing terminated by timeout, forcing pending writes to stop");
					writePool.shutdownNow();
					break;
				}
				try {
					writePool.awaitTermination(remaining, TimeUnit.NANOSECONDS);
				} catch (InterruptedException e) {
					// Project reload can interrupt its coordinator. Do not let old cache
					// writers escape into the next project lifecycle.
					interrupted = true;
				}
			}
			long forcedDeadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
			while (!writePool.isTerminated() && System.nanoTime() < forcedDeadline) {
				try {
					writePool.awaitTermination(100, TimeUnit.MILLISECONDS);
				} catch (InterruptedException e) {
					interrupted = true;
				}
			}
			if (!writePool.isTerminated()) {
				LOG.warn("Disk code cache write pool did not terminate");
			}
		} finally {
			if (interrupted) {
				Thread.currentThread().interrupt();
			}
		}
	}

	private static final class CacheData {
		private final int clsId;
		private volatile boolean cached;
		private volatile @Nullable ICodeInfo tmpCodeInfo;
		private boolean published;
		private long writeGeneration;

		public CacheData(int clsId) {
			this.clsId = clsId;
		}

		public int getClsId() {
			return clsId;
		}

		public boolean isCached() {
			return cached;
		}

		public synchronized void reset() {
			writeGeneration++;
			cached = false;
			tmpCodeInfo = null;
			published = false;
		}

		public synchronized WriteRequest beginWrite(ICodeInfo codeInfo) {
			writeGeneration++;
			tmpCodeInfo = codeInfo;
			cached = true;
			String fileToken = Long.toHexString(writeGeneration) + '-' + UUID.randomUUID().toString().replace("-", "");
			return new WriteRequest(writeGeneration, fileToken);
		}

		public synchronized boolean isCurrentWrite(long generation) {
			return cached && writeGeneration == generation;
		}

		public synchronized void finishWrite(long generation) {
			if (writeGeneration == generation) {
				published = true;
				tmpCodeInfo = null;
				cached = true;
			}
		}

		public synchronized void failWrite(long generation) {
			if (writeGeneration == generation) {
				tmpCodeInfo = null;
				cached = published;
			}
		}

		public synchronized void loadPublished() {
			published = true;
			tmpCodeInfo = null;
			cached = true;
		}

		public synchronized void clearPublished() {
			published = false;
			if (tmpCodeInfo == null) {
				cached = false;
			}
		}

		public synchronized boolean invalidate() {
			boolean wasCached = cached;
			writeGeneration++;
			cached = false;
			tmpCodeInfo = null;
			published = false;
			return wasCached;
		}

		public @Nullable ICodeInfo getTmpCodeInfo() {
			return tmpCodeInfo;
		}

	}

	private static final class WriteRequest {
		private final long generation;
		private final String fileToken;

		private WriteRequest(long generation, String fileToken) {
			this.generation = generation;
			this.fileToken = fileToken;
		}
	}

	@FunctionalInterface
	private interface LockedAction<T> {
		T run() throws Exception;
	}

	private static final class ProcessLockCoordinator {
		private static final int MAX_BATCH_SIZE = 64;

		private final ReentrantLock queueLock = new ReentrantLock();
		private final ArrayDeque<LockRequest<?>> queue = new ArrayDeque<>();
		private boolean draining;
		private volatile @Nullable Thread lockOwner;

		private <T> T execute(Path lockFile, LockedAction<T> action) throws Exception {
			if (Thread.currentThread() == lockOwner) {
				return action.run();
			}
			LockRequest<T> request = new LockRequest<>(action);
			boolean startDraining = false;
			queueLock.lock();
			try {
				queue.addLast(request);
				if (!draining) {
					draining = true;
					startDraining = true;
				}
			} finally {
				queueLock.unlock();
			}
			if (startDraining) {
				drain(lockFile);
			}
			return request.await();
		}

		private void drain(Path lockFile) {
			while (true) {
				try {
					Files.createDirectories(lockFile.getParent());
					try (FileChannel channel = FileChannel.open(
							lockFile, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
							FileLock ignored = channel.lock()) {
						lockOwner = Thread.currentThread();
						for (int i = 0; i < MAX_BATCH_SIZE; i++) {
							LockRequest<?> request = pollRequest();
							if (request == null) {
								break;
							}
							request.run();
						}
					} finally {
						lockOwner = null;
					}
				} catch (Throwable e) {
					failPending(e);
					return;
				}
				queueLock.lock();
				try {
					if (queue.isEmpty()) {
						draining = false;
						return;
					}
				} finally {
					queueLock.unlock();
				}
			}
		}

		private @Nullable LockRequest<?> pollRequest() {
			queueLock.lock();
			try {
				return queue.pollFirst();
			} finally {
				queueLock.unlock();
			}
		}

		private void failPending(Throwable error) {
			queueLock.lock();
			try {
				LockRequest<?> request;
				while ((request = queue.pollFirst()) != null) {
					request.fail(error);
				}
				draining = false;
			} finally {
				queueLock.unlock();
			}
		}

	}

	private static final class LockRequest<T> {
		private final LockedAction<T> action;
		private final CompletableFuture<T> result = new CompletableFuture<>();

		private LockRequest(LockedAction<T> action) {
			this.action = action;
		}

		private void run() {
			try {
				result.complete(action.run());
			} catch (Throwable e) {
				result.completeExceptionally(e);
			}
		}

		private void fail(Throwable error) {
			result.completeExceptionally(error);
		}

		private T await() throws Exception {
			try {
				return result.get();
			} catch (ExecutionException e) {
				Throwable cause = e.getCause();
				if (cause instanceof Exception) {
					throw (Exception) cause;
				}
				if (cause instanceof Error) {
					throw (Error) cause;
				}
				throw new JadxRuntimeException("Unexpected process-lock failure", cause);
			}
		}
	}
}
