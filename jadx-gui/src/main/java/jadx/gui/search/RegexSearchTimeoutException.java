package jadx.gui.search;

final class RegexSearchTimeoutException extends RuntimeException {
	private static final long serialVersionUID = -4344158286765513709L;

	RegexSearchTimeoutException() {
		super("Regex search stopped: expression exceeded the 2 second match limit");
	}

	RegexSearchTimeoutException(Throwable cause) {
		super("Regex search stopped: expression exceeded safe matcher complexity", cause);
	}
}
