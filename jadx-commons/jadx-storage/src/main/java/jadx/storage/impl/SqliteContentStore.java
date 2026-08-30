package jadx.storage.impl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.DosFileAttributeView;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jadx.storage.api.CompactionStats;
import jadx.storage.api.ContentIndexMode;
import jadx.storage.api.ContentIngestSession;
import jadx.storage.api.ContentStore;
import jadx.storage.api.IngestRequest;
import jadx.storage.api.IngestStats;
import jadx.storage.api.MaterializationMode;
import jadx.storage.api.PruneStats;
import jadx.storage.api.SearchResult;
import jadx.storage.api.StoreStats;

public final class SqliteContentStore implements ContentStore {
	private static final Logger LOG = LoggerFactory.getLogger(SqliteContentStore.class);
	private static final int SCHEMA_VERSION = 6;
	private static final int COMMIT_BATCH_SIZE = 512;
	private static final long MAX_INDEXED_TEXT_BYTES = 4L * 1024 * 1024;
	private static final int COPY_BUFFER_SIZE = 64 * 1024;
	private static final String OBJECT_SHARD_DEPTH_PROPERTY = "jadx.storage.object-shard-depth";
	private static final String STALE_INGEST_AGE_PROPERTY = "jadx.storage.stale-ingest-age-seconds";
	private static final long DEFAULT_STALE_INGEST_AGE_SECONDS = Duration.ofHours(6).toSeconds();

	private final Path root;
	private final Path objectsDir;
	private final Path packsDir;
	private final int objectShardDepth;
	private final Connection connection;
	private final boolean ftsAvailable;
	private final boolean structuredFtsAvailable;
	private @Nullable IngestSession activeIngestSession;

	private SqliteContentStore(Path root, Connection connection) throws SQLException, IOException {
		this.root = root;
		this.objectsDir = root.resolve("objects");
		this.packsDir = root.resolve("packs");
		this.objectShardDepth = readObjectShardDepth();
		this.connection = connection;
		Files.createDirectories(objectsDir);
		Files.createDirectories(packsDir);
		configureConnection();
		migrateSchema();
		this.ftsAvailable = initializeFts();
		this.structuredFtsAvailable = initializeStructuredFts();
	}

	public static SqliteContentStore open(Path root) throws IOException {
		Path normalizedRoot = root.toAbsolutePath().normalize();
		Connection connection = null;
		try {
			Files.createDirectories(normalizedRoot);
			Class.forName("org.sqlite.JDBC");
			Path database = normalizedRoot.resolve("index.sqlite");
			connection = DriverManager.getConnection("jdbc:sqlite:" + database);
			SqliteContentStore store = new SqliteContentStore(normalizedRoot, connection);
			store.recoverAbandonedRuns();
			store.cleanupFailedRuns();
			return store;
		} catch (ClassNotFoundException | SQLException e) {
			closeAfterOpenFailure(connection, e);
			throw new IOException("SQLite content store is unavailable", e);
		} catch (IOException | RuntimeException e) {
			closeAfterOpenFailure(connection, e);
			throw e;
		}
	}

	private static void closeAfterOpenFailure(@Nullable Connection connection, Exception failure) {
		if (connection == null) {
			return;
		}
		try {
			connection.close();
		} catch (SQLException closeError) {
			failure.addSuppressed(closeError);
		}
	}

	@Override
	public synchronized IngestStats ingest(IngestRequest request) throws IOException {
		Path outputRoot = request.getOutputDirectory().toAbsolutePath().normalize();
		if (!Files.isDirectory(outputRoot)) {
			throw new IOException("Output directory does not exist: " + outputRoot);
		}
		try (ContentIngestSession session = beginIngest(request);
				Stream<Path> files = walkOutputFiles(outputRoot)) {
			Iterator<Path> iterator = files.iterator();
			while (iterator.hasNext()) {
				session.ingest(iterator.next());
			}
			return session.complete();
		}
	}

	@Override
	public synchronized ContentIngestSession beginIngest(IngestRequest request) throws IOException {
		if (activeIngestSession != null) {
			throw new IOException("A content-store ingest session is already active");
		}
		recoverAbandonedRuns();
		cleanupFailedRuns();
		Path outputRoot = request.getOutputDirectory().toAbsolutePath().normalize();
		if (root.startsWith(outputRoot)) {
			throw new IOException("Content store must be outside the output directory: " + root);
		}
		String applicationHash = hashApplication(request.getInputFiles());
		long runId = -1;
		PreparedStatements statements = null;
		try {
			connection.setAutoCommit(false);
			long applicationId = upsertApplication(applicationHash, request);
			runId = createRun(applicationId, request, outputRoot);
			connection.commit();
			statements = new PreparedStatements(connection, ftsAvailable);
			connection.setAutoCommit(false);
			IngestSession session = new IngestSession(request, outputRoot,
					new MutableStats(applicationId, runId), statements);
			activeIngestSession = session;
			return session;
		} catch (SQLException e) {
			rollbackQuietly();
			if (statements != null) {
				try {
					statements.close();
				} catch (SQLException closeError) {
					e.addSuppressed(closeError);
				}
			}
			setAutoCommitQuietly(true);
			if (runId != -1) {
				finishRunQuietly(runId, "FAILED", e.getMessage());
			}
			throw storageError("Failed to create content-store ingest session", e);
		}
	}

	@Override
	public synchronized List<SearchResult> search(String query, int limit) throws IOException {
		if (query.trim().isEmpty() || limit <= 0) {
			return new ArrayList<>();
		}
		List<SearchResult> results = new ArrayList<>();
		if (ftsAvailable) {
			searchFullText(query, limit, results);
		}
		if (results.size() < limit) {
			if (structuredFtsAvailable && supportsTrigramSearch(query)) {
				searchStructuredIndex(query, limit - results.size(), results);
			} else {
				searchStructuredFallback(query, limit - results.size(), results);
			}
		}
		return results;
	}

	private void searchFullText(String query, int limit, List<SearchResult> results) throws IOException {
		String sql = "SELECT app.id, app.display_name, se.path, f.object_hash, "
				+ "snippet(source_fts, 2, '[', ']', ' ... ', 24) "
				+ "FROM source_fts f "
				+ "JOIN search_entries se ON se.object_hash = f.object_hash "
				+ "JOIN applications app ON app.id = se.application_id "
				+ "WHERE source_fts MATCH ? ORDER BY app.id DESC LIMIT ?";
		try (PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setString(1, makeSafeFtsQuery(query));
			statement.setInt(2, limit);
			try (ResultSet resultSet = statement.executeQuery()) {
				while (resultSet.next()) {
					results.add(new SearchResult(
							resultSet.getLong(1), resultSet.getString(2), resultSet.getString(3),
							resultSet.getString(4), resultSet.getString(5)));
				}
			}
		} catch (SQLException e) {
			throw storageError("Content search failed", e);
		}
	}

	private void searchStructuredIndex(String query, int limit, List<SearchResult> results) throws IOException {
		String sql = "SELECT app.id, app.display_name, se.path, se.object_hash, "
				+ "COALESCE(se.symbol, se.path) FROM artifact_fts af "
				+ "JOIN search_entries se ON se.id = af.rowid "
				+ "JOIN applications app ON app.id = se.application_id "
				+ "WHERE artifact_fts MATCH ? ORDER BY app.id DESC LIMIT ?";
		try (PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setString(1, makeSafeTrigramQuery(query));
			statement.setInt(2, limit);
			appendUniqueResults(statement, results);
		} catch (SQLException e) {
			throw storageError("Structured content search failed", e);
		}
	}

	private void searchStructuredFallback(String query, int limit, List<SearchResult> results) throws IOException {
		String sql = "SELECT app.id, app.display_name, se.path, se.object_hash, "
				+ "COALESCE(se.symbol, se.path) FROM search_entries se "
				+ "JOIN applications app ON app.id = se.application_id "
				+ "WHERE se.path LIKE ? ESCAPE '\\' OR se.symbol LIKE ? ESCAPE '\\' "
				+ "ORDER BY app.id DESC LIMIT ?";
		String like = '%' + escapeLike(query.trim()) + '%';
		try (PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setString(1, like);
			statement.setString(2, like);
			statement.setInt(3, limit);
			appendUniqueResults(statement, results);
		} catch (SQLException e) {
			throw storageError("Structured content search failed", e);
		}
	}

	private static void appendUniqueResults(PreparedStatement statement, List<SearchResult> results) throws SQLException {
		try (ResultSet resultSet = statement.executeQuery()) {
			while (resultSet.next()) {
				SearchResult result = new SearchResult(
						resultSet.getLong(1), resultSet.getString(2), resultSet.getString(3),
						resultSet.getString(4), resultSet.getString(5));
				boolean duplicate = results.stream().anyMatch(existing -> existing.getApplicationId() == result.getApplicationId()
						&& existing.getPath().equals(result.getPath())
						&& existing.getObjectHash().equals(result.getObjectHash()));
				if (!duplicate) {
					results.add(result);
				}
			}
		}
	}

	@Override
	public synchronized StoreStats getStats() throws IOException {
		String sql = "SELECT "
				+ "(SELECT COUNT(*) FROM applications), "
				+ "(SELECT COUNT(*) FROM runs), "
				+ "COUNT(*), COALESCE(SUM(size), 0), "
				+ "COALESCE(SUM(CASE WHEN pack_name IS NOT NULL THEN 1 ELSE 0 END), 0), "
				+ "COALESCE(SUM(CASE WHEN pack_name IS NOT NULL THEN pack_length ELSE 0 END), 0) "
				+ "FROM objects";
		try (Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(sql)) {
			if (!resultSet.next()) {
				throw new SQLException("Content-store statistics query returned no rows");
			}
			return new StoreStats(resultSet.getLong(1), resultSet.getLong(2), resultSet.getLong(3),
					resultSet.getLong(4), resultSet.getLong(5), resultSet.getLong(6));
		} catch (SQLException e) {
			throw storageError("Content-store statistics query failed", e);
		}
	}

	@Override
	public synchronized CompactionStats compact(long requestedMaxPackBytes) throws IOException {
		long startNanos = System.nanoTime();
		long maxPackBytes = requestedMaxPackBytes > 0 ? requestedMaxPackBytes : 512L * 1024 * 1024;
		Set<String> pinnedObjects = findOutputPinnedObjects();
		List<PackEntry> entries = loadLooseObjects(pinnedObjects);
		PackRewritePlan rewritePlan = planFragmentedPacks();
		entries.addAll(rewritePlan.entries);
		List<Path> createdPacks = new ArrayList<>();
		try {
			if (!entries.isEmpty()) {
				writePacks(entries, maxPackBytes, createdPacks);
				connection.setAutoCommit(false);
				try (PreparedStatement updateLoose = connection.prepareStatement(
						"UPDATE objects SET pack_name = ?, pack_offset = ?, pack_length = ? "
								+ "WHERE hash = ? AND pack_name IS NULL");
						PreparedStatement updatePacked = connection.prepareStatement(
								"UPDATE objects SET pack_name = ?, pack_offset = ?, pack_length = ? "
										+ "WHERE hash = ? AND pack_name = ? AND pack_offset = ?")) {
					for (PackEntry entry : entries) {
						PreparedStatement update = entry.sourcePackName == null ? updateLoose : updatePacked;
						update.setString(1, entry.packName);
						update.setLong(2, entry.offset);
						update.setLong(3, entry.length);
						update.setString(4, entry.hash);
						if (entry.sourcePackName != null) {
							update.setString(5, entry.sourcePackName);
							update.setLong(6, entry.sourceOffset);
						}
						if (update.executeUpdate() != 1) {
							throw new SQLException("Object changed during compaction: " + entry.hash);
						}
					}
					connection.commit();
				} catch (SQLException e) {
					connection.rollback();
					throw e;
				} finally {
					connection.setAutoCommit(true);
				}
			}
		} catch (Exception e) {
			for (Path pack : createdPacks) {
				Files.deleteIfExists(pack);
			}
			if (e instanceof IOException) {
				throw (IOException) e;
			}
			throw storageError("Content-store compaction failed", e);
		}
		long deletedBytes = deletePackedLooseObjects();
		long packGarbageBytesReclaimed = deleteObsoletePacks(rewritePlan.obsoletePacks);
		vacuumDatabase();
		long packedBytes = entries.stream().mapToLong(entry -> entry.length).sum();
		return new CompactionStats(entries.size(), packedBytes, deletedBytes, packGarbageBytesReclaimed,
				createdPacks.size(),
				(System.nanoTime() - startNanos) / 1_000_000);
	}

	@Override
	public synchronized PruneStats pruneRuns(int keepCompleteRunsPerApplication) throws IOException {
		return pruneRuns(keepCompleteRunsPerApplication, true);
	}

	private PruneStats pruneRuns(int keepCompleteRunsPerApplication, boolean optimizeDatabase) throws IOException {
		if (keepCompleteRunsPerApplication <= 0) {
			throw new IllegalArgumentException("Retained run count must be positive");
		}
		long startNanos = System.nanoTime();
		List<PrunedObject> prunedObjects = new ArrayList<>();
		long runCount;
		try {
			connection.setAutoCommit(false);
			try (Statement statement = connection.createStatement()) {
				statement.execute("CREATE TEMP TABLE prune_runs(id INTEGER PRIMARY KEY) WITHOUT ROWID");
				statement.execute("INSERT INTO prune_runs(id) SELECT id FROM ("
						+ "SELECT id, ROW_NUMBER() OVER(PARTITION BY application_id ORDER BY id DESC) AS rank "
						+ "FROM runs WHERE status = 'COMPLETE') WHERE rank > " + keepCompleteRunsPerApplication);
				statement.execute("INSERT OR IGNORE INTO prune_runs(id) "
						+ "SELECT id FROM runs WHERE status = 'FAILED'");
				statement.execute("DELETE FROM prune_runs WHERE id IN ("
						+ "SELECT artifact_source_run_id FROM runs WHERE artifact_source_run_id IS NOT NULL "
						+ "AND id NOT IN (SELECT id FROM prune_runs))");
				runCount = querySingleLong(statement, "SELECT COUNT(*) FROM prune_runs");

				statement.execute("CREATE TEMP TABLE live_search_refs("
						+ "application_id INTEGER NOT NULL, path TEXT NOT NULL, object_hash TEXT NOT NULL, "
						+ "first_run INTEGER NOT NULL, PRIMARY KEY(application_id, path, object_hash)) WITHOUT ROWID");
				statement.execute("INSERT INTO live_search_refs "
						+ "SELECT r.application_id, ar.path, ar.object_hash, MIN(r.id) FROM runs r "
						+ "JOIN artifacts ar ON ar.run_id = COALESCE(r.artifact_source_run_id, r.id) "
						+ "WHERE r.id NOT IN (SELECT id FROM prune_runs) "
						+ "GROUP BY r.application_id, ar.path, ar.object_hash");
				statement.execute("CREATE TEMP TABLE live_application_objects("
						+ "application_id INTEGER NOT NULL, object_hash TEXT NOT NULL, first_run INTEGER NOT NULL, "
						+ "PRIMARY KEY(application_id, object_hash)) WITHOUT ROWID");
				statement.execute("INSERT INTO live_application_objects "
						+ "SELECT application_id, object_hash, MIN(first_run) FROM live_search_refs "
						+ "GROUP BY application_id, object_hash");

				statement.execute("DELETE FROM search_entries WHERE NOT EXISTS (SELECT 1 FROM live_search_refs live "
						+ "WHERE live.application_id = search_entries.application_id AND live.path = search_entries.path "
						+ "AND live.object_hash = search_entries.object_hash)");
				statement.execute("UPDATE search_entries SET first_seen_run = (SELECT first_run FROM live_search_refs live "
						+ "WHERE live.application_id = search_entries.application_id AND live.path = search_entries.path "
						+ "AND live.object_hash = search_entries.object_hash) "
						+ "WHERE first_seen_run IN (SELECT id FROM prune_runs)");
				statement.execute("DELETE FROM application_objects WHERE NOT EXISTS ("
						+ "SELECT 1 FROM live_application_objects live "
						+ "WHERE live.application_id = application_objects.application_id "
						+ "AND live.object_hash = application_objects.object_hash)");
				statement.execute("UPDATE application_objects SET first_seen_run = ("
						+ "SELECT first_run FROM live_application_objects live "
						+ "WHERE live.application_id = application_objects.application_id "
						+ "AND live.object_hash = application_objects.object_hash) "
						+ "WHERE first_seen_run IN (SELECT id FROM prune_runs)");

				statement.execute("DELETE FROM runs WHERE id IN (SELECT id FROM prune_runs)");
				statement.execute("DELETE FROM applications WHERE NOT EXISTS ("
						+ "SELECT 1 FROM runs WHERE runs.application_id = applications.id)");
				statement.execute("CREATE TEMP TABLE prune_objects AS SELECT hash, size, pack_name, pack_length "
						+ "FROM objects WHERE NOT EXISTS (SELECT 1 FROM artifacts WHERE object_hash = objects.hash)");
				try (ResultSet resultSet = statement.executeQuery(
						"SELECT hash, size, pack_name, pack_length FROM prune_objects")) {
					while (resultSet.next()) {
						prunedObjects.add(new PrunedObject(
								resultSet.getString(1), resultSet.getLong(2),
								resultSet.getString(3), resultSet.getLong(4)));
					}
				}
				if (ftsAvailable) {
					statement.execute("DELETE FROM source_fts WHERE object_hash IN (SELECT hash FROM prune_objects)");
				}
				statement.execute("DELETE FROM text_indexed WHERE object_hash IN (SELECT hash FROM prune_objects)");
				statement.execute("DELETE FROM object_metadata WHERE object_hash IN (SELECT hash FROM prune_objects)");
				statement.execute("DELETE FROM objects WHERE hash IN (SELECT hash FROM prune_objects)");
				statement.execute("DROP TABLE live_application_objects");
				statement.execute("DROP TABLE live_search_refs");
				statement.execute("DROP TABLE prune_objects");
				statement.execute("DROP TABLE prune_runs");
				connection.commit();
			}
		} catch (SQLException e) {
			rollbackQuietly();
			throw storageError("Failed to prune content-store runs", e);
		} finally {
			setAutoCommitQuietly(true);
		}

		long looseBytesUnlinked = 0;
		long packedBytesPendingRepack = 0;
		for (PrunedObject object : prunedObjects) {
			if (object.packName == null) {
				Path loose = existingObjectPath(object.hash);
				try {
					if (deleteLooseObject(object.hash)) {
						looseBytesUnlinked += object.size;
					}
				} catch (IOException e) {
					LOG.warn("Failed to remove pruned loose object: {}", loose, e);
				}
			} else {
				packedBytesPendingRepack += object.packLength;
			}
		}
		if (optimizeDatabase) {
			vacuumDatabase();
		}
		return new PruneStats(runCount, prunedObjects.size(), looseBytesUnlinked,
				packedBytesPendingRepack, (System.nanoTime() - startNanos) / 1_000_000);
	}

	private static long querySingleLong(Statement statement, String sql) throws SQLException {
		try (ResultSet resultSet = statement.executeQuery(sql)) {
			if (!resultSet.next()) {
				throw new SQLException("Query returned no rows: " + sql);
			}
			return resultSet.getLong(1);
		}
	}

	private void vacuumDatabase() throws IOException {
		try (Statement statement = connection.createStatement()) {
			if (ftsAvailable) {
				statement.execute("INSERT INTO source_fts(source_fts) VALUES('optimize')");
			}
			if (structuredFtsAvailable) {
				statement.execute("INSERT INTO artifact_fts(artifact_fts) VALUES('optimize')");
			}
		} catch (SQLException e) {
			throw storageError("Failed to optimize content-store search metadata", e);
		}
		try (Statement statement = connection.createStatement()) {
			statement.execute("VACUUM");
		} catch (SQLException e) {
			throw storageError("Failed to compact content-store metadata", e);
		}
	}

	private long deletePackedLooseObjects() throws IOException {
		String sql = "SELECT hash, size, pack_name FROM objects WHERE pack_name IS NOT NULL";
		try (Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(sql)) {
			long deletedBytes = 0;
			while (resultSet.next()) {
				String hash = resultSet.getString(1);
				long size = resultSet.getLong(2);
				Path pack = packsDir.resolve(resultSet.getString(3)).normalize();
				if (!pack.startsWith(packsDir) || !Files.isRegularFile(pack)) {
					throw new IOException("Refusing to delete loose object without a valid pack: " + hash);
				}
				Path loose = existingObjectPath(hash);
				try {
					if (deleteLooseObject(hash)) {
						deletedBytes += size;
					}
				} catch (IOException e) {
					LOG.warn("Packed object retained as a loose fallback: {}", loose, e);
				}
			}
			return deletedBytes;
		} catch (SQLException e) {
			throw storageError("Failed to clean packed loose objects", e);
		}
	}

	@Override
	public synchronized void materializeObject(String objectHash, Path target) throws IOException {
		validateObjectHash(objectHash);
		ObjectLocation location = findObjectLocation(objectHash);
		Path absoluteTarget = target.toAbsolutePath().normalize();
		Path parent = absoluteTarget.getParent();
		if (parent != null) {
			Files.createDirectories(parent);
		}
		Path object = existingObjectPath(objectHash);
		if (Files.isRegularFile(object)) {
			Files.copy(object, absoluteTarget, StandardCopyOption.REPLACE_EXISTING);
		} else if (location.packName != null) {
			copyFromPack(location, absoluteTarget);
		} else {
			throw new IOException("CAS object content not found: " + objectHash);
		}
		setWritable(absoluteTarget, true);
		String actualHash = hashFile(absoluteTarget);
		if (!objectHash.equals(actualHash)) {
			Files.deleteIfExists(absoluteTarget);
			throw new IOException("CAS integrity check failed for: " + objectHash);
		}
	}

	@Override
	public synchronized long materializeRun(long runId, Path targetDirectory) throws IOException {
		Path targetRoot = targetDirectory.toAbsolutePath().normalize();
		try {
			Files.createDirectories(targetRoot);
		} catch (IOException e) {
			throw new IOException("Failed to create materialization directory: " + targetRoot, e);
		}
		String sql = "SELECT ar.path, ar.object_hash FROM runs r "
				+ "JOIN artifacts ar ON ar.run_id = COALESCE(r.artifact_source_run_id, r.id) "
				+ "WHERE r.id = ? ORDER BY ar.path";
		try (PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setLong(1, runId);
			try (ResultSet resultSet = statement.executeQuery()) {
				long count = 0;
				while (resultSet.next()) {
					String relativePath = resultSet.getString(1);
					Path target = targetRoot.resolve(relativePath).normalize();
					if (!target.startsWith(targetRoot)) {
						throw new IOException("Invalid stored artifact path: " + relativePath);
					}
					materializeObject(resultSet.getString(2), target);
					count++;
				}
				return count;
			}
		} catch (SQLException e) {
			throw storageError("Failed to materialize run: " + runId, e);
		}
	}

	@Override
	public synchronized void close() throws IOException {
		if (activeIngestSession != null) {
			activeIngestSession.close();
		}
		try {
			connection.close();
		} catch (SQLException e) {
			throw storageError("Failed to close content store", e);
		}
	}

	private void ingestFile(IngestRequest request, Path outputRoot, Path file,
			@Nullable String suppliedHash, long suppliedSize,
			MutableStats stats, PreparedStatements statements) throws IOException, SQLException {
		long size = Files.size(file);
		if (suppliedSize >= 0 && suppliedSize != size) {
			throw new IOException("Output size changed before ingest: " + file);
		}
		String hash;
		if (suppliedHash == null) {
			hash = hashFile(file);
		} else {
			try {
				validateObjectHash(suppliedHash);
			} catch (IllegalArgumentException e) {
				throw new IOException("Invalid supplied output hash for: " + file, e);
			}
			hash = suppliedHash;
		}
		String relativePath = normalizeRelativePath(outputRoot.relativize(file));
		String kind = detectKind(relativePath);
		String mediaType = detectMediaType(kind);
		stats.artifactCount++;
		stats.logicalBytes += size;

		boolean newObject = insertObject(statements.insertObject, hash, size, kind);
		Path object = newObject ? newObjectPath(hash) : existingObjectPath(hash);
		boolean sourceLinked = false;
		if (newObject) {
			if (request.getMaterializationMode() == MaterializationMode.HARD_LINK) {
				sourceLinked = moveObjectAndRestoreLink(file, object, size);
			} else {
				storeObject(file, object, size);
			}
			stats.uniqueObjectCount++;
			stats.newStoredBytes += size;
		} else {
			stats.reusedObjectCount++;
			ObjectLocation location = findObjectLocation(statements.selectObjectLocation, hash);
			if (Files.isRegularFile(object)) {
				ensureExistingObject(file, object, size);
			} else if (request.getMaterializationMode() == MaterializationMode.HARD_LINK) {
				materializeLooseObject(hash, object, location);
			}
		}

		boolean textCandidate = isTextCandidate(kind, size);
		boolean needsTextIndex = textCandidate
				&& request.getIndexMode() == ContentIndexMode.FULL_TEXT
				&& ftsAvailable
				&& !exists(statements.hasTextIndex, hash);
		String symbol = findObjectSymbol(statements.selectObjectSymbol, hash);
		if (needsTextIndex) {
			String content = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
			symbol = SourceSymbolExtractor.extract(relativePath, content);
			insertTextIndex(statements, hash, symbol, content);
			stats.indexedObjectCount++;
			upsertObjectSymbol(statements.upsertObjectSymbol, hash, symbol);
			updateSearchEntrySymbols(statements.updateSearchEntrySymbols, hash, symbol);
		}
		insertArtifact(statements.insertArtifact, stats.runId, relativePath, hash, mediaType, symbol);
		insertApplicationObject(statements.insertApplicationObject, stats.applicationId, hash, stats.runId);
		insertSearchEntry(statements.insertSearchEntry, stats.applicationId, relativePath, hash, symbol, stats.runId);

		if (request.getMaterializationMode() == MaterializationMode.HARD_LINK) {
			if (sourceLinked || replaceWithHardLink(file, object)) {
				stats.hardLinkCount++;
			} else {
				stats.hardLinkFallbackCount++;
			}
		}
	}

	private Set<String> findOutputPinnedObjects() throws IOException {
		Set<String> pinnedObjects = new HashSet<>();
		String sql = "SELECT ar.object_hash, r.output_root, ar.path FROM runs r "
				+ "JOIN artifacts ar ON ar.run_id = COALESCE(r.artifact_source_run_id, r.id) "
				+ "WHERE r.materialization_mode = ?";
		try (PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setString(1, MaterializationMode.HARD_LINK.name());
			try (ResultSet resultSet = statement.executeQuery()) {
				while (resultSet.next()) {
					String hash = resultSet.getString(1);
					if (pinnedObjects.contains(hash)) {
						continue;
					}
					Path outputRoot;
					Path output;
					try {
						outputRoot = Path.of(resultSet.getString(2)).toAbsolutePath().normalize();
						output = outputRoot.resolve(resultSet.getString(3)).normalize();
						if (!output.startsWith(outputRoot)) {
							continue;
						}
						Path object = existingObjectPath(hash);
						if (isSameRegularFile(output, object)) {
							pinnedObjects.add(hash);
						}
					} catch (Exception e) {
						LOG.debug("Failed to inspect content-store output link for object {}", hash, e);
					}
				}
			}
		} catch (SQLException e) {
			throw storageError("Failed to inspect hard-linked content-store outputs", e);
		}
		if (!pinnedObjects.isEmpty()) {
			LOG.info("Content-store compaction retained {} objects linked by output trees", pinnedObjects.size());
		}
		return pinnedObjects;
	}

	private static boolean isSameRegularFile(Path first, Path second) throws IOException {
		BasicFileAttributes firstAttributes = Files.readAttributes(
				first, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
		BasicFileAttributes secondAttributes = Files.readAttributes(
				second, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
		if (!firstAttributes.isRegularFile() || !secondAttributes.isRegularFile()) {
			return false;
		}
		Object firstKey = firstAttributes.fileKey();
		Object secondKey = secondAttributes.fileKey();
		if (firstKey != null && secondKey != null) {
			return firstKey.equals(secondKey);
		}
		return Files.isSameFile(first, second);
	}

	private List<PackEntry> loadLooseObjects(Set<String> pinnedObjects) throws IOException {
		String sql = "SELECT hash, size FROM objects WHERE pack_name IS NULL ORDER BY hash";
		try (Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(sql)) {
			List<PackEntry> entries = new ArrayList<>();
			while (resultSet.next()) {
				String hash = resultSet.getString(1);
				if (pinnedObjects.contains(hash)) {
					continue;
				}
				long size = resultSet.getLong(2);
				Path loose = existingObjectPath(hash);
				if (!Files.isRegularFile(loose) || Files.size(loose) != size) {
					throw new IOException("Loose CAS object is missing or invalid: " + hash);
				}
				entries.add(new PackEntry(hash, size));
			}
			return entries;
		} catch (SQLException e) {
			throw storageError("Failed to enumerate loose CAS objects", e);
		}
	}

	private PackRewritePlan planFragmentedPacks() throws IOException {
		Map<String, Long> liveBytesByPack = new HashMap<>();
		String aggregateSql = "SELECT pack_name, COALESCE(SUM(pack_length), 0) FROM objects "
				+ "WHERE pack_name IS NOT NULL GROUP BY pack_name";
		try (Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(aggregateSql)) {
			while (resultSet.next()) {
				liveBytesByPack.put(resultSet.getString(1), resultSet.getLong(2));
			}
		} catch (SQLException e) {
			throw storageError("Failed to inspect content-store packs", e);
		}

		List<ObsoletePack> obsoletePacks = new ArrayList<>();
		Set<String> fragmentedPackNames = new HashSet<>();
		try (Stream<Path> packFiles = Files.list(packsDir)) {
			Iterator<Path> iterator = packFiles
					.filter(path -> Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS))
					.filter(path -> path.getFileName().toString().endsWith(".cas"))
					.iterator();
			while (iterator.hasNext()) {
				Path pack = iterator.next();
				String packName = pack.getFileName().toString();
				Long liveBytes = liveBytesByPack.remove(packName);
				long live = liveBytes == null ? 0 : liveBytes;
				long size = Files.size(pack);
				if (live > size) {
					throw new IOException("CAS pack index exceeds file size: " + packName);
				}
				if (live < size) {
					fragmentedPackNames.add(packName);
					obsoletePacks.add(new ObsoletePack(pack, size - live));
				}
			}
		}
		if (!liveBytesByPack.isEmpty()) {
			throw new IOException("CAS pack files are missing: " + liveBytesByPack.keySet());
		}
		if (fragmentedPackNames.isEmpty()) {
			return new PackRewritePlan(new ArrayList<>(), obsoletePacks);
		}

		List<PackEntry> entries = new ArrayList<>();
		String objectSql = "SELECT hash, pack_length, pack_name, pack_offset FROM objects "
				+ "WHERE pack_name IS NOT NULL ORDER BY pack_name, pack_offset";
		try (Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(objectSql)) {
			while (resultSet.next()) {
				String packName = resultSet.getString(3);
				if (fragmentedPackNames.contains(packName)) {
					entries.add(new PackEntry(
							resultSet.getString(1), resultSet.getLong(2), packName, resultSet.getLong(4)));
				}
			}
		} catch (SQLException e) {
			throw storageError("Failed to enumerate live packed objects", e);
		}
		return new PackRewritePlan(entries, obsoletePacks);
	}

	private long deleteObsoletePacks(List<ObsoletePack> obsoletePacks) {
		long garbageBytesReclaimed = 0;
		for (ObsoletePack obsoletePack : obsoletePacks) {
			try {
				if (Files.deleteIfExists(obsoletePack.path)) {
					garbageBytesReclaimed += obsoletePack.garbageBytes;
				}
			} catch (IOException e) {
				LOG.warn("Obsolete content-store pack retained: {}", obsoletePack.path, e);
			}
		}
		return garbageBytesReclaimed;
	}

	private void writePacks(List<PackEntry> entries, long maxPackBytes, List<Path> createdPacks) throws IOException {
		int entryIndex = 0;
		Map<String, FileChannel> sourceChannels = new HashMap<>();
		try {
			while (entryIndex < entries.size()) {
				String packName = "pack-" + Instant.now().toEpochMilli() + '-' + UUID.randomUUID() + ".cas";
				Path pack = packsDir.resolve(packName);
				Path temporary = packsDir.resolve(packName + ".tmp");
				long packSize = 0;
				try {
					try (FileChannel channel = FileChannel.open(temporary,
							StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
						while (entryIndex < entries.size()) {
							PackEntry entry = entries.get(entryIndex);
							if (packSize != 0 && packSize + entry.length > maxPackBytes) {
								break;
							}
							entry.packName = packName;
							entry.offset = packSize;
							copyToPack(entry, channel, sourceChannels);
							packSize += entry.length;
							entryIndex++;
						}
						channel.force(true);
					}
					try {
						Files.move(temporary, pack, StandardCopyOption.ATOMIC_MOVE);
					} catch (AtomicMoveNotSupportedException e) {
						Files.move(temporary, pack);
					}
					createdPacks.add(pack);
				} finally {
					Files.deleteIfExists(temporary);
				}
			}
		} finally {
			for (FileChannel sourceChannel : sourceChannels.values()) {
				sourceChannel.close();
			}
		}
	}

	private void copyToPack(PackEntry entry, FileChannel channel, Map<String, FileChannel> sourceChannels)
			throws IOException {
		if (entry.sourcePackName != null) {
			copyPackedEntryToPack(entry, channel, sourceChannels);
			return;
		}
		MessageDigest digest = sha256();
		byte[] bytes = new byte[COPY_BUFFER_SIZE];
		try (InputStream input = Files.newInputStream(existingObjectPath(entry.hash))) {
			long written = 0;
			int read;
			while ((read = input.read(bytes)) != -1) {
				digest.update(bytes, 0, read);
				ByteBuffer buffer = ByteBuffer.wrap(bytes, 0, read);
				while (buffer.hasRemaining()) {
					written += channel.write(buffer);
				}
			}
			if (written != entry.length || !entry.hash.equals(toHex(digest.digest()))) {
				throw new IOException("CAS integrity check failed during compaction: " + entry.hash);
			}
		}
	}

	private void copyPackedEntryToPack(PackEntry entry, FileChannel output,
			Map<String, FileChannel> sourceChannels) throws IOException {
		FileChannel input = sourceChannels.get(entry.sourcePackName);
		if (input == null) {
			Path sourcePack = packsDir.resolve(entry.sourcePackName).normalize();
			if (!sourcePack.startsWith(packsDir) || !Files.isRegularFile(sourcePack)) {
				throw new IOException("CAS source pack not found: " + entry.sourcePackName);
			}
			input = FileChannel.open(sourcePack, StandardOpenOption.READ);
			sourceChannels.put(entry.sourcePackName, input);
		}
		MessageDigest digest = sha256();
		ByteBuffer buffer = ByteBuffer.allocate(COPY_BUFFER_SIZE);
		long remaining = entry.length;
		long sourcePosition = entry.sourceOffset;
		long written = 0;
		while (remaining > 0) {
			buffer.clear();
			buffer.limit((int) Math.min(buffer.capacity(), remaining));
			int read = input.read(buffer, sourcePosition);
			if (read <= 0) {
				throw new IOException("Unexpected end of CAS pack: " + entry.sourcePackName);
			}
			sourcePosition += read;
			remaining -= read;
			buffer.flip();
			digest.update(buffer.asReadOnlyBuffer());
			while (buffer.hasRemaining()) {
				written += output.write(buffer);
			}
		}
		if (written != entry.length || !entry.hash.equals(toHex(digest.digest()))) {
			throw new IOException("CAS integrity check failed during pack rewrite: " + entry.hash);
		}
	}

	private ObjectLocation findObjectLocation(String hash) throws IOException {
		try (PreparedStatement statement = connection.prepareStatement(
				"SELECT size, pack_name, pack_offset, pack_length FROM objects WHERE hash = ?")) {
			return findObjectLocation(statement, hash);
		} catch (SQLException e) {
			throw storageError("Failed to locate CAS object: " + hash, e);
		}
	}

	private static ObjectLocation findObjectLocation(PreparedStatement statement, String hash)
			throws SQLException, IOException {
		statement.setString(1, hash);
		try (ResultSet resultSet = statement.executeQuery()) {
			if (!resultSet.next()) {
				throw new IOException("CAS object not found: " + hash);
			}
			return new ObjectLocation(resultSet.getLong(1), resultSet.getString(2),
					resultSet.getLong(3), resultSet.getLong(4));
		}
	}

	private void materializeLooseObject(String hash, Path object, ObjectLocation location) throws IOException {
		Files.createDirectories(object.getParent());
		Path temporary = Files.createTempFile(object.getParent(), hash, ".tmp");
		try {
			copyFromPack(location, temporary);
			if (!hash.equals(hashFile(temporary))) {
				throw new IOException("Packed CAS integrity check failed: " + hash);
			}
			try {
				Files.move(temporary, object, StandardCopyOption.ATOMIC_MOVE);
			} catch (AtomicMoveNotSupportedException e) {
				Files.move(temporary, object);
			} catch (java.nio.file.FileAlreadyExistsException e) {
				Files.deleteIfExists(temporary);
			}
			setWritable(object, false);
		} finally {
			Files.deleteIfExists(temporary);
		}
	}

	private void copyFromPack(ObjectLocation location, Path target) throws IOException {
		if (location.packName == null) {
			throw new IOException("CAS object has no packed content");
		}
		Path pack = packsDir.resolve(location.packName).normalize();
		if (!pack.startsWith(packsDir) || !Files.isRegularFile(pack)) {
			throw new IOException("CAS pack not found: " + location.packName);
		}
		try (FileChannel input = FileChannel.open(pack, StandardOpenOption.READ);
				FileChannel output = FileChannel.open(target, StandardOpenOption.CREATE,
						StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
			long remaining = location.packLength;
			long position = location.packOffset;
			while (remaining > 0) {
				long transferred = input.transferTo(position, remaining, output);
				if (transferred <= 0) {
					throw new IOException("Unexpected end of CAS pack: " + location.packName);
				}
				position += transferred;
				remaining -= transferred;
			}
		}
		if (Files.size(target) != location.size) {
			throw new IOException("Packed CAS object size mismatch");
		}
	}

	private void configureConnection() throws SQLException {
		try (Statement statement = connection.createStatement()) {
			statement.execute("PRAGMA foreign_keys=ON");
			statement.execute("PRAGMA journal_mode=WAL");
			statement.execute("PRAGMA synchronous=NORMAL");
			statement.execute("PRAGMA busy_timeout=10000");
			statement.execute("PRAGMA temp_store=MEMORY");
		}
	}

	private void migrateSchema() throws SQLException {
		int currentVersion;
		try (Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery("PRAGMA user_version")) {
			currentVersion = resultSet.next() ? resultSet.getInt(1) : 0;
		}
		if (currentVersion > SCHEMA_VERSION) {
			throw new SQLException("Content store schema is newer than this jadx build: " + currentVersion);
		}
		if (currentVersion == SCHEMA_VERSION) {
			return;
		}
		if (currentVersion == 1) {
			migrateSchemaV1ToV2();
			currentVersion = 2;
		}
		if (currentVersion == 2) {
			migrateSchemaV2ToV3();
			currentVersion = 3;
		}
		if (currentVersion == 3) {
			migrateSchemaV3ToV4();
			currentVersion = 4;
		}
		if (currentVersion == 4) {
			migrateSchemaV4ToV5();
			currentVersion = 5;
		}
		if (currentVersion == 5) {
			migrateSchemaV5ToV6();
			return;
		}
		connection.setAutoCommit(false);
		try (Statement statement = connection.createStatement()) {
			statement.execute("CREATE TABLE applications ("
					+ "id INTEGER PRIMARY KEY, app_hash TEXT NOT NULL UNIQUE, display_name TEXT NOT NULL, "
					+ "inputs_json TEXT NOT NULL, first_seen TEXT NOT NULL)");
			statement.execute("CREATE TABLE runs ("
					+ "id INTEGER PRIMARY KEY, application_id INTEGER NOT NULL REFERENCES applications(id), "
					+ "analysis_key TEXT NOT NULL, output_root TEXT NOT NULL, materialization_mode TEXT NOT NULL, "
					+ "created_at TEXT NOT NULL, heartbeat_at TEXT, completed_at TEXT, status TEXT NOT NULL, error TEXT, "
					+ "artifact_manifest_hash TEXT, artifact_source_run_id INTEGER REFERENCES runs(id))");
			statement.execute("CREATE INDEX idx_runs_application ON runs(application_id, id)");
			statement.execute("CREATE INDEX idx_runs_artifact_snapshot "
					+ "ON runs(application_id, artifact_manifest_hash, artifact_source_run_id)");
			statement.execute("CREATE TABLE objects ("
					+ "hash TEXT PRIMARY KEY, size INTEGER NOT NULL, kind TEXT NOT NULL, created_at TEXT NOT NULL, "
					+ "pack_name TEXT, pack_offset INTEGER, pack_length INTEGER)");
			statement.execute("CREATE TABLE artifacts ("
					+ "run_id INTEGER NOT NULL REFERENCES runs(id) ON DELETE CASCADE, path TEXT NOT NULL, "
					+ "object_hash TEXT NOT NULL REFERENCES objects(hash), media_type TEXT NOT NULL, symbol TEXT, "
					+ "PRIMARY KEY(run_id, path))");
			statement.execute("CREATE INDEX idx_artifacts_object ON artifacts(object_hash, run_id)");
			statement.execute("CREATE TABLE application_objects ("
					+ "application_id INTEGER NOT NULL REFERENCES applications(id), "
					+ "object_hash TEXT NOT NULL REFERENCES objects(hash), first_seen_run INTEGER NOT NULL REFERENCES runs(id), "
					+ "PRIMARY KEY(application_id, object_hash)) WITHOUT ROWID");
			statement.execute("CREATE TABLE text_indexed (object_hash TEXT PRIMARY KEY REFERENCES objects(hash))");
			createSearchSchema(statement);
			statement.execute("PRAGMA user_version=" + SCHEMA_VERSION);
			connection.commit();
		} catch (SQLException e) {
			connection.rollback();
			throw e;
		} finally {
			connection.setAutoCommit(true);
		}
	}

	private void migrateSchemaV1ToV2() throws SQLException {
		connection.setAutoCommit(false);
		try (Statement statement = connection.createStatement()) {
			statement.execute("ALTER TABLE objects ADD COLUMN pack_name TEXT");
			statement.execute("ALTER TABLE objects ADD COLUMN pack_offset INTEGER");
			statement.execute("ALTER TABLE objects ADD COLUMN pack_length INTEGER");
			statement.execute("PRAGMA user_version=2");
			connection.commit();
		} catch (SQLException e) {
			connection.rollback();
			throw e;
		} finally {
			connection.setAutoCommit(true);
		}
	}

	private void migrateSchemaV3ToV4() throws SQLException {
		try (Statement statement = connection.createStatement()) {
			statement.execute("PRAGMA user_version=4");
		}
	}

	private void migrateSchemaV4ToV5() throws SQLException {
		connection.setAutoCommit(false);
		try (Statement statement = connection.createStatement()) {
			if (!tableExists("runs")) {
				statement.execute("PRAGMA user_version=5");
				connection.commit();
				return;
			}
			statement.execute("ALTER TABLE runs ADD COLUMN artifact_manifest_hash TEXT");
			statement.execute("ALTER TABLE runs ADD COLUMN artifact_source_run_id INTEGER REFERENCES runs(id)");
			statement.execute("CREATE INDEX idx_runs_artifact_snapshot "
					+ "ON runs(application_id, artifact_manifest_hash, artifact_source_run_id)");
			if (canConsolidateArtifactSnapshots()) {
				consolidateExistingArtifactSnapshots();
			}
			statement.execute("PRAGMA user_version=5");
			connection.commit();
		} catch (SQLException e) {
			connection.rollback();
			throw e;
		} finally {
			connection.setAutoCommit(true);
		}
	}

	private void migrateSchemaV5ToV6() throws SQLException {
		connection.setAutoCommit(false);
		try (Statement statement = connection.createStatement()) {
			if (tableExists("runs") && !columnExists("runs", "heartbeat_at")) {
				statement.execute("ALTER TABLE runs ADD COLUMN heartbeat_at TEXT");
			}
			statement.execute("PRAGMA user_version=6");
			connection.commit();
		} catch (SQLException e) {
			connection.rollback();
			throw e;
		} finally {
			connection.setAutoCommit(true);
		}
	}

	private boolean canConsolidateArtifactSnapshots() throws SQLException {
		return columnExists("runs", "application_id")
				&& columnExists("runs", "status")
				&& columnExists("artifacts", "run_id")
				&& columnExists("artifacts", "path")
				&& columnExists("artifacts", "object_hash")
				&& columnExists("artifacts", "media_type")
				&& columnExists("artifacts", "symbol");
	}

	private void consolidateExistingArtifactSnapshots() throws SQLException {
		List<Long> completedRuns = new ArrayList<>();
		try (Statement statement = connection.createStatement();
				ResultSet resultSet = statement.executeQuery("SELECT id FROM runs WHERE status = 'COMPLETE' ORDER BY id")) {
			while (resultSet.next()) {
				completedRuns.add(resultSet.getLong(1));
			}
		}
		for (long runId : completedRuns) {
			consolidateArtifactSnapshot(runId, false);
		}
	}

	private boolean columnExists(String tableName, String columnName) throws SQLException {
		try (Statement statement = connection.createStatement();
				ResultSet resultSet = statement.executeQuery("PRAGMA table_info(" + tableName + ')')) {
			while (resultSet.next()) {
				if (columnName.equals(resultSet.getString("name"))) {
					return true;
				}
			}
			return false;
		}
	}

	private void migrateSchemaV2ToV3() throws SQLException {
		connection.setAutoCommit(false);
		try (Statement statement = connection.createStatement()) {
			createSearchSchema(statement);
			statement.execute("DROP INDEX IF EXISTS idx_artifacts_symbol");
			if (tableExists("artifacts") && tableExists("runs")) {
				statement.execute("INSERT OR IGNORE INTO object_metadata(object_hash, symbol) "
						+ "SELECT object_hash, MAX(symbol) FROM artifacts WHERE symbol IS NOT NULL GROUP BY object_hash");
				statement.execute("INSERT OR IGNORE INTO search_entries("
						+ "application_id, path, object_hash, symbol, first_seen_run) "
						+ "SELECT r.application_id, ar.path, ar.object_hash, "
						+ "COALESCE(MAX(ar.symbol), MAX(om.symbol)), MIN(ar.run_id) "
						+ "FROM artifacts ar JOIN runs r ON r.id = ar.run_id "
						+ "LEFT JOIN object_metadata om ON om.object_hash = ar.object_hash "
						+ "GROUP BY r.application_id, ar.path, ar.object_hash");
			}
			statement.execute("PRAGMA user_version=3");
			connection.commit();
		} catch (SQLException e) {
			connection.rollback();
			throw e;
		} finally {
			connection.setAutoCommit(true);
		}
	}

	private static void createSearchSchema(Statement statement) throws SQLException {
		statement.execute("CREATE TABLE IF NOT EXISTS store_metadata (key TEXT PRIMARY KEY, value TEXT NOT NULL) WITHOUT ROWID");
		statement.execute("CREATE TABLE IF NOT EXISTS object_metadata ("
				+ "object_hash TEXT PRIMARY KEY REFERENCES objects(hash), symbol TEXT) WITHOUT ROWID");
		statement.execute("CREATE TABLE IF NOT EXISTS search_entries ("
				+ "id INTEGER PRIMARY KEY, application_id INTEGER NOT NULL REFERENCES applications(id), "
				+ "path TEXT NOT NULL, object_hash TEXT NOT NULL REFERENCES objects(hash), symbol TEXT, "
				+ "first_seen_run INTEGER NOT NULL REFERENCES runs(id), "
				+ "UNIQUE(application_id, path, object_hash))");
		statement.execute("CREATE INDEX IF NOT EXISTS idx_search_entries_object "
				+ "ON search_entries(object_hash, application_id)");
	}

	private boolean tableExists(String tableName) throws SQLException {
		try (PreparedStatement statement = connection.prepareStatement(
				"SELECT 1 FROM sqlite_schema WHERE type = 'table' AND name = ?")) {
			statement.setString(1, tableName);
			try (ResultSet resultSet = statement.executeQuery()) {
				return resultSet.next();
			}
		}
	}

	private boolean initializeFts() {
		try (Statement statement = connection.createStatement()) {
			statement.execute("CREATE VIRTUAL TABLE IF NOT EXISTS source_fts "
					+ "USING fts5(object_hash UNINDEXED, symbol, content, tokenize='unicode61')");
			return true;
		} catch (SQLException e) {
			LOG.warn("SQLite FTS5 is unavailable; content search is disabled: {}", e.getMessage());
			return false;
		}
	}

	private boolean initializeStructuredFts() {
		try (Statement statement = connection.createStatement()) {
			statement.execute("CREATE VIRTUAL TABLE IF NOT EXISTS artifact_fts USING fts5("
					+ "path, symbol, content='search_entries', content_rowid='id', tokenize='trigram')");
			statement.execute("CREATE TRIGGER IF NOT EXISTS search_entries_ai AFTER INSERT ON search_entries BEGIN "
					+ "INSERT INTO artifact_fts(rowid, path, symbol) VALUES (new.id, new.path, new.symbol); END");
			statement.execute("CREATE TRIGGER IF NOT EXISTS search_entries_ad AFTER DELETE ON search_entries BEGIN "
					+ "INSERT INTO artifact_fts(artifact_fts, rowid, path, symbol) "
					+ "VALUES ('delete', old.id, old.path, old.symbol); END");
			statement.execute("CREATE TRIGGER IF NOT EXISTS search_entries_au AFTER UPDATE ON search_entries BEGIN "
					+ "INSERT INTO artifact_fts(artifact_fts, rowid, path, symbol) "
					+ "VALUES ('delete', old.id, old.path, old.symbol); "
					+ "INSERT INTO artifact_fts(rowid, path, symbol) VALUES (new.id, new.path, new.symbol); END");
			if (!hasMetadataValue("artifact_fts_version", "1")) {
				statement.execute("INSERT INTO artifact_fts(artifact_fts) VALUES('rebuild')");
				statement.execute("INSERT OR REPLACE INTO store_metadata(key, value) "
						+ "VALUES('artifact_fts_version', '1')");
			}
			return true;
		} catch (SQLException e) {
			LOG.warn("SQLite trigram FTS5 is unavailable; path and symbol search will use LIKE: {}", e.getMessage());
			return false;
		}
	}

	private boolean hasMetadataValue(String key, String value) throws SQLException {
		try (PreparedStatement statement = connection.prepareStatement(
				"SELECT 1 FROM store_metadata WHERE key = ? AND value = ?")) {
			statement.setString(1, key);
			statement.setString(2, value);
			try (ResultSet resultSet = statement.executeQuery()) {
				return resultSet.next();
			}
		}
	}

	private long upsertApplication(String applicationHash, IngestRequest request) throws SQLException {
		String insertSql = "INSERT OR IGNORE INTO applications(app_hash, display_name, inputs_json, first_seen) VALUES(?, ?, ?, ?)";
		try (PreparedStatement insert = connection.prepareStatement(insertSql)) {
			insert.setString(1, applicationHash);
			insert.setString(2, request.getApplicationName());
			insert.setString(3, pathsToJson(request.getInputFiles()));
			insert.setString(4, Instant.now().toString());
			insert.executeUpdate();
		}
		try (PreparedStatement select = connection.prepareStatement("SELECT id FROM applications WHERE app_hash = ?")) {
			select.setString(1, applicationHash);
			try (ResultSet resultSet = select.executeQuery()) {
				if (!resultSet.next()) {
					throw new SQLException("Failed to resolve application id");
				}
				return resultSet.getLong(1);
			}
		}
	}

	private long createRun(long applicationId, IngestRequest request, Path outputRoot) throws SQLException {
		String sql = "INSERT INTO runs(application_id, analysis_key, output_root, materialization_mode, "
				+ "created_at, heartbeat_at, status) VALUES(?, ?, ?, ?, ?, ?, 'INGESTING')";
		try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			statement.setLong(1, applicationId);
			statement.setString(2, request.getAnalysisKey());
			statement.setString(3, outputRoot.toString());
			statement.setString(4, request.getMaterializationMode().name());
			String now = Instant.now().toString();
			statement.setString(5, now);
			statement.setString(6, now);
			statement.executeUpdate();
			try (ResultSet keys = statement.getGeneratedKeys()) {
				if (!keys.next()) {
					throw new SQLException("Failed to create run id");
				}
				return keys.getLong(1);
			}
		}
	}

	private void finishRun(long runId, String status, String error) throws IOException {
		try {
			connection.setAutoCommit(true);
			try (PreparedStatement statement = connection.prepareStatement(
					"UPDATE runs SET status = ?, error = ?, completed_at = ? WHERE id = ?")) {
				statement.setString(1, status);
				statement.setString(2, error);
				statement.setString(3, Instant.now().toString());
				statement.setLong(4, runId);
				statement.executeUpdate();
			}
		} catch (SQLException e) {
			throw storageError("Failed to finish content-store run", e);
		}
	}

	private void consolidateArtifactSnapshot(long runId, boolean completeRun) throws SQLException {
		long applicationId;
		try (PreparedStatement statement = connection.prepareStatement(
				"SELECT application_id FROM runs WHERE id = ?")) {
			statement.setLong(1, runId);
			try (ResultSet resultSet = statement.executeQuery()) {
				if (!resultSet.next()) {
					throw new SQLException("Content-store run not found: " + runId);
				}
				applicationId = resultSet.getLong(1);
			}
		}

		String manifestHash = calculateArtifactManifest(runId);
		Long sourceRunId = findCanonicalArtifactRun(applicationId, manifestHash, runId);
		String sql;
		if (completeRun) {
			sql = "UPDATE runs SET artifact_manifest_hash = ?, artifact_source_run_id = ?, "
					+ "status = 'COMPLETE', error = NULL, completed_at = ? WHERE id = ?";
		} else {
			sql = "UPDATE runs SET artifact_manifest_hash = ?, artifact_source_run_id = ? WHERE id = ?";
		}
		try (PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setString(1, manifestHash);
			if (sourceRunId == null) {
				statement.setNull(2, Types.INTEGER);
			} else {
				statement.setLong(2, sourceRunId);
			}
			if (completeRun) {
				statement.setString(3, Instant.now().toString());
				statement.setLong(4, runId);
			} else {
				statement.setLong(3, runId);
			}
			if (statement.executeUpdate() != 1) {
				throw new SQLException("Failed to finalize content-store run: " + runId);
			}
		}
		if (sourceRunId != null) {
			try (PreparedStatement statement = connection.prepareStatement(
					"DELETE FROM artifacts WHERE run_id = ?")) {
				statement.setLong(1, runId);
				statement.executeUpdate();
			}
		}
	}

	private String calculateArtifactManifest(long runId) throws SQLException {
		MessageDigest digest = sha256();
		String sql = "SELECT path, object_hash, media_type, COALESCE(symbol, '') "
				+ "FROM artifacts WHERE run_id = ? ORDER BY path";
		try (PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setLong(1, runId);
			try (ResultSet resultSet = statement.executeQuery()) {
				while (resultSet.next()) {
					for (int column = 1; column <= 4; column++) {
						updateManifestField(digest, resultSet.getString(column));
					}
				}
			}
		}
		return toHex(digest.digest());
	}

	private static void updateManifestField(MessageDigest digest, String value) {
		byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
		int length = bytes.length;
		digest.update((byte) (length >>> 24));
		digest.update((byte) (length >>> 16));
		digest.update((byte) (length >>> 8));
		digest.update((byte) length);
		digest.update(bytes);
	}

	private @Nullable Long findCanonicalArtifactRun(long applicationId, String manifestHash, long runId)
			throws SQLException {
		String sql = "SELECT id FROM runs WHERE application_id = ? AND artifact_manifest_hash = ? "
				+ "AND artifact_source_run_id IS NULL AND status = 'COMPLETE' AND id <> ? ORDER BY id LIMIT 1";
		try (PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setLong(1, applicationId);
			statement.setString(2, manifestHash);
			statement.setLong(3, runId);
			try (ResultSet resultSet = statement.executeQuery()) {
				return resultSet.next() ? resultSet.getLong(1) : null;
			}
		}
	}

	private void finishRunQuietly(long runId, String status, String error) {
		try {
			finishRun(runId, status, error);
		} catch (Exception finishError) {
			LOG.warn("Failed to mark content-store run {} as {}", runId, status, finishError);
		}
	}

	/**
	 * A process can disappear after a committed ingest batch without running the session close
	 * path. Preserve live concurrent writers, but convert sufficiently old INGESTING rows into
	 * FAILED rows so the normal reachability-based prune can reclaim their artifacts and objects.
	 */
	private void recoverAbandonedRuns() throws IOException {
		try {
			if (!tableExists("runs")
					|| !columnExists("runs", "status")
					|| !columnExists("runs", "created_at")) {
				return;
			}
		} catch (SQLException e) {
			throw storageError("Failed to inspect content-store run schema", e);
		}
		long staleAgeSeconds = Long.getLong(
				STALE_INGEST_AGE_PROPERTY, DEFAULT_STALE_INGEST_AGE_SECONDS);
		if (staleAgeSeconds < 0) {
			throw new IOException("Stale ingest age can't be negative: " + staleAgeSeconds);
		}
		Instant now = Instant.now();
		Instant cutoff = now.minusSeconds(staleAgeSeconds);
		try (PreparedStatement statement = connection.prepareStatement(
				"UPDATE runs SET status = 'FAILED', error = ?, completed_at = ? "
						+ "WHERE status = 'INGESTING' AND COALESCE(heartbeat_at, created_at) <= ?")) {
			statement.setString(1, "Recovered abandoned ingest after process restart");
			statement.setString(2, now.toString());
			statement.setString(3, cutoff.toString());
			int recovered = statement.executeUpdate();
			if (recovered != 0) {
				LOG.info("Recovered {} abandoned content-store ingest run(s)", recovered);
			}
		} catch (SQLException e) {
			throw storageError("Failed to recover abandoned content-store runs", e);
		}
	}

	/**
	 * Reuse the same referentially-safe pruning transaction while retaining every complete run.
	 * This bounds failed-session storage even when callers never issue a manual retention command.
	 */
	private void cleanupFailedRuns() throws IOException {
		try {
			if (!tableExists("runs") || !columnExists("runs", "status")) {
				return;
			}
		} catch (SQLException e) {
			throw storageError("Failed to inspect content-store run schema", e);
		}
		if (!hasRunsWithStatus("FAILED")) {
			return;
		}
		// Automatic failure recovery is part of the ingest/open hot path. Reclaim unreachable
		// rows and object payloads immediately, but leave the full SQLite VACUUM to an explicit
		// maintenance prune: rewriting a large database after every failed run is disproportionate.
		pruneRuns(Integer.MAX_VALUE, false);
	}

	private void cleanupFailedRunsQuietly() {
		try {
			cleanupFailedRuns();
		} catch (IOException e) {
			LOG.warn("Failed to reclaim artifacts from failed content-store runs", e);
		}
	}

	private boolean hasRunsWithStatus(String status) throws IOException {
		try (PreparedStatement statement = connection.prepareStatement(
				"SELECT 1 FROM runs WHERE status = ? LIMIT 1")) {
			statement.setString(1, status);
			try (ResultSet resultSet = statement.executeQuery()) {
				return resultSet.next();
			}
		} catch (SQLException e) {
			throw storageError("Failed to inspect content-store runs", e);
		}
	}

	private void updateRunHeartbeat(long runId) throws SQLException {
		try (PreparedStatement statement = connection.prepareStatement(
				"UPDATE runs SET heartbeat_at = ? WHERE id = ? AND status = 'INGESTING'")) {
			statement.setString(1, Instant.now().toString());
			statement.setLong(2, runId);
			statement.executeUpdate();
		}
	}

	private Stream<Path> walkOutputFiles(Path outputRoot) throws IOException {
		return Files.walk(outputRoot)
				.filter(path -> Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS))
				.filter(path -> !path.toAbsolutePath().normalize().startsWith(root));
	}

	private boolean insertObject(PreparedStatement statement, String hash, long size, String kind) throws SQLException {
		statement.setString(1, hash);
		statement.setLong(2, size);
		statement.setString(3, kind);
		statement.setString(4, Instant.now().toString());
		return statement.executeUpdate() == 1;
	}

	private static void insertArtifact(PreparedStatement statement, long runId, String path,
			String hash, String mediaType, String symbol) throws SQLException {
		statement.setLong(1, runId);
		statement.setString(2, path);
		statement.setString(3, hash);
		statement.setString(4, mediaType);
		statement.setString(5, symbol);
		statement.executeUpdate();
	}

	private static void insertApplicationObject(PreparedStatement statement, long applicationId,
			String hash, long runId) throws SQLException {
		statement.setLong(1, applicationId);
		statement.setString(2, hash);
		statement.setLong(3, runId);
		statement.executeUpdate();
	}

	private static void insertSearchEntry(PreparedStatement statement, long applicationId,
			String path, String hash, String symbol, long runId) throws SQLException {
		statement.setLong(1, applicationId);
		statement.setString(2, path);
		statement.setString(3, hash);
		statement.setString(4, symbol);
		statement.setLong(5, runId);
		statement.executeUpdate();
	}

	private static void upsertObjectSymbol(PreparedStatement statement, String hash, String symbol) throws SQLException {
		statement.setString(1, hash);
		statement.setString(2, symbol);
		statement.executeUpdate();
	}

	private static void updateSearchEntrySymbols(PreparedStatement statement, String hash, String symbol)
			throws SQLException {
		statement.setString(1, symbol);
		statement.setString(2, hash);
		statement.executeUpdate();
	}

	private static @Nullable String findObjectSymbol(PreparedStatement statement, String hash) throws SQLException {
		statement.setString(1, hash);
		try (ResultSet resultSet = statement.executeQuery()) {
			return resultSet.next() ? resultSet.getString(1) : null;
		}
	}

	private static boolean exists(PreparedStatement statement, String hash) throws SQLException {
		statement.setString(1, hash);
		try (ResultSet resultSet = statement.executeQuery()) {
			return resultSet.next();
		}
	}

	private static void insertTextIndex(PreparedStatements statements, String hash, String symbol, String content)
			throws SQLException {
		PreparedStatement insertFts = statements.insertFts;
		if (insertFts == null) {
			return;
		}
		insertFts.setString(1, hash);
		insertFts.setString(2, symbol);
		insertFts.setString(3, content);
		insertFts.executeUpdate();
		statements.insertTextIndex.setString(1, hash);
		statements.insertTextIndex.executeUpdate();
	}

	private void storeObject(Path source, Path object, long expectedSize) throws IOException {
		Files.createDirectories(object.getParent());
		if (Files.exists(object)) {
			ensureExistingObject(source, object, expectedSize);
			return;
		}
		Path temporary = Files.createTempFile(object.getParent(), object.getFileName().toString(), ".tmp");
		try {
			Files.copy(source, temporary, StandardCopyOption.REPLACE_EXISTING);
			if (Files.size(temporary) != expectedSize) {
				throw new IOException("Output changed while storing: " + source);
			}
			try {
				Files.move(temporary, object, StandardCopyOption.ATOMIC_MOVE);
			} catch (AtomicMoveNotSupportedException e) {
				Files.move(temporary, object);
			} catch (java.nio.file.FileAlreadyExistsException e) {
				Files.deleteIfExists(temporary);
			}
			setWritable(object, false);
		} finally {
			Files.deleteIfExists(temporary);
		}
	}

	/**
	 * Move a newly completed output into the CAS and restore its output path as a hard link. Unlike
	 * copy-then-relink this writes the payload only once. If hard links are unavailable, restore a
	 * normal output copy and let the caller report the materialization fallback.
	 */
	private static boolean moveObjectAndRestoreLink(Path source, Path object, long expectedSize) throws IOException {
		Files.createDirectories(object.getParent());
		if (Files.exists(object)) {
			throw new IOException("New CAS object path already exists: " + object);
		}
		try {
			try {
				Files.move(source, object, StandardCopyOption.ATOMIC_MOVE);
			} catch (AtomicMoveNotSupportedException e) {
				Files.move(source, object);
			}
			if (Files.size(object) != expectedSize) {
				throw new IOException("Output changed while storing: " + source);
			}
			setWritable(object, false);
			try {
				Files.createLink(source, object);
				return Files.isSameFile(source, object);
			} catch (Exception e) {
				LOG.debug("Hard-link materialization unavailable for {}: {}", source, e.getMessage());
				Files.copy(object, source, StandardCopyOption.REPLACE_EXISTING);
				setWritable(source, true);
				return false;
			}
		} catch (IOException e) {
			if (!Files.exists(source) && Files.isRegularFile(object)) {
				try {
					Files.copy(object, source, StandardCopyOption.REPLACE_EXISTING);
					setWritable(source, true);
				} catch (IOException restoreError) {
					e.addSuppressed(restoreError);
				}
			}
			throw e;
		}
	}

	private static void ensureExistingObject(Path source, Path object, long expectedSize) throws IOException {
		if (!Files.isRegularFile(object)) {
			throw new IOException("CAS index references a missing object for " + source + ": " + object);
		}
		if (Files.size(object) != expectedSize) {
			throw new IOException("CAS object size mismatch for: " + object);
		}
	}

	private static boolean replaceWithHardLink(Path file, Path object) {
		Path temporaryLink = file.resolveSibling(file.getFileName() + ".jadx-link-" + UUID.randomUUID());
		try {
			if (Files.isSameFile(file, object)) {
				return true;
			}
			Files.createLink(temporaryLink, object);
			try {
				Files.move(temporaryLink, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
			} catch (AtomicMoveNotSupportedException e) {
				Files.move(temporaryLink, file, StandardCopyOption.REPLACE_EXISTING);
			}
			return Files.isSameFile(file, object);
		} catch (Exception e) {
			LOG.debug("Hard-link materialization unavailable for {}: {}", file, e.getMessage());
			return false;
		} finally {
			try {
				Files.deleteIfExists(temporaryLink);
			} catch (IOException e) {
				LOG.debug("Failed to remove temporary hard link: {}", temporaryLink, e);
			}
		}
	}

	private Path preferredObjectPath(String hash) {
		return objectsDir.resolve(hash.substring(0, 2)).resolve(hash);
	}

	private Path newObjectPath(String hash) {
		return objectShardDepth == 1 ? preferredObjectPath(hash) : legacyObjectPath(hash);
	}

	private Path legacyObjectPath(String hash) {
		return objectsDir.resolve(hash.substring(0, 2)).resolve(hash.substring(2, 4)).resolve(hash);
	}

	private static int readObjectShardDepth() throws IOException {
		int depth = Integer.getInteger(OBJECT_SHARD_DEPTH_PROPERTY, 1);
		if (depth != 1 && depth != 2) {
			throw new IOException("Unsupported CAS object shard depth: " + depth + " (expected 1 or 2)");
		}
		return depth;
	}

	private Path existingObjectPath(String hash) {
		Path preferred = preferredObjectPath(hash);
		if (Files.isRegularFile(preferred)) {
			return preferred;
		}
		Path legacy = legacyObjectPath(hash);
		return Files.isRegularFile(legacy) ? legacy : preferred;
	}

	private boolean deleteLooseObject(String hash) throws IOException {
		boolean deleted = false;
		Path preferred = preferredObjectPath(hash);
		Path legacy = legacyObjectPath(hash);
		for (Path object : List.of(preferred, legacy)) {
			setWritable(object, true);
			if (Files.deleteIfExists(object)) {
				deleted = true;
				removeEmptyObjectParents(object.getParent());
			}
		}
		return deleted;
	}

	private void removeEmptyObjectParents(Path directory) {
		Path current = directory;
		while (current != null && !current.equals(objectsDir) && current.startsWith(objectsDir)) {
			try {
				Files.delete(current);
			} catch (IOException e) {
				return;
			}
			current = current.getParent();
		}
	}

	private static String hashApplication(List<Path> inputFiles) throws IOException {
		List<String> hashes = new ArrayList<>(inputFiles.size());
		for (Path input : inputFiles) {
			hashes.add(hashFile(input));
		}
		hashes.sort(String::compareTo);
		MessageDigest digest = sha256();
		for (String hash : hashes) {
			digest.update(hash.getBytes(StandardCharsets.US_ASCII));
			digest.update((byte) 0);
		}
		return toHex(digest.digest());
	}

	private static String hashFile(Path file) throws IOException {
		MessageDigest digest = sha256();
		byte[] buffer = new byte[COPY_BUFFER_SIZE];
		try (InputStream input = Files.newInputStream(file)) {
			int read;
			while ((read = input.read(buffer)) != -1) {
				digest.update(buffer, 0, read);
			}
		}
		return toHex(digest.digest());
	}

	private static MessageDigest sha256() {
		try {
			return MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("SHA-256 is unavailable", e);
		}
	}

	private static String toHex(byte[] bytes) {
		StringBuilder result = new StringBuilder(bytes.length * 2);
		for (byte value : bytes) {
			result.append(Character.forDigit((value >>> 4) & 0xF, 16));
			result.append(Character.forDigit(value & 0xF, 16));
		}
		return result.toString();
	}

	private static String detectKind(String path) {
		String lower = path.toLowerCase(Locale.ROOT);
		int extensionStart = lower.lastIndexOf('.');
		return extensionStart == -1 ? "binary" : lower.substring(extensionStart + 1);
	}

	private static String detectMediaType(String kind) {
		switch (kind) {
			case "java":
				return "text/x-java-source";
			case "xml":
				return "application/xml";
			case "json":
				return "application/json";
			case "smali":
				return "text/x-smali";
			default:
				return "application/octet-stream";
		}
	}

	private static boolean isTextCandidate(String kind, long size) {
		if (size > MAX_INDEXED_TEXT_BYTES) {
			return false;
		}
		switch (kind) {
			case "java":
			case "kt":
			case "xml":
			case "json":
			case "smali":
			case "txt":
			case "properties":
			case "gradle":
			case "kts":
				return true;
			default:
				return false;
		}
	}

	private static String makeSafeFtsQuery(String query) {
		return Stream.of(query.trim().split("\\s+"))
				.filter(token -> !token.isEmpty())
				.map(token -> '"' + token.replace("\"", "\"\"") + '"')
				.collect(Collectors.joining(" AND "));
	}

	private static String makeSafeTrigramQuery(String query) {
		return '"' + query.trim().replace("\"", "\"\"") + '"';
	}

	private static boolean supportsTrigramSearch(String query) {
		String value = query.trim();
		return value.codePointCount(0, value.length()) >= 3;
	}

	private static String escapeLike(String query) {
		return query.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
	}

	private static String normalizeRelativePath(Path path) {
		return path.toString().replace('\\', '/');
	}

	private static String pathsToJson(List<Path> paths) {
		return paths.stream()
				.map(path -> path.toAbsolutePath().normalize().toString())
				.map(value -> value.replace("\\", "\\\\").replace("\"", "\\\""))
				.map(value -> '"' + value + '"')
				.collect(Collectors.joining(",", "[", "]"));
	}

	private static void validateObjectHash(String objectHash) {
		if (!objectHash.matches("[0-9a-f]{64}")) {
			throw new IllegalArgumentException("Invalid SHA-256 object hash");
		}
	}

	private static void setWritable(Path file, boolean writable) {
		try {
			DosFileAttributeView dosView = Files.getFileAttributeView(file, DosFileAttributeView.class);
			if (dosView != null) {
				dosView.setReadOnly(!writable);
			} else {
				file.toFile().setWritable(writable, false);
			}
		} catch (Exception e) {
			LOG.debug("Failed to update CAS object write protection: {}", file, e);
		}
	}

	private static IOException storageError(String message, Exception cause) {
		return new IOException(message, cause);
	}

	private void rollbackQuietly() {
		try {
			connection.rollback();
		} catch (SQLException e) {
			LOG.debug("Content-store rollback failed", e);
		}
	}

	private void setAutoCommitQuietly(boolean value) {
		try {
			connection.setAutoCommit(value);
		} catch (SQLException e) {
			LOG.debug("Failed to set content-store auto-commit to {}", value, e);
		}
	}

	private final class IngestSession implements ContentIngestSession {
		private final IngestRequest request;
		private final Path outputRoot;
		private final MutableStats stats;
		private final PreparedStatements statements;
		private final long startNanos = System.nanoTime();
		private int batchCount;
		private boolean finished;

		private IngestSession(IngestRequest request, Path outputRoot,
				MutableStats stats, PreparedStatements statements) {
			this.request = request;
			this.outputRoot = outputRoot;
			this.stats = stats;
			this.statements = statements;
		}

		@Override
		public void ingest(Path file) throws IOException {
			ingest(file, null, -1);
		}

		@Override
		public void ingest(Path file, String contentHash, long size) throws IOException {
			synchronized (SqliteContentStore.this) {
				ensureActive();
				Path normalizedFile = file.toAbsolutePath().normalize();
				if (!normalizedFile.startsWith(outputRoot)) {
					throw new IOException("Output file is outside the ingest root: " + normalizedFile);
				}
				if (!Files.isRegularFile(normalizedFile, LinkOption.NOFOLLOW_LINKS)) {
					throw new IOException("Output file does not exist: " + normalizedFile);
				}
				try {
					ingestFile(request, outputRoot, normalizedFile, contentHash, size, stats, statements);
					batchCount++;
					if (batchCount == COMMIT_BATCH_SIZE) {
						updateRunHeartbeat(stats.runId);
						connection.commit();
						batchCount = 0;
					}
				} catch (SQLException e) {
					throw storageError("Failed to ingest output file: " + normalizedFile, e);
				}
			}
		}

		@Override
		public IngestStats complete() throws IOException {
			synchronized (SqliteContentStore.this) {
				ensureActive();
				try {
					connection.commit();
					statements.close();
					consolidateArtifactSnapshot(stats.runId, true);
					connection.commit();
					connection.setAutoCommit(true);
					finished = true;
					activeIngestSession = null;
					return stats.toResult((System.nanoTime() - startNanos) / 1_000_000);
				} catch (Exception e) {
					fail(e.getMessage());
					if (e instanceof IOException) {
						throw (IOException) e;
					}
					throw storageError("Failed to complete content-store ingest", e);
				}
			}
		}

		@Override
		public void close() throws IOException {
			synchronized (SqliteContentStore.this) {
				if (!finished) {
					fail("Ingest session closed before completion");
				}
			}
		}

		private void ensureActive() throws IOException {
			if (finished || activeIngestSession != this) {
				throw new IOException("Content-store ingest session is not active");
			}
		}

		private void fail(String error) {
			rollbackQuietly();
			try {
				statements.close();
			} catch (SQLException e) {
				LOG.debug("Failed to close ingest statements", e);
			}
			setAutoCommitQuietly(true);
			finishRunQuietly(stats.runId, "FAILED", error);
			finished = true;
			activeIngestSession = null;
			cleanupFailedRunsQuietly();
		}
	}

	private static final class MutableStats {
		private final long applicationId;
		private final long runId;
		private long artifactCount;
		private long uniqueObjectCount;
		private long reusedObjectCount;
		private long logicalBytes;
		private long newStoredBytes;
		private long indexedObjectCount;
		private long hardLinkCount;
		private long hardLinkFallbackCount;

		private MutableStats(long applicationId, long runId) {
			this.applicationId = applicationId;
			this.runId = runId;
		}

		private IngestStats toResult(long elapsedMillis) {
			return new IngestStats(applicationId, runId, artifactCount, uniqueObjectCount, reusedObjectCount,
					logicalBytes, newStoredBytes, indexedObjectCount,
					hardLinkCount, hardLinkFallbackCount, elapsedMillis);
		}
	}

	private static final class PackEntry {
		private final String hash;
		private final long length;
		private final @Nullable String sourcePackName;
		private final long sourceOffset;
		private String packName;
		private long offset;

		private PackEntry(String hash, long length) {
			this(hash, length, null, 0);
		}

		private PackEntry(String hash, long length, @Nullable String sourcePackName, long sourceOffset) {
			this.hash = hash;
			this.length = length;
			this.sourcePackName = sourcePackName;
			this.sourceOffset = sourceOffset;
		}
	}

	private static final class PackRewritePlan {
		private final List<PackEntry> entries;
		private final List<ObsoletePack> obsoletePacks;

		private PackRewritePlan(List<PackEntry> entries, List<ObsoletePack> obsoletePacks) {
			this.entries = entries;
			this.obsoletePacks = obsoletePacks;
		}
	}

	private static final class ObsoletePack {
		private final Path path;
		private final long garbageBytes;

		private ObsoletePack(Path path, long garbageBytes) {
			this.path = path;
			this.garbageBytes = garbageBytes;
		}
	}

	private static final class PrunedObject {
		private final String hash;
		private final long size;
		private final @Nullable String packName;
		private final long packLength;

		private PrunedObject(String hash, long size, @Nullable String packName, long packLength) {
			this.hash = hash;
			this.size = size;
			this.packName = packName;
			this.packLength = packLength;
		}
	}

	private static final class ObjectLocation {
		private final long size;
		private final @Nullable String packName;
		private final long packOffset;
		private final long packLength;

		private ObjectLocation(long size, @Nullable String packName, long packOffset, long packLength) {
			this.size = size;
			this.packName = packName;
			this.packOffset = packOffset;
			this.packLength = packLength;
		}
	}

	private static final class PreparedStatements implements AutoCloseable {
		private final PreparedStatement insertObject;
		private final PreparedStatement insertArtifact;
		private final PreparedStatement insertApplicationObject;
		private final PreparedStatement insertSearchEntry;
		private final PreparedStatement upsertObjectSymbol;
		private final PreparedStatement updateSearchEntrySymbols;
		private final PreparedStatement selectObjectSymbol;
		private final PreparedStatement hasTextIndex;
		private final PreparedStatement insertTextIndex;
		private final @Nullable PreparedStatement insertFts;
		private final PreparedStatement selectObjectLocation;

		private PreparedStatements(Connection connection, boolean ftsAvailable) throws SQLException {
			insertObject = connection.prepareStatement(
					"INSERT OR IGNORE INTO objects(hash, size, kind, created_at) VALUES(?, ?, ?, ?)");
			insertArtifact = connection.prepareStatement(
					"INSERT INTO artifacts(run_id, path, object_hash, media_type, symbol) VALUES(?, ?, ?, ?, ?)");
			insertApplicationObject = connection.prepareStatement(
					"INSERT OR IGNORE INTO application_objects(application_id, object_hash, first_seen_run) VALUES(?, ?, ?)");
			insertSearchEntry = connection.prepareStatement("INSERT INTO search_entries("
					+ "application_id, path, object_hash, symbol, first_seen_run) VALUES(?, ?, ?, ?, ?) "
					+ "ON CONFLICT(application_id, path, object_hash) DO UPDATE SET symbol = excluded.symbol "
					+ "WHERE search_entries.symbol IS NULL AND excluded.symbol IS NOT NULL");
			upsertObjectSymbol = connection.prepareStatement("INSERT INTO object_metadata(object_hash, symbol) VALUES(?, ?) "
					+ "ON CONFLICT(object_hash) DO UPDATE SET symbol = excluded.symbol "
					+ "WHERE object_metadata.symbol IS NULL AND excluded.symbol IS NOT NULL");
			updateSearchEntrySymbols = connection.prepareStatement(
					"UPDATE search_entries SET symbol = ? WHERE object_hash = ? AND symbol IS NULL");
			selectObjectSymbol = connection.prepareStatement(
					"SELECT symbol FROM object_metadata WHERE object_hash = ?");
			hasTextIndex = connection.prepareStatement("SELECT 1 FROM text_indexed WHERE object_hash = ?");
			insertTextIndex = connection.prepareStatement("INSERT OR IGNORE INTO text_indexed(object_hash) VALUES(?)");
			insertFts = ftsAvailable
					? connection.prepareStatement("INSERT INTO source_fts(object_hash, symbol, content) VALUES(?, ?, ?)")
					: null;
			selectObjectLocation = connection.prepareStatement(
					"SELECT size, pack_name, pack_offset, pack_length FROM objects WHERE hash = ?");
		}

		@Override
		public void close() throws SQLException {
			insertObject.close();
			insertArtifact.close();
			insertApplicationObject.close();
			insertSearchEntry.close();
			upsertObjectSymbol.close();
			updateSearchEntrySymbols.close();
			selectObjectSymbol.close();
			hasTextIndex.close();
			insertTextIndex.close();
			if (insertFts != null) {
				insertFts.close();
			}
			selectObjectLocation.close();
		}
	}
}
