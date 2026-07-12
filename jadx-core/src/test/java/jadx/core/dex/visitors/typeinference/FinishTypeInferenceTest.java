package jadx.core.dex.visitors.typeinference;

import org.junit.jupiter.api.Test;

import jadx.core.dex.attributes.AFlag;
import jadx.core.dex.instructions.InsnType;
import jadx.core.dex.instructions.args.ArgType;
import jadx.core.dex.instructions.args.InsnArg;
import jadx.core.dex.instructions.args.RegisterArg;
import jadx.core.dex.instructions.args.SSAVar;
import jadx.core.dex.nodes.InsnNode;

import static org.assertj.core.api.Assertions.assertThat;

class FinishTypeInferenceTest {

	@Test
	void ignoreUseInNonGeneratedInsn() {
		SSAVar var = makeVarWithUse(true);

		assertThat(FinishTypeInference.hasGeneratedUse(var)).isFalse();
	}

	@Test
	void retainUseInGeneratedInsn() {
		SSAVar var = makeVarWithUse(false);

		assertThat(FinishTypeInference.hasGeneratedUse(var)).isTrue();
	}

	private static SSAVar makeVarWithUse(boolean dontGenerate) {
		RegisterArg assign = InsnArg.reg(0, ArgType.UNKNOWN);
		SSAVar var = new SSAVar(0, 0, assign);
		RegisterArg use = InsnArg.reg(0, ArgType.UNKNOWN);
		var.use(use);
		InsnNode useInsn = new InsnNode(InsnType.MOVE, 1);
		useInsn.addArg(use);
		if (dontGenerate) {
			useInsn.add(AFlag.DONT_GENERATE);
		}
		return var;
	}
}
