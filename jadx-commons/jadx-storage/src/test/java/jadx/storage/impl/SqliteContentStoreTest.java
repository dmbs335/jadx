package jadx.storage.impl;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.DriverManager;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import jadx.storage.api.CompactionStats;
import jadx.storage.api.ContentIndexMode;
import jadx.storage.api.IngestRequest;
import jadx.storage.api.IngestStats;
import jadx.storage.api.MaterializationMode;

import static org.assertj.core.api.Assertions.assertThat;

class SqliteContentStoreTest {
	private static final String SOURCE = "package test;\n"
			+ "class Web {\n"
			+ "  void configure(android.webkit.WebView view) {\n"
			+ "    view.addJavascriptInterface(new Object(), \"bridge\");\n"
			+ "    String endpoint = \"https://example.test/api\";\n"
			+ "  }\n"
			+ "}\n";

	@TempDir
	Path tempDir;

	@Test
	void deduplicatesIndexesAndKeepsApplicationProvenance() throws Exception {
		Path storeDir = tempDir.resolve("store");
		Path firstInput = writeFile("one.apk", "first app");
		Path secondInput = writeFile("two.apk", "second app");
		Path firstOutput = writeOutput("first-output");
		Path secondOutput = writeOutput("second-output");

		try (SqliteContentStore store = SqliteContentStore.open(storeDir)) {
			IngestStats first = store.ingest(request("one", firstInput, firstOutput, MaterializationMode.KEEP));
			IngestStats second = store.ingest(request("two", secondInput, secondOutput, MaterializationMode.KEEP));
			IngestStats repeated = store.ingest(request(
					"one", firstInput, writeOutput("first-output-repeat"), MaterializationMode.KEEP));

			assertThat(first.getArtifactCount()).isEqualTo(1);
			assertThat(first.getUniqueObjectCount()).isEqualTo(1);
			assertThat(first.getIndexedObjectCount()).isEqualTo(1);
			assertThat(first.getSecurityFactCount()).isEqualTo(2);
			assertThat(second.getUniqueObjectCount()).isZero();
			assertThat(second.getReusedObjectCount()).isEqualTo(1);
			assertThat(second.getIndexedObjectCount()).isZero();
			assertThat(second.getSecurityFactCount()).isZero();
			assertThat(repeated.getApplicationId()).isEqualTo(first.getApplicationId());
			assertThat(repeated.getReusedObjectCount()).isEqualTo(1);

			assertThat(store.search("addJavascriptInterface", 10))
					.extracting(result -> result.getApplicationName())
					.containsExactlyInAnyOrder("one", "two");
			assertThat(store.search("sources/test/Web.java", 10))
					.extracting(result -> result.getApplicationName())
					.containsExactlyInAnyOrder("one", "two");
			assertThat(store.search("We", 10))
					.extracting(result -> result.getApplicationName())
					.containsExactlyInAnyOrder("one", "two");
			assertThat(store.findSecurityFacts(first.getApplicationId(), "DANGEROUS_API", 10))
					.singleElement()
					.satisfies(result -> assertThat(result.getFact().getValue())
							.isEqualTo("android.webkit.WebView.addJavascriptInterface"));

			String objectHash = store.search("example.test", 1).get(0).getObjectHash();
			CompactionStats compaction = store.compact(1024);
			assertThat(compaction.getObjectCount()).isEqualTo(1);
			assertThat(compaction.getPackedBytes()).isEqualTo(SOURCE.getBytes(StandardCharsets.UTF_8).length);
			assertThat(compaction.getLooseBytesDeleted()).isEqualTo(compaction.getPackedBytes());
			assertThat(store.getStats().getPackedObjectCount()).isEqualTo(1);
			try (var packs = Files.walk(storeDir.resolve("packs"))) {
				assertThat(packs.filter(Files::isRegularFile).count()).isEqualTo(1);
			}

			Path materialized = tempDir.resolve("restored/Web.java");
			store.materializeObject(objectHash, materialized);
			assertThat(materialized).hasContent(SOURCE);
			Path restoredRun = tempDir.resolve("restored-run");
			assertThat(store.materializeRun(first.getRunId(), restoredRun)).isEqualTo(1);
			assertThat(restoredRun.resolve("sources/test/Web.java")).hasContent(SOURCE);

			Path packedOutput = writeOutput("packed-output");
			IngestStats packedReuse = store.ingest(request(
					"one", firstInput, packedOutput, MaterializationMode.HARD_LINK));
			assertThat(packedReuse.getReusedObjectCount()).isEqualTo(1);
			assertThat(packedReuse.getHardLinkCount() + packedReuse.getHardLinkFallbackCount()).isEqualTo(1);
			assertThat(packedOutput.resolve("sources/test/Web.java")).hasContent(SOURCE);
		}
	}

	@Test
	void hardLinkModeIsSafeToFallBack() throws Exception {
		Path output = writeOutput("hard-link-output");
		Path input = writeFile("hard-link.apk", "app");
		try (SqliteContentStore store = SqliteContentStore.open(tempDir.resolve("hard-link-store"))) {
			IngestStats stats = store.ingest(request("hard-link", input, output, MaterializationMode.HARD_LINK));
			assertThat(stats.getHardLinkCount() + stats.getHardLinkFallbackCount()).isEqualTo(1);
			assertThat(output.resolve("sources/test/Web.java")).hasContent(SOURCE);
		}
	}

	@Test
	void migratesVersionOneStoreForPackLocations() throws Exception {
		Path storeDir = tempDir.resolve("v1-store");
		Files.createDirectories(storeDir);
		Class.forName("org.sqlite.JDBC");
		String url = "jdbc:sqlite:" + storeDir.resolve("index.sqlite");
		try (var connection = DriverManager.getConnection(url); var statement = connection.createStatement()) {
			statement.execute("CREATE TABLE objects ("
					+ "hash TEXT PRIMARY KEY, size INTEGER NOT NULL, kind TEXT NOT NULL, created_at TEXT NOT NULL)");
			statement.execute("PRAGMA user_version=1");
		}
		try (SqliteContentStore ignored = SqliteContentStore.open(storeDir)) {
			// opening performs the migration
		}
		try (var connection = DriverManager.getConnection(url);
				var statement = connection.createStatement();
				var resultSet = statement.executeQuery("PRAGMA user_version")) {
			assertThat(resultSet.next()).isTrue();
			assertThat(resultSet.getInt(1)).isEqualTo(3);
		}
	}

	@Test
	void migratesVersionTwoSearchProvenanceWithoutRepeatedRuns() throws Exception {
		Path storeDir = tempDir.resolve("v2-store");
		Files.createDirectories(storeDir);
		Class.forName("org.sqlite.JDBC");
		String url = "jdbc:sqlite:" + storeDir.resolve("index.sqlite");
		try (var connection = DriverManager.getConnection(url); var statement = connection.createStatement()) {
			statement.execute("CREATE TABLE applications(id INTEGER PRIMARY KEY, display_name TEXT NOT NULL)");
			statement.execute("CREATE TABLE runs(id INTEGER PRIMARY KEY, application_id INTEGER NOT NULL)");
			statement.execute("CREATE TABLE objects(hash TEXT PRIMARY KEY)");
			statement.execute("CREATE TABLE artifacts("
					+ "run_id INTEGER NOT NULL, path TEXT NOT NULL, object_hash TEXT NOT NULL, symbol TEXT)");
			statement.execute("INSERT INTO applications VALUES(1, 'app')");
			statement.execute("INSERT INTO runs VALUES(1, 1), (2, 1)");
			statement.execute("INSERT INTO objects VALUES('hash')");
			statement.execute("INSERT INTO artifacts VALUES(1, 'sources/test/Web.java', 'hash', 'test.Web')");
			statement.execute("INSERT INTO artifacts VALUES(2, 'sources/test/Web.java', 'hash', NULL)");
			statement.execute("PRAGMA user_version=2");
		}

		try (SqliteContentStore store = SqliteContentStore.open(storeDir)) {
			assertThat(store.search("Web.java", 10))
					.singleElement()
					.satisfies(result -> assertThat(result.getApplicationName()).isEqualTo("app"));
		}
		try (var connection = DriverManager.getConnection(url); var statement = connection.createStatement()) {
			try (var resultSet = statement.executeQuery("SELECT COUNT(*) FROM search_entries")) {
				assertThat(resultSet.next()).isTrue();
				assertThat(resultSet.getInt(1)).isEqualTo(1);
			}
			try (var resultSet = statement.executeQuery("PRAGMA user_version")) {
				assertThat(resultSet.next()).isTrue();
				assertThat(resultSet.getInt(1)).isEqualTo(3);
			}
		}
	}

	private IngestRequest request(String name, Path input, Path output, MaterializationMode mode) {
		return new IngestRequest(
				name, Collections.singletonList(input), output, "jadx-dev|java|auto", mode, ContentIndexMode.FULL_TEXT);
	}

	private Path writeOutput(String name) throws Exception {
		Path source = tempDir.resolve(name).resolve("sources/test/Web.java");
		Files.createDirectories(source.getParent());
		Files.write(source, SOURCE.getBytes(StandardCharsets.UTF_8));
		return tempDir.resolve(name);
	}

	private Path writeFile(String name, String content) throws Exception {
		Path file = tempDir.resolve(name);
		Files.write(file, content.getBytes(StandardCharsets.UTF_8));
		return file;
	}
}
