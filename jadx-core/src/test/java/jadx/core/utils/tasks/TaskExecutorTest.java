package jadx.core.utils.tasks;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TaskExecutorTest {

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
