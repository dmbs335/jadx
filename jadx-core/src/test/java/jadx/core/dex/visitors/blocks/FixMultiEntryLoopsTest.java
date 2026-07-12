package jadx.core.dex.visitors.blocks;

import org.junit.jupiter.api.Test;

import jadx.core.dex.attributes.AFlag;
import jadx.core.dex.attributes.nodes.SpecialEdgeAttr;
import jadx.core.dex.attributes.nodes.SpecialEdgeAttr.SpecialEdgeType;
import jadx.core.dex.nodes.BlockNode;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

class FixMultiEntryLoopsTest {

	@Test
	void testIgnoreExceptionHandlerBackEdge() {
		BlockNode header = block(0);
		BlockNode loopEnd = block(1);
		header.getPredecessors().add(block(2));
		header.getPredecessors().add(loopEnd);
		SpecialEdgeAttr backEdge = new SpecialEdgeAttr(SpecialEdgeType.BACK_EDGE, loopEnd, header);

		assertThat(FixMultiEntryLoops.isMultiEntryLoop(backEdge)).isTrue();
		loopEnd.add(AFlag.EXC_BOTTOM_SPLITTER);
		assertThat(FixMultiEntryLoops.isMultiEntryLoop(backEdge)).isFalse();
	}

	private static BlockNode block(int id) {
		return new BlockNode(id, id, id);
	}
}
