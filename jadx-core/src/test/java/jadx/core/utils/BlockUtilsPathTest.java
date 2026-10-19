package jadx.core.utils;

import java.util.List;

import org.junit.jupiter.api.Test;

import jadx.core.dex.instructions.InsnType;
import jadx.core.dex.nodes.BlockNode;
import jadx.core.dex.nodes.InsnNode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BlockUtilsPathTest {
	@Test
	void findNonEmptyPredecessorsThroughEmptyBranches() {
		BlockNode first = block(0);
		BlockNode second = block(1);
		BlockNode firstEmpty = block(2);
		BlockNode secondEmpty = block(3);
		BlockNode join = block(4);
		first.getInstructions().add(new InsnNode(InsnType.CONST, 0));
		second.getInstructions().add(new InsnNode(InsnType.CONST, 0));
		connect(first, firstEmpty);
		connect(second, secondEmpty);
		connect(firstEmpty, join);
		connect(secondEmpty, join);

		assertThat(BlockUtils.followEmptyUpPathWithinSet(join,
				List.of(first, second, firstEmpty, secondEmpty))).containsExactlyInAnyOrder(first, second);
	}

	@Test
	void reuseClearedVisitedSet() {
		BlockNode start = block(0);
		BlockNode middle = block(1);
		BlockNode end = block(2);
		connect(start, middle);
		connect(middle, end);

		assertThat(BlockUtils.isPathExists(start, end)).isTrue();
		assertThat(BlockUtils.isPathExists(start, end)).isTrue();
	}

	@Test
	void supportReentrantPathSearch() {
		BlockNode start = block(0);
		BlockNode middle = block(1);
		BlockNode end = block(2);
		connect(start, middle);
		connect(middle, end);

		BlockNode nestedStart = block(10);
		BlockNode nestedMiddle = block(1);
		BlockNode nestedEnd = block(12);
		connect(nestedStart, nestedMiddle);
		connect(nestedMiddle, nestedEnd);

		assertThat(BlockUtils.isPathExists(start, end, block -> {
			assertThat(BlockUtils.isAnyPathExists(nestedStart, nestedEnd)).isTrue();
			return true;
		})).isTrue();
	}

	@Test
	void releaseVisitedSetAfterPredicateFailure() {
		BlockNode start = block(0);
		BlockNode middle = block(1);
		BlockNode end = block(2);
		connect(start, middle);
		connect(middle, end);

		assertThatThrownBy(() -> BlockUtils.isPathExists(start, end, block -> {
			if (block == end) {
				throw new IllegalStateException("expected");
			}
			return true;
		})).isInstanceOf(IllegalStateException.class);
		assertThat(BlockUtils.isPathExists(start, end)).isTrue();
	}

	@Test
	void invalidateScopedResultAfterSuccessorChange() throws Exception {
		BlockNode start = block(0);
		BlockNode middle = block(1);
		BlockNode end = block(2);
		connect(start, middle);
		connect(middle, end);

		try (AutoCloseable ignored = BlockUtils.enterPathCache()) {
			assertThat(BlockUtils.isPathExists(start, end)).isTrue();
			middle.getSuccessors().clear();
			middle.updateCleanSuccessors();
			BlockUtils.invalidatePathCache();
			assertThat(BlockUtils.isPathExists(start, end)).isFalse();
		}
	}

	@Test
	void restoreOuterPathCacheAfterNestedScope() throws Exception {
		BlockNode start = block(0);
		BlockNode middle = block(1);
		BlockNode end = block(2);
		connect(start, middle);
		connect(middle, end);

		try (AutoCloseable ignored = BlockUtils.enterPathCache()) {
			assertThat(BlockUtils.isPathExists(start, end)).isTrue();
			try (AutoCloseable nested = BlockUtils.enterPathCache()) {
				assertThat(BlockUtils.isPathExists(start, end)).isTrue();
			}
			assertThat(BlockUtils.isPathExists(start, end)).isTrue();
		}
	}

	private static BlockNode block(int pos) {
		return new BlockNode(pos, pos, pos);
	}

	private static void connect(BlockNode source, BlockNode target) {
		source.getSuccessors().add(target);
		source.updateCleanSuccessors();
		target.getPredecessors().add(source);
		target.updateCleanSuccessors();
	}
}
