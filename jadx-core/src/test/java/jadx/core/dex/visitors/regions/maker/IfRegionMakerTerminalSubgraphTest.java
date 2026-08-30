package jadx.core.dex.visitors.regions.maker;

import org.junit.jupiter.api.Test;

import jadx.core.dex.instructions.InsnType;
import jadx.core.dex.nodes.BlockNode;
import jadx.core.dex.nodes.InsnNode;

import static org.assertj.core.api.Assertions.assertThat;

class IfRegionMakerTerminalSubgraphTest {

	@Test
	void acceptDiamondWithTerminalLeaves() {
		BlockNode start = block(0);
		BlockNode left = block(1);
		BlockNode right = block(2);
		BlockNode exit = exitBlock(3);
		connect(start, left);
		connect(start, right);
		connect(left, exit);
		connect(right, exit);

		assertThat(IfRegionMaker.isAcyclicTerminalSubgraph(start, 12)).isTrue();
	}

	@Test
	void rejectNonTerminalBranchAndCycle() {
		BlockNode branch = block(0);
		connect(branch, exitBlock(1));
		connect(branch, block(2));
		assertThat(IfRegionMaker.isAcyclicTerminalSubgraph(branch, 12)).isFalse();

		BlockNode first = block(0);
		BlockNode second = block(1);
		connect(first, second);
		connect(second, first);
		assertThat(IfRegionMaker.isAcyclicTerminalSubgraph(first, 12)).isFalse();
	}

	@Test
	void preserveDepthLimit() {
		BlockNode start = block(0);
		BlockNode exit = exitBlock(1);
		connect(start, exit);

		assertThat(IfRegionMaker.isAcyclicTerminalSubgraph(start, 1)).isFalse();
		assertThat(IfRegionMaker.isAcyclicTerminalSubgraph(start, 2)).isTrue();
	}

	@Test
	void clearPooledStateBetweenCalls() {
		assertThat(IfRegionMaker.isAcyclicTerminalSubgraph(exitBlock(0), 12)).isTrue();
		assertThat(IfRegionMaker.isAcyclicTerminalSubgraph(block(0), 12)).isFalse();
	}

	private static BlockNode block(int pos) {
		BlockNode block = new BlockNode(pos, pos, pos);
		block.updateCleanSuccessors();
		return block;
	}

	private static BlockNode exitBlock(int pos) {
		BlockNode block = block(pos);
		block.getInstructions().add(new InsnNode(InsnType.RETURN, 0));
		return block;
	}

	private static void connect(BlockNode source, BlockNode target) {
		source.getSuccessors().add(target);
		source.updateCleanSuccessors();
		target.getPredecessors().add(source);
	}
}
