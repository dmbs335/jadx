package jadx.cli;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.DriverManager;

import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

import jadx.storage.impl.SqliteContentStore;

import static org.assertj.core.api.Assertions.assertThat;

public class TestExport extends BaseCliIntegrationTest {

	@Test
	public void testBasicExport() throws Exception {
		int result = execJadxCli("samples/small.apk");
		assertThat(result).isEqualTo(0);
		assertThat(collectAllFilesInDir(outputDir))
				.map(this::pathToUniformString)
				.haveExactly(2, new Condition<>(f -> f.startsWith("sources/") && f.endsWith(".java"), "sources"))
				.haveExactly(10, new Condition<>(f -> f.startsWith("resources/"), "resources"))
				.haveExactly(1, new Condition<>(f -> f.equals("resources/AndroidManifest.xml"), "manifest"))
				.hasSize(12);
	}

	@Test
	public void testDependencyInputIsNotExported() throws Exception {
		URL dependency = getClass().getClassLoader().getResource("samples/hello.dex");
		assertThat(dependency).isNotNull();
		int result = execJadxCli(
				"samples/small.apk",
				"--dependency-input", Path.of(dependency.toURI()).toString());

		assertThat(result).isEqualTo(0);
		assertThat(collectJavaFilesInDir(outputDir)).hasSize(2);
	}

	@Test
	public void testContentStoreExport() throws Exception {
		Path storeDir = testDir.resolve("content-store");
		int result = execJadxCli("samples/small.apk", "--content-store-dir", storeDir.toString());
		assertThat(result).isEqualTo(0);
		assertThat(storeDir.resolve("index.sqlite")).isRegularFile();
		try (var objects = Files.walk(storeDir.resolve("objects"))) {
			assertThat(objects.filter(Files::isRegularFile).count()).isPositive();
		}

		assertThat(JadxCLI.execute(new String[] {
				"--content-store-dir", storeDir.toString(), "--content-store-stats"
		})).isEqualTo(0);
		assertThat(JadxCLI.execute(new String[] {
				"--content-store-dir", storeDir.toString(), "--content-store-search", "sources"
		})).isEqualTo(0);
		assertThat(JadxCLI.execute(new String[] {
				"--content-store-dir", storeDir.toString(), "--content-store-compact", "--content-store-pack-size-mib", "1"
		})).isEqualTo(0);
		assertThat(JadxCLI.execute(new String[] {
				"--content-store-dir", storeDir.toString(), "--content-store-retain-runs", "1"
		})).isEqualTo(0);
		try (SqliteContentStore store = SqliteContentStore.open(storeDir)) {
			assertThat(store.getStats().getPackedObjectCount()).isPositive();
		}
	}

	@Test
	public void testStreamingHardLinkContentStoreExport() throws Exception {
		Path storeDir = testDir.resolve("stream-content-store");
		int result = execJadxCli(
				"samples/small.apk",
				"--content-store-dir", storeDir.toString(),
				"--content-store-hardlink");
		assertThat(result).isEqualTo(0);
		long outputFiles;
		try (var files = Files.walk(outputDir)) {
			outputFiles = files.filter(Files::isRegularFile).count();
		}
		try (var connection = DriverManager.getConnection("jdbc:sqlite:" + storeDir.resolve("index.sqlite"));
				var statement = connection.createStatement()) {
			try (var rows = statement.executeQuery("SELECT COUNT(*) FROM artifacts")) {
				assertThat(rows.next()).isTrue();
				assertThat(rows.getLong(1)).isEqualTo(outputFiles);
			}
			try (var rows = statement.executeQuery("SELECT status FROM runs")) {
				assertThat(rows.next()).isTrue();
				assertThat(rows.getString(1)).isEqualTo("COMPLETE");
			}
		}
	}

	@Test
	public void testImportExistingOutputIntoContentStore() throws Exception {
		assertThat(execJadxCli("samples/small.apk")).isEqualTo(0);
		Path storeDir = testDir.resolve("import-content-store");
		URL input = getClass().getClassLoader().getResource("samples/small.apk");
		assertThat(input).isNotNull();
		long outputFiles;
		try (var files = Files.walk(outputDir)) {
			outputFiles = files.filter(Files::isRegularFile).count();
		}

		int result = JadxCLI.execute(new String[] {
				"--content-store-dir", storeDir.toString(),
				"--content-store-import-dir", outputDir.toString(),
				"--content-store-hardlink",
				Path.of(input.toURI()).toString()
		});

		assertThat(result).isEqualTo(0);
		assertThat(collectAllFilesInDir(outputDir)).hasSize((int) outputFiles);
		try (SqliteContentStore store = SqliteContentStore.open(storeDir)) {
			assertThat(store.getStats().getRunCount()).isEqualTo(1);
			assertThat(store.getStats().getObjectCount()).isPositive();
		}
		try (var connection = DriverManager.getConnection("jdbc:sqlite:" + storeDir.resolve("index.sqlite"));
				var statement = connection.createStatement();
				var rows = statement.executeQuery("SELECT COUNT(*) FROM artifacts")) {
			assertThat(rows.next()).isTrue();
			assertThat(rows.getLong(1)).isEqualTo(outputFiles);
		}
	}

	@Test
	public void testGradleExportApk() throws Exception {
		int result = execJadxCli("samples/small.apk", "--export-gradle");
		assertThat(result).isEqualTo(0);
		assertThat(collectAllFilesInDir(outputDir))
				.describedAs("check output files")
				.map(this::pathToUniformString)
				.haveExactly(2, new Condition<>(f -> f.endsWith(".java"), "java classes"))
				.haveExactly(0, new Condition<>(f -> f.endsWith("classes.dex"), "dex files"))
				.hasSize(15);
	}

	@Test
	public void testGradleExportAAR() throws Exception {
		int result = execJadxCli("samples/test-lib.aar", "--export-gradle");
		assertThat(result).isEqualTo(0);
		assertThat(collectAllFilesInDir(outputDir))
				.describedAs("check output files")
				.map(this::printFileContent)
				.map(this::pathToUniformString)
				.haveExactly(1, new Condition<>(f -> f.startsWith("lib/src/main/java/") && f.endsWith(".java"), "java"))
				.haveExactly(0, new Condition<>(f -> f.endsWith(".jar"), "jar files"))
				.hasSize(8);
	}

	@Test
	public void testGradleExportSimpleJava() throws Exception {
		int result = execJadxCli("samples/HelloWorld.class", "--export-gradle");
		assertThat(result).isEqualTo(0);
		assertThat(collectAllFilesInDir(outputDir))
				.describedAs("check output files")
				.map(this::printFileContent)
				.map(this::pathToUniformString)
				.haveExactly(1, new Condition<>(f -> f.endsWith(".java") && f.startsWith("app/src/main/java/"), "java"))
				.haveExactly(0, new Condition<>(f -> f.endsWith(".class"), "class files"))
				.haveExactly(1, new Condition<>(f -> f.equals("settings.gradle.kts"), "settings"))
				.haveExactly(1, new Condition<>(f -> f.equals("app/build.gradle.kts"), "build"))
				.hasSize(3);
	}

	@Test
	public void testGradleExportInvalidType() throws Exception {
		int result = execJadxCli("samples/HelloWorld.class", "--export-gradle-type", "android-app");
		assertThat(result).isEqualTo(0);
		// expect output in 'android-app' template, but most fields will be set to UNKNOWN.
		assertThat(collectAllFilesInDir(outputDir))
				.describedAs("check output files")
				.map(this::printFileContent)
				.map(this::pathToUniformString)
				.haveExactly(1, new Condition<>(f -> f.endsWith(".java") && f.startsWith("app/src/main/java/"), "java"))
				.haveExactly(1, new Condition<>(f -> f.equals("settings.gradle"), "settings"))
				.haveExactly(1, new Condition<>(f -> f.equals("build.gradle"), "build"))
				.haveExactly(1, new Condition<>(f -> f.equals("app/build.gradle"), "app build"))
				.hasSize(4);
	}
}
