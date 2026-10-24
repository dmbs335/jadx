package jadx.tests.integration.arith;

import jadx.api.args.IntegerFormat;
import jadx.tests.api.IntegrationTest;
import jadx.tests.api.extensions.profiles.TestProfile;
import jadx.tests.api.extensions.profiles.TestWithProfiles;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestAddMinValueLiteral extends IntegrationTest {

	public static class TestCls {
		public static int addIntMin(int value) {
			return value + Integer.MIN_VALUE;
		}

		public static long addLongMin(long value) {
			return value + Long.MIN_VALUE;
		}

		public void check() {
			if (addIntMin(0) != Integer.MIN_VALUE || addIntMin(Integer.MIN_VALUE) != 0) {
				throw new AssertionError("int min-value addition");
			}
			if (addLongMin(0) != Long.MIN_VALUE || addLongMin(Long.MIN_VALUE) != 0) {
				throw new AssertionError("long min-value addition");
			}
		}
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void keepsMinValueAdditionRepresentable() {
		getArgs().setIntegerFormat(IntegerFormat.DECIMAL);
		assertThat(getClassNode(TestCls.class))
				.code()
				.doesNotContain("value - 2147483648")
				.doesNotContain("value - -9223372036854775808L");
	}
}
