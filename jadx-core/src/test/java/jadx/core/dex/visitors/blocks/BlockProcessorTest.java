package jadx.core.dex.visitors.blocks;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import jadx.core.dex.attributes.AFlag;
import jadx.core.dex.instructions.IfNode;
import jadx.core.dex.instructions.IfOp;
import jadx.core.dex.instructions.InsnType;
import jadx.core.dex.instructions.args.ArgType;
import jadx.core.dex.instructions.args.InsnArg;
import jadx.core.dex.nodes.BlockNode;
import jadx.core.dex.nodes.InsnNode;
import jadx.core.dex.trycatch.CatchAttr;

import static org.assertj.core.api.Assertions.assertThat;

class BlockProcessorTest {
	@Test
	void protectsAssignmentsBeforeInstructionLevelTryEntry() {
		BlockNode block = makeBranchBlock(0, ArgType.INT);
		block.getInstructions().get(1).add(AFlag.TRY_ENTER);
		assertThat(BlockProcessor.hasProtectedInstructions(block)).isTrue();
	}

	@Test
	void protectsAssignmentsBeforeInstructionLevelCatchRange() {
		BlockNode block = makeBranchBlock(0, ArgType.INT);
		block.getInstructions().get(1).addAttr(CatchAttr.build(new ArrayList<>()));
		assertThat(BlockProcessor.hasProtectedInstructions(block)).isTrue();
	}

	@Test
	void detectsBlockLevelProtection() {
		BlockNode block = makeBranchBlock(0, ArgType.INT);
		block.addAttr(CatchAttr.build(new ArrayList<>()));
		assertThat(BlockProcessor.hasProtectedInstructions(block)).isTrue();
	}

	@Test
	void allowsUnprotectedLoopDeduplication() {
		BlockNode block = makeBranchBlock(0, ArgType.INT);
		block.add(AFlag.LOOP_START);
		assertThat(BlockProcessor.hasProtectedInstructions(block)).isFalse();
	}

	@Test
	void rejectEquivalentIfMergeAcrossPrimitiveAndReferenceLifetimes() {
		BlockNode booleanBlock = makeBranchBlock(0, ArgType.BOOLEAN);
		BlockNode referenceBlock = makeBranchBlock(1, ArgType.object("java.lang.Thread"));

		assertThat(BlockProcessor.haveCompatibleEquivalentIfInputTypes(
				booleanBlock, referenceBlock, getIf(booleanBlock), getIf(referenceBlock))).isFalse();
	}

	@Test
	void allowEquivalentIfMergeForCompatibleLifetimes() {
		BlockNode firstBlock = makeBranchBlock(0, ArgType.BOOLEAN);
		BlockNode secondBlock = makeBranchBlock(1, ArgType.BOOLEAN);

		assertThat(BlockProcessor.haveCompatibleEquivalentIfInputTypes(
				firstBlock, secondBlock, getIf(firstBlock), getIf(secondBlock))).isTrue();
	}

	private static BlockNode makeBranchBlock(int id, ArgType assignType) {
		BlockNode block = new BlockNode(id, id, id);
		InsnNode move = new InsnNode(InsnType.MOVE, 1);
		move.setResult(InsnArg.reg(5, assignType));
		move.addArg(InsnArg.reg(6, assignType));
		block.getInstructions().add(move);
		block.getInstructions().add(new IfNode(
				IfOp.EQ, -1, InsnArg.reg(5, ArgType.UNKNOWN), InsnArg.lit(0, ArgType.UNKNOWN)));
		return block;
	}

	private static IfNode getIf(BlockNode block) {
		return (IfNode) block.getInstructions().get(1);
	}
}
