package jadx.core.dex.visitors.regions.maker;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import jadx.core.dex.attributes.nodes.LoopInfo;
import jadx.core.dex.nodes.BlockNode;
import jadx.core.dex.trycatch.ExceptionHandler;

import static jadx.core.dex.visitors.blocks.BlockSplitter.connect;
import static org.assertj.core.api.Assertions.assertThat;

class LoopConditionScopeTest {
	@Test
	void keepsHandlerOnlyRetryPredicateInsideCatch() {
		BlockNode start = block(0);
		BlockNode condition = block(1);
		ExceptionHandler handler = handler(condition);
		LoopInfo loop = new LoopInfo(start, condition, Set.of(start, condition));
		assertThat(LoopRegionMaker.isHandlerOnlyCondition(List.of(handler), condition, loop)).isTrue();
	}

	@Test
	void allowsLoopConditionReachableNormallyInsideHandler() {
		BlockNode start = block(0);
		BlockNode condition = block(1);
		connect(start, condition);
		start.updateCleanSuccessors();
		ExceptionHandler handler = handler(condition);
		handler.getBlocks().add(start);
		LoopInfo loop = new LoopInfo(start, condition, Set.of(start, condition));
		assertThat(LoopRegionMaker.isHandlerOnlyCondition(List.of(handler), condition, loop)).isFalse();
	}

	@Test
	void doesNotChangeOrdinaryConditions() {
		BlockNode start = block(0);
		BlockNode condition = block(1);
		LoopInfo loop = new LoopInfo(start, condition, Set.of(start, condition));
		assertThat(LoopRegionMaker.isHandlerOnlyCondition(List.of(), condition, loop)).isFalse();
		assertThat(LoopRegionMaker.isHandlerOnlyCondition(List.of(handler(block(2))), condition, loop)).isFalse();
	}

	private static ExceptionHandler handler(BlockNode block) {
		ExceptionHandler handler = ExceptionHandler.build(null, 0, null);
		handler.getBlocks().add(block);
		return handler;
	}

	private static BlockNode block(int id) {
		BlockNode block = new BlockNode(id, id, 0);
		block.updateCleanSuccessors();
		return block;
	}
}
