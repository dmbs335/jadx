package jadx.core.dex.visitors.regions;

import org.junit.jupiter.api.Test;

import jadx.core.dex.instructions.IfNode;
import jadx.core.dex.instructions.IfOp;
import jadx.core.dex.instructions.InsnType;
import jadx.core.dex.instructions.args.ArgType;
import jadx.core.dex.instructions.args.CodeVar;
import jadx.core.dex.instructions.args.InsnArg;
import jadx.core.dex.instructions.args.LiteralArg;
import jadx.core.dex.instructions.args.RegisterArg;
import jadx.core.dex.instructions.args.SSAVar;
import jadx.core.dex.instructions.mods.TernaryInsn;
import jadx.core.dex.regions.conditions.IfCondition;

import static org.assertj.core.api.Assertions.assertThat;

class TernaryModTest {

	@Test
	void normalizeDynamicBooleanNumericBranch() {
		RegisterArg result = register(0, ArgType.UNKNOWN, new CodeVar());
		RegisterArg intArg = register(1, ArgType.INT, codeVar(ArgType.INT));
		RegisterArg booleanArg = register(2, ArgType.BOOLEAN, codeVar(ArgType.BOOLEAN));

		IfNode ifNode = new IfNode(IfOp.NE, -1, intArg.duplicate(), LiteralArg.make(0, ArgType.INT));
		TernaryInsn ternary = new TernaryInsn(
				IfCondition.fromIfNode(ifNode), result, intArg.duplicate(), booleanArg.duplicate());

		TernaryMod.normalizeDynamicBooleanNumericBranches(ternary);

		assertThat(result.getSVar().getCodeVar().getType()).isEqualTo(ArgType.INT);
		assertThat(ternary.getArg(1).isInsnWrap()).isTrue();
		assertThat(ternary.getArg(1).unwrap().getType()).isEqualTo(InsnType.TERNARY);
		assertThat(ternary.getArg(1).getType()).isEqualTo(ArgType.INT);
	}

	private static CodeVar codeVar(ArgType type) {
		CodeVar codeVar = new CodeVar();
		codeVar.setType(type);
		return codeVar;
	}

	private static RegisterArg register(int regNum, ArgType type, CodeVar codeVar) {
		RegisterArg arg = InsnArg.reg(regNum, type);
		SSAVar var = new SSAVar(regNum, 0, arg);
		var.setCodeVar(codeVar);
		return arg;
	}
}
