package jadx.tests.integration.others;

import jadx.tests.api.IntegrationTest;
import jadx.tests.api.extensions.profiles.TestProfile;
import jadx.tests.api.extensions.profiles.TestWithProfiles;

public class TestNumericCastAssignmentRoundTrip extends IntegrationTest {
	public static class TestCls {
		private static double value;

		public static void storeRounded(long source) {
			value = (float) source;
		}

		public void check() {
			storeRounded(16_777_217L);
			if (value != 16_777_216.0d) {
				throw new AssertionError("field assignment must retain float rounding");
			}
		}
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void preservesNumericCastInAssignment() {
		getClassNode(TestCls.class);
	}
}
