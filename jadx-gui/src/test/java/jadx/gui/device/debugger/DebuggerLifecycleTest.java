package jadx.gui.device.debugger;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DebuggerLifecycleTest {
	@Test
	void suspendEventSnapshotDoesNotChangeWithNextEvent() {
		SuspendInfo info = new SuspendInfo();
		info.update().updateThread(1).updateClass(2).updateMethod(3).updateOffset(4);
		SuspendInfo snapshot = info.snapshot();

		info.nextRound();
		info.update().updateThread(10).updateClass(20).updateMethod(30).updateOffset(40);

		assertThat(snapshot.getThreadID()).isEqualTo(1);
		assertThat(snapshot.getClassID()).isEqualTo(2);
		assertThat(snapshot.getMethodID()).isEqualTo(3);
		assertThat(snapshot.getOffset()).isEqualTo(4);
	}

	@Test
	void commandWaitHasABoundedTimeout() {
		ArrayBlockingQueue<Object> replies = new ArrayBlockingQueue<>(1);

		long start = System.nanoTime();
		assertThatThrownBy(() -> SmaliDebugger.waitForCommandResult(replies, 20))
				.isInstanceOf(SmaliDebuggerException.class)
				.hasMessageContaining("timed out");

		long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
		assertThat(elapsedMs).isLessThan(1_000);
	}

	@Test
	void disposeStopsQueuesAndDropsQueuedUpdates() throws Exception {
		DebugController controller = new DebugController();
		CountDownLatch firstStarted = new CountDownLatch(1);
		CountDownLatch releaseFirst = new CountDownLatch(1);
		CountDownLatch staleUpdate = new CountDownLatch(1);
		controller.executeLazy(() -> {
			firstStarted.countDown();
			try {
				releaseFirst.await(1, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		});
		controller.executeLazy(staleUpdate::countDown);
		assertThat(firstStarted.await(1, TimeUnit.SECONDS)).isTrue();

		controller.dispose();
		releaseFirst.countDown();
		controller.dispose();

		assertThat(controller.isDisposed()).isTrue();
		assertThat(staleUpdate.await(100, TimeUnit.MILLISECONDS)).isFalse();
	}
}
