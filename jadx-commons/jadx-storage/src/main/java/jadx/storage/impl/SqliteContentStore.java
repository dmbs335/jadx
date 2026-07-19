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
import java.nio.file.attribute.DosFileAttributeView;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jadx.storage.api.CompactionStats;
import jadx.storage.api.ContentIndexMode;
import jadx.storage.api.ContentStore;
import jadx.storage.api.IngestRequest;
import jadx.storage.api.IngestStats;
import jadx.storage.api.MaterializationMode;
import jadx.storage.api.SearchResult;
import jadx.storage.api.SecurityFact;
import jadx.storage.api.SecurityFactResult;
import jadx.storage.api.StoreStats;

public final class SqliteContentStore implements ContentStore {
	private static final Logger LOG = LoggerFactory.getLogger(SqliteContentStore.class);
	private static final int SCHEMA_VERSION = 3;
	private static final int COMMIT_BATCH_SIZE = 512;
	private static final long MAX_INDEXED_TEXT_BYTES = 4L * 1024 * 1024;
	private static final int COPY_BUFFER_SIZE = 64 * 1024;

	private final Path root;
	private final Path objectsDir;
	private final Path packsDir;
	private final Connection connection;
	private final boolean ftsAvailable;
	private final boolean structuredFtsAvailable;

	private SqliteContentStore(Path root, Connection connection) throws SQLException, IOException {
		this.root = root;
		this.objectsDir = root.resolve("objects");
		this.packsDir = root.resolve("packs");
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
		try {
			Files.createDirectories(normalizedRoot);
			Class.forName("org.sqlite.JDBC");
			Path database = normalizedRoot.resolve("index.sqlite");
			Connection connection = DriverManager.getConnection("jdbc:sqlite:" + database);
			return new SqliteContentStore(normalizedRoot, connection);
		} catch (ClassNotFoundException | SQLException e) {
			throw new IOException("SQLite content store is unavailable", e);
		}
	}

	@Override
	public synchronized IngestStats ingest(IngestRequest request) throws IOException {
		long startNanos = System.nanoTime();
		Path outputRoot = request.getOutputDirectory().toAbsolutePath().normalize();
		if (!Files.isDirectory(outputRoot)) {
			throw new IOException("Output directory does not exist: " + outputRoot);
		}
		if (root.startsWith(outputRoot)) {
			throw new IOException("Content store must be outside the output directory: " + root);
		}
		String applicationHash = hashApplication(request.getInputFiles());
		long applicationId;
		long runId;
		try {
			connection.setAutoCommit(false);
			applicationId = upsertApplication(applicationHash, request);
			runId = createRun(applicationId, request, outputRoot);
			connection.commit();
		} catch (SQLException e) {
			rollbackQuietly();
			throw storageError("Failed to create content-store run", e);
		}

		MutableStats stats = new MutableStats(applicationId, runId);
		try {
			connection.setAutoCommit(false);
			try (Stream<Path> files = walkOutputFiles(outputRoot);
					PreparedStatements statements = new PreparedStatements(connection, ftsAvailable)) {
				int batchCount = 0;
				Iterator<Path> iterator = files.iterator();
				while (iterator.hasNext()) {
					Path file = iterator.next();
					ingestFile(request, outputRoot, file, stats, statements);
					batchCount++;
					if (batchCount == COMMIT_BATCH_SIZE) {
						connection.commit();
						batchCount = 0;
					}
				}
				connection.commit();
			}
			finishRun(runId, "COMPLETE", null);
		} catch (Exception e) {
			rollbackQuietly();
			finishRunQuietly(runId, "FAILED", e.getMessage());
			if (e instanceof IOException) {
				throw (IOException) e;
			}
			throw storageError("Failed to ingest output tree", e);
		} finally {
			setAutoCommitQuietly(true);
		}
		return stats.toResult((System.nanoTime() - startNanos) / 1_000_000);
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
	public synchronized List<SecurityFactResult> findSecurityFacts(long applicationId, String kind, int limit)
			throws IOException {
		String sql = "SELECT DISTINCT ao.application_id, ar.path, f.object_hash, f.kind, f.value, f.line, f.detail "
				+ "FROM analysis_facts f "
				+ "JOIN application_objects ao ON ao.object_hash = f.object_hash "
				+ "JOIN runs r ON r.application_id = ao.application_id "
				+ "JOIN artifacts ar ON ar.run_id = r.id AND ar.object_hash = f.object_hash "
				+ "WHERE (? <= 0 OR ao.application_id = ?) AND (? = '' OR f.kind = ?) "
				+ "ORDER BY f.kind, ar.path, f.line LIMIT ?";
		try (PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setLong(1, applicationId);
			statement.setLong(2, applicationId);
			statement.setString(3, kind);
			statement.setString(4, kind);
			statement.setInt(5, Math.max(0, limit));
			try (ResultSet resultSet = statement.executeQuery()) {
				List<SecurityFactResult> results = new ArrayList<>();
				while (resultSet.next()) {
					SecurityFact fact = new SecurityFact(
							resultSet.getString(4), resultSet.getString(5), resultSet.getInt(6), resultSet.getString(7));
					results.add(new SecurityFactResult(
							resultSet.getLong(1), resultSet.getString(2), resultSet.getString(3), fact));
				}
				return results;
			}
		} catch (SQLException e) {
			throw storageError("Security fact query failed", e);
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
		List<PackEntry> entries = loadLooseObjects();
		if (entries.isEmpty()) {
			long deletedBytes = deletePackedLooseObjects();
			return new CompactionStats(0, 0, deletedBytes, 0,
					(System.nanoTime() - startNanos) / 1_000_000);
		}
		List<Path> createdPacks = new ArrayList<>();
		try {
			writePacks(entries, maxPackBytes, createdPacks);
			connection.setAutoCommit(false);
			try (PreparedStatement update = connection.prepareStatement(
					"UPDATE objects SET pack_name = ?, pack_offset = ?, pack_length = ? "
							+ "WHERE hash = ? AND pack_name IS NULL")) {
				for (PackEntry entry : entries) {
					update.setString(1, entry.packName);
					update.setLong(2, entry.offset);
					update.setLong(3, entry.length);
					update.setString(4, entry.hash);
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
		long packedBytes = entries.stream().mapToLong(entry -> entry.length).sum();
		return new CompactionStats(entries.size(), packedBytes, deletedBytes, createdPacks.size(),
				(System.nanoTime() - startNanos) / 1_000_000);
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
				Path loose = objectPath(hash);
				try {
					setWritable(loose, true);
					if (Files.deleteIfExists(loose)) {
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
		Path object = objectPath(objectHash);
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
		String sql = "SELECT path, object_hash FROM artifacts WHERE run_id = ? ORDER BY path";
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
		try {
			connection.close();
		} catch (SQLException e) {
			throw storageError("Failed to close content store", e);
		}
	}

	private void ingestFile(IngestRequest request, Path outputRoot, Path file,
			MutableStats stats, PreparedStatements statements) throws IOException, SQLException {
		long size = Files.size(file);
		String hash = hashFile(file);
		String relativePath = normalizeRelativePath(outputRoot.relativize(file));
		String kind = detectKind(relativePath);
		String mediaType = detectMediaType(kind);
		stats.artifactCount++;
		stats.logicalBytes += size;

		boolean newObject = insertObject(statements.insertObject, hash, size, kind);
		Path object = objectPath(hash);
		if (newObject) {
			storeObject(file, object, size);
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

		TextArtifactAnalyzer.Analysis analysis = null;
		boolean textCandidate = isTextCandidate(kind, size);
		boolean needsTextIndex = textCandidate
				&& request.getIndexMode() == ContentIndexMode.FULL_TEXT
				&& ftsAvailable
				&& !exists(statements.hasTextIndex, hash);
		boolean needsSecurityAnalysis = textCandidate
				&& request.getIndexMode() != ContentIndexMode.NONE
				&& !existsAnalyzerCache(statements.hasAnalyzerCache, hash);
		if (needsTextIndex || needsSecurityAnalysis) {
			String content = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
			analysis = TextArtifactAnalyzer.analyze(relativePath, content);
			if (needsTextIndex) {
				insertTextIndex(statements, hash, analysis.getSymbol(), content);
				stats.indexedObjectCount++;
			}
			if (needsSecurityAnalysis) {
				stats.securityFactCount += insertSecurityAnalysis(statements, hash, analysis);
			}
		}
		String symbol;
		if (analysis == null) {
			symbol = findObjectSymbol(statements.selectObjectSymbol, hash);
		} else {
			symbol = analysis.getSymbol();
			upsertObjectSymbol(statements.upsertObjectSymbol, hash, symbol);
			updateSearchEntrySymbols(statements.updateSearchEntrySymbols, hash, symbol);
		}
		insertArtifact(statements.insertArtifact, stats.runId, relativePath, hash, mediaType, symbol);
		insertApplicationObject(statements.insertApplicationObject, stats.applicationId, hash, stats.runId);
		insertSearchEntry(statements.insertSearchEntry, stats.applicationId, relativePath, hash, symbol, stats.runId);

		if (request.getMaterializationMode() == MaterializationMode.HARD_LINK) {
			if (replaceWithHardLink(file, object)) {
				stats.hardLinkCount++;
			} else {
				stats.hardLinkFallbackCount++;
			}
		}
	}

	private List<PackEntry> loadLooseObjects() throws IOException {
		String sql = "SELECT hash, size FROM objects WHERE pack_name IS NULL ORDER BY hash";
		try (Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(sql)) {
			List<PackEntry> entries = new ArrayList<>();
			while (resultSet.next()) {
				String hash = resultSet.getString(1);
				long size = resultSet.getLong(2);
				Path loose = objectPath(hash);
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

	private void writePacks(List<PackEntry> entries, long maxPackBytes, List<Path> createdPacks) throws IOException {
		int entryIndex = 0;
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
						copyToPack(entry, channel);
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
	}

	private void copyToPack(PackEntry entry, FileChannel channel) throws IOException {
		MessageDigest digest = sha256();
		byte[] bytes = new byte[COPY_BUFFER_SIZE];
		try (InputStream input = Files.newInputStream(objectPath(entry.hash))) {
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
					+ "created_at TEXT NOT NULL, completed_at TEXT, status TEXT NOT NULL, error TEXT)");
			statement.execute("CREATE INDEX idx_runs_application ON runs(application_id, id)");
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
			statement.execute("CREATE TABLE analysis_facts ("
					+ "object_hash TEXT NOT NULL REFERENCES objects(hash), analyzer_id TEXT NOT NULL, "
					+ "analyzer_version TEXT NOT NULL, kind TEXT NOT NULL, value TEXT NOT NULL, "
					+ "line INTEGER NOT NULL, detail TEXT NOT NULL, "
					+ "PRIMARY KEY(object_hash, analyzer_id, analyzer_version, kind, value, line)) WITHOUT ROWID");
			statement.execute("CREATE INDEX idx_analysis_facts_lookup ON analysis_facts(kind, value, object_hash)");
			statement.execute("CREATE TABLE analyzer_cache ("
					+ "object_hash TEXT NOT NULL REFERENCES objects(hash), analyzer_id TEXT NOT NULL, "
					+ "analyzer_version TEXT NOT NULL, options_hash TEXT NOT NULL, completed_at TEXT NOT NULL, "
					+ "PRIMARY KEY(object_hash, analyzer_id, analyzer_version, options_hash)) WITHOUT ROWID");
			statement.execute("CREATE TABLE text_indexed (object_hash TEXT PRIMARY KEY REFERENCES objects(hash))");
			createSearchSchema(statement);
			statement.execute("CREATE TABLE findings ("
					+ "id INTEGER PRIMARY KEY, run_id INTEGER NOT NULL REFERENCES runs(id) ON DELETE CASCADE, "
					+ "rule_id TEXT NOT NULL, severity TEXT NOT NULL, object_hash TEXT REFERENCES objects(hash), "
					+ "location TEXT NOT NULL, evidence TEXT NOT NULL, UNIQUE(run_id, rule_id, location, evidence))");
			statement.execute("CREATE INDEX idx_findings_rule ON findings(rule_id, severity, run_id)");
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
		String sql = "INSERT INTO runs(application_id, analysis_key, output_root, materialization_mode, created_at, status) "
				+ "VALUES(?, ?, ?, ?, ?, 'INGESTING')";
		try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			statement.setLong(1, applicationId);
			statement.setString(2, request.getAnalysisKey());
			statement.setString(3, outputRoot.toString());
			statement.setString(4, request.getMaterializationMode().name());
			statement.setString(5, Instant.now().toString());
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

	private void finishRunQuietly(long runId, String status, String error) {
		try {
			finishRun(runId, status, error);
		} catch (Exception finishError) {
			LOG.warn("Failed to mark content-store run {} as {}", runId, status, finishError);
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

	private static boolean existsAnalyzerCache(PreparedStatement statement, String hash) throws SQLException {
		statement.setString(1, hash);
		statement.setString(2, TextArtifactAnalyzer.getAnalyzerId());
		statement.setString(3, TextArtifactAnalyzer.getAnalyzerVersion());
		statement.setString(4, "default");
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

	private static int insertSecurityAnalysis(PreparedStatements statements, String hash,
			TextArtifactAnalyzer.Analysis analysis) throws SQLException {
		int count = 0;
		for (SecurityFact fact : analysis.getFacts()) {
			statements.insertFact.setString(1, hash);
			statements.insertFact.setString(2, TextArtifactAnalyzer.getAnalyzerId());
			statements.insertFact.setString(3, TextArtifactAnalyzer.getAnalyzerVersion());
			statements.insertFact.setString(4, fact.getKind());
			statements.insertFact.setString(5, fact.getValue());
			statements.insertFact.setInt(6, fact.getLine());
			statements.insertFact.setString(7, fact.getDetail());
			count += statements.insertFact.executeUpdate();
		}
		statements.insertAnalyzerCache.setString(1, hash);
		statements.insertAnalyzerCache.setString(2, TextArtifactAnalyzer.getAnalyzerId());
		statements.insertAnalyzerCache.setString(3, TextArtifactAnalyzer.getAnalyzerVersion());
		statements.insertAnalyzerCache.setString(4, "default");
		statements.insertAnalyzerCache.setString(5, Instant.now().toString());
		statements.insertAnalyzerCache.executeUpdate();
		return count;
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

	private Path objectPath(String hash) {
		return objectsDir.resolve(hash.substring(0, 2)).resolve(hash.substring(2, 4)).resolve(hash);
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

	private static final class MutableStats {
		private final long applicationId;
		private final long runId;
		private long artifactCount;
		private long uniqueObjectCount;
		private long reusedObjectCount;
		private long logicalBytes;
		private long newStoredBytes;
		private long indexedObjectCount;
		private long securityFactCount;
		private long hardLinkCount;
		private long hardLinkFallbackCount;

		private MutableStats(long applicationId, long runId) {
			this.applicationId = applicationId;
			this.runId = runId;
		}

		private IngestStats toResult(long elapsedMillis) {
			return new IngestStats(applicationId, runId, artifactCount, uniqueObjectCount, reusedObjectCount,
					logicalBytes, newStoredBytes, indexedObjectCount, securityFactCount,
					hardLinkCount, hardLinkFallbackCount, elapsedMillis);
		}
	}

	private static final class PackEntry {
		private final String hash;
		private final long length;
		private String packName;
		private long offset;

		private PackEntry(String hash, long length) {
			this.hash = hash;
			this.length = length;
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
		private final PreparedStatement hasAnalyzerCache;
		private final PreparedStatement insertAnalyzerCache;
		private final PreparedStatement insertFact;
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
			hasAnalyzerCache = connection.prepareStatement("SELECT 1 FROM analyzer_cache "
					+ "WHERE object_hash = ? AND analyzer_id = ? AND analyzer_version = ? AND options_hash = ?");
			insertAnalyzerCache = connection.prepareStatement("INSERT OR IGNORE INTO analyzer_cache("
					+ "object_hash, analyzer_id, analyzer_version, options_hash, completed_at) VALUES(?, ?, ?, ?, ?)");
			insertFact = connection.prepareStatement("INSERT OR IGNORE INTO analysis_facts("
					+ "object_hash, analyzer_id, analyzer_version, kind, value, line, detail) VALUES(?, ?, ?, ?, ?, ?, ?)");
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
			hasAnalyzerCache.close();
			insertAnalyzerCache.close();
			insertFact.close();
			selectObjectLocation.close();
		}
	}
}
