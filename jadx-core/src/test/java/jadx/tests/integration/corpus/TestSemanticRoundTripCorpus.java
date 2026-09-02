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

	@TestWithProfiles({ TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testExceptionPhiFlow() {
		getClassNode(ExceptionPhiFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testExpressionMatrixFlow() {
		getClassNode(ExpressionMatrixFlow.class);
	}
}
