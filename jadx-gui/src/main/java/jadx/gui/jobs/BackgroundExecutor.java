package jadx.gui.jobs;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jadx.api.utils.tasks.ITaskExecutor;
import jadx.core.utils.Utils;
import jadx.core.utils.exceptions.JadxRuntimeException;
import jadx.gui.settings.JadxSettings;
import jadx.gui.ui.MainWindow;
import jadx.gui.ui.panel.ProgressPanel;
import jadx.gui.utils.NLS;
import jadx.gui.utils.UiUtils;

/**
 * Run tasks in the background with progress bar indication.
 * Use instance created in {@link MainWindow}.
 */
public class BackgroundExecutor {
	private static final Logger LOG = LoggerFactory.getLogger(BackgroundExecutor.class);

	private final JadxSettings settings;
	private final ProgressUpdater progressUpdater;

	private ThreadPoolExecutor taskQueueExecutor;
	private final Map<Long, InternalTask> taskRunning = new ConcurrentHashMap<>();
	private final AtomicLong idSupplier = new AtomicLong(0);
	private volatile boolean canceling;
	private volatile boolean shutdown;

	public BackgroundExecutor(JadxSettings settings, ProgressPanel progressPane) {
		this.settings = Objects.requireNonNull(settings);
		this.progressUpdater = new ProgressUpdater(progressPane, this::taskCanceled);
		reset();
	}

	public synchronized void execute(IBackgroundTask task) {
		if (canceling || shutdown) {
			task.cancel();
			return;
		}
		InternalTask internalTask = buildTask(task);
		taskQueueExecutor.execute(() -> runTask(internalTask));
	}

	public synchronized Future<TaskStatus> executeWithFuture(IBackgroundTask task) {
		if (canceling || shutdown) {
			task.cancel();
			return CompletableFuture.completedFuture(TaskStatus.CANCEL_BY_USER);
		}
		InternalTask internalTask = buildTask(task);
		return taskQueueExecutor.submit(() -> {
			runTask(internalTask);
			return internalTask.getStatus();
		});
	}

	public void cancelAll() {
		stopExecutor(false);
	}

	public void shutdown() {
		stopExecutor(true);
	}

	private void stopExecutor(boolean finalShutdown) {
		ThreadPoolExecutor executor;
		List<InternalTask> tasksToCancel;
		synchronized (this) {
			if (finalShutdown) {
				shutdown = true;
			}
			if (canceling) {
				// The active stop owner can be inside a task's EDT completion callback.
				// Interrupting its queue runner here lets the Future complete before the
				// callback and produces an "UI thread interrupted" warning. The owner sees
				// the shutdown flag in its finally block and performs the final cleanup.
				return;
			}
			if (shutdown && !finalShutdown) {
				return;
			}
			canceling = true;
			executor = taskQueueExecutor;
			tasksToCancel = List.copyOf(taskRunning.values());
		}
		try {
			tasksToCancel.forEach(this::cancelTask);
			// Child task executors are already terminated above. Let the queue runner
			// finish its EDT completion callback without interrupting invokeAndWait.
			executor.shutdown();
			boolean complete = executor.awaitTermination(3, TimeUnit.SECONDS);
			if (!complete) {
				executor.shutdownNow();
				complete = executor.awaitTermination(3, TimeUnit.SECONDS);
			}
			if (complete) {
				LOG.debug("Background task executor canceled successfully");
			} else {
				String taskNames = taskRunning.values().stream()
						.map(t -> t.getBgTask().getTitle())
						.collect(Collectors.joining(", "));
				LOG.debug("Background task executor cancel failed. Running tasks: {}", taskNames);
			}
		} catch (Exception e) {
			LOG.error("Error terminating task executor", e);
		} finally {
			synchronized (this) {
				taskRunning.clear();
				if (!shutdown) {
					taskQueueExecutor = newTaskExecutor();
				}
				canceling = false;
			}
			if (shutdown) {
				progressUpdater.shutdown();
			}
		}
	}

	boolean isShutdown() {
		return shutdown && progressUpdater.isShutdown();
	}

	public void waitForComplete() {
		Future<?> waitMarker;
		synchronized (this) {
			// Serialize only queue access. Holding this monitor while waiting can
			// deadlock if a task's EDT completion callback submits another task.
			waitMarker = taskQueueExecutor.submit(UiUtils.EMPTY_RUNNABLE);
		}
		try {
			waitMarker.get();
		} catch (Exception e) {
			throw new JadxRuntimeException("Failed to wait tasks completion", e);
		}
	}

	public void execute(String title, List<Runnable> backgroundJobs, Consumer<TaskStatus> onFinishUiRunnable) {
		execute(new SimpleTask(title, backgroundJobs, onFinishUiRunnable));
	}

	public void execute(String title, Runnable backgroundRunnable, Consumer<TaskStatus> onFinishUiRunnable) {
		execute(new SimpleTask(title, Collections.singletonList(backgroundRunnable), onFinishUiRunnable));
	}

	public void execute(String title, Runnable backgroundRunnable) {
		execute(new SimpleTask(title, Collections.singletonList(backgroundRunnable)));
	}

	public void startLoading(Runnable backgroundRunnable, Runnable onFinishUiRunnable) {
		execute(new SimpleTask(NLS.str("progress.load"), backgroundRunnable, onFinishUiRunnable));
	}

	public void startLoading(Runnable backgroundRunnable) {
		execute(new SimpleTask(NLS.str("progress.load"), backgroundRunnable));
	}

	private synchronized void reset() {
		taskQueueExecutor = newTaskExecutor();
		taskRunning.clear();
	}

	private static ThreadPoolExecutor newTaskExecutor() {
		return (ThreadPoolExecutor) Executors.newFixedThreadPool(1, Utils.simpleThreadFactory("bg"));
	}

	private InternalTask buildTask(IBackgroundTask task) {
		long id = idSupplier.incrementAndGet();
		InternalTask internalTask = new InternalTask(id, task);
		taskRunning.put(id, internalTask);
		return internalTask;
	}

	private void runTask(InternalTask internalTask) {
		try {
			IBackgroundTask task = internalTask.getBgTask();
			if (task.isCanceled()) {
				internalTask.setStatus(TaskStatus.CANCEL_BY_USER);
				return;
			}
			ITaskExecutor taskExecutor = task.scheduleTasks();
			taskExecutor.setThreadsCount(settings.getThreadsCount());
			int tasksCount = taskExecutor.getTasksCount();
			internalTask.setTaskExecutor(taskExecutor);
			internalTask.setJobsCount(tasksCount);
			if (UiUtils.JADX_GUI_DEBUG) {
				LOG.debug("Starting background task '{}', jobs count: {}, time limit: {} ms, memory check: {}",
						task.getTitle(), tasksCount, task.timeLimit(), task.checkMemoryUsage());
			}
			long startTime = System.currentTimeMillis();
			Supplier<TaskStatus> cancelCheck = buildCancelCheck(internalTask, startTime);
			internalTask.taskStart(startTime, cancelCheck);
			if (task.isCanceled()) {
				internalTask.setStatus(TaskStatus.CANCEL_BY_USER);
				return;
			}
			progressUpdater.addTask(internalTask);
			taskExecutor.execute();
			taskExecutor.awaitTermination();
		} catch (Exception e) {
			LOG.error("Task failed", e);
			internalTask.setStatus(TaskStatus.ERROR);
		} finally {
			taskComplete(internalTask);
		}
	}

	private void taskComplete(InternalTask internalTask) {
		// TaskExecutor waits uninterruptibly to prevent workers escaping the project
		// lifecycle, then restores the interrupt flag. Clear it while dispatching the
		// completion callback: Swing invokeAndWait rejects an interrupted caller.
		boolean restoreInterrupt = Thread.interrupted();
		try {
			IBackgroundTask task = internalTask.getBgTask();
			ITaskExecutor taskExecutor = internalTask.getTaskExecutor();
			if (taskExecutor != null) {
				internalTask.setJobsComplete(taskExecutor.getProgress());
			}
			if (internalTask.getStatus() == TaskStatus.STARTED) {
				internalTask.setStatus(TaskStatus.COMPLETE);
			}
			internalTask.updateExecTime();
			task.onDone(internalTask);
			// treat UI task operations as part of the task to not mix with others
			UiUtils.uiRunAndWait(() -> {
				try {
					task.onFinish(internalTask);
				} catch (Exception e) {
					LOG.error("Task onFinish failed", e);
					internalTask.setStatus(TaskStatus.ERROR);
				}
			});
		} catch (Exception e) {
			LOG.error("Task complete failed", e);
			internalTask.setStatus(TaskStatus.ERROR);
		} finally {
			internalTask.taskComplete();
			progressUpdater.taskComplete(internalTask);
			removeTask(internalTask);
			if (restoreInterrupt) {
				Thread.currentThread().interrupt();
			}
		}
	}

	private void removeTask(InternalTask internalTask) {
		taskRunning.remove(internalTask.getId());
	}

	private void cancelTask(InternalTask internalTask) {
		try {
			IBackgroundTask task = internalTask.getBgTask();
			if (!internalTask.isRunning()) {
				// task complete or not yet started
				if (!task.isCanceled()) {
					task.cancel();
				}
				removeTask(internalTask);
				return;
			}
			ITaskExecutor taskExecutor = internalTask.getTaskExecutor();
			// force termination
			if (!task.isCanceled()) {
				task.cancel();
			}
			taskExecutor.terminate();

			ExecutorService executor = taskExecutor.getInternalExecutor();
			if (executor == null) {
				return;
			}
			int cancelTimeout = task.getCancelTimeoutMS();
			if (cancelTimeout != 0) {
				if (executor.awaitTermination(cancelTimeout, TimeUnit.MILLISECONDS)) {
					LOG.debug("Task cancel complete");
					return;
				}
			}
			LOG.debug("Forcing tasks cancel");
			executor.shutdownNow();
			boolean complete = executor.awaitTermination(task.getShutdownTimeoutMS(), TimeUnit.MILLISECONDS);
			LOG.debug("Forced task cancel status: {}", complete
					? "success"
					: "fail, still active: " + (taskExecutor.getTasksCount() - taskExecutor.getProgress()));
		} catch (Exception e) {
			LOG.error("Failed to cancel task: {}", internalTask, e);
		}
	}

	/**
	 * Task cancel notification from progress updater
	 */
	private void taskCanceled(InternalTask task) {
		cancelTask(task);
	}

	private Supplier<TaskStatus> buildCancelCheck(InternalTask internalTask, long startTime) {
		IBackgroundTask task = internalTask.getBgTask();
		int timeLimit = task.timeLimit();
		long waitUntilTime = timeLimit == 0 ? 0 : startTime + timeLimit;
		boolean checkMemoryUsage = task.checkMemoryUsage();
		return () -> {
			if (task.isCanceled() || Thread.currentThread().isInterrupted()) {
				return TaskStatus.CANCEL_BY_USER;
			}
			if (waitUntilTime != 0 && waitUntilTime < System.currentTimeMillis()) {
				LOG.warn("Task '{}' execution timeout, force cancel", task.getTitle());
				return TaskStatus.CANCEL_BY_TIMEOUT;
			}
			if (checkMemoryUsage && !UiUtils.isFreeMemoryAvailable()) {
				LOG.warn("High memory usage: {}", UiUtils.memoryInfo());
				if (internalTask.getTaskExecutor().getThreadsCount() == 1) {
					LOG.warn("Task '{}' memory limit reached, force cancel", task.getTitle());
					return TaskStatus.CANCEL_BY_MEMORY;
				}
				LOG.warn("Low free memory, reduce processing threads count to 1");
				// reduce threads count and continue
				internalTask.getTaskExecutor().setThreadsCount(1);
				System.gc();
				UiUtils.sleep(1000); // wait GC
				if (!UiUtils.isFreeMemoryAvailable()) {
					LOG.error("Task '{}' memory limit reached (after GC), force cancel", task.getTitle());
					return TaskStatus.CANCEL_BY_MEMORY;
				}
			}
			return null;
		};
	}
}
