package jadx.storage.impl;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import jadx.storage.api.ContentIngestSession;
import jadx.storage.api.IngestStats;

/**
 * Collects completed output paths during a parallel decompile wave and ingests them at the
 * following task barrier. This avoids CAS I/O competing with decompiler workers while bounding
 * the amount of duplicated output to one wave.
 */
public final class WaveContentIngestSession implements AutoCloseable {
	private final ContentIngestSession session;
	private final Queue<OutputFile> pending = new ConcurrentLinkedQueue<>();
	private final AtomicBoolean terminal = new AtomicBoolean();
	private volatile IOException failure;

	public WaveContentIngestSession(ContentIngestSession session) {
		this.session = session;
	}

	public void submit(Path file) throws IOException {
		submit(file, null, -1);
	}

	public void submit(Path file, String contentHash, long size) throws IOException {
		if (terminal.get()) {
			throw new IOException("Content ingest is already finished");
		}
		IOException currentFailure = failure;
		if (currentFailure != null) {
			throw currentFailure;
		}
		pending.add(new OutputFile(file, contentHash, size));
	}

	public synchronized void checkpoint() throws IOException {
		if (terminal.get()) {
			throw new IOException("Content ingest is already finished");
		}
		drainPending();
	}

	public synchronized IngestStats complete() throws IOException {
		if (!terminal.compareAndSet(false, true)) {
			throw new IOException("Content ingest is already finished");
		}
		try {
			drainPending();
			return session.complete();
		} catch (IOException e) {
			session.close();
			throw e;
		}
	}

	@Override
	public synchronized void close() throws IOException {
		if (terminal.compareAndSet(false, true)) {
			pending.clear();
			session.close();
		}
	}

	private void drainPending() throws IOException {
		if (failure != null) {
			throw failure;
		}
		OutputFile file;
		while ((file = pending.poll()) != null) {
			try {
				if (file.contentHash == null) {
					session.ingest(file.path);
				} else {
					session.ingest(file.path, file.contentHash, file.size);
				}
			} catch (IOException e) {
				failure = e;
				pending.clear();
				throw e;
			}
		}
	}

	private static final class OutputFile {
		private final Path path;
		private final String contentHash;
		private final long size;

		private OutputFile(Path path, String contentHash, long size) {
			this.path = path;
			this.contentHash = contentHash;
			this.size = size;
		}
	}
}
