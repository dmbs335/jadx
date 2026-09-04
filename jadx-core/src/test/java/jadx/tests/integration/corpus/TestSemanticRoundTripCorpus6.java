package jadx.tests.integration.corpus;

import java.io.IOException;

import jadx.tests.api.IntegrationTest;
import jadx.tests.api.extensions.profiles.TestProfile;
import jadx.tests.api.extensions.profiles.TestWithProfiles;

/** Semantic round-trip cases focused on promotions, masking, cloning and exception identity. */
public class TestSemanticRoundTripCorpus6 extends IntegrationTest {

	public static class ShiftDistanceMaskingFlow {
		private int trace;

		private int distance(int marker, int value) {
			trace = trace * 10 + marker;
			return value;
		}

		private long run() {
			trace = 0;
			int left = 1 << distance(1, 33);
			long wide = 1L << distance(2, 65);
			int unsigned = -1 >>> distance(3, 32);
			return trace * 1000L + left * 100L + wide * 10L + (unsigned == -1 ? 1 : 0);
		}

		public void check() {
			if (run() != 123221L) {
				throw new AssertionError("shift distance masking");
			}
		}
	}

	public static class StringConcatEvaluationFlow {
		private int trace;

		private String prefix() {
			trace = trace * 10 + 1;
			return "x";
		}

		private int number(int marker, int value) {
			trace = trace * 10 + marker;
			return value;
		}

		private char character(int marker, char value) {
			trace = trace * 10 + marker;
			return value;
		}

		private String suffix() {
			trace = trace * 10 + 6;
			return "z";
		}

		private String run() {
			trace = 0;
			String first = prefix() + number(2, 2) + character(3, 'A');
			int numeric = number(4, 2) + character(5, 'A');
			String second = numeric + suffix();
			return first + ':' + second + ':' + trace;
		}

		public void check() {
			if (!run().equals("x2A:67z:123456")) {
				throw new AssertionError("string concat evaluation");
			}
		}
	}

	public static class ArrayCloneTypeFlow {
		private int run() {
			String[][] values = { { "a" }, { "b" } };
			String[][] copy = values.clone();
			copy[0][0] = "changed";
			copy[1] = new String[] { "copy" };
			int score = values != copy ? 1 : 0;
			score = score * 10 + (values[0] == copy[0] ? 2 : 0);
			score = score * 10 + values[0][0].length();
			score = score * 10 + values[1][0].length();
			return score;
		}

		public void check() {
			if (run() != 1271) {
				throw new AssertionError("array clone covariance");
			}
		}
	}

	public static class MultiCatchRethrowFlow {
		private static final class FirstException extends Exception {
			private static final long serialVersionUID = 1L;
		}

		private static final class SecondException extends Exception {
			private static final long serialVersionUID = 1L;
		}

		private int trace;

		private void fail(int mode) throws FirstException, SecondException {
			trace = trace * 10 + 1;
			if (mode == 1) {
				throw new FirstException();
			}
			throw new SecondException();
		}

		private void relay(int mode) throws FirstException, SecondException {
			try {
				fail(mode);
			} catch (FirstException | SecondException e) {
				trace = trace * 10 + (e instanceof FirstException ? 2 : 3);
				throw e;
			}
		}

		private int run(int mode) {
			trace = 0;
			try {
				relay(mode);
				return 0;
			} catch (FirstException e) {
				return trace * 10 + 4;
			} catch (SecondException e) {
				return trace * 10 + 5;
			}
		}

		public void check() {
			if (run(1) != 124 || run(2) != 135) {
				throw new AssertionError("multi-catch rethrow identity");
			}
		}
	}

	public static class ConditionalNumericPromotionFlow {
		private int trace;

		private byte small(int marker, int value) {
			trace = trace * 10 + marker;
			return (byte) value;
		}

		private int run(boolean narrow) {
			trace = 0;
			byte selected = narrow ? small(1, 120) : 1;
			int promoted = narrow ? selected : small(2, -126);
			int mixed = narrow ? Character.MAX_VALUE : selected;
			return trace * 1000000 + (selected & 0xff) * 1000 + (promoted & 0xff) * 10 + mixed;
		}

		public void check() {
			if (run(true) != 1186735 || run(false) != 2002301) {
				throw new AssertionError("conditional numeric promotion");
			}
		}
	}

	public static class BitwiseBooleanEvaluationFlow {
		private int trace;

		private boolean value(int marker, boolean result) {
			trace = trace * 10 + marker;
			return result;
		}

		private int run(boolean first) throws IOException {
			trace = 0;
			boolean eager = value(1, first) & value(2, true);
			boolean lazy = value(3, first) && value(4, true);
			if (eager != lazy) {
				throw new IOException("boolean result");
			}
			return trace;
		}

		public void check() throws IOException {
			if (run(false) != 123 || run(true) != 1234) {
				throw new AssertionError("bitwise boolean evaluation");
			}
		}
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testShiftDistanceMaskingFlow() {
		getClassNode(ShiftDistanceMaskingFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testStringConcatEvaluationFlow() {
		getClassNode(StringConcatEvaluationFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testArrayCloneTypeFlow() {
		getClassNode(ArrayCloneTypeFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testMultiCatchRethrowFlow() {
		getClassNode(MultiCatchRethrowFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testConditionalNumericPromotionFlow() {
		getClassNode(ConditionalNumericPromotionFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testBitwiseBooleanEvaluationFlow() {
		getClassNode(BitwiseBooleanEvaluationFlow.class);
	}
}
