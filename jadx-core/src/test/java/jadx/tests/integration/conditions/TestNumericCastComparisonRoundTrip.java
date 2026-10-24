package jadx.tests.integration.conditions;

import jadx.tests.api.IntegrationTest;
import jadx.tests.api.extensions.profiles.TestProfile;
import jadx.tests.api.extensions.profiles.TestWithProfiles;

public class TestNumericCastComparisonRoundTrip extends IntegrationTest {
	public static class TestCls {
		public static boolean longAsDoubleEqualsFloat(long value, float other) {
			return (double) value == other;
		}

		public static boolean longAsFloatEqualsDouble(long value, double other) {
			return (float) value == other;
		}

		public void check() {
			if (longAsDoubleEqualsFloat(16_777_217L, 16_777_216.0f)) {
				throw new AssertionError("explicit double conversion must precede comparison");
			}
			if (!longAsFloatEqualsDouble(16_777_217L, 16_777_216.0d)) {
				throw new AssertionError("explicit float rounding must precede comparison");
			}
		}
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void preservesNumericCastInComparison() {
		getClassNode(TestCls.class);
	}
}
