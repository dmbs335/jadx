package jadx.storage.api;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface ContentStore extends AutoCloseable {
	IngestStats ingest(IngestRequest request) throws IOException;

	List<SearchResult> search(String query, int limit) throws IOException;

	List<SecurityFactResult> findSecurityFacts(long applicationId, String kind, int limit) throws IOException;

	StoreStats getStats() throws IOException;

	CompactionStats compact(long maxPackBytes) throws IOException;

	void materializeObject(String objectHash, Path target) throws IOException;

	long materializeRun(long runId, Path targetDirectory) throws IOException;

	@Override
	void close() throws IOException;
}
