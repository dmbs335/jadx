package jadx.core.utils.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TaskExecutorTest {
	@Test
	void cancelPendingClearsQueueWithoutInterruptingActiveTasks() throws Exception {
		TaskExecutor executor = new TaskExecutor();
		executor.setThreadsCount(2);
		CountDownLatch twoTasksStarted = new CountDownLatch(2);
		CountDownLatch releaseTasks = new CountDownLatch(1);
		AtomicInteger executed = new AtomicInteger();
		AtomicInteger interrupted = new AtomicInteger();
		List<Runnable> tasks = new ArrayList<>();
		for (int i = 0; i < 50; i++) {
			tasks.add(() -> {
				executed.incrementAndGet();
				twoTasksStarted.countDown();
				try {
					releaseTasks.await(5, TimeUnit.SECONDS);
				} catch (InterruptedException e) {
					interrupted.incrementAndGet();
					Thread.currentThread().interrupt();
				}
			});
		}
		executor.addParallelTasks(tasks);
		try {
			executor.execute();
			assertThat(twoTasksStarted.await(1, TimeUnit.SECONDS)).isTrue();
			executor.cancelPendingTasks();
			releaseTasks.countDown();
			executor.awaitTermination();

			assertThat(executed).hasValue(2);
			assertThat(interrupted).hasValue(0);
			assertThat(executor.isRunning()).isFalse();
		} finally {
			releaseTasks.countDown();
			executor.terminate();
		}
	}

	@Test
	void interruptedWaitDoesNotAbandonRunningTask() throws Exception {
		ExecutorService executor = Executors.newSingleThreadExecutor();
		CountDownLatch taskStarted = new CountDownLatch(1);
		CountDownLatch releaseTask = new CountDownLatch(1);
		CountDownLatch waitReturned = new CountDownLatch(1);
		AtomicBoolean interruptRestored = new AtomicBoolean();
		try {
			executor.execute(() -> {
				taskStarted.countDown();
				try {
					releaseTask.await();
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			});
			executor.shutdown();
			assertThat(taskStarted.await(1, TimeUnit.SECONDS)).isTrue();

			Thread waiter = new Thread(() -> {
				TaskExecutor.awaitExecutorTermination(executor);
				interruptRestored.set(Thread.currentThread().isInterrupted());
				waitReturned.countDown();
			});
			waiter.start();
			waiter.interrupt();

			assertThat(waitReturned.await(100, TimeUnit.MILLISECONDS)).isFalse();
			releaseTask.countDown();
			assertThat(waitReturned.await(10, TimeUnit.SECONDS)).isTrue();
			assertThat(interruptRestored).isTrue();
		} finally {
			releaseTask.countDown();
			executor.shutdownNow();
		}
	}
}
