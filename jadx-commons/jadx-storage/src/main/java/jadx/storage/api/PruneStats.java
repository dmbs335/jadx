package jadx.storage.api;

public final class PruneStats {
	private final long runCount;
	private final long objectCount;
	private final long looseBytesUnlinked;
	private final long packedBytesPendingRepack;
	private final long elapsedMillis;

	public PruneStats(long runCount, long objectCount, long looseBytesUnlinked,
			long packedBytesPendingRepack, long elapsedMillis) {
		this.runCount = runCount;
		this.objectCount = objectCount;
		this.looseBytesUnlinked = looseBytesUnlinked;
		this.packedBytesPendingRepack = packedBytesPendingRepack;
		this.elapsedMillis = elapsedMillis;
	}

	public long getRunCount() {
		return runCount;
	}

	public long getObjectCount() {
		return objectCount;
	}

	public long getLooseBytesUnlinked() {
		return looseBytesUnlinked;
	}

	public long getPackedBytesPendingRepack() {
		return packedBytesPendingRepack;
	}

	public long getElapsedMillis() {
		return elapsedMillis;
	}
}
