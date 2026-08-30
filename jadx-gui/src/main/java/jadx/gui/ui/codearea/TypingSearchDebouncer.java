package jadx.gui.ui.codearea;

import javax.swing.Timer;

final class TypingSearchDebouncer {
	private final Timer timer;

	TypingSearchDebouncer(int delayMs, Runnable search) {
		timer = new Timer(delayMs, event -> search.run());
		timer.setRepeats(false);
	}

	void restart() {
		timer.restart();
	}

	void cancel() {
		timer.stop();
	}
}
