package jadx.tests.integration.others;

import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.IntegrationTest;
import jadx.tests.api.extensions.profiles.TestProfile;
import jadx.tests.api.extensions.profiles.TestWithProfiles;
import jadx.tests.integration.others.fieldcallback.CallbackChild;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestExternalCallbackBeforeFinalFieldInit extends IntegrationTest {
	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void test() {
		ClassNode cls = getClassNode(CallbackChild.class);
		assertThat(cls).code()
				.containsOne("ExternalFieldObserver.observe(this);")
				.containsOne("this.initial = 17;")
				.doesNotContain("final int initial = 17");
	}
}
