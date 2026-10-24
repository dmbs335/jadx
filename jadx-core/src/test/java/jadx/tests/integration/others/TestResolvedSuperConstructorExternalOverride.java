package jadx.tests.integration.others;

import java.util.List;

import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.IntegrationTest;
import jadx.tests.api.extensions.profiles.TestProfile;
import jadx.tests.api.extensions.profiles.TestWithProfiles;
import jadx.tests.integration.others.resolvedbase.AliasBase;
import jadx.tests.integration.others.resolvedbase.AliasMiddle;
import jadx.tests.integration.others.resolvedbase.BridgeBase;
import jadx.tests.integration.others.resolvedbase.BridgeMiddle;
import jadx.tests.integration.others.resolvedbase.ExternalAliasChild;
import jadx.tests.integration.others.resolvedbase.ExternalRelayChild;
import jadx.tests.integration.others.resolvedbase.ForwardBase;
import jadx.tests.integration.others.resolvedbase.ForwardMiddle;
import jadx.tests.integration.others.resolvedbase.RelayBase;
import jadx.tests.integration.others.resolvedbase.RelayMiddle;
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

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testForwardedReceiver() {
		List<ClassNode> classes = getClassNodes(ForwardBase.class, ForwardMiddle.class);
		assertThat(searchCls(classes, ForwardMiddle.class.getName())).code()
				.containsOne(
						"JADX WARN: Field initializer kept in constructor: superclass constructor may dispatch to an external override")
				.containsOne("protected final int initial;")
				.containsOne("this.initial = 23;")
				.doesNotContain("final int initial = 23");
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testAliasedReceiver() {
		new ExternalAliasChild().check();
		List<ClassNode> classes = getClassNodes(AliasBase.class, AliasMiddle.class);
		assertThat(searchCls(classes, AliasMiddle.class.getName())).code()
				.containsOne(
						"JADX WARN: Field initializer kept in constructor: superclass constructor may dispatch to an external override")
				.containsOne("protected final int initial;")
				.containsOne("this.initial = 29;")
				.doesNotContain("final int initial = 29");
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testRelayedReceiver() {
		new ExternalRelayChild().check();
		List<ClassNode> classes = getClassNodes(RelayBase.class, RelayBase.Dispatcher.class, RelayMiddle.class);
		assertThat(searchCls(classes, RelayMiddle.class.getName())).code()
				.containsOne(
						"JADX WARN: Field initializer kept in constructor: superclass constructor may dispatch to an external override")
				.containsOne("protected final int initial;")
				.containsOne("this.initial = 31;")
				.doesNotContain("final int initial = 31");
	}
}
