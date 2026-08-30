package jadx.core.dex.trycatch;

import java.util.List;

import org.junit.jupiter.api.Test;

import jadx.core.dex.attributes.AFlag;
import jadx.core.dex.nodes.BlockNode;

import static org.assertj.core.api.Assertions.assertThat;

class ExceptionHandlerTest {

	@Test
	void resolveBottomSplitterForSequentialInnerTries() {
		TestGraph graph = new TestGraph(true);

		assertThat(graph.handler.getBottomSplitter()).isSameAs(graph.secondSplitter);
	}

	@Test
	void rejectAmbiguousBottomSplitterForParallelInnerTries() {
		TestGraph graph = new TestGraph(false);

		assertThat(graph.handler.getBottomSplitter()).isNull();
	}

	private static final class TestGraph {
		private final ExceptionHandler handler = ExceptionHandler.build(null, 0, null);
		private final BlockNode secondSplitter;

		private TestGraph(boolean sequential) {
			BlockNode firstExit = block(0);
			BlockNode secondExit = block(1);
			BlockNode firstSplitter = splitter(2);
			secondSplitter = splitter(3);
			BlockNode handlerBlock = block(4);

			TryCatchBlockAttr outerTry = new TryCatchBlockAttr(0, List.of(handler), List.of(firstExit, secondExit));
			TryCatchBlockAttr firstInnerTry = new TryCatchBlockAttr(1, List.of(), List.of(firstExit));
			TryCatchBlockAttr secondInnerTry = new TryCatchBlockAttr(2, List.of(), List.of(secondExit));
			outerTry.addInnerTryBlock(firstInnerTry);
			outerTry.addInnerTryBlock(secondInnerTry);
			firstExit.addAttr(firstInnerTry);
			secondExit.addAttr(secondInnerTry);

			if (sequential) {
				connect(firstExit, secondExit);
			}
			connect(firstExit, firstSplitter);
			connect(secondExit, secondSplitter);
			connect(firstSplitter, handlerBlock);
			connect(secondSplitter, handlerBlock);
			firstExit.updateCleanSuccessors();
			secondExit.updateCleanSuccessors();
			firstSplitter.updateCleanSuccessors();
			secondSplitter.updateCleanSuccessors();
			handlerBlock.updateCleanSuccessors();
			handler.setHandlerBlock(handlerBlock);
		}
	}

	private static BlockNode block(int id) {
		return new BlockNode(id, id, id);
	}

	private static BlockNode splitter(int id) {
		BlockNode block = block(id);
		block.add(AFlag.EXC_BOTTOM_SPLITTER);
		return block;
	}

	private static void connect(BlockNode from, BlockNode to) {
		from.getSuccessors().add(to);
		to.getPredecessors().add(from);
	}
}
