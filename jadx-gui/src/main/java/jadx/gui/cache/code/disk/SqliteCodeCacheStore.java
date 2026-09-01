package jadx.gui.cache.code.disk;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import jadx.core.utils.files.FileUtils;

final class SqliteCodeCacheStore implements AutoCloseable {
	private static final int MAX_BUNDLE_SIZE = 256 * 1024 * 1024;
	private final Path dbFile;
	private Connection connection;
	private PreparedStatement readStatement;
	private PreparedStatement containsStatement;
	private PreparedStatement writeStatement;
	private PreparedStatement deleteStatement;

	SqliteCodeCacheStore(Path dbFile) {
		this.dbFile = dbFile;
		open();
	}

	private void open() {
		try {
			FileUtils.makeDirsForFile(dbFile);
			connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.toAbsolutePath());
			try (Statement statement = connection.createStatement()) {
				statement.execute("PRAGMA journal_mode=WAL");
				statement.execute("PRAGMA synchronous=NORMAL");
				statement.execute("PRAGMA temp_store=MEMORY");
				statement.execute("PRAGMA busy_timeout=10000");
				statement.execute("PRAGMA wal_autocheckpoint=2048");
				statement.execute("CREATE TABLE IF NOT EXISTS code_entries ("
						+ "cls_id INTEGER PRIMARY KEY, "
						+ "raw_size INTEGER NOT NULL, "
						+ "bundle BLOB NOT NULL) WITHOUT ROWID");
			}
			readStatement = connection.prepareStatement("SELECT raw_size, bundle FROM code_entries WHERE cls_id = ?");
			containsStatement = connection.prepareStatement("SELECT 1 FROM code_entries WHERE cls_id = ?");
			writeStatement = connection.prepareStatement(
					"INSERT INTO code_entries(cls_id, raw_size, bundle) VALUES(?, ?, ?) "
							+ "ON CONFLICT(cls_id) DO UPDATE SET raw_size = excluded.raw_size, bundle = excluded.bundle");
			deleteStatement = connection.prepareStatement("DELETE FROM code_entries WHERE cls_id = ?");
		} catch (SQLException e) {
			throw new IllegalStateException("Failed to initialize SQLite code cache", e);
		}
	}

	private void ensureOpen() {
		try {
			if (connection == null || connection.isClosed()) {
				open();
			}
		} catch (SQLException e) {
			throw new IllegalStateException("Failed to check SQLite code cache state", e);
		}
	}

	synchronized List<Integer> loadIds() throws SQLException {
		ensureOpen();
		List<Integer> ids = new ArrayList<>();
		try (Statement statement = connection.createStatement();
				ResultSet result = statement.executeQuery("SELECT cls_id FROM code_entries")) {
			while (result.next()) {
				ids.add(result.getInt(1));
			}
		}
		return ids;
	}

	synchronized byte[] read(int clsId) throws SQLException {
		ensureOpen();
		readStatement.setInt(1, clsId);
		try (ResultSet result = readStatement.executeQuery()) {
			return result.next() ? decompress(result.getBytes(2), result.getInt(1)) : null;
		}
	}

	synchronized boolean contains(int clsId) throws SQLException {
		ensureOpen();
		containsStatement.setInt(1, clsId);
		try (ResultSet result = containsStatement.executeQuery()) {
			return result.next();
		}
	}

	synchronized void write(int clsId, CodeMetadataAdapter.CacheBundle bundle) throws SQLException {
		ensureOpen();
		byte[] compressed = compress(bundle.getData(), bundle.getSize());
		writeStatement.setInt(1, clsId);
		writeStatement.setInt(2, bundle.getSize());
		writeStatement.setBytes(3, compressed);
		writeStatement.executeUpdate();
	}

	private static byte[] compress(byte[] bundle, int bundleSize) throws SQLException {
		try (ByteArrayOutputStream bytes = new ByteArrayOutputStream(Math.max(64, bundleSize / 2));
				DeflaterOutputStream out = new DeflaterOutputStream(
						bytes, new Deflater(Deflater.BEST_SPEED), 8192)) {
			out.write(bundle, 0, bundleSize);
			out.finish();
			return bytes.toByteArray();
		} catch (IOException e) {
			throw new SQLException("Failed to compress code cache bundle", e);
		}
	}

	private static byte[] decompress(byte[] compressed, int rawSize) throws SQLException {
		if (rawSize < 0 || rawSize > MAX_BUNDLE_SIZE) {
			throw new SQLException("Invalid code cache bundle size: " + rawSize);
		}
		byte[] bundle = new byte[rawSize];
		try (InflaterInputStream in = new InflaterInputStream(new ByteArrayInputStream(compressed))) {
			int offset = 0;
			while (offset < rawSize) {
				int read = in.read(bundle, offset, rawSize - offset);
				if (read == -1) {
					throw new IOException("Compressed code cache bundle ended early");
				}
				offset += read;
			}
			if (in.read() != -1) {
				throw new IOException("Compressed code cache bundle exceeds declared size");
			}
			return bundle;
		} catch (IOException e) {
			throw new SQLException("Failed to decompress code cache bundle", e);
		}
	}

	synchronized void delete(int clsId) throws SQLException {
		ensureOpen();
		deleteStatement.setInt(1, clsId);
		deleteStatement.executeUpdate();
	}

	@Override
	public synchronized void close() throws IOException {
		if (connection == null) {
			return;
		}
		try {
			// Closing the connection also closes every prepared statement. Doing this as one JDBC
			// operation avoids leaking the connection if an individual statement close fails.
			connection.close();
		} catch (SQLException e) {
			throw new IOException("Failed to close SQLite code cache", e);
		} finally {
			connection = null;
			readStatement = null;
			containsStatement = null;
			writeStatement = null;
			deleteStatement = null;
		}
	}
}
