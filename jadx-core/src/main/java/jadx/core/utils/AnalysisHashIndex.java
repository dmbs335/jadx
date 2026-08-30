package jadx.core.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Base64;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jadx.core.utils.files.FileUtils;

/**
 * Persistent exact-hash index for ZIP-family analysis inputs.
 *
 * <p>
 * A cryptographic hash of the complete archive is always calculated before a persisted entry can
 * be reported as a hit. ZIP CRC and filesystem metadata are intentionally insufficient because an
 * input can preserve both while changing payload bytes. Non-archives and malformed archives are
 * deliberately treated as misses.
 * </p>
 */
final class AnalysisHashIndex implements AutoCloseable {
	private static final Logger LOG = LoggerFactory.getLogger(AnalysisHashIndex.class);
	private static final int FORMAT_VERSION = 1;
	private static final int MAX_ENTRIES = 4_096;
	private static final String CACHE_PATH_PROPERTY = "jadx.analysis.hash-index";
	private static final Map<Path, ReentrantLock> JVM_INDEX_LOCKS = new ConcurrentHashMap<>();

	private final Path indexPath;
	private final Map<String, CacheEntry> entries = new LinkedHashMap<>();
	private final Set<String> updatedKeys = new HashSet<>();
	private boolean dirty;
	private long hitCount;
	private long missCount;

	private AnalysisHashIndex(Path indexPath) {
		this.indexPath = indexPath;
		load();
	}

	static AnalysisHashIndex openDefault() {
		return new AnalysisHashIndex(resolveDefaultPath());
	}

	static AnalysisHashIndex open(Path indexPath) {
		return new AnalysisHashIndex(indexPath.toAbsolutePath().normalize());
	}

	byte[] hash(Path file) throws IOException {
		Path normalized = file.toAbsolutePath().normalize();
		BasicFileAttributes attributes = Files.readAttributes(normalized, BasicFileAttributes.class);
		String contentHash = FileUtils.sha256Sum(normalized);
		ensureUnchanged(normalized, attributes);
		String archiveDigest = validateArchive(normalized) ? contentHash : null;
		if (archiveDigest == null) {
			missCount++;
			return hexToBytes(contentHash);
		}
		String key = encode(normalized.toString());
		String fileKey = attributes.fileKey() == null ? "" : attributes.fileKey().toString();
		String modified = attributes.lastModifiedTime().toString();
		CacheEntry cached = entries.get(key);
		if (cached != null
				&& cached.size == attributes.size()
				&& cached.modified.equals(modified)
				&& cached.fileKey.equals(fileKey)
				&& cached.archiveDigest.equals(archiveDigest)) {
			hitCount++;
			return hexToBytes(cached.contentHash);
		}
		missCount++;
		if (!entries.containsKey(key) && entries.size() >= MAX_ENTRIES) {
			Iterator<String> iterator = entries.keySet().iterator();
			if (iterator.hasNext()) {
				iterator.next();
				iterator.remove();
			}
		}
		entries.put(key, new CacheEntry(
				attributes.size(), modified, fileKey, archiveDigest, contentHash));
		updatedKeys.add(key);
		dirty = true;
		return hexToBytes(contentHash);
	}

	private static void ensureUnchanged(Path file, BasicFileAttributes before) throws IOException {
		BasicFileAttributes after = Files.readAttributes(file, BasicFileAttributes.class);
		if (before.size() != after.size()
				|| !before.lastModifiedTime().equals(after.lastModifiedTime())
				|| !java.util.Objects.equals(before.fileKey(), after.fileKey())) {
			throw new IOException("Input changed while hashing: " + file);
		}
	}

	long getHitCount() {
		return hitCount;
	}

	long getMissCount() {
		return missCount;
	}

	@Override
	public void close() {
		if (!dirty) {
			return;
		}
		try {
			Path parent = indexPath.getParent();
			Files.createDirectories(parent);
			Path lockPath = indexPath.resolveSibling(indexPath.getFileName() + ".lock");
			ReentrantLock jvmLock = JVM_INDEX_LOCKS.computeIfAbsent(lockPath, key -> new ReentrantLock());
			jvmLock.lock();
			try {
				try (FileChannel lockChannel = FileChannel.open(
						lockPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
						FileLock ignored = lockChannel.lock()) {
					Map<String, CacheEntry> merged = loadEntries(indexPath);
					for (String key : updatedKeys) {
						CacheEntry entry = entries.get(key);
						if (entry != null) {
							merged.remove(key);
							merged.put(key, entry);
						}
					}
					trimToLimit(merged);
					persistEntries(parent, merged);
				}
				dirty = false;
				updatedKeys.clear();
			} finally {
				jvmLock.unlock();
			}
		} catch (Exception e) {
			LOG.debug("Failed to persist analysis hash index: {}", indexPath, e);
		}
	}

	private void load() {
		entries.putAll(loadEntries(indexPath));
	}

	private static Map<String, CacheEntry> loadEntries(Path path) {
		Map<String, CacheEntry> loaded = new LinkedHashMap<>();
		if (!Files.isRegularFile(path)) {
			return loaded;
		}
		try (InputStream input = Files.newInputStream(path)) {
			Properties properties = new Properties();
			properties.load(input);
			if (!Integer.toString(FORMAT_VERSION).equals(properties.getProperty("format"))) {
				return loaded;
			}
			for (String name : properties.stringPropertyNames()) {
				if (!name.startsWith("entry.") || loaded.size() >= MAX_ENTRIES) {
					continue;
				}
				CacheEntry entry = CacheEntry.parse(properties.getProperty(name));
				if (entry != null) {
					loaded.put(name.substring("entry.".length()), entry);
				}
			}
		} catch (Exception e) {
			LOG.debug("Ignoring unreadable analysis hash index: {}", path, e);
			loaded.clear();
		}
		return loaded;
	}

	private void persistEntries(Path parent, Map<String, CacheEntry> merged) throws IOException {
		Properties properties = new Properties();
		properties.setProperty("format", Integer.toString(FORMAT_VERSION));
		for (Map.Entry<String, CacheEntry> entry : merged.entrySet()) {
			properties.setProperty("entry." + entry.getKey(), entry.getValue().serialize());
		}
		Path temporary = Files.createTempFile(parent, indexPath.getFileName().toString(), ".tmp");
		try {
			try (OutputStream output = Files.newOutputStream(
					temporary, StandardOpenOption.TRUNCATE_EXISTING)) {
				properties.store(output, "jadx verified analysis content hashes");
			}
			try {
				Files.move(temporary, indexPath,
						StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
			} catch (AtomicMoveNotSupportedException e) {
				Files.move(temporary, indexPath, StandardCopyOption.REPLACE_EXISTING);
			}
		} finally {
			Files.deleteIfExists(temporary);
		}
	}

	private static void trimToLimit(Map<String, CacheEntry> values) {
		while (values.size() > MAX_ENTRIES) {
			Iterator<String> iterator = values.keySet().iterator();
			if (!iterator.hasNext()) {
				return;
			}
			iterator.next();
			iterator.remove();
		}
	}

	private static boolean validateArchive(Path file) throws IOException {
		String name = file.getFileName().toString().toLowerCase(Locale.ROOT);
		if (!(name.endsWith(".apk") || name.endsWith(".xapk") || name.endsWith(".apks")
				|| name.endsWith(".aab") || name.endsWith(".jar") || name.endsWith(".zip"))) {
			return false;
		}
		try (ZipFile zip = new ZipFile(file.toFile())) {
			zip.size();
			return true;
		} catch (ZipException e) {
			return false;
		}
	}

	private static byte[] hexToBytes(String hash) {
		return java.util.HexFormat.of().parseHex(hash);
	}

	private static String encode(String value) {
		return Base64.getUrlEncoder().withoutPadding()
				.encodeToString(value.getBytes(StandardCharsets.UTF_8));
	}

	private static Path resolveDefaultPath() {
		String configured = System.getProperty(CACHE_PATH_PROPERTY);
		if (configured != null && !configured.isBlank()) {
			return Path.of(configured).toAbsolutePath().normalize();
		}
		String localAppData = System.getenv("LOCALAPPDATA");
		if (localAppData != null && !localAppData.isBlank()) {
			return Path.of(localAppData, "jadx", "cache", "analysis-hashes-v1.properties");
		}
		String xdgCache = System.getenv("XDG_CACHE_HOME");
		if (xdgCache != null && !xdgCache.isBlank()) {
			return Path.of(xdgCache, "jadx", "analysis-hashes-v1.properties");
		}
		return Path.of(System.getProperty("user.home"), ".cache", "jadx", "analysis-hashes-v1.properties");
	}

	private static final class CacheEntry {
		private final long size;
		private final String modified;
		private final String fileKey;
		private final String archiveDigest;
		private final String contentHash;

		private CacheEntry(long size, String modified, String fileKey, String archiveDigest, String contentHash) {
			this.size = size;
			this.modified = modified;
			this.fileKey = fileKey;
			this.archiveDigest = archiveDigest;
			this.contentHash = contentHash;
		}

		private String serialize() {
			return size + "\t" + encode(modified) + "\t" + encode(fileKey)
					+ "\t" + archiveDigest + "\t" + contentHash;
		}

		private static @Nullable CacheEntry parse(String value) {
			try {
				String[] parts = value.split("\t", -1);
				if (parts.length != 5 || parts[3].length() != 64 || parts[4].length() != 64) {
					return null;
				}
				// Validate persisted digests while loading. A corrupt entry must degrade to a
				// cache miss, not fail a later analysis fingerprint calculation.
				hexToBytes(parts[3]);
				hexToBytes(parts[4]);
				return new CacheEntry(
						Long.parseLong(parts[0]), decode(parts[1]), decode(parts[2]), parts[3], parts[4]);
			} catch (Exception e) {
				return null;
			}
		}

		private static String decode(String value) {
			return new String(Base64.getUrlDecoder().decode(value), StandardCharsets.UTF_8);
		}
	}
}
