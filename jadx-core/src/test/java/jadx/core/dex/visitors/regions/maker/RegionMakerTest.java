package jadx.core.dex.visitors.regions.maker;

import org.junit.jupiter.api.Test;

import jadx.core.dex.instructions.InsnType;
import jadx.core.dex.instructions.args.ArgType;
import jadx.core.dex.instructions.args.InsnArg;
import jadx.core.dex.nodes.BlockNode;
import jadx.core.dex.nodes.InsnNode;

import static org.assertj.core.api.Assertions.assertThat;

class RegionMakerTest {

	@Test
	void classifyLocalConstAndMoveAssignmentsAsSafeDuplication() {
		BlockNode block = makeConstBlock(ArgType.INT, ArgType.INT);
		InsnNode moveInsn = new InsnNode(InsnType.MOVE, 1);
		moveInsn.setResult(InsnArg.reg(1, ArgType.OBJECT));
		moveInsn.addArg(InsnArg.reg(2, ArgType.OBJECT));
		block.getInstructions().add(moveInsn);

		assertThat(RegionMaker.isSafeLocalAssignmentDuplication(block)).isTrue();
	}

	@Test
	void rejectNonLocalAssignmentDuplication() {
		InsnNode fieldPutInsn = new InsnNode(InsnType.IPUT, 2);
		fieldPutInsn.addArg(InsnArg.reg(0, ArgType.INT));
		fieldPutInsn.addArg(InsnArg.reg(1, ArgType.OBJECT));
		BlockNode block = new BlockNode(1, 0, 0);
		block.getInstructions().add(fieldPutInsn);

		assertThat(RegionMaker.isSafeLocalAssignmentDuplication(block)).isFalse();
	}

	private static BlockNode makeConstBlock(ArgType resultType, ArgType literalType) {
		InsnNode constInsn = new InsnNode(InsnType.CONST, 1);
		constInsn.setResult(InsnArg.reg(0, resultType));
		constInsn.addArg(InsnArg.lit(0, literalType));
		BlockNode block = new BlockNode(1, 0, 0);
		block.getInstructions().add(constInsn);
		return block;
	}

}
