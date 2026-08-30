package jadx.gui.utils;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UiUtilsTest {
	@Test
	void globalBackgroundWorkerDoesNotKeepJvmAlive() throws Exception {
		CountDownLatch completed = new CountDownLatch(1);
		AtomicBoolean daemon = new AtomicBoolean();

		UiUtils.bgRun(() -> {
			daemon.set(Thread.currentThread().isDaemon());
			completed.countDown();
		});

		assertThat(completed.await(5, TimeUnit.SECONDS)).isTrue();
		assertThat(daemon).isTrue();
	}
}
