package jadx.tests.integration.others;

import jadx.tests.api.IntegrationTest;
import jadx.tests.api.extensions.profiles.TestProfile;
import jadx.tests.api.extensions.profiles.TestWithProfiles;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestFieldInitOtherInstance extends IntegrationTest {
	public static class TestCls {
		int value;

		public TestCls(TestCls target) {
			target.value = 7;
		}
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void keepsWriteToOtherInstanceInConstructor() {
		assertThat(getClassNode(TestCls.class))
				.code()
				.containsOne("target.value = 7;")
				.doesNotContain("int value = 7;");
	}
}
