package jadx.storage.api;

import java.io.IOException;
import java.nio.file.Path;

public interface ContentIngestSession extends AutoCloseable {
	void ingest(Path file) throws IOException;

	/**
	 * Ingest a file using SHA-256 and size captured while that exact output was written. Callers must
	 * only use this overload for immutable, completed output files owned by the current session.
	 */
	default void ingest(Path file, String contentHash, long size) throws IOException {
		ingest(file);
	}

	IngestStats complete() throws IOException;

	@Override
	void close() throws IOException;
}
