package jadx.gui.utils.fileswatcher;

import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.processors.FlowableProcessor;
import io.reactivex.rxjava3.processors.PublishProcessor;

import jadx.core.utils.Utils;
import jadx.gui.ui.MainWindow;
import jadx.gui.utils.UiUtils;

public class LiveReloadWorker {
	private static final Logger LOG = LoggerFactory.getLogger(LiveReloadWorker.class);

	private final MainWindow mainWindow;
	private final FlowableProcessor<Path> processor;
	private final Disposable subscription;
	private final AtomicBoolean disposed = new AtomicBoolean();
	private volatile boolean started = false;
	private ExecutorService executor;
	private FilesWatcher watcher;

	@SuppressWarnings("ResultOfMethodCallIgnored")
	public LiveReloadWorker(MainWindow mainWindow) {
		this.mainWindow = mainWindow;
		this.processor = PublishProcessor.<Path>create().toSerialized();
		this.subscription = this.processor
				.debounce(1, TimeUnit.SECONDS)
				.filter(path -> started)
				.subscribe(path -> {
					LOG.debug("Reload triggered");
					UiUtils.uiRun(mainWindow::reopen);
				}, error -> LOG.error("Live reload event stream failed", error));
	}

	public boolean isStarted() {
		return started;
	}

	public synchronized void updateState(boolean enabled) {
		if (disposed.get()) {
			return;
		}
		if (this.started == enabled) {
			return;
		}
		if (enabled) {
			LOG.debug("Starting live reload worker");
			start();
		} else {
			LOG.debug("Stopping live reload worker");
			stop();
		}
	}

	private void onUpdate(Path path, WatchEvent.Kind<Path> pathKind) {
		if (disposed.get()) {
			return;
		}
		LOG.debug("Path updated: {}", path);
		processor.onNext(path);
	}

	private synchronized void start() {
		try {
			watcher = new FilesWatcher(mainWindow.getProject().getFilePaths(), this::onUpdate);
			executor = Executors.newSingleThreadExecutor(Utils.simpleThreadFactory("live-reload"));
			started = true;
			executor.submit(watcher::watch);
		} catch (Exception e) {
			LOG.warn("Failed to start live reload worker", e);
			resetState();
		}
	}

	private synchronized void stop() {
		// Gate already debounced events before waiting for the watcher thread.
		started = false;
		try {
			watcher.cancel();
			executor.shutdownNow();
			boolean canceled = executor.awaitTermination(5, TimeUnit.SECONDS);
			if (!canceled) {
				LOG.warn("Failed to cancel live reload worker");
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			LOG.debug("Interrupted while stopping live reload worker");
		} catch (Exception e) {
			LOG.warn("Failed to stop live reload worker", e);
		} finally {
			resetState();
		}
	}

	public void dispose() {
		if (!disposed.compareAndSet(false, true)) {
			return;
		}
		if (started) {
			stop();
		}
		subscription.dispose();
		processor.onComplete();
	}

	private void resetState() {
		started = false;
		executor = null;
		watcher = null;
	}
}
