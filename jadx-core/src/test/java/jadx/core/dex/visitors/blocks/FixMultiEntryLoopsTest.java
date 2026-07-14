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
		connect(header, loopEnd);
		SpecialEdgeAttr backEdge = new SpecialEdgeAttr(SpecialEdgeType.BACK_EDGE, loopEnd, header);

		assertThat(FixMultiEntryLoops.isMultiEntryLoop(backEdge)).isTrue();
		loopEnd.add(AFlag.EXC_BOTTOM_SPLITTER);
		assertThat(FixMultiEntryLoops.isMultiEntryLoop(backEdge)).isFalse();
	}

	@Test
	void testDetectCycleThroughExceptionSplitterForStructuralSplit() {
		BlockNode header = block(0);
		BlockNode loopEnd = block(1);
		BlockNode exceptionSplitter = block(2);
		BlockNode handler = block(3);
		header.getPredecessors().add(block(4));
		header.getPredecessors().add(loopEnd);
		exceptionSplitter.add(AFlag.EXC_BOTTOM_SPLITTER);
		connect(header, exceptionSplitter);
		connect(exceptionSplitter, handler);
		connect(handler, loopEnd);
		SpecialEdgeAttr backEdge = new SpecialEdgeAttr(SpecialEdgeType.BACK_EDGE, loopEnd, header);

		assertThat(FixMultiEntryLoops.isMultiEntryLoop(backEdge)).isTrue();
		assertThat(FixMultiEntryLoops.isExceptionOnlyCycle(backEdge)).isTrue();

		BlockNode normalPath = block(5);
		connect(header, normalPath);
		connect(normalPath, loopEnd);
		assertThat(FixMultiEntryLoops.isMultiEntryLoop(backEdge)).isTrue();
		assertThat(FixMultiEntryLoops.isExceptionOnlyCycle(backEdge)).isFalse();
	}

	private static void connect(BlockNode source, BlockNode target) {
		source.getSuccessors().add(target);
		target.getPredecessors().add(source);
	}

	private static BlockNode block(int id) {
		return new BlockNode(id, id, id);
	}
}
