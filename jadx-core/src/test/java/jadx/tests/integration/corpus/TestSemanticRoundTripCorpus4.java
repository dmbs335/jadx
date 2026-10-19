package jadx.tests.integration.corpus;

import jadx.tests.api.IntegrationTest;
import jadx.tests.api.extensions.profiles.TestProfile;
import jadx.tests.api.extensions.profiles.TestWithProfiles;

/** Semantic round-trip cases focused on abrupt completion, resource cleanup and late checks. */
public class TestSemanticRoundTripCorpus4 extends IntegrationTest {

	public static class TryWithResourcesSuppressionFlow {
		private int trace;

		private final class Resource implements AutoCloseable {
			private final int marker;

			private Resource(int marker) {
				this.marker = marker;
				trace = trace * 10 + marker;
			}

			@Override
			public void close() throws Exception {
				trace = trace * 10 + marker;
				throw new Exception("close-" + marker);
			}
		}

		private int run(boolean bodyFailure) {
			trace = 0;
			try (Resource first = new Resource(1); Resource second = new Resource(2)) {
				trace = trace * 10 + 3;
				if (bodyFailure) {
					throw new Exception("body");
				}
				return trace;
			} catch (Exception e) {
				int kind = e.getMessage().equals("body") ? 10 : 20;
				return trace * 100 + kind + e.getSuppressed().length;
			}
		}

		public void check() {
			if (run(false) != 1232121 || run(true) != 1232112) {
				throw new AssertionError("resource suppression");
			}
		}
	}

	public static class CompoundArrayNarrowingFlow {
		private int trace;

		private int index() {
			trace = trace * 10 + 1;
			return 0;
		}

		private int value(boolean fail) {
			trace = trace * 10 + 2;
			if (fail) {
				throw new IllegalStateException("rhs");
			}
			return 130;
		}

		private int run(boolean fail) {
			byte[] values = { 120 };
			trace = 0;
			try {
				values[index()] += value(fail);
				return trace * 1000 + values[0];
			} catch (IllegalStateException e) {
				return -(trace * 1000 + values[0]);
			}
		}

		public void check() {
			if (run(false) != 11994 || run(true) != -12120) {
				throw new AssertionError("compound array narrowing");
			}
		}
	}

	@SuppressWarnings("checkstyle:FallThrough")
	public static class LabeledStringSwitchFlow {
		private int run(String[] values) {
			int score = 0;
			outer: for (int i = 0; i < values.length; i++) {
				switch (values[i]) {
					case "FB":
						score += 10;
						if (i == 0) {
							continue outer;
						}
					case "Ea":
						score += 20;
						break;
					case "stop":
						break outer;
					default:
						score += i;
				}
				score += 100;
			}
			return score;
		}

		public void check() {
			if (run(new String[] { "FB", "Ea" }) != 130
					|| run(new String[] { "Ea", "x" }) != 221
					|| run(new String[] { "x", "stop", "FB" }) != 100) {
				throw new AssertionError("labeled string switch");
			}
		}
	}

	public static class FinallyAbruptCompletionFlow {
		private int trace;

		private int run(int mode) {
			trace = 0;
			try {
				trace = 1;
				if (mode == 0) {
					return trace;
				}
				if (mode == 1) {
					throw new IllegalArgumentException("mode");
				}
				return 3;
			} catch (IllegalArgumentException e) {
				trace = trace * 10 + 2;
				return trace;
			} finally {
				trace = trace * 10 + 4;
				if (mode == 2) {
					return trace;
				}
			}
		}

		public void check() {
			int first = run(0);
			if (first != 1 || trace != 14) {
				throw new AssertionError("finally return: " + first + '/' + trace);
			}
			int second = run(1);
			if (second != 12 || trace != 124) {
				throw new AssertionError("finally catch: " + second + '/' + trace);
			}
			int third = run(2);
			if (third != 14 || trace != 14) {
				throw new AssertionError("finally override: " + third + '/' + trace);
			}
		}
	}

	public static class MultiArrayDimensionOrderFlow {
		private int trace;

		private int dimension(int marker, int value) {
			trace = trace * 10 + marker;
			return value;
		}

		private int run(int rows, int columns) {
			trace = 0;
			try {
				int[][] values = new int[dimension(1, rows)][dimension(2, columns)];
				return trace * 100 + values.length * 10 + values[0].length;
			} catch (NegativeArraySizeException e) {
				return -trace;
			}
		}

		public void check() {
			if (run(2, 3) != 1223 || run(-1, 3) != -12 || run(2, -1) != -12) {
				throw new AssertionError("multi-array dimension order");
			}
		}
	}

	public static class NullReceiverInvocationOrderFlow {
		private static final class Target {
			private final NullReceiverInvocationOrderFlow owner;

			private Target(NullReceiverInvocationOrderFlow owner) {
				this.owner = owner;
			}

			private void accept(int value) {
				owner.trace = owner.trace * 10 + value;
			}
		}

		private int trace;

		private Target receiver(boolean missing) {
			trace = trace * 10 + 1;
			return missing ? null : new Target(this);
		}

		private int argument() {
			trace = trace * 10 + 2;
			return 3;
		}

		private int run(boolean missing) {
			trace = 0;
			try {
				receiver(missing).accept(argument());
				return trace;
			} catch (NullPointerException e) {
				return -trace;
			}
		}

		public void check() {
			if (run(false) != 123 || run(true) != -12) {
				throw new AssertionError("null receiver invocation order");
			}
		}
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testTryWithResourcesSuppressionFlow() {
		getClassNode(TryWithResourcesSuppressionFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testCompoundArrayNarrowingFlow() {
		getClassNode(CompoundArrayNarrowingFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testLabeledStringSwitchFlow() {
		getClassNode(LabeledStringSwitchFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testFinallyAbruptCompletionFlow() {
		getClassNode(FinallyAbruptCompletionFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testMultiArrayDimensionOrderFlow() {
		getClassNode(MultiArrayDimensionOrderFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testNullReceiverInvocationOrderFlow() {
		getClassNode(NullReceiverInvocationOrderFlow.class);
	}
}
