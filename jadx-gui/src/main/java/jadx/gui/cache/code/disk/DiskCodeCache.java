package jadx.gui.cache.code.disk;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
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
import jadx.core.utils.StringUtils;
import jadx.core.utils.Utils;
import jadx.core.utils.exceptions.JadxRuntimeException;
import jadx.core.utils.files.FileUtils;

public class DiskCodeCache implements ICodeCache {
	private static final Logger LOG = LoggerFactory.getLogger(DiskCodeCache.class);

	private static final int DATA_FORMAT_VERSION = 16;

	private final Path baseDir;
	private final Path srcDir;
	private final Path metaDir;
	private final Path codeVersionFile;
	private final String codeVersion;
	private final CodeMetadataAdapter codeMetadataAdapter;
	private final ExecutorService writePool;
	private final Map<String, CacheData> clsDataMap;

	public DiskCodeCache(RootNode root, Path projectCacheDir) {
		baseDir = projectCacheDir.resolve("code");
		srcDir = baseDir.resolve("sources");
		metaDir = baseDir.resolve("metadata");
		codeVersionFile = baseDir.resolve("code-version");
		JadxArgs args = root.getArgs();
		codeVersion = buildCodeVersion(args, root.getDecompiler());
		writePool = buildWritePool(args.getThreadsCount());
		codeMetadataAdapter = new CodeMetadataAdapter(root);
		clsDataMap = buildClassDataMap(root.getClasses());
		if (checkCodeVersion()) {
			loadCachedSet();
		} else {
			reset();
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
			if (pool.isShutdown()) {
				throw new RejectedExecutionException("Disk code cache is closed");
			}
			try {
				pool.getQueue().put(runnable);
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
			if (Files.exists(baseDir.getParent().resolve(codeVersionFile.getFileName()))) {
				// remove old version cache files
				FileUtils.deleteDirIfExists(baseDir.getParent());
			}
			FileUtils.makeDirs(srcDir);
			FileUtils.makeDirs(metaDir);
			FileUtils.writeFile(codeVersionFile, codeVersion);
			if (LOG.isDebugEnabled()) {
				LOG.info("Reset done in: {}ms", System.currentTimeMillis() - start);
			}
		} catch (Exception e) {
			throw new JadxRuntimeException("Failed to reset code cache", e);
		} finally {
			clsDataMap.values().forEach(d -> d.setCached(false));
		}
	}

	/**
	 * Async writes backed by in-memory store
	 */
	@Override
	public void add(String clsFullName, ICodeInfo codeInfo) {
		CacheData clsData = getClsData(clsFullName);
		clsData.setTmpCodeInfo(codeInfo);
		clsData.setCached(true);
		writePool.execute(() -> {
			try {
				int clsId = clsData.getClsId();
				ICodeInfo code = clsData.getTmpCodeInfo();
				if (code != null) {
					FileUtils.writeFile(getJavaFile(clsId), code.getCodeStr());
					codeMetadataAdapter.write(getMetadataFile(clsId), code.getCodeMetadata());
				}
			} catch (Exception e) {
				LOG.error("Failed to write code cache for " + clsFullName, e);
				remove(clsFullName);
			} finally {
				clsData.setTmpCodeInfo(null);
			}
		});
	}

	@Override
	public @Nullable String getCode(String clsFullName) {
		try {
			if (!contains(clsFullName)) {
				return null;
			}
			CacheData clsData = getClsData(clsFullName);
			ICodeInfo tmpCodeInfo = clsData.getTmpCodeInfo();
			if (tmpCodeInfo != null) {
				return tmpCodeInfo.getCodeStr();
			}
			Path javaFile = getJavaFile(clsData.getClsId());
			if (!Files.exists(javaFile)) {
				return null;
			}
			return FileUtils.readFile(javaFile);
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
			if (!contains(clsFullName)) {
				return ICodeInfo.EMPTY;
			}
			CacheData clsData = getClsData(clsFullName);
			ICodeInfo tmpCodeInfo = clsData.getTmpCodeInfo();
			if (tmpCodeInfo != null) {
				return tmpCodeInfo;
			}
			int clsId = clsData.getClsId();
			String code = knownCode;
			if (code == null) {
				Path javaFile = getJavaFile(clsId);
				if (!Files.exists(javaFile)) {
					return ICodeInfo.EMPTY;
				}
				code = FileUtils.readFile(javaFile);
			}
			return codeMetadataAdapter.readAndBuild(getMetadataFile(clsId), code);
		} catch (Exception e) {
			LOG.error("Failed to read code cache for {}", clsFullName, e);
			invalidateEntry(clsFullName);
			return ICodeInfo.EMPTY;
		}
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
			if (clsData.isCached()) {
				clsData.setCached(false);
				if (clsData.getTmpCodeInfo() == null) {
					LOG.debug("Removing class info from disk: {}", clsFullName);
					int clsId = clsData.getClsId();
					Files.deleteIfExists(getJavaFile(clsId));
					Files.deleteIfExists(getMetadataFile(clsId));
				} else {
					// class info not yet written to disk
					clsData.setTmpCodeInfo(null);
				}
			}
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
		BitSet cachedSet = new BitSet(clsDataMap.size());
		try (Stream<Path> stream = Files.walk(metaDir)) {
			stream.forEach(file -> {
				String fileName = file.getFileName().toString();
				if (fileName.endsWith(".jadxmd")) {
					String idStr = StringUtils.removeSuffix(fileName, ".jadxmd");
					int clsId = Integer.parseInt(idStr, 16);
					cachedSet.set(clsId);
				}
			});
		} catch (Exception e) {
			throw new JadxRuntimeException("Failed to enumerate cached classes", e);
		}
		int count = 0;
		for (CacheData data : clsDataMap.values()) {
			int clsId = data.getClsId();
			if (cachedSet.get(clsId)) {
				data.setCached(true);
				count++;
			}
		}
		LOG.info("Found {} classes in disk cache, time: {}ms, dir: {}",
				count, System.currentTimeMillis() - start, metaDir.getParent());
	}

	private Path getJavaFile(int clsId) {
		return srcDir.resolve(getPathForClsId(clsId, ".java"));
	}

	private Path getMetadataFile(int clsId) {
		return metaDir.resolve(getPathForClsId(clsId, ".jadxmd"));
	}

	private Path getPathForClsId(int clsId, String ext) {
		// all classes divided between 256 top level folders
		String firstByte = FileUtils.byteToHex(clsId);
		return Paths.get(firstByte, FileUtils.intToHex(clsId) + ext);
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
		private boolean cached;
		private @Nullable ICodeInfo tmpCodeInfo;

		public CacheData(int clsId) {
			this.clsId = clsId;
		}

		public int getClsId() {
			return clsId;
		}

		public boolean isCached() {
			return cached;
		}

		public void setCached(boolean cached) {
			this.cached = cached;
		}

		public @Nullable ICodeInfo getTmpCodeInfo() {
			return tmpCodeInfo;
		}

		public void setTmpCodeInfo(@Nullable ICodeInfo tmpCodeInfo) {
			this.tmpCodeInfo = tmpCodeInfo;
		}
	}
}
