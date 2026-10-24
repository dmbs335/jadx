package jadx.tests.integration.corpus;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import jadx.tests.api.IntegrationTest;
import jadx.tests.api.extensions.profiles.TestProfile;
import jadx.tests.api.extensions.profiles.TestWithProfiles;

/** Additional receiver-alias and resource-exit execution-order round trips. */
public class TestSemanticRoundTripCorpus11 extends IntegrationTest {

	public static class CompoundFieldReceiverAliasFlow {
		private static class Box {
			int value;
		}

		private Box current;
		private int trace;

		private Box receiver() {
			trace = trace * 10 + 1;
			return current;
		}

		private int rhs() {
			trace = trace * 10 + 2;
			current.value = 50;
			current = new Box();
			current.value = 7;
			return 4;
		}

		private int run() {
			current = new Box();
			current.value = 5;
			Box original = current;
			trace = 0;
			receiver().value += rhs();
			return original.value * 100 + current.value;
		}

		public void check() {
			int value = run();
			if (value != 907 || trace != 12) {
				throw new AssertionError("compound field receiver alias: " + value + '/' + trace);
			}
		}
	}

	public static class ResourceCloseOverridesReturnFlow {
		private int trace;

		private final class Resource implements AutoCloseable {
			@Override
			public void close() {
				trace = trace * 10 + 2;
				throw new IllegalStateException("close");
			}
		}

		private int run() {
			trace = 0;
			try (Resource resource = new Resource()) {
				trace = trace * 10 + 1;
				return 10;
			} catch (IllegalStateException e) {
				trace = trace * 10 + 3;
				return trace;
			} finally {
				trace = trace * 10 + 4;
			}
		}

		public void check() {
			int value = run();
			if (value != 123 || trace != 1234) {
				throw new AssertionError("resource close overrides return: " + value + '/' + trace);
			}
		}
	}

	public static class SecondResourceInitFailureFlow {
		private StringBuilder trace;

		private final class Resource implements AutoCloseable {
			private final int id;

			private Resource(int id, boolean fail) {
				this.id = id;
				trace.append('I').append(id);
				if (fail) {
					throw new IllegalStateException("init-" + id);
				}
			}

			@Override
			public void close() {
				trace.append('C').append(id);
				throw new IllegalStateException("close-" + id);
			}
		}

		private String run(boolean failSecond) {
			trace = new StringBuilder();
			try (Resource first = new Resource(1, false); Resource second = new Resource(2, failSecond)) {
				trace.append('B');
			} catch (IllegalStateException e) {
				return trace + "|" + e.getMessage() + "|" + e.getSuppressed().length;
			}
			throw new AssertionError("resource close must throw");
		}

		public void check() {
			String success = run(false);
			String initFailure = run(true);
			if (!"I1I2BC2C1|close-2|1".equals(success)
					|| !"I1I2C1|init-2|1".equals(initFailure)) {
				throw new AssertionError("second resource init failure: " + success + '/' + initFailure);
			}
		}
	}

	public static class AssertionEvaluationFlow {
		private int trace;

		private boolean condition() {
			trace = trace * 10 + 1;
			return false;
		}

		private String message() {
			trace = trace * 10 + 2;
			return "failure";
		}

		private String run() {
			trace = 0;
			try {
				assert condition() : message();
				return "ok:" + trace;
			} catch (AssertionError e) {
				return e.getMessage() + ':' + trace;
			}
		}

		public void check() {
			String expected = getClass().desiredAssertionStatus() ? "failure:12" : "ok:0";
			String actual = run();
			if (!expected.equals(actual)) {
				throw new AssertionError("assert evaluation order: " + actual);
			}
		}
	}

	public static class MethodHandleExactFlow {
		private static int addThree(int value) {
			return value + 3;
		}

		private int run() throws Throwable {
			MethodHandle handle = MethodHandles.lookup().findStatic(
					MethodHandleExactFlow.class, "addThree", MethodType.methodType(int.class, int.class));
			return (int) handle.invokeExact(4);
		}

		public void check() throws Throwable {
			if (run() != 7) {
				throw new AssertionError("signature-polymorphic invokeExact");
			}
		}
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testCompoundFieldReceiverAliasFlow() {
		getClassNode(CompoundFieldReceiverAliasFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testResourceCloseOverridesReturnFlow() {
		getClassNode(ResourceCloseOverridesReturnFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testSecondResourceInitFailureFlow() {
		getClassNode(SecondResourceInitFailureFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testAssertionEvaluationFlow() {
		getClassNode(AssertionEvaluationFlow.class);
	}

	@TestWithProfiles({ TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testMethodHandleExactFlow() {
		getClassNode(MethodHandleExactFlow.class);
	}
}
