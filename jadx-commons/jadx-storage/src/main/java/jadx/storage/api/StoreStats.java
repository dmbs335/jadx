package jadx.storage.api;

public final class StoreStats {
	private final long applicationCount;
	private final long runCount;
	private final long objectCount;
	private final long logicalBytes;
	private final long packedObjectCount;
	private final long packedBytes;

	public StoreStats(long applicationCount, long runCount, long objectCount, long logicalBytes,
			long packedObjectCount, long packedBytes) {
		this.applicationCount = applicationCount;
		this.runCount = runCount;
		this.objectCount = objectCount;
		this.logicalBytes = logicalBytes;
		this.packedObjectCount = packedObjectCount;
		this.packedBytes = packedBytes;
	}

	public long getApplicationCount() {
		return applicationCount;
	}

	public long getRunCount() {
		return runCount;
	}

	public long getObjectCount() {
		return objectCount;
	}

	public long getLogicalBytes() {
		return logicalBytes;
	}

	public long getPackedObjectCount() {
		return packedObjectCount;
	}

	public long getPackedBytes() {
		return packedBytes;
	}
}
