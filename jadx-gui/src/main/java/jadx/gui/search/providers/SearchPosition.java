package jadx.gui.search.providers;

/** Guarantees forward progress for literal and zero-width regex search matches. */
final class SearchPosition {
	private SearchPosition() {
	}

	static int afterMatch(int contentLength, int matchStart, int preferredNext) {
		long next = Math.max((long) preferredNext, (long) matchStart + 1L);
		long terminal = Math.min(Integer.MAX_VALUE, (long) contentLength + 1L);
		return (int) Math.min(terminal, next);
	}

	static boolean exhausted(int contentLength, int position) {
		return position > contentLength;
	}
}
