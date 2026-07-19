package jadx.storage.api;

public final class CompactionStats {
	private final long objectCount;
	private final long packedBytes;
	private final long looseBytesDeleted;
	private final int packCount;
	private final long elapsedMillis;

	public CompactionStats(long objectCount, long packedBytes, long looseBytesDeleted,
			int packCount, long elapsedMillis) {
		this.objectCount = objectCount;
		this.packedBytes = packedBytes;
		this.looseBytesDeleted = looseBytesDeleted;
		this.packCount = packCount;
		this.elapsedMillis = elapsedMillis;
	}

	public long getObjectCount() {
		return objectCount;
	}

	public long getPackedBytes() {
		return packedBytes;
	}

	public long getLooseBytesDeleted() {
		return looseBytesDeleted;
	}

	public int getPackCount() {
		return packCount;
	}

	public long getElapsedMillis() {
		return elapsedMillis;
	}
}
