package jadx.storage.api;

public final class IngestStats {
	private final long applicationId;
	private final long runId;
	private final long artifactCount;
	private final long uniqueObjectCount;
	private final long reusedObjectCount;
	private final long logicalBytes;
	private final long newStoredBytes;
	private final long indexedObjectCount;
	private final long securityFactCount;
	private final long hardLinkCount;
	private final long hardLinkFallbackCount;
	private final long elapsedMillis;

	public IngestStats(long applicationId, long runId, long artifactCount, long uniqueObjectCount,
			long reusedObjectCount, long logicalBytes, long newStoredBytes, long indexedObjectCount,
			long securityFactCount, long hardLinkCount, long hardLinkFallbackCount, long elapsedMillis) {
		this.applicationId = applicationId;
		this.runId = runId;
		this.artifactCount = artifactCount;
		this.uniqueObjectCount = uniqueObjectCount;
		this.reusedObjectCount = reusedObjectCount;
		this.logicalBytes = logicalBytes;
		this.newStoredBytes = newStoredBytes;
		this.indexedObjectCount = indexedObjectCount;
		this.securityFactCount = securityFactCount;
		this.hardLinkCount = hardLinkCount;
		this.hardLinkFallbackCount = hardLinkFallbackCount;
		this.elapsedMillis = elapsedMillis;
	}

	public long getApplicationId() {
		return applicationId;
	}

	public long getRunId() {
		return runId;
	}

	public long getArtifactCount() {
		return artifactCount;
	}

	public long getUniqueObjectCount() {
		return uniqueObjectCount;
	}

	public long getReusedObjectCount() {
		return reusedObjectCount;
	}

	public long getLogicalBytes() {
		return logicalBytes;
	}

	public long getNewStoredBytes() {
		return newStoredBytes;
	}

	public long getIndexedObjectCount() {
		return indexedObjectCount;
	}

	public long getSecurityFactCount() {
		return securityFactCount;
	}

	public long getHardLinkCount() {
		return hardLinkCount;
	}

	public long getHardLinkFallbackCount() {
		return hardLinkFallbackCount;
	}

	public long getElapsedMillis() {
		return elapsedMillis;
	}

	public long getDeduplicatedBytes() {
		return Math.max(0, logicalBytes - newStoredBytes);
	}
}
