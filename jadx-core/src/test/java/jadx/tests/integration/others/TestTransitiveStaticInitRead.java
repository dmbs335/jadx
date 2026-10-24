package jadx.tests.integration.others;

import java.util.List;

import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.IntegrationTest;
import jadx.tests.api.extensions.profiles.TestProfile;
import jadx.tests.api.extensions.profiles.TestWithProfiles;
import jadx.tests.integration.others.statictransitive.Helper;
import jadx.tests.integration.others.statictransitive.Primary;
import jadx.tests.integration.others.statictransitive.Secondary;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestTransitiveStaticInitRead extends IntegrationTest {
	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void test() {
		List<ClassNode> classes = getClassNodes(Primary.class, Helper.class, Secondary.class);
		assertThat(searchCls(classes, Primary.class.getName())).code()
				.containsOne("JADX WARN: Final modifier removed: static field is read indirectly before assignment")
				.containsOne("public static int initial = 0;")
				.containsOne("initial = 17;")
				.doesNotContain("static final int initial = 17");
	}
}
