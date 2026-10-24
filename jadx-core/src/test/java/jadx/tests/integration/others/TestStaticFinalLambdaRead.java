package jadx.tests.integration.others;

import java.util.function.IntSupplier;

import jadx.tests.api.IntegrationTest;
import jadx.tests.api.extensions.profiles.TestProfile;
import jadx.tests.api.extensions.profiles.TestWithProfiles;

public class TestStaticFinalLambdaRead extends IntegrationTest {
	public static class TestCls {
		private static final int INITIAL;
		private static final int observed;

		static {
			IntSupplier reader = TestCls::readInitial;
			observed = reader.getAsInt();
			INITIAL = 9;
		}

		private static int readInitial() {
			return INITIAL;
		}

		public void check() {
			if (observed != 0 || INITIAL != 9) {
				throw new AssertionError("static final read order changed");
			}
		}
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void test() {
		getClassNode(TestCls.class);
	}
}
