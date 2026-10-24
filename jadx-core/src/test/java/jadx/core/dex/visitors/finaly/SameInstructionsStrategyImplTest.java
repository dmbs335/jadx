package jadx.core.dex.visitors.finaly;

import org.junit.jupiter.api.Test;

import jadx.core.dex.instructions.IndexInsnNode;
import jadx.core.dex.instructions.InsnType;
import jadx.core.dex.instructions.args.ArgType;
import jadx.core.dex.instructions.args.InsnArg;
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

	private static InsnNode makeReturnWithCast(int value) {
		IndexInsnNode cast = new IndexInsnNode(InsnType.CAST, ArgType.INT, 1);
		cast.addArg(InsnArg.lit(value, ArgType.INT));
		InsnNode ret = new InsnNode(InsnType.RETURN, 1);
		ret.addArg(InsnArg.wrapArg(cast));
		return ret;
	}
}
