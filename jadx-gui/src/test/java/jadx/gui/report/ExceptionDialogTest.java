package jadx.gui.report;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExceptionDialogTest {

	@AfterEach
	void tearDown() {
		ExceptionDialog.markClosed();
	}

	@Test
	void shouldSuppressRecursiveDialogUntilClosed() {
		assertThat(ExceptionDialog.tryOpen()).isTrue();
		assertThat(ExceptionDialog.tryOpen()).isFalse();

		ExceptionDialog.markClosed();

		assertThat(ExceptionDialog.tryOpen()).isTrue();
	}
}
