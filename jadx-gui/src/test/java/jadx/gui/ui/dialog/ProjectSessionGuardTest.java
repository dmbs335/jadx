package jadx.gui.ui.dialog;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.RepeatedTest;

import static org.assertj.core.api.Assertions.assertThat;

class ProjectSessionGuardTest {

	@RepeatedTest(50)
	void dropsQueuedOldCallbacksAndAcceptsImmediateNewProjectWork() throws Exception {
		AtomicLong generation = new AtomicLong(10);
		ProjectSessionGuard oldProject = new ProjectSessionGuard(generation::get);
		Queue<Runnable> uiQueue = new ArrayDeque<>();
		AtomicInteger oldUpdates = new AtomicInteger();
		AtomicInteger newUpdates = new AtomicInteger();

		// Result/progress/finish callbacks were queued while the old project was current.
		for (int index = 0; index < 3; index++) {
			uiQueue.add(() -> oldProject.runIfActive(oldUpdates::incrementAndGet));
		}

		generation.incrementAndGet();
		assertThat(oldProject.close()).isTrue();
		assertThat(oldProject.close()).isFalse();
		ProjectSessionGuard newProject = new ProjectSessionGuard(generation::get);

		while (!uiQueue.isEmpty()) {
			uiQueue.remove().run();
		}
		assertThat(oldUpdates).hasValue(0);
		assertThat(newProject.runIfActive(newUpdates::incrementAndGet)).isTrue();
		assertThat(newUpdates).hasValue(1);

		// Late old-project workers released after reload also fail closed.
		CountDownLatch start = new CountDownLatch(1);
		Thread[] workers = new Thread[4];
		for (int index = 0; index < workers.length; index++) {
			workers[index] = new Thread(() -> {
				try {
					start.await(1, TimeUnit.SECONDS);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
				for (int attempt = 0; attempt < 100; attempt++) {
					oldProject.runIfActive(oldUpdates::incrementAndGet);
				}
			});
			workers[index].start();
		}
		start.countDown();
		for (Thread worker : workers) {
			worker.join(1_000);
			assertThat(worker.isAlive()).isFalse();
		}
		assertThat(oldUpdates).hasValue(0);
	}
}
