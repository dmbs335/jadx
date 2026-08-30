# jadx content store

This module stores generated artifacts once by SHA-256 and keeps application/run provenance in SQLite.
Text content is indexed once per unique object with FTS5, while path and symbol substring search uses a compact
trigram index over application-level provenance. Repeated runs retain materialization history without duplicating
search rows. Security analysis belongs to analysis plugins and is deliberately outside this storage module.

CLI usage:

```bash
jadx app.apk -d out --content-store-dir /data/jadx-store
```

`--content-store-hardlink` replaces generated files with read-only hard links to CAS objects when the output
and store are on a file system that supports hard links. If linking is unavailable, the ordinary output file is kept.
The default mode never changes generated files. Hard-link mode is intended for immutable analysis outputs; materialize
an object to a new writable file before editing it.

The default index mode is `none`, which keeps ingest focused on hashing, provenance, and deduplication. Use
`--content-store-index full-text` when interactive cross-application source search is needed.

The store directory must be outside the output directory. It contains `index.sqlite`, two-level-sharded loose objects
under `objects/`, and immutable compacted files under `packs/`. SQLite uses WAL mode, batched writes, prepared
statements, and a versioned schema.

Store commands do not require an APK input:

```bash
# FTS5 when available, with indexed path/symbol fallback
jadx --content-store-dir /data/jadx-store --content-store-search addJavascriptInterface

# Verify loose objects, write immutable packs, commit locations, then remove loose copies
jadx --content-store-dir /data/jadx-store --content-store-compact --content-store-pack-size-mib 512

jadx --content-store-dir /data/jadx-store --content-store-stats
jadx --content-store-dir /data/jadx-store --content-store-materialize-run 1 --content-store-materialize-dir restored
```

The GUI exposes the same source search under **Tools → Content Store Search**. Select a store directory and search
without blocking the Swing event thread. Set `-Djadx.content.store=/data/jadx-store` to prefill the directory.

Compaction is crash-safe by ordering operations as pack write and SHA-256 verification, fsync, atomic rename,
SQLite commit, and finally loose-object deletion. A failure can leave redundant data but never a committed reference
to an incomplete pack. The schema migrates existing stores in place. Legacy analysis tables are retained during the
version-4 migration to avoid deleting user data, but storage no longer reads or writes them.
