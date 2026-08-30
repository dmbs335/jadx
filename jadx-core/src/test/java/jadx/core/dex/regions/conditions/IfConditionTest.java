package jadx.core.dex.regions.conditions;

import org.junit.jupiter.api.Test;

import jadx.core.dex.instructions.IfNode;
import jadx.core.dex.instructions.IfOp;
import jadx.core.dex.instructions.args.ArgType;
import jadx.core.dex.instructions.args.InsnArg;
import jadx.core.dex.instructions.args.LiteralArg;

import static org.assertj.core.api.Assertions.assertThat;

class IfConditionTest {

	@Test
	void invertMustNotMutateSharedIfNode() {
		ArgType booleanType = ArgType.BOOLEAN;
		IfNode sharedIf = new IfNode(
				IfOp.EQ,
				-1,
				InsnArg.reg(0, booleanType),
				LiteralArg.litFalse());
		IfCondition first = IfCondition.fromIfNode(sharedIf);
		IfCondition second = IfCondition.fromIfNode(sharedIf);

		IfCondition inverted = IfCondition.invert(IfCondition.copyForSharedView(first));

		assertThat(sharedIf.getOp()).isEqualTo(IfOp.EQ);
		assertThat(first.getCompare().getOp()).isEqualTo(IfOp.EQ);
		assertThat(second.getCompare().getOp()).isEqualTo(IfOp.EQ);
		assertThat(inverted.getCompare().getOp()).isEqualTo(IfOp.NE);
	}
}
