package jadx.tests.integration.others;

import jadx.tests.api.IntegrationTest;
import jadx.tests.api.extensions.profiles.TestProfile;
import jadx.tests.api.extensions.profiles.TestWithProfiles;

public class TestFieldInitSideEffectOrder extends IntegrationTest {
	public static class TestCls {
		private static int stage;
		private final int observed;

		public TestCls() {
			stage = 1;
			this.observed = advance();
		}

		private static int advance() {
			return ++stage;
		}

		public void check() {
			stage = 0;
			TestCls next = new TestCls();
			if (next.observed != 2 || stage != 2) {
				throw new AssertionError("field assignment must stay after constructor side effects");
			}
		}
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void keepsFieldAssignmentAfterConstructorSideEffects() {
		getClassNode(TestCls.class);
	}
}
