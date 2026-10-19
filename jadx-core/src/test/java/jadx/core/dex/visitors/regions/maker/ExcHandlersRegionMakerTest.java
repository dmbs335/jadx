package jadx.core.dex.visitors.regions.maker;

import org.junit.jupiter.api.Test;

import jadx.core.dex.instructions.IfNode;
import jadx.core.dex.instructions.IfOp;
import jadx.core.dex.instructions.InsnType;
import jadx.core.dex.instructions.args.ArgType;
import jadx.core.dex.instructions.args.InsnArg;
import jadx.core.dex.nodes.BlockNode;
import jadx.core.dex.nodes.InsnNode;

import static jadx.core.dex.visitors.blocks.BlockSplitter.connect;
import static org.assertj.core.api.Assertions.assertThat;

class ExcHandlersRegionMakerTest {

	@Test
	void collectLinearHandlerPrefixThroughTerminalReturn() {
		BlockNode handlerTail = new BlockNode(1, 0, 0);
		handlerTail.getInstructions().add(new InsnNode(InsnType.INVOKE, 0));
		BlockNode terminal = new BlockNode(2, 0, 0);
		terminal.getInstructions().add(new InsnNode(InsnType.RETURN, 0));
		connect(handlerTail, terminal);
		handlerTail.updateCleanSuccessors();
		terminal.updateCleanSuccessors();

		assertThat(ExcHandlersRegionMaker.collectSharedHandlerPrefixBlocks(handlerTail))
				.containsExactly(handlerTail, terminal);
	}

	@Test
	void rejectBranchedHandlerPrefix() {
		BlockNode branch = new BlockNode(1, 0, 0);
		branch.getInstructions().add(new IfNode(
				IfOp.EQ, 0, InsnArg.reg(0, ArgType.INT), InsnArg.lit(0, ArgType.INT)));
		BlockNode first = new BlockNode(2, 0, 0);
		BlockNode second = new BlockNode(3, 0, 0);
		connect(branch, first);
		connect(branch, second);
		branch.updateCleanSuccessors();
		first.updateCleanSuccessors();
		second.updateCleanSuccessors();

		assertThat(ExcHandlersRegionMaker.collectSharedHandlerPrefixBlocks(branch)).isEmpty();
	}
}
