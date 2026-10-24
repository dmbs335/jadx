package jadx.tests.integration.others;

import java.util.List;

import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.IntegrationTest;
import jadx.tests.api.extensions.profiles.TestProfile;
import jadx.tests.api.extensions.profiles.TestWithProfiles;
import jadx.tests.integration.others.staticlambdatransitive.Helper;
import jadx.tests.integration.others.staticlambdatransitive.Primary;
import jadx.tests.integration.others.staticlambdatransitive.Secondary;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestLambdaTransitiveStaticInitRead extends IntegrationTest {
	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void test() {
		List<ClassNode> classes = getClassNodes(Primary.class, Helper.class, Secondary.class);
		assertThat(searchCls(classes, Primary.class.getName())).code()
				.containsOne("public static final int initial;")
				.containsOne("initial = 17;")
				.doesNotContain("static final int initial = 17");
	}
}
