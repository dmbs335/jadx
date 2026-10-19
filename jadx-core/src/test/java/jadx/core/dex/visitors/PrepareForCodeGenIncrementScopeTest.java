package jadx.core.dex.visitors;

import org.junit.jupiter.api.Test;

import jadx.core.dex.instructions.IfNode;
import jadx.core.dex.instructions.IfOp;
import jadx.core.dex.instructions.args.ArgType;
import jadx.core.dex.instructions.args.InsnArg;
import jadx.core.dex.nodes.BlockNode;
import jadx.core.dex.regions.Region;
import jadx.core.dex.regions.conditions.IfRegion;

import static org.assertj.core.api.Assertions.assertThat;

class PrepareForCodeGenIncrementScopeTest {

	private static BlockNode conditionBlock() {
		BlockNode block = new BlockNode(1, 1, 1);
		block.getInstructions().add(new IfNode(IfOp.LT, -1,
				InsnArg.reg(0, ArgType.INT), InsnArg.lit(0, ArgType.INT)));
		return block;
	}

	@Test
	void acceptsFollowingIfInSameScope() {
		Region root = new Region(null);
		BlockNode increment = new BlockNode(0, 0, 0);
		BlockNode condition = conditionBlock();
		IfRegion branch = new IfRegion(root);
		branch.updateCondition(condition);
		root.add(increment);
		root.add(branch);
		assertThat(PrepareForCodeGen.hasFollowingIfScope(root, increment, condition)).isTrue();
		branch.markConditionShared();
		assertThat(PrepareForCodeGen.hasFollowingIfScope(root, increment, condition)).isFalse();
	}

	@Test
	void acceptsFirstIfInsidePlainRegionWrappers() {
		Region root = new Region(null);
		BlockNode increment = new BlockNode(0, 0, 0);
		BlockNode condition = conditionBlock();
		Region wrapper = new Region(root);
		Region nested = new Region(wrapper);
		IfRegion branch = new IfRegion(nested);
		branch.updateCondition(condition);
		nested.add(branch);
		nested.add(new BlockNode(2, 2, 2));
		wrapper.add(nested);
		root.add(increment);
		root.add(wrapper);
		assertThat(PrepareForCodeGen.hasFollowingIfScope(root, increment, condition)).isTrue();
		// A preceding statement means this is no longer the immediately following IF.
		nested.getSubBlocks().add(0, new BlockNode(3, 3, 3));
		assertThat(PrepareForCodeGen.hasFollowingIfScope(root, increment, condition)).isFalse();
	}

	@Test
	void rejectsEmptyWrapper() {
		Region root = new Region(null);
		BlockNode increment = new BlockNode(0, 0, 0);
		root.add(increment);
		root.add(new Region(root));
		assertThat(PrepareForCodeGen.hasFollowingIfScope(root, increment, new BlockNode(1, 1, 1))).isFalse();
	}

	@Test
	void rejectsVariableDeclaredInsidePreviousBranch() {
		Region root = new Region(null);
		BlockNode increment = new BlockNode(0, 0, 0);
		Region thenRegion = new Region(root);
		thenRegion.add(increment);
		IfRegion previous = new IfRegion(root);
		previous.setThenRegion(thenRegion);
		BlockNode condition = conditionBlock();
		IfRegion branch = new IfRegion(root);
		branch.updateCondition(condition);
		root.add(previous);
		root.add(branch);
		assertThat(PrepareForCodeGen.hasFollowingIfScope(root, increment, condition)).isFalse();
	}

	@Test
	void rejectsBareConditionAndMissingIncrement() {
		Region root = new Region(null);
		BlockNode increment = new BlockNode(0, 0, 0);
		BlockNode condition = conditionBlock();
		root.add(increment);
		root.add(condition);
		assertThat(PrepareForCodeGen.hasFollowingIfScope(root, increment, condition)).isFalse();
		assertThat(PrepareForCodeGen.hasFollowingIfScope(root, new BlockNode(2, 2, 2), condition)).isFalse();
	}

	@Test
	void rejectsEarlierCondition() {
		Region root = new Region(null);
		BlockNode increment = new BlockNode(0, 0, 0);
		BlockNode condition = conditionBlock();
		IfRegion branch = new IfRegion(root);
		branch.updateCondition(condition);
		root.add(branch);
		root.add(increment);
		assertThat(PrepareForCodeGen.hasFollowingIfScope(root, increment, condition)).isFalse();
	}
}
