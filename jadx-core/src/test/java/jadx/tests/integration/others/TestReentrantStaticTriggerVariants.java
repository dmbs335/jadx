package jadx.tests.integration.others;

import java.util.List;

import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.IntegrationTest;
import jadx.tests.api.extensions.profiles.TestProfile;
import jadx.tests.api.extensions.profiles.TestWithProfiles;
import jadx.tests.integration.others.statictriggervariants.NewPrimary;
import jadx.tests.integration.others.statictriggervariants.NewSecondary;
import jadx.tests.integration.others.statictriggervariants.WritePrimary;
import jadx.tests.integration.others.statictriggervariants.WriteSecondary;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestReentrantStaticTriggerVariants extends IntegrationTest {
	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testStaticWrite() {
		List<ClassNode> classes = getClassNodes(WritePrimary.class, WriteSecondary.class);
		assertThat(searchCls(classes, WritePrimary.class.getName())).code()
				.containsOne("JADX WARN: Final modifier removed: static field is read indirectly before assignment")
				.containsOne("initial = 17;")
				.doesNotContain("static final int initial = 17");
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testNewInstance() {
		List<ClassNode> classes = getClassNodes(NewPrimary.class, NewSecondary.class);
		assertThat(searchCls(classes, NewPrimary.class.getName())).code()
				.containsOne("JADX WARN: Final modifier removed: static field is read indirectly before assignment")
				.containsOne("initial = 17;")
				.doesNotContain("static final int initial = 17");
	}
}
