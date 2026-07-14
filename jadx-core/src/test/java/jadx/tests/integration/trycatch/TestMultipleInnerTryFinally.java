package jadx.tests.integration.trycatch;

import jadx.tests.api.IntegrationTest;
import jadx.tests.api.extensions.profiles.TestProfile;
import jadx.tests.api.extensions.profiles.TestWithProfiles;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestMultipleInnerTryFinally extends IntegrationTest {

	public static class TestCls {
		public void test() {
			try {
				try {
					first();
				} catch (IllegalArgumentException e) {
					onFirstError(e);
				}
				try {
					second();
				} catch (IllegalStateException e) {
					onSecondError(e);
				}
			} finally {
				finish();
			}
		}

		private void first() {
		}

		private void second() {
		}

		private void onFirstError(IllegalArgumentException e) {
		}

		private void onSecondError(IllegalStateException e) {
		}

		private void finish() {
		}
	}

	@TestWithProfiles({ TestProfile.JAVA8, TestProfile.DX_J8 })
	public void test() {
		assertThat(getClassNode(TestCls.class))
				.code()
				.containsOne("first();")
				.containsOne("second();")
				.countString(3, "} catch (")
				.countString(2, "finish();");
	}
}
