package jadx.tests.integration.invoke;

import jadx.tests.api.IntegrationTest;
import jadx.tests.api.extensions.profiles.TestProfile;
import jadx.tests.api.extensions.profiles.TestWithProfiles;

public class TestNumericCastOverloadRoundTrip extends IntegrationTest {
	public static class TestCls {
		public static int select(long value) {
			return 1;
		}

		public static int select(float value) {
			return 2;
		}

		public static int select(double value) {
			return 3;
		}

		public static int callFloat(long value) {
			return select((float) value);
		}

		public static int callDouble(long value) {
			return select((double) value);
		}

		public void check() {
			if (callFloat(16_777_217L) != 2 || callDouble(16_777_217L) != 3) {
				throw new AssertionError("explicit casts must select their overloads");
			}
		}
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void preservesNumericCastForOverloadSelection() {
		getClassNode(TestCls.class);
	}
}
