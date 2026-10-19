package jadx.core.dex.nodes;

import org.junit.jupiter.api.Test;

import jadx.core.dex.instructions.ArithNode;
import jadx.core.dex.instructions.ArithOp;
import jadx.core.dex.instructions.InsnType;
import jadx.core.dex.instructions.args.ArgType;
import jadx.core.dex.instructions.args.InsnArg;

import static org.assertj.core.api.Assertions.assertThat;

class InsnNodeReorderTest {
	@Test
	void throwingExpressionsMustNotMoveAcrossObservableOperations() {
		assertThat(new InsnNode(InsnType.CHECK_CAST, 1).canReorder()).isFalse();
		assertThat(arith(ArithOp.DIV).canReorder()).isFalse();
		assertThat(arith(ArithOp.REM).canReorder()).isFalse();
		assertThat(new InsnNode(InsnType.NEW_ARRAY, 1).canReorder()).isFalse();
		assertThat(new InsnNode(InsnType.FILL_ARRAY, 1).canReorder()).isFalse();
		assertThat(new InsnNode(InsnType.STR_CONCAT, 1).canReorder()).isFalse();
		assertThat(arith(ArithOp.ADD).canReorder()).isTrue();
		assertThat(new InsnNode(InsnType.CAST, 1).canReorder()).isTrue();
	}

	private static ArithNode arith(ArithOp op) {
		return new ArithNode(op, null, InsnArg.lit(10, ArgType.INT), InsnArg.lit(2, ArgType.INT));
	}
}
