package jadx.core.dex.visitors.ssa;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

import org.junit.jupiter.api.Test;

import jadx.core.dex.instructions.InsnType;
import jadx.core.dex.instructions.args.ArgType;
import jadx.core.dex.instructions.args.InsnArg;
import jadx.core.dex.instructions.args.RegisterArg;
import jadx.core.dex.instructions.args.SSAVar;
import jadx.core.dex.nodes.InsnNode;

import static org.assertj.core.api.Assertions.assertThat;

class SSATransformTest {

	@Test
	void detectDeadMoveChain() {
		SSAVar source = makeVar(0);
		SSAVar intermediate = addMoveUse(source, 1);
		addMoveUse(intermediate, 2);

		assertThat(isUsedOnlyInDeadMoves(source)).isTrue();
	}

	@Test
	void retainWarningForLiveMoveChain() {
		SSAVar source = makeVar(0);
		SSAVar terminal = addMoveUse(source, 1);
		addUse(terminal, InsnType.RETURN);

		assertThat(isUsedOnlyInDeadMoves(source)).isFalse();
	}

	@Test
	void retainWarningForDirectNonMoveUse() {
		SSAVar source = makeVar(0);
		addUse(source, InsnType.RETURN);

		assertThat(isUsedOnlyInDeadMoves(source)).isFalse();
	}

	private static boolean isUsedOnlyInDeadMoves(SSAVar var) {
		Set<SSAVar> visited = Collections.newSetFromMap(new IdentityHashMap<>());
		return SSATransform.isUsedOnlyInDeadMoves(var, visited);
	}

	private static SSAVar makeVar(int regNum) {
		return new SSAVar(regNum, 0, InsnArg.reg(regNum, ArgType.UNKNOWN));
	}

	private static SSAVar addMoveUse(SSAVar source, int resultReg) {
		InsnNode move = addUse(source, InsnType.MOVE);
		RegisterArg result = InsnArg.reg(resultReg, ArgType.UNKNOWN);
		move.setResult(result);
		return new SSAVar(resultReg, 0, result);
	}

	private static InsnNode addUse(SSAVar var, InsnType insnType) {
		RegisterArg use = InsnArg.reg(var.getRegNum(), ArgType.UNKNOWN);
		var.use(use);
		InsnNode insn = new InsnNode(insnType, 1);
		insn.addArg(use);
		return insn;
	}
}
