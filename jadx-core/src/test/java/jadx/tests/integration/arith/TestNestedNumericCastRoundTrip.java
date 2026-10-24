package jadx.tests.integration.arith;

import jadx.tests.api.IntegrationTest;
import jadx.tests.api.extensions.profiles.TestProfile;
import jadx.tests.api.extensions.profiles.TestWithProfiles;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestNestedNumericCastRoundTrip extends IntegrationTest {
	public static class TestCls {
		public static int longThroughFloat(long value) {
			return (int) (float) value;
		}

		public static long longThroughDouble(long value) {
			return (long) (double) value;
		}

		public void check() {
			if (longThroughFloat(16_777_217L) != 16_777_216) {
				throw new AssertionError("long-to-float rounding must be preserved");
			}
			if (longThroughFloat(Long.MAX_VALUE) != Integer.MAX_VALUE) {
				throw new AssertionError("float-to-int saturation must be preserved");
			}
			if (longThroughDouble(9_007_199_254_740_993L) != 9_007_199_254_740_992L) {
				throw new AssertionError("long-to-double rounding must be preserved");
			}
		}
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void preservesNestedNumericCastSemantics() {
		assertThat(getClassNode(TestCls.class))
				.code()
				.contains("return (int) ((float) value);")
				.contains("return (long) ((double) value);");
	}
}
