package jadx.core.dex.visitors.blocks;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import jadx.core.dex.attributes.AType;
import jadx.core.dex.attributes.nodes.TmpEdgeAttr;
import jadx.core.dex.instructions.InsnType;
import jadx.core.dex.instructions.args.ArgType;
import jadx.core.dex.instructions.args.InsnArg;
import jadx.core.dex.instructions.args.PrimitiveType;
import jadx.core.dex.nodes.BlockNode;
import jadx.core.dex.nodes.InsnNode;
import jadx.core.dex.trycatch.CatchAttr;
import jadx.core.dex.trycatch.ExceptionHandler;

import static jadx.core.dex.visitors.blocks.BlockSplitter.connect;
import static org.assertj.core.api.Assertions.assertThat;

class SplitEntryRetryNormalizerTest {
	@Test
	void plansBeforeSsaWithoutMutatingGraph() {
		Graph graph = graph();
		var successors = graph.blocks.stream().map(b -> List.copyOf(b.getSuccessors())).collect(Collectors.toList());
		var predecessors = graph.blocks.stream().map(b -> List.copyOf(b.getPredecessors())).collect(Collectors.toList());
		var plan = SplitEntryRetryNormalizer.findPlan(graph.blocks);
		assertThat(plan).isNotNull();
		assertThat(plan.body).isSameAs(graph.body);
		assertThat(plan.alternate).isSameAs(graph.alternate);
		assertThat(plan.register).isEqualTo(1);
		assertThat(graph.blocks.stream().map(BlockNode::getSuccessors).collect(Collectors.toList())).isEqualTo(successors);
		assertThat(graph.blocks.stream().map(BlockNode::getPredecessors).collect(Collectors.toList())).isEqualTo(predecessors);
	}

	@Test
	void ignoresTemporaryHandlerEdgeOnEitherEntryArm() {
		for (boolean onBody : List.of(false, true)) {
			Graph graph = graph();
			BlockSplitter.removeConnection(graph.entry, graph.handler);
			BlockNode source = onBody ? graph.body : graph.alternate;
			connect(source, graph.handler);
			graph.handler.addAttr(new TmpEdgeAttr(source));
			assertThat(SplitEntryRetryNormalizer.findPlan(graph.blocks)).isNotNull();
			assertThat(source.getSuccessors()).contains(graph.handler);
		}
	}

	@Test
	void rejectsLiveOrDifferentEntryRegister() {
		Graph graph = graph();
		graph.alternate.getInstructions().get(0).setResult(InsnArg.reg(2, ArgType.INT));
		assertThat(SplitEntryRetryNormalizer.findPlan(graph.blocks)).isNull();
		graph.alternate.getInstructions().set(0, new InsnNode(InsnType.MOVE, 1));
		assertThat(SplitEntryRetryNormalizer.findPlan(graph.blocks)).isNull();
	}

	@Test
	void rejectsWideEntryAssignment() {
		Graph graph = graph();
		graph.alternate.getInstructions().get(0).setResult(InsnArg.reg(1, ArgType.LONG));
		assertThat(SplitEntryRetryNormalizer.findPlan(graph.blocks)).isNull();
	}

	@Test
	void rejectsUnprotectedOperationInsideCycle() {
		Graph graph = graph();
		graph.join.getInstructions().add(new InsnNode(InsnType.INVOKE, 0));
		assertThat(SplitEntryRetryNormalizer.findPlan(graph.blocks)).isNull();
	}

	@Test
	void rejectsProtectedOperationOutsidePlannedScope() {
		Graph graph = graph();
		InsnNode extra = new InsnNode(InsnType.INVOKE, 0);
		extra.addAttr(graph.body.getInstructions().get(1).get(AType.EXC_CATCH));
		graph.entry.getInstructions().add(extra);
		assertThat(SplitEntryRetryNormalizer.findPlan(graph.blocks)).isNull();
	}

	@Test
	void requiresHandlerRetryPath() {
		Graph graph = graph();
		BlockSplitter.removeConnection(graph.handler, graph.body);
		assertThat(SplitEntryRetryNormalizer.findPlan(graph.blocks)).isNull();
	}

	@Test
	void rejectsAnotherEntryIntoLoopInterior() {
		Graph graph = graph();
		connect(graph.entry, graph.condition);
		assertThat(SplitEntryRetryNormalizer.findPlan(graph.blocks)).isNull();
	}

	@Test
	void rejectsDifferentHandlerAndMultipleEntryPredecessors() {
		Graph graph = graph();
		graph.alternate.getInstructions().get(1).addAttr(
				CatchAttr.build(new ArrayList<>(List.of(ExceptionHandler.build(null, 99, null)))));
		assertThat(SplitEntryRetryNormalizer.findPlan(graph.blocks)).isNull();
		graph = graph();
		connect(graph.condition, graph.alternate);
		assertThat(SplitEntryRetryNormalizer.findPlan(graph.blocks)).isNull();
	}

	private static Graph graph() {
		List<BlockNode> blocks = new ArrayList<>();
		for (int i = 0; i < 8; i++) {
			blocks.add(new BlockNode(i, i, i));
		}
		BlockNode entry = blocks.get(0);
		BlockNode initial = blocks.get(1);
		BlockNode body = blocks.get(2);
		BlockNode alternate = blocks.get(3);
		BlockNode join = blocks.get(4);
		BlockNode condition = blocks.get(5);
		BlockNode handler = blocks.get(6);
		BlockNode exit = blocks.get(7);
		connect(entry, initial);
		connect(initial, body);
		connect(entry, alternate);
		connect(entry, handler); // Temporary exception reachability, not a normal loop entry.
		handler.addAttr(new TmpEdgeAttr(entry));
		connect(handler, body);
		connect(handler, exit);
		connect(body, join);
		connect(alternate, join);
		connect(join, condition);
		connect(condition, body);
		connect(condition, exit);
		handler.getInstructions().add(new InsnNode(InsnType.MOVE_EXCEPTION, 0));
		CatchAttr catchAttr = CatchAttr.build(new ArrayList<>(List.of(ExceptionHandler.build(null, 6, null))));
		for (BlockNode arm : List.of(body, alternate)) {
			InsnNode constant = new InsnNode(InsnType.CONST, 1);
			constant.setResult(InsnArg.reg(1, ArgType.unknown(PrimitiveType.INT, PrimitiveType.FLOAT)));
			constant.addArg(InsnArg.lit(1, ArgType.INT));
			arm.getInstructions().add(constant);
			InsnNode call = new InsnNode(InsnType.INVOKE, 0);
			call.addAttr(catchAttr);
			arm.getInstructions().add(call);
		}
		return new Graph(blocks, entry, body, alternate, join, condition, handler);
	}

	private static final class Graph {
		final List<BlockNode> blocks;
		final BlockNode entry;
		final BlockNode body;
		final BlockNode alternate;
		final BlockNode join;
		final BlockNode condition;
		final BlockNode handler;

		Graph(List<BlockNode> blocks, BlockNode entry, BlockNode body, BlockNode alternate,
				BlockNode join, BlockNode condition, BlockNode handler) {
			this.blocks = blocks;
			this.entry = entry;
			this.body = body;
			this.alternate = alternate;
			this.join = join;
			this.condition = condition;
			this.handler = handler;
		}
	}
}
