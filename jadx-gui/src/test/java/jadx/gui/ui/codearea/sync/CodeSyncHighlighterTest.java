package jadx.gui.ui.codearea.sync;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.Timer;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CodeSyncHighlighterTest {
	@Test
	void removalTimerRunsOnlyOnce() {
		AtomicBoolean removed = new AtomicBoolean();

		Timer timer = CodeSyncHighlighter.buildRemovalTimer(() -> removed.set(true));
		timer.getActionListeners()[0].actionPerformed(null);

		assertThat(timer.isRepeats()).isFalse();
		assertThat(removed).isTrue();
	}
}
