package jadx.plugins.input.dex;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jadx.api.plugins.utils.CommonFileUtils;
import jadx.core.utils.files.FileUtils;
import jadx.plugins.input.dex.sections.DexConsts;
import jadx.plugins.input.dex.sections.DexHeaderV41;
import jadx.plugins.input.dex.utils.DexCheckSum;
import jadx.zip.IZipEntry;
import jadx.zip.ZipContent;
import jadx.zip.ZipReader;

public class DexFileLoader {
	private static final Logger LOG = LoggerFactory.getLogger(DexFileLoader.class);

	// sharing between all instances (can be used in other plugins) // TODO:
	private static int dexUniqId = 1;

	private final DexInputOptions options;

	private ZipReader zipReader = new ZipReader();
	private @Nullable LoadErrorHandler loadErrorHandler;
	private @Nullable LoadErrorHandler loadExclusionHandler;

	@FunctionalInterface
	public interface LoadErrorHandler {
		void accept(String category, String message, Throwable error);
	}

	public DexFileLoader(DexInputOptions options) {
		this.options = options;
	}

	public void setZipReader(ZipReader zipReader) {
		this.zipReader = zipReader;
	}

	public void setLoadErrorHandler(LoadErrorHandler loadErrorHandler) {
		this.loadErrorHandler = loadErrorHandler;
	}

	public void setLoadExclusionHandler(LoadErrorHandler loadExclusionHandler) {
		this.loadExclusionHandler = loadExclusionHandler;
	}

	public List<DexReader> collectDexFiles(List<Path> pathsList) {
		return pathsList.stream()
				.map(Path::toFile)
				.map(this::loadDexFromFile)
				.filter(list -> !list.isEmpty())
				.flatMap(Collection::stream)
				.peek(dr -> LOG.debug("Loading dex: {}", dr))
				.collect(Collectors.toList());
	}

	private List<DexReader> loadDexFromFile(File file) {
		try (InputStream inputStream = new FileInputStream(file)) {
			return load(file, inputStream, file.getAbsolutePath());
		} catch (Exception e) {
			String category = file.getName().toLowerCase(java.util.Locale.ROOT).endsWith(".dex")
					? "input-load.top-level-dex"
					: "input-load.archive";
			reportLoadError(category, "Input load failed: provenance=TOP_LEVEL, file="
					+ file.getAbsolutePath() + fingerprintFile(file), e);
			return Collections.emptyList();
		}
	}

	private List<DexReader> load(@Nullable File file, InputStream inputStream, String fileName) throws IOException {
		try (InputStream in = inputStream.markSupported() ? inputStream : new BufferedInputStream(inputStream)) {
			byte[] magic = new byte[DexConsts.MAX_MAGIC_SIZE];
			in.mark(magic.length);
			if (in.read(magic) != magic.length) {
				return Collections.emptyList();
			}
			if (isStartWithBytes(magic, DexConsts.DEX_FILE_MAGIC)) {
				in.reset();
				byte[] content = readAllBytes(in);
				return loadDexReaders(fileName, content);
			}
			if (fileName.endsWith(".dex")) {
				// report invalid magic in '.dex' file
				String hex = FileUtils.bytesToHex(magic);
				String str = new String(magic, StandardCharsets.US_ASCII);
				LOG.warn("Invalid DEX magic: 0x{}(\"{}\") in file: {}", hex, str, fileName);
			}
			if (file != null) {
				// allow only top level zip files
				if (isStartWithBytes(magic, DexConsts.ZIP_FILE_MAGIC) || CommonFileUtils.isZipFileExt(fileName)) {
					return collectDexFromZip(file);
				}
			}
			return Collections.emptyList();
		}
	}

	private List<DexReader> loadFromZipEntry(byte[] content, String fileName) {
		if (isStartWithBytes(content, DexConsts.DEX_FILE_MAGIC) || fileName.endsWith(".dex")) {
			return loadDexReaders(fileName, content);
		}
		return Collections.emptyList();
	}

	public List<DexReader> loadDexReaders(String fileName, byte[] content) {
		DexHeaderV41 dexHeaderV41 = DexHeaderV41.readIfPresent(content);
		if (dexHeaderV41 != null) {
			return DexHeaderV41.readSubDexOffsets(content, dexHeaderV41)
					.stream()
					.map(offset -> loadSingleDex(fileName, content, offset))
					.collect(Collectors.toList());
		}
		DexReader dexReader = loadSingleDex(fileName, content, 0);
		return Collections.singletonList(dexReader);
	}

	private DexReader loadSingleDex(String fileName, byte[] content, int offset) {
		if (options.isVerifyChecksum()) {
			DexCheckSum.verify(fileName, content, offset);
		}
		return new DexReader(getNextUniqId(), fileName, content, offset);
	}

	/**
	 * Since DEX v41, several sub DEX structures can be stored inside container of a single DEX file
	 * Use {@link DexFileLoader#loadDexReaders(String, byte[])} instead.
	 */
	@Deprecated
	public DexReader loadDexReader(String fileName, byte[] content) {
		return loadSingleDex(fileName, content, 0);
	}

	private List<DexReader> collectDexFromZip(File file) {
		List<DexReader> result = new ArrayList<>();
		try (ZipContent zip = zipReader.open(file)) {
			for (IZipEntry entry : zip.getEntries()) {
				if (entry.isDirectory()) {
					continue;
				}
				byte[] dexContent = null;
				try {
					List<DexReader> readers;
					if (entry.preferBytes()) {
						dexContent = entry.getBytes();
						readers = loadFromZipEntry(dexContent, entry.getName());
					} else {
						readers = load(null, entry.getInputStream(), entry.getName());
					}
					if (!readers.isEmpty()) {
						result.addAll(readers);
					}
				} catch (Exception e) {
					if (isDexEntry(entry.getName())) {
						if (dexContent == null) {
							try {
								dexContent = entry.getBytes();
							} catch (Exception readError) {
								e.addSuppressed(readError);
							}
						}
						if (dexContent != null) {
							reportZipDexFailure(file, entry, dexContent, e);
						} else {
							reportZipDexFailureWithoutFingerprint(file, entry, e);
						}
					} else {
						reportLoadError("input-load.archive",
								"Input load failed: provenance=ARCHIVE_ENTRY, entry=" + entry, e);
					}
				}
			}
		} catch (Exception e) {
			reportLoadError("input-load.archive", "Input load failed: provenance=ARCHIVE, file="
					+ file.getAbsolutePath(), e);
		}
		return result;
	}

	private void reportZipDexFailure(File file, IZipEntry entry, byte[] content, Exception error) {
		String entryName = entry.getName().replace('\\', '/');
		boolean rootDex = entryName.matches("classes(?:[0-9]+)?\\.dex");
		String provenance = rootDex ? "APK_ROOT_DEX" : "EMBEDDED_DEX";
		String category = rootDex ? "input-load.apk-root-dex" : "input-load.embedded-dex";
		String sha256 = sha256(content);
		String message = "Input load failed: provenance=" + provenance
				+ ", sha256=" + sha256 + ", apk=" + file.getAbsolutePath() + ", entry=" + entryName;
		if (!rootDex && options.isAuditExcluded(sha256)) {
			LoadErrorHandler handler = loadExclusionHandler;
			if (handler == null) {
				LOG.warn("Audited analysis exclusion [{}]: {}", category, message, error);
			} else {
				handler.accept(category, message, error);
			}
			return;
		}
		reportLoadError(category, message, error);
	}

	private void reportZipDexFailureWithoutFingerprint(File file, IZipEntry entry, Exception error) {
		String entryName = entry.getName().replace('\\', '/');
		boolean rootDex = entryName.matches("classes(?:[0-9]+)?\\.dex");
		String provenance = rootDex ? "APK_ROOT_DEX" : "EMBEDDED_DEX";
		String category = rootDex ? "input-load.apk-root-dex" : "input-load.embedded-dex";
		reportLoadError(category, "Input load failed: provenance=" + provenance
				+ ", sha256=unavailable, apk=" + file.getAbsolutePath() + ", entry=" + entryName, error);
	}

	private void reportLoadError(String category, String message, Throwable error) {
		LoadErrorHandler handler = loadErrorHandler;
		if (handler == null) {
			LOG.error(message, error);
		} else {
			handler.accept(category, message, error);
		}
	}

	private static boolean isDexEntry(String name) {
		return name.toLowerCase(java.util.Locale.ROOT).endsWith(".dex");
	}

	private static String fingerprintFile(File file) {
		if (!file.getName().toLowerCase(java.util.Locale.ROOT).endsWith(".dex")) {
			return "";
		}
		try (InputStream input = new FileInputStream(file)) {
			return ", sha256=" + sha256(readAllBytes(input));
		} catch (Exception ignored) {
			return ", sha256=unavailable";
		}
	}

	private static String sha256(byte[] content) {
		try {
			byte[] digest = MessageDigest.getInstance("SHA-256").digest(content);
			StringBuilder hex = new StringBuilder(digest.length * 2);
			for (byte value : digest) {
				hex.append(String.format("%02x", value & 0xff));
			}
			return hex.toString();
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("SHA-256 is unavailable", e);
		}
	}

	private static boolean isStartWithBytes(byte[] fileMagic, byte[] expectedBytes) {
		int len = expectedBytes.length;
		if (fileMagic.length < len) {
			return false;
		}
		for (int i = 0; i < len; i++) {
			if (fileMagic[i] != expectedBytes[i]) {
				return false;
			}
		}
		return true;
	}

	private static byte[] readAllBytes(InputStream in) throws IOException {
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		byte[] data = new byte[8192];
		while (true) {
			int read = in.read(data);
			if (read == -1) {
				break;
			}
			buf.write(data, 0, read);
		}
		return buf.toByteArray();
	}

	private static synchronized int getNextUniqId() {
		dexUniqId++;
		if (dexUniqId >= 0xFFFF) {
			dexUniqId = 1;
		}
		return dexUniqId;
	}

	private static synchronized void resetDexUniqId() {
		dexUniqId = 1;
	}
}
