package jadx.core.dex.visitors.blocks;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

import jadx.core.dex.attributes.AFlag;
import jadx.core.dex.attributes.AType;
import jadx.core.dex.attributes.nodes.SpecialEdgeAttr;
import jadx.core.dex.attributes.nodes.SpecialEdgeAttr.SpecialEdgeType;
import jadx.core.dex.instructions.IfNode;
import jadx.core.dex.instructions.IfOp;
import jadx.core.dex.instructions.InsnType;
import jadx.core.dex.instructions.InvokeNode;
import jadx.core.dex.instructions.SwitchInsn;
import jadx.core.dex.instructions.args.ArgType;
import jadx.core.dex.instructions.args.InsnArg;
import jadx.core.dex.instructions.args.RegisterArg;
import jadx.core.dex.nodes.BlockNode;
import jadx.core.dex.nodes.InsnNode;
import jadx.core.dex.nodes.MethodNode;
import jadx.core.utils.BlockUtils;
import jadx.core.utils.ListUtils;

public class FixMultiEntryLoops {
	static boolean hasMultiEntryLoops(MethodNode mth) {
		detectSpecialEdges(mth);
		return mth.getAll(AType.SPECIAL_EDGE).stream()
				.anyMatch(FixMultiEntryLoops::isMultiEntryLoop);
	}

	public static boolean process(MethodNode mth) {
		try {
			detectSpecialEdges(mth);
		} catch (Exception e) {
			mth.addWarnComment("Failed to detect multi-entry loops", e);
			return false;
		}
		List<SpecialEdgeAttr> specialEdges = mth.getAll(AType.SPECIAL_EDGE);
		List<SpecialEdgeAttr> multiEntryLoops = specialEdges.stream()
				.filter(FixMultiEntryLoops::isMultiEntryLoop)
				.collect(Collectors.toList());
		if (multiEntryLoops.isEmpty()) {
			return false;
		}
		try {
			List<SpecialEdgeAttr> crossEdges = ListUtils.filter(specialEdges, e -> e.getType() == SpecialEdgeType.CROSS_EDGE);
			boolean changed = false;
			for (SpecialEdgeAttr backEdge : multiEntryLoops) {
				changed |= fixLoop(mth, backEdge, crossEdges);
			}
			return changed;
		} catch (Exception e) {
			mth.addWarnComment("Failed to fix multi-entry loops", e);
			return false;
		}
	}

	private static boolean fixLoop(MethodNode mth, SpecialEdgeAttr backEdge, List<SpecialEdgeAttr> crossEdges) {
		if (splitSharedLoopBranchTarget(mth, backEdge)) {
			return true;
		}
		if (splitSharedLoopReset(mth, backEdge)) {
			return true;
		}
		if (splitSharedExceptionExit(mth, backEdge)) {
			return true;
		}
		if (!isCoroutineMethod(mth) && isExceptionOnlyCycle(backEdge)) {
			return false;
		}
		if (normalizeCoroutineResumeLoop(mth, backEdge)) {
			return true;
		}
		if (isHeaderSuccessorEntry(mth, backEdge, crossEdges)) {
			return true;
		}
		if (isEndBlockEntry(mth, backEdge, crossEdges)) {
			return true;
		}
		mth.addWarnComment("Unsupported multi-entry loop pattern (" + backEdge + "). Please report as a decompilation issue!!!");
		return false;
	}

	private static boolean splitSharedLoopBranchTarget(MethodNode mth, SpecialEdgeAttr backEdge) {
		if (isCoroutineMethod(mth)) {
			return false;
		}
		BlockNode loopBranch = backEdge.getStart();
		BlockNode sharedTarget = backEdge.getEnd();
		if (!(BlockUtils.getLastInsn(loopBranch) instanceof IfNode)
				|| sharedTarget.getPredecessors().size() < 2
				|| !isSafeSharedLoopBranch(sharedTarget)) {
			return false;
		}
		BlockNode loopHeader = findLoopHeaderAfter(sharedTarget, loopBranch);
		if (loopHeader == null
				|| sharedTarget.getPredecessors().stream().noneMatch(pred -> !pred.getDoms().get(loopHeader.getId()))) {
			return false;
		}

		BlockNode branchCopy = BlockSplitter.insertBlockBetween(mth, loopBranch, sharedTarget);
		branchCopy.add(AFlag.SYNTHETIC);
		BlockSplitter.copyBlockData(sharedTarget, branchCopy);
		InsnNode continueInsn = new InsnNode(InsnType.CONTINUE, 0);
		continueInsn.add(AFlag.SYNTHETIC);
		branchCopy.getInstructions().add(continueInsn);
		BlockSplitter.replaceConnection(branchCopy, sharedTarget, loopHeader);
		mth.addDebugComment("Split shared loop branch target: " + sharedTarget);
		return true;
	}

	private static boolean isSafeSharedLoopBranch(BlockNode block) {
		if (block.getInstructions().isEmpty() || block.getInstructions().size() > 2) {
			return false;
		}
		int invokes = 0;
		for (InsnNode insn : block.getInstructions()) {
			switch (insn.getType()) {
				case MOVE:
				case CONST:
				case SGET:
					break;
				case INVOKE:
					InvokeNode invoke = (InvokeNode) insn;
					if (!invoke.getCallMth().getName().equals("add")
							|| !invoke.getCallMth().getDeclClass().getFullName().equals("java.util.List")
							|| invoke.getResult() != null) {
						return false;
					}
					invokes++;
					break;
				default:
					return false;
			}
		}
		return invokes <= 1;
	}

	private static @Nullable BlockNode findLoopHeaderAfter(BlockNode block, BlockNode loopBranch) {
		BlockNode current = block;
		Set<BlockNode> visited = new HashSet<>();
		for (int depth = 0; depth < 5 && current != null && visited.add(current); depth++) {
			if (current != block && loopBranch.getDoms().get(current.getId())) {
				return current;
			}
			if (current != block && !current.getInstructions().isEmpty()) {
				return null;
			}
			current = current.getSuccessors().size() == 1 ? current.getSuccessors().get(0) : null;
		}
		return null;
	}

	private static boolean splitSharedLoopReset(MethodNode mth, SpecialEdgeAttr backEdge) {
		if (isCoroutineMethod(mth)) {
			return false;
		}
		BlockNode sharedReset = backEdge.getStart();
		BlockNode header = backEdge.getEnd();
		if (!ListUtils.isSingleElement(sharedReset.getSuccessors(), header)
				|| sharedReset.getInstructions().isEmpty()
				|| sharedReset.getInstructions().stream()
						.anyMatch(insn -> insn.getType() != InsnType.MOVE && insn.getType() != InsnType.CONST)) {
			return false;
		}
		List<BlockNode> loopPreds = ListUtils.filter(sharedReset.getPredecessors(),
				pred -> pred == header || pred.getDoms().get(header.getId()));
		List<BlockNode> entryPreds = ListUtils.filter(sharedReset.getPredecessors(), pred -> !loopPreds.contains(pred));
		if (loopPreds.isEmpty() || loopPreds.size() > 4 || entryPreds.isEmpty()) {
			return false;
		}

		for (BlockNode loopPred : loopPreds) {
			BlockNode loopReset = BlockSplitter.startNewBlock(mth, sharedReset.getStartOffset());
			loopReset.add(AFlag.SYNTHETIC);
			BlockSplitter.copyBlockData(sharedReset, loopReset);
			InsnNode continueInsn = new InsnNode(InsnType.CONTINUE, 0);
			continueInsn.add(AFlag.SYNTHETIC);
			loopReset.getInstructions().add(continueInsn);
			BlockSplitter.connect(loopReset, header);
			BlockSplitter.replaceConnection(loopPred, sharedReset, loopReset);
		}
		mth.addDebugComment("Split shared loop entry/reset block for " + loopPreds.size() + " loop branches: " + sharedReset);
		return true;
	}

	private static boolean splitSharedExceptionExit(MethodNode mth, SpecialEdgeAttr backEdge) {
		BlockNode sharedExit = backEdge.getStart();
		BlockNode merge = backEdge.getEnd();
		if (!ListUtils.isSingleElement(sharedExit.getSuccessors(), merge)
				|| hasNormalPath(merge, sharedExit)
				|| merge.getSuccessors().stream().noneMatch(block -> block.contains(AFlag.EXC_BOTTOM_SPLITTER))
				|| sharedExit.getInstructions().isEmpty()
				|| sharedExit.getInstructions().stream()
						.anyMatch(insn -> insn.getType() != InsnType.MOVE && insn.getType() != InsnType.CONST)) {
			return false;
		}
		List<BlockNode> exceptionPreds = ListUtils.filter(sharedExit.getPredecessors(), BlockUtils::isExceptionHandlerPath);
		List<BlockNode> normalPreds = ListUtils.filter(sharedExit.getPredecessors(), pred -> !BlockUtils.isExceptionHandlerPath(pred));
		if (exceptionPreds.size() != 1 || normalPreds.isEmpty()) {
			return false;
		}

		BlockNode normalExit = BlockSplitter.startNewBlock(mth, sharedExit.getStartOffset());
		normalExit.add(AFlag.SYNTHETIC);
		BlockSplitter.copyBlockData(sharedExit, normalExit);
		BlockSplitter.connect(normalExit, merge);
		for (BlockNode normalPred : normalPreds) {
			BlockSplitter.replaceConnection(normalPred, sharedExit, normalExit);
		}
		mth.addDebugComment("Split shared normal/exception exit block: " + sharedExit);
		return true;
	}

	/**
	 * Exception table edges can close a cycle in the raw CFG even when normal control flow cannot
	 * return to the alleged loop end. Keep structural exception-exit fixes above this check, but do
	 * not report the remaining exception-only cycles as unsupported source loops.
	 */
	static boolean isExceptionOnlyCycle(SpecialEdgeAttr backEdge) {
		return !hasNormalPath(backEdge.getEnd(), backEdge.getStart());
	}

	private static boolean normalizeCoroutineResumeLoop(MethodNode mth, SpecialEdgeAttr backEdge) {
		if (!isCoroutineMethod(mth)) {
			return false;
		}
		BlockNode loopEnd = backEdge.getStart();
		BlockNode body = backEdge.getEnd();
		BlockNode resume = ListUtils.filterOnlyOne(body.getPredecessors(), pred -> pred != loopEnd);
		if (resume == null) {
			return false;
		}
		CoroutineStateEntry stateEntry = findCoroutineStateEntry(resume);
		if (stateEntry == null) {
			return false;
		}
		RegisterArg stateArg = stateEntry.stateArg;
		AwaitEntry awaitEntry = findAwaitEntry(body, loopEnd);
		if (awaitEntry == null) {
			awaitEntry = findAwaitEntryOnCycle(body);
		}
		if (awaitEntry == null
				|| !containsInvoke(resume, "throwOnFailure")
				|| !containsInvoke(awaitEntry.awaitBlock, "await")
				|| !containsInsnType(resume, InsnType.IGET)
				|| !containsInsnType(awaitEntry.awaitBlock, InsnType.IPUT)) {
			return false;
		}

		BlockNode stateHeader = BlockSplitter.startNewBlock(mth, awaitEntry.awaitBlock.getStartOffset());
		stateHeader.add(AFlag.SYNTHETIC);
		BlockNode resumedBody;
		if (stateEntry.dispatchBlock == null) {
			BlockSplitter.replaceConnection(resume, body, stateHeader);
			resumedBody = body;
		} else {
			BlockSplitter.replaceConnection(stateEntry.dispatchBlock, resume, stateHeader);
			resumedBody = resume;
		}
		BlockSplitter.connect(stateHeader, resumedBody);
		BlockSplitter.connect(stateHeader, awaitEntry.awaitBlock);

		for (BlockNode awaitPred : awaitEntry.awaitPreds) {
			BlockNode resetBlock = BlockSplitter.startNewBlock(mth, awaitEntry.awaitBlock.getStartOffset());
			resetBlock.add(AFlag.SYNTHETIC);
			InsnNode resetState = new InsnNode(InsnType.CONST, 1);
			resetState.setResult(InsnArg.reg(stateArg.getRegNum(), ArgType.INT));
			resetState.addArg(InsnArg.lit(0, ArgType.INT));
			resetBlock.getInstructions().add(resetState);
			BlockSplitter.replaceConnection(awaitPred, awaitEntry.awaitBlock, resetBlock);
			BlockSplitter.connect(resetBlock, stateHeader);
		}

		IfNode stateIf = new IfNode(IfOp.NE, resumedBody.getStartOffset(),
				InsnArg.reg(stateArg.getRegNum(), ArgType.INT), InsnArg.lit(0, ArgType.INT));
		stateHeader.getInstructions().add(stateIf);
		stateIf.initBlocks(stateHeader);
		stateHeader.updateCleanSuccessors();
		mth.addDebugComment("Normalize coroutine resume entry for loop: " + backEdge);
		return true;
	}

	private static boolean isCoroutineMethod(MethodNode mth) {
		if (mth.getMethodInfo().getArgumentsTypes().stream()
				.anyMatch(type -> type.toString().startsWith("kotlin.coroutines.Continuation"))) {
			return true;
		}
		ArgType superType = mth.getParentClass().getSuperClass();
		return mth.getName().equals("invokeSuspend")
				&& superType != null
				&& superType.toString().contains("SuspendLambda");
	}

	private static boolean containsInvoke(BlockNode block, String namePart) {
		for (InsnNode insn : block.getInstructions()) {
			if (insn instanceof InvokeNode
					&& ((InvokeNode) insn).getCallMth().getName().contains(namePart)) {
				return true;
			}
		}
		return false;
	}

	private static boolean containsInsnType(BlockNode block, InsnType type) {
		return block.getInstructions().stream().anyMatch(insn -> insn.getType() == type);
	}

	private static @Nullable CoroutineStateEntry findCoroutineStateEntry(BlockNode resume) {
		BlockNode block = resume;
		Set<BlockNode> visited = new HashSet<>();
		for (int depth = 0; depth < 5 && block != null && visited.add(block); depth++) {
			for (BlockNode pred : block.getPredecessors()) {
				InsnNode lastInsn = BlockUtils.getLastInsn(pred);
				if (lastInsn instanceof IfNode) {
					for (InsnArg arg : lastInsn.getArguments()) {
						if (arg instanceof RegisterArg) {
							return new CoroutineStateEntry((RegisterArg) arg, null);
						}
					}
				} else if (lastInsn instanceof SwitchInsn && lastInsn.getArg(0) instanceof RegisterArg) {
					return new CoroutineStateEntry((RegisterArg) lastInsn.getArg(0), pred);
				}
			}
			block = block.getPredecessors().size() == 1 ? block.getPredecessors().get(0) : null;
		}
		return null;
	}

	private static final class CoroutineStateEntry {
		private final RegisterArg stateArg;
		private final @Nullable BlockNode dispatchBlock;

		private CoroutineStateEntry(RegisterArg stateArg, @Nullable BlockNode dispatchBlock) {
			this.stateArg = stateArg;
			this.dispatchBlock = dispatchBlock;
		}
	}

	private static @Nullable AwaitEntry findAwaitEntry(BlockNode body, BlockNode loopEnd) {
		BlockNode block = loopEnd;
		Set<BlockNode> visited = new HashSet<>();
		for (int depth = 0; depth < 5 && block != null && visited.add(block); depth++) {
			if (block.getPredecessors().size() == 2) {
				BlockNode cyclePred = ListUtils.filterOnlyOne(block.getPredecessors(), pred -> isPathExists(body, pred));
				BlockNode initialPred = ListUtils.filterOnlyOne(block.getPredecessors(), pred -> pred != cyclePred);
				if (cyclePred != null && initialPred != null) {
					return new AwaitEntry(block, new ArrayList<>(block.getPredecessors()));
				}
			}
			block = block.getPredecessors().size() == 1 ? block.getPredecessors().get(0) : null;
		}
		return null;
	}

	private static @Nullable AwaitEntry findAwaitEntryOnCycle(BlockNode body) {
		ArrayDeque<BlockNode> queue = new ArrayDeque<>();
		Set<BlockNode> visited = new HashSet<>();
		queue.add(body);
		while (!queue.isEmpty() && visited.size() < 80) {
			BlockNode block = queue.removeFirst();
			if (!visited.add(block)) {
				continue;
			}
			if (block != body
					&& block.getPredecessors().size() >= 2
					&& containsInvoke(block, "await")
					&& containsInsnType(block, InsnType.IPUT)
					&& isPathExists(block, body)) {
				return new AwaitEntry(block, new ArrayList<>(block.getPredecessors()));
			}
			queue.addAll(block.getSuccessors());
		}
		return null;
	}

	private static final class AwaitEntry {
		private final BlockNode awaitBlock;
		private final List<BlockNode> awaitPreds;

		private AwaitEntry(BlockNode awaitBlock, List<BlockNode> awaitPreds) {
			this.awaitBlock = awaitBlock;
			this.awaitPreds = awaitPreds;
		}
	}

	private static boolean isHeaderSuccessorEntry(MethodNode mth, SpecialEdgeAttr backEdge, List<SpecialEdgeAttr> crossEdges) {
		BlockNode header = backEdge.getEnd();
		BlockNode headerIDom = header.getIDom();
		SpecialEdgeAttr subEntry = ListUtils.filterOnlyOne(crossEdges, e -> e.getStart() == headerIDom);
		if (subEntry == null || !ListUtils.isSingleElement(header.getSuccessors(), subEntry.getEnd())) {
			subEntry = ListUtils.filterOnlyOne(crossEdges,
					e -> header.getSuccessors().contains(e.getEnd())
							&& isPathExists(e.getEnd(), backEdge.getStart()));
		}
		if (subEntry == null || !header.getSuccessors().contains(subEntry.getEnd())) {
			return false;
		}
		BlockNode loopEnd = backEdge.getStart();
		BlockNode subEntryBlock = subEntry.getEnd();
		BlockNode copyHeader = BlockSplitter.insertBlockBetween(mth, loopEnd, header);
		BlockSplitter.copyBlockData(header, copyHeader);
		BlockSplitter.replaceConnection(copyHeader, header, subEntryBlock);
		for (BlockNode successor : header.getSuccessors()) {
			if (successor != subEntryBlock) {
				BlockSplitter.connect(copyHeader, successor);
			}
		}
		mth.addDebugComment("Duplicate block (" + header + ") to fix multi-entry loop: " + backEdge);
		return true;
	}

	private static boolean isPathExists(BlockNode start, BlockNode target) {
		Set<BlockNode> visited = new HashSet<>();
		ArrayDeque<BlockNode> queue = new ArrayDeque<>();
		queue.add(start);
		while (!queue.isEmpty()) {
			BlockNode block = queue.removeFirst();
			if (block == target) {
				return true;
			}
			if (visited.add(block)) {
				queue.addAll(block.getSuccessors());
			}
		}
		return false;
	}

	private static boolean isEndBlockEntry(MethodNode mth, SpecialEdgeAttr backEdge, List<SpecialEdgeAttr> crossEdges) {
		BlockNode loopEnd = backEdge.getStart();
		SpecialEdgeAttr subEntry = ListUtils.filterOnlyOne(crossEdges, e -> e.getEnd() == loopEnd);
		if (subEntry == null) {
			return false;
		}
		dupPath(mth, subEntry.getStart(), loopEnd, backEdge.getEnd());
		mth.addDebugComment("Duplicate block (" + loopEnd + ") to fix multi-entry loop: " + backEdge);
		return true;
	}

	/**
	 * Duplicate 'center' block on path from 'start' to 'end'
	 */
	private static void dupPath(MethodNode mth, BlockNode start, BlockNode center, BlockNode end) {
		BlockNode copyCenter = BlockSplitter.insertBlockBetween(mth, start, center);
		BlockSplitter.copyBlockData(center, copyCenter);
		BlockSplitter.replaceConnection(copyCenter, center, end);
	}

	private static boolean isSingleEntryLoop(SpecialEdgeAttr e) {
		BlockNode header = e.getEnd();
		BlockNode loopEnd = e.getStart();
		return header == loopEnd
				|| loopEnd.getDoms().get(header.getId()); // header dominates loop end
	}

	static boolean isMultiEntryLoop(SpecialEdgeAttr edge) {
		return edge.getType() == SpecialEdgeType.BACK_EDGE
				&& !BlockUtils.isExceptionHandlerPath(edge.getStart())
				&& !BlockUtils.isExceptionHandlerPath(edge.getEnd())
				&& !isSingleEntryLoop(edge);
	}

	private static boolean hasNormalPath(BlockNode start, BlockNode target) {
		Set<BlockNode> visited = new HashSet<>();
		ArrayDeque<BlockNode> queue = new ArrayDeque<>();
		queue.add(start);
		while (!queue.isEmpty()) {
			BlockNode block = queue.removeFirst();
			if (block == target) {
				return true;
			}
			if (!visited.add(block)) {
				continue;
			}
			for (BlockNode successor : block.getSuccessors()) {
				if (!BlockUtils.isExceptionHandlerPath(successor)) {
					queue.add(successor);
				}
			}
		}
		return false;
	}

	private enum BlockColor {
		WHITE, GRAY, BLACK
	}

	private static void detectSpecialEdges(MethodNode mth) {
		mth.remove(AType.SPECIAL_EDGE);
		BlockColor[] colors = new BlockColor[mth.getBasicBlocks().size()];
		Arrays.fill(colors, BlockColor.WHITE);
		colorDFS(mth, colors, mth.getEnterBlock());
	}

	// TODO: transform to non-recursive form
	private static void colorDFS(MethodNode mth, BlockColor[] colors, BlockNode block) {
		colors[block.getId()] = BlockColor.GRAY;
		for (BlockNode v : block.getSuccessors()) {
			switch (colors[v.getId()]) {
				case WHITE:
					colorDFS(mth, colors, v);
					break;
				case GRAY:
					mth.addAttr(AType.SPECIAL_EDGE, new SpecialEdgeAttr(SpecialEdgeType.BACK_EDGE, block, v));
					break;
				case BLACK:
					mth.addAttr(AType.SPECIAL_EDGE, new SpecialEdgeAttr(SpecialEdgeType.CROSS_EDGE, block, v));
					break;
			}
		}
		colors[block.getId()] = BlockColor.BLACK;
	}
}
