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

		@SuppressWarnings("checkstyle:InterfaceIsType")
		private interface Parent {
			int VALUE = mark(1);
		}

		@SuppressWarnings("checkstyle:InterfaceIsType")
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

	public static class SwitchSharedCatchFlow {
		private int trace;

		private void step(int value, boolean fail) {
			trace = trace * 10 + value;
			if (fail) {
				throw new IllegalStateException();
			}
		}

		private int run(int branch, boolean failBefore, boolean failProtected, boolean failAfter, boolean failHandler) {
			trace = 0;
			int result = 9;
			try {
				switch (branch) {
					case 0:
						step(1, failBefore);
						try {
							step(2, failProtected);
						} catch (IllegalStateException e) {
							step(3, failHandler);
							break;
						}
						step(4, failAfter);
						result = 0;
						break;
					case 1:
						result = 5;
						step(5, failBefore);
						try {
							step(6, failProtected);
						} catch (IllegalStateException e) {
							step(7, failHandler);
							break;
						}
						step(8, failAfter);
						result = 1;
						break;
					default:
						result = -1;
				}
			} catch (IllegalStateException e) {
				return -trace;
			}
			return result * 10000 + trace;
		}

		public void check() {
			for (int branch = -1; branch < 3; branch++) {
				for (int mask = 0; mask < 16; mask++) {
					int actual = run(branch, (mask & 1) != 0, (mask & 2) != 0,
							(mask & 4) != 0, (mask & 8) != 0);
					int expected;
					if (branch == 0) {
						expected = (mask & 1) != 0 ? -1 : (mask & 2) != 0
								? (mask & 8) != 0 ? -123 : 90123
								: (mask & 4) != 0 ? -124 : 124;
					} else if (branch == 1) {
						expected = (mask & 1) != 0 ? -5 : (mask & 2) != 0
								? (mask & 8) != 0 ? -567 : 50567
								: (mask & 4) != 0 ? -568 : 10568;
					} else {
						expected = -10000;
					}
					if (actual != expected) {
						throw new AssertionError("switch catch scope: " + branch + "/" + mask + " -> " + actual + " != " + expected);
					}
				}
			}
		}
	}

	public static class SynchronizedEarlyReturnCatchFlow {
		private int trace;
		private boolean registered;

		private void schedule(Runnable task, boolean rejected) {
			trace = trace * 10 + (Thread.holdsLock(this) ? 9 : 2);
			if (rejected) {
				throw new java.util.concurrent.RejectedExecutionException();
			}
			task.run();
		}

		private int run(boolean duplicate, boolean rejected) {
			trace = 0;
			synchronized (this) {
				trace = trace * 10 + 1;
				if (duplicate && registered) {
					trace = trace * 10 + 3;
					return trace;
				}
				registered = true;
				trace = trace * 10 + 4;
			}
			try {
				schedule(new Runnable() {
					@Override
					public void run() {
						trace = trace * 10 + 6;
					}
				}, rejected);
			} catch (java.util.concurrent.RejectedExecutionException ignored) {
				trace = trace * 10 + 5;
			}
			return trace;
		}

		public void check() {
			registered = false;
			if (run(false, false) != 1426 || run(true, false) != 13
					|| run(false, true) != 1425) {
				throw new AssertionError("synchronized early return/catch boundary");
			}
		}
	}

	public static class StringSwitchNestedLoopFlow {
		private int run(String action, int outerCount, int innerCount) {
			int result = 0;
			switch (action) {
				case "concat":
					for (int i = 0; i < outerCount; i++) {
						for (int j = 0; j < innerCount; j++) {
							result += 10;
						}
						result++;
					}
					break;
				case "filter":
					for (int i = 0; i < outerCount; i++) {
						result += 2;
					}
					break;
				default:
					result = -1;
			}
			return result;
		}

		public void check() {
			if (run("concat", 3, 2) != 63 || run("filter", 3, 2) != 6
					|| run("missing", 3, 2) != -1) {
				throw new AssertionError("string switch nested loop back edge");
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

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testSwitchSharedCatchFlow() {
		getClassNode(SwitchSharedCatchFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testSynchronizedEarlyReturnCatchFlow() {
		getClassNode(SynchronizedEarlyReturnCatchFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testStringSwitchNestedLoopFlow() {
		getClassNode(StringSwitchNestedLoopFlow.class);
	}
}
