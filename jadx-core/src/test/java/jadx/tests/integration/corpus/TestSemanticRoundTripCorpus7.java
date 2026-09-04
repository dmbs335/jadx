package jadx.tests.integration.corpus;

import jadx.tests.api.IntegrationTest;
import jadx.tests.api.extensions.profiles.TestProfile;
import jadx.tests.api.extensions.profiles.TestWithProfiles;

/** Semantic round-trip cases focused on discarded qualifiers and integral edge behavior. */
public class TestSemanticRoundTripCorpus7 extends IntegrationTest {

	public static class StaticQualifierEvaluationFlow {
		private static final class Target {
			private static void accept(StaticQualifierEvaluationFlow owner, int value) {
				owner.trace = owner.trace * 10 + value;
			}
		}

		private int trace;

		private Target receiver(boolean missing) {
			trace = trace * 10 + 1;
			return missing ? null : new Target();
		}

		private int argument() {
			trace = trace * 10 + 2;
			return 3;
		}

		@SuppressWarnings("static-access")
		private int run(boolean missing) {
			trace = 0;
			receiver(missing).accept(this, argument());
			return trace;
		}

		public void check() {
			if (run(false) != 123 || run(true) != 123) {
				throw new AssertionError("static qualifier evaluation");
			}
		}
	}

	public static class CompoundFieldNullOrderFlow {
		private static final class Target {
			private int value = 5;
		}

		private int trace;

		private Target target(boolean missing) {
			trace = trace * 10 + 1;
			return missing ? null : new Target();
		}

		private int right() {
			trace = trace * 10 + 2;
			return 7;
		}

		private int run(boolean missing) {
			trace = 0;
			try {
				Target target = target(missing);
				target.value += right();
				return trace * 100 + target.value;
			} catch (NullPointerException e) {
				return -trace;
			}
		}

		public void check() {
			if (run(false) != 1212 || run(true) != -12) {
				throw new AssertionError("compound field null order");
			}
		}
	}

	public static class IntegralDivisionEdgeFlow {
		private int trace;

		private int left() {
			trace = trace * 10 + 1;
			return Integer.MIN_VALUE;
		}

		private int right(int marker, int value) {
			trace = trace * 10 + marker;
			return value;
		}

		private long runOverflow() {
			trace = 0;
			int quotient = left() / right(2, -1);
			int remainder = left() % right(3, -1);
			return trace * 10000000000L + (quotient & 0xffffffffL) * 10L + remainder;
		}

		private int runZero() {
			trace = 0;
			try {
				return left() / right(4, 0);
			} catch (ArithmeticException e) {
				return -trace;
			}
		}

		public void check() {
			if (runOverflow() != 12151474836480L || runZero() != -14) {
				throw new AssertionError("integral division edges");
			}
		}
	}

	public static class NarrowIncrementFlow {
		private int run() {
			char character = Character.MAX_VALUE;
			int oldCharacter = character++;
			byte number = Byte.MAX_VALUE;
			int oldNumber = number++;
			number += 1;
			return (oldCharacter == 65535 ? 1 : 0) * 10000
					+ character * 1000
					+ oldNumber * 10
					+ (number & 0xff);
		}

		public void check() {
			if (run() != 11399) {
				throw new AssertionError("narrow increment wrapping");
			}
		}

	}

	public static class BoxingBoundaryFlow {
		private Integer integer(int value) {
			return value;
		}

		private Character character(char value) {
			return value;
		}

		private int run() {
			Integer first = integer(127);
			Integer second = integer(127);
			Character left = character((char) 127);
			Character right = character((char) 127);
			int score = first == second ? 1 : 0;
			return score * 10 + (left == right ? 2 : 0);
		}

		public void check() {
			if (run() != 12) {
				throw new AssertionError("boxing cache boundary");
			}
		}
	}

	public static class EnumSwitchNullFlow {
		private enum Mode {
			FIRST,
			SECOND
		}

		private int trace;

		private Mode mode(Mode value) {
			trace = trace * 10 + 1;
			return value;
		}

		private int run(Mode value) {
			trace = 0;
			try {
				switch (mode(value)) {
					case FIRST:
						trace = trace * 10 + 2;
						break;
					case SECOND:
						trace = trace * 10 + 3;
						break;
					default:
						trace = trace * 10 + 4;
				}
				return trace;
			} catch (NullPointerException e) {
				return -trace;
			}
		}

		public void check() {
			if (run(Mode.FIRST) != 12 || run(Mode.SECOND) != 13 || run(null) != -1) {
				throw new AssertionError("enum switch null behavior");
			}
		}
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testStaticQualifierEvaluationFlow() {
		getClassNode(StaticQualifierEvaluationFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testCompoundFieldNullOrderFlow() {
		getClassNode(CompoundFieldNullOrderFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testIntegralDivisionEdgeFlow() {
		getClassNode(IntegralDivisionEdgeFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testNarrowIncrementFlow() {
		getClassNode(NarrowIncrementFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testBoxingBoundaryFlow() {
		getClassNode(BoxingBoundaryFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testEnumSwitchNullFlow() {
		getClassNode(EnumSwitchNullFlow.class);
	}
}
