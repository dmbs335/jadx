package jadx.gui.report;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JadxExceptionHandlerTest {
	@Test
	void unregisterRemovesRegisteredHandler() {
		Thread.UncaughtExceptionHandler previous = Thread.getDefaultUncaughtExceptionHandler();
		try {
			JadxExceptionHandler.register(null);
			Thread.UncaughtExceptionHandler registered = Thread.getDefaultUncaughtExceptionHandler();
			assertThat(registered).isInstanceOf(JadxExceptionHandler.class);

			JadxExceptionHandler.unregister(null);

			assertThat(Thread.getDefaultUncaughtExceptionHandler()).isNull();
		} finally {
			Thread.setDefaultUncaughtExceptionHandler(previous);
		}
	}
}
