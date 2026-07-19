package jadx.storage.api;

public final class SecurityFactResult {
	private final long applicationId;
	private final String path;
	private final String objectHash;
	private final SecurityFact fact;

	public SecurityFactResult(long applicationId, String path, String objectHash, SecurityFact fact) {
		this.applicationId = applicationId;
		this.path = path;
		this.objectHash = objectHash;
		this.fact = fact;
	}

	public long getApplicationId() {
		return applicationId;
	}

	public String getPath() {
		return path;
	}

	public String getObjectHash() {
		return objectHash;
	}

	public SecurityFact getFact() {
		return fact;
	}
}
