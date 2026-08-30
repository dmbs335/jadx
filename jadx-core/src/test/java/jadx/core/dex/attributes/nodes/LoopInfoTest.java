package jadx.core.dex.attributes.nodes;

import java.util.Set;

import org.junit.jupiter.api.Test;

import jadx.core.dex.attributes.AFlag;
import jadx.core.dex.nodes.BlockNode;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

class LoopInfoTest {

	@Test
	void testIgnoreRemovedExitPath() {
		BlockNode loopBlock = new BlockNode(0, 0, 0);
		BlockNode removedBlock = new BlockNode(1, 1, 1);
		removedBlock.add(AFlag.REMOVE);
		loopBlock.getSuccessors().add(removedBlock);

		LoopInfo loop = new LoopInfo(loopBlock, loopBlock, Set.of(loopBlock));

		assertThat(loop.getExitNodes()).isEmpty();
		assertThat(loop.getExitEdges()).isEmpty();
	}
}
