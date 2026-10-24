package jadx.tests.integration.others;

import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.IntegrationTest;
import jadx.tests.api.extensions.profiles.TestProfile;
import jadx.tests.api.extensions.profiles.TestWithProfiles;
import jadx.tests.integration.others.unresolvedbase.NestedPrivateFieldOwner;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestUnresolvedSuperConstructorNestedPrivateRead extends IntegrationTest {
	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void test() {
		ClassNode cls = getClassNode(NestedPrivateFieldOwner.class);
		assertThat(cls).code()
				.containsOne("JADX WARN: Field initializer kept in constructor: superclass constructor unavailable to verify read order")
				.containsOne("private final int initial;")
				.containsOne("this.initial = 17;")
				.doesNotContain("final int initial = 17");
	}
}
