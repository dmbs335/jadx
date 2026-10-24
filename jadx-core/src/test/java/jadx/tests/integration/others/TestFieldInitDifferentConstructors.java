package jadx.tests.integration.others;

import jadx.tests.api.IntegrationTest;
import jadx.tests.api.extensions.profiles.TestProfile;
import jadx.tests.api.extensions.profiles.TestWithProfiles;

public class TestFieldInitDifferentConstructors extends IntegrationTest {
	public static class TestCls {
		private final int kind;

		public TestCls() {
			this.kind = 3;
		}

		public TestCls(int value) {
			this.kind = 1;
		}

		public TestCls(String value) {
			this.kind = 2;
		}

		public void check() {
			if (new TestCls(0).kind != 1 || new TestCls("x").kind != 2) {
				throw new AssertionError("constructor-specific field initializers must not be merged");
			}
		}
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void keepsDifferentInitializersInConstructors() {
		getClassNode(TestCls.class);
	}
}
