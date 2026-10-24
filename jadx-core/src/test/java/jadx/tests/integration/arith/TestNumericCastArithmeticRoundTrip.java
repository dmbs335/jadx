package jadx.tests.integration.arith;

import jadx.tests.api.IntegrationTest;
import jadx.tests.api.extensions.profiles.TestProfile;
import jadx.tests.api.extensions.profiles.TestWithProfiles;

public class TestNumericCastArithmeticRoundTrip extends IntegrationTest {
	public static class TestCls {
		public static double addRounded(long value) {
			return (float) value + 0.0d;
		}

		public static float divideAsFloat(int numerator, int denominator) {
			return (float) numerator / denominator;
		}

		public static long shiftAfterNarrowing(long value) {
			return ((int) value) << 1;
		}

		public void check() {
			if (addRounded(16_777_217L) != 16_777_216.0d) {
				throw new AssertionError("addition must retain float rounding");
			}
			if (divideAsFloat(1, 2) != 0.5f) {
				throw new AssertionError("division must retain float promotion");
			}
			if (shiftAfterNarrowing(0x1_0000_0001L) != 2L) {
				throw new AssertionError("shift must retain narrowing before arithmetic");
			}
		}
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void preservesNumericCastsInArithmetic() {
		getClassNode(TestCls.class);
	}
}
