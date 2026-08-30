package jadx.gui.search;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jadx.api.utils.tasks.ITaskExecutor;
import jadx.core.utils.tasks.TaskExecutor;
import jadx.gui.jobs.CancelableBackgroundTask;
import jadx.gui.jobs.IBackgroundTask;
import jadx.gui.jobs.ITaskInfo;
import jadx.gui.jobs.ITaskProgress;
import jadx.gui.jobs.TaskProgress;
import jadx.gui.jobs.TaskStatus;
import jadx.gui.treemodel.JNode;
import jadx.gui.ui.MainWindow;
import jadx.gui.utils.NLS;

public class SearchTask extends CancelableBackgroundTask {
	private static final Logger LOG = LoggerFactory.getLogger(SearchTask.class);

	private final Function<IBackgroundTask, Future<TaskStatus>> taskSubmitter;
	private final Consumer<JNode> resultsListener;
	private final BiConsumer<SearchTask, Boolean> onFinish;
	private final BooleanSupplier resultOwnerValid;
	private final List<SearchJob> jobs = new ArrayList<>();
	private final TaskProgress taskProgress = new TaskProgress();

	private final AtomicInteger resultsCount = new AtomicInteger(0);
	private final AtomicReference<String> failure = new AtomicReference<>();
	private int resultsLimit;
	private Future<TaskStatus> future;
	private volatile @Nullable TaskExecutor taskExecutor;

	private Consumer<ITaskProgress> progressListener;

	public SearchTask(MainWindow mainWindow, Consumer<JNode> results, BiConsumer<SearchTask, Boolean> onFinish) {
		this(mainWindow.getBackgroundExecutor()::executeWithFuture, results, onFinish, () -> true);
	}

	public SearchTask(MainWindow mainWindow, Consumer<JNode> results,
			BiConsumer<SearchTask, Boolean> onFinish, BooleanSupplier resultOwnerValid) {
		this(mainWindow.getBackgroundExecutor()::executeWithFuture, results, onFinish, resultOwnerValid);
	}

	SearchTask(Function<IBackgroundTask, Future<TaskStatus>> taskSubmitter,
			Consumer<JNode> results, BiConsumer<SearchTask, Boolean> onFinish) {
		this(taskSubmitter, results, onFinish, () -> true);
	}

	SearchTask(Function<IBackgroundTask, Future<TaskStatus>> taskSubmitter,
			Consumer<JNode> results, BiConsumer<SearchTask, Boolean> onFinish,
			BooleanSupplier resultOwnerValid) {
		this.taskSubmitter = taskSubmitter;
		this.resultsListener = results;
		this.onFinish = onFinish;
		this.resultOwnerValid = resultOwnerValid;
	}

	public void addProviderJob(ISearchProvider provider) {
		jobs.add(new SearchJob(this, provider));
	}

	public void setResultsLimit(int limit) {
		this.resultsLimit = limit;
	}

	public synchronized void fetchResults() {
		if (future != null) {
			throw new IllegalStateException("Previous task not yet finished");
		}
		resetCancel();
		failure.set(null);
		resultsCount.set(0);
		taskProgress.updateTotal(jobs.stream().mapToInt(s -> s.getProvider().total()).sum());
		future = taskSubmitter.apply(this);
	}

	void reportRegexTimeout(RegexSearchTimeoutException error) {
		failure.compareAndSet(null, error.getMessage());
		cancel();
	}

	public @Nullable String getFailure() {
		return failure.get();
	}

	public synchronized boolean addResult(JNode resultNode) {
		if (isCanceled() || !resultOwnerValid.getAsBoolean()) {
			// Ignore results after cancel or after their owning project was closed.
			return true;
		}
		this.resultsListener.accept(resultNode);
		if (resultsLimit != 0 && resultsCount.incrementAndGet() >= resultsLimit) {
			cancel();
			return true;
		}
		return false;
	}

	@Override
	public synchronized void cancel() {
		// Serialize cancellation with result delivery. Once this method returns,
		// an old search can no longer append a result after the next search reset.
		if (isCanceled()) {
			return;
		}
		super.cancel();
		TaskExecutor executor = taskExecutor;
		if (executor != null) {
			executor.cancelPendingTasks();
		}
	}

	public boolean waitTask() {
		Future<TaskStatus> taskFuture;
		synchronized (this) {
			taskFuture = future;
		}
		if (taskFuture == null) {
			return true;
		}
		try {
			taskFuture.get(200, TimeUnit.MILLISECONDS);
			clearFuture(taskFuture);
			return true;
		} catch (TimeoutException e) {
			LOG.debug("Canceled search task is still finishing");
			return false;
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			LOG.debug("Interrupted while waiting for search task completion");
			return false;
		} catch (CancellationException e) {
			clearFuture(taskFuture);
			return true;
		} catch (ExecutionException e) {
			LOG.warn("Search task failed while waiting for completion", e.getCause());
			clearFuture(taskFuture);
			return true;
		}
	}

	private synchronized void clearFuture(Future<TaskStatus> taskFuture) {
		if (future == taskFuture) {
			future = null;
		}
	}

	@Override
	public String getTitle() {
		return NLS.str("search_dialog.tip_searching");
	}

	@Override
	public ITaskExecutor scheduleTasks() {
		TaskExecutor executor = new TaskExecutor();
		executor.addParallelTasks(jobs);
		taskExecutor = executor;
		return executor;
	}

	@Override
	public void onFinish(ITaskInfo task) {
		boolean complete = !isCanceled()
				&& task.getStatus() == TaskStatus.COMPLETE
				&& task.getJobsComplete() == task.getJobsCount();
		this.onFinish.accept(this, complete);
	}

	@Override
	public boolean checkMemoryUsage() {
		return true;
	}

	@Override
	public @NotNull ITaskProgress getTaskProgress() {
		taskProgress.updateProgress(jobs.stream().mapToInt(s -> s.getProvider().progress()).sum());
		return taskProgress;
	}

	public void setProgressListener(Consumer<ITaskProgress> progressListener) {
		this.progressListener = progressListener;
	}

	@Override
	public @Nullable Consumer<ITaskProgress> getProgressListener() {
		return this.progressListener;
	}

	@Override
	public int getCancelTimeoutMS() {
		return 0;
	}

	@Override
	public int getShutdownTimeoutMS() {
		return 10;
	}
}
