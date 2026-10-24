package jadx.tests.integration.others;

import jadx.tests.api.IntegrationTest;
import jadx.tests.api.extensions.profiles.TestProfile;
import jadx.tests.api.extensions.profiles.TestWithProfiles;

public class TestNumericCastAssignmentRoundTrip extends IntegrationTest {
	public static class TestCls {
		private static double value;
		private static final double[] values = new double[1];

		public static void storeRounded(long source) {
			value = (float) source;
		}

		public static void storeArrayRounded(long source) {
			values[0] = (float) source;
		}

		public static double returnRounded(long source) {
			double rounded = (float) source;
			return rounded;
		}

		public static double returnRoundedViaLocal(long source) {
			float intermediate = (float) source;
			double rounded = intermediate;
			return rounded;
		}

		public void check() {
			storeRounded(16_777_217L);
			if (value != 16_777_216.0d) {
				throw new AssertionError("field assignment must retain float rounding");
			}
			storeArrayRounded(16_777_217L);
			if (values[0] != 16_777_216.0d) {
				throw new AssertionError("array assignment must retain float rounding");
			}
			if (returnRounded(16_777_217L) != 16_777_216.0d) {
				throw new AssertionError("local assignment must retain float rounding");
			}
			if (returnRoundedViaLocal(16_777_217L) != 16_777_216.0d) {
				throw new AssertionError("intermediate local must retain float rounding");
			}
		}
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void preservesNumericCastInAssignment() {
		getClassNode(TestCls.class);
	}
}
