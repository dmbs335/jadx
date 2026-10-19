package jadx.tests.integration.corpus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntUnaryOperator;

import jadx.tests.api.IntegrationTest;
import jadx.tests.api.extensions.profiles.TestProfile;
import jadx.tests.api.extensions.profiles.TestWithProfiles;

/**
 * Semantic round-trip corpus: every sample is compiled, decompiled, compiled again and its
 * {@code check()} method is executed both before and after decompilation.
 */
public class TestSemanticRoundTripCorpus extends IntegrationTest {

	public static class ComplexLoopFlow {
		private int calculate(int[][] values, int limit) {
			int result = 0;
			outer: for (int row = 0; row < values.length; row++) {
				int col = 0;
				do {
					int value = values[row][col++];
					switch ((value + row) & 3) {
						case 0:
							result += value;
							break;
						case 1:
							continue;
						case 2:
							if (value < 0) {
								continue outer;
							}
							result -= value;
							break;
						default:
							result ^= value;
					}
					if (Math.abs(result) > limit) {
						break outer;
					}
				} while (col < values[row].length);
			}
			return result;
		}

		public void check() {
			int[][] values = { { 4, 1, 7 }, { 2, -3, 8 }, { 5, 6, 9 } };
			if (calculate(values, 100) != 3 || calculate(values, 3) != 4) {
				throw new AssertionError("complex loop flow");
			}
		}
	}

	public static class FinallyControlFlow {
		private int trace;

		private int calculate(int stop) {
			int result = 0;
			for (int i = 0; i < 7; i++) {
				try {
					if (i == stop) {
						break;
					}
					if ((i & 1) == 0) {
						continue;
					}
					result += 10 / (i - 3);
				} catch (ArithmeticException e) {
					result += e.getClass().getSimpleName().length();
				} finally {
					trace = trace * 10 + i;
					result += i;
				}
			}
			return result;
		}

		public void check() {
			trace = 0;
			int first = calculate(6);
			if (first != 40 || trace != 123456) {
				throw new AssertionError("finally loop: " + first + '/' + trace);
			}
			trace = 0;
			int second = calculate(2);
			if (second != -2 || trace != 12) {
				throw new AssertionError("finally break: " + second + '/' + trace);
			}
		}
	}

	public static class ResourceAndSuppressionFlow {
		private static class Probe implements AutoCloseable {
			private final List<String> events;
			private final String name;
			private final boolean fail;

			Probe(List<String> events, String name, boolean fail) {
				this.events = events;
				this.name = name;
				this.fail = fail;
				events.add("open-" + name);
			}

			@Override
			public void close() {
				events.add("close-" + name);
				if (fail) {
					throw new IllegalStateException(name);
				}
			}
		}

		private String run(boolean bodyFails) {
			List<String> events = new ArrayList<>();
			try (Probe first = new Probe(events, "a", true);
					Probe second = new Probe(events, "b", false)) {
				events.add("body");
				if (bodyFails) {
					throw new IllegalArgumentException("body");
				}
			} catch (RuntimeException e) {
				events.add(e.getClass().getSimpleName() + ':' + e.getSuppressed().length);
			}
			return String.join(",", events);
		}

		public void check() {
			String closeFailure = "open-a,open-b,body,close-b,close-a,IllegalStateException:0";
			String bodyFailure = "open-a,open-b,body,close-b,close-a,IllegalArgumentException:1";
			String normal = run(false);
			String failing = run(true);
			if (!closeFailure.equals(normal) || !bodyFailure.equals(failing)) {
				throw new AssertionError("resource suppression: " + normal + " | " + failing);
			}
		}
	}

	public static class SynchronizedExceptionFlow {
		private final Object lock = new Object();
		private int state;

		private int update(int value) {
			synchronized (lock) {
				state += value;
				try {
					if (value < 0) {
						throw new IllegalArgumentException("negative");
					}
					return state * 2;
				} finally {
					state ^= value << 1;
				}
			}
		}

		public void check() {
			if (update(3) != 6 || state != 5) {
				throw new AssertionError("synchronized return");
			}
			try {
				update(-2);
				throw new AssertionError("missing exception");
			} catch (IllegalArgumentException expected) {
				if (state != -1) {
					throw new AssertionError("synchronized finally: " + state);
				}
			}
		}
	}

	public static class PrimitiveArrayFlow {
		private long calculate(int seed) {
			byte b = (byte) (seed * 37);
			short s = (short) (b * 257);
			char c = (char) (s ^ 0xaaaa);
			int[][] matrix = new int[3][];
			matrix[0] = new int[] { b, s, c };
			matrix[1] = Arrays.copyOf(matrix[0], 5);
			matrix[2] = matrix[1].clone();
			long result = 0;
			for (int[] row : matrix) {
				for (int value : row) {
					result = Long.rotateLeft(result ^ value, 7) + (value >>> 3);
				}
			}
			return result ^ (((long) c << 48) | ((long) s & 0xffffL));
		}

		public void check() {
			if (calculate(0) != 9079256940138190314L
					|| calculate(17) != -636972479920501535L
					|| calculate(-91) != 8569513702387388454L) {
				throw new AssertionError("primitive array flow");
			}
		}
	}

	public static class StringSwitchCollisionFlow {
		private int classify(String value) {
			if (value == null) {
				return -1;
			}
			switch (value) {
				case "FB":
					return 10;
				case "Ea": // same String hash as "FB"
					return 20;
				case "Aa":
					return value.length() + 30;
				case "BB": // same String hash as "Aa"
					return value.charAt(0) + 40;
				default:
					return value.hashCode() & 7;
			}
		}

		public void check() {
			if (classify(null) != -1 || classify("FB") != 10 || classify("Ea") != 20
					|| classify("Aa") != 32 || classify("BB") != 106 || classify("other") != 0) {
				throw new AssertionError("string switch collision");
			}
		}
	}

	public static class GenericBridgeFlow {
		private interface Mapper<T> {
			T map(T value);
		}

		private static class NumberMapper<N extends Number & Comparable<N>> implements Mapper<N> {
			private final N fallback;

			NumberMapper(N fallback) {
				this.fallback = fallback;
			}

			@Override
			public N map(N value) {
				return value.compareTo(fallback) < 0 ? fallback : value;
			}
		}

		private <T> List<T> copyAndReverse(List<? extends T> input, T tail) {
			List<T> result = new ArrayList<>(input);
			result.add(tail);
			Collections.reverse(result);
			return result;
		}

		public void check() {
			Mapper<Integer> mapper = new NumberMapper<>(5);
			List<Number> values = copyAndReverse(Arrays.asList(mapper.map(2), mapper.map(9)), 11L);
			if (!values.equals(Arrays.asList(11L, 9, 5))) {
				throw new AssertionError("generic bridge: " + values);
			}
		}
	}

	public static class LambdaCaptureFlow {
		private int calculate(int base) {
			AtomicInteger calls = new AtomicInteger();
			IntUnaryOperator first = value -> {
				calls.incrementAndGet();
				return value * 3 + base;
			};
			IntUnaryOperator second = value -> first.applyAsInt(value ^ base) - calls.get();
			return second.andThen(value -> value >>> 1).applyAsInt(13) * 10 + calls.get();
		}

		public void check() {
			if (calculate(7) != 181 || calculate(-3) != -259) {
				throw new AssertionError("lambda capture");
			}
		}
	}

	@SuppressWarnings("checkstyle:FallThrough")
	public static class EnumFallThroughFlow {
		private enum Phase {
			NEW,
			READY,
			RUNNING,
			FAILED,
			DONE
		}

		private int advance(Phase... phases) {
			int score = 0;
			for (Phase phase : phases) {
				switch (phase) {
					case NEW:
						score++;
						continue;
					case READY:
						score += 2;
					case RUNNING:
						score *= 3;
						break;
					case FAILED:
						score -= 7;
						if (score < 0) {
							continue;
						}
					case DONE:
						score ^= 0x55;
						break;
					default:
						throw new AssertionError(phase);
				}
			}
			return score;
		}

		public void check() {
			if (advance(Phase.NEW, Phase.READY, Phase.RUNNING, Phase.FAILED, Phase.DONE) != 20
					|| advance(Phase.FAILED, Phase.DONE) != -84) {
				throw new AssertionError("enum fall-through");
			}
		}
	}

	public static class ExceptionPhiFlow {
		private int calculate(String input, int mode) {
			int value = 5;
			try {
				value += Integer.parseInt(input);
				if (mode == 1) {
					throw new UnsupportedOperationException("mode");
				}
				if (mode == 2) {
					throw new IllegalStateException("state");
				}
				return value * 2;
			} catch (NumberFormatException e) {
				value += 11;
			} catch (UnsupportedOperationException e) {
				value += 11;
			} catch (IllegalStateException e) {
				value = -value;
			} finally {
				value ^= 3;
			}
			return value;
		}

		public void check() {
			if (calculate("4", 0) != 18 || calculate("x", 0) != 19
					|| calculate("4", 1) != 23 || calculate("4", 2) != -12) {
				throw new AssertionError("exception phi");
			}
		}
	}

	public static class ExceptionStateBetweenCallsFlow {
		private static void parse(String value) {
			Integer.parseInt(value);
		}

		private int evaluate(String first, String second) {
			int state = 1;
			try {
				parse(first);
				state = 2;
				parse(second);
				state = 3;
			} catch (NumberFormatException e) {
				return state;
			}
			return state;
		}

		private int evaluateAssignment(String first, String second) {
			int state = 7;
			try {
				state = Integer.parseInt(first);
				Integer.parseInt(second);
			} catch (NumberFormatException e) {
				return state;
			}
			return state;
		}

		public void check() {
			if (evaluate("bad", "4") != 1
					|| evaluate("4", "bad") != 2
					|| evaluate("4", "5") != 3
					|| evaluateAssignment("bad", "5") != 7
					|| evaluateAssignment("4", "bad") != 4
					|| evaluateAssignment("4", "5") != 4) {
				throw new AssertionError("exception state between calls");
			}
		}
	}

	public static class ExpressionMatrixFlow {
		private long mix(int x, long y) {
			int narrowByte = (byte) (x * 31);
			int narrowShort = (short) (x >>> 2);
			long rotated = Long.rotateRight(y ^ ((long) narrowByte << 32), x & 63);
			long selected = (x & 1) == 0 ? rotated + narrowShort : ~rotated - narrowShort;
			return selected ^ (char) (narrowByte - narrowShort);
		}

		public void check() {
			if (mix(0, 5) != 5
					|| mix(1, 5) != 9223371970282782690L
					|| mix(-7, 0x123456789abcdef0L) != -1885653428083849263L) {
				throw new AssertionError("expression matrix");
			}
		}
	}

	public static class LabeledFinallyExitFlow {
		private int run(int mode) {
			int result = 0;
			outer: for (int i = 0; i < 5; i++) {
				try {
					if (i + mode == 1) {
						continue;
					}
					if (i + mode == 3) {
						break outer;
					}
					if (i == 4 && mode < 0) {
						return result - 7;
					}
					result = result * 10 + i;
				} finally {
					result += 5;
				}
			}
			return result;
		}

		public void check() {
			if (run(0) != 112 || run(1) != 61 || run(-4) != 5671) {
				throw new AssertionError("labeled finally exits");
			}
		}
	}

	public static class MultiCatchStateFlow {
		private int evaluate(String text, int mode) {
			int value = 7;
			try {
				value += Integer.parseInt(text);
				if (mode == 1) {
					throw new UnsupportedOperationException("operation");
				}
				if (mode == 2) {
					throw new IllegalStateException("state");
				}
				value *= 2;
			} catch (NumberFormatException | UnsupportedOperationException e) {
				value += 9;
			} catch (IllegalStateException e) {
				value = -value;
			} finally {
				value = (value << 1) ^ 5;
			}
			return value;
		}

		public void check() {
			if (evaluate("3", 0) != 45 || evaluate("x", 0) != 37
					|| evaluate("3", 1) != 35 || evaluate("3", 2) != -23) {
				throw new AssertionError("multi-catch state");
			}
		}
	}

	public static class SwitchLoopExceptionStateFlow {
		private int fold(String[] values, int mode) {
			int acc = 3;
			for (int i = 0; i < values.length; i++) {
				try {
					switch ((i + mode) & 3) {
						case 0:
							acc += Integer.parseInt(values[i]);
							break;
						case 1:
							if (values[i] == null) {
								throw new NullPointerException("value");
							}
							acc ^= values[i].length();
							continue;
						case 2:
							acc *= Integer.parseInt(values[i]);
							break;
						default:
							return acc - i;
					}
				} catch (NumberFormatException | NullPointerException e) {
					acc = acc * 5 + i;
				} finally {
					acc ^= i + 11;
				}
			}
			return acc;
		}

		public void check() {
			String[] first = { "2", "bad", null };
			String[] second = { null, "4", "5" };
			if (fold(first, 0) != 10 || fold(first, 1) != 32
					|| fold(second, 0) != 32 || fold(second, 3) != 3) {
				throw new AssertionError("switch loop exception state");
			}
		}
	}

	public static class NestedFinallyReturnFlow {
		private int state;

		private int evaluate(int mode) {
			try {
				state = 1;
				try {
					if (mode == 0) {
						return 10;
					}
					if (mode == 1) {
						throw new IllegalArgumentException("mode");
					}
					state = 2;
					return 20;
				} catch (IllegalArgumentException e) {
					state = 3;
					return 30;
				} finally {
					state = state * 10 + 4;
				}
			} finally {
				state = state * 10 + 5;
			}
		}

		public void check() {
			state = 0;
			if (evaluate(0) != 10 || state != 145) {
				throw new AssertionError("nested finally direct return: " + state);
			}
			state = 0;
			if (evaluate(1) != 30 || state != 345) {
				throw new AssertionError("nested finally caught return: " + state);
			}
			state = 0;
			if (evaluate(2) != 20 || state != 245) {
				throw new AssertionError("nested finally tail return: " + state);
			}
		}
	}

	public static class ArrayEvaluationOrderFlow {
		private int cursor;
		private int calls;

		private int next() {
			calls++;
			return cursor++;
		}

		@SuppressWarnings("checkstyle:InnerAssignment")
		private int evaluate() {
			int[] values = { 2, 3, 4, 5 };
			cursor = 0;
			calls = 0;
			values[next()] += values[next()] *= 2;
			values[cursor++] ^= cursor + values[cursor++];
			return values[0] * 10000 + values[1] * 1000 + values[2] * 100 + values[3] * 10
					+ cursor + calls;
		}

		public void check() {
			if (evaluate() != 87256 || cursor != 4 || calls != 2) {
				throw new AssertionError("array evaluation order");
			}
		}
	}

	public static class ExceptionalExpressionOrderFlow {
		private int trace;

		private int step(int value, int failAt) {
			trace = trace * 10 + value;
			if (value == failAt) {
				throw new IllegalStateException("step " + value);
			}
			return value;
		}

		private int evaluate(int failAt) {
			trace = 0;
			try {
				int result = step(1, failAt) + step(2, failAt) * step(3, failAt);
				return result * 10000 + trace;
			} catch (IllegalStateException e) {
				return -trace;
			} finally {
				trace = trace * 10 + 9;
			}
		}

		public void check() {
			if (evaluate(0) != 70123 || trace != 1239) {
				throw new AssertionError("expression normal order: " + trace);
			}
			if (evaluate(2) != -12 || trace != 129) {
				throw new AssertionError("expression middle failure: " + trace);
			}
			if (evaluate(3) != -123 || trace != 1239) {
				throw new AssertionError("expression tail failure: " + trace);
			}
		}
	}

	public static class FinallyOverrideFlow {
		private int trace;

		private int evaluate(int mode) {
			try {
				trace = 1;
				if (mode == 3) {
					throw new IllegalArgumentException("body");
				}
				return 10 + mode;
			} catch (IllegalArgumentException e) {
				trace = 2;
				return 20;
			} finally {
				trace = trace * 10 + 5;
				if (mode == 1) {
					return 99;
				}
				if (mode == 2) {
					throw new IllegalStateException("finally");
				}
			}
		}

		public void check() {
			if (evaluate(0) != 10 || trace != 15) {
				throw new AssertionError("finally keeps return: " + trace);
			}
			if (evaluate(1) != 99 || trace != 15) {
				throw new AssertionError("finally overrides return: " + trace);
			}
			try {
				evaluate(2);
				throw new AssertionError("finally must throw");
			} catch (IllegalStateException e) {
				if (trace != 15) {
					throw new AssertionError("finally throw state: " + trace);
				}
			}
			if (evaluate(3) != 20 || trace != 25) {
				throw new AssertionError("finally after catch return: " + trace);
			}
		}
	}

	public static class FinallyThrowOverrideFlow {
		private int trace;

		private int evaluate(int mode) {
			try {
				trace = 1;
				if (mode == 3) {
					throw new IllegalArgumentException("body");
				}
				return 10 + mode;
			} catch (IllegalArgumentException e) {
				trace = 2;
				return 20;
			} finally {
				trace = trace * 10 + 6;
				if (mode == 1) {
					throw new IllegalStateException("finally");
				}
			}
		}

		public void check() {
			if (evaluate(0) != 10 || trace != 16) {
				throw new AssertionError("finally keeps return: " + trace);
			}
			try {
				evaluate(1);
				throw new AssertionError("finally must throw");
			} catch (IllegalStateException e) {
				if (trace != 16) {
					throw new AssertionError("finally throw state: " + trace);
				}
			}
			if (evaluate(3) != 20 || trace != 26) {
				throw new AssertionError("finally after catch return: " + trace);
			}
		}
	}

	public static class FinallyReturnOverrideFlow {
		private int trace;

		private int evaluate(int mode) {
			try {
				trace = 1;
				if (mode == 3) {
					throw new IllegalArgumentException("body");
				}
				return 10 + mode;
			} catch (IllegalArgumentException e) {
				trace = 2;
				return 20;
			} finally {
				trace = trace * 10 + 7;
				if (mode == 1) {
					return 99;
				}
			}
		}

		public void check() {
			if (evaluate(0) != 10 || trace != 17) {
				throw new AssertionError("finally keeps return: " + trace);
			}
			if (evaluate(1) != 99 || trace != 17) {
				throw new AssertionError("finally overrides return: " + trace);
			}
			if (evaluate(3) != 20 || trace != 27) {
				throw new AssertionError("finally after catch return: " + trace);
			}
		}
	}

	public static class FinallySwitchOverrideFlow {
		private int trace;

		private int evaluate(int mode) {
			try {
				trace = 1;
				if (mode == 3) {
					throw new IllegalArgumentException("body");
				}
				return 10 + mode;
			} catch (IllegalArgumentException e) {
				trace = 2;
				return 20;
			} finally {
				trace = trace * 10 + 8;
				switch (mode) {
					case 1:
						throw new IllegalStateException("finally");
					case 2:
						return 98;
					default:
						break;
				}
			}
		}

		public void check() {
			if (evaluate(0) != 10 || trace != 18) {
				throw new AssertionError("finally keeps return: " + trace);
			}
			try {
				evaluate(1);
				throw new AssertionError("finally must throw");
			} catch (IllegalStateException e) {
				if (trace != 18) {
					throw new AssertionError("finally throw state: " + trace);
				}
			}
			if (evaluate(2) != 98 || trace != 18) {
				throw new AssertionError("finally switch return: " + trace);
			}
			if (evaluate(3) != 20 || trace != 28) {
				throw new AssertionError("finally after catch return: " + trace);
			}
		}
	}

	public static class MultiCatchFinallyThrowFlow {
		private int trace;

		private int evaluate(String input, int mode) {
			try {
				trace = 1;
				if (mode == 4) {
					throw new IllegalArgumentException("mode");
				}
				return Integer.parseInt(input) + mode;
			} catch (NumberFormatException e) {
				trace = 2;
				return 20;
			} catch (IllegalArgumentException e) {
				trace = 3;
				return 30;
			} finally {
				trace = trace * 10 + 9;
				if (mode == 1) {
					throw new IllegalStateException("finally");
				}
			}
		}

		public void check() {
			if (evaluate("5", 0) != 5 || trace != 19) {
				throw new AssertionError("normal return: " + trace);
			}
			if (evaluate("bad", 0) != 20 || trace != 29) {
				throw new AssertionError("number catch: " + trace);
			}
			if (evaluate("5", 4) != 30 || trace != 39) {
				throw new AssertionError("argument catch: " + trace);
			}
			try {
				evaluate("5", 1);
				throw new AssertionError("finally must throw");
			} catch (IllegalStateException e) {
				if (trace != 19) {
					throw new AssertionError("finally throw state: " + trace);
				}
			}
		}
	}

	public static class FinallySameTypeThrowFlow {
		private int trace;

		private int evaluate(String input, int mode) {
			try {
				trace = 1;
				return Integer.parseInt(input);
			} catch (IllegalArgumentException e) {
				trace = 2;
				return 20;
			} finally {
				trace = trace * 10 + 4;
				if (mode == 1) {
					throw new IllegalArgumentException("finally");
				}
			}
		}

		public void check() {
			if (evaluate("5", 0) != 5 || trace != 14) {
				throw new AssertionError("normal return: " + trace);
			}
			if (evaluate("bad", 0) != 20 || trace != 24) {
				throw new AssertionError("caught return: " + trace);
			}
			try {
				evaluate("5", 1);
				throw new AssertionError("finally exception must escape");
			} catch (IllegalArgumentException e) {
				if (trace != 14 || !"finally".equals(e.getMessage())) {
					throw new AssertionError("finally exception identity: " + trace);
				}
			}
		}
	}

	public static class NestedSwitchFinallyLoopFlow {
		private int run(int mode) {
			int trace = 0;
			int acc = 2;
			outer: for (int i = 0; i < 4; i++) {
				try {
					switch ((i + mode) & 3) {
						case 0:
							acc = acc * 10 + i;
							break;
						case 1:
							acc += 7;
							continue outer;
						case 2:
							try {
								acc += 20 / (i - 1);
							} catch (ArithmeticException e) {
								acc += 13;
							}
							break;
						default:
							if (mode < 0) {
								break outer;
							}
							acc ^= i;
					}
					acc += 3;
				} finally {
					trace = trace * 10 + i;
					acc += 1;
				}
			}
			return acc * 10_000 + trace;
		}

		public void check() {
			if (run(-1) != 30_000 || run(0) != 630_123
					|| run(1) != 2_970_123 || run(2) != -759_877) {
				throw new AssertionError("nested switch/finally loop");
			}
		}
	}

	@SuppressWarnings("checkstyle:FallThrough")
	public static class SwitchFallThroughCatchFlow {
		private int run(int mode) {
			int acc = 1;
			int trace = 0;
			for (int i = 0; i < 5; i++) {
				try {
					switch ((i + mode) & 3) {
						case 0:
							acc += i + 1;
						case 1:
							acc *= 2;
							if (i == 2) {
								throw new IllegalArgumentException("case");
							}
						case 2:
							acc += 3;
							break;
						default:
							continue;
					}
				} catch (IllegalArgumentException e) {
					acc = -acc;
				} finally {
					trace = trace * 10 + i;
					acc += 10;
				}
				acc ^= i + mode;
			}
			return acc * 100_000 + trace;
		}

		public void check() {
			if (run(-2) != 5_901_234 || run(0) != 15_301_234
					|| run(1) != 20_601_234 || run(3) != -4_598_766) {
				throw new AssertionError("switch fall-through catch");
			}
		}
	}

	public static class NestedLoopSwitchCatchFinallyFlow {
		private int run(int mode) {
			int acc = 1;
			int trace = 0;
			outer: for (int row = 0; row < 4; row++) {
				int col = 0;
				while (col < 5) {
					int value = row * 5 + col++;
					try {
						switch ((value + mode) & 3) {
							case 0:
								acc += value;
								continue;
							case 1:
								if (row + mode == 2) {
									throw new IllegalStateException("row");
								}
								acc = acc * 2 + col;
								break;
							case 2:
								if (col == 3) {
									continue outer;
								}
								acc ^= value;
								break;
							default:
								if (row == 3) {
									break outer;
								}
								acc -= col;
						}
						acc += 7;
					} catch (IllegalStateException e) {
						acc += 100 + row;
						if ((mode & 1) == 0) {
							continue outer;
						}
					} finally {
						trace = (trace * 17 + row * 5 + col) % 10_000;
						acc += 3;
					}
					acc ^= trace & 7;
				}
			}
			return acc * 10_000 + trace;
		}

		public void check() {
			if (run(-2) != 8_913_598 || run(-1) != 7_036_038
					|| run(0) != 3_305_216 || run(1) != 11_102_683
					|| run(2) != 13_393_545 || run(3) != 7_036_038) {
				throw new AssertionError("nested loop switch catch/finally");
			}
		}
	}

	public static class SideEffectGuardCatchFinallyFlow {
		private int state;

		private int probe(int value) {
			state = (state * 17 + value) & 0xffff;
			if ((state & 31) == 7) {
				throw new IllegalStateException("probe");
			}
			return state;
		}

		private long run(int mode) {
			state = mode + 7;
			int acc = 3;
			outer: for (int row = 0; row < 5; row++) {
				for (int col = 0; col < 4; col++) {
					try {
						int selector = (probe(row + 1) + col + mode) & 3;
						switch (selector) {
							case 0:
								if ((probe(col + 2) & 1) == 0
										&& (probe(mode + 5) % 3 != 0 || probe(row + 6) > 0)) {
									acc += 11;
								} else {
									acc -= 3;
								}
								break;
							case 1:
								acc ^= probe(row + col + 3);
								continue;
							case 2:
								try {
									acc += 50 / (probe(col - mode + 1) % 4);
								} catch (ArithmeticException e) {
									acc += 13;
								}
								if ((acc & 7) == 0) {
									continue outer;
								}
								break;
							default:
								if (row == 4 && (probe(col + 1) & 1) != 0) {
									break outer;
								}
								acc += probe(2);
						}
						acc += 5;
					} catch (IllegalStateException e) {
						acc -= 17;
						if ((state & 1) == 0) {
							continue outer;
						}
					} finally {
						acc = acc * 3 + row - col;
						state ^= acc & 15;
					}
					acc ^= state;
				}
			}
			return ((long) acc << 32) | (state & 0xffffL);
		}

		public void check() {
			long[] actual = { run(-3), run(-1), run(0), run(2), run(5) };
			long[] expected = {
					-3_886_225_716_684_984_206L,
					-8_259_634_345_464_023_146L,
					-4_648_195_649_920_028_198L,
					-4_882_732_028_564_298_879L,
					-7_263_064_725_766_769_318L
			};
			if (!Arrays.equals(actual, expected)) {
				throw new AssertionError("side-effect guard/catch/finally: " + Arrays.toString(actual));
			}
		}
	}

	public static class CatchHierarchyFinallyLoopFlow {
		private String run(String[] values, int mode) {
			StringBuilder trace = new StringBuilder();
			outer: for (int i = 0; i < values.length; i++) {
				try {
					trace.append('T').append(i);
					int value = Integer.parseInt(values[i]);
					if (value < 0) {
						throw new IllegalArgumentException("negative");
					}
					if (mode == 1 && i == 1) {
						throw new IllegalStateException("stop");
					}
					trace.append('V').append(value);
				} catch (NumberFormatException e) {
					trace.append('N');
					continue;
				} catch (IllegalArgumentException e) {
					trace.append('A');
				} catch (RuntimeException e) {
					trace.append('R');
					break outer;
				} finally {
					trace.append('F');
				}
				trace.append('Z');
			}
			return trace.toString();
		}

		public void check() {
			String mixed = run(new String[] { "2", "x", "-1", "4" }, 0);
			String stopped = run(new String[] { "2", "3", "-1", "4" }, 1);
			if (!"T0V2FZT1NFT2AFZT3V4FZ".equals(mixed)
					|| !"T0V2FZT1RF".equals(stopped)) {
				throw new AssertionError("catch hierarchy/finally: " + mixed + " | " + stopped);
			}
		}
	}

	public static class NestedHandlerLoopExitFlow {
		private String continueOuter() {
			StringBuilder trace = new StringBuilder();
			outer: for (int i = 0; i < 3; i++) {
				for (int j = 0; j < 3; j++) {
					try {
						if (j == 1) {
							throw new NumberFormatException("next");
						}
						trace.append('T').append(i).append(j);
					} catch (NumberFormatException e) {
						trace.append('C');
						continue outer;
					} finally {
						trace.append('F');
					}
					trace.append('Z');
				}
				trace.append('O');
			}
			return trace.toString();
		}

		private String breakOuter() {
			StringBuilder trace = new StringBuilder();
			outer: for (int i = 0; i < 3; i++) {
				for (int j = 0; j < 3; j++) {
					try {
						if (j == 1) {
							throw new NumberFormatException("stop");
						}
						trace.append('T').append(i).append(j);
					} catch (NumberFormatException e) {
						trace.append('C');
						break outer;
					} finally {
						trace.append('F');
					}
					trace.append('Z');
				}
				trace.append('O');
			}
			return trace.toString();
		}

		public void check() {
			String continued = continueOuter();
			String stopped = breakOuter();
			if (!"T00FZCFT10FZCFT20FZCF".equals(continued)
					|| !"T00FZCF".equals(stopped)) {
				throw new AssertionError("nested handler loop exits: " + continued + " | " + stopped);
			}
		}
	}

	public static class WhileCatchSharedTailFlow {
		private int run(String[] values) {
			int acc = 1;
			int i = 0;
			while (i < values.length) {
				try {
					acc += Integer.parseInt(values[i]);
				} catch (NumberFormatException e) {
					acc += 7;
				} finally {
					i++;
				}
				acc = acc * 3 + i;
			}
			return acc;
		}

		public void check() {
			int result = run(new String[] { "2", "bad", "4" });
			if (result != 174) {
				throw new AssertionError("while catch shared tail: " + result);
			}
		}
	}

	public static class NestedFinallySharedTailFlow {
		private int run(boolean fail) {
			int result = 1;
			try {
				try {
					if (fail) {
						throw new IllegalArgumentException("fail");
					}
					result += 2;
				} catch (IllegalArgumentException e) {
					result += 4;
				} finally {
					result *= 3;
				}
			} finally {
				result += 5;
			}
			return result * 2;
		}

		public void check() {
			int success = run(false);
			int failed = run(true);
			if (success != 28 || failed != 40) {
				throw new AssertionError("nested finally shared tail: " + success + " | " + failed);
			}
		}
	}

	public static class CatchFinallyExpressionTailFlow {
		private int run(String value, int mode) {
			int state = 3;
			try {
				state += Integer.parseInt(value);
				if (mode == 1) {
					return state * 2;
				}
			} catch (NumberFormatException e) {
				state = state * 5 + 1;
			} finally {
				state = state * 3 + mode;
			}
			return (state + 7) * (mode + 2);
		}

		public void check() {
			int normal = run("4", 0);
			int caught = run("bad", 0);
			int returned = run("4", 1);
			int caughtWithMode = run("bad", 2);
			if (normal != 56 || caught != 110 || returned != 14 || caughtWithMode != 228) {
				throw new AssertionError("catch/finally expression tail: "
						+ normal + " | " + caught + " | " + returned + " | " + caughtWithMode);
			}
		}
	}

	public static class NestedFinallyConditionalTailFlow {
		private int run(boolean fail, boolean early) {
			int result = 1;
			try {
				try {
					if (fail) {
						throw new IllegalArgumentException("fail");
					}
					result += 2;
				} catch (IllegalArgumentException e) {
					result += 4;
				} finally {
					result *= 3;
				}
			} finally {
				result += 5;
			}
			if (early) {
				return result;
			}
			return result * 2;
		}

		public void check() {
			int normal = run(false, false);
			int caught = run(true, false);
			int normalEarly = run(false, true);
			int caughtEarly = run(true, true);
			if (normal != 28 || caught != 40 || normalEarly != 14 || caughtEarly != 20) {
				throw new AssertionError("nested finally conditional tail: "
						+ normal + " | " + caught + " | " + normalEarly + " | " + caughtEarly);
			}
		}
	}

	public static class NestedFinallyExceptionalStateFlow {
		private int observed;

		private void run(boolean fail) {
			int result = 1;
			try {
				try {
					if (fail) {
						throw new IllegalStateException("fail");
					}
					result += 2;
				} finally {
					result *= 3;
				}
			} finally {
				result += 5;
				observed = result;
			}
		}

		public void check() {
			run(false);
			if (observed != 14) {
				throw new AssertionError("normal nested finally state: " + observed);
			}
			try {
				run(true);
				throw new AssertionError("exception expected");
			} catch (IllegalStateException expected) {
				if (observed != 8) {
					throw new AssertionError("exceptional nested finally state: " + observed);
				}
			}
		}
	}

	public static class SynchronizedAbruptExitFlow {
		private final Object lock = new Object();

		private String run(int mode) {
			StringBuilder trace = new StringBuilder();
			outer: for (int i = 0; i < 4; i++) {
				synchronized (lock) {
					trace.append('L').append(i);
					try {
						if (mode == 0 && i == 1) {
							continue outer;
						}
						if (mode == 1 && i == 2) {
							break outer;
						}
						if (mode == 2 && i == 1) {
							trace.append('R');
							return trace.toString();
						}
						if (mode == 3 && i == 1) {
							throw new IllegalArgumentException("caught");
						}
						trace.append('B');
					} catch (IllegalArgumentException e) {
						trace.append('C');
					} finally {
						trace.append('F');
					}
				}
				trace.append('Z');
			}
			return trace.toString();
		}

		public void check() {
			String continued = run(0);
			String stopped = run(1);
			String returned = run(2);
			String caught = run(3);
			if (!"L0BFZL1FL2BFZL3BFZ".equals(continued)
					|| !"L0BFZL1BFZL2F".equals(stopped)
					|| !"L0BFZL1R".equals(returned)
					|| !"L0BFZL1CFZL2BFZL3BFZ".equals(caught)) {
				throw new AssertionError("synchronized abrupt exits: "
						+ continued + " | " + stopped + " | " + returned + " | " + caught);
			}
		}
	}

	public static class NestedFinallyRethrowStateFlow {
		private int run(int mode) {
			int state = 1;
			try {
				try {
					state += 2;
					if (mode != 0) {
						throw new IllegalArgumentException("inner");
					}
					state *= 3;
				} catch (IllegalArgumentException e) {
					state += 5;
					throw e;
				} finally {
					state += 7;
				}
				state += 11;
			} catch (IllegalArgumentException e) {
				state = state * 3 + 13;
			} finally {
				state = state * 2 + 17;
			}
			return state;
		}

		public void check() {
			int normal = run(0);
			int rethrown = run(1);
			if (normal != 71 || rethrown != 133) {
				throw new AssertionError("nested finally rethrow state: "
						+ normal + " | " + rethrown);
			}
		}
	}

	public static class FinallyLabeledSwitchExitFlow {
		private String run(int mode) {
			StringBuilder trace = new StringBuilder();
			outer: for (int i = 0; i < 3; i++) {
				for (int j = 0; j < 3; j++) {
					try {
						trace.append('T').append(i).append(j);
						if (i == 1 && j == 1) {
							switch (mode) {
								case 0:
									continue outer;
								case 1:
									break outer;
								case 2:
									return trace.append('R').toString();
								case 3:
									throw new IllegalStateException("caught");
								default:
									trace.append('D');
							}
						}
					} catch (IllegalStateException e) {
						trace.append('C');
					} finally {
						trace.append('F');
					}
					trace.append('Z');
				}
				trace.append('O');
			}
			return trace.toString();
		}

		public void check() {
			String continued = run(0);
			String stopped = run(1);
			String returned = run(2);
			String caught = run(3);
			if (!"T00FZT01FZT02FZOT10FZT11FT20FZT21FZT22FZO".equals(continued)
					|| !"T00FZT01FZT02FZOT10FZT11F".equals(stopped)
					|| !"T00FZT01FZT02FZOT10FZT11R".equals(returned)
					|| !"T00FZT01FZT02FZOT10FZT11CFZT12FZOT20FZT21FZT22FZO".equals(caught)) {
				throw new AssertionError("finally labeled switch exits: "
						+ continued + " | " + stopped + " | " + returned + " | " + caught);
			}
		}
	}

	public static class SynchronizedReturnSnapshotFlow {
		private final Object lock = new Object();
		private String observed;

		private String run(boolean fail) {
			StringBuilder trace = new StringBuilder();
			synchronized (lock) {
				try {
					trace.append('T');
					if (fail) {
						throw new IllegalArgumentException("fail");
					}
					return trace.append('R').toString();
				} catch (IllegalArgumentException e) {
					return trace.append('C').toString();
				} finally {
					trace.append('F');
					observed = trace.toString();
				}
			}
		}

		public void check() {
			String normal = run(false);
			String normalObserved = observed;
			String failed = run(true);
			String failedObserved = observed;
			if (!"TR".equals(normal) || !"TRF".equals(normalObserved)
					|| !"TC".equals(failed) || !"TCF".equals(failedObserved)) {
				throw new AssertionError("synchronized return snapshot: "
						+ normal + " / " + normalObserved + " | " + failed + " / " + failedObserved);
			}
		}
	}

	public static class ResourceLabeledExitFlow {
		private int state;

		private final class Probe implements AutoCloseable {
			private final int id;
			private final boolean fail;

			private Probe(int id, boolean fail) {
				this.id = id;
				this.fail = fail;
			}

			@Override
			public void close() {
				state = (state * 7 + id) % 1_000_003;
				if (fail) {
					throw new IllegalStateException("close");
				}
			}
		}

		private int run(int mode) {
			state = 1;
			outer: for (int i = 0; i < 3; i++) {
				for (int j = 0; j < 3; j++) {
					int id = i * 3 + j + 1;
					try (Probe ignored = new Probe(id, mode == 4 && i == 1 && j == 1)) {
						state = (state * 5 + id) % 1_000_003;
						if (i == 1 && j == 1) {
							switch (mode) {
								case 0:
									continue outer;
								case 1:
									break outer;
								case 2:
									return state;
								case 3:
									throw new IllegalArgumentException("body");
								default:
									break;
							}
						}
					} catch (IllegalArgumentException e) {
						state = (state * 11 + 3) % 1_000_003;
					} catch (IllegalStateException e) {
						state = (state * 13 + 5) % 1_000_003;
					} finally {
						state = (state * 17 + 7) % 1_000_003;
					}
					state ^= id;
				}
			}
			return state;
		}

		public void check() {
			int continued = run(0);
			int continuedState = state;
			int stopped = run(1);
			int stoppedState = state;
			int returned = run(2);
			int returnedState = state;
			int bodyFailure = run(3);
			int bodyFailureState = state;
			int closeFailure = run(4);
			int closeFailureState = state;
			if (continued != 939_606 || continuedState != 939_606
					|| stopped != 822_260 || stoppedState != 822_260
					|| returned != 116_153 || returnedState != 822_260
					|| bodyFailure != 367_465 || bodyFailureState != 367_465
					|| closeFailure != 404_732 || closeFailureState != 404_732) {
				throw new AssertionError("resource labeled exits: "
						+ continued + '/' + continuedState + " | "
						+ stopped + '/' + stoppedState + " | "
						+ returned + '/' + returnedState + " | "
						+ bodyFailure + '/' + bodyFailureState + " | "
						+ closeFailure + '/' + closeFailureState);
			}
		}
	}

	public static class MultiResourceAbruptFlow {
		private StringBuilder events;

		private final class Probe implements AutoCloseable {
			private final String name;
			private final boolean closeFail;

			private Probe(String name, boolean initFail, boolean closeFail) {
				this.name = name;
				this.closeFail = closeFail;
				events.append('O').append(name);
				if (initFail) {
					throw new IllegalArgumentException("init-" + name);
				}
			}

			@Override
			public void close() {
				events.append('C').append(name);
				if (closeFail) {
					throw new IllegalStateException(name);
				}
			}
		}

		private String run(int mode) {
			events = new StringBuilder();
			outer: for (int i = 0; i < 3; i++) {
				try (Probe first = new Probe("a" + i, false, mode == 5 && i == 1);
						Probe second = new Probe("b" + i, mode == 6 && i == 1,
								(mode == 4 || mode == 5) && i == 1)) {
					events.append('B').append(i);
					if (i == 1) {
						switch (mode) {
							case 0:
								continue outer;
							case 1:
								break outer;
							case 2:
								return events.toString();
							case 3:
								throw new IllegalArgumentException("body");
							default:
								break;
						}
					}
				} catch (RuntimeException e) {
					events.append('X').append(e.getClass().getSimpleName())
							.append(':').append(e.getMessage())
							.append(':').append(e.getSuppressed().length);
				} finally {
					events.append('F').append(i);
				}
				events.append('T').append(i);
			}
			return events.toString();
		}

		private static String normal(int i) {
			return "Oa" + i + "Ob" + i + "B" + i + "Cb" + i + "Ca" + i + "F" + i + "T" + i;
		}

		private void assertRun(int mode, String expectedResult, String expectedEvents) {
			String result = run(mode);
			String actualEvents = events.toString();
			if (!expectedResult.equals(result) || !expectedEvents.equals(actualEvents)) {
				throw new AssertionError("multi-resource abrupt " + mode + ": " + result + " | " + actualEvents);
			}
		}

		public void check() {
			String first = normal(0);
			String secondPrefix = "Oa1Ob1B1";
			String secondClose = "Cb1Ca1F1";
			String third = normal(2);
			assertRun(0, first + secondPrefix + secondClose + third, first + secondPrefix + secondClose + third);
			assertRun(1, first + secondPrefix + secondClose, first + secondPrefix + secondClose);
			assertRun(2, first + secondPrefix, first + secondPrefix + secondClose);
			assertRun(3,
					first + secondPrefix + "Cb1Ca1XIllegalArgumentException:body:0F1T1" + third,
					first + secondPrefix + "Cb1Ca1XIllegalArgumentException:body:0F1T1" + third);
			assertRun(4,
					first + secondPrefix + "Cb1Ca1XIllegalStateException:b1:0F1T1" + third,
					first + secondPrefix + "Cb1Ca1XIllegalStateException:b1:0F1T1" + third);
			assertRun(5,
					first + secondPrefix + "Cb1Ca1XIllegalStateException:b1:1F1T1" + third,
					first + secondPrefix + "Cb1Ca1XIllegalStateException:b1:1F1T1" + third);
			assertRun(6,
					first + "Oa1Ob1Ca1XIllegalArgumentException:init-b1:0F1T1" + third,
					first + "Oa1Ob1Ca1XIllegalArgumentException:init-b1:0F1T1" + third);
		}
	}

	public static class NestedMonitorSwitchFlow {
		private final Object outerLock = new Object();
		private final Object innerLock = new Object();
		private String observed;

		private String run(int mode) {
			StringBuilder trace = new StringBuilder();
			outer: for (int i = 0; i < 3; i++) {
				synchronized (outerLock) {
					trace.append('A').append(i);
					synchronized (innerLock) {
						trace.append('B');
						try {
							trace.append('T');
							if (i == 1) {
								switch (mode) {
									case 0:
										continue outer;
									case 1:
										break outer;
									case 2:
										return trace.append('R').toString();
									case 3:
										throw new IllegalArgumentException("caught");
									default:
										break;
								}
							}
							trace.append('N');
						} catch (IllegalArgumentException e) {
							trace.append('X');
						} finally {
							trace.append('F');
							observed = trace.toString();
						}
						trace.append('C');
					}
					trace.append('D');
				}
				trace.append('Z');
			}
			return trace.toString();
		}

		private void assertRun(int mode, String expectedResult, String expectedObserved) {
			String result = run(mode);
			if (!expectedResult.equals(result) || !expectedObserved.equals(observed)) {
				throw new AssertionError("nested monitor switch " + mode + ": " + result + " | " + observed);
			}
		}

		public void check() {
			String first = "A0BTNFCDZ";
			String third = "A2BTNFCDZ";
			assertRun(0, first + "A1BTF" + third, first + "A1BTF" + "A2BTNF");
			assertRun(1, first + "A1BTF", first + "A1BTF");
			assertRun(2, first + "A1BTR", first + "A1BTRF");
			assertRun(3, first + "A1BTXFCDZ" + third, first + "A1BTXFCDZ" + "A2BTNF");
			assertRun(9,
					first + "A1BTNFCDZ" + third,
					first + "A1BTNFCDZ" + "A2BTNF");
		}
	}

	public static class DoWhileMonitorFinallyFlow {
		private final Object lock = new Object();
		private String observed;

		private String run(int mode) {
			StringBuilder trace = new StringBuilder();
			int i = 0;
			outer: do {
				trace.append('A').append(i);
				synchronized (lock) {
					trace.append('B');
					try {
						switch (i == 1 ? mode : 9) {
							case 0:
								continue outer;
							case 1:
								break outer;
							case 2:
								return trace.append('R').toString();
							case 3:
								throw new IllegalStateException("caught");
							default:
								trace.append('N');
						}
					} catch (IllegalStateException e) {
						trace.append('X');
					} finally {
						trace.append('F');
						observed = trace.toString();
					}
					trace.append('C');
				}
				trace.append('D');
			} while (++i < 3);
			return trace.toString();
		}

		private void assertRun(int mode, String expectedResult, String expectedObserved) {
			String result = run(mode);
			if (!expectedResult.equals(result) || !expectedObserved.equals(observed)) {
				throw new AssertionError("do-while monitor/finally " + mode + ": " + result + " | " + observed);
			}
		}

		public void check() {
			String first = "A0BNFCD";
			String third = "A2BNFCD";
			assertRun(0, first + "A1BF" + third, first + "A1BF" + "A2BNF");
			assertRun(1, first + "A1BF", first + "A1BF");
			assertRun(2, first + "A1BR", first + "A1BRF");
			assertRun(3, first + "A1BXFCD" + third, first + "A1BXFCD" + "A2BNF");
			assertRun(9, first + "A1BNFCD" + third, first + "A1BNFCD" + "A2BNF");
		}
	}

	public static class NestedCatchReturnMutationFlow {
		private int state;
		private int index;

		private int next(int value) {
			state = state * 7 + value;
			return index++ & 3;
		}

		private int run(int mode) {
			int[] values = { 11, 17, 23, 29 };
			state = 1;
			index = 0;
			try {
				try {
					values[next(2)] += 3;
					switch (mode) {
						case 0:
							return values[next(5)]++;
						case 1:
							throw new IllegalArgumentException("caught");
						case 2:
							values[next(7)] = values[next(11)]++;
							break;
						default:
							return state;
					}
					return values[next(13)] + state;
				} catch (IllegalArgumentException e) {
					values[next(17)] ^= state;
					return values[(index - 1) & 3];
				} finally {
					values[next(19)] += state;
					state = state * 23 + values[(index + 1) & 3];
				}
			} finally {
				state = state * 29 + index;
			}
		}

		public void check() {
			StringBuilder result = new StringBuilder();
			for (int mode = 0; mode < 4; mode++) {
				int value = run(mode);
				result.append(value).append('/').append(state).append('/').append(index).append(';');
			}
			String actual = result.toString();
			if (!actual.equals("17/330574/3;65/386602/3;3549/16448254/5;9/55537/2;")) {
				throw new AssertionError("nested catch/return mutation: " + actual);
			}
		}
	}

	public static class GenericInnerConstructionFlow {
		private static class Box<T> {
			private final T prefix;
			private int sequence;

			Box(T prefix) {
				this.prefix = prefix;
			}

			class Entry<U> {
				private final U value;

				Entry(U value) {
					this.value = value;
					sequence = sequence * 10 + 1;
				}

				String render(int suffix) {
					sequence = sequence * 10 + suffix;
					return prefix + ":" + value + ":" + sequence;
				}
			}
		}

		private String run() {
			Box<String> first = new Box<>("a");
			Box<String>.Entry<Integer> one = first.new Entry<>(7);
			Box<String> second = new Box<>("b");
			Box<String>.Entry<String> two = second.new Entry<>("x");
			return one.render(2) + '|' + two.render(3);
		}

		public void check() {
			String actual = run();
			if (!actual.equals("a:7:12|b:x:13")) {
				throw new AssertionError("generic inner construction: " + actual);
			}
		}
	}

	public static class CompoundArrayEvaluationFlow {
		private int state;
		private int index;
		private int[] active;

		private int nextIndex() {
			state = state * 10 + 1;
			return index++;
		}

		private int right(boolean fail) {
			state = state * 10 + 2;
			active[0] = 40;
			if (fail) {
				throw new IllegalStateException("rhs");
			}
			return 4;
		}

		private int run(boolean fail) {
			active = new int[] { 5, 7 };
			state = 0;
			index = 0;
			try {
				active[nextIndex()] += right(fail);
				return active[0] * 100 + active[1];
			} catch (IllegalStateException e) {
				return -state;
			} finally {
				state = state * 10 + 3;
			}
		}

		public void check() {
			int value = run(false);
			if (value != 907 || state != 123 || index != 1) {
				throw new AssertionError("compound array success: " + value + '/' + state + '/' + index);
			}
			value = run(true);
			if (value != -12 || state != 123 || index != 1 || active[0] != 40) {
				throw new AssertionError("compound array failure: " + value + '/' + state + '/' + index);
			}
		}
	}

	public static class EnhancedForFinallyFlow {
		private int trace;

		private int run(int stop) {
			int result = 0;
			outer: for (List<Integer> row : Arrays.asList(Arrays.asList(1, 2, 3), Arrays.asList(4, 5))) {
				for (int value : row) {
					try {
						if (value == stop) {
							break outer;
						}
						if ((value & 1) == 0) {
							continue;
						}
						result += value;
					} finally {
						trace = trace * 10 + value;
						result += 10;
					}
				}
			}
			return result;
		}

		public void check() {
			trace = 0;
			int first = run(4);
			if (first != 44 || trace != 1234) {
				throw new AssertionError("enhanced for break: " + first + '/' + trace);
			}
			trace = 0;
			int second = run(9);
			if (second != 59 || trace != 12345) {
				throw new AssertionError("enhanced for complete: " + second + '/' + trace);
			}
		}
	}

	public static class DefaultMethodLambdaBridgeFlow {
		private interface Mapper<T> {
			T map(T value);

			default T twice(T value) {
				return map(map(value));
			}
		}

		private String run(String prefix) {
			int[] calls = { 0 };
			Mapper<String> mapper = value -> {
				calls[0]++;
				return prefix + calls[0] + value;
			};
			return mapper.twice("x") + '/' + calls[0];
		}

		public void check() {
			String actual = run("p");
			if (!actual.equals("p2p1x/2")) {
				throw new AssertionError("default method lambda bridge: " + actual);
			}
		}
	}

	@SuppressWarnings("checkstyle:InnerAssignment")
	public static class BooleanAssignmentOrderFlow {
		private int state;
		private int value;

		private boolean step(int marker, boolean result) {
			state = state * 10 + marker;
			return result;
		}

		private int run(int initial) {
			state = 0;
			value = initial;
			try {
				boolean result = (step(1, value++ > 0) && step(2, value++ > 2))
						|| (step(3, (value += 2) > 0) && step(4, value-- > 2));
				return (result ? 100 : 0) + value;
			} finally {
				state = state * 10 + 9;
			}
		}

		private void assertRun(int initial, int expectedValue, int expectedState) {
			int actual = run(initial);
			if (actual != expectedValue || state != expectedState) {
				throw new AssertionError("boolean assignment order: " + initial + '=' + actual + '/' + state);
			}
		}

		public void check() {
			assertRun(0, 102, 1349);
			assertRun(1, 104, 12349);
			assertRun(3, 105, 129);
		}
	}

	public static class ArrayAliasAssignmentFlow {
		private int state;
		private int index;
		private int[] active;

		private int nextIndex() {
			state = state * 10 + 1;
			return index++;
		}

		private int[] replace(int[] replacement) {
			state = state * 10 + 2;
			active = replacement;
			return replacement;
		}

		private String run() {
			int[] first = { 10, 20 };
			int[] second = { 3, 4 };
			active = first;
			state = 0;
			index = 0;
			active[nextIndex()] += replace(second)[nextIndex()];
			return first[0] + ":" + active[0] + ":" + index + ":" + state;
		}

		public void check() {
			String actual = run();
			if (!actual.equals("14:3:2:121")) {
				throw new AssertionError("array alias assignment: " + actual);
			}
		}
	}

	public static class ConstructorArgumentOrderFlow {
		private static int trace;

		private static int step(int marker) {
			trace = trace * 10 + marker;
			return trace;
		}

		private static class Base {
			private final int first;
			private final int second;

			Base(int first, int second) {
				this.first = first;
				this.second = second;
				trace = trace * 10 + 4;
			}
		}

		private static class Child extends Base {
			private final int field = step(3);

			Child() {
				super(step(1), step(2));
				trace = trace * 10 + 5;
			}
		}

		private String run() {
			trace = 0;
			Child child = new Child();
			Base base = child;
			return base.first + ":" + base.second + ":" + child.field + ":" + trace;
		}

		public void check() {
			String actual = run();
			if (!actual.equals("1:12:1243:12435")) {
				throw new AssertionError("constructor argument order: " + actual);
			}
		}
	}

	public static class EnumNullSwitchFlow {
		private enum Mode {
			FIRST, SECOND
		}

		private int state;

		private int run(Mode mode) {
			state = 1;
			try {
				switch (mode) {
					case FIRST:
						state = state * 10 + 2;
						return 10;
					case SECOND:
						state = state * 10 + 3;
						return 20;
					default:
						return 30;
				}
			} catch (NullPointerException e) {
				state = state * 10 + 4;
				return -1;
			} finally {
				state = state * 10 + 5;
			}
		}

		private void assertRun(Mode mode, int expectedValue, int expectedState) {
			int actual = run(mode);
			if (actual != expectedValue || state != expectedState) {
				throw new AssertionError("enum null switch: " + mode + '=' + actual + '/' + state);
			}
		}

		public void check() {
			assertRun(Mode.FIRST, 10, 125);
			assertRun(Mode.SECOND, 20, 135);
			assertRun(null, -1, 145);
		}
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testComplexLoopFlow() {
		getClassNode(ComplexLoopFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testFinallyControlFlow() {
		getClassNode(FinallyControlFlow.class);
	}

	@TestWithProfiles({ TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testResourceAndSuppressionFlow() {
		getClassNode(ResourceAndSuppressionFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testSynchronizedExceptionFlow() {
		getClassNode(SynchronizedExceptionFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testPrimitiveArrayFlow() {
		getClassNode(PrimitiveArrayFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testStringSwitchCollisionFlow() {
		getClassNode(StringSwitchCollisionFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testGenericBridgeFlow() {
		getClassNode(GenericBridgeFlow.class);
	}

	@TestWithProfiles({ TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testLambdaCaptureFlow() {
		getClassNode(LambdaCaptureFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testEnumFallThroughFlow() {
		getClassNode(EnumFallThroughFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testExceptionPhiFlow() {
		getClassNode(ExceptionPhiFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testExceptionStateBetweenCallsFlow() {
		getClassNode(ExceptionStateBetweenCallsFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testExpressionMatrixFlow() {
		getClassNode(ExpressionMatrixFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testLabeledFinallyExitFlow() {
		getClassNode(LabeledFinallyExitFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testMultiCatchStateFlow() {
		getClassNode(MultiCatchStateFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testSwitchLoopExceptionStateFlow() {
		getClassNode(SwitchLoopExceptionStateFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testNestedFinallyReturnFlow() {
		getClassNode(NestedFinallyReturnFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testArrayEvaluationOrderFlow() {
		getClassNode(ArrayEvaluationOrderFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testExceptionalExpressionOrderFlow() {
		getClassNode(ExceptionalExpressionOrderFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testFinallyOverrideFlow() {
		getClassNode(FinallyOverrideFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testFinallyThrowOverrideFlow() {
		getClassNode(FinallyThrowOverrideFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testFinallyReturnOverrideFlow() {
		getClassNode(FinallyReturnOverrideFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testFinallySwitchOverrideFlow() {
		getClassNode(FinallySwitchOverrideFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testMultiCatchFinallyThrowFlow() {
		getClassNode(MultiCatchFinallyThrowFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testFinallySameTypeThrowFlow() {
		getClassNode(FinallySameTypeThrowFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testNestedSwitchFinallyLoopFlow() {
		getClassNode(NestedSwitchFinallyLoopFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testSwitchFallThroughCatchFlow() {
		getClassNode(SwitchFallThroughCatchFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testNestedLoopSwitchCatchFinallyFlow() {
		getClassNode(NestedLoopSwitchCatchFinallyFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testSideEffectGuardCatchFinallyFlow() {
		getClassNode(SideEffectGuardCatchFinallyFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testCatchHierarchyFinallyLoopFlow() {
		getClassNode(CatchHierarchyFinallyLoopFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testNestedHandlerLoopExitFlow() {
		getClassNode(NestedHandlerLoopExitFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testWhileCatchSharedTailFlow() {
		getClassNode(WhileCatchSharedTailFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testNestedFinallySharedTailFlow() {
		getClassNode(NestedFinallySharedTailFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testCatchFinallyExpressionTailFlow() {
		getClassNode(CatchFinallyExpressionTailFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testNestedFinallyConditionalTailFlow() {
		getClassNode(NestedFinallyConditionalTailFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testNestedFinallyExceptionalStateFlow() {
		getClassNode(NestedFinallyExceptionalStateFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testSynchronizedAbruptExitFlow() {
		getClassNode(SynchronizedAbruptExitFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.JAVA8 })
	public void testNestedFinallyRethrowStateFlow() {
		getClassNode(NestedFinallyRethrowStateFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testFinallyLabeledSwitchExitFlow() {
		getClassNode(FinallyLabeledSwitchExitFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testSynchronizedReturnSnapshotFlow() {
		getClassNode(SynchronizedReturnSnapshotFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testResourceLabeledExitFlow() {
		getClassNode(ResourceLabeledExitFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testMultiResourceAbruptFlow() {
		getClassNode(MultiResourceAbruptFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testNestedMonitorSwitchFlow() {
		getClassNode(NestedMonitorSwitchFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testDoWhileMonitorFinallyFlow() {
		getClassNode(DoWhileMonitorFinallyFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testNestedCatchReturnMutationFlow() {
		getClassNode(NestedCatchReturnMutationFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testGenericInnerConstructionFlow() {
		getClassNode(GenericInnerConstructionFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testCompoundArrayEvaluationFlow() {
		getClassNode(CompoundArrayEvaluationFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testEnhancedForFinallyFlow() {
		getClassNode(EnhancedForFinallyFlow.class);
	}

	@TestWithProfiles({ TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testDefaultMethodLambdaBridgeFlow() {
		getClassNode(DefaultMethodLambdaBridgeFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testBooleanAssignmentOrderFlow() {
		getClassNode(BooleanAssignmentOrderFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testArrayAliasAssignmentFlow() {
		getClassNode(ArrayAliasAssignmentFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testConstructorArgumentOrderFlow() {
		getClassNode(ConstructorArgumentOrderFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testEnumNullSwitchFlow() {
		getClassNode(EnumNullSwitchFlow.class);
	}

}
