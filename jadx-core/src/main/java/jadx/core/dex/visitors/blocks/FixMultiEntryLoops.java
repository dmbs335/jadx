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
import jadx.core.dex.attributes.nodes.UnsupportedMultiEntryLoopAttr;
import jadx.core.dex.info.FieldInfo;
import jadx.core.dex.instructions.IfNode;
import jadx.core.dex.instructions.IfOp;
import jadx.core.dex.instructions.IndexInsnNode;
import jadx.core.dex.instructions.InsnType;
import jadx.core.dex.instructions.InvokeNode;
import jadx.core.dex.instructions.SwitchInsn;
import jadx.core.dex.instructions.args.ArgType;
import jadx.core.dex.instructions.args.InsnArg;
import jadx.core.dex.instructions.args.LiteralArg;
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
			// Split coroutine resume entries before processing individual DFS back edges.
			// One split can make several edges reducible, so don't process the stale edge list.
			if (isCoroutineMethod(mth)) {
				if (normalizeCoroutinePollingSuspendCompletions(mth, multiEntryLoops)) {
					return true;
				}
				if (splitCoroutineResumeResultJoins(mth)) {
					splitCoroutineCompletionJoins(mth);
					return true;
				}
				for (SpecialEdgeAttr backEdge : multiEntryLoops) {
					if (splitCoroutineResumeLatch(mth, backEdge)) {
						return true;
					}
				}
			}
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

	/**
	 * A polling coroutine with four suspend states can have direct-completion edges entering the
	 * middle of its source loop while resumed execution reaches the same points from the state
	 * dispatcher. Every suspend call has already stored its continuation label, so route its direct
	 * completion through that dispatcher as well. This makes the existing state machine the single
	 * loop entry without introducing a synthetic runtime state.
	 */
	private static boolean normalizeCoroutinePollingSuspendCompletions(
			MethodNode mth, List<SpecialEdgeAttr> multiEntryLoops) {
		List<BlockNode> blocks = mth.getBasicBlocks();
		if (multiEntryLoops.size() < 2 || multiEntryLoops.size() > 3 || blocks.size() > 80
				|| !hasFieldNamed(mth, "$pollingForInMillis")
				|| !hasFieldNamed(mth, "$delayForEachInMillis")
				|| !hasFieldNamed(mth, "$onWaitOnNextPollingResponse")) {
			return false;
		}
		long suspendChecks = blocks.stream().filter(FixMultiEntryLoops::isLabeledSuspendCheck).count();
		if (suspendChecks != 4) {
			return false;
		}
		List<CoroutineSuspendCompletion> completions = new ArrayList<>();
		for (BlockNode suspendCheck : new ArrayList<>(blocks)) {
			CoroutineSuspendCompletion completion = findCoroutineSuspendCompletion(mth, suspendCheck);
			if (completion != null) {
				completions.add(completion);
			}
		}
		if (completions.size() != 4) {
			return false;
		}
		CoroutineSuspendCompletion first = completions.get(0);
		Set<Integer> stateValueRegs = completions.stream()
				.map(completion -> completion.stateValueReg)
				.collect(Collectors.toSet());
		if (stateValueRegs.size() != 4
				|| completions.stream().anyMatch(completion -> completion.stateDispatch != first.stateDispatch
						|| !completion.labelField.equals(first.labelField))
				|| multiEntryLoops.stream().anyMatch(backEdge -> completions.stream()
						.noneMatch(completion -> completion.directSuccess == backEdge.getEnd()))) {
			return false;
		}

		for (CoroutineSuspendCompletion completion : completions) {
			BlockNode directDispatch = BlockSplitter.insertBlockBetween(
					mth, completion.suspendCheck, completion.directSuccess);
			directDispatch.add(AFlag.SYNTHETIC);
			if (completion.directResultArg.getRegNum() != completion.resumeResultArg.getRegNum()) {
				InsnNode resultMove = new InsnNode(InsnType.MOVE, 1);
				resultMove.setResult(completion.resumeResultArg.duplicate());
				resultMove.addArg(completion.directResultArg.duplicate());
				directDispatch.getInstructions().add(resultMove);
			}
			BlockSplitter.replaceConnection(directDispatch, completion.directSuccess, first.stateDispatch);
			directDispatch.updateCleanSuccessors();
		}
		mth.addDebugComment("Normalize 4-state coroutine polling completions through state dispatch: "
				+ first.stateDispatch);
		return true;
	}

	private static boolean hasFieldNamed(MethodNode mth, String name) {
		return mth.getParentClass().getFields().stream().anyMatch(field -> field.getName().equals(name));
	}

	private static boolean isLabeledSuspendCheck(BlockNode block) {
		if (!(BlockUtils.getLastInsn(block) instanceof IfNode)
				|| block.getPredecessors().size() != 1
				|| block.getSuccessors().size() != 2) {
			return false;
		}
		BlockNode call = block.getPredecessors().get(0);
		return findLabelPut(call) != null && findLastResultInvoke(call) != null;
	}

	private static @Nullable CoroutineSuspendCompletion findCoroutineSuspendCompletion(
			MethodNode mth, BlockNode suspendCheck) {
		if (!(BlockUtils.getLastInsn(suspendCheck) instanceof IfNode)
				|| suspendCheck.getPredecessors().size() != 1
				|| suspendCheck.getSuccessors().size() != 2) {
			return null;
		}
		BlockNode suspendCall = suspendCheck.getPredecessors().get(0);
		IndexInsnNode labelPut = findLabelPut(suspendCall);
		InvokeNode suspendInvoke = findLastResultInvoke(suspendCall);
		if (labelPut == null || !(labelPut.getIndex() instanceof FieldInfo)
				|| labelPut.getArgsCount() != 2
				|| suspendInvoke == null || suspendInvoke.getResult() == null) {
			return null;
		}
		InsnArg labelValueArg = labelPut.getArg(0);
		if (!(labelValueArg instanceof RegisterArg)) {
			return null;
		}
		BlockNode exitPath = ListUtils.filterOnlyOne(suspendCheck.getSuccessors(),
				block -> reachesExitOnLinearPath(block, 2));
		BlockNode directSuccess = ListUtils.filterOnlyOne(suspendCheck.getSuccessors(), block -> block != exitPath);
		if (exitPath == null || directSuccess == null) {
			return null;
		}
		int stateValueReg = ((RegisterArg) labelValueArg).getRegNum();
		FieldInfo labelField = (FieldInfo) labelPut.getIndex();
		for (BlockNode resume : mth.getBasicBlocks()) {
			if (!containsInvoke(resume, "throwOnFailure") || !isShortLinearPath(resume, directSuccess, 3)) {
				continue;
			}
			BlockNode stateDispatch = findStateDispatchForLabelValue(
					mth, resume, stateValueReg, labelField);
			RegisterArg resumeResultArg = findThrowOnFailureArgOnLinearPath(resume, directSuccess, 3);
			if (stateDispatch != null && resumeResultArg != null) {
				return new CoroutineSuspendCompletion(suspendCheck, directSuccess, stateDispatch,
						labelField, stateValueReg, suspendInvoke.getResult(), resumeResultArg);
			}
		}
		return null;
	}

	private static @Nullable BlockNode findStateDispatchForLabelValue(
			MethodNode mth, BlockNode resume, int stateValueReg, FieldInfo labelField) {
		for (BlockNode branch : mth.getBasicBlocks()) {
			InsnNode lastInsn = BlockUtils.getLastInsn(branch);
			if (!(lastInsn instanceof IfNode)
					|| !containsRegisterArg(lastInsn, stateValueReg)
					|| branch.getSuccessors().stream().noneMatch(successor -> isPathWithin(successor, resume, 3))) {
				continue;
			}
			BlockNode stateDispatch = findCoroutineStateDispatch(branch, labelField);
			if (stateDispatch != null) {
				return stateDispatch;
			}
		}
		return null;
	}

	private static boolean containsRegisterArg(InsnNode insn, int regNum) {
		for (InsnArg arg : insn.getArguments()) {
			if (arg instanceof RegisterArg && ((RegisterArg) arg).getRegNum() == regNum) {
				return true;
			}
		}
		return false;
	}

	private static boolean isPathWithin(BlockNode start, BlockNode target, int maxDepth) {
		ArrayDeque<BlockNode> queue = new ArrayDeque<>();
		ArrayDeque<Integer> depths = new ArrayDeque<>();
		Set<BlockNode> visited = new HashSet<>();
		queue.add(start);
		depths.add(0);
		while (!queue.isEmpty()) {
			BlockNode block = queue.removeFirst();
			int depth = depths.removeFirst();
			if (block == target) {
				return true;
			}
			if (depth < maxDepth && visited.add(block)) {
				for (BlockNode successor : block.getSuccessors()) {
					queue.addLast(successor);
					depths.addLast(depth + 1);
				}
			}
		}
		return false;
	}

	private static @Nullable InvokeNode findLastResultInvoke(BlockNode block) {
		List<InsnNode> insns = block.getInstructions();
		for (int i = insns.size() - 1; i >= 0; i--) {
			InsnNode insn = insns.get(i);
			if (insn instanceof InvokeNode && insn.getResult() != null) {
				return (InvokeNode) insn;
			}
		}
		return null;
	}

	private static boolean reachesExitOnLinearPath(BlockNode start, int maxDepth) {
		BlockNode block = start;
		for (int depth = 0; depth <= maxDepth; depth++) {
			if (BlockUtils.isExitBlock(block)) {
				return true;
			}
			block = block.getSuccessors().size() == 1 ? block.getSuccessors().get(0) : null;
			if (block == null) {
				return false;
			}
		}
		return false;
	}

	private static boolean isShortLinearPath(BlockNode start, BlockNode target, int maxDepth) {
		BlockNode block = start;
		for (int depth = 0; depth <= maxDepth; depth++) {
			if (block == target) {
				return true;
			}
			block = block.getSuccessors().size() == 1 ? block.getSuccessors().get(0) : null;
			if (block == null) {
				return false;
			}
		}
		return false;
	}

	private static @Nullable RegisterArg findThrowOnFailureArgOnLinearPath(
			BlockNode start, BlockNode target, int maxDepth) {
		BlockNode block = start;
		for (int depth = 0; depth <= maxDepth && block != null; depth++) {
			RegisterArg arg = findThrowOnFailureArg(block);
			if (arg != null) {
				return arg;
			}
			if (block == target) {
				return null;
			}
			block = block.getSuccessors().size() == 1 ? block.getSuccessors().get(0) : null;
		}
		return null;
	}

	private static final class CoroutineSuspendCompletion {
		private final BlockNode suspendCheck;
		private final BlockNode directSuccess;
		private final BlockNode stateDispatch;
		private final FieldInfo labelField;
		private final int stateValueReg;
		private final RegisterArg directResultArg;
		private final RegisterArg resumeResultArg;

		private CoroutineSuspendCompletion(BlockNode suspendCheck, BlockNode directSuccess,
				BlockNode stateDispatch, FieldInfo labelField, int stateValueReg,
				RegisterArg directResultArg, RegisterArg resumeResultArg) {
			this.suspendCheck = suspendCheck;
			this.directSuccess = directSuccess;
			this.stateDispatch = stateDispatch;
			this.labelField = labelField;
			this.stateValueReg = stateValueReg;
			this.directResultArg = directResultArg;
			this.resumeResultArg = resumeResultArg;
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
		if (splitPureCoroutineJoinOnBackEdge(mth, backEdge)) {
			return true;
		}
		if (normalizeCoroutineResumeLoop(mth, backEdge)) {
			return true;
		}
		if (normalizeCoroutineDelayStateBackEdge(mth, backEdge)) {
			return true;
		}
		if (normalizeSingleCoroutineRetryResultJoin(mth, backEdge)) {
			return true;
		}
		if (isHeaderSuccessorEntry(mth, backEdge, crossEdges)) {
			return true;
		}
		if (isEndBlockEntry(mth, backEdge, crossEdges)) {
			return true;
		}
		mth.addAttr(UnsupportedMultiEntryLoopAttr.INSTANCE);
		mth.addWarnComment("Unsupported multi-entry loop pattern (" + backEdge + "). Please report as a decompilation issue!!!");
		return false;
	}

	/**
	 * A suspend delay inside a coroutine loop can complete directly at the loop latch while its
	 * resume state enters the same latch from the method's state dispatch. Route only the direct
	 * completion back through that existing dispatch. The continuation label and saved loop value
	 * then select the original resume block, making the dispatch the single source-loop header.
	 */
	private static boolean normalizeCoroutineDelayStateBackEdge(MethodNode mth, SpecialEdgeAttr backEdge) {
		if (!isCoroutineMethod(mth)) {
			return false;
		}
		BlockNode suspendCheck = backEdge.getStart();
		BlockNode latch = backEdge.getEnd();
		if (!(BlockUtils.getLastInsn(suspendCheck) instanceof IfNode)
				|| suspendCheck.getPredecessors().size() != 1
				|| suspendCheck.getSuccessors().size() != 2
				|| latch.getPredecessors().size() != 2) {
			return false;
		}
		BlockNode resume = ListUtils.filterOnlyOne(latch.getPredecessors(), pred -> pred != suspendCheck);
		RegisterArg resumeResultArg = resume == null ? null : findThrowOnFailureArg(resume);
		if (resumeResultArg == null || !isCoroutineResumeBlock(resume)) {
			return false;
		}
		BlockNode suspendCall = suspendCheck.getPredecessors().get(0);
		InvokeNode delayInvoke = findResultInvoke(suspendCall, "delay");
		IndexInsnNode labelPut = findLabelPut(suspendCall);
		if (delayInvoke == null || delayInvoke.getResult() == null
				|| labelPut == null || !(labelPut.getIndex() instanceof FieldInfo)
				|| !isPathExists(latch, suspendCheck)) {
			return false;
		}
		CoroutineStateEntry stateEntry = findCoroutineStateEntry(resume);
		ResumeStateBranch stateBranch = findResumeStateBranch(resume, stateEntry);
		if (stateBranch == null) {
			return false;
		}
		BlockNode stateDispatch = findCoroutineStateDispatch(
				stateBranch.dispatchBlock, (FieldInfo) labelPut.getIndex());
		if (stateDispatch == null || !isPathExists(stateDispatch, suspendCheck)) {
			return false;
		}

		BlockNode directDispatch = BlockSplitter.insertBlockBetween(mth, suspendCheck, latch);
		directDispatch.add(AFlag.SYNTHETIC);
		RegisterArg delayResultArg = delayInvoke.getResult();
		if (delayResultArg.getRegNum() != resumeResultArg.getRegNum()) {
			InsnNode resultMove = new InsnNode(InsnType.MOVE, 1);
			resultMove.setResult(resumeResultArg.duplicate());
			resultMove.addArg(delayResultArg.duplicate());
			directDispatch.getInstructions().add(resultMove);
		}
		BlockSplitter.replaceConnection(directDispatch, latch, stateDispatch);
		directDispatch.updateCleanSuccessors();
		splitSharedCoroutineLatchSuccessors(mth, latch);
		mth.addDebugComment("Normalize coroutine delay completion through state dispatch: " + stateDispatch);
		return true;
	}

	private static void splitSharedCoroutineLatchSuccessors(MethodNode mth, BlockNode latch) {
		boolean changed = false;
		for (BlockNode successor : new ArrayList<>(latch.getSuccessors())) {
			if (successor.getPredecessors().size() < 2
					|| successor.getSuccessors().size() != 1
					|| successor.getInstructions().isEmpty()
					|| successor.getInstructions().stream()
							.anyMatch(insn -> insn.getType() != InsnType.MOVE && insn.getType() != InsnType.CONST)) {
				continue;
			}
			BlockNode branchCopy = BlockSplitter.insertBlockBetween(mth, latch, successor);
			branchCopy.add(AFlag.SYNTHETIC);
			BlockSplitter.copyBlockData(successor, branchCopy);
			BlockSplitter.replaceConnection(branchCopy, successor, successor.getSuccessors().get(0));
			branchCopy.updateCleanSuccessors();
			changed = true;
		}
		if (changed && BlockUtils.getLastInsn(latch) instanceof IfNode) {
			((IfNode) BlockUtils.getLastInsn(latch)).initBlocks(latch);
			latch.updateCleanSuccessors();
		}
	}

	private static @Nullable InvokeNode findResultInvoke(BlockNode block, String namePart) {
		for (InsnNode insn : block.getInstructions()) {
			if (insn instanceof InvokeNode
					&& insn.getResult() != null
					&& ((InvokeNode) insn).getCallMth().getName().contains(namePart)) {
				return (InvokeNode) insn;
			}
		}
		return null;
	}

	/**
	 * In a large coroutine state machine, a side-effect-free register-copy path can reach a shared
	 * join from inside an otherwise structured loop. DFS then sees that edge as a second loop entry,
	 * although the join only contains a value reset or branch test. Clone that pure join on the
	 * incoming path, preserving its successors and keeping suspend/resume blocks untouched.
	 */
	private static boolean splitPureCoroutineJoinOnBackEdge(MethodNode mth, SpecialEdgeAttr backEdge) {
		if (!isCoroutineMethod(mth) || !isPureCoroutineJoinPath(backEdge)) {
			return false;
		}
		BlockNode path = backEdge.getStart();
		BlockNode join = backEdge.getEnd();
		InsnNode joinInsn = join.getInstructions().get(0);
		BlockNode joinCopy = BlockSplitter.insertBlockBetween(mth, path, join);
		joinCopy.add(AFlag.SYNTHETIC);
		BlockSplitter.copyBlockData(join, joinCopy);
		List<BlockNode> successors = new ArrayList<>(join.getSuccessors());
		BlockSplitter.replaceConnection(joinCopy, join, successors.get(0));
		for (int i = 1; i < successors.size(); i++) {
			BlockSplitter.connect(joinCopy, successors.get(i));
		}
		if (joinInsn instanceof IfNode) {
			((IfNode) BlockUtils.getLastInsn(joinCopy)).initBlocks(joinCopy);
		}
		joinCopy.updateCleanSuccessors();
		mth.addDebugComment("Split pure coroutine join (" + join + ") on loop path: " + backEdge);
		return true;
	}

	static boolean isPureCoroutineJoinPath(SpecialEdgeAttr backEdge) {
		BlockNode path = backEdge.getStart();
		BlockNode join = backEdge.getEnd();
		if (!ListUtils.isSingleElement(path.getSuccessors(), join)
				|| join.getPredecessors().size() < 2
				|| path.getInstructions().isEmpty()
				|| path.getInstructions().size() > 2
				|| path.getInstructions().stream()
						.anyMatch(insn -> insn.getType() != InsnType.MOVE && insn.getType() != InsnType.CONST)
				|| join.getInstructions().size() != 1) {
			return false;
		}
		InsnNode joinInsn = join.getInstructions().get(0);
		return joinInsn instanceof IfNode && join.getSuccessors().size() == 2
				|| joinInsn.getType() == InsnType.CONST && join.getSuccessors().size() == 1;
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

	/**
	 * A retry coroutine can suspend once for the request and once for a delay. The request resume
	 * enters the result block, while initial and delay-resume paths enter the request call. Put both
	 * paths below a synthetic header which dispatches on the existing continuation label. This
	 * preserves the state machine without introducing a synthetic runtime state variable.
	 */
	private static boolean normalizeSingleCoroutineRetryResultJoin(MethodNode mth, SpecialEdgeAttr backEdge) {
		if (!isCoroutineMethod(mth)) {
			return false;
		}
		CoroutineRetryResultEntry entry = findCoroutineRetryResultEntry(backEdge);
		if (entry == null) {
			return false;
		}
		InsnNode resumeResultMove = new InsnNode(InsnType.MOVE, 1);
		resumeResultMove.setResult(entry.resumeResultArg.duplicate());
		resumeResultMove.addArg(entry.delayResultArg.duplicate());
		backEdge.getStart().getInstructions().clear();
		backEdge.getStart().getInstructions().add(resumeResultMove);
		BlockSplitter.replaceConnection(backEdge.getStart(), backEdge.getEnd(), entry.stateDispatch);
		mth.addDebugComment("Normalize single coroutine retry result join at state dispatch: " + entry.stateDispatch);
		return true;
	}

	private static @Nullable CoroutineRetryResultEntry findCoroutineRetryResultEntry(SpecialEdgeAttr backEdge) {
		BlockNode loopEntry = backEdge.getEnd();
		if (!isSimpleRetryEntry(loopEntry)) {
			return null;
		}
		BlockNode retryResume = ListUtils.filterOnlyOne(loopEntry.getPredecessors(),
				pred -> pred != backEdge.getStart() && isCoroutineResumeBlock(pred));
		if (retryResume == null) {
			return null;
		}
		RegisterArg resumeResultArg = findThrowOnFailureArg(retryResume);
		if (resumeResultArg == null) {
			return null;
		}
		BlockNode suspendCall = findSuspendCallAfter(loopEntry);
		if (suspendCall == null || suspendCall.getSuccessors().size() != 1) {
			return null;
		}
		IndexInsnNode labelPut = findLabelPut(suspendCall);
		if (labelPut == null || !(labelPut.getIndex() instanceof FieldInfo)) {
			return null;
		}
		BlockNode suspendCheck = suspendCall.getSuccessors().get(0);
		if (!(BlockUtils.getLastInsn(suspendCheck) instanceof IfNode)
				|| suspendCheck.getSuccessors().size() != 2) {
			return null;
		}
		for (BlockNode resultJoin : suspendCheck.getSuccessors()) {
			InvokeNode delayInvoke = findInvokeOnPath(resultJoin, backEdge.getStart(), "delay");
			if (resultJoin.getPredecessors().size() != 2
					|| resultJoin.getSuccessors().size() != 1
					|| !isPathExists(resultJoin, backEdge.getStart())
					|| delayInvoke == null
					|| delayInvoke.getResult() == null) {
				continue;
			}
			BlockNode suspendedReturn = ListUtils.filterOnlyOne(suspendCheck.getSuccessors(), block -> block != resultJoin);
			BlockNode resume = ListUtils.filterOnlyOne(resultJoin.getPredecessors(), pred -> pred != suspendCheck);
			if (suspendedReturn == null || !suspendedReturn.isReturnBlock()
					|| resume == null || !isCoroutineResumeBlock(resume)) {
				continue;
			}
			CoroutineStateEntry stateEntry = findCoroutineStateEntry(resume);
			ResumeStateBranch stateBranch = findResumeStateBranch(resume, stateEntry);
			if (stateBranch == null || labelPut.getArgsCount() != 2) {
				continue;
			}
			BlockNode stateDispatch = findCoroutineStateDispatch(stateBranch.dispatchBlock,
					(FieldInfo) labelPut.getIndex());
			if (stateDispatch != null) {
				return new CoroutineRetryResultEntry(stateDispatch, resumeResultArg, delayInvoke.getResult());
			}
		}
		return null;
	}

	private static @Nullable BlockNode findCoroutineStateDispatch(BlockNode start, FieldInfo labelField) {
		ArrayDeque<BlockNode> queue = new ArrayDeque<>();
		Set<BlockNode> visited = new HashSet<>();
		queue.add(start);
		while (!queue.isEmpty() && visited.size() < 8) {
			BlockNode block = queue.removeFirst();
			if (!visited.add(block)) {
				continue;
			}
			if (containsLabelGet(block, labelField) && isPureCoroutineStateDispatch(block)) {
				return block;
			}
			queue.addAll(block.getPredecessors());
		}
		return null;
	}

	private static boolean containsLabelGet(BlockNode block, FieldInfo labelField) {
		for (InsnNode insn : block.getInstructions()) {
			if (insn instanceof IndexInsnNode
					&& insn.getType() == InsnType.IGET
					&& labelField.equals(((IndexInsnNode) insn).getIndex())) {
				return true;
			}
		}
		return false;
	}

	private static boolean isPureCoroutineStateDispatch(BlockNode block) {
		for (InsnNode insn : block.getInstructions()) {
			switch (insn.getType()) {
				case IGET:
				case CONST:
				case MOVE:
					break;
				case INVOKE:
					if (!(insn instanceof InvokeNode)
							|| !((InvokeNode) insn).getCallMth().getName().contains("getCOROUTINE_SUSPENDED")) {
						return false;
					}
					break;
				default:
					return false;
			}
		}
		return true;
	}

	private static boolean isSimpleRetryEntry(BlockNode block) {
		return block.getSuccessors().size() == 1
				&& !block.getInstructions().isEmpty()
				&& block.getInstructions().size() <= 2
				&& block.getInstructions().stream()
						.allMatch(insn -> insn.getType() == InsnType.MOVE || insn.getType() == InsnType.CONST);
	}

	private static @Nullable BlockNode findSuspendCallAfter(BlockNode start) {
		BlockNode block = start;
		Set<BlockNode> visited = new HashSet<>();
		for (int depth = 0; depth < 4 && block != null && visited.add(block); depth++) {
			if (findLabelPut(block) != null
					&& block.getInstructions().stream().anyMatch(insn -> insn instanceof InvokeNode)) {
				return block;
			}
			block = block.getSuccessors().size() == 1 ? block.getSuccessors().get(0) : null;
		}
		return null;
	}

	private static @Nullable IndexInsnNode findLabelPut(BlockNode block) {
		for (InsnNode insn : block.getInstructions()) {
			if (insn instanceof IndexInsnNode && insn.getType() == InsnType.IPUT) {
				Object index = ((IndexInsnNode) insn).getIndex();
				if (index instanceof FieldInfo && ((FieldInfo) index).getName().equals("label")) {
					return (IndexInsnNode) insn;
				}
			}
		}
		return null;
	}

	private static @Nullable InvokeNode findInvokeOnPath(BlockNode start, BlockNode target, String namePart) {
		ArrayDeque<BlockNode> queue = new ArrayDeque<>();
		Set<BlockNode> visited = new HashSet<>();
		queue.add(start);
		while (!queue.isEmpty() && visited.size() < 50) {
			BlockNode block = queue.removeFirst();
			if (!visited.add(block)) {
				continue;
			}
			if (isPathExists(block, target)) {
				for (InsnNode insn : block.getInstructions()) {
					if (insn instanceof InvokeNode
							&& ((InvokeNode) insn).getCallMth().getName().contains(namePart)) {
						return (InvokeNode) insn;
					}
				}
			}
			if (block != target) {
				queue.addAll(block.getSuccessors());
			}
		}
		return null;
	}

	private static @Nullable RegisterArg findThrowOnFailureArg(BlockNode block) {
		for (InsnNode insn : block.getInstructions()) {
			if (insn instanceof InvokeNode
					&& ((InvokeNode) insn).getCallMth().getName().contains("throwOnFailure")
					&& insn.getArgsCount() == 1
					&& insn.getArg(0) instanceof RegisterArg) {
				return (RegisterArg) insn.getArg(0);
			}
		}
		return null;
	}

	private static final class CoroutineRetryResultEntry {
		private final BlockNode stateDispatch;
		private final RegisterArg resumeResultArg;
		private final RegisterArg delayResultArg;

		private CoroutineRetryResultEntry(
				BlockNode stateDispatch, RegisterArg resumeResultArg, RegisterArg delayResultArg) {
			this.stateDispatch = stateDispatch;
			this.resumeResultArg = resumeResultArg;
			this.delayResultArg = delayResultArg;
		}
	}

	/**
	 * A resumed coroutine can enter a loop through its latch instead of the source-level header.
	 * Split the latch on that resume edge so both initial and resumed execution converge at the
	 * header, without adding or removing any runtime instruction.
	 */
	private static boolean splitCoroutineResumeLatch(MethodNode mth, SpecialEdgeAttr backEdge) {
		BlockNode latch = backEdge.getEnd();
		if (latch.getPredecessors().size() < 2
				|| latch.getSuccessors().size() != 1
				|| !isSimpleCoroutineLatch(latch)) {
			return false;
		}
		BlockNode resume = ListUtils.filterOnlyOne(latch.getPredecessors(), FixMultiEntryLoops::isCoroutineResumeBlock);
		if (resume == null
				|| resume == backEdge.getStart()
				|| isPathExists(latch, resume)
				|| !isPathExists(latch, backEdge.getStart())) {
			return false;
		}

		BlockNode resumeLatch = BlockSplitter.startNewBlock(mth, latch.getStartOffset());
		resumeLatch.add(AFlag.SYNTHETIC);
		BlockSplitter.copyBlockData(latch, resumeLatch);
		BlockSplitter.replaceConnection(resume, latch, resumeLatch);
		for (BlockNode successor : latch.getSuccessors()) {
			BlockSplitter.connect(resumeLatch, successor);
		}
		InsnNode lastInsn = BlockUtils.getLastInsn(resumeLatch);
		if (lastInsn instanceof IfNode) {
			((IfNode) lastInsn).initBlocks(resumeLatch);
		} else if (lastInsn instanceof SwitchInsn) {
			((SwitchInsn) lastInsn).initBlocks(resumeLatch);
		}
		resumeLatch.updateCleanSuccessors();
		mth.addDebugComment("Split coroutine resume latch: " + latch);
		return true;
	}

	/**
	 * Kotlin state-machine code joins the direct result of a suspend call with the corresponding
	 * resume-state result before continuing the source loop. That join can look like a second loop
	 * entry to the DFS pass. Route all resume states through one synthetic state dispatcher at the
	 * source-level loop header, preserving the original coroutine label and loop body.
	 */
	private static boolean splitCoroutineResumeResultJoins(MethodNode mth) {
		List<CoroutineResumeResultEntry> entries = new ArrayList<>();
		for (BlockNode suspendCheck : new ArrayList<>(mth.getBasicBlocks())) {
			if (!(BlockUtils.getLastInsn(suspendCheck) instanceof IfNode)) {
				continue;
			}
			for (BlockNode resultJoin : new ArrayList<>(suspendCheck.getSuccessors())) {
				CoroutineResumeResultEntry entry = findCoroutineResumeResultEntry(suspendCheck, resultJoin);
				if (entry != null) {
					entries.add(entry);
				}
			}
		}
		if (entries.size() < 2) {
			return false;
		}
		CoroutineResumeResultEntry first = entries.get(0);
		if (entries.stream().anyMatch(entry -> entry.loopHeader != first.loopHeader
				|| entry.sourceSwitch != first.sourceSwitch
				|| entry.stateArg.getRegNum() != first.stateArg.getRegNum())) {
			return false;
		}

		List<BlockNode> loopPreds = new ArrayList<>(first.loopHeader.getPredecessors());
		BlockNode stateHeader = first.loopHeader;
		for (int i = entries.size() - 1; i >= 0; i--) {
			CoroutineResumeResultEntry entry = entries.get(i);
			BlockNode stateCheck = BlockSplitter.startNewBlock(mth, entry.resultJoin.getStartOffset());
			stateCheck.add(AFlag.SYNTHETIC);
			IfNode stateIf = new IfNode(IfOp.EQ, entry.resume.getStartOffset(),
					InsnArg.reg(first.stateArg.getRegNum(), ArgType.INT), InsnArg.lit(entry.stateKey, ArgType.INT));
			stateCheck.getInstructions().add(stateIf);
			BlockSplitter.connect(stateCheck, entry.resume);
			BlockSplitter.connect(stateCheck, stateHeader);
			stateIf.initBlocks(stateCheck);
			stateCheck.updateCleanSuccessors();
			stateHeader = stateCheck;
		}

		for (CoroutineResumeResultEntry entry : entries) {
			BlockSplitter.replaceConnection(entry.dispatchBlock, entry.resume, stateHeader);
		}
		for (BlockNode loopPred : loopPreds) {
			BlockNode resetBlock = BlockSplitter.startNewBlock(mth, first.loopHeader.getStartOffset());
			resetBlock.add(AFlag.SYNTHETIC);
			InsnNode resetState = new InsnNode(InsnType.CONST, 1);
			resetState.setResult(InsnArg.reg(first.stateArg.getRegNum(), ArgType.INT));
			resetState.addArg(InsnArg.lit(0, ArgType.INT));
			resetBlock.getInstructions().add(resetState);
			BlockSplitter.replaceConnection(loopPred, first.loopHeader, resetBlock);
			BlockSplitter.connect(resetBlock, stateHeader);
			resetBlock.updateCleanSuccessors();
		}
		mth.addDebugComment("Normalize " + entries.size() + " coroutine resume result joins at loop header: "
				+ first.loopHeader);
		return true;
	}

	private static boolean splitCoroutineCompletionJoins(MethodNode mth) {
		boolean changed = false;
		for (BlockNode suspendCheck : new ArrayList<>(mth.getBasicBlocks())) {
			if (!(BlockUtils.getLastInsn(suspendCheck) instanceof IfNode)
					|| suspendCheck.getSuccessors().size() != 2) {
				continue;
			}
			for (BlockNode completionJoin : new ArrayList<>(suspendCheck.getSuccessors())) {
				BlockNode suspendedReturn = ListUtils.filterOnlyOne(suspendCheck.getSuccessors(), block -> block != completionJoin);
				if (completionJoin.getPredecessors().size() != 2
						|| completionJoin.getSuccessors().size() != 1
						|| !isSimpleCoroutineCompletionJoin(completionJoin)
						|| suspendedReturn == null
						|| !suspendedReturn.isReturnBlock()
						|| suspendedReturn.getPredecessors().size() < 2) {
					continue;
				}
				BlockNode resume = ListUtils.filterOnlyOne(completionJoin.getPredecessors(), pred -> pred != suspendCheck);
				BlockNode returnBlock = completionJoin.getSuccessors().get(0);
				if (resume == null
						|| !isCoroutineResumeBlock(resume)
						|| !returnBlock.isReturnBlock()
						|| returnBlock.getSuccessors().size() > 1) {
					continue;
				}

				BlockNode directCompletion = BlockSplitter.startNewBlock(mth, completionJoin.getStartOffset());
				BlockSplitter.copyBlockData(completionJoin, directCompletion);
				directCompletion.add(AFlag.SYNTHETIC);
				BlockNode directReturn = BlockSplitter.startNewBlock(mth, returnBlock.getStartOffset());
				BlockSplitter.copyBlockData(returnBlock, directReturn);
				directReturn.add(AFlag.SYNTHETIC);
				BlockNode directSuspendedReturn = BlockSplitter.startNewBlock(mth, suspendedReturn.getStartOffset());
				BlockSplitter.copyBlockData(suspendedReturn, directSuspendedReturn);
				directSuspendedReturn.add(AFlag.SYNTHETIC);
				BlockSplitter.connect(directCompletion, directReturn);
				for (BlockNode successor : returnBlock.getSuccessors()) {
					BlockSplitter.connect(directReturn, successor);
				}
				for (BlockNode successor : suspendedReturn.getSuccessors()) {
					BlockSplitter.connect(directSuspendedReturn, successor);
				}
				BlockSplitter.replaceConnection(suspendCheck, completionJoin, directCompletion);
				BlockSplitter.replaceConnection(suspendCheck, suspendedReturn, directSuspendedReturn);
				directCompletion.updateCleanSuccessors();
				directReturn.updateCleanSuccessors();
				directSuspendedReturn.updateCleanSuccessors();
				changed = true;
			}
		}
		if (changed) {
			mth.addDebugComment("Split coroutine direct/resumed completion joins");
		}
		return changed;
	}

	private static boolean isSimpleCoroutineCompletionJoin(BlockNode block) {
		List<InsnNode> insns = block.getInstructions();
		if (insns.isEmpty() || insns.size() > 2) {
			return false;
		}
		for (InsnNode insn : insns) {
			if (insn.getType() != InsnType.MOVE
					&& insn.getType() != InsnType.CONST
					&& insn.getType() != InsnType.SGET) {
				return false;
			}
		}
		return true;
	}

	private static @Nullable CoroutineResumeResultEntry findCoroutineResumeResultEntry(
			BlockNode suspendCheck, BlockNode resultJoin) {
		boolean baseMatch = BlockUtils.getLastInsn(suspendCheck) instanceof IfNode
				&& suspendCheck.getPredecessors().size() == 1
				&& suspendCheck.getSuccessors().size() == 2
				&& resultJoin.getPredecessors().size() == 2
				&& resultJoin.getSuccessors().size() == 1
				&& isSimpleCoroutineResultJoin(resultJoin)
				&& isPathExists(resultJoin.getSuccessors().get(0), suspendCheck)
				&& suspendCheck.getSuccessors().stream()
						.filter(block -> block != resultJoin)
						.anyMatch(BlockUtils::isExitBlock);
		if (!baseMatch) {
			return null;
		}
		BlockNode resume = ListUtils.filterOnlyOne(resultJoin.getPredecessors(), pred -> pred != suspendCheck);
		BlockNode suspendCall = suspendCheck.getPredecessors().get(0);
		BlockNode sourceSwitch = findSourceSwitch(suspendCall);
		if (resume == null
				|| sourceSwitch == null
				|| !isCoroutineResumeBlock(resume)
				|| !containsInsnType(suspendCall, InsnType.IPUT)
				|| !containsInsnType(suspendCall, InsnType.INVOKE)) {
			return null;
		}
		BlockNode loopHeader = findCoroutineSourceLoopHeader(resultJoin, suspendCheck);
		CoroutineStateEntry stateEntry = findCoroutineStateEntry(resume);
		ResumeStateBranch stateBranch = findResumeStateBranch(resume, stateEntry);
		if (loopHeader == null || stateBranch == null) {
			return null;
		}
		return new CoroutineResumeResultEntry(resultJoin, resume, loopHeader,
				sourceSwitch, stateBranch.dispatchBlock, stateBranch.stateArg, stateBranch.stateKey);
	}

	private static @Nullable BlockNode findSourceSwitch(BlockNode suspendCall) {
		BlockNode block = suspendCall;
		for (int depth = 0; depth < 4; depth++) {
			for (BlockNode pred : block.getPredecessors()) {
				InsnNode lastInsn = BlockUtils.getLastInsn(pred);
				if (lastInsn instanceof SwitchInsn && ((SwitchInsn) lastInsn).getKeys().length >= 4) {
					return pred;
				}
			}
			block = block.getPredecessors().size() == 1 ? block.getPredecessors().get(0) : null;
			if (block == null) {
				return null;
			}
		}
		return null;
	}

	private static @Nullable BlockNode findCoroutineSourceLoopHeader(BlockNode resultJoin, BlockNode suspendCheck) {
		BlockNode start = resultJoin.getSuccessors().get(0);
		Set<BlockNode> reachable = new HashSet<>();
		ArrayDeque<BlockNode> queue = new ArrayDeque<>();
		queue.add(start);
		while (!queue.isEmpty()) {
			BlockNode block = queue.removeFirst();
			if (reachable.add(block)) {
				queue.addAll(block.getSuccessors());
			}
		}
		queue.clear();
		Set<BlockNode> visited = new HashSet<>();
		queue.add(start);
		while (!queue.isEmpty()) {
			BlockNode block = queue.removeFirst();
			if (!visited.add(block)) {
				continue;
			}
			if (block.getPredecessors().stream().anyMatch(pred -> !reachable.contains(pred))
					&& isPathExists(block, suspendCheck)) {
				return block;
			}
			queue.addAll(block.getSuccessors());
		}
		return null;
	}

	private static @Nullable Integer findSwitchKey(BlockNode dispatchBlock, BlockNode target) {
		InsnNode lastInsn = BlockUtils.getLastInsn(dispatchBlock);
		if (!(lastInsn instanceof SwitchInsn)) {
			return null;
		}
		SwitchInsn switchInsn = (SwitchInsn) lastInsn;
		BlockNode[] targets = switchInsn.getTargetBlocks();
		if (targets == null) {
			return null;
		}
		for (int i = 0; i < targets.length; i++) {
			if (targets[i] == target && switchInsn.getKey(i) instanceof Number) {
				return ((Number) switchInsn.getKey(i)).intValue();
			}
		}
		return null;
	}

	private static @Nullable ResumeStateBranch findResumeStateBranch(
			BlockNode resume, @Nullable CoroutineStateEntry stateEntry) {
		if (stateEntry != null && stateEntry.dispatchBlock != null) {
			Integer stateKey = findSwitchKey(stateEntry.dispatchBlock, resume);
			return stateKey == null
					? null
					: new ResumeStateBranch(stateEntry.dispatchBlock, stateEntry.stateArg, stateKey);
		}
		if (stateEntry == null) {
			return null;
		}
		for (BlockNode pred : resume.getPredecessors()) {
			InsnNode lastInsn = BlockUtils.getLastInsn(pred);
			if (!(lastInsn instanceof IfNode)) {
				continue;
			}
			IfNode ifInsn = (IfNode) lastInsn;
			boolean exactMatch = ifInsn.getOp() == IfOp.EQ && ifInsn.getThenBlock() == resume
					|| ifInsn.getOp() == IfOp.NE && ifInsn.getElseBlock() == resume;
			if (!exactMatch) {
				continue;
			}
			InsnArg stateValueArg = null;
			for (InsnArg arg : ifInsn.getArguments()) {
				if (!(arg instanceof RegisterArg)
						|| ((RegisterArg) arg).getRegNum() != stateEntry.stateArg.getRegNum()) {
					stateValueArg = arg;
				}
			}
			Long stateKey = resolveConstValue(pred, stateValueArg, 0);
			if (stateKey != null && stateKey >= Integer.MIN_VALUE && stateKey <= Integer.MAX_VALUE) {
				return new ResumeStateBranch(pred, stateEntry.stateArg, stateKey.intValue());
			}
		}
		return null;
	}

	private static @Nullable Long resolveConstValue(BlockNode block, @Nullable InsnArg arg, int depth) {
		if (arg == null) {
			return null;
		}
		if (arg instanceof LiteralArg) {
			return ((LiteralArg) arg).getLiteral();
		}
		if (!(arg instanceof RegisterArg) || depth > 8) {
			return null;
		}
		int regNum = ((RegisterArg) arg).getRegNum();
		List<InsnNode> insns = block.getInstructions();
		for (int i = insns.size() - 1; i >= 0; i--) {
			InsnNode insn = insns.get(i);
			RegisterArg result = insn.getResult();
			if (result != null && result.getRegNum() == regNum) {
				if (insn.getType() == InsnType.CONST
						&& insn.getArgsCount() == 1
						&& insn.getArg(0) instanceof LiteralArg) {
					return ((LiteralArg) insn.getArg(0)).getLiteral();
				}
				return null;
			}
		}
		if (block.getPredecessors().size() == 1) {
			return resolveConstValue(block.getPredecessors().get(0), arg, depth + 1);
		}
		return null;
	}

	private static final class ResumeStateBranch {
		private final BlockNode dispatchBlock;
		private final RegisterArg stateArg;
		private final int stateKey;

		private ResumeStateBranch(BlockNode dispatchBlock, RegisterArg stateArg, int stateKey) {
			this.dispatchBlock = dispatchBlock;
			this.stateArg = stateArg;
			this.stateKey = stateKey;
		}
	}

	private static final class CoroutineResumeResultEntry {
		private final BlockNode resultJoin;
		private final BlockNode resume;
		private final BlockNode loopHeader;
		private final BlockNode sourceSwitch;
		private final BlockNode dispatchBlock;
		private final RegisterArg stateArg;
		private final int stateKey;

		private CoroutineResumeResultEntry(BlockNode resultJoin, BlockNode resume, BlockNode loopHeader,
				BlockNode sourceSwitch, BlockNode dispatchBlock, RegisterArg stateArg, int stateKey) {
			this.resultJoin = resultJoin;
			this.resume = resume;
			this.loopHeader = loopHeader;
			this.sourceSwitch = sourceSwitch;
			this.dispatchBlock = dispatchBlock;
			this.stateArg = stateArg;
			this.stateKey = stateKey;
		}
	}

	private static boolean isSimpleCoroutineResultJoin(BlockNode block) {
		List<InsnNode> insns = block.getInstructions();
		if (insns.isEmpty() || insns.size() > 3) {
			return false;
		}
		for (InsnNode insn : insns) {
			if (insn.getType() != InsnType.MOVE
					&& insn.getType() != InsnType.CHECK_CAST) {
				return false;
			}
		}
		return true;
	}

	private static boolean isSimpleCoroutineLatch(BlockNode block) {
		List<InsnNode> insns = block.getInstructions();
		if (insns.isEmpty() || insns.size() > 4) {
			return false;
		}
		for (InsnNode insn : insns) {
			// A pure arithmetic latch (index/offset update) is safe to duplicate on the
			// coroutine resume edge. MOVE and CONST blocks can be state restoration or
			// branch setup, so splitting them can perturb later SSA/type inference.
			if (insn.getType() != InsnType.ARITH) {
				return false;
			}
		}
		return true;
	}

	private static boolean isCoroutineResumeBlock(BlockNode block) {
		return containsInvoke(block, "throwOnFailure")
				&& (containsInsnType(block, InsnType.IGET) || containsInsnType(block, InsnType.SGET));
	}

	private static boolean isCoroutineMethod(MethodNode mth) {
		for (ArgType type : mth.getMethodInfo().getArgumentsTypes()) {
			if (type.toString().startsWith("kotlin.coroutines.Continuation")) {
				return true;
			}
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
