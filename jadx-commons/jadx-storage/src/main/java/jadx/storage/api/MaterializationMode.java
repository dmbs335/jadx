package jadx.storage.api;

/** Controls how an ingested output tree is retained. */
public enum MaterializationMode {
	/** Keep ordinary output files. The CAS and index are populated without changing the tree. */
	KEEP,
	/**
	 * Replace output files with read-only hard links to CAS objects when the file system supports it.
	 */
	HARD_LINK
}
