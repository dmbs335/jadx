package jadx.tests.integration.others;

import java.util.List;

import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.IntegrationTest;
import jadx.tests.api.extensions.profiles.TestProfile;
import jadx.tests.api.extensions.profiles.TestWithProfiles;
import jadx.tests.integration.others.unresolvedbase.UnresolvedGrandchild;
import jadx.tests.integration.others.unresolvedbase.UnresolvedMiddle;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestUnresolvedSuperConstructorDescendantFieldRead extends IntegrationTest {
	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void test() {
		List<ClassNode> classes = getClassNodes(UnresolvedMiddle.class, UnresolvedGrandchild.class);
		assertThat(searchCls(classes, UnresolvedMiddle.class.getName())).code()
				.containsOne("JADX WARN: Field initializer kept in constructor: superclass constructor unavailable to verify read order")
				.containsOne("protected final int initial;")
				.containsOne("this.initial = 17;")
				.doesNotContain("final int initial = 17");
	}
}
