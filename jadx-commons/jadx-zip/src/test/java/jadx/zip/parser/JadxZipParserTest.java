package jadx.zip.parser;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import jadx.zip.ZipContent;
import jadx.zip.ZipReaderOptions;

import static org.assertj.core.api.Assertions.assertThat;

class JadxZipParserTest {
	@TempDir
	Path tempDir;

	@Test
	void releasesMappedZipBeforeTempCleanup() throws Exception {
		Path zip = tempDir.resolve("mapped.apk");
		try (OutputStream output = Files.newOutputStream(zip);
				ZipOutputStream zipOutput = new ZipOutputStream(output)) {
			zipOutput.putNextEntry(new ZipEntry("classes.dex"));
			zipOutput.write("not-a-real-dex".getBytes(StandardCharsets.UTF_8));
			zipOutput.closeEntry();
		}

		JadxZipParser parser = new JadxZipParser(zip.toFile(), ZipReaderOptions.getDefault(), 0);
		try (ZipContent content = parser.open()) {
			assertThat(content.getEntries()).hasSize(1);
		}

		// On Windows this delete fails while the mapped ByteBuffer still owns the file handle.
		assertThat(Files.deleteIfExists(zip)).isTrue();
	}
}
