package jadx.gui.search;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.Test;

import jadx.core.utils.tasks.TaskExecutor;
import jadx.gui.jobs.TaskStatus;

import static org.assertj.core.api.Assertions.assertThat;

class SearchTaskTest {

	@Test
	void unfinishedTaskIsAStateNotAnError() {
		CompletableFuture<TaskStatus> future = new CompletableFuture<>();
		SearchTask task = newTask(future);

		task.fetchResults();
		long start = System.nanoTime();
		assertThat(task.waitTask()).isFalse();
		long waitMs = (System.nanoTime() - start) / 1_000_000L;
		assertThat(waitMs).isBetween(150L, 1_000L);

		future.complete(TaskStatus.COMPLETE);
		assertThat(task.waitTask()).isTrue();
	}

	@Test
	void canceledButStillRunningTaskIsNotSafeForClassUnload() {
		CompletableFuture<TaskStatus> future = new CompletableFuture<>();
		SearchTask task = newTask(future);

		task.fetchResults();
		task.cancel();

		assertThat(task.waitTask()).isFalse();
		future.complete(TaskStatus.CANCEL_BY_USER);
		assertThat(task.waitTask()).isTrue();
	}

	@Test
	void canceledTaskCanBeReused() {
		CompletableFuture<TaskStatus> future = new CompletableFuture<>();
		SearchTask task = newTask(future);

		task.fetchResults();
		future.cancel(true);
		assertThat(task.waitTask()).isTrue();

		// waitTask clears the completed Future, so load-more can submit again.
		task.fetchResults();
		assertThat(task.waitTask()).isTrue();
	}

	@Test
	void interruptedWaitPreservesInterruptStatus() {
		CompletableFuture<TaskStatus> future = new CompletableFuture<>();
		SearchTask task = newTask(future);
		task.fetchResults();
		try {
			Thread.currentThread().interrupt();
			assertThat(task.waitTask()).isFalse();
			assertThat(Thread.currentThread().isInterrupted()).isTrue();
		} finally {
			Thread.interrupted();
		}
		future.complete(TaskStatus.COMPLETE);
		assertThat(task.waitTask()).isTrue();
	}

	@Test
	void waitingDoesNotBlockResultDelivery() throws Exception {
		CountDownLatch waitEntered = new CountDownLatch(1);
		CountDownLatch releaseWait = new CountDownLatch(1);
		CompletableFuture<TaskStatus> future = new CompletableFuture<>() {
			@Override
			public TaskStatus get(long timeout, TimeUnit unit)
					throws InterruptedException, ExecutionException, TimeoutException {
				waitEntered.countDown();
				if (!releaseWait.await(5, TimeUnit.SECONDS)) {
					throw new TimeoutException("Test wait was not released");
				}
				return TaskStatus.COMPLETE;
			}
		};
		SearchTask task = newTask(future);
		task.fetchResults();

		Thread waiter = new Thread(task::waitTask);
		FutureTask<Boolean> result = new FutureTask<>(() -> task.addResult(null));
		Thread resultThread = new Thread(result);
		waiter.start();
		assertThat(waitEntered.await(1, TimeUnit.SECONDS)).isTrue();
		try {
			resultThread.start();
			assertThat(result.get(1, TimeUnit.SECONDS)).isFalse();
		} finally {
			releaseWait.countDown();
			waiter.join(1_000);
			resultThread.join(1_000);
		}
		assertThat(waiter.isAlive()).isFalse();
		assertThat(resultThread.isAlive()).isFalse();
	}

	@Test
	void cancellationIsSerializedWithResultDelivery() throws Exception {
		CountDownLatch resultEntered = new CountDownLatch(1);
		CountDownLatch releaseResult = new CountDownLatch(1);
		AtomicInteger delivered = new AtomicInteger();
		SearchTask task = new SearchTask(ignored -> new CompletableFuture<>(), node -> {
			resultEntered.countDown();
			try {
				releaseResult.await(5, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			delivered.incrementAndGet();
		}, (searchTask, complete) -> {
		});

		Thread resultThread = new Thread(() -> task.addResult(null));
		Thread cancelThread = new Thread(task::cancel);
		resultThread.start();
		assertThat(resultEntered.await(1, TimeUnit.SECONDS)).isTrue();
		cancelThread.start();
		try {
			Thread.sleep(50);
			assertThat(cancelThread.isAlive()).isTrue();
		} finally {
			releaseResult.countDown();
			resultThread.join(1_000);
			cancelThread.join(1_000);
		}

		assertThat(resultThread.isAlive()).isFalse();
		assertThat(cancelThread.isAlive()).isFalse();
		assertThat(delivered).hasValue(1);
		assertThat(task.addResult(null)).isTrue();
		assertThat(delivered).hasValue(1);
	}

	@Test
	void cancellationStopsPendingSearchJobs() {
		SearchTask task = newTask(new CompletableFuture<>());
		TaskExecutor executor = (TaskExecutor) task.scheduleTasks();

		assertThat(executor.isTerminating()).isFalse();
		task.cancel();
		assertThat(executor.isTerminating()).isTrue();
	}

	@Test
	void resultFromPreviousProjectGenerationIsRejected() {
		AtomicLong currentGeneration = new AtomicLong(7);
		AtomicInteger delivered = new AtomicInteger();
		long ownerGeneration = currentGeneration.get();
		SearchTask task = new SearchTask(ignored -> new CompletableFuture<>(),
				node -> delivered.incrementAndGet(), (searchTask, complete) -> {
				}, () -> currentGeneration.get() == ownerGeneration);

		assertThat(task.addResult(null)).isFalse();
		assertThat(delivered).hasValue(1);

		currentGeneration.incrementAndGet();
		assertThat(task.addResult(null)).isTrue();
		assertThat(delivered).hasValue(1);
	}

	@Test
	void regexTimeoutIsReportedAndCancelsSiblingJobs() {
		SearchTask task = newTask(new CompletableFuture<>());
		TaskExecutor executor = (TaskExecutor) task.scheduleTasks();

		task.reportRegexTimeout(new RegexSearchTimeoutException());

		assertThat(task.getFailure()).contains("Regex search stopped");
		assertThat(task.isCanceled()).isTrue();
		assertThat(executor.isTerminating()).isTrue();
	}

	private static SearchTask newTask(CompletableFuture<TaskStatus> future) {
		return new SearchTask(task -> future, node -> {
		}, (task, complete) -> {
		});
	}
}
