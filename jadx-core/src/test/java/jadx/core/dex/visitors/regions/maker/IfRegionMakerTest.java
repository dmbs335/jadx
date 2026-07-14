package jadx.core.dex.visitors.regions.maker;

import org.junit.jupiter.api.Test;

import jadx.core.dex.nodes.BlockNode;

import static org.assertj.core.api.Assertions.assertThat;

class IfRegionMakerTest {

	@Test
	void acceptBranchStartAsJoinWithoutInspectingTerminalPaths() {
		BlockNode thenBlock = new BlockNode(1, 0, 0);
		BlockNode elseBlock = new BlockNode(2, 1, 1);

		assertThat(IfRegionMaker.isCommonPostDominator(null, thenBlock, elseBlock, thenBlock)).isTrue();
		assertThat(IfRegionMaker.isCommonPostDominator(null, thenBlock, elseBlock, elseBlock)).isTrue();
	}
}
