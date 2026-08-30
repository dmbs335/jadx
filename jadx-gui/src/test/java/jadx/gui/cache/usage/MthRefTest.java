package jadx.gui.cache.usage;

import org.junit.jupiter.api.Test;

import jadx.core.dex.nodes.ClassNode;
import jadx.core.dex.nodes.MethodNode;
import jadx.tests.api.IntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;

class MthRefTest extends IntegrationTest {

	@Test
	void resolvesSharedReferenceToTheSameMethodNode() {
		disableCompilation();
		ClassNode cls = getClassNode(MthRefTest.class);
		MethodNode target = cls.searchMethodByShortId("target()V");
		assertThat(target).isNotNull();

		MthRef ref = new MthRef(cls.getRawName(), target.getMethodInfo().getShortId());
		assertThat(ref.resolve(cls.root())).isSameAs(target);
		assertThat(ref.resolve(cls.root())).isSameAs(target);
	}

	private void target() {
	}
}
