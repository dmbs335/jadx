package jadx.tests.integration.corpus;

import jadx.tests.api.IntegrationTest;
import jadx.tests.api.extensions.profiles.TestProfile;
import jadx.tests.api.extensions.profiles.TestWithProfiles;

/** Semantic round-trip cases focused on unboxing, initialization and discarded expressions. */
public class TestSemanticRoundTripCorpus8 extends IntegrationTest {

	public static class ConditionalUnboxingFlow {
		private int trace;

		private int primitive() {
			trace = trace * 10 + 2;
			return 7;
		}

		private int run(boolean selectNull) {
			trace = 1;
			Integer boxed = null;
			try {
				Integer result = selectNull ? boxed : primitive();
				return trace * 10 + result;
			} catch (NullPointerException e) {
				return -trace;
			}
		}

		public void check() {
			if (run(true) != -1 || run(false) != 127) {
				throw new AssertionError("conditional unboxing");
			}
		}
	}

	public static class StaticFieldQualifierFlow {
		private static final class Target {
			private static int value = 7;
		}

		private int trace;

		private Target receiver(boolean missing) {
			trace = trace * 10 + 1;
			return missing ? null : new Target();
		}

		@SuppressWarnings("static-access")
		private int run(boolean missing) {
			trace = 0;
			int value = receiver(missing).value;
			return trace * 10 + value;
		}

		public void check() {
			if (run(false) != 17 || run(true) != 17) {
				throw new AssertionError("static field qualifier evaluation");
			}
		}
	}

	public static class SynchronizedMonitorFlow {
		private int trace;

		private Object monitor(boolean missing) {
			trace = trace * 10 + 1;
			return missing ? null : this;
		}

		private int run(boolean missing) {
			trace = 0;
			try {
				synchronized (monitor(missing)) {
					trace = trace * 10 + 2;
				}
				return trace;
			} catch (NullPointerException e) {
				return -trace;
			}
		}

		public void check() {
			if (run(false) != 12 || run(true) != -1) {
				throw new AssertionError("synchronized null monitor");
			}
		}

	}

	public static class ConditionalOverloadFlow {
		private int select(Object value) {
			return 1;
		}

		private int select(String value) {
			return 2;
		}

		private int run(boolean missing) {
			return select(missing ? null : "text");
		}

		public void check() {
			if (run(false) != 2 || run(true) != 2) {
				throw new AssertionError("conditional overload selection");
			}
		}
	}

	public static class InterfaceInitializationFlow {
		private static int events;

		private static int mark(int marker) {
			events = events * 10 + marker;
			return marker;
		}

		private interface Parent {
			int VALUE = mark(1);
		}

		private interface Child extends Parent {
			int VALUE = mark(2);
		}

		private static final class Implementation implements Child {
			private static int value = mark(3);
		}

		private int run() {
			events = 0;
			int implementation = Implementation.value;
			int child = Child.VALUE;
			int parent = Parent.VALUE;
			return events * 1000 + implementation * 100 + child * 10 + parent;
		}

		public void check() {
			if (run() != 321321) {
				throw new AssertionError("interface initialization order");
			}
		}
	}

	public static class StringConcatNullFlow {
		private int trace;

		private String prefix() {
			trace = trace * 10 + 1;
			return "p";
		}

		private Object value() {
			trace = trace * 10 + 2;
			return null;
		}

		private String suffix() {
			trace = trace * 10 + 3;
			return "s";
		}

		private String run() {
			trace = 0;
			String result = prefix() + value() + suffix();
			return result + ':' + trace;
		}

		public void check() {
			if (!run().equals("pnulls:123")) {
				throw new AssertionError("string concat null evaluation");
			}
		}
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testConditionalUnboxingFlow() {
		getClassNode(ConditionalUnboxingFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testStaticFieldQualifierFlow() {
		getClassNode(StaticFieldQualifierFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testSynchronizedMonitorFlow() {
		getClassNode(SynchronizedMonitorFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testConditionalOverloadFlow() {
		getClassNode(ConditionalOverloadFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testInterfaceInitializationFlow() {
		getClassNode(InterfaceInitializationFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testStringConcatNullFlow() {
		getClassNode(StringConcatNullFlow.class);
	}
}
