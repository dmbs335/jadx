package jadx.core.dex.visitors.regions.variables;

import java.util.List;

import org.junit.jupiter.api.Test;

import jadx.core.dex.nodes.BlockNode;
import jadx.core.dex.regions.Region;

import static org.assertj.core.api.Assertions.assertThat;

class ProcessVariablesTest {

	@Test
	void checksUsePlacesAgainstSiblingOrderWithoutAllocatingASet() {
		Region root = new Region(null);
		BlockNode before = new BlockNode(0, 0, 0);
		BlockNode check = new BlockNode(1, 1, 1);
		BlockNode after = new BlockNode(2, 2, 2);
		Region nestedAfter = new Region(root);
		BlockNode nestedBlock = new BlockNode(3, 3, 3);
		nestedAfter.getSubBlocks().add(nestedBlock);
		root.getSubBlocks().addAll(List.of(before, check, after, nestedAfter));

		UsePlace checkPlace = new UsePlace(root, check);
		assertThat(ProcessVariables.isAllUseAfter(checkPlace, List.of())).isTrue();
		assertThat(ProcessVariables.isAllUseAfter(
				checkPlace,
				List.of(new UsePlace(root, check), new UsePlace(root, after), new UsePlace(nestedAfter, nestedBlock))))
				.isTrue();
		assertThat(ProcessVariables.isAllUseAfter(checkPlace, List.of(new UsePlace(root, before)))).isFalse();
	}

	@Test
	void rejectsCheckBlockOutsideRegion() {
		Region root = new Region(null);
		BlockNode check = new BlockNode(0, 0, 0);

		assertThat(ProcessVariables.isAllUseAfter(new UsePlace(root, check), List.of())).isFalse();
	}
}
