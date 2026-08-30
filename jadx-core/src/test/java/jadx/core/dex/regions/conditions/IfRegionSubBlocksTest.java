package jadx.core.dex.regions.conditions;

import java.util.List;

import org.junit.jupiter.api.Test;

import jadx.core.dex.nodes.BlockNode;
import jadx.core.dex.nodes.IContainer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IfRegionSubBlocksTest {

	@Test
	void subBlocksViewTracksRegionUpdatesAndRemainsReadOnly() {
		IfRegion ifRegion = new IfRegion(null);
		List<IContainer> subBlocks = ifRegion.getSubBlocks();
		assertThat(ifRegion.getSubBlocks()).isSameAs(subBlocks);
		assertThat(subBlocks).isEmpty();

		BlockNode condition1 = block(1);
		BlockNode condition2 = block(2);
		BlockNode thenBlock = block(3);
		BlockNode elseBlock = block(4);
		ifRegion.updateCondition(null, List.of(condition1, condition2));
		ifRegion.setThenRegion(thenBlock);
		ifRegion.setElseRegion(elseBlock);
		assertThat(subBlocks).containsExactly(condition1, condition2, thenBlock, elseBlock);

		ifRegion.invert();
		assertThat(subBlocks).containsExactly(condition1, condition2, elseBlock, thenBlock);
		assertThatThrownBy(() -> subBlocks.add(block(5)))
				.isInstanceOf(UnsupportedOperationException.class);
	}

	private static BlockNode block(int id) {
		return new BlockNode(id, id, id);
	}
}
