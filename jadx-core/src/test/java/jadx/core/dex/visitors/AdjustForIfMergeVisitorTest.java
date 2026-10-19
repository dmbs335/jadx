package jadx.core.dex.visitors;

import org.junit.jupiter.api.Test;

import jadx.core.dex.instructions.IfNode;
import jadx.core.dex.instructions.IfOp;
import jadx.core.dex.instructions.InsnType;
import jadx.core.dex.instructions.args.ArgType;
import jadx.core.dex.instructions.args.InsnArg;
import jadx.core.dex.instructions.args.RegisterArg;
import jadx.core.dex.instructions.args.SSAVar;
import jadx.core.dex.nodes.BlockNode;
import jadx.core.dex.nodes.InsnNode;

import static org.assertj.core.api.Assertions.assertThat;

class AdjustForIfMergeVisitorTest {

	@Test
	void keepsMoveBeforeIfWhenIfReadsItsResult() {
		BlockNode moveBlock = new BlockNode(0, 0, 0);
		BlockNode ifBlock = new BlockNode(1, 1, 1);
		InsnNode move = new InsnNode(InsnType.MOVE, 1);
		RegisterArg result = InsnArg.reg(2, ArgType.INT);
		move.setResult(result);
		move.addArg(InsnArg.reg(2, ArgType.INT));
		moveBlock.getInstructions().add(move);
		SSAVar value = new SSAVar(2, 1, result);
		RegisterArg conditionArg = InsnArg.reg(2, ArgType.INT);
		IfNode condition = new IfNode(IfOp.EQ, -1, conditionArg, InsnArg.lit(0, ArgType.INT));
		value.use(conditionArg);
		ifBlock.getInstructions().add(condition);

		assertThat(AdjustForIfMergeVisitor.getMovableInstructions(moveBlock, ifBlock)).isEmpty();
	}

	@Test
	void keepsMoveBeforeIfWhenWrappedExpressionReadsItsResult() {
		BlockNode moveBlock = new BlockNode(0, 0, 0);
		BlockNode ifBlock = new BlockNode(1, 1, 1);
		InsnNode move = new InsnNode(InsnType.MOVE, 1);
		RegisterArg result = InsnArg.reg(2, ArgType.INT);
		move.setResult(result);
		move.addArg(InsnArg.reg(2, ArgType.INT));
		moveBlock.getInstructions().add(move);
		SSAVar value = new SSAVar(2, 1, result);
		InsnNode wrapped = new InsnNode(InsnType.ARITH, 1);
		RegisterArg wrappedArg = InsnArg.reg(2, ArgType.INT);
		wrapped.addArg(wrappedArg);
		value.use(wrappedArg);
		ifBlock.getInstructions().add(new IfNode(
				IfOp.EQ, -1, InsnArg.wrapArg(wrapped), InsnArg.lit(0, ArgType.INT)));

		assertThat(AdjustForIfMergeVisitor.getMovableInstructions(moveBlock, ifBlock)).isEmpty();
	}

	@Test
	void keepsMoveBeforeInstructionThatStillUsesItsResult() {
		BlockNode moveBlock = new BlockNode(0, 0, 0);
		BlockNode ifBlock = new BlockNode(1, 1, 1);
		InsnNode move = new InsnNode(InsnType.MOVE, 1);
		RegisterArg result = InsnArg.reg(2, ArgType.INT);
		move.setResult(result);
		move.addArg(InsnArg.reg(2, ArgType.INT));
		moveBlock.getInstructions().add(move);
		SSAVar value = new SSAVar(2, 1, result);
		InsnNode dependent = new InsnNode(InsnType.MOVE, 1);
		RegisterArg dependentResult = InsnArg.reg(3, ArgType.INT);
		dependent.setResult(dependentResult);
		new SSAVar(3, 1, dependentResult);
		RegisterArg dependentArg = InsnArg.reg(2, ArgType.INT);
		dependent.addArg(dependentArg);
		value.use(dependentArg);
		moveBlock.getInstructions().add(dependent);
		ifBlock.getInstructions().add(new IfNode(
				IfOp.EQ, -1, InsnArg.reg(4, ArgType.INT), InsnArg.lit(0, ArgType.INT)));

		assertThat(AdjustForIfMergeVisitor.getMovableInstructions(moveBlock, ifBlock)).isEmpty();
	}

	@Test
	void allowsMoveWhenNextIfDoesNotReadItsResult() {
		BlockNode moveBlock = new BlockNode(0, 0, 0);
		BlockNode ifBlock = new BlockNode(1, 1, 1);
		InsnNode move = new InsnNode(InsnType.MOVE, 1);
		RegisterArg result = InsnArg.reg(2, ArgType.INT);
		move.setResult(result);
		move.addArg(InsnArg.reg(2, ArgType.INT));
		moveBlock.getInstructions().add(move);
		new SSAVar(2, 1, result);
		ifBlock.getInstructions().add(new IfNode(
				IfOp.EQ, -1, InsnArg.reg(3, ArgType.INT), InsnArg.lit(0, ArgType.INT)));

		assertThat(AdjustForIfMergeVisitor.getMovableInstructions(moveBlock, ifBlock)).containsExactly(move);
	}
}
