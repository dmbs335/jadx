package jadx.gui.utils.fileswatcher;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

class FilesWatcherTest {
	@TempDir
	Path tempDir;

	@Test
	void cancelBeforeWatchDoesNotBlock() throws Exception {
		FilesWatcher watcher = new FilesWatcher(List.of(tempDir), (path, kind) -> {
		});

		watcher.cancel();

		assertTimeoutPreemptively(Duration.ofSeconds(1), watcher::watch);
	}

	@Test
	void relativeFilePathReceivesUpdates() throws Exception {
		Path file = Files.writeString(tempDir.resolve("input.apk"), "before");
		Path relativeFile = Path.of("").toAbsolutePath().relativize(file.toAbsolutePath());
		CountDownLatch update = new CountDownLatch(1);
		FilesWatcher watcher = new FilesWatcher(List.of(relativeFile), (path, kind) -> update.countDown());
		ExecutorService executor = Executors.newSingleThreadExecutor();
		try {
			Future<?> future = executor.submit(watcher::watch);
			Files.writeString(file, "after");
			assertThat(update.await(2, TimeUnit.SECONDS)).isTrue();
			watcher.cancel();
			future.get(1, TimeUnit.SECONDS);
		} finally {
			watcher.cancel();
			executor.shutdownNow();
		}
	}
}
