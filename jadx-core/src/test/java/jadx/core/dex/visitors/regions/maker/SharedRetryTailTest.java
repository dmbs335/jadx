package jadx.core.dex.visitors.regions.maker;

import java.util.ArrayList;
import java.util.Set;

import org.junit.jupiter.api.Test;

import jadx.core.dex.attributes.AFlag;
import jadx.core.dex.instructions.InsnType;
import jadx.core.dex.nodes.BlockNode;
import jadx.core.dex.nodes.InsnNode;
import jadx.core.dex.trycatch.CatchAttr;
import jadx.core.dex.trycatch.ExcHandlerAttr;
import jadx.core.dex.trycatch.ExceptionHandler;

import static jadx.core.dex.visitors.blocks.BlockSplitter.connect;
import static org.assertj.core.api.Assertions.assertThat;

class SharedRetryTailTest {
	@Test
	void acceptsCommonTailWithConditionalRethrow() {
		Tail tail = new Tail();
		assertThat(tail.valid()).isTrue();
	}

	@Test
	void rejectsTailAlreadyOwnedByMainRegion() {
		Tail tail = new Tail();
		assertThat(ExcHandlersRegionMaker.isSharedRetryTail(tail.start, tail.end, tail.exit, Set.of(tail.branch))).isFalse();
	}

	@Test
	void rejectsProtectedTail() {
		Tail tail = new Tail();
		tail.start.addAttr(CatchAttr.build(new ArrayList<>()));
		assertThat(tail.valid()).isFalse();
	}

	@Test
	void rejectsSecondEntryIntoInterior() {
		Tail tail = new Tail();
		edge(block(7, InsnType.MOVE), tail.branch);
		assertThat(tail.valid()).isFalse();
	}

	@Test
	void rejectsOtherEntryIntoLoopEnd() {
		Tail tail = new Tail();
		edge(block(7, InsnType.MOVE), tail.end);
		assertThat(tail.valid()).isFalse();
	}

	@Test
	void rejectsHiddenCycle() {
		Tail tail = new Tail();
		edge(tail.branch, tail.start);
		assertThat(tail.valid()).isFalse();
	}

	@Test
	void rejectsNonTerminalDeadEnd() {
		Tail tail = new Tail();
		edge(tail.branch, block(7, InsnType.INVOKE));
		assertThat(tail.valid()).isFalse();
	}

	@Test
	void rejectsFallthroughToMethodExit() {
		Tail tail = new Tail();
		edge(tail.branch, tail.exit);
		assertThat(tail.valid()).isFalse();
	}

	@Test
	void acceptsSharedConditionalCleanupWithSeparateThrowBlock() {
		BlockNode firstHandler = block(0, InsnType.MOVE);
		BlockNode secondHandler = block(1, InsnType.MOVE);
		BlockNode cleanup = block(2, InsnType.INVOKE);
		BlockNode condition = block(3, InsnType.IF);
		BlockNode optional = block(4, InsnType.MOVE);
		BlockNode invoke = block(5, InsnType.INVOKE);
		BlockNode rethrow = block(6, InsnType.THROW);
		BlockNode exit = block(7, null);
		edge(firstHandler, cleanup);
		edge(secondHandler, cleanup);
		edge(cleanup, condition);
		edge(condition, invoke);
		edge(condition, optional);
		edge(optional, invoke);
		edge(invoke, rethrow);
		edge(rethrow, exit);
		assertThat(ExcHandlersRegionMaker.isSharedConditionalCleanupRethrow(cleanup, exit)).isTrue();
		rethrow.getInstructions().clear();
		assertThat(ExcHandlersRegionMaker.isSharedConditionalCleanupRethrow(cleanup, exit)).isFalse();
		rethrow.getInstructions().add(new InsnNode(InsnType.THROW, 0));
		edge(block(8, InsnType.MOVE), rethrow);
		assertThat(ExcHandlersRegionMaker.isSharedConditionalCleanupRethrow(cleanup, exit)).isFalse();
	}

	@Test
	void acceptsLinearCatchEntryMoves() {
		BlockNode handler = handler();
		BlockNode move = block(1, InsnType.MOVE);
		BlockNode join = block(2, InsnType.INVOKE);
		edge(handler, move);
		edge(move, join);
		assertThat(ExcHandlersRegionMaker.findMoveOnlyHandlerStart(move, join)).isSameAs(handler);
	}

	@Test
	void rejectsEffectfulCatchEntryPrefix() {
		BlockNode handler = handler();
		BlockNode invoke = block(1, InsnType.INVOKE);
		BlockNode join = block(2, InsnType.INVOKE);
		edge(handler, invoke);
		edge(invoke, join);
		assertThat(ExcHandlersRegionMaker.findMoveOnlyHandlerStart(invoke, join)).isNull();
	}

	@Test
	void rejectsBranchedCatchEntryPrefix() {
		BlockNode handler = handler();
		BlockNode join = block(2, InsnType.INVOKE);
		edge(handler, join);
		edge(handler, block(3, InsnType.MOVE));
		assertThat(ExcHandlersRegionMaker.findMoveOnlyHandlerStart(handler, join)).isNull();
	}

	@Test
	void rejectsFinallyEntryPrefix() {
		ExceptionHandler exception = ExceptionHandler.build(null, 0, null);
		exception.setFinally(true);
		BlockNode handler = block(0, InsnType.MOVE);
		handler.addAttr(new ExcHandlerAttr(exception));
		BlockNode join = block(1, InsnType.INVOKE);
		edge(handler, join);
		assertThat(ExcHandlersRegionMaker.findMoveOnlyHandlerStart(handler, join)).isNull();
	}

	private static BlockNode handler() {
		BlockNode handler = block(0, InsnType.MOVE);
		handler.addAttr(new ExcHandlerAttr(ExceptionHandler.build(null, 0, null)));
		return handler;
	}

	private static class Tail {
		final BlockNode start = block(0, InsnType.INVOKE);
		final BlockNode branch = block(1, InsnType.IF);
		final BlockNode rethrow = block(2, InsnType.THROW);
		final BlockNode end = block(3, InsnType.MOVE);
		final BlockNode exit = block(4, null);

		Tail() {
			end.add(AFlag.LOOP_END);
			edge(start, branch);
			edge(branch, rethrow);
			edge(branch, end);
			edge(rethrow, exit);
		}

		boolean valid() {
			return ExcHandlersRegionMaker.isSharedRetryTail(start, end, exit, Set.of());
		}
	}

	private static BlockNode block(int id, InsnType type) {
		BlockNode block = new BlockNode(id, id, 0);
		if (type != null) {
			block.getInstructions().add(new InsnNode(type, 0));
		}
		block.updateCleanSuccessors();
		return block;
	}

	private static void edge(BlockNode from, BlockNode to) {
		connect(from, to);
		from.updateCleanSuccessors();
	}
}
