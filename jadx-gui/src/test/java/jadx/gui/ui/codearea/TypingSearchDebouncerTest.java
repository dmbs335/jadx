package jadx.gui.ui.codearea;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.SwingUtilities;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import static org.assertj.core.api.Assertions.assertThat;

class TypingSearchDebouncerTest {

	@Test
	@Timeout(2)
	void rapidTypingRunsOnlyLatestScheduledSearch() throws Exception {
		AtomicInteger searches = new AtomicInteger();
		CountDownLatch completed = new CountDownLatch(1);
		TypingSearchDebouncer debouncer = new TypingSearchDebouncer(25, () -> {
			searches.incrementAndGet();
			completed.countDown();
		});

		SwingUtilities.invokeAndWait(() -> {
			for (int i = 0; i < 10_000; i++) {
				debouncer.restart();
			}
		});

		assertThat(completed.await(1, TimeUnit.SECONDS)).isTrue();
		Thread.sleep(50);
		assertThat(searches).hasValue(1);
	}
}
