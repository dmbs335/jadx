package jadx.core.dex.visitors.blocks;

import org.junit.jupiter.api.Test;

import jadx.core.dex.attributes.AFlag;
import jadx.core.dex.attributes.nodes.SpecialEdgeAttr;
import jadx.core.dex.attributes.nodes.SpecialEdgeAttr.SpecialEdgeType;
import jadx.core.dex.instructions.IfNode;
import jadx.core.dex.instructions.IfOp;
import jadx.core.dex.instructions.InsnType;
import jadx.core.dex.instructions.args.ArgType;
import jadx.core.dex.instructions.args.InsnArg;
import jadx.core.dex.nodes.BlockNode;
import jadx.core.dex.nodes.InsnNode;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

class FixMultiEntryLoopsTest {

	@Test
	void testIgnoreExceptionHandlerBackEdge() {
		BlockNode header = block(0);
		BlockNode loopEnd = block(1);
		header.getPredecessors().add(block(2));
		header.getPredecessors().add(loopEnd);
		connect(header, loopEnd);
		SpecialEdgeAttr backEdge = new SpecialEdgeAttr(SpecialEdgeType.BACK_EDGE, loopEnd, header);

		assertThat(FixMultiEntryLoops.isMultiEntryLoop(backEdge)).isTrue();
		loopEnd.add(AFlag.EXC_BOTTOM_SPLITTER);
		assertThat(FixMultiEntryLoops.isMultiEntryLoop(backEdge)).isFalse();
	}

	@Test
	void testDetectCycleThroughExceptionSplitterForStructuralSplit() {
		BlockNode header = block(0);
		BlockNode loopEnd = block(1);
		BlockNode exceptionSplitter = block(2);
		BlockNode handler = block(3);
		header.getPredecessors().add(block(4));
		header.getPredecessors().add(loopEnd);
		exceptionSplitter.add(AFlag.EXC_BOTTOM_SPLITTER);
		connect(header, exceptionSplitter);
		connect(exceptionSplitter, handler);
		connect(handler, loopEnd);
		SpecialEdgeAttr backEdge = new SpecialEdgeAttr(SpecialEdgeType.BACK_EDGE, loopEnd, header);

		assertThat(FixMultiEntryLoops.isMultiEntryLoop(backEdge)).isTrue();
		assertThat(FixMultiEntryLoops.isExceptionOnlyCycle(backEdge)).isTrue();

		BlockNode normalPath = block(5);
		connect(header, normalPath);
		connect(normalPath, loopEnd);
		assertThat(FixMultiEntryLoops.isMultiEntryLoop(backEdge)).isTrue();
		assertThat(FixMultiEntryLoops.isExceptionOnlyCycle(backEdge)).isFalse();
	}

	@Test
	void testDetectPureCoroutineBranchJoinPath() {
		BlockNode entry = block(0);
		BlockNode path = block(1);
		BlockNode join = block(2);
		BlockNode thenBlock = block(3);
		BlockNode elseBlock = block(4);
		path.getInstructions().add(new InsnNode(InsnType.MOVE, 0));
		InsnArg value = InsnArg.reg(0, ArgType.INT);
		InsnArg zero = InsnArg.lit(0, ArgType.INT);
		join.getInstructions().add(new IfNode(IfOp.EQ, -1, value, zero));
		connect(entry, join);
		connect(path, join);
		connect(join, thenBlock);
		connect(join, elseBlock);
		SpecialEdgeAttr backEdge = new SpecialEdgeAttr(SpecialEdgeType.BACK_EDGE, path, join);

		assertThat(FixMultiEntryLoops.isPureCoroutineJoinPath(backEdge)).isTrue();
	}

	@Test
	void testRejectEffectfulCoroutineJoinPath() {
		BlockNode entry = block(0);
		BlockNode path = block(1);
		BlockNode join = block(2);
		BlockNode exit = block(3);
		path.getInstructions().add(new InsnNode(InsnType.CONST, 0));
		join.getInstructions().add(new InsnNode(InsnType.INVOKE, 0));
		connect(entry, join);
		connect(path, join);
		connect(join, exit);
		SpecialEdgeAttr backEdge = new SpecialEdgeAttr(SpecialEdgeType.BACK_EDGE, path, join);

		assertThat(FixMultiEntryLoops.isPureCoroutineJoinPath(backEdge)).isFalse();
	}

	@Test
	void testDetectPureCoroutineConstantJoinPath() {
		BlockNode entry = block(0);
		BlockNode path = block(1);
		BlockNode join = block(2);
		BlockNode exit = block(3);
		path.getInstructions().add(new InsnNode(InsnType.MOVE, 0));
		join.getInstructions().add(new InsnNode(InsnType.CONST, 0));
		connect(entry, join);
		connect(path, join);
		connect(join, exit);
		SpecialEdgeAttr backEdge = new SpecialEdgeAttr(SpecialEdgeType.BACK_EDGE, path, join);

		assertThat(FixMultiEntryLoops.isPureCoroutineJoinPath(backEdge)).isTrue();
	}

	@Test
	void testRejectCoroutineMoveJoinPath() {
		BlockNode entry = block(0);
		BlockNode path = block(1);
		BlockNode join = block(2);
		BlockNode exit = block(3);
		path.getInstructions().add(new InsnNode(InsnType.MOVE, 0));
		join.getInstructions().add(new InsnNode(InsnType.MOVE, 0));
		connect(entry, join);
		connect(path, join);
		connect(join, exit);
		SpecialEdgeAttr backEdge = new SpecialEdgeAttr(SpecialEdgeType.BACK_EDGE, path, join);

		assertThat(FixMultiEntryLoops.isPureCoroutineJoinPath(backEdge)).isFalse();
	}

	private static void connect(BlockNode source, BlockNode target) {
		source.getSuccessors().add(target);
		target.getPredecessors().add(source);
	}

	private static BlockNode block(int id) {
		return new BlockNode(id, id, id);
	}
}
