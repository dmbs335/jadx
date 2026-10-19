package jadx.core.dex.visitors.regions;

import java.util.Set;

import org.junit.jupiter.api.Test;

import jadx.core.dex.attributes.AFlag;
import jadx.core.dex.attributes.AType;
import jadx.core.dex.attributes.nodes.EdgeInsnAttr;
import jadx.core.dex.attributes.nodes.LoopInfo;
import jadx.core.dex.instructions.InsnType;
import jadx.core.dex.nodes.BlockNode;
import jadx.core.dex.nodes.Edge;
import jadx.core.dex.nodes.InsnNode;

import static jadx.core.dex.visitors.blocks.BlockSplitter.connect;
import static org.assertj.core.api.Assertions.assertThat;

class RestoreExceptionHandlerLoopExitsTest {
	@Test
	void ignoresAlreadyEmittedLoopTransfers() {
		for (InsnType type : new InsnType[] { InsnType.CONTINUE, InsnType.BREAK }) {
			BlockNode target = block(0, InsnType.INVOKE);
			BlockNode source = block(1, type);
			edge(source, target);
			assertThat(RestoreExceptionHandlerLoopExits.collectBoundaryTargets(Set.of(source))).isEmpty();
		}
	}

	@Test
	void ignoresMaterializedLoopTransferButPreservesOtherBranch() {
		for (InsnType type : new InsnType[] { InsnType.CONTINUE, InsnType.BREAK }) {
			BlockNode source = block(0, InsnType.IF);
			BlockNode target = block(1, InsnType.INVOKE);
			BlockNode fallThrough = block(2, InsnType.INVOKE);
			edge(source, target);
			edge(source, fallThrough);
			EdgeInsnAttr.addEdgeInsn(new Edge(source, target), new InsnNode(type, 0));
			assertThat(RestoreExceptionHandlerLoopExits.collectBoundaryTargets(Set.of(source))).containsExactly(fallThrough);
		}
	}

	@Test
	void preservesBackEdgeFromEmittedEffectfulLoopEnd() {
		BlockNode start = block(0, InsnType.INVOKE);
		BlockNode end = block(1, InsnType.INVOKE);
		loopBackEdge(start, end);
		assertThat(end.getCleanSuccessors()).isEmpty();
		assertThat(RestoreExceptionHandlerLoopExits.collectBoundaryTargets(Set.of(end))).containsExactly(start);
		assertThat(RestoreExceptionHandlerLoopExits.collectBoundaryTargets(Set.of(start, end))).isEmpty();
	}

	@Test
	void doesNotInventBackEdgeFromLoopMetadataAlone() {
		BlockNode start = block(0, InsnType.INVOKE);
		BlockNode end = block(1, InsnType.INVOKE);
		end.add(AFlag.LOOP_END);
		end.addAttr(AType.LOOP, new LoopInfo(start, end, Set.of(start, end)));
		assertThat(RestoreExceptionHandlerLoopExits.collectBoundaryTargets(Set.of(end))).isEmpty();
	}

	@Test
	void ignoresBackEdgeReplacedByTerminalInstruction() {
		BlockNode start = block(0, InsnType.INVOKE);
		BlockNode end = block(1, InsnType.INVOKE);
		loopBackEdge(start, end);
		EdgeInsnAttr.addEdgeInsn(new Edge(end, start), new InsnNode(InsnType.THROW, 0));
		assertThat(RestoreExceptionHandlerLoopExits.collectBoundaryTargets(Set.of(end))).isEmpty();
	}

	private static void loopBackEdge(BlockNode start, BlockNode end) {
		end.add(AFlag.LOOP_END);
		end.addAttr(AType.LOOP, new LoopInfo(start, end, Set.of(start, end)));
		edge(end, start);
	}

	@Test
	void ignoresTerminalMethodExitEdges() {
		BlockNode methodExit = block(0, null);
		BlockNode rethrow = block(1, InsnType.THROW);
		BlockNode returnBlock = block(2, InsnType.RETURN);
		edge(rethrow, methodExit);
		edge(returnBlock, methodExit);
		assertThat(RestoreExceptionHandlerLoopExits.collectBoundaryTargets(Set.of(rethrow, returnBlock))).isEmpty();
	}

	@Test
	void ignoresEmptyConnectorToEmittedHandlerBlock() {
		BlockNode branch = block(0, InsnType.IF);
		BlockNode connector = block(1, null);
		BlockNode terminal = block(2, InsnType.THROW);
		edge(branch, connector);
		edge(connector, terminal);
		assertThat(RestoreExceptionHandlerLoopExits.collectBoundaryTargets(Set.of(branch, terminal))).isEmpty();
	}

	@Test
	void keepsNormalBoundaryAlongsideRethrow() {
		BlockNode branch = block(0, InsnType.IF);
		BlockNode terminal = block(1, InsnType.THROW);
		BlockNode methodExit = block(2, null);
		BlockNode normalExit = block(3, InsnType.INVOKE);
		edge(branch, terminal);
		edge(branch, normalExit);
		edge(terminal, methodExit);
		assertThat(RestoreExceptionHandlerLoopExits.collectBoundaryTargets(Set.of(branch, terminal)))
				.containsExactly(normalExit);
	}

	@Test
	void keepsConnectorThatExecutesCode() {
		BlockNode branch = block(0, InsnType.IF);
		BlockNode connector = block(1, InsnType.INVOKE);
		BlockNode terminal = block(2, InsnType.THROW);
		edge(branch, connector);
		edge(connector, terminal);
		assertThat(RestoreExceptionHandlerLoopExits.collectBoundaryTargets(Set.of(branch, terminal)))
				.containsExactly(connector);
	}

	@Test
	void ignoresOnlyMatchingMaterializedRethrowEdge() {
		BlockNode branch = block(0, InsnType.IF);
		BlockNode rethrow = block(1, InsnType.THROW);
		BlockNode normalExit = block(2, InsnType.INVOKE);
		edge(branch, rethrow);
		edge(branch, normalExit);
		EdgeInsnAttr.addEdgeInsn(new Edge(branch, rethrow), new InsnNode(InsnType.THROW, 0));
		assertThat(RestoreExceptionHandlerLoopExits.collectBoundaryTargets(Set.of(branch)))
				.containsExactly(normalExit);
	}

	@Test
	void preservesBoundaryBeforeSharedTail() {
		BlockNode handler = block(0, InsnType.INVOKE);
		BlockNode connector = block(1, null);
		BlockNode sharedTail = block(2, InsnType.MOVE);
		edge(handler, connector);
		edge(connector, sharedTail);
		assertThat(RestoreExceptionHandlerLoopExits.collectBoundaryTargets(Set.of(handler)))
				.containsExactly(connector);
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
