package jadx.tests.integration.others;

import java.util.List;

import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.IntegrationTest;
import jadx.tests.api.extensions.profiles.TestProfile;
import jadx.tests.api.extensions.profiles.TestWithProfiles;
import jadx.tests.integration.others.staticindirectread.IndirectChild;
import jadx.tests.integration.others.staticindirectread.IndirectParent;
import jadx.tests.integration.others.staticindirectread.IndirectTrigger;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestStaticIndirectParentReadBeforeChildInit extends IntegrationTest {
	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void test() {
		List<ClassNode> classes = getClassNodes(IndirectTrigger.class, IndirectParent.class, IndirectChild.class);
		assertThat(searchCls(classes, IndirectChild.class.getName())).code()
				.containsOne(
						"JADX WARN: Field initializer kept in class initializer: supertype initializer may read field before assignment")
				.containsOne("public static final int initial;")
				.containsOne("initial = 17;")
				.doesNotContain("static final int initial = 17");
	}
}
