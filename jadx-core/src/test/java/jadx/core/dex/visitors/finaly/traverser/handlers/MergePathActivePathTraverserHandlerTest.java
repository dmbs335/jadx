package jadx.core.dex.visitors.finaly.traverser.handlers;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import jadx.core.dex.nodes.BlockNode;

import static org.assertj.core.api.Assertions.assertThat;

class MergePathActivePathTraverserHandlerTest {

	@Test
	void generatesPermutationsWithinBudget() {
		List<BlockNode> roots = roots(4);

		assertThat(MergePathActivePathTraverserHandler.getAllPermutationsOfCollection(roots))
				.hasSize(24);
		assertThat(roots).hasSize(4);
	}

	@Test
	void skipsFactorialExplosionAboveBudget() {
		List<BlockNode> roots = roots(9);

		assertThat(MergePathActivePathTraverserHandler.getAllPermutationsOfCollection(roots))
				.isEmpty();
		assertThat(roots).hasSize(9);
	}

	private static List<BlockNode> roots(int count) {
		List<BlockNode> roots = new ArrayList<>(count);
		for (int i = 0; i < count; i++) {
			roots.add(new BlockNode(i, i, i));
		}
		return roots;
	}
}
