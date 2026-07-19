package jadx.core.dex.visitors.regions.maker;

import org.junit.jupiter.api.Test;

import jadx.core.dex.nodes.BlockNode;

import static jadx.core.dex.visitors.blocks.BlockSplitter.connect;
import static org.assertj.core.api.Assertions.assertThat;

class IfRegionMakerTest {

	@Test
	void acceptBranchStartAsJoinWithoutInspectingTerminalPaths() {
		BlockNode thenBlock = new BlockNode(1, 0, 0);
		BlockNode elseBlock = new BlockNode(2, 1, 1);

		assertThat(IfRegionMaker.isCommonPostDominator(thenBlock, elseBlock, thenBlock)).isTrue();
		assertThat(IfRegionMaker.isCommonPostDominator(thenBlock, elseBlock, elseBlock)).isTrue();
	}

	@Test
	void identifyDirectBranchJoin() {
		BlockNode thenBlock = new BlockNode(1, 0, 0);
		BlockNode elseBlock = new BlockNode(2, 1, 1);
		BlockNode laterJoin = new BlockNode(3, 2, 2);

		assertThat(IfRegionMaker.isDirectBranchJoin(thenBlock, thenBlock, elseBlock)).isTrue();
		assertThat(IfRegionMaker.isDirectBranchJoin(elseBlock, thenBlock, elseBlock)).isTrue();
		assertThat(IfRegionMaker.isDirectBranchJoin(laterJoin, thenBlock, elseBlock)).isFalse();
		assertThat(IfRegionMaker.isDirectBranchJoin(null, thenBlock, elseBlock)).isFalse();
	}

	@Test
	void distinguishCommonJoinFromPartiallyBypassedCandidate() {
		BlockNode thenBlock = new BlockNode(1, 0, 0);
		BlockNode elseBlock = new BlockNode(2, 1, 1);
		BlockNode bypassedCandidate = new BlockNode(3, 2, 2);
		BlockNode commonJoin = new BlockNode(4, 3, 3);
		connect(thenBlock, bypassedCandidate);
		connect(thenBlock, commonJoin);
		connect(elseBlock, bypassedCandidate);
		connect(bypassedCandidate, commonJoin);
		thenBlock.updateCleanSuccessors();
		elseBlock.updateCleanSuccessors();
		bypassedCandidate.updateCleanSuccessors();
		commonJoin.updateCleanSuccessors();

		assertThat(IfRegionMaker.isCommonPostDominator(thenBlock, elseBlock, commonJoin)).isTrue();
		assertThat(IfRegionMaker.isCommonPostDominator(thenBlock, elseBlock, bypassedCandidate)).isFalse();
	}
}
