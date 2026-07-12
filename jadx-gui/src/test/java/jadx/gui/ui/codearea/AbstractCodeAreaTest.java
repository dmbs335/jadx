package jadx.gui.ui.codearea;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AbstractCodeAreaTest {

	@Test
	void boundScrollPosition() {
		assertThat(AbstractCodeArea.boundScrollPosition(0, 100)).isZero();
		assertThat(AbstractCodeArea.boundScrollPosition(42, 100)).isEqualTo(42);
		assertThat(AbstractCodeArea.boundScrollPosition(100, 100)).isEqualTo(100);
		assertThat(AbstractCodeArea.boundScrollPosition(101, 100)).isEqualTo(100);
		assertThat(AbstractCodeArea.boundScrollPosition(6853, 100)).isEqualTo(100);
		assertThat(AbstractCodeArea.boundScrollPosition(-1, 100)).isZero();
	}
}
