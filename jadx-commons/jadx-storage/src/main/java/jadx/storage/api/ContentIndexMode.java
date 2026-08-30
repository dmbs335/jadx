package jadx.storage.api;

/** Selects optional search indexing performed while artifacts enter the CAS. */
public enum ContentIndexMode {
	/** Hash, deduplicate, and retain provenance only. */
	NONE,
	/** Also build the SQLite FTS5 source index. */
	FULL_TEXT
}
