package jadx.tests.integration.corpus;

import jadx.tests.api.IntegrationTest;
import jadx.tests.api.extensions.profiles.TestProfile;
import jadx.tests.api.extensions.profiles.TestWithProfiles;

/** Exception and loop-transfer round trips with observable finally execution order. */
public class TestSemanticRoundTripCorpus10 extends IntegrationTest {

	public static class FinallyContinueFlow {
		private String run() {
			StringBuilder trace = new StringBuilder();
			for (int i = 0; i < 3; i++) {
				try {
					trace.append('A').append(i);
					if (i == 1) {
						continue;
					}
					trace.append('B');
				} finally {
					trace.append('F').append(i);
				}
				trace.append('Z');
			}
			return trace.toString();
		}

		public void check() {
			if (!"A0BF0ZA1F1A2BF2Z".equals(run())) {
				throw new AssertionError("finally before continue");
			}
		}
	}

	public static class CatchContinueFinallyFlow {
		private String run() {
			StringBuilder trace = new StringBuilder();
			for (int i = 0; i < 3; i++) {
				try {
					if (i == 1) {
						throw new IllegalStateException();
					}
					trace.append('A').append(i);
				} catch (IllegalStateException e) {
					trace.append('C').append(i);
					continue;
				} finally {
					trace.append('F').append(i);
				}
				trace.append('Z');
			}
			return trace.toString();
		}

		public void check() {
			if (!"A0F0ZC1F1A2F2Z".equals(run())) {
				throw new AssertionError("catch continuation must execute finally");
			}
		}
	}

	public static class FinallyOverridesReturnFlow {
		private int run(boolean override) {
			try {
				return 10;
			} finally {
				if (override) {
					return 20;
				}
			}
		}

		public void check() {
			if (run(false) != 10 || run(true) != 20) {
				throw new AssertionError("finally return overrides pending return");
			}
		}
	}

	public static class LabeledBreakFinallyFlow {
		private String run() {
			StringBuilder trace = new StringBuilder();
			outer: for (int i = 0; i < 2; i++) {
				try {
					for (int j = 0; j < 2; j++) {
						try {
							trace.append('A').append(i).append(j);
							if (i == 1 && j == 0) {
								break outer;
							}
						} finally {
							trace.append('F').append(i).append(j);
						}
					}
				} finally {
					trace.append('O').append(i);
				}
			}
			return trace.toString();
		}

		public void check() {
			if (!"A00F00A01F01O0A10F10O1".equals(run())) {
				throw new AssertionError("labeled break must execute nested finally blocks");
			}
		}
	}

	public static class FinallyContinueOverridesBreakFlow {
		private String run() {
			StringBuilder trace = new StringBuilder();
			for (int i = 0; i < 3; i++) {
				try {
					trace.append('T').append(i);
					if (i == 1) {
						break;
					}
				} finally {
					trace.append('F').append(i);
					if (i == 1) {
						continue;
					}
				}
				trace.append('Z');
			}
			return trace.toString();
		}

		public void check() {
			if (!"T0F0ZT1F1T2F2Z".equals(run())) {
				throw new AssertionError("finally continue overrides pending break");
			}
		}
	}

	public static class FinallyBreakOverridesContinueFlow {
		private String run() {
			StringBuilder trace = new StringBuilder();
			for (int i = 0; i < 3; i++) {
				try {
					trace.append('T').append(i);
					if (i == 1) {
						continue;
					}
				} finally {
					trace.append('F').append(i);
					if (i == 1) {
						break;
					}
				}
				trace.append('Z');
			}
			return trace.toString();
		}

		public void check() {
			if (!"T0F0ZT1F1".equals(run())) {
				throw new AssertionError("finally break overrides pending continue");
			}
		}
	}

	public static class NestedCatchRethrowFinallyFlow {
		private String run(int kind) {
			StringBuilder trace = new StringBuilder();
			try {
				try {
					trace.append('A');
					if (kind == 1) {
						throw new IllegalArgumentException();
					}
					if (kind == 2) {
						throw new IllegalStateException();
					}
					trace.append('B');
				} catch (IllegalArgumentException e) {
					trace.append('C');
					throw e;
				} finally {
					trace.append('F');
					if (kind == 3) {
						throw new IllegalStateException();
					}
				}
			} catch (RuntimeException e) {
				trace.append('O').append(e instanceof IllegalArgumentException ? 'A' : 'S');
			}
			return trace.toString();
		}

		public void check() {
			if (!"ABF".equals(run(0)) || !"ACFOA".equals(run(1))
					|| !"AFOS".equals(run(2)) || !"ABFOS".equals(run(3))) {
				throw new AssertionError("nested catch rethrow and finally ordering");
			}
		}
	}

	public static class NestedCatchRecoveryFinallyFlow {
		private String run(int kind) {
			StringBuilder trace = new StringBuilder();
			try {
				try {
					trace.append('A');
					if (kind == 1) {
						throw new IllegalArgumentException();
					}
					if (kind == 2) {
						throw new IllegalStateException();
					}
					trace.append('B');
				} catch (IllegalArgumentException e) {
					trace.append('C');
				} finally {
					trace.append('F');
					if (kind == 3) {
						throw new IllegalStateException();
					}
				}
			} catch (RuntimeException e) {
				trace.append('O').append(e instanceof IllegalArgumentException ? 'A' : 'S');
			}
			return trace.toString();
		}

		public void check() {
			String zero = run(0);
			String one = run(1);
			String two = run(2);
			String three = run(3);
			if (!"ABF".equals(zero) || !"ACF".equals(one)
					|| !"AFOS".equals(two) || !"ABFOS".equals(three)) {
				throw new AssertionError("nested catch recovery and finally ordering: "
						+ zero + ", " + one + ", " + two + ", " + three);
			}
		}
	}

	public static class NestedCatchRecoveryVoidFinallyFlow {
		private void run(int kind, StringBuilder trace) {
			try {
				try {
					trace.append('A');
					if (kind == 1) {
						throw new IllegalArgumentException();
					}
					if (kind == 2) {
						throw new IllegalStateException();
					}
					trace.append('B');
				} catch (IllegalArgumentException e) {
					trace.append('C');
				} finally {
					trace.append('F');
					if (kind == 3) {
						throw new IllegalStateException();
					}
				}
			} catch (RuntimeException e) {
				trace.append('O').append(e instanceof IllegalArgumentException ? 'A' : 'S');
			}
		}

		public void check() {
			StringBuilder trace = new StringBuilder();
			run(0, trace);
			if (!"ABF".contentEquals(trace)) {
				throw new AssertionError("normal exit: " + trace);
			}
			trace.setLength(0);
			run(1, trace);
			if (!"ACF".contentEquals(trace)) {
				throw new AssertionError("recovering catch: " + trace);
			}
			trace.setLength(0);
			run(2, trace);
			if (!"AFOS".contentEquals(trace)) {
				throw new AssertionError("body throw: " + trace);
			}
			trace.setLength(0);
			run(3, trace);
			if (!"ABFOS".contentEquals(trace)) {
				throw new AssertionError("cleanup throw: " + trace);
			}
		}
	}

	public static class NestedCatchRecoveryReturnEvaluationFlow {
		private int identity(int kind) {
			return kind;
		}

		private String finish(StringBuilder trace, int kind) {
			trace.append('R');
			if (kind == 4 || kind == 5) {
				throw new IllegalStateException("return expression");
			}
			return trace.toString();
		}

		private String run(int kind, StringBuilder trace) {
			try {
				try {
					trace.append('A');
					if (kind == 1 || kind == 5) {
						throw new IllegalArgumentException();
					}
					if (kind == 2) {
						throw new IllegalStateException();
					}
					trace.append('B');
				} catch (IllegalArgumentException e) {
					trace.append('C');
				} finally {
					trace.append('F');
					if (kind == 3) {
						throw new IllegalStateException();
					}
				}
			} catch (RuntimeException e) {
				trace.append('O');
			}
			return finish(trace, identity(kind));
		}

		public void check() {
			StringBuilder trace = new StringBuilder();
			if (!"ABFR".equals(run(0, trace))) {
				throw new AssertionError("normal return");
			}
			trace.setLength(0);
			if (!"ACFR".equals(run(1, trace))) {
				throw new AssertionError("catch return");
			}
			trace.setLength(0);
			try {
				run(4, trace);
				throw new AssertionError("return must throw");
			} catch (IllegalStateException expected) {
				if (!"ABFR".contentEquals(trace)) {
					throw new AssertionError("outer catch must not intercept return expression: " + trace);
				}
			}
			trace.setLength(0);
			try {
				run(5, trace);
				throw new AssertionError("recovered catch return must throw");
			} catch (IllegalStateException expected) {
				if (!"ACFR".contentEquals(trace)) {
					throw new AssertionError("recovered return must escape outer catch: " + trace);
				}
			}
		}
	}

	public static class NestedCatchRecoveryPlainFinallyFlow {
		private String run(int kind) {
			StringBuilder trace = new StringBuilder();
			try {
				try {
					trace.append('A');
					if (kind == 1) {
						throw new IllegalArgumentException();
					}
					if (kind == 2) {
						throw new IllegalStateException();
					}
					trace.append('B');
				} catch (IllegalArgumentException e) {
					trace.append('C');
				} finally {
					trace.append('F');
				}
			} catch (RuntimeException e) {
				trace.append('O').append(e instanceof IllegalArgumentException ? 'A' : 'S');
			}
			return trace.toString();
		}

		public void check() {
			String zero = run(0);
			String one = run(1);
			String two = run(2);
			if (!"ABF".equals(zero) || !"ACF".equals(one) || !"AFOS".equals(two)) {
				throw new AssertionError("nested catch and plain finally ordering: " + zero + ", " + one + ", " + two);
			}
		}
	}

	public static class NestedCatchRecoveryThrowingCleanupFlow {
		private void cleanup(StringBuilder trace, int kind) {
			trace.append('F');
			if (kind == 3) {
				throw new IllegalStateException();
			}
		}

		private String run(int kind) {
			StringBuilder trace = new StringBuilder();
			try {
				try {
					trace.append('A');
					if (kind == 1) {
						throw new IllegalArgumentException();
					}
					if (kind == 2) {
						throw new IllegalStateException();
					}
					trace.append('B');
				} catch (IllegalArgumentException e) {
					trace.append('C');
				} finally {
					cleanup(trace, kind);
				}
			} catch (RuntimeException e) {
				trace.append('O').append(e instanceof IllegalArgumentException ? 'A' : 'S');
			}
			return trace.toString();
		}

		public void check() {
			if (!"ABF".equals(run(0)) || !"ACF".equals(run(1))
					|| !"AFOS".equals(run(2)) || !"ABFOS".equals(run(3))) {
				throw new AssertionError("cleanup exception must reach the outer catch once");
			}
		}
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testFinallyContinueFlow() {
		getClassNode(FinallyContinueFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testCatchContinueFinallyFlow() {
		getClassNode(CatchContinueFinallyFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testFinallyOverridesReturnFlow() {
		getClassNode(FinallyOverridesReturnFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testLabeledBreakFinallyFlow() {
		getClassNode(LabeledBreakFinallyFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testFinallyContinueOverridesBreakFlow() {
		getClassNode(FinallyContinueOverridesBreakFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testFinallyBreakOverridesContinueFlow() {
		getClassNode(FinallyBreakOverridesContinueFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testNestedCatchRethrowFinallyFlow() {
		getClassNode(NestedCatchRethrowFinallyFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.JAVA8 })
	public void testNestedCatchRecoveryFinallyFlow() {
		getClassNode(NestedCatchRecoveryFinallyFlow.class);
	}

	/** D8 emits both a rethrow and a fresh cleanup throw around a duplicated return. */
	@TestWithProfiles({ TestProfile.D8_J8 })
	public void testNestedCatchRecoveryFinallyFlowD8() {
		getClassNode(NestedCatchRecoveryFinallyFlow.class);
	}

	/** All three inputs need both conditional exits factored, not just the cleanup invoke. */
	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testNestedCatchRecoveryVoidFinallyFlow() {
		getClassNode(NestedCatchRecoveryVoidFinallyFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testNestedCatchRecoveryReturnEvaluationFlow() {
		getClassNode(NestedCatchRecoveryReturnEvaluationFlow.class);
	}

	/**
	 * The JVM exception table protects the body and recovering IllegalArgumentException catch
	 * separately, but excludes both inline F copies from the catch-all. The outer RuntimeException
	 * handler covers those copies as well. Merely swapping the generated catch nesting would make
	 * an exception from F run the cleanup twice; recovery must factor the normal copies before
	 * relocating the catch-all. DX/Java emit AOS instead of AFOS on kind=2, while D8 also omits F
	 * on kind=1 (AC instead of ACF).
	 */
	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testNestedCatchRecoveryPlainFinallyFlow() {
		getClassNode(NestedCatchRecoveryPlainFinallyFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testNestedCatchRecoveryThrowingCleanupFlow() {
		getClassNode(NestedCatchRecoveryThrowingCleanupFlow.class);
	}
}
