package jadx.tests.integration.corpus;

import java.util.function.IntSupplier;

import jadx.tests.api.IntegrationTest;
import jadx.tests.api.extensions.profiles.TestProfile;
import jadx.tests.api.extensions.profiles.TestWithProfiles;

/** Regression probes for eager bound-method-reference evaluation and exception boundaries. */
public class TestSemanticRoundTripCorpus9 extends IntegrationTest {

	public static class BoundReferenceReceiverFlow {
		private int trace;

		private static final class Receiver {
			private int value() {
				return 7;
			}
		}

		private Receiver receiver(boolean missing) {
			trace = trace * 10 + 1;
			return missing ? null : new Receiver();
		}

		private int run(boolean missing) {
			trace = 0;
			try {
				IntSupplier supplier = receiver(missing)::value;
				trace = trace * 10 + 2;
				return trace * 10 + supplier.getAsInt();
			} catch (NullPointerException e) {
				return -trace;
			}
		}

		public void check() {
			if (run(false) != 127 || run(true) != -1) {
				throw new AssertionError("bound receiver must be evaluated at reference creation");
			}
		}
	}

	public static class BoundReferenceArgumentOrderFlow {
		private int trace;

		private static final class Receiver {
			private int value() {
				return 3;
			}
		}

		private Receiver receiver() {
			trace = trace * 10 + 1;
			return new Receiver();
		}

		private int consume(IntSupplier supplier, int argument) {
			trace = trace * 10 + 3;
			return trace * 100 + argument * 10 + supplier.getAsInt();
		}

		private int argument() {
			trace = trace * 10 + 2;
			return 4;
		}

		private int run() {
			trace = 0;
			return consume(receiver()::value, argument());
		}

		public void check() {
			if (run() != 12343) {
				throw new AssertionError("bound reference argument evaluation order");
			}
		}
	}

	public static class ArrayAssignmentNullOrderFlow {
		private int trace;

		private int index() {
			trace = trace * 10 + 1;
			return 0;
		}

		private int value() {
			trace = trace * 10 + 2;
			return 7;
		}

		private int run(boolean missing) {
			trace = 0;
			int[] values = missing ? null : new int[1];
			try {
				values[index()] = value();
				return trace * 10 + values[0];
			} catch (NullPointerException e) {
				return -trace;
			}
		}

		public void check() {
			if (run(false) != 127 || run(true) != -12) {
				throw new AssertionError("array assignment null-check after RHS evaluation");
			}
		}
	}

	public static class CompoundArrayAssignmentNullOrderFlow {
		private int trace;

		private int index() {
			trace = trace * 10 + 1;
			return 0;
		}

		private int value() {
			trace = trace * 10 + 2;
			return 7;
		}

		private int run(boolean missing) {
			trace = 0;
			int[] values = missing ? null : new int[] { 5 };
			try {
				values[index()] += value();
				return trace * 100 + values[0];
			} catch (NullPointerException e) {
				return -trace;
			}
		}

		public void check() {
			if (run(false) != 1212 || run(true) != -1) {
				throw new AssertionError("compound array assignment null-check before RHS evaluation");
			}
		}
	}

	public static class TryWithResourcesSuppressionFlow {
		private static final class Resource implements AutoCloseable {
			private final int[] trace;
			private final boolean failOnClose;

			private Resource(int[] trace, boolean failOnClose) {
				this.trace = trace;
				this.failOnClose = failOnClose;
			}

			@Override
			public void close() {
				trace[0] = trace[0] * 10 + 3;
				if (failOnClose) {
					throw new IllegalStateException("close");
				}
			}
		}

		private int run(boolean bodyFails, boolean closeFails) {
			int[] trace = { 0 };
			try (Resource ignored = new Resource(trace, closeFails)) {
				trace[0] = 1;
				if (bodyFails) {
					throw new IllegalStateException("body");
				}
				trace[0] = 2;
			} catch (IllegalStateException e) {
				int primary = "body".equals(e.getMessage()) ? 10 : 20;
				return trace[0] * 100 + primary + e.getSuppressed().length;
			}
			return trace[0] * 100;
		}

		public void check() {
			if (run(false, false) != 2300 || run(true, false) != 1310
					|| run(false, true) != 2320 || run(true, true) != 1311) {
				throw new AssertionError("try-with-resources primary and suppressed exception order");
			}
		}
	}

	public static class StringSwitchHashCollisionFlow {
		private int run(String input) {
			try {
				switch (input) {
					case "Aa":
						return 11;
					case "BB":
						return 22;
					case "Ea":
						return 33;
					case "FB":
						return 44;
					default:
						return 55;
				}
			} catch (NullPointerException e) {
				return -1;
			}
		}

		public void check() {
			if (run("Aa") != 11 || run("BB") != 22 || run("Ea") != 33
					|| run("FB") != 44 || run("missing") != 55 || run(null) != -1) {
				throw new AssertionError("colliding string-switch cases and null branch");
			}
		}
	}

	public static class VarargsNullArrayDistinctionFlow {
		private int count(Object... values) {
			if (values == null) {
				return -1;
			}
			return values.length * 10 + (values.length != 0 && values[0] == null ? 1 : 0);
		}

		public void check() {
			if (count((Object[]) null) != -1 || count((Object) null) != 11
					|| count(new Object[] { null }) != 11
					|| count((Object[]) new String[] { null }) != 11
					|| count((Object) new String[] { "x" }) != 10) {
				throw new AssertionError("varargs null-array versus one null element");
			}
		}
	}

	public static class BooleanEvaluationOrderFlow {
		private int trace;

		private boolean leftFalse() {
			trace = trace * 10 + 1;
			return false;
		}

		private boolean rightTrue() {
			trace = trace * 10 + 2;
			return true;
		}

		private boolean leftTrue() {
			trace = trace * 10 + 3;
			return true;
		}

		private boolean rightFalse() {
			trace = trace * 10 + 4;
			return false;
		}

		private int eager() {
			trace = 0;
			boolean and = leftFalse() & rightTrue();
			boolean or = leftTrue() | rightFalse();
			return trace * 10 + (and ? 1 : 0) + (or ? 2 : 0);
		}

		private int shortCircuit() {
			trace = 0;
			boolean and = leftFalse() && rightTrue();
			boolean or = leftTrue() || rightFalse();
			return trace * 10 + (and ? 1 : 0) + (or ? 2 : 0);
		}

		public void check() {
			if (eager() != 12342 || shortCircuit() != 132) {
				throw new AssertionError("eager versus short-circuit boolean evaluation");
			}
		}
	}

	public static class CallBeforeFieldReadFlow {
		private int state;

		private int update() {
			state = 7;
			return 3;
		}

		private int run() {
			state = 1;
			int value = update();
			return state * 10 + value;
		}

		public void check() {
			if (run() != 73) {
				throw new AssertionError("call must precede field read");
			}
		}
	}

	public static class BooleanCallBeforeFieldReadFlow {
		private int state;

		private boolean update() {
			state = 7;
			return true;
		}

		private int run() {
			state = 1;
			boolean value = update();
			return state * 10 + (value ? 3 : 0);
		}

		public void check() {
			if (run() != 73) {
				throw new AssertionError("boolean call must precede field read");
			}
		}
	}

	public static class EagerCallBeforeLaterCallFlow {
		private int trace;

		private boolean step(int marker, boolean result) {
			trace = trace * 10 + marker;
			return result;
		}

		private int last() {
			trace = trace * 10 + 5;
			return trace * 10;
		}

		private int run() {
			trace = 0;
			boolean and = step(1, false) & step(2, true);
			boolean or = step(3, true) | step(4, false);
			return last() + (and ? 1 : 0) + (or ? 2 : 0);
		}

		public void check() {
			if (run() != 123452) {
				throw new AssertionError("eager calls must precede later call");
			}
		}
	}

	public static class ArithmeticCallsBeforeFieldReadFlow {
		private int trace;

		private int step(int marker) {
			trace = trace * 10 + marker;
			return marker;
		}

		private int run() {
			trace = 0;
			int sum = step(1) + step(2);
			return trace * 10 + sum;
		}

		private int last() {
			trace = trace * 10 + 3;
			return trace * 10;
		}

		private int runCall() {
			trace = 0;
			int sum = step(1) + step(2);
			return last() + sum;
		}

		public void check() {
			if (run() != 123 || runCall() != 1233) {
				throw new AssertionError("arithmetic calls must precede field read");
			}
		}
	}

	public static class EagerCallsBeforeArrayReadFlow {
		private final int[] state = new int[1];

		private boolean step(int marker, boolean result) {
			state[0] = state[0] * 10 + marker;
			return result;
		}

		private int run() {
			state[0] = 0;
			boolean and = step(1, false) & step(2, true);
			boolean or = step(3, true) | step(4, false);
			return state[0] * 10 + (and ? 1 : 0) + (or ? 2 : 0);
		}

		public void check() {
			if (run() != 12342) {
				throw new AssertionError("eager calls must precede array read");
			}
		}
	}

	public static class EagerCallsBeforeLocalArrayReadFlow {
		private static boolean step(int[] state, int marker, boolean result) {
			state[0] = state[0] * 10 + marker;
			return result;
		}

		private int run() {
			int[] state = new int[1];
			boolean and = step(state, 1, false) & step(state, 2, true);
			boolean or = step(state, 3, true) | step(state, 4, false);
			return state[0] * 10 + (and ? 1 : 0) + (or ? 2 : 0);
		}

		public void check() {
			if (run() != 12342) {
				throw new AssertionError("eager calls must precede local array read");
			}
		}
	}

	public static class EagerCallsBeforeDivisionFlow {
		private int trace;

		private boolean step(int marker, boolean result) {
			trace = trace * 10 + marker;
			return result;
		}

		private int run(int divisor) {
			trace = 0;
			boolean and = step(1, false) & step(2, true);
			boolean or = step(3, true) | step(4, false);
			return (10 / divisor) + (and ? 1 : 0) + (or ? 2 : 0);
		}

		public void check() {
			try {
				run(0);
				throw new AssertionError("division should fail");
			} catch (ArithmeticException expected) {
				if (trace != 1234) {
					throw new AssertionError("eager calls must precede division");
				}
			}
		}
	}

	public static class EagerCallsBeforeCastFlow {
		private int trace;

		private boolean step(int marker, boolean result) {
			trace = trace * 10 + marker;
			return result;
		}

		private int run(Object value) {
			trace = 0;
			boolean and = step(1, false) & step(2, true);
			boolean or = step(3, true) | step(4, false);
			String casted = (String) value;
			return (casted == null ? 0 : 10) + (and ? 1 : 0) + (or ? 2 : 0);
		}

		public void check() {
			try {
				run(Integer.valueOf(5));
				throw new AssertionError("cast should fail");
			} catch (ClassCastException expected) {
				if (trace != 1234) {
					throw new AssertionError("eager calls must precede cast");
				}
			}
		}
	}

	public static class PlainCallBeforeCastFlow {
		private int trace;

		private int step() {
			trace = 1;
			return 2;
		}

		private int run(Object value) {
			trace = 0;
			int result = step();
			String casted = (String) value;
			return (casted == null ? 0 : 10) + result;
		}

		public void check() {
			try {
				run(Integer.valueOf(5));
				throw new AssertionError("cast should fail");
			} catch (ClassCastException expected) {
				if (trace != 1) {
					throw new AssertionError("call must precede cast");
				}
			}
		}
	}

	public static class PlainCallBeforeNewArrayFlow {
		private int trace;

		private int step() {
			trace = 1;
			return 2;
		}

		private int run(int size) {
			trace = 0;
			int result = step();
			int[] array = new int[size];
			return array.length + result;
		}

		public void check() {
			try {
				run(-1);
				throw new AssertionError("array creation should fail");
			} catch (NegativeArraySizeException expected) {
				if (trace != 1) {
					throw new AssertionError("call must precede array creation");
				}
			}
		}
	}

	public static class EagerCallsBeforeNewArrayFlow {
		private int trace;

		private boolean step(int marker, boolean result) {
			trace = trace * 10 + marker;
			return result;
		}

		private int run(int size) {
			trace = 0;
			boolean and = step(1, false) & step(2, true);
			boolean or = step(3, true) | step(4, false);
			int[] array = new int[size];
			return array.length + (and ? 1 : 0) + (or ? 2 : 0);
		}

		public void check() {
			try {
				run(-1);
				throw new AssertionError("array creation should fail");
			} catch (NegativeArraySizeException expected) {
				if (trace != 1234) {
					throw new AssertionError("eager calls must precede array creation");
				}
			}
		}
	}

	public static class EagerCallsBeforeConstructorFlow {
		private int trace;

		private static class ThrowingBox {
			int value;

			ThrowingBox() {
				throw new IllegalStateException("box");
			}
		}

		private boolean step(int marker, boolean result) {
			trace = trace * 10 + marker;
			return result;
		}

		private int run() {
			trace = 0;
			boolean and = step(1, false) & step(2, true);
			boolean or = step(3, true) | step(4, false);
			return new ThrowingBox().value + (and ? 1 : 0) + (or ? 2 : 0);
		}

		public void check() {
			try {
				run();
				throw new AssertionError("constructor should fail");
			} catch (IllegalStateException expected) {
				if (trace != 1234) {
					throw new AssertionError("eager calls must precede constructor");
				}
			}
		}
	}

	public static class StringConcatBeforeCallFlow {
		private int trace;

		private final class Marker {
			@Override
			public String toString() {
				trace = trace * 10 + 1;
				return "marker";
			}
		}

		private int step() {
			trace = trace * 10 + 2;
			return 7;
		}

		private String run() {
			trace = 0;
			String joined = "value=" + new Marker();
			int result = step();
			return joined + result;
		}

		public void check() {
			if (!"value=marker7".equals(run()) || trace != 12) {
				throw new AssertionError("string conversion must precede later call");
			}
		}
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testBoundReferenceReceiverFlow() {
		getClassNode(BoundReferenceReceiverFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testBoundReferenceArgumentOrderFlow() {
		getClassNode(BoundReferenceArgumentOrderFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testArrayAssignmentNullOrderFlow() {
		getClassNode(ArrayAssignmentNullOrderFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testCompoundArrayAssignmentNullOrderFlow() {
		getClassNode(CompoundArrayAssignmentNullOrderFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testTryWithResourcesSuppressionFlow() {
		getClassNode(TryWithResourcesSuppressionFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testStringSwitchHashCollisionFlow() {
		getClassNode(StringSwitchHashCollisionFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testVarargsNullArrayDistinctionFlow() {
		getClassNode(VarargsNullArrayDistinctionFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testBooleanEvaluationOrderFlow() {
		getClassNode(BooleanEvaluationOrderFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testCallBeforeFieldReadFlow() {
		getClassNode(CallBeforeFieldReadFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testBooleanCallBeforeFieldReadFlow() {
		getClassNode(BooleanCallBeforeFieldReadFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testEagerCallBeforeLaterCallFlow() {
		getClassNode(EagerCallBeforeLaterCallFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testArithmeticCallsBeforeFieldReadFlow() {
		getClassNode(ArithmeticCallsBeforeFieldReadFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testEagerCallsBeforeArrayReadFlow() {
		getClassNode(EagerCallsBeforeArrayReadFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testEagerCallsBeforeLocalArrayReadFlow() {
		getClassNode(EagerCallsBeforeLocalArrayReadFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testEagerCallsBeforeDivisionFlow() {
		getClassNode(EagerCallsBeforeDivisionFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testEagerCallsBeforeCastFlow() {
		getClassNode(EagerCallsBeforeCastFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testPlainCallBeforeCastFlow() {
		getClassNode(PlainCallBeforeCastFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testPlainCallBeforeNewArrayFlow() {
		getClassNode(PlainCallBeforeNewArrayFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testEagerCallsBeforeNewArrayFlow() {
		getClassNode(EagerCallsBeforeNewArrayFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testEagerCallsBeforeConstructorFlow() {
		getClassNode(EagerCallsBeforeConstructorFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.D8_J11, TestProfile.JAVA8, TestProfile.JAVA11 })
	public void testStringConcatBeforeCallFlow() {
		getClassNode(StringConcatBeforeCallFlow.class);
	}
}
