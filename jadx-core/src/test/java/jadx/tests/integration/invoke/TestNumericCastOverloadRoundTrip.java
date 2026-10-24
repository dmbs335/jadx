package jadx.tests.integration.invoke;

import jadx.tests.api.IntegrationTest;
import jadx.tests.api.extensions.profiles.TestProfile;
import jadx.tests.api.extensions.profiles.TestWithProfiles;

public class TestNumericCastOverloadRoundTrip extends IntegrationTest {
	public static class TestCls {
		public static class Target {
			final int kind;

			public Target(long value) {
				this.kind = 1;
			}

			public Target(float value) {
				this.kind = 2;
			}

			public Target(double value) {
				this.kind = 3;
			}
		}

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

		public static int callFloatViaLocal(long value) {
			float converted = (float) value;
			return select(converted);
		}

		public static int callDoubleViaLocal(long value) {
			double converted = (double) value;
			return select(converted);
		}

		public static int constructFloat(long value) {
			return new Target((float) value).kind;
		}

		public static int constructDouble(long value) {
			return new Target((double) value).kind;
		}

		public void check() {
			if (callFloat(16_777_217L) != 2 || callDouble(16_777_217L) != 3) {
				throw new AssertionError("explicit casts must select their overloads");
			}
			if (callFloatViaLocal(16_777_217L) != 2 || callDoubleViaLocal(16_777_217L) != 3) {
				throw new AssertionError("local conversions must select their overloads");
			}
			if (constructFloat(16_777_217L) != 2 || constructDouble(16_777_217L) != 3) {
				throw new AssertionError("explicit casts must select constructor overloads");
			}
		}
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void preservesNumericCastForOverloadSelection() {
		getClassNode(TestCls.class);
	}
}
