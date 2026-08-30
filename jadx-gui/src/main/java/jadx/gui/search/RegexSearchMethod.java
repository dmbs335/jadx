package jadx.gui.search;

import java.time.Duration;
import java.util.function.BooleanSupplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jadx.gui.jobs.Cancelable;

final class RegexSearchMethod implements ISearchMethod {
	private static final Duration DEFAULT_MATCH_TIMEOUT = Duration.ofSeconds(2);

	private final Pattern pattern;
	private final long timeoutNanos;

	static ISearchMethod build(Pattern pattern) {
		if (!RegexSearchSafety.requiresGuard(pattern.pattern())) {
			return (input, subStr, start) -> {
				Matcher matcher = pattern.matcher(input);
				return matcher.find(start) ? matcher.start() : -1;
			};
		}
		return new RegexSearchMethod(pattern);
	}

	RegexSearchMethod(Pattern pattern) {
		this(pattern, DEFAULT_MATCH_TIMEOUT);
	}

	RegexSearchMethod(Pattern pattern, Duration matchTimeout) {
		this.pattern = pattern;
		this.timeoutNanos = matchTimeout.toNanos();
	}

	@Override
	public int find(String input, String subStr, int start) {
		return find(input, start, () -> false);
	}

	@Override
	public int find(String input, String subStr, int start, Cancelable cancelable) {
		return find(input, start, cancelable::isCanceled);
	}

	private int find(String input, int start, BooleanSupplier canceled) {
		if (canceled.getAsBoolean()) {
			return -1;
		}
		RegexGuard guard = new RegexGuard(canceled, timeoutNanos);
		CharSequence searchInput = new GuardedCharSequence(input, 0, input.length(), guard);
		Matcher matcher = pattern.matcher(searchInput);
		try {
			if (matcher.find(start)) {
				return matcher.start();
			}
			return -1;
		} catch (RegexCanceledException e) {
			return -1;
		} catch (StackOverflowError e) {
			throw new RegexSearchTimeoutException(e);
		}
	}

	private static final class RegexGuard {
		private static final int CHECK_MASK = 0x3FF;

		private final BooleanSupplier canceled;
		private final long deadlineNanos;
		private int operations;

		private RegexGuard(BooleanSupplier canceled, long timeoutNanos) {
			this.canceled = canceled;
			this.deadlineNanos = System.nanoTime() + timeoutNanos;
		}

		private void check() {
			if ((++operations & CHECK_MASK) != 0) {
				return;
			}
			if (canceled.getAsBoolean()) {
				throw RegexCanceledException.INSTANCE;
			}
			if (System.nanoTime() - deadlineNanos >= 0) {
				throw new RegexSearchTimeoutException();
			}
		}
	}

	private static final class GuardedCharSequence implements CharSequence {
		private final String text;
		private final int start;
		private final int end;
		private final RegexGuard guard;

		private GuardedCharSequence(String text, int start, int end, RegexGuard guard) {
			this.text = text;
			this.start = start;
			this.end = end;
			this.guard = guard;
		}

		@Override
		public int length() {
			return end - start;
		}

		@Override
		public char charAt(int index) {
			guard.check();
			if (index < 0 || index >= length()) {
				throw new IndexOutOfBoundsException(index);
			}
			return text.charAt(start + index);
		}

		@Override
		public CharSequence subSequence(int subStart, int subEnd) {
			if (subStart < 0 || subEnd < subStart || subEnd > length()) {
				throw new IndexOutOfBoundsException(subStart + ".." + subEnd);
			}
			return new GuardedCharSequence(text, start + subStart, start + subEnd, guard);
		}

		@Override
		public String toString() {
			return text.substring(start, end);
		}
	}

	private static final class RegexCanceledException extends RuntimeException {
		private static final long serialVersionUID = -3130260617032523233L;
		private static final RegexCanceledException INSTANCE = new RegexCanceledException();

		private RegexCanceledException() {
			super(null, null, false, false);
		}
	}
}
