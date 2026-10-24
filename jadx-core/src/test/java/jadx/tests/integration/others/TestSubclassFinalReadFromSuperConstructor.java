package jadx.tests.integration.others;

import java.util.List;

import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.IntegrationTest;
import jadx.tests.api.extensions.profiles.TestProfile;
import jadx.tests.api.extensions.profiles.TestWithProfiles;
import jadx.tests.integration.others.subclassfield.SubclassFieldBase;
import jadx.tests.integration.others.subclassfield.SubclassFieldChild;
import jadx.tests.integration.others.subclassfield.SubclassFieldGrandchild;
import jadx.tests.integration.others.subclassfield.SubclassFieldMiddle;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestSubclassFinalReadFromSuperConstructor extends IntegrationTest {
	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void test() {
		List<ClassNode> classes = getClassNodes(
				SubclassFieldBase.class, SubclassFieldMiddle.class, SubclassFieldChild.class, SubclassFieldGrandchild.class);
		assertThat(searchCls(classes, SubclassFieldChild.class.getName())).code()
				.containsOne("JADX WARN: Final modifier removed: instance field is read before assignment in constructor")
				.doesNotContain("final int initial");
		assertThat(searchCls(classes, SubclassFieldGrandchild.class.getName())).code()
				.containsOne("JADX WARN: Final modifier removed: instance field is read before assignment in constructor")
				.doesNotContain("final int initial");
	}
}
