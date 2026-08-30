package jadx.core.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jadx.core.utils.exceptions.JadxRuntimeException;
import jadx.core.utils.files.FileUtils;

/**
 * Persistent exact-hash index guarded by a cheap semantic archive digest.
 *
 * <p>
 * Only valid ZIP-family inputs can hit the cache. Every entry's central-directory identity,
 * order, CRC and sizes are included, along with the raw central directory and APK signing block.
 * Non-archives, ZIP64 layouts and malformed archives deliberately fall back to a full SHA-256.
 * </p>
 */
final class AnalysisHashIndex implements AutoCloseable {
	private static final Logger LOG = LoggerFactory.getLogger(AnalysisHashIndex.class);
	private static final int FORMAT_VERSION = 1;
	private static final int MAX_ENTRIES = 4_096;
	private static final int EOCD_MAX_SIZE = 65_557;
	private static final byte[] APK_SIG_BLOCK_MAGIC = "APK Sig Block 42".getBytes(StandardCharsets.US_ASCII);
	private static final String CACHE_PATH_PROPERTY = "jadx.analysis.hash-index";

	private final Path indexPath;
	private final Map<String, CacheEntry> entries = new LinkedHashMap<>();
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
		String archiveDigest = buildArchiveDigest(normalized, attributes.size());
		if (archiveDigest == null) {
			missCount++;
			byte[] hash = hexToBytes(FileUtils.sha256Sum(normalized));
			ensureUnchanged(normalized, attributes);
			return hash;
		}
		ensureUnchanged(normalized, attributes);
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
		String contentHash = FileUtils.sha256Sum(normalized);
		ensureUnchanged(normalized, attributes);
		if (!entries.containsKey(key) && entries.size() >= MAX_ENTRIES) {
			Iterator<String> iterator = entries.keySet().iterator();
			if (iterator.hasNext()) {
				iterator.next();
				iterator.remove();
			}
		}
		entries.put(key, new CacheEntry(
				attributes.size(), modified, fileKey, archiveDigest, contentHash));
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
			Files.createDirectories(indexPath.getParent());
			Properties properties = new Properties();
			properties.setProperty("format", Integer.toString(FORMAT_VERSION));
			for (Map.Entry<String, CacheEntry> entry : entries.entrySet()) {
				properties.setProperty("entry." + entry.getKey(), entry.getValue().serialize());
			}
			Path temporary = indexPath.resolveSibling(indexPath.getFileName() + ".tmp");
			try (OutputStream output = Files.newOutputStream(
					temporary, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
				properties.store(output, "jadx verified analysis content hashes");
			}
			try {
				Files.move(temporary, indexPath,
						StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
			} catch (AtomicMoveNotSupportedException e) {
				Files.move(temporary, indexPath, StandardCopyOption.REPLACE_EXISTING);
			}
		} catch (Exception e) {
			LOG.debug("Failed to persist analysis hash index: {}", indexPath, e);
		}
	}

	private void load() {
		if (!Files.isRegularFile(indexPath)) {
			return;
		}
		try (InputStream input = Files.newInputStream(indexPath)) {
			Properties properties = new Properties();
			properties.load(input);
			if (!Integer.toString(FORMAT_VERSION).equals(properties.getProperty("format"))) {
				return;
			}
			for (String name : properties.stringPropertyNames()) {
				if (!name.startsWith("entry.") || entries.size() >= MAX_ENTRIES) {
					continue;
				}
				CacheEntry entry = CacheEntry.parse(properties.getProperty(name));
				if (entry != null) {
					entries.put(name.substring("entry.".length()), entry);
				}
			}
		} catch (Exception e) {
			LOG.debug("Ignoring unreadable analysis hash index: {}", indexPath, e);
			entries.clear();
		}
	}

	private static @Nullable String buildArchiveDigest(Path file, long fileSize) throws IOException {
		String name = file.getFileName().toString().toLowerCase(Locale.ROOT);
		if (!(name.endsWith(".apk") || name.endsWith(".xapk") || name.endsWith(".apks")
				|| name.endsWith(".aab") || name.endsWith(".jar") || name.endsWith(".zip"))) {
			return null;
		}
		try (ZipFile zip = new ZipFile(file.toFile())) {
			MessageDigest digest = sha256();
			Enumeration<? extends ZipEntry> enumeration = zip.entries();
			while (enumeration.hasMoreElements()) {
				ZipEntry entry = enumeration.nextElement();
				update(digest, entry.getName());
				update(digest, entry.getCrc());
				update(digest, entry.getSize());
				update(digest, entry.getCompressedSize());
				update(digest, entry.getMethod());
				byte[] extra = entry.getExtra();
				if (extra != null) {
					update(digest, extra.length);
					digest.update(extra);
				} else {
					update(digest, 0);
				}
			}
			if (!hashArchiveFooter(file, fileSize, digest)) {
				return null;
			}
			return FileUtils.bytesToHex(digest.digest());
		} catch (ZipException e) {
			return null;
		}
	}

	private static boolean hashArchiveFooter(Path file, long fileSize, MessageDigest digest) throws IOException {
		int tailSize = (int) Math.min(fileSize, EOCD_MAX_SIZE);
		byte[] tail = new byte[tailSize];
		try (SeekableByteChannel channel = Files.newByteChannel(file, StandardOpenOption.READ)) {
			channel.position(fileSize - tailSize);
			readFully(channel, ByteBuffer.wrap(tail));
			int eocd = findEocd(tail);
			if (eocd == -1) {
				return false;
			}
			long centralSize = uint32(tail, eocd + 12);
			long centralOffset = uint32(tail, eocd + 16);
			if (centralSize == 0xFFFF_FFFFL || centralOffset == 0xFFFF_FFFFL
					|| centralOffset + centralSize > fileSize) {
				return false;
			}
			hashRange(channel, centralOffset, fileSize - centralOffset, digest);
			hashApkSigningBlock(channel, centralOffset, digest);
			return true;
		}
	}

	private static void hashApkSigningBlock(
			SeekableByteChannel channel, long centralOffset, MessageDigest digest) throws IOException {
		if (centralOffset < 24) {
			return;
		}
		ByteBuffer footer = ByteBuffer.allocate(24).order(ByteOrder.LITTLE_ENDIAN);
		channel.position(centralOffset - 24);
		readFully(channel, footer);
		byte[] bytes = footer.array();
		for (int i = 0; i < APK_SIG_BLOCK_MAGIC.length; i++) {
			if (bytes[8 + i] != APK_SIG_BLOCK_MAGIC[i]) {
				return;
			}
		}
		long payloadSize = footer.getLong(0);
		long totalSize = payloadSize + 8;
		if (payloadSize < 24 || totalSize > centralOffset) {
			throw new IOException("Invalid APK signing block size");
		}
		hashRange(channel, centralOffset - totalSize, totalSize, digest);
	}

	private static int findEocd(byte[] tail) {
		for (int i = tail.length - 22; i >= 0; i--) {
			if (tail[i] == 0x50 && tail[i + 1] == 0x4b
					&& tail[i + 2] == 0x05 && tail[i + 3] == 0x06
					&& i + 22 + uint16(tail, i + 20) == tail.length) {
				return i;
			}
		}
		return -1;
	}

	private static long uint32(byte[] bytes, int offset) {
		return Integer.toUnsignedLong(ByteBuffer.wrap(bytes, offset, 4)
				.order(ByteOrder.LITTLE_ENDIAN).getInt());
	}

	private static int uint16(byte[] bytes, int offset) {
		return Short.toUnsignedInt(ByteBuffer.wrap(bytes, offset, 2)
				.order(ByteOrder.LITTLE_ENDIAN).getShort());
	}

	private static void hashRange(
			SeekableByteChannel channel, long offset, long length, MessageDigest digest) throws IOException {
		channel.position(offset);
		ByteBuffer buffer = ByteBuffer.allocate(64 * 1024);
		long remaining = length;
		while (remaining > 0) {
			buffer.clear();
			buffer.limit((int) Math.min(buffer.capacity(), remaining));
			int read = channel.read(buffer);
			if (read < 0) {
				throw new IOException("Unexpected end of archive");
			}
			if (read == 0) {
				throw new IOException("Unable to make progress while hashing archive");
			}
			digest.update(buffer.array(), 0, read);
			remaining -= read;
		}
	}

	private static void readFully(SeekableByteChannel channel, ByteBuffer buffer) throws IOException {
		while (buffer.hasRemaining()) {
			int read = channel.read(buffer);
			if (read < 0) {
				throw new IOException("Unexpected end of archive");
			}
			if (read == 0) {
				throw new IOException("Unable to make progress while reading archive");
			}
		}
	}

	private static MessageDigest sha256() {
		try {
			return MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			throw new JadxRuntimeException("SHA-256 is unavailable", e);
		}
	}

	private static void update(MessageDigest digest, String value) {
		byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
		update(digest, bytes.length);
		digest.update(bytes);
	}

	private static void update(MessageDigest digest, long value) {
		for (int shift = 56; shift >= 0; shift -= 8) {
			digest.update((byte) (value >>> shift));
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
