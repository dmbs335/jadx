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

	private static final int DATA_FORMAT_VERSION = 17;
	private static final String CURRENT_FILE = "current";
	private static final String CODE_FILE = "code.java";
	private static final String METADATA_FILE = "metadata.jadxmd";
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
	}

	private static ExecutorService buildWritePool(int threads) {
		int queueCapacity = Math.max(32, threads * 16);
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
		Path stagingDir = null;
		Path bundleDir = null;
		try {
			int clsId = clsData.getClsId();
			Path classDir = getClassDir(clsId);
			stagingDir = stagingRoot.resolve(Integer.toHexString(clsId) + '-' + write.bundleName);
			bundleDir = classDir.resolve(write.bundleName);
			FileUtils.makeDirs(stagingDir);
			FileUtils.writeFile(stagingDir.resolve(CODE_FILE), codeInfo.getCodeStr());
			codeMetadataAdapter.write(stagingDir.resolve(METADATA_FILE), codeInfo.getCodeMetadata());
			Path completedStagingDir = stagingDir;
			Path completedBundleDir = bundleDir;
			boolean published = withProcessLock(() -> {
				synchronized (clsData) {
					if (!clsData.isCurrentWrite(write.generation) || !checkCodeVersion()) {
						return false;
					}
					FileUtils.makeDirs(classDir);
					Path currentPointer = classDir.resolve(CURRENT_FILE);
					String previousBundle = null;
					boolean cleanupAllBundles = false;
					if (Files.isRegularFile(currentPointer)) {
						String currentBundle = FileUtils.readFile(currentPointer).trim();
						if (isValidBundleName(currentBundle)) {
							previousBundle = currentBundle;
						} else {
							cleanupAllBundles = true;
						}
					}
					moveAtomically(completedStagingDir, completedBundleDir);
					Path localPointerTmp = classDir.resolve(CURRENT_FILE + '.' + write.bundleName + ".tmp");
					try {
						FileUtils.writeFile(localPointerTmp, write.bundleName);
						moveAtomically(localPointerTmp, currentPointer);
					} finally {
						deleteTemporary(localPointerTmp);
					}
					clsData.finishWrite(write.generation, write.bundleName);
					if (cleanupAllBundles) {
						deleteOldBundles(classDir, write.bundleName);
					} else if (previousBundle != null && !previousBundle.equals(write.bundleName)) {
						deleteDirectory(classDir.resolve(previousBundle));
					}
					return true;
				}
			});
			if (!published) {
				clsData.failWrite(write.generation);
			}
			bundleDir = null;
		} catch (Exception e) {
			LOG.error("Failed to write code cache for " + clsFullName, e);
			clsData.failWrite(write.generation);
			deleteDirectory(bundleDir);
		} finally {
			deleteDirectory(stagingDir);
		}
	}

	private static void deleteOldBundles(Path classDir, String currentBundle) {
		try (Stream<Path> stream = Files.list(classDir)) {
			stream.forEach(path -> {
				String name = path.getFileName().toString();
				if (Files.isDirectory(path) && !name.equals(currentBundle)) {
					deleteDirectory(path);
				} else if (Files.isRegularFile(path) && name.endsWith(".tmp")) {
					deleteTemporary(path);
				}
			});
		} catch (Exception e) {
			LOG.debug("Failed to clean old code cache bundles in {}", classDir, e);
		}
	}

	private static void deleteDirectory(@Nullable Path path) {
		if (path == null) {
			return;
		}
		try {
			FileUtils.deleteDirIfExists(path);
		} catch (Exception e) {
			LOG.debug("Failed to delete temporary code cache directory: {}", path, e);
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
			return withProcessLock(() -> {
				synchronized (clsData) {
					ICodeInfo tmpCodeInfo = clsData.getTmpCodeInfo();
					if (tmpCodeInfo != null) {
						return tmpCodeInfo.getCodeStr();
					}
					refreshPublishedBundle(clsData, true);
					if (!clsData.isCached()) {
						return null;
					}
					Path codeFile = getPublishedFile(clsData, CODE_FILE);
					if (codeFile == null || !Files.isRegularFile(codeFile)) {
						throw new IOException("Published code cache bundle is incomplete");
					}
					return FileUtils.readFile(codeFile);
				}
			});
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
			return withProcessLock(() -> {
				synchronized (clsData) {
					ICodeInfo tmpCodeInfo = clsData.getTmpCodeInfo();
					if (tmpCodeInfo != null) {
						return tmpCodeInfo;
					}
					refreshPublishedBundle(clsData, knownCode == null);
					if (!clsData.isCached()) {
						return ICodeInfo.EMPTY;
					}
					Path codeFile = getPublishedFile(clsData, CODE_FILE);
					Path metadataFile = getPublishedFile(clsData, METADATA_FILE);
					if (metadataFile == null || !Files.isRegularFile(metadataFile)
							|| knownCode == null && (codeFile == null || !Files.isRegularFile(codeFile))) {
						throw new IOException("Published code cache bundle is incomplete");
					}
					String code = knownCode == null ? FileUtils.readFile(codeFile) : knownCode;
					return codeMetadataAdapter.readAndBuild(metadataFile, code);
				}
			});
		} catch (Exception e) {
			LOG.error("Failed to read code cache for {}", clsFullName, e);
			invalidateEntry(clsFullName);
			return ICodeInfo.EMPTY;
		}
	}

	/**
	 * Cold-cache misses are the overwhelmingly common path during the first decompilation. Avoid
	 * entering the cross-process publication lock unless this process already knows about an entry
	 * or another process has installed its atomic pointer. A publisher can win immediately after the
	 * pointer check; that only causes one harmless local cache miss and the next lookup observes it.
	 */
	private boolean isUnpublishedMiss(CacheData clsData) {
		return !clsData.isCached()
				&& !Files.isRegularFile(getClassDir(clsData.getClsId()).resolve(CURRENT_FILE));
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
					if (clsData.invalidate()) {
						LOG.debug("Removing class info from disk: {}", clsFullName);
						FileUtils.deleteDirIfExists(getClassDir(clsData.getClsId()));
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
		List<Path> pointers;
		try (Stream<Path> stream = Files.walk(entriesDir)) {
			pointers = stream.filter(Files::isRegularFile)
					.filter(file -> file.getFileName().toString().equals(CURRENT_FILE))
					.toList();
		} catch (Exception e) {
			throw new JadxRuntimeException("Failed to enumerate cached classes", e);
		}
		int count = 0;
		for (Path pointer : pointers) {
			try {
				int clsId = Integer.parseInt(pointer.getParent().getFileName().toString(), 16);
				CacheData data = dataById.get(clsId);
				if (data == null) {
					continue;
				}
				String bundleName = FileUtils.readFile(pointer).trim();
				if (!isValidBundleName(bundleName)) {
					throw new IOException("Invalid code cache bundle name: " + bundleName);
				}
				Path bundleDir = pointer.getParent().resolve(bundleName);
				if (!Files.isRegularFile(bundleDir.resolve(CODE_FILE))
						|| !Files.isRegularFile(bundleDir.resolve(METADATA_FILE))) {
					throw new IOException("Incomplete code cache bundle: " + bundleDir);
				}
				data.loadPublished(bundleName);
				deleteOldBundles(pointer.getParent(), bundleName);
				count++;
			} catch (Exception e) {
				LOG.warn("Ignoring invalid code cache entry: {}", pointer, e);
				deleteDirectory(pointer.getParent());
			}
		}
		LOG.info("Found {} classes in disk cache, time: {}ms, dir: {}",
				count, System.currentTimeMillis() - start, entriesDir.getParent());
	}

	private static boolean isValidBundleName(String bundleName) {
		return !bundleName.isEmpty() && bundleName.chars()
				.allMatch(ch -> ch == '-' || ch >= '0' && ch <= '9' || ch >= 'a' && ch <= 'f');
	}

	private @Nullable Path getPublishedFile(CacheData clsData, String fileName) {
		String bundleName = clsData.getPublishedBundle();
		if (bundleName == null) {
			return null;
		}
		return getClassDir(clsData.getClsId()).resolve(bundleName).resolve(fileName);
	}

	private void refreshPublishedBundle(CacheData clsData, boolean requireCode) throws IOException {
		Path classDir = getClassDir(clsData.getClsId());
		Path pointer = classDir.resolve(CURRENT_FILE);
		if (!Files.isRegularFile(pointer)) {
			clsData.clearPublished();
			return;
		}
		String bundleName = FileUtils.readFile(pointer).trim();
		if (!isValidBundleName(bundleName)) {
			throw new IOException("Invalid code cache bundle name: " + bundleName);
		}
		Path bundleDir = classDir.resolve(bundleName);
		if (requireCode && !Files.isRegularFile(bundleDir.resolve(CODE_FILE))
				|| !Files.isRegularFile(bundleDir.resolve(METADATA_FILE))) {
			throw new IOException("Incomplete code cache bundle: " + bundleDir);
		}
		clsData.loadPublished(bundleName);
	}

	private <T> T withProcessLock(LockedAction<T> action) throws Exception {
		return processLockCoordinator.execute(processLockFile, action);
	}

	private Path getClassDir(int clsId) {
		// all classes divided between 256 top level folders
		String firstByte = FileUtils.byteToHex(clsId);
		return entriesDir.resolve(firstByte).resolve(FileUtils.intToHex(clsId));
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
		private volatile @Nullable String publishedBundle;
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
			publishedBundle = null;
		}

		public synchronized WriteRequest beginWrite(ICodeInfo codeInfo) {
			writeGeneration++;
			tmpCodeInfo = codeInfo;
			cached = true;
			String bundleName = Long.toHexString(writeGeneration) + '-' + UUID.randomUUID().toString().replace("-", "");
			return new WriteRequest(writeGeneration, bundleName);
		}

		public synchronized boolean isCurrentWrite(long generation) {
			return cached && writeGeneration == generation;
		}

		public synchronized void finishWrite(long generation, String bundleName) {
			if (writeGeneration == generation) {
				publishedBundle = bundleName;
				tmpCodeInfo = null;
				cached = true;
			}
		}

		public synchronized void failWrite(long generation) {
			if (writeGeneration == generation) {
				tmpCodeInfo = null;
				cached = publishedBundle != null;
			}
		}

		public synchronized void loadPublished(String bundleName) {
			publishedBundle = bundleName;
			tmpCodeInfo = null;
			cached = true;
		}

		public synchronized void clearPublished() {
			publishedBundle = null;
			if (tmpCodeInfo == null) {
				cached = false;
			}
		}

		public synchronized boolean invalidate() {
			boolean wasCached = cached;
			writeGeneration++;
			cached = false;
			tmpCodeInfo = null;
			publishedBundle = null;
			return wasCached;
		}

		public @Nullable ICodeInfo getTmpCodeInfo() {
			return tmpCodeInfo;
		}

		public @Nullable String getPublishedBundle() {
			return publishedBundle;
		}
	}

	private static final class WriteRequest {
		private final long generation;
		private final String bundleName;

		private WriteRequest(long generation, String bundleName) {
			this.generation = generation;
			this.bundleName = bundleName;
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
