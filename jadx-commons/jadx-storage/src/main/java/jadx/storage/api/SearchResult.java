package jadx.storage.api;

public final class SearchResult {
	private final long applicationId;
	private final String applicationName;
	private final String path;
	private final String objectHash;
	private final String snippet;

	public SearchResult(long applicationId, String applicationName, String path, String objectHash, String snippet) {
		this.applicationId = applicationId;
		this.applicationName = applicationName;
		this.path = path;
		this.objectHash = objectHash;
		this.snippet = snippet;
	}

	public long getApplicationId() {
		return applicationId;
	}

	public String getApplicationName() {
		return applicationName;
	}

	public String getPath() {
		return path;
	}

	public String getObjectHash() {
		return objectHash;
	}

	public String getSnippet() {
		return snippet;
	}
}
