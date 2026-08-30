package jadx.gui.jobs;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.SwingUtilities;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import jadx.gui.settings.JadxSettings;
import jadx.gui.ui.panel.ProgressPanel;

import static org.assertj.core.api.Assertions.assertThat;

class BackgroundExecutorTest {
	@Test
	void cancelFromEdtDoesNotWaitForTaskCompletion() throws Exception {
		BackgroundExecutor executor = newExecutor();
		CountDownLatch jobStarted = new CountDownLatch(1);
		CountDownLatch releaseJob = new CountDownLatch(1);
		CountDownLatch callbackReached = new CountDownLatch(1);
		executor.execute(new SimpleTask("edt cancel", () -> {
			jobStarted.countDown();
			boolean interrupted = false;
			while (releaseJob.getCount() != 0) {
				try {
					releaseJob.await(100, TimeUnit.MILLISECONDS);
				} catch (InterruptedException e) {
					interrupted = true;
				}
			}
			if (interrupted) {
				Thread.currentThread().interrupt();
			}
		}, callbackReached::countDown));
		assertThat(jobStarted.await(1, TimeUnit.SECONDS)).isTrue();

		long start = System.nanoTime();
		SwingUtilities.invokeAndWait(executor::cancelAll);
		long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
		assertThat(elapsedMs).isLessThan(500);

		AtomicBoolean replacementRan = new AtomicBoolean();
		Future<TaskStatus> replacement = executor.executeWithFuture(
				new SimpleTask("must stay blocked", () -> replacementRan.set(true)));
		assertThat(replacement.get(1, TimeUnit.SECONDS)).isEqualTo(TaskStatus.CANCEL_BY_USER);
		assertThat(replacementRan).isFalse();

		releaseJob.countDown();
		assertThat(callbackReached.await(1, TimeUnit.SECONDS)).isTrue();
		executor.shutdown();
	}

	@Test
	void completionCallbackCanSubmitWhileAnotherThreadWaits() throws Exception {
		JadxSettings settings = new JadxSettings(null) {
			@Override
			public int getThreadsCount() {
				return 1;
			}
		};
		BackgroundExecutor executor = new BackgroundExecutor(settings, new ProgressPanel(null, false));
		CountDownLatch jobStarted = new CountDownLatch(1);
		CountDownLatch releaseJob = new CountDownLatch(1);
		CountDownLatch nestedRun = new CountDownLatch(1);
		executor.execute(new SimpleTask("first", () -> {
			jobStarted.countDown();
			try {
				releaseJob.await(2, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}, () -> executor.execute(new SimpleTask("nested", nestedRun::countDown))));
		assertThat(jobStarted.await(1, TimeUnit.SECONDS)).isTrue();

		Thread waiter = new Thread(executor::waitForComplete);
		waiter.start();
		awaitWaiting(waiter);
		releaseJob.countDown();

		waiter.join(1_000);
		assertThat(waiter.isAlive()).isFalse();
		assertThat(nestedRun.await(1, TimeUnit.SECONDS)).isTrue();
		executor.shutdown();
	}

	@Test
	void cancelCallbackSubmissionDoesNotDeadlock() throws Exception {
		BackgroundExecutor executor = newExecutor();
		CountDownLatch jobStarted = new CountDownLatch(1);
		CountDownLatch releaseJob = new CountDownLatch(1);
		CountDownLatch callbackReached = new CountDownLatch(1);
		executor.execute(new SimpleTask("running", () -> {
			jobStarted.countDown();
			try {
				releaseJob.await(2, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}, () -> {
			callbackReached.countDown();
			executor.execute(new SimpleTask("nested", () -> {
			}));
		}));
		assertThat(jobStarted.await(1, TimeUnit.SECONDS)).isTrue();

		Thread cancelThread = new Thread(executor::cancelAll);
		cancelThread.start();
		Thread.sleep(50);
		releaseJob.countDown();

		assertThat(callbackReached.await(1, TimeUnit.SECONDS)).isTrue();
		cancelThread.join(2_000);
		assertThat(cancelThread.isAlive()).isFalse();
		executor.shutdown();
	}

	@RepeatedTest(20)
	void concurrentShutdownDoesNotCompleteFutureBeforeEdtCallback() throws Exception {
		BackgroundExecutor executor = newExecutor();
		CountDownLatch jobStarted = new CountDownLatch(1);
		CountDownLatch callbackEntered = new CountDownLatch(1);
		CountDownLatch releaseCallback = new CountDownLatch(1);
		SimpleTask task = new SimpleTask("cancel/reload race", () -> {
			jobStarted.countDown();
			try {
				Thread.sleep(5_000);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}, () -> {
			callbackEntered.countDown();
			try {
				releaseCallback.await(2, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}) {
			@Override
			public int getCancelTimeoutMS() {
				return 0;
			}

			@Override
			public int getShutdownTimeoutMS() {
				return 1_000;
			}
		};
		Future<TaskStatus> future = executor.executeWithFuture(task);
		assertThat(jobStarted.await(1, TimeUnit.SECONDS)).isTrue();

		Thread cancelThread = new Thread(executor::cancelAll);
		cancelThread.start();
		assertThat(callbackEntered.await(1, TimeUnit.SECONDS)).isTrue();

		// Simulate window disposal while project reload cancellation still owns the stop.
		executor.shutdown();
		Thread.sleep(100);
		assertThat(future.isDone()).isFalse();

		releaseCallback.countDown();
		assertThat(future.get(1, TimeUnit.SECONDS)).isIn(
				TaskStatus.CANCEL_BY_USER, TaskStatus.COMPLETE);
		cancelThread.join(1_000);
		assertThat(cancelThread.isAlive()).isFalse();
		assertThat(executor.isShutdown()).isTrue();
	}

	@Test
	void shutdownStopsProgressThreadAndRejectsNewTasks() throws Exception {
		BackgroundExecutor executor = newExecutor();
		executor.shutdown();

		AtomicBoolean ran = new AtomicBoolean();
		Future<TaskStatus> result = executor.executeWithFuture(new SimpleTask("rejected", () -> ran.set(true)));

		assertThat(result.get(1, TimeUnit.SECONDS)).isEqualTo(TaskStatus.CANCEL_BY_USER);
		assertThat(ran).isFalse();
		assertThat(executor.isShutdown()).isTrue();
	}

	@Test
	void scheduleFailureStillRunsFinishCallback() throws Exception {
		BackgroundExecutor executor = newExecutor();
		CountDownLatch callbackReached = new CountDownLatch(1);
		AtomicBoolean canceled = new AtomicBoolean();
		IBackgroundTask task = new IBackgroundTask() {
			@Override
			public String getTitle() {
				return "broken schedule";
			}

			@Override
			public jadx.api.utils.tasks.ITaskExecutor scheduleTasks() {
				throw new IllegalStateException("expected");
			}

			@Override
			public void onFinish(ITaskInfo taskInfo) {
				callbackReached.countDown();
			}

			@Override
			public boolean isCanceled() {
				return canceled.get();
			}

			@Override
			public void cancel() {
				canceled.set(true);
			}
		};

		Future<TaskStatus> result = executor.executeWithFuture(task);

		assertThat(result.get(1, TimeUnit.SECONDS)).isEqualTo(TaskStatus.ERROR);
		assertThat(callbackReached.await(1, TimeUnit.SECONDS)).isTrue();
		executor.shutdown();
	}

	private static BackgroundExecutor newExecutor() {
		JadxSettings settings = new JadxSettings(null) {
			@Override
			public int getThreadsCount() {
				return 1;
			}
		};
		return new BackgroundExecutor(settings, new ProgressPanel(null, false));
	}

	private static void awaitWaiting(Thread thread) throws InterruptedException {
		long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(1);
		while (thread.getState() != Thread.State.WAITING && System.nanoTime() < deadline) {
			Thread.sleep(5);
		}
		assertThat(thread.getState()).isEqualTo(Thread.State.WAITING);
	}
}
