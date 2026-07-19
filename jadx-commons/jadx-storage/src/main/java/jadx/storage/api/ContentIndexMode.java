package jadx.storage.api;

/** Selects reusable analysis performed while artifacts enter the CAS. */
public enum ContentIndexMode {
	/** Hash, deduplicate, and retain provenance only. */
	NONE,
	/** Also extract reusable security facts once per unique text object. */
	SECURITY,
	/** Also build the SQLite FTS5 source index. */
	FULL_TEXT
}
