package jadx.storage.impl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Stream;

import jadx.storage.api.ContentIngestSession;
import jadx.storage.api.IngestRequest;
import jadx.storage.api.IngestStats;

/**
 * Imports an immutable output tree while overlapping bounded parallel SHA-256 reads with the
 * serialized content-store transaction. No path or timestamp is used as a cache key: attributes
 * only detect files changed concurrently with hashing.
 */
public final class ParallelContentImporter {
	private static final int HASH_BUFFER_SIZE = 64 * 1024;

	private ParallelContentImporter() {
	}

	public static IngestStats ingest(SqliteContentStore store, IngestRequest request, int hashingThreads)
			throws IOException {
		if (hashingThreads <= 1) {
			return store.ingest(request);
		}
		Path outputRoot = request.getOutputDirectory().toAbsolutePath().normalize();
		if (!Files.isDirectory(outputRoot)) {
			throw new IOException("Output directory does not exist: " + outputRoot);
		}
		int maxInFlight = Math.max(2, Math.multiplyExact(hashingThreads, 2));
		ExecutorService executor = Executors.newFixedThreadPool(hashingThreads, runnable -> {
			Thread thread = new Thread(runnable, "jadx-cas-hash");
			thread.setDaemon(true);
			return thread;
		});
		CompletionService<HashedFile> hashes = new ExecutorCompletionService<>(executor);
		try (ContentIngestSession session = store.beginIngest(request);
				Stream<Path> files = Files.walk(outputRoot)) {
			Iterator<Path> iterator = files
					.filter(path -> Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS))
					.iterator();
			int inFlight = 0;
			while (iterator.hasNext()) {
				Path file = iterator.next();
				hashes.submit(() -> hash(file));
				inFlight++;
				if (inFlight == maxInFlight) {
					ingestCompleted(hashes, session);
					inFlight--;
				}
			}
			while (inFlight != 0) {
				ingestCompleted(hashes, session);
				inFlight--;
			}
			return session.complete();
		} finally {
			executor.shutdownNow();
		}
	}

	private static void ingestCompleted(
			CompletionService<HashedFile> hashes, ContentIngestSession session) throws IOException {
		try {
			Future<HashedFile> future = hashes.take();
			HashedFile file = future.get();
			session.ingest(file.path, file.hash, file.size);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IOException("Interrupted while importing content", e);
		} catch (ExecutionException e) {
			Throwable cause = e.getCause();
			if (cause instanceof IOException) {
				throw (IOException) cause;
			}
			throw new IOException("Failed to hash output file", cause);
		}
	}

	private static HashedFile hash(Path file) throws IOException {
		BasicFileAttributes before = Files.readAttributes(file, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
		MessageDigest digest = sha256();
		long readBytes = 0;
		byte[] buffer = new byte[HASH_BUFFER_SIZE];
		try (InputStream input = Files.newInputStream(file)) {
			int read;
			while ((read = input.read(buffer)) != -1) {
				digest.update(buffer, 0, read);
				readBytes += read;
			}
		}
		BasicFileAttributes after = Files.readAttributes(file, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
		if (readBytes != before.size()
				|| before.size() != after.size()
				|| !before.lastModifiedTime().equals(after.lastModifiedTime())
				|| !sameFileKey(before.fileKey(), after.fileKey())) {
			throw new IOException("Output changed while hashing: " + file);
		}
		return new HashedFile(file, toHex(digest.digest()), readBytes);
	}

	private static boolean sameFileKey(Object first, Object second) {
		return first == null || second == null || first.equals(second);
	}

	private static MessageDigest sha256() {
		try {
			return MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("SHA-256 is unavailable", e);
		}
	}

	private static String toHex(byte[] bytes) {
		StringBuilder result = new StringBuilder(bytes.length * 2);
		for (byte value : bytes) {
			result.append(Character.forDigit((value >>> 4) & 0xF, 16));
			result.append(Character.forDigit(value & 0xF, 16));
		}
		return result.toString();
	}

	private static final class HashedFile {
		private final Path path;
		private final String hash;
		private final long size;

		private HashedFile(Path path, String hash, long size) {
			this.path = path;
			this.hash = hash;
			this.size = size;
		}
	}
}
