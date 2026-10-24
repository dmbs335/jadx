package jadx.tests.integration.others;

import java.util.List;

import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.IntegrationTest;
import jadx.tests.api.extensions.profiles.TestProfile;
import jadx.tests.api.extensions.profiles.TestWithProfiles;
import jadx.tests.integration.others.staticparentread.StaticChild;
import jadx.tests.integration.others.staticparentread.StaticParent;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestStaticParentReadBeforeChildInit extends IntegrationTest {
	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void test() {
		List<ClassNode> classes = getClassNodes(StaticParent.class, StaticChild.class);
		assertThat(searchCls(classes, StaticChild.class.getName())).code()
				.containsOne(
						"JADX WARN: Field initializer kept in class initializer: supertype initializer may read field before assignment")
				.containsOne("public static final int initial;")
				.containsOne("initial = 17;")
				.doesNotContain("static final int initial = 17");
	}
}
