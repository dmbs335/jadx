package jadx.core.utils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import jadx.api.JadxArgs;
import jadx.api.JadxDecompiler;
import jadx.core.utils.files.FileUtils;

import static org.assertj.core.api.Assertions.assertThat;

class AnalysisFingerprintTest {
	@TempDir
	Path tempDir;

	@Test
	void inputContentChangeInvalidatesEvenWithPreservedTimestamp() throws Exception {
		Path input = tempDir.resolve("sample.apk");
		Files.writeString(input, "aaaa");
		FileTime timestamp = Files.getLastModifiedTime(input);
		String before = FileUtils.buildInputsContentHash(List.of(input));

		Files.writeString(input, "bbbb");
		Files.setLastModifiedTime(input, timestamp);
		String after = FileUtils.buildInputsContentHash(List.of(input));

		assertThat(after).isNotEqualTo(before);
	}

	@Test
	void inputOrderIsPartOfSemanticIdentity() throws Exception {
		Path first = tempDir.resolve("base.apk");
		Path second = tempDir.resolve("feature.apk");
		Files.writeString(first, "base");
		Files.writeString(second, "feature");

		assertThat(FileUtils.buildInputsContentHash(List.of(first, second)))
				.isNotEqualTo(FileUtils.buildInputsContentHash(List.of(second, first)));
	}

	@Test
	void parentDirectoryIsNotPartOfSemanticIdentity() throws Exception {
		Path firstDir = tempDir.resolve("first");
		Path secondDir = tempDir.resolve("second");
		Files.createDirectories(firstDir);
		Files.createDirectories(secondDir);
		Path first = firstDir.resolve("sample.apk");
		Path second = secondDir.resolve("sample.apk");
		Files.writeString(first, "same-content");
		Files.writeString(second, "same-content");

		assertThat(FileUtils.buildInputsContentHash(List.of(first)))
				.isEqualTo(FileUtils.buildInputsContentHash(List.of(second)));
	}

	@Test
	void codeOptionsInvalidateAnalysisFingerprint() throws Exception {
		Path input = tempDir.resolve("sample.dex");
		Files.writeString(input, "dex-content");
		try (JadxArgs args = new JadxArgs()) {
			args.getInputFiles().add(input.toFile());
			String before = AnalysisFingerprint.build(args, null);
			args.setUseImports(false);
			String after = AnalysisFingerprint.build(args, null);

			assertThat(after).startsWith("af2:").isNotEqualTo(before);
		}
	}

	@Test
	void reloadPassesInvalidatesCachedFingerprint() throws Exception {
		Path input = tempDir.resolve("empty.jar");
		try (ZipOutputStream ignored = new ZipOutputStream(Files.newOutputStream(input))) {
			// A valid empty archive is sufficient to initialize the decompiler lifecycle.
		}
		try (JadxArgs args = new JadxArgs();
				JadxDecompiler decompiler = new JadxDecompiler(args)) {
			args.getInputFiles().add(input.toFile());
			decompiler.load();
			String before = decompiler.getAnalysisFingerprint();

			args.setUseImports(!args.isUseImports());
			decompiler.reloadPasses();

			assertThat(decompiler.getAnalysisFingerprint()).isNotEqualTo(before);
		}
	}

	@Test
	void passReloadReusesLoadedInputIdentity() throws Exception {
		Path input = tempDir.resolve("loaded.jar");
		try (ZipOutputStream ignored = new ZipOutputStream(Files.newOutputStream(input))) {
			// valid empty archive
		}
		try (JadxArgs args = new JadxArgs();
				JadxDecompiler decompiler = new JadxDecompiler(args)) {
			args.getInputFiles().add(input.toFile());
			decompiler.load();
			String before = decompiler.getAnalysisFingerprint();
			Files.delete(input);

			args.setUseImports(!args.isUseImports());
			decompiler.reloadPasses();

			assertThat(decompiler.getAnalysisFingerprint()).isNotEqualTo(before);
		}
	}

	@Test
	void runtimeContentChangeInvalidatesEvenWithPreservedTimestamp() throws Exception {
		Path runtimeDir = tempDir.resolve("runtime");
		Files.createDirectories(runtimeDir);
		Path classFile = runtimeDir.resolve("Engine.class");
		Files.writeString(classFile, "1111");
		FileTime timestamp = Files.getLastModifiedTime(classFile);
		String before = AnalysisFingerprint.buildRuntimeHash(List.of(runtimeDir));

		Files.writeString(classFile, "2222");
		Files.setLastModifiedTime(classFile, timestamp);
		String after = AnalysisFingerprint.buildRuntimeHash(List.of(runtimeDir));

		assertThat(after).isNotEqualTo(before);
	}

	@Test
	void persistentArchiveHashHitStillDetectsPreservedMetadataChange() throws Exception {
		Path archive = tempDir.resolve("sample.apk");
		Path index = tempDir.resolve("hash-index.properties");
		writeStoredArchive(archive, "aaaa");
		FileTime timestamp = Files.getLastModifiedTime(archive);
		byte[] first;
		try (AnalysisHashIndex hashIndex = AnalysisHashIndex.open(index)) {
			first = hashIndex.hash(archive);
			assertThat(hashIndex.getMissCount()).isEqualTo(1);
		}
		try (AnalysisHashIndex hashIndex = AnalysisHashIndex.open(index)) {
			assertThat(hashIndex.hash(archive)).isEqualTo(first);
			assertThat(hashIndex.getHitCount()).isEqualTo(1);
		}

		writeStoredArchive(archive, "bbbb");
		Files.setLastModifiedTime(archive, timestamp);
		try (AnalysisHashIndex hashIndex = AnalysisHashIndex.open(index)) {
			assertThat(hashIndex.hash(archive)).isNotEqualTo(first);
			assertThat(hashIndex.getHitCount()).isZero();
			assertThat(hashIndex.getMissCount()).isEqualTo(1);
		}
	}

	@Test
	void persistentArchiveHashDetectsPayloadChangeWithPreservedDirectory() throws Exception {
		Path archive = tempDir.resolve("payload-change.apk");
		Path index = tempDir.resolve("payload-change-index.properties");
		writeStoredArchive(archive, "aaaa");
		FileTime timestamp = Files.getLastModifiedTime(archive);
		byte[] first;
		try (AnalysisHashIndex hashIndex = AnalysisHashIndex.open(index)) {
			first = hashIndex.hash(archive);
		}

		byte[] archiveBytes = Files.readAllBytes(archive);
		byte[] payload = "aaaa".getBytes(java.nio.charset.StandardCharsets.UTF_8);
		int payloadOffset = indexOf(archiveBytes, payload);
		assertThat(payloadOffset).isGreaterThanOrEqualTo(0);
		System.arraycopy("bbbb".getBytes(java.nio.charset.StandardCharsets.UTF_8), 0,
				archiveBytes, payloadOffset, payload.length);
		Files.write(archive, archiveBytes);
		Files.setLastModifiedTime(archive, timestamp);

		try (AnalysisHashIndex hashIndex = AnalysisHashIndex.open(index)) {
			assertThat(hashIndex.hash(archive)).isNotEqualTo(first);
			assertThat(hashIndex.getHitCount()).isZero();
			assertThat(hashIndex.getMissCount()).isEqualTo(1);
		}
	}

	@Test
	void nonArchiveAlwaysUsesExactFullHash() throws Exception {
		Path dex = tempDir.resolve("classes.dex");
		Path index = tempDir.resolve("hash-index.properties");
		Files.writeString(dex, "dex-content");
		try (AnalysisHashIndex hashIndex = AnalysisHashIndex.open(index)) {
			hashIndex.hash(dex);
			hashIndex.hash(dex);
			assertThat(hashIndex.getHitCount()).isZero();
			assertThat(hashIndex.getMissCount()).isEqualTo(2);
		}
	}

	@Test
	void corruptPersistentHashEntryFallsBackToFullHash() throws Exception {
		Path archive = tempDir.resolve("corrupt-index.apk");
		Path index = tempDir.resolve("corrupt-index.properties");
		writeStoredArchive(archive, "content");
		byte[] expected;
		try (AnalysisHashIndex hashIndex = AnalysisHashIndex.open(index)) {
			expected = hashIndex.hash(archive);
		}

		Properties properties = new Properties();
		try (java.io.InputStream input = Files.newInputStream(index)) {
			properties.load(input);
		}
		String entryName = properties.stringPropertyNames().stream()
				.filter(name -> name.startsWith("entry."))
				.findFirst()
				.orElseThrow();
		String[] parts = properties.getProperty(entryName).split("\t", -1);
		parts[4] = "z".repeat(64);
		properties.setProperty(entryName, String.join("\t", parts));
		try (java.io.OutputStream output = Files.newOutputStream(index)) {
			properties.store(output, "corrupt test entry");
		}

		try (AnalysisHashIndex hashIndex = AnalysisHashIndex.open(index)) {
			assertThat(hashIndex.hash(archive)).isEqualTo(expected);
			assertThat(hashIndex.getHitCount()).isZero();
			assertThat(hashIndex.getMissCount()).isEqualTo(1);
		}
	}

	@Test
	void concurrentWritersMergeTheirEntries() throws Exception {
		Path firstArchive = tempDir.resolve("first.apk");
		Path secondArchive = tempDir.resolve("second.apk");
		Path index = tempDir.resolve("shared-index.properties");
		writeStoredArchive(firstArchive, "first");
		writeStoredArchive(secondArchive, "second");

		AnalysisHashIndex first = AnalysisHashIndex.open(index);
		AnalysisHashIndex second = AnalysisHashIndex.open(index);
		try {
			first.hash(firstArchive);
			second.hash(secondArchive);
			try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
				Future<?> firstClose = executor.submit(first::close);
				Future<?> secondClose = executor.submit(second::close);
				firstClose.get();
				secondClose.get();
			}
		} finally {
			first.close();
			second.close();
		}

		try (AnalysisHashIndex verifier = AnalysisHashIndex.open(index)) {
			verifier.hash(firstArchive);
			verifier.hash(secondArchive);
			assertThat(verifier.getHitCount()).isEqualTo(2);
		}
	}

	private static void writeStoredArchive(Path archive, String content) throws Exception {
		byte[] bytes = content.getBytes(java.nio.charset.StandardCharsets.UTF_8);
		CRC32 crc = new CRC32();
		crc.update(bytes);
		ZipEntry entry = new ZipEntry("classes.dex");
		entry.setMethod(ZipEntry.STORED);
		entry.setSize(bytes.length);
		entry.setCompressedSize(bytes.length);
		entry.setCrc(crc.getValue());
		try (ZipOutputStream output = new ZipOutputStream(Files.newOutputStream(archive))) {
			output.putNextEntry(entry);
			output.write(bytes);
			output.closeEntry();
		}
	}

	private static int indexOf(byte[] bytes, byte[] target) {
		for (int i = 0; i <= bytes.length - target.length; i++) {
			boolean match = true;
			for (int j = 0; j < target.length; j++) {
				if (bytes[i + j] != target[j]) {
					match = false;
					break;
				}
			}
			if (match) {
				return i;
			}
		}
		return -1;
	}
}
