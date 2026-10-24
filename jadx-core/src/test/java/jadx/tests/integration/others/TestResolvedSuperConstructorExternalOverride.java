package jadx.tests.integration.others;

import java.util.List;

import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.IntegrationTest;
import jadx.tests.api.extensions.profiles.TestProfile;
import jadx.tests.api.extensions.profiles.TestWithProfiles;
import jadx.tests.integration.others.resolvedbase.BridgeBase;
import jadx.tests.integration.others.resolvedbase.BridgeMiddle;
import jadx.tests.integration.others.resolvedbase.ResolvedBase;
import jadx.tests.integration.others.resolvedbase.ResolvedMiddle;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestResolvedSuperConstructorExternalOverride extends IntegrationTest {
	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void test() {
		// ExternalChild is deliberately absent from the decompilation input.
		List<ClassNode> classes = getClassNodes(ResolvedBase.class, ResolvedMiddle.class);
		assertThat(searchCls(classes, ResolvedMiddle.class.getName())).code()
				.containsOne(
						"JADX WARN: Field initializer kept in constructor: superclass constructor may dispatch to an external override")
				.containsOne("protected final int initial;")
				.containsOne("this.initial = 17;")
				.doesNotContain("final int initial = 17");
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testPrivateBridge() {
		List<ClassNode> classes = getClassNodes(BridgeBase.class, BridgeMiddle.class);
		assertThat(searchCls(classes, BridgeMiddle.class.getName())).code()
				.containsOne(
						"JADX WARN: Field initializer kept in constructor: superclass constructor may dispatch to an external override")
				.containsOne("protected final int initial;")
				.containsOne("this.initial = 19;")
				.doesNotContain("final int initial = 19");
	}
}
