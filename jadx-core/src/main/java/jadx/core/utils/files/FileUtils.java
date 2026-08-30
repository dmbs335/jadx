package jadx.core.utils.files;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jadx.core.plugins.files.IJadxFilesGetter;
import jadx.core.utils.ListUtils;
import jadx.core.utils.exceptions.JadxRuntimeException;

public class FileUtils {
	private static final Logger LOG = LoggerFactory.getLogger(FileUtils.class);

	public static final int READ_BUFFER_SIZE = 8 * 1024;
	private static final int MAX_FILENAME_LENGTH = 128;
	private static final int MAX_UNIQUE_ID_LENGTH = 3;

	public static final String JADX_TMP_INSTANCE_PREFIX = "jadx-instance-";
	public static final String JADX_TMP_PREFIX = "jadx-tmp-";

	private static Path tempRootDir = createTempRootDir();

	private FileUtils() {
		// utility class
	}

	public static synchronized Path updateTempRootDir(Path newTempRootDir) {
		try {
			makeDirs(newTempRootDir);
			Path dir = Files.createTempDirectory(newTempRootDir, JADX_TMP_INSTANCE_PREFIX);
			tempRootDir = dir;
			dir.toFile().deleteOnExit();
			return dir;
		} catch (Exception e) {
			throw new JadxRuntimeException("Failed to update temp root directory", e);
		}
	}

	private static Path createTempRootDir() {
		try {
			Path dir = Files.createTempDirectory(JADX_TMP_INSTANCE_PREFIX);
			dir.toFile().deleteOnExit();
			return dir;
		} catch (Exception e) {
			throw new JadxRuntimeException("Failed to create temp root directory", e);
		}
	}

	public static List<Path> listFiles(Path dir) {
		try (Stream<Path> files = Files.list(dir)) {
			return files.collect(Collectors.toList());
		} catch (IOException e) {
			throw new JadxRuntimeException("Failed to list files in directory: " + dir, e);
		}
	}

	public static List<Path> listFiles(Path dir, Predicate<? super Path> filter) {
		try (Stream<Path> files = Files.list(dir)) {
			return files.filter(filter).collect(Collectors.toList());
		} catch (IOException e) {
			throw new JadxRuntimeException("Failed to list files in directory: " + dir, e);
		}
	}

	public static List<Path> expandDirs(List<Path> paths) {
		List<Path> files = new ArrayList<>(paths.size());
		for (Path path : paths) {
			if (Files.isDirectory(path)) {
				expandDir(path, files);
			} else {
				files.add(path);
			}
		}
		return files;
	}

	private static void expandDir(Path dir, List<Path> files) {
		try (Stream<Path> walk = Files.walk(dir, FileVisitOption.FOLLOW_LINKS)) {
			walk.filter(Files::isRegularFile).forEach(files::add);
		} catch (Exception e) {
			LOG.error("Failed to list files in directory: {}", dir, e);
		}
	}

	public static void addFileToJar(JarOutputStream jar, File source, String entryName) throws IOException {
		try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(source))) {
			JarEntry entry = new JarEntry(entryName);
			entry.setTime(source.lastModified());
			jar.putNextEntry(entry);

			copyStream(in, jar);
			jar.closeEntry();
		}
	}

	public static void makeDirsForFile(Path path) {
		if (path != null) {
			makeDirs(path.toAbsolutePath().getParent().toFile());
		}
	}

	public static void makeDirsForFile(File file) {
		if (file != null) {
			makeDirs(file.getParentFile());
		}
	}

	private static final Object MKDIR_SYNC = new Object();

	public static void makeDirs(@Nullable File dir) {
		if (dir != null) {
			synchronized (MKDIR_SYNC) {
				if (!dir.mkdirs() && !dir.isDirectory()) {
					throw new JadxRuntimeException("Can't create directory " + dir);
				}
			}
		}
	}

	public static void makeDirs(@Nullable Path dir) {
		if (dir != null) {
			makeDirs(dir.toFile());
		}
	}

	public static void deleteFileIfExists(Path filePath) throws IOException {
		Files.deleteIfExists(filePath);
	}

	public static boolean deleteDir(File dir) {
		deleteDir(dir.toPath());
		return true;
	}

	public static void deleteDir(Path dir) {
		deleteDir(dir, false);
	}

	public static void deleteDirIfExists(Path dir) {
		if (Files.exists(dir)) {
			try {
				deleteDir(dir);
			} catch (Exception e) {
				LOG.error("Failed to delete dir: {}", dir.toAbsolutePath(), e);
			}
		}
	}

	private static void deleteDir(Path dir, boolean keepRootDir) {
		try {
			List<Path> files = new ArrayList<>();
			List<Path> directories = new ArrayList<>();
			Files.walkFileTree(dir, Collections.emptySet(), Integer.MAX_VALUE, new SimpleFileVisitor<>() {
				@Override
				public @NotNull FileVisitResult visitFile(@NotNull Path file, @NotNull BasicFileAttributes attrs) {
					files.add(file);
					return FileVisitResult.CONTINUE;
				}

				@Override
				public @NotNull FileVisitResult postVisitDirectory(@NotNull Path directory, IOException exc) {
					directories.add(directory);
					return FileVisitResult.CONTINUE;
				}
			});
			// delete files in parallel
			if (!files.isEmpty()) {
				files.parallelStream().forEach(path -> {
					try {
						Files.delete(path);
					} catch (Exception e) {
						LOG.warn("Failed to delete file {}", path.toAbsolutePath(), e);
					}
				});
			}
			// after all files are deleted, remove empty directories
			if (keepRootDir) {
				// root dir always last
				ListUtils.removeLast(directories);
			}
			for (Path directory : directories) {
				try {
					Files.delete(directory);
				} catch (IOException e) {
					LOG.warn("Failed to delete directory {}", directory.toAbsolutePath(), e);
				}
			}
		} catch (Exception e) {
			throw new JadxRuntimeException("Failed to delete directory " + dir, e);
		}
	}

	public static void clearTempRootDir() {
		if (Files.isDirectory(tempRootDir)) {
			clearDir(tempRootDir);
		}
	}

	public static void clearDir(Path clearDir) {
		try {
			deleteDir(clearDir, true);
		} catch (Exception e) {
			throw new JadxRuntimeException("Failed to clear directory " + clearDir, e);
		}
	}

	/**
	 * Deprecated.
	 * Migrate to {@link IJadxFilesGetter} from jadx args to get temp dir
	 */
	@Deprecated
	public static Path createTempDir(String prefix) {
		try {
			Path dir = Files.createTempDirectory(tempRootDir, prefix);
			dir.toFile().deleteOnExit();
			return dir;
		} catch (Exception e) {
			throw new JadxRuntimeException("Failed to create temp directory with suffix: " + prefix, e);
		}
	}

	/**
	 * Deprecated.
	 * Migrate to {@link IJadxFilesGetter} from jadx args to get temp dir
	 */
	@Deprecated
	public static Path createTempFile(String suffix) {
		try {
			Path path = Files.createTempFile(tempRootDir, JADX_TMP_PREFIX, suffix);
			path.toFile().deleteOnExit();
			return path;
		} catch (Exception e) {
			throw new JadxRuntimeException("Failed to create temp file with suffix: " + suffix, e);
		}
	}

	/**
	 * Deprecated.
	 * Prefer {@link IJadxFilesGetter} from jadx args to get temp dir
	 */
	@Deprecated
	public static Path createTempFileNoDelete(String suffix) {
		try {
			return Files.createTempFile(Files.createTempDirectory("jadx-persist"), "jadx-", suffix);
		} catch (Exception e) {
			throw new JadxRuntimeException("Failed to create temp file with suffix: " + suffix, e);
		}
	}

	/**
	 * Deprecated.
	 * Migrate to {@link IJadxFilesGetter} from jadx args to get temp dir
	 */
	@Deprecated
	public static Path createTempFileNonPrefixed(String fileName) {
		try {
			Path path = Files.createFile(tempRootDir.resolve(fileName));
			path.toFile().deleteOnExit();
			return path;
		} catch (Exception e) {
			throw new JadxRuntimeException("Failed to create non-prefixed temp file: " + fileName, e);
		}
	}

	public static void copyStream(InputStream input, OutputStream output) throws IOException {
		byte[] buffer = new byte[READ_BUFFER_SIZE];
		while (true) {
			int count = input.read(buffer);
			if (count == -1) {
				break;
			}
			output.write(buffer, 0, count);
		}
	}

	public static byte[] streamToByteArray(InputStream input) throws IOException {
		return input.readAllBytes();
	}

	public static String streamToString(InputStream input) throws IOException {
		return new String(streamToByteArray(input), StandardCharsets.UTF_8);
	}

	public static void close(Closeable c) {
		if (c == null) {
			return;
		}
		try {
			c.close();
		} catch (IOException e) {
			LOG.error("Close exception for {}", c, e);
		}
	}

	public static void writeFile(Path file, String data) throws IOException {
		FileUtils.makeDirsForFile(file);
		Files.writeString(file, data, StandardCharsets.UTF_8,
				StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
	}

	public static void writeFile(Path file, byte[] data) throws IOException {
		FileUtils.makeDirsForFile(file);
		Files.write(file, data, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
	}

	public static void writeFile(Path file, InputStream is) throws IOException {
		FileUtils.makeDirsForFile(file);
		Files.copy(is, file, StandardCopyOption.REPLACE_EXISTING);
	}

	public static String readFile(Path textFile) throws IOException {
		return Files.readString(textFile);
	}

	public static boolean renameFile(Path sourcePath, Path targetPath) {
		try {
			Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
			return true;
		} catch (NoSuchFileException e) {
			LOG.error("File to rename not found {}", sourcePath, e);
		} catch (FileAlreadyExistsException e) {
			LOG.error("File with that name already exists {}", targetPath, e);
		} catch (IOException e) {
			LOG.error("Error renaming file {}", e.getMessage(), e);
		}
		return false;
	}

	@NotNull
	public static File prepareFile(File file) {
		File saveFile = cutFileName(file);
		makeDirsForFile(saveFile);
		return saveFile;
	}

	public static File cutFileName(File file) {
		String name = file.getName();
		if (name.length() <= MAX_FILENAME_LENGTH) {
			return file;
		}

		String uniqueID = String.valueOf(name.hashCode());
		if (uniqueID.length() > MAX_UNIQUE_ID_LENGTH) {
			uniqueID = uniqueID.substring(0, MAX_UNIQUE_ID_LENGTH);
		}
		int dotIndex = name.indexOf('.');
		int lengthOfSuffix = name.length() - dotIndex;
		int cutAt = MAX_FILENAME_LENGTH - lengthOfSuffix - uniqueID.length() - 1;
		if (cutAt <= 0) {
			name = name.substring(0, MAX_FILENAME_LENGTH - 1);
		} else {
			name = name.substring(0, cutAt) + uniqueID + name.substring(dotIndex);
		}
		return new File(file.getParentFile(), name);
	}

	/**
	 * Convert a validated, host-independent archive path into a path that can be materialized on all
	 * supported host file systems. ZIP uses '/' as a separator and permits characters such as ':'
	 * that Windows does not. Percent escaping keeps the mapping deterministic and collision-free.
	 */
	public static String toSafeFilePath(String archivePath) {
		String normalizedPath = archivePath.replace('\\', '/');
		String[] segments = normalizedPath.split("/", -1);
		StringBuilder result = new StringBuilder(normalizedPath.length());
		for (int i = 0; i < segments.length; i++) {
			if (i != 0) {
				result.append('/');
			}
			result.append(escapeFileNameSegment(segments[i]));
		}
		return result.toString();
	}

	private static String escapeFileNameSegment(String segment) {
		StringBuilder result = new StringBuilder(segment.length());
		int lastIndex = segment.length() - 1;
		for (int i = 0; i < segment.length(); i++) {
			char ch = segment.charAt(i);
			boolean trailingDotOrSpace = i == lastIndex && (ch == '.' || ch == ' ');
			if (ch == '%' || ch < 0x20 || "<>:\"|?*".indexOf(ch) != -1 || trailingDotOrSpace) {
				appendPercentEncoded(result, ch);
			} else {
				result.append(ch);
			}
		}
		if (isWindowsReservedName(segment)) {
			String escaped = result.toString();
			result.setLength(0);
			appendPercentEncoded(result, segment.charAt(0));
			result.append(escaped, 1, escaped.length());
		}
		return result.toString();
	}

	private static boolean isWindowsReservedName(String segment) {
		if (segment.isEmpty()) {
			return false;
		}
		int end = segment.length();
		while (end > 0 && (segment.charAt(end - 1) == '.' || segment.charAt(end - 1) == ' ')) {
			end--;
		}
		String normalized = segment.substring(0, end);
		int dot = normalized.indexOf('.');
		String baseName = normalized.substring(0, dot == -1 ? normalized.length() : dot).stripTrailing()
				.toUpperCase(Locale.ROOT);
		if (baseName.equals("CON") || baseName.equals("PRN") || baseName.equals("AUX") || baseName.equals("NUL")) {
			return true;
		}
		return baseName.length() == 4
				&& (baseName.startsWith("COM") || baseName.startsWith("LPT"))
				&& baseName.charAt(3) >= '1'
				&& baseName.charAt(3) <= '9';
	}

	private static void appendPercentEncoded(StringBuilder result, char ch) {
		result.append('%');
		int value = ch;
		if (value > 0xFF) {
			result.append('u');
			for (int shift = 12; shift >= 0; shift -= 4) {
				result.append((char) HEX_ARRAY[(value >>> shift) & 0xF]);
			}
		} else {
			result.append((char) HEX_ARRAY[(value >>> 4) & 0xF]);
			result.append((char) HEX_ARRAY[value & 0xF]);
		}
	}

	private static final byte[] HEX_ARRAY = "0123456789abcdef".getBytes(StandardCharsets.US_ASCII);

	public static String bytesToHex(byte[] bytes) {
		if (bytes == null || bytes.length == 0) {
			return "";
		}
		byte[] hexChars = new byte[bytes.length * 2];
		for (int j = 0; j < bytes.length; j++) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = HEX_ARRAY[v >>> 4];
			hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
		}
		return new String(hexChars, StandardCharsets.UTF_8);
	}

	/**
	 * Zero padded hex string for first byte
	 */
	public static String byteToHex(int value) {
		int v = value & 0xFF;
		byte[] hexChars = new byte[] { HEX_ARRAY[v >>> 4], HEX_ARRAY[v & 0x0F] };
		return new String(hexChars, StandardCharsets.US_ASCII);
	}

	/**
	 * Zero padded hex string for int value
	 */
	public static String intToHex(int value) {
		byte[] hexChars = new byte[8];
		int v = value;
		for (int i = 7; i >= 0; i--) {
			hexChars[i] = HEX_ARRAY[v & 0x0F];
			v >>>= 4;
		}
		return new String(hexChars, StandardCharsets.US_ASCII);
	}

	private static final byte[] ZIP_FILE_MAGIC = { 0x50, 0x4B, 0x03, 0x04 };

	public static boolean isZipFile(File file) {
		try (InputStream is = new FileInputStream(file)) {
			int len = ZIP_FILE_MAGIC.length;
			byte[] headers = new byte[len];
			int read = is.read(headers);
			return read == len && Arrays.equals(headers, ZIP_FILE_MAGIC);
		} catch (Exception e) {
			LOG.error("Failed to read zip file: {}", file.getAbsolutePath(), e);
			return false;
		}
	}

	public static String getPathBaseName(Path file) {
		String fileName = file.getFileName().toString();
		int extEndIndex = fileName.lastIndexOf('.');
		if (extEndIndex == -1) {
			return fileName;
		}
		return fileName.substring(0, extEndIndex);
	}

	public static boolean hasExtension(Path path, String extension) {
		String fileName = path.getFileName().toString();
		return fileName.toLowerCase().endsWith(extension);
	}

	public static File toFile(String path) {
		if (path == null) {
			return null;
		}
		return new File(path);
	}

	public static List<Path> toPaths(List<File> files) {
		return files.stream().map(File::toPath).collect(Collectors.toList());
	}

	public static List<Path> toPaths(File[] files) {
		return Stream.of(files).map(File::toPath).collect(Collectors.toList());
	}

	public static List<Path> toPathsWithTrim(File[] files) {
		return Stream.of(files).map(FileUtils::toPathWithTrim).collect(Collectors.toList());
	}

	public static Path toPathWithTrim(File file) {
		return toPathWithTrim(file.getPath());
	}

	public static Path toPathWithTrim(String file) {
		return Path.of(file.trim());
	}

	public static List<Path> fileNamesToPaths(List<String> fileNames) {
		return fileNames.stream().map(Paths::get).collect(Collectors.toList());
	}

	public static List<File> toFiles(List<Path> paths) {
		return paths.stream().map(Path::toFile).collect(Collectors.toList());
	}

	public static String md5Sum(String str) {
		return md5Sum(str.getBytes(StandardCharsets.UTF_8));
	}

	public static String md5Sum(byte[] data) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(data);
			return bytesToHex(md.digest());
		} catch (Exception e) {
			throw new JadxRuntimeException("Failed to build hash", e);
		}
	}

	public static String sha256Sum(String str) {
		return sha256Sum(str.getBytes(StandardCharsets.UTF_8));
	}

	public static String sha256Sum(byte[] data) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(data);
			return bytesToHex(md.digest());
		} catch (Exception e) {
			throw new JadxRuntimeException("Failed to build SHA-256 hash", e);
		}
	}

	public static String sha256Sum(Path file) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] buffer = new byte[64 * 1024];
			try (InputStream input = Files.newInputStream(file)) {
				int read;
				while ((read = input.read(buffer)) != -1) {
					md.update(buffer, 0, read);
				}
			}
			return bytesToHex(md.digest());
		} catch (Exception e) {
			throw new JadxRuntimeException("Failed to build SHA-256 hash for: " + file, e);
		}
	}

	/**
	 * Exact, order-sensitive content hash for semantic cache invalidation.
	 *
	 * <p>
	 * Unlike {@link #buildInputsHash(List)}, this method never relies on timestamps. Top-level
	 * input name and directory-relative entry names are included because input order and split roles
	 * can affect generated output. Parent directories are intentionally excluded so moving an
	 * unchanged input does not invalidate reusable results.
	 * </p>
	 */
	public static String buildInputsContentHash(List<Path> inputPaths) {
		return buildInputsContentHash(inputPaths, null);
	}

	/**
	 * Build the same path/order-sensitive aggregate while substituting a verified SHA-256 for each
	 * file body. The provider is responsible for returning exactly 32 bytes. File size is checked
	 * before and after the lookup so a concurrent replacement cannot silently enter the aggregate.
	 */
	public static String buildInputsContentHash(
			List<Path> inputPaths, @Nullable FileContentHashProvider hashProvider) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			updateDigestInt(md, inputPaths.size());
			for (int i = 0; i < inputPaths.size(); i++) {
				Path input = inputPaths.get(i).toAbsolutePath().normalize();
				updateDigestInt(md, i);
				Path fileName = input.getFileName();
				updateDigestString(md, fileName == null ? "" : fileName.toString());
				if (Files.isDirectory(input)) {
					updateDigestString(md, "directory");
					List<Path> files;
					try (Stream<Path> walk = Files.walk(input)) {
						files = walk.filter(Files::isRegularFile)
								.sorted((first, second) -> input.relativize(first).toString()
										.compareTo(input.relativize(second).toString()))
								.collect(Collectors.toList());
					}
					updateDigestInt(md, files.size());
					for (Path file : files) {
						updateDigestString(md, input.relativize(file).toString());
						updateDigestFile(md, file, hashProvider);
					}
				} else {
					updateDigestString(md, "file");
					updateDigestFile(md, input, hashProvider);
				}
			}
			return bytesToHex(md.digest());
		} catch (Exception e) {
			throw new JadxRuntimeException("Failed to build content hash for inputs", e);
		}
	}

	private static void updateDigestFile(MessageDigest md, Path file) throws IOException {
		updateDigestFile(md, file, null);
	}

	private static void updateDigestFile(
			MessageDigest md, Path file, @Nullable FileContentHashProvider hashProvider) throws IOException {
		BasicFileAttributes before = Files.readAttributes(file, BasicFileAttributes.class);
		updateDigestLong(md, before.size());
		if (hashProvider == null) {
			byte[] buffer = new byte[64 * 1024];
			try (InputStream input = Files.newInputStream(file)) {
				int read;
				while ((read = input.read(buffer)) != -1) {
					md.update(buffer, 0, read);
				}
			}
		} else {
			byte[] contentHash = hashProvider.hash(file);
			if (contentHash.length != 32) {
				throw new IOException("Expected a SHA-256 content hash for: " + file);
			}
			md.update(contentHash);
		}
		BasicFileAttributes after = Files.readAttributes(file, BasicFileAttributes.class);
		if (before.size() != after.size()
				|| !before.lastModifiedTime().equals(after.lastModifiedTime())
				|| !java.util.Objects.equals(before.fileKey(), after.fileKey())) {
			throw new IOException("Input changed while hashing: " + file);
		}
	}

	private static void updateDigestString(MessageDigest md, String value) {
		byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
		updateDigestInt(md, bytes.length);
		md.update(bytes);
	}

	private static void updateDigestInt(MessageDigest md, int value) {
		md.update((byte) (value >>> 24));
		md.update((byte) (value >>> 16));
		md.update((byte) (value >>> 8));
		md.update((byte) value);
	}

	private static void updateDigestLong(MessageDigest md, long value) {
		updateDigestInt(md, (int) (value >>> 32));
		updateDigestInt(md, (int) value);
	}

	/**
	 * Hash timestamps of input files
	 */
	public static String buildInputsHash(List<Path> inputPaths) {
		try (ByteArrayOutputStream bout = new ByteArrayOutputStream();
				DataOutputStream data = new DataOutputStream(bout)) {
			List<Path> inputFiles = FileUtils.expandDirs(inputPaths);
			Collections.sort(inputFiles);
			data.write(inputPaths.size());
			data.write(inputFiles.size());
			for (Path inputFile : inputFiles) {
				FileTime modifiedTime = Files.getLastModifiedTime(inputFile);
				data.writeLong(modifiedTime.toMillis());
			}
			return FileUtils.md5Sum(bout.toByteArray());
		} catch (Exception e) {
			throw new JadxRuntimeException("Failed to build hash for inputs", e);
		}
	}

	@FunctionalInterface
	public interface FileContentHashProvider {
		byte[] hash(Path file) throws IOException;
	}
}
