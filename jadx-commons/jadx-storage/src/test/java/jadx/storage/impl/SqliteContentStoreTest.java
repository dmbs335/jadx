package jadx.storage.impl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.time.Instant;
import java.util.Collections;
import java.util.HexFormat;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import jadx.storage.api.CompactionStats;
import jadx.storage.api.ContentIndexMode;
import jadx.storage.api.ContentIngestSession;
import jadx.storage.api.IngestRequest;
import jadx.storage.api.IngestStats;
import jadx.storage.api.MaterializationMode;
import jadx.storage.api.PruneStats;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
			assertThat(second.getUniqueObjectCount()).isZero();
			assertThat(second.getReusedObjectCount()).isEqualTo(1);
			assertThat(second.getIndexedObjectCount()).isZero();
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

			CompactionStats compaction = store.compact(1024);
			if (stats.getHardLinkCount() == 1) {
				assertThat(compaction.getObjectCount()).isZero();
				assertThat(store.getStats().getPackedObjectCount()).isZero();
			} else {
				assertThat(compaction.getObjectCount()).isEqualTo(1);
				assertThat(store.getStats().getPackedObjectCount()).isEqualTo(1);
			}
			assertThat(output.resolve("sources/test/Web.java")).hasContent(SOURCE);
		}
	}

	@Test
	void streamsFilesBeforeTheOutputTreeIsComplete() throws Exception {
		Path storeDir = tempDir.resolve("stream-store");
		Path output = tempDir.resolve("stream-output");
		Path input = writeFile("stream.apk", "app");
		IngestStats stats;
		try (SqliteContentStore store = SqliteContentStore.open(storeDir);
				WaveContentIngestSession session = new WaveContentIngestSession(
						store.beginIngest(request("stream", input, output, MaterializationMode.HARD_LINK)))) {
			for (int i = 0; i < 3; i++) {
				Path source = output.resolve("sources/test/Web" + i + ".java");
				Files.createDirectories(source.getParent());
				Files.writeString(source, SOURCE);
				session.submit(source, sha256(source), Files.size(source));
				if (i == 0) {
					session.checkpoint();
					assertThat(source).hasContent(SOURCE);
				}
			}
			stats = session.complete();
		}

		assertThat(stats.getArtifactCount()).isEqualTo(3);
		assertThat(stats.getUniqueObjectCount()).isEqualTo(1);
		assertThat(stats.getReusedObjectCount()).isEqualTo(2);
		assertThat(stats.getHardLinkCount() + stats.getHardLinkFallbackCount()).isEqualTo(3);
		for (int i = 0; i < 3; i++) {
			assertThat(output.resolve("sources/test/Web" + i + ".java")).hasContent(SOURCE);
		}
	}

	@Test
	void maintenanceCannotCommitAnActiveIngestTransaction() throws Exception {
		Path storeDir = tempDir.resolve("active-transaction-store");
		Path output = writeOutput("active-transaction-output");
		Path input = writeFile("active-transaction.apk", "app");
		try (SqliteContentStore store = SqliteContentStore.open(storeDir);
				ContentIngestSession session = store.beginIngest(
						request("active-transaction", input, output, MaterializationMode.KEEP))) {
			session.ingest(output.resolve("sources/test/Web.java"));

			assertThatThrownBy(() -> store.compact(1024))
					.isInstanceOf(IOException.class)
					.hasMessageContaining("ingest session is active");
			assertThatThrownBy(() -> store.pruneRuns(1))
					.isInstanceOf(IOException.class)
					.hasMessageContaining("ingest session is active");

			IngestStats stats = session.complete();
			assertThat(stats.getArtifactCount()).isEqualTo(1);
			assertThat(store.search("addJavascriptInterface", 10)).hasSize(1);
			assertThat(store.compact(1024).getObjectCount()).isEqualTo(1);
			assertThat(store.pruneRuns(1).getRunCount()).isZero();
		}
	}

	@Test
	void parallelImportPreservesAllArtifacts() throws Exception {
		Path input = writeFile("parallel.apk", "app");
		Path output = tempDir.resolve("parallel-output");
		for (int i = 0; i < 25; i++) {
			Path file = output.resolve("sources/test/C" + i + ".java");
			Files.createDirectories(file.getParent());
			Files.writeString(file, "class C" + i + " {}");
		}
		IngestRequest request = new IngestRequest(
				"parallel", List.of(input), output, "parallel",
				MaterializationMode.HARD_LINK, ContentIndexMode.NONE);

		try (SqliteContentStore store = SqliteContentStore.open(tempDir.resolve("parallel-store"))) {
			IngestStats stats = ParallelContentImporter.ingest(store, request, 3);
			assertThat(stats.getArtifactCount()).isEqualTo(25);
			assertThat(stats.getHardLinkCount() + stats.getHardLinkFallbackCount()).isEqualTo(25);
			assertThat(store.getStats().getObjectCount()).isEqualTo(25);
		}
		for (int i = 0; i < 25; i++) {
			assertThat(output.resolve("sources/test/C" + i + ".java"))
					.hasContent("class C" + i + " {}");
		}
	}

	@Test
	void writesOneLevelObjectShardsAndReadsLegacyLayout() throws Exception {
		Path input = writeFile("layout.apk", "app");
		Path output = writeOutput("layout-output");
		Path storeDir = tempDir.resolve("layout-store");
		String hash = sha256(output.resolve("sources/test/Web.java"));
		Path preferred = storeDir.resolve("objects").resolve(hash.substring(0, 2)).resolve(hash);
		Path legacy = preferred.getParent().resolve(hash.substring(2, 4)).resolve(hash);

		try (SqliteContentStore store = SqliteContentStore.open(storeDir)) {
			store.ingest(request("layout", input, output, MaterializationMode.KEEP));
			assertThat(preferred).isRegularFile();
			assertThat(legacy).doesNotExist();

			Files.createDirectories(legacy.getParent());
			Files.move(preferred, legacy);
			Path restored = tempDir.resolve("layout-restored.java");
			store.materializeObject(hash, restored);
			assertThat(restored).hasContent(SOURCE);

			assertThat(store.compact(1024).getObjectCount()).isEqualTo(1);
			assertThat(legacy).doesNotExist();
			assertThat(legacy.getParent()).doesNotExist();
		}
	}

	@Test
	void sharesArtifactSnapshotOnlyWhenTheCompleteManifestMatches() throws Exception {
		Path storeDir = tempDir.resolve("snapshot-store");
		Path input = writeFile("snapshot.apk", "app");
		IngestStats first;
		IngestStats repeated;
		IngestStats changed;
		try (SqliteContentStore store = SqliteContentStore.open(storeDir)) {
			first = store.ingest(request("snapshot", input, writeOutput("snapshot-first"), MaterializationMode.KEEP));
			repeated = store.ingest(request(
					"snapshot", input, writeOutput("snapshot-repeat"), MaterializationMode.KEEP));
			Path changedOutput = writeOutput("snapshot-changed");
			Files.writeString(changedOutput.resolve("sources/test/Web.java"), SOURCE + "// changed\n");
			changed = store.ingest(request("snapshot", input, changedOutput, MaterializationMode.KEEP));

			Path restored = tempDir.resolve("snapshot-restored");
			assertThat(store.materializeRun(repeated.getRunId(), restored)).isEqualTo(1);
			assertThat(restored.resolve("sources/test/Web.java")).hasContent(SOURCE);
		}

		String url = "jdbc:sqlite:" + storeDir.resolve("index.sqlite");
		try (var connection = DriverManager.getConnection(url); var statement = connection.createStatement()) {
			try (var resultSet = statement.executeQuery("SELECT COUNT(*) FROM artifacts")) {
				assertThat(resultSet.next()).isTrue();
				assertThat(resultSet.getInt(1)).isEqualTo(2);
			}
			try (var query = connection.prepareStatement(
					"SELECT artifact_source_run_id FROM runs WHERE id = ?")) {
				query.setLong(1, repeated.getRunId());
				try (var resultSet = query.executeQuery()) {
					assertThat(resultSet.next()).isTrue();
					assertThat(resultSet.getLong(1)).isEqualTo(first.getRunId());
				}
				query.setLong(1, changed.getRunId());
				try (var resultSet = query.executeQuery()) {
					assertThat(resultSet.next()).isTrue();
					assertThat(resultSet.getObject(1)).isNull();
				}
			}
		}
	}

	@Test
	void prunesOldRunsAndObjectsWithoutLeavingStaleSearchEntries() throws Exception {
		Path storeDir = tempDir.resolve("prune-store");
		Path input = writeFile("prune.apk", "app");
		try (SqliteContentStore store = SqliteContentStore.open(storeDir)) {
			store.ingest(request("prune", input, writeOutputWithSuffix("prune-one", "// old-one\n"),
					MaterializationMode.KEEP));
			store.ingest(request("prune", input, writeOutputWithSuffix("prune-two", "// old-one\n"),
					MaterializationMode.KEEP));
			store.ingest(request("prune", input, writeOutputWithSuffix("prune-three", "// old-three\n"),
					MaterializationMode.KEEP));
			IngestStats latest = store.ingest(request(
					"prune", input, writeOutputWithSuffix("prune-four", "// retained-four\n"),
					MaterializationMode.KEEP));

			PruneStats stats = store.pruneRuns(1);
			assertThat(stats.getRunCount()).isEqualTo(3);
			assertThat(stats.getObjectCount()).isEqualTo(2);
			assertThat(stats.getLooseBytesUnlinked()).isPositive();
			assertThat(stats.getPackedBytesPendingRepack()).isZero();
			assertThat(store.getStats().getRunCount()).isEqualTo(1);
			assertThat(store.getStats().getObjectCount()).isEqualTo(1);
			assertThat(store.search("old-one", 10)).isEmpty();
			assertThat(store.search("old-three", 10)).isEmpty();
			assertThat(store.search("retained-four", 10)).hasSize(1);

			Path restored = tempDir.resolve("prune-restored");
			assertThat(store.materializeRun(latest.getRunId(), restored)).isEqualTo(1);
			assertThat(restored.resolve("sources/test/Web.java")).content().contains("retained-four");
		}
	}

	@Test
	void automaticallyPrunesCommittedBatchesFromFailedRuns() throws Exception {
		Path storeDir = tempDir.resolve("failed-prune-store");
		Path input = writeFile("failed-prune.apk", "app");
		Path output = tempDir.resolve("failed-prune-output");
		Files.createDirectories(output);
		IngestRequest request = new IngestRequest(
				"failed-prune", List.of(input), output, "failed-prune",
				MaterializationMode.KEEP, ContentIndexMode.NONE);

		try (SqliteContentStore store = SqliteContentStore.open(storeDir)) {
			try (ContentIngestSession session = store.beginIngest(request)) {
				for (int i = 0; i <= 512; i++) {
					Path source = output.resolve("sources/test/C" + i + ".java");
					Files.createDirectories(source.getParent());
					Files.writeString(source, "class C" + i + " {}");
					session.ingest(source);
				}
				// Closing without complete() marks the run as failed after one committed batch.
			}

			assertThat(store.getStats().getRunCount()).isZero();
			assertThat(store.getStats().getObjectCount()).isZero();
		}
	}

	@Test
	void recoversAndPrunesAbandonedIngestOnOpen() throws Exception {
		Path storeDir = tempDir.resolve("abandoned-store");
		try (SqliteContentStore ignored = SqliteContentStore.open(storeDir)) {
			// Create the schema before simulating a process that disappeared mid-ingest.
		}
		String hash = "ab" + "0".repeat(62);
		Path object = storeDir.resolve("objects/ab").resolve(hash);
		Files.createDirectories(object.getParent());
		Files.writeString(object, "orphan");
		try (Connection connection = DriverManager.getConnection(
				"jdbc:sqlite:" + storeDir.resolve("index.sqlite"));
				Statement statement = connection.createStatement()) {
			statement.execute("PRAGMA foreign_keys=ON");
			statement.execute("INSERT INTO applications(id, app_hash, display_name, inputs_json, first_seen) "
					+ "VALUES(1, 'abandoned', 'abandoned', '[]', '" + Instant.EPOCH + "')");
			statement.execute("INSERT INTO runs(id, application_id, analysis_key, output_root, "
					+ "materialization_mode, created_at, status) VALUES(1, 1, 'analysis', 'output', "
					+ "'KEEP', '" + Instant.EPOCH + "', 'INGESTING')");
			statement.execute("INSERT INTO objects(hash, size, kind, created_at) VALUES('"
					+ hash + "', 6, 'source', '" + Instant.EPOCH + "')");
			statement.execute("INSERT INTO artifacts(run_id, path, object_hash, media_type) VALUES("
					+ "1, 'sources/Orphan.java', '" + hash + "', 'text/x-java')");
		}

		String property = "jadx.storage.stale-ingest-age-seconds";
		String previous = System.getProperty(property);
		System.setProperty(property, "0");
		try (SqliteContentStore store = SqliteContentStore.open(storeDir)) {
			assertThat(store.getStats().getRunCount()).isZero();
			assertThat(store.getStats().getObjectCount()).isZero();
			assertThat(object).doesNotExist();
		} finally {
			if (previous == null) {
				System.clearProperty(property);
			} else {
				System.setProperty(property, previous);
			}
		}
	}

	@Test
	void preservesRecentlyActiveIngestOnOpen() throws Exception {
		Path storeDir = tempDir.resolve("active-ingest-store");
		try (SqliteContentStore ignored = SqliteContentStore.open(storeDir)) {
			// Create the schema before simulating a concurrently active ingest.
		}
		String now = Instant.now().toString();
		try (Connection connection = DriverManager.getConnection(
				"jdbc:sqlite:" + storeDir.resolve("index.sqlite"));
				Statement statement = connection.createStatement()) {
			statement.execute("INSERT INTO applications(id, app_hash, display_name, inputs_json, first_seen) "
					+ "VALUES(1, 'active', 'active', '[]', '" + now + "')");
			statement.execute("INSERT INTO runs(id, application_id, analysis_key, output_root, "
					+ "materialization_mode, created_at, heartbeat_at, status) "
					+ "VALUES(1, 1, 'analysis', 'output', 'KEEP', '" + now + "', '" + now + "', 'INGESTING')");
		}

		String property = "jadx.storage.stale-ingest-age-seconds";
		String previous = System.getProperty(property);
		System.setProperty(property, "3600");
		try (SqliteContentStore store = SqliteContentStore.open(storeDir)) {
			assertThat(store.getStats().getRunCount()).isEqualTo(1);
		} finally {
			if (previous == null) {
				System.clearProperty(property);
			} else {
				System.setProperty(property, previous);
			}
		}
	}

	@Test
	void keepsCanonicalSnapshotRequiredByNewestRun() throws Exception {
		Path storeDir = tempDir.resolve("prune-canonical-store");
		Path input = writeFile("prune-canonical.apk", "app");
		try (SqliteContentStore store = SqliteContentStore.open(storeDir)) {
			IngestStats canonical = store.ingest(request(
					"prune", input, writeOutput("prune-canonical-one"), MaterializationMode.KEEP));
			IngestStats newest = store.ingest(request(
					"prune", input, writeOutput("prune-canonical-two"), MaterializationMode.KEEP));

			PruneStats stats = store.pruneRuns(1);
			assertThat(stats.getRunCount()).isZero();
			assertThat(store.getStats().getRunCount()).isEqualTo(2);
			Path restored = tempDir.resolve("prune-canonical-restored");
			assertThat(store.materializeRun(newest.getRunId(), restored)).isEqualTo(1);
			assertThat(restored.resolve("sources/test/Web.java")).hasContent(SOURCE);
			assertThat(canonical.getRunId()).isLessThan(newest.getRunId());
		}
	}

	@Test
	void rewritesPacksToReclaimObjectsRemovedByRetention() throws Exception {
		Path storeDir = tempDir.resolve("packed-prune-store");
		Path input = writeFile("packed-prune.apk", "app");
		try (SqliteContentStore store = SqliteContentStore.open(storeDir)) {
			store.ingest(request("packed-prune", input, writeOutputWithSuffix("packed-old", "// packed-old\n"),
					MaterializationMode.KEEP));
			CompactionStats firstCompaction = store.compact(1024);
			assertThat(firstCompaction.getObjectCount()).isEqualTo(1);
			assertThat(store.getStats().getPackedObjectCount()).isEqualTo(1);

			IngestStats retained = store.ingest(request(
					"packed-prune", input, writeOutputWithSuffix("packed-new", "// packed-new\n"),
					MaterializationMode.KEEP));
			PruneStats prune = store.pruneRuns(1);
			assertThat(prune.getRunCount()).isEqualTo(1);
			assertThat(prune.getObjectCount()).isEqualTo(1);
			assertThat(prune.getPackedBytesPendingRepack()).isPositive();

			CompactionStats rewrite = store.compact(1024);
			assertThat(rewrite.getPackGarbageBytesReclaimed())
					.isEqualTo(prune.getPackedBytesPendingRepack());
			assertThat(store.getStats().getObjectCount()).isEqualTo(1);
			assertThat(store.getStats().getPackedObjectCount()).isEqualTo(1);
			try (var packs = Files.list(storeDir.resolve("packs"))) {
				assertThat(packs.filter(Files::isRegularFile).count()).isEqualTo(1);
			}
			Path restored = tempDir.resolve("packed-prune-restored");
			assertThat(store.materializeRun(retained.getRunId(), restored)).isEqualTo(1);
			assertThat(restored.resolve("sources/test/Web.java")).content().contains("packed-new");
		}
	}

	private static String sha256(Path file) throws Exception {
		byte[] hash = MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(file));
		return HexFormat.of().formatHex(hash);
	}

	@Test
	void newStoreContainsNoAnalysisOwnedTables() throws Exception {
		Path storeDir = tempDir.resolve("storage-only-schema");
		try (SqliteContentStore ignored = SqliteContentStore.open(storeDir)) {
			// opening creates the schema
		}
		String url = "jdbc:sqlite:" + storeDir.resolve("index.sqlite");
		try (var connection = DriverManager.getConnection(url);
				var statement = connection.prepareStatement(
						"SELECT name FROM sqlite_master WHERE type = 'table' AND name IN "
								+ "('analysis_facts', 'analyzer_cache', 'findings')");
				var resultSet = statement.executeQuery()) {
			assertThat(resultSet.next()).isFalse();
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
			assertThat(resultSet.getInt(1)).isEqualTo(6);
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
				assertThat(resultSet.getInt(1)).isEqualTo(6);
			}
		}
	}

	@Test
	void versionFiveMigrationConsolidatesHistoricalDuplicateArtifacts() throws Exception {
		Path storeDir = tempDir.resolve("v4-store");
		Files.createDirectories(storeDir);
		Class.forName("org.sqlite.JDBC");
		String url = "jdbc:sqlite:" + storeDir.resolve("index.sqlite");
		try (var connection = DriverManager.getConnection(url); var statement = connection.createStatement()) {
			statement.execute("CREATE TABLE applications(id INTEGER PRIMARY KEY)");
			statement.execute("CREATE TABLE runs("
					+ "id INTEGER PRIMARY KEY, application_id INTEGER NOT NULL, analysis_key TEXT NOT NULL, "
					+ "output_root TEXT NOT NULL, materialization_mode TEXT NOT NULL, created_at TEXT NOT NULL, "
					+ "completed_at TEXT, status TEXT NOT NULL, error TEXT)");
			statement.execute("CREATE TABLE objects(hash TEXT PRIMARY KEY)");
			statement.execute("CREATE TABLE artifacts("
					+ "run_id INTEGER NOT NULL, path TEXT NOT NULL, object_hash TEXT NOT NULL, "
					+ "media_type TEXT NOT NULL, symbol TEXT, PRIMARY KEY(run_id, path))");
			statement.execute("INSERT INTO applications VALUES(1)");
			statement.execute("INSERT INTO runs VALUES"
					+ "(1, 1, 'key', 'one', 'KEEP', 'now', 'now', 'COMPLETE', NULL), "
					+ "(2, 1, 'key', 'two', 'KEEP', 'now', 'now', 'COMPLETE', NULL)");
			statement.execute("INSERT INTO objects VALUES('hash')");
			statement.execute("INSERT INTO artifacts VALUES"
					+ "(1, 'sources/test/Web.java', 'hash', 'text/x-java-source', 'test.Web'), "
					+ "(2, 'sources/test/Web.java', 'hash', 'text/x-java-source', 'test.Web')");
			statement.execute("PRAGMA user_version=4");
		}

		try (SqliteContentStore ignored = SqliteContentStore.open(storeDir)) {
			// opening performs the migration and consolidation
		}
		try (var connection = DriverManager.getConnection(url); var statement = connection.createStatement()) {
			try (var resultSet = statement.executeQuery("SELECT COUNT(*) FROM artifacts")) {
				assertThat(resultSet.next()).isTrue();
				assertThat(resultSet.getInt(1)).isEqualTo(1);
			}
			try (var resultSet = statement.executeQuery("SELECT artifact_source_run_id FROM runs WHERE id = 2")) {
				assertThat(resultSet.next()).isTrue();
				assertThat(resultSet.getLong(1)).isEqualTo(1);
			}
			try (var resultSet = statement.executeQuery("PRAGMA user_version")) {
				assertThat(resultSet.next()).isTrue();
				assertThat(resultSet.getInt(1)).isEqualTo(6);
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

	private Path writeOutputWithSuffix(String name, String suffix) throws Exception {
		Path output = writeOutput(name);
		Files.writeString(output.resolve("sources/test/Web.java"), SOURCE + suffix);
		return output;
	}

	private Path writeFile(String name, String content) throws Exception {
		Path file = tempDir.resolve(name);
		Files.write(file, content.getBytes(StandardCharsets.UTF_8));
		return file;
	}
}
