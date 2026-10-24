package jadx.tests.integration.conditions;

import jadx.tests.api.IntegrationTest;
import jadx.tests.api.extensions.profiles.TestProfile;
import jadx.tests.api.extensions.profiles.TestWithProfiles;

public class TestNumericCastTernaryRoundTrip extends IntegrationTest {
	public static class TestCls {
		public static double chooseFloat(boolean choose, long value, double fallback) {
			return choose ? (float) value : fallback;
		}

		public static double chooseDouble(boolean choose, long value, float fallback) {
			return choose ? (double) value : fallback;
		}

		public void check() {
			if (chooseFloat(true, 16_777_217L, 0.0d) != 16_777_216.0d) {
				throw new AssertionError("ternary must retain long-to-float rounding");
			}
			if (chooseDouble(true, 16_777_217L, 0.0f) != 16_777_217.0d) {
				throw new AssertionError("ternary must retain long-to-double precision");
			}
		}
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void preservesNumericCastInTernary() {
		getClassNode(TestCls.class);
	}
}
