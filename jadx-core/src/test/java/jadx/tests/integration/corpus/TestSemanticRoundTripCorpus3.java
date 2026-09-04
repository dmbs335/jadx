package jadx.tests.integration.corpus;

import jadx.tests.api.IntegrationTest;
import jadx.tests.api.extensions.profiles.TestProfile;
import jadx.tests.api.extensions.profiles.TestWithProfiles;

/** Semantic round-trip cases focused on VM edge semantics and evaluation order. */
public class TestSemanticRoundTripCorpus3 extends IntegrationTest {

	public static class FloatingComparisonFlow {
		private int classify(double value) {
			int result = 0;
			if (value < 0.0d) {
				result |= 1;
			}
			if (value > 0.0d) {
				result |= 2;
			}
			if (value == value) {
				result |= 4;
			}
			if (Double.doubleToRawLongBits(value) == Long.MIN_VALUE) {
				result |= 8;
			}
			return result;
		}

		public void check() {
			if (classify(Double.NaN) != 0
					|| classify(-0.0d) != 12
					|| classify(0.0d) != 4
					|| classify(-2.0d) != 5
					|| classify(2.0d) != 6) {
				throw new AssertionError("floating comparisons");
			}
		}
	}

	public static class ArrayStoreEvaluationFlow {
		private int trace;

		private int index() {
			trace = trace * 10 + 1;
			return 0;
		}

		private Object value(boolean invalid) {
			trace = trace * 10 + 2;
			return invalid ? "bad" : Integer.valueOf(7);
		}

		private int run(boolean invalid) {
			Number[] numbers = { Integer.valueOf(3) };
			Object[] alias = numbers;
			trace = 0;
			try {
				alias[index()] = value(invalid);
				return trace * 10 + numbers[0].intValue();
			} catch (ArrayStoreException e) {
				return trace * 10 + numbers[0].intValue();
			}
		}

		public void check() {
			if (run(false) != 127 || run(true) != 123) {
				throw new AssertionError("array store evaluation");
			}
		}
	}

	public static class UnboxingIncrementFlow {
		private int trace;

		private int index() {
			trace = trace * 10 + 1;
			return 0;
		}

		private int run(Integer initial) {
			Integer[] values = { initial };
			trace = 0;
			try {
				int previous = values[index()]++;
				return previous * 100 + values[0] * 10 + trace;
			} catch (NullPointerException e) {
				return -trace;
			}
		}

		public void check() {
			if (run(Integer.valueOf(5)) != 561 || run(null) != -1) {
				throw new AssertionError("unboxing increment");
			}
		}
	}

	public static class BooleanCarrierCascadeFlow {
		private long trace;

		private boolean step(int marker, boolean value) {
			trace = trace * 10 + marker;
			return value;
		}

		private String run(int mask) {
			trace = 0;
			boolean selected = step(1, (mask & 1) != 0) && step(2, (mask & 2) != 0)
					|| step(3, (mask & 4) != 0)
							&& (step(4, (mask & 8) != 0) || step(5, (mask & 16) != 0));
			return selected + ":" + trace;
		}

		public void check() {
			if (!run(0).equals("false:13")
					|| !run(1).equals("false:123")
					|| !run(3).equals("true:12")
					|| !run(4).equals("false:1345")
					|| !run(20).equals("true:1345")
					|| !run(12).equals("true:134")) {
				throw new AssertionError("boolean carrier cascade");
			}
		}
	}

	public static class VarargsEvaluationFlow {
		private int trace;

		private int mark(int marker, int value) {
			trace = trace * 10 + marker;
			return value;
		}

		private int sum(int base, int... values) {
			int result = base;
			for (int value : values) {
				result = result * 10 + value;
			}
			return result;
		}

		private int run() {
			trace = 0;
			int result = sum(mark(1, 4), mark(2, 5), mark(3, 6), mark(4, 7));
			return result * 10000 + trace;
		}

		public void check() {
			if (run() != 45671234) {
				throw new AssertionError("varargs evaluation");
			}
		}
	}

	public static class MonitorExpressionFlow {
		private final Object first = new Object();
		private final Object second = new Object();
		private Object active;
		private int trace;

		private Object monitor() {
			trace = trace * 10 + 1;
			Object current = active;
			active = second;
			return current;
		}

		private int run(boolean fail) {
			active = first;
			trace = 0;
			try {
				synchronized (monitor()) {
					trace = trace * 10 + 2;
					if (fail) {
						throw new IllegalStateException("fail");
					}
					return trace;
				}
			} catch (IllegalStateException e) {
				trace = trace * 10 + 3;
				return -trace;
			}
		}

		public void check() {
			if (run(false) != 12 || active != second || run(true) != -123 || active != second) {
				throw new AssertionError("monitor expression");
			}
		}
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testFloatingComparisonFlow() {
		getClassNode(FloatingComparisonFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testArrayStoreEvaluationFlow() {
		getClassNode(ArrayStoreEvaluationFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testUnboxingIncrementFlow() {
		getClassNode(UnboxingIncrementFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testBooleanCarrierCascadeFlow() {
		getClassNode(BooleanCarrierCascadeFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testVarargsEvaluationFlow() {
		getClassNode(VarargsEvaluationFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testMonitorExpressionFlow() {
		getClassNode(MonitorExpressionFlow.class);
	}
}
