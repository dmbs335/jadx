package jadx.core.dex.visitors.finaly;

import org.junit.jupiter.api.Test;

import jadx.core.dex.instructions.IndexInsnNode;
import jadx.core.dex.instructions.InsnType;
import jadx.core.dex.instructions.args.ArgType;
import jadx.core.dex.instructions.args.CodeVar;
import jadx.core.dex.instructions.args.InsnArg;
import jadx.core.dex.instructions.args.RegisterArg;
import jadx.core.dex.instructions.args.SSAVar;
import jadx.core.dex.nodes.InsnNode;

import static org.assertj.core.api.Assertions.assertThat;

class SameInstructionsStrategyImplTest {
	@Test
	void rejectsDifferentConstantsInsideWrappedInstructions() {
		InsnNode first = makeReturnWithCast(1);
		InsnNode second = makeReturnWithCast(2);

		assertThat(new SameInstructionsStrategyImpl().sameInsns(first, second)).isFalse();
		assertThat(new SameInstructionsStrategyImpl().sameInsns(first, makeReturnWithCast(1))).isTrue();
	}

	@Test
	void rejectsDifferentNonConstantAssignments() {
		RegisterArg first = makeAssignedCast(1, 1);
		RegisterArg second = makeAssignedCast(2, 2);

		assertThat(new SameInstructionsStrategyImpl().isSameArgs(first, second)).isFalse();
		assertThat(new SameInstructionsStrategyImpl().isSameArgs(first, makeAssignedCast(3, 1))).isTrue();
		assertThat(new SameInstructionsStrategyImpl().isSameArgs(
				makeAssignedCast(4, 1, false), makeAssignedCast(5, 2, false))).isFalse();
		assertThat(new SameInstructionsStrategyImpl().isSameArgs(
				makeAssignedCastFromRegister(6, 1), makeAssignedCastFromRegister(7, 2))).isFalse();
	}

	@Test
	void doesNotAssumeCyclicAssignmentsAreEqual() {
		assertThat(new SameInstructionsStrategyImpl().isSameArgs(
				makeCyclicMove(20), makeCyclicMove(21))).isFalse();
	}

	private static RegisterArg makeCyclicMove(int regNum) {
		RegisterArg assign = InsnArg.reg(regNum, ArgType.INT);
		SSAVar ssaVar = new SSAVar(regNum, 0, assign);
		ssaVar.setCodeVar(new CodeVar());
		InsnNode move = new InsnNode(InsnType.MOVE, 1);
		move.setResult(assign);
		RegisterArg selfUse = assign.duplicate();
		ssaVar.use(selfUse);
		move.addArg(selfUse);
		RegisterArg outputUse = assign.duplicate();
		ssaVar.use(outputUse);
		return outputUse;
	}

	private static RegisterArg makeAssignedCastFromRegister(int regNum, int value) {
		RegisterArg inputAssign = InsnArg.reg(regNum + 10, ArgType.INT);
		SSAVar inputVar = new SSAVar(regNum + 10, 0, inputAssign);
		inputVar.setCodeVar(new CodeVar());
		InsnNode inputConst = new InsnNode(InsnType.CONST, 1);
		inputConst.setResult(inputAssign);
		inputConst.addArg(InsnArg.lit(value, ArgType.INT));

		RegisterArg inputUse = inputAssign.duplicate();
		inputVar.use(inputUse);
		RegisterArg assign = InsnArg.reg(regNum, ArgType.INT);
		SSAVar outputVar = new SSAVar(regNum, 0, assign);
		outputVar.setCodeVar(new CodeVar());
		IndexInsnNode cast = new IndexInsnNode(InsnType.CAST, ArgType.INT, 1);
		cast.setResult(assign);
		cast.addArg(inputUse);
		RegisterArg outputUse = assign.duplicate();
		outputVar.use(outputUse);
		return outputUse;
	}

	private static RegisterArg makeAssignedCast(int regNum, int value) {
		return makeAssignedCast(regNum, value, true);
	}

	private static RegisterArg makeAssignedCast(int regNum, int value, boolean attachCodeVar) {
		RegisterArg assign = InsnArg.reg(regNum, ArgType.INT);
		SSAVar ssaVar = new SSAVar(regNum, 0, assign);
		if (attachCodeVar) {
			ssaVar.setCodeVar(new CodeVar());
		}
		IndexInsnNode cast = new IndexInsnNode(InsnType.CAST, ArgType.INT, 1);
		cast.setResult(assign);
		cast.addArg(InsnArg.lit(value, ArgType.INT));
		RegisterArg use = assign.duplicate();
		ssaVar.use(use);
		return use;
	}

	private static InsnNode makeReturnWithCast(int value) {
		IndexInsnNode cast = new IndexInsnNode(InsnType.CAST, ArgType.INT, 1);
		cast.addArg(InsnArg.lit(value, ArgType.INT));
		InsnNode ret = new InsnNode(InsnType.RETURN, 1);
		ret.addArg(InsnArg.wrapArg(cast));
		return ret;
	}
}
