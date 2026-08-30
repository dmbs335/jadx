package jadx.commons.app;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class JadxTempFiles {
	private static final String JADX_TMP_INSTANCE_PREFIX = "jadx-instance-";

	private static final Path TEMP_ROOT_DIR = createTempRootDir();

	public static Path getTempRootDir() {
		return TEMP_ROOT_DIR;
	}

	private static Path createTempRootDir() {
		try {
			String jadxTmpDir = System.getenv("JADX_TMP_DIR");
			Path dir;
			if (jadxTmpDir != null) {
				Path customTmpRootDir = Paths.get(jadxTmpDir);
				Files.createDirectories(customTmpRootDir);
				dir = Files.createTempDirectory(customTmpRootDir, JADX_TMP_INSTANCE_PREFIX);
			} else {
				dir = Files.createTempDirectory(JADX_TMP_INSTANCE_PREFIX);
			}
			registerCleanup(dir);
			return dir;
		} catch (Exception e) {
			throw new RuntimeException("Failed to create temp root directory", e);
		}
	}

	private static void registerCleanup(Path dir) {
		// deleteOnExit only removes an empty directory. XAPK and plugin extraction leaves a
		// populated tree, so crashed or test JVMs used to leak hundreds of MB per launch.
		Runtime.getRuntime().addShutdownHook(new Thread(() -> deleteTree(dir), "jadx-temp-cleanup"));
	}

	private static void deleteTree(Path dir) {
		if (!Files.exists(dir)) {
			return;
		}
		try {
			Files.walkFileTree(dir, new SimpleFileVisitor<>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					Files.deleteIfExists(file);
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult postVisitDirectory(Path directory, IOException error) throws IOException {
					if (error != null) {
						throw error;
					}
					Files.deleteIfExists(directory);
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (Exception ignored) {
			// Best-effort shutdown cleanup: files can still be locked by native libraries.
		}
	}
}
