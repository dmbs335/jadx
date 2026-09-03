package jadx.core.dex.visitors.regions.maker;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jadx.core.dex.attributes.AFlag;
import jadx.core.dex.attributes.AType;
import jadx.core.dex.attributes.nodes.LoopInfo;
import jadx.core.dex.attributes.nodes.LoopLabelAttr;
import jadx.core.dex.attributes.nodes.PhiListAttr;
import jadx.core.dex.attributes.nodes.RegionRefAttr;
import jadx.core.dex.instructions.InsnType;
import jadx.core.dex.instructions.InvokeNode;
import jadx.core.dex.instructions.SwitchInsn;
import jadx.core.dex.instructions.args.ArgType;
import jadx.core.dex.instructions.args.InsnArg;
import jadx.core.dex.instructions.args.RegisterArg;
import jadx.core.dex.nodes.BlockNode;
import jadx.core.dex.nodes.IBlock;
import jadx.core.dex.nodes.IContainer;
import jadx.core.dex.nodes.IRegion;
import jadx.core.dex.nodes.InsnContainer;
import jadx.core.dex.nodes.InsnNode;
import jadx.core.dex.nodes.MethodNode;
import jadx.core.dex.regions.Region;
import jadx.core.dex.regions.SwitchRegion;
import jadx.core.dex.regions.SwitchRegion.CaseInfo;
import jadx.core.dex.regions.TryCatchRegion;
import jadx.core.dex.regions.loops.LoopRegion;
import jadx.core.dex.trycatch.CatchAttr;
import jadx.core.dex.trycatch.ExceptionHandler;
import jadx.core.dex.visitors.kotlin.KtorCioRecovery;
import jadx.core.dex.visitors.regions.AbstractRegionVisitor;
import jadx.core.dex.visitors.regions.DepthRegionTraversal;
import jadx.core.dex.visitors.regions.SwitchBreakVisitor;
import jadx.core.utils.BlockUtils;
import jadx.core.utils.ListUtils;
import jadx.core.utils.RegionUtils;
import jadx.core.utils.Utils;
import jadx.core.utils.blocks.BlockSet;

public final class SwitchRegionMaker {
	private final MethodNode mth;
	private final RegionMaker regionMaker;

	private static final Logger LOG = LoggerFactory.getLogger(SwitchRegionMaker.class);

	SwitchRegionMaker(MethodNode mth, RegionMaker regionMaker) {
		this.mth = mth;
		this.regionMaker = regionMaker;
	}

	BlockNode process(IRegion currentRegion, BlockNode block, SwitchInsn insn, RegionStack stack) {
		// map case blocks to keys
		int len = insn.getTargets().length;
		Map<BlockNode, List<Object>> blocksMap = new LinkedHashMap<>(len);
		BlockNode[] targetBlocksArr = insn.getTargetBlocks();
		for (int i = 0; i < len; i++) {
			List<Object> keys = blocksMap.computeIfAbsent(targetBlocksArr[i], k -> new ArrayList<>(2));
			keys.add(insn.getKey(i));
		}
		BlockNode defCase = insn.getDefTargetBlock();
		if (defCase != null) {
			List<Object> keys = blocksMap.computeIfAbsent(defCase, k -> new ArrayList<>(1));
			keys.add(SwitchRegion.DEFAULT_CASE_KEY);
		}

		SwitchRegion sw = new SwitchRegion(currentRegion, block, insn);
		insn.addAttr(new RegionRefAttr(sw));
		currentRegion.getSubBlocks().add(sw);
		stack.push(sw);

		BlockNode out = calcSwitchOut(block, insn, stack);
		BlockNode originalOut = out;
		BlockNode commonNormalCaseOut = findCommonNormalCaseOut(out, blocksMap, stack);
		if (commonNormalCaseOut != null) {
			LoopInfo loop = getInnermostLoop(block);
			if (loop != null && hasCasePathAvoidingOut(blocksMap, commonNormalCaseOut, loop)) {
				if (hasExecutableLoopTail(loop.getEnd())) {
					commonNormalCaseOut = null;
				} else {
					insertContinueInSwitch(block, commonNormalCaseOut, loop.getEnd());
				}
			}
			out = commonNormalCaseOut != null ? commonNormalCaseOut : originalOut;
		}
		BlockNode sharedCaseOut = detectSharedCaseOut(blocksMap);
		if (sharedCaseOut != null && shouldUseSharedCaseOut(out, sharedCaseOut)) {
			out = sharedCaseOut;
		}
		BlockNode ktorCioDirectReadTail = findKtorCioDirectReadTail(out, blocksMap);
		if (ktorCioDirectReadTail != null) {
			mth.addDebugComment("Share Ktor CIO direct-reader close tail at " + ktorCioDirectReadTail);
			out = ktorCioDirectReadTail;
		}
		stack.addExit(out);
		markAcyclicSharedCasePaths(out, blocksMap.keySet());

		addCases(sw, out, stack, blocksMap);
		removeEmptyCases(insn, sw, defCase, out);

		stack.pop();
		return out;
	}

	/**
	 * A DEX switch can share an action suffix between several mutually exclusive case entries. Java
	 * has no label for an arbitrary basic block, so region rendering may spell that suffix in each
	 * case. This is source-level duplication, not repeated runtime execution. Mark only acyclic blocks
	 * reached from at least two distinct case entries, stopping at the owned switch out and loops.
	 */
	private void markAcyclicSharedCasePaths(@Nullable BlockNode out, Set<BlockNode> caseEntries) {
		if (out == null || caseEntries.size() < 2) {
			return;
		}
		Map<BlockNode, Integer> reachCount = new HashMap<>();
		for (BlockNode caseEntry : caseEntries) {
			Set<BlockNode> visited = new HashSet<>();
			ArrayDeque<BlockNode> queue = new ArrayDeque<>();
			queue.add(caseEntry);
			while (!queue.isEmpty() && visited.size() <= 256) {
				BlockNode current = queue.removeFirst();
				if (current == out
						|| current.contains(AFlag.LOOP_START)
						|| current.contains(AFlag.LOOP_END)
						|| !visited.add(current)) {
					continue;
				}
				for (BlockNode successor : current.getCleanSuccessors()) {
					queue.addLast(successor);
				}
			}
			if (visited.size() > 256) {
				continue;
			}
			for (BlockNode reached : visited) {
				reachCount.merge(reached, 1, Integer::sum);
			}
		}
		for (Map.Entry<BlockNode, Integer> entry : reachCount.entrySet()) {
			if (entry.getValue() >= 2) {
				regionMaker.registerSafeSwitchSharedDuplication(entry.getKey());
			}
		}
	}

	/**
	 * Insert 'break' for all cases in switch region
	 * Executed in {@link jadx.core.dex.visitors.regions.PostProcessRegions} after try/catch wrap to
	 * handle all blocks
	 */
	public static void insertBreaks(MethodNode mth, SwitchRegion sw) {
		for (SwitchRegion.CaseInfo caseInfo : sw.getCases()) {
			insertBreaksForCase(mth, sw, caseInfo.getContainer());
		}
	}

	private void addCases(SwitchRegion sw, @Nullable BlockNode out,
			RegionStack stack, Map<BlockNode, List<Object>> blocksMap) {
		Map<BlockNode, BlockNode> fallThroughCases = new LinkedHashMap<>();
		if (out != null) {
			// detect fallthrough cases
			BitSet caseBlocks = BlockUtils.blocksToBitSet(mth, blocksMap.keySet());
			caseBlocks.clear(out.getPos());
			for (BlockNode successor : sw.getHeader().getSuccessors()) {
				BitSet df = successor.getDomFrontier();
				if (df.intersects(caseBlocks)) {
					BlockNode fallThroughBlock = getOneIntersectionBlock(out, caseBlocks, df);
					fallThroughCases.put(successor, fallThroughBlock);
				}
			}
			// check fallthrough cases order
			if (!fallThroughCases.isEmpty() && isBadCasesOrder(blocksMap, fallThroughCases)) {
				Map<BlockNode, List<Object>> newBlocksMap = reOrderSwitchCases(blocksMap, fallThroughCases);
				if (isBadCasesOrder(newBlocksMap, fallThroughCases)) {
					// Keep the original case order and let region construction preserve the CFG by
					// duplicating paths where needed. RegionMaker reports a warning separately if
					// that duplication is unsafe, so emitting a warning here is only a prediction
					// and produces false positives for terminal/shared-suffix case paths.
					LOG.debug("Can't reorder switch cases in {}, using CFG-preserving fallback", mth);
					fallThroughCases.clear();
				} else {
					blocksMap = newBlocksMap;
				}
			}
		}
		for (Map.Entry<BlockNode, List<Object>> entry : blocksMap.entrySet()) {
			List<Object> keysList = entry.getValue();
			BlockNode caseBlock = entry.getKey();
			Region caseRegion;
			if (stack.containsExit(caseBlock)) {
				caseRegion = new Region(stack.peekRegion());
			} else {
				BlockNode next = fallThroughCases.get(caseBlock);
				stack.addExit(next);
				caseRegion = regionMaker.makeRegion(caseBlock);
				stack.removeExit(next);
				if (next != null) {
					next.add(AFlag.FALL_THROUGH);
					caseRegion.add(AFlag.FALL_THROUGH);
				}
			}
			sw.addCase(keysList, caseRegion);
		}
	}

	/**
	 * Detect a switch case used as a shared exit by every other case. This shape is common in
	 * compiler generated string switches: successful comparisons leave the switch, while all
	 * failed comparisons converge on the default case. Treating these edges as fall-throughs
	 * requires placing several cases directly before the same target, which is impossible.
	 */
	private @Nullable BlockNode detectSharedCaseOut(Map<BlockNode, List<Object>> blocksMap) {
		int expectedSources = blocksMap.size() - 1;
		if (expectedSources < 2) {
			return null;
		}
		boolean possibleSharedTarget = false;
		for (BlockNode caseBlock : blocksMap.keySet()) {
			if (caseBlock.getPredecessors().size() >= expectedSources) {
				possibleSharedTarget = true;
				break;
			}
		}
		if (!possibleSharedTarget) {
			return null;
		}
		BitSet caseBlocks = BlockUtils.blocksToBitSet(mth, blocksMap.keySet());
		Map<BlockNode, Integer> targetCounts = new LinkedHashMap<>();
		for (BlockNode caseBlock : blocksMap.keySet()) {
			BitSet intersections = BlockUtils.copyBlocksBitSet(mth, caseBlock.getDomFrontier());
			intersections.and(caseBlocks);
			BlockNode target = BlockUtils.bitSetToOneBlock(mth, intersections);
			if (target != null && target != caseBlock) {
				targetCounts.merge(target, 1, Integer::sum);
			}
		}
		for (Map.Entry<BlockNode, Integer> entry : targetCounts.entrySet()) {
			if (entry.getValue() == expectedSources) {
				return entry.getKey();
			}
		}
		return null;
	}

	private @Nullable BlockNode findCommonNormalCaseOut(
			@Nullable BlockNode currentOut,
			Map<BlockNode, List<Object>> blocksMap,
			RegionStack stack) {
		if (currentOut != null
				&& currentOut != mth.getExitBlock()
				&& !hasOnlyTerminalPaths(currentOut)) {
			return null;
		}
		Set<BlockNode> enclosingExits = new HashSet<>();
		for (BlockNode caseBlock : blocksMap.keySet()) {
			LoopInfo loop = getInnermostLoop(caseBlock);
			while (loop != null) {
				enclosingExits.add(loop.getStart());
				enclosingExits.add(loop.getEnd());
				loop.getExitEdges().forEach(edge -> enclosingExits.add(edge.getTarget()));
				loop = loop.getParentLoop();
			}
		}
		enclosingExits.remove(mth.getExitBlock());
		if (enclosingExits.isEmpty()) {
			return null;
		}

		List<BlockNode> caseEntries = new ArrayList<>(blocksMap.size());
		for (BlockNode caseBlock : blocksMap.keySet()) {
			if (!hasPathToAny(caseBlock, enclosingExits) && !isTerminalCasePath(caseBlock, enclosingExits)) {
				return null;
			}
			caseEntries.add(caseBlock);
		}
		if (caseEntries.size() < 2) {
			return null;
		}
		return findCommonNormalJoin(caseEntries, enclosingExits);
	}

	private @Nullable BlockNode findCommonNormalJoin(
			List<BlockNode> caseEntries, Set<BlockNode> enclosingExits) {
		Set<ExceptionHandler> caseHandlers = collectCaseHandlers(caseEntries, enclosingExits);
		BlockSet normalReach = collectReachable(caseEntries, enclosingExits);
		BlockNode best = null;
		int bestJoinedCases = 1;
		long bestDistance = Long.MAX_VALUE;
		boolean ambiguous = false;
		for (BlockNode candidate : mth.getBasicBlocks()) {
			/*
			 * A subset of cases can share a normal continuation while the remaining cases leave an
			 * enclosing loop. Search only before those enclosing exits: unrestricted reachability can
			 * cross a loop latch and make a continue-only case appear to reach the join next iteration.
			 */
			if (candidate == mth.getExitBlock()
					|| enclosingExits.contains(candidate)
					|| candidate.getPredecessors().size() < 2
					|| !hasPathToAny(candidate, enclosingExits)) {
				continue;
			}
			int joinedCases = 0;
			long distance = 0;
			boolean invalid = false;
			for (BlockNode caseBlock : caseEntries) {
				int caseDistance = shortestPathDistance(caseBlock, candidate, enclosingExits);
				if (caseDistance >= 0) {
					joinedCases++;
					distance += caseDistance;
				} else if (!hasPathToAny(caseBlock, enclosingExits)
						&& !isTerminalCasePath(caseBlock, enclosingExits)) {
					invalid = true;
					break;
				}
			}
			if (invalid || joinedCases < 2) {
				continue;
			}
			if (!preservesHandlerJoin(candidate, caseHandlers, normalReach, enclosingExits)) {
				continue;
			}
			if (joinedCases > bestJoinedCases || joinedCases == bestJoinedCases && distance < bestDistance) {
				best = candidate;
				bestJoinedCases = joinedCases;
				bestDistance = distance;
				ambiguous = false;
			} else if (joinedCases == bestJoinedCases && distance == bestDistance && candidate != best) {
				ambiguous = true;
			}
		}
		return ambiguous ? null : best;
	}

	private Set<ExceptionHandler> collectCaseHandlers(List<BlockNode> caseEntries, Set<BlockNode> exits) {
		Set<ExceptionHandler> handlers = new HashSet<>();
		BlockSet visited = BlockSet.empty(mth);
		ArrayDeque<BlockNode> queue = new ArrayDeque<>(caseEntries);
		while (!queue.isEmpty()) {
			BlockNode block = queue.removeFirst();
			if (exits.contains(block) || visited.addChecked(block)) {
				continue;
			}
			addHandlers(handlers, block.get(AType.EXC_CATCH));
			for (InsnNode insn : block.getInstructions()) {
				addHandlers(handlers, insn.get(AType.EXC_CATCH));
			}
			queue.addAll(block.getCleanSuccessors());
		}
		return handlers;
	}

	private static void addHandlers(Set<ExceptionHandler> handlers, @Nullable CatchAttr catchAttr) {
		if (catchAttr != null) {
			handlers.addAll(catchAttr.getHandlers());
		}
	}

	private BlockSet collectReachable(List<BlockNode> starts, Set<BlockNode> exits) {
		BlockSet visited = BlockSet.empty(mth);
		ArrayDeque<BlockNode> queue = new ArrayDeque<>(starts);
		while (!queue.isEmpty()) {
			BlockNode block = queue.removeFirst();
			if (exits.contains(block) || visited.addChecked(block)) {
				continue;
			}
			queue.addAll(block.getCleanSuccessors());
		}
		return visited;
	}

	private boolean preservesHandlerJoin(BlockNode candidate, Set<ExceptionHandler> handlers,
			BlockSet normalReach, Set<BlockNode> exits) {
		for (ExceptionHandler handler : handlers) {
			BlockNode handlerBlock = handler.getHandlerBlock();
			if (handlerBlock == null
					|| shortestPathDistance(handlerBlock, candidate, exits) >= 0
					|| !handlerMergesWithNormalFlow(handlerBlock, normalReach, exits)) {
				continue;
			}
			return false;
		}
		return true;
	}

	private boolean handlerMergesWithNormalFlow(BlockNode start, BlockSet normalReach, Set<BlockNode> exits) {
		BlockSet visited = BlockSet.empty(mth);
		ArrayDeque<BlockNode> queue = new ArrayDeque<>();
		visited.add(start);
		queue.addAll(start.getCleanSuccessors());
		while (!queue.isEmpty()) {
			BlockNode block = queue.removeFirst();
			if (exits.contains(block) || visited.addChecked(block)) {
				continue;
			}
			if (normalReach.contains(block)) {
				return true;
			}
			queue.addAll(block.getCleanSuccessors());
		}
		return false;
	}

	private boolean hasPathToAny(BlockNode start, Set<BlockNode> ends) {
		if (ends.contains(start)) {
			return true;
		}
		BlockSet visited = BlockSet.empty(mth);
		ArrayDeque<BlockNode> queue = new ArrayDeque<>();
		visited.add(start);
		queue.add(start);
		while (!queue.isEmpty()) {
			BlockNode current = queue.removeFirst();
			for (BlockNode successor : current.getCleanSuccessors()) {
				if (ends.contains(successor)) {
					return true;
				}
				if (!visited.addChecked(successor)) {
					queue.addLast(successor);
				}
			}
		}
		return false;
	}

	private @Nullable LoopInfo getInnermostLoop(BlockNode block) {
		LoopInfo innermost = null;
		for (LoopInfo loop : mth.getAllLoopsForBlock(block)) {
			if (innermost == null || loop.getLoopBlocks().size() < innermost.getLoopBlocks().size()) {
				innermost = loop;
			}
		}
		return innermost;
	}

	private boolean hasCasePathAvoidingOut(
			Map<BlockNode, List<Object>> blocksMap, BlockNode out, LoopInfo loop) {
		Set<BlockNode> loopBoundaries = new HashSet<>();
		loopBoundaries.add(loop.getStart());
		loopBoundaries.add(loop.getEnd());
		loop.getExitEdges().forEach(edge -> loopBoundaries.add(edge.getTarget()));
		for (BlockNode caseBlock : blocksMap.keySet()) {
			if (hasPathToAnyAvoiding(caseBlock, loopBoundaries, out)) {
				return true;
			}
		}
		return false;
	}

	private boolean hasPathToAnyAvoiding(BlockNode start, Set<BlockNode> ends, BlockNode avoid) {
		if (start == avoid) {
			return false;
		}
		BlockSet visited = BlockSet.empty(mth);
		ArrayDeque<BlockNode> queue = new ArrayDeque<>();
		visited.add(start);
		queue.add(start);
		while (!queue.isEmpty()) {
			BlockNode current = queue.removeFirst();
			for (BlockNode successor : current.getCleanSuccessors()) {
				if (successor == avoid || visited.addChecked(successor)) {
					continue;
				}
				if (ends.contains(successor)) {
					return true;
				}
				queue.addLast(successor);
			}
		}
		return false;
	}

	private int shortestPathDistance(BlockNode start, BlockNode end, Set<BlockNode> exits) {
		if (start == end) {
			return 0;
		}
		BlockSet visited = BlockSet.empty(mth);
		ArrayDeque<BlockNode> queue = new ArrayDeque<>();
		ArrayDeque<Integer> distances = new ArrayDeque<>();
		visited.add(start);
		queue.add(start);
		distances.add(0);
		while (!queue.isEmpty()) {
			BlockNode current = queue.removeFirst();
			int distance = distances.removeFirst() + 1;
			for (BlockNode successor : current.getCleanSuccessors()) {
				if (successor == end) {
					return distance;
				}
				if (exits.contains(successor)) {
					continue;
				}
				if (!visited.addChecked(successor)) {
					queue.addLast(successor);
					distances.addLast(distance);
				}
			}
		}
		return -1;
	}

	private boolean isTerminalCasePath(BlockNode start, Set<BlockNode> enclosingExits) {
		boolean foundTerminal = false;
		BlockSet visited = BlockSet.empty(mth);
		ArrayDeque<BlockNode> queue = new ArrayDeque<>();
		visited.add(start);
		queue.add(start);
		while (!queue.isEmpty()) {
			BlockNode current = queue.removeFirst();
			if (current.contains(AFlag.LOOP_START) || current.contains(AFlag.LOOP_END)) {
				return false;
			}
			for (BlockNode successor : current.getCleanSuccessors()) {
				if (successor == mth.getExitBlock()) {
					InsnNode lastInsn = BlockUtils.getLastInsn(current);
					if (lastInsn == null
							|| lastInsn.getType() != InsnType.RETURN
									&& lastInsn.getType() != InsnType.THROW) {
						return false;
					}
					foundTerminal = true;
				} else if (enclosingExits.contains(successor)) {
					return false;
				} else if (!visited.addChecked(successor)) {
					queue.addLast(successor);
				}
			}
		}
		return foundTerminal;
	}

	private boolean hasOnlyTerminalPaths(BlockNode start) {
		boolean foundTerminal = false;
		BlockSet visited = BlockSet.empty(mth);
		ArrayDeque<BlockNode> queue = new ArrayDeque<>();
		visited.add(start);
		queue.add(start);
		while (!queue.isEmpty()) {
			BlockNode current = queue.removeFirst();
			if (current.contains(AFlag.LOOP_START) || current.contains(AFlag.LOOP_END)) {
				return false;
			}
			List<BlockNode> successors = current.getCleanSuccessors();
			if (successors.isEmpty()) {
				return false;
			}
			for (BlockNode successor : successors) {
				if (successor == mth.getExitBlock()) {
					InsnNode lastInsn = BlockUtils.getLastInsn(current);
					if (lastInsn == null
							|| lastInsn.getType() != InsnType.RETURN
									&& lastInsn.getType() != InsnType.THROW) {
						return false;
					}
					foundTerminal = true;
				} else if (!visited.addChecked(successor)) {
					queue.addLast(successor);
				}
			}
		}
		return foundTerminal;
	}

	private boolean shouldUseSharedCaseOut(@Nullable BlockNode currentOut, BlockNode sharedCaseOut) {
		if (currentOut == null || currentOut == mth.getExitBlock() || currentOut == sharedCaseOut) {
			return true;
		}
		/*
		 * In compiler-generated string switches, failed equality checks converge on the default
		 * case and then immediately join successful cases at the calculated post-dominator.
		 * Selecting that default case as the switch exit makes every successful case consume the
		 * common continuation independently. Keep the later calculated join in this shape.
		 */
		return !BlockUtils.isPathExists(sharedCaseOut, currentOut);
	}

	/**
	 * The direct Ktor CIO reader uses an eight-state coroutine switch. Normal and resumed states
	 * join at a timeout PHI immediately before the channel-closed decision, but the enclosing
	 * state-dispatch loop can make its latch look like the switch exit. If the latch is used, each
	 * case consumes the close/finish/socket-shutdown suffix independently.
	 *
	 * Select only the unique PHI reached by the five active states whose sole successor tests
	 * {@code ByteChannel.isClosedForWrite}. The other four valid states suspend-return before this
	 * join. A nearby incoming timeout-stop call and reachability to the original structural exit
	 * tie the candidate to the real direct-reader shape. The exception cleanup path does not pass
	 * this join and therefore remains separate.
	 */
	private @Nullable BlockNode findKtorCioDirectReadTail(
			@Nullable BlockNode currentOut, Map<BlockNode, List<Object>> blocksMap) {
		boolean targetMethod = KtorCioRecovery.isDirectReadStateMachine(mth);
		if (!targetMethod || currentOut == null || blocksMap.size() < 9) {
			return null;
		}
		BlockNode match = null;
		for (BlockNode candidate : mth.getBasicBlocks()) {
			PhiListAttr phiList = candidate.get(AType.PHI_LIST);
			if (phiList == null
					|| phiList.getList().isEmpty()
					|| candidate.getPredecessors().size() < 2
					|| candidate.getPredecessors().size() > 5
					|| candidate.getCleanSuccessors().size() != 1
					|| BlockUtils.isExceptionHandlerPath(candidate)
					|| !hasTimeoutStopNearPredecessor(candidate)
					|| !hasByteChannelClosedDecision(candidate)
					|| (!BlockUtils.isPathExists(candidate, currentOut)
							&& !BlockUtils.isPathExists(currentOut, candidate))
					|| countNonDefaultCasesReaching(blocksMap, candidate) < 5) {
				continue;
			}
			if (match != null) {
				return null;
			}
			match = candidate;
		}
		return match;
	}

	private static int countNonDefaultCasesReaching(
			Map<BlockNode, List<Object>> blocksMap, BlockNode candidate) {
		int count = 0;
		for (Map.Entry<BlockNode, List<Object>> entry : blocksMap.entrySet()) {
			if (entry.getValue().contains(SwitchRegion.DEFAULT_CASE_KEY)) {
				continue;
			}
			if (BlockUtils.isPathExists(entry.getKey(), candidate)) {
				count++;
			}
		}
		return count;
	}

	private static boolean hasByteChannelClosedDecision(BlockNode block) {
		return containsByteChannelClosedForWrite(block)
				|| containsByteChannelClosedForWrite(block.getCleanSuccessors().get(0));
	}

	private static boolean hasTimeoutStopNearPredecessor(BlockNode block) {
		for (BlockNode predecessor : block.getPredecessors()) {
			if (containsTimeoutStop(predecessor)) {
				return true;
			}
			for (BlockNode predecessorOfPredecessor : predecessor.getPredecessors()) {
				if (containsTimeoutStop(predecessorOfPredecessor)) {
					return true;
				}
			}
		}
		return false;
	}

	private static boolean containsTimeoutStop(BlockNode block) {
		for (InsnNode insn : block.getInstructions()) {
			Boolean found = insn.visitInsns(inner -> inner instanceof InvokeNode
					&& KtorCioRecovery.isTimeoutStopInvoke((InvokeNode) inner) ? Boolean.TRUE : null);
			if (found != null) {
				return true;
			}
		}
		return false;
	}

	private static boolean containsByteChannelClosedForWrite(BlockNode block) {
		for (InsnNode insn : block.getInstructions()) {
			Boolean found = insn.visitInsns(inner -> {
				if (!(inner instanceof InvokeNode)) {
					return null;
				}
				InvokeNode invoke = (InvokeNode) inner;
				return KtorCioRecovery.isByteChannelClosedForWriteInvoke(invoke) ? Boolean.TRUE : null;
			});
			if (found != null) {
				return true;
			}
		}
		return false;
	}

	@Nullable
	private BlockNode getOneIntersectionBlock(BlockNode out, BitSet caseBlocks, BitSet fallThroughSet) {
		BitSet caseExits = BlockUtils.copyBlocksBitSet(mth, fallThroughSet);
		caseExits.clear(out.getPos());
		caseExits.and(caseBlocks);
		return BlockUtils.bitSetToOneBlock(mth, caseExits);
	}

	private @Nullable BlockNode calcSwitchOut(BlockNode block, SwitchInsn insn, RegionStack stack) {
		// union of case blocks dominance frontier
		// works if no fallthrough cases and no returns inside switch
		BitSet outs = BlockUtils.newBlocksBitSet(mth);
		for (BlockNode s : block.getCleanSuccessors()) {
			if (s.contains(AFlag.LOOP_END)) {
				// loop end dom frontier is loop start, ignore it
				continue;
			}
			outs.or(s.getDomFrontier());
		}
		outs.clear(block.getId());
		outs.clear(mth.getExitBlock().getId());

		BlockNode out = null;
		if (outs.cardinality() == 1) {
			// single exit
			out = BlockUtils.bitSetToOneBlock(mth, outs);
		} else {
			// several switch exits
			// possible 'return', 'continue' or fallthrough in one of the cases
			LoopInfo loop = mth.getLoopForBlock(block);
			if (loop != null) {
				outs.andNot(loop.getStart().getPostDoms());
				outs.andNot(loop.getEnd().getPostDoms());
				BlockNode loopEnd = loop.getEnd();
				if (outs.cardinality() == 2 && outs.get(loopEnd.getId())) {
					// insert 'continue' for cases lead to loop end
					// expect only 2 exits: loop end and switch out
					List<BlockNode> outList = BlockUtils.bitSetToBlocks(mth, outs);
					outList.remove(loopEnd);
					BlockNode possibleOut = Utils.getOne(outList);
					if (possibleOut != null && insertContinueInSwitch(block, possibleOut, loopEnd)) {
						outs.clear(loopEnd.getId());
						out = possibleOut;
					} else if (possibleOut != null && hasExecutableLoopTail(loopEnd)) {
						/*
						 * A synthetic continue would jump directly to the loop update and skip
						 * executable instructions shared by the case paths (for example an
						 * expanded finally assignment). Keep the loop tail outside the switch so
						 * every normal and caught-exception path executes it exactly once.
						 */
						out = loopEnd;
					}
				}
				if (outs.isEmpty()) {
					// all exits inside switch, keep inside to exit from loop
					return mth.getExitBlock();
				}
			}
			if (out == null) {
				BlockNode imPostDom = block.getIPostDom();
				if (outs.get(imPostDom.getId())) {
					out = imPostDom;
				} else {
					outs.andNot(block.getPostDoms());
					out = BlockUtils.bitSetToOneBlock(mth, outs);
				}
			}
		}
		if (out != null && mth.isPreExitBlock(out)) {
			// include 'return' or 'throw' in case blocks
			out = mth.getExitBlock();
		}
		BlockNode imPostDom = block.getIPostDom();
		if (out == null && imPostDom == mth.getExitBlock()) {
			// all exits inside switch
			// check if all returns are equals and should be treated as single out block
			return allSameReturns(stack);
		}
		if (imPostDom == insn.getDefTargetBlock()
				&& block.getCleanSuccessors().contains(imPostDom)
				&& block.getDomFrontier().get(imPostDom.getId())) {
			// add exit to stop on empty 'default' block
			stack.addExit(imPostDom);
		}
		if (out == null) {
			// No unambiguous dominance-frontier candidate. The immediate post-dominator is
			// the existing fallback and is a valid structural switch exit.
			out = block.getIPostDom();
			if (out == null) {
				mth.addWarnComment("Failed to find 'out' block for switch in " + block + ". Please report as an issue.");
			}
		}
		if (out != null && regionMaker.isProcessed(out)) {
			if (stack.containsExit(out)) {
				// An enclosing region already owns this boundary. Reusing it as the nested
				// switch exit is expected and must not be treated as a traversal failure.
				return out;
			}
			// 'out' block already processed, prevent endless loop
			// in this case it might be that 'out' is the LOOP_START of a loop and occurs before 'block'
			// use the immediate post dominator if it has not been processed yet
			BlockNode fallback = block.getIPostDom();
			if (fallback != null && !regionMaker.isProcessed(fallback)) {
				out = fallback;
			} else {
				mth.addWarnComment("Switch 'out' block " + out + " for " + block
						+ " already processed. Failed to find an unprocessed fallback.");
				out = fallback;
			}
		}
		return out;
	}

	private BlockNode allSameReturns(RegionStack stack) {
		BlockNode exitBlock = mth.getExitBlock();
		List<BlockNode> preds = exitBlock.getPredecessors();
		int count = preds.size();
		if (count == 1) {
			return preds.get(0);
		}
		if (mth.getReturnType() == ArgType.VOID) {
			for (BlockNode pred : preds) {
				InsnNode insn = BlockUtils.getLastInsn(pred);
				if (insn == null || insn.getType() != InsnType.RETURN) {
					return exitBlock;
				}
			}
		} else {
			List<InsnArg> returnArgs = new ArrayList<>();
			for (BlockNode pred : preds) {
				InsnNode insn = BlockUtils.getLastInsn(pred);
				if (insn == null || insn.getType() != InsnType.RETURN) {
					return exitBlock;
				}
				returnArgs.add(insn.getArg(0));
			}
			InsnArg firstArg = returnArgs.get(0);
			if (firstArg.isRegister()) {
				RegisterArg reg = (RegisterArg) firstArg;
				for (int i = 1; i < count; i++) {
					InsnArg arg = returnArgs.get(i);
					if (!arg.isRegister() || !((RegisterArg) arg).sameCodeVar(reg)) {
						return exitBlock;
					}
				}
			} else {
				for (int i = 1; i < count; i++) {
					InsnArg arg = returnArgs.get(i);
					if (!arg.equals(firstArg)) {
						return exitBlock;
					}
				}
			}
		}
		// confirmed
		stack.addExits(preds);
		// ignore other returns
		for (int i = 1; i < count; i++) {
			BlockNode block = preds.get(i);
			block.add(AFlag.REMOVE);
			BlockUtils.invalidatePathCache();
			block.add(AFlag.ADDED_TO_REGION);
		}
		return preds.get(0);
	}

	/**
	 * Remove empty case blocks:
	 * 1. single 'default' case
	 * 2. filler cases if switch is 'packed' and 'default' case is empty
	 */
	private void removeEmptyCases(SwitchInsn insn, SwitchRegion sw, BlockNode defCase, BlockNode outBlock) {
		boolean defaultCaseIsEmpty;
		if (defCase == null) {
			defaultCaseIsEmpty = true;
		} else {
			defaultCaseIsEmpty = sw.getCases().stream()
					.anyMatch(c -> c.getKeys().contains(SwitchRegion.DEFAULT_CASE_KEY)
							&& canRemove(c.getContainer(), outBlock));
		}
		if (defaultCaseIsEmpty) {
			List<CaseInfo> cases = new ArrayList<>(sw.getCases());
			for (CaseInfo caseInfo : cases) {
				if (canRemove(caseInfo.getContainer(), outBlock)) {
					List<Object> keys = caseInfo.getKeys();
					if (keys.contains(SwitchRegion.DEFAULT_CASE_KEY) || insn.isPacked()) {
						// Remove case and mark all blocks as don't generate
						RegionUtils.addToAll(mth, caseInfo.getContainer(), AFlag.DONT_GENERATE);
						sw.getCases().remove(caseInfo);
					}
				}
			}
		}
	}

	/*
	 * Check container is empty and all paths through container are empty up until outBlock
	 */
	private boolean canRemove(IContainer container, BlockNode outBlock) {
		if (RegionUtils.isEmpty(container)) {
			if (container instanceof BlockNode) {
				// Base case - empty path from block node to outBlock
				return BlockUtils.followEmptyPath((BlockNode) container) == outBlock;
			} else if (container instanceof IRegion) {
				// Recursive case - every subBlock can be removed
				List<IContainer> subBlocks = ((IRegion) container).getSubBlocks();
				for (IContainer subBlock : subBlocks) {
					if (!canRemove(subBlock, outBlock)) {
						return false;
					}
				}
				return true;
			}
			LOG.debug("Unexpected container type in switch");
		}
		return false;
	}

	private boolean isBadCasesOrder(Map<BlockNode, List<Object>> blocksMap, Map<BlockNode, BlockNode> fallThroughCases) {
		BlockNode nextCaseBlock = null;
		for (BlockNode caseBlock : blocksMap.keySet()) {
			if (nextCaseBlock != null && !caseBlock.equals(nextCaseBlock)) {
				return true;
			}
			nextCaseBlock = fallThroughCases.get(caseBlock);
		}
		return nextCaseBlock != null;
	}

	private Map<BlockNode, List<Object>> reOrderSwitchCases(Map<BlockNode, List<Object>> blocksMap,
			Map<BlockNode, BlockNode> fallThroughCases) {
		List<BlockNode> list = new ArrayList<>(blocksMap.size());
		Set<BlockNode> targets = new HashSet<>(fallThroughCases.values());
		Set<BlockNode> added = new HashSet<>(blocksMap.size());
		// Start with chain heads and preserve the original order between independent chains.
		for (BlockNode caseBlock : blocksMap.keySet()) {
			if (!targets.contains(caseBlock)) {
				addFallThroughChain(list, added, caseBlock, fallThroughCases);
			}
		}
		// Cycles and converging chains have no complete linear ordering. Keep their remaining
		// nodes deterministic; the caller will reject the result if adjacency is still invalid.
		for (BlockNode caseBlock : blocksMap.keySet()) {
			addFallThroughChain(list, added, caseBlock, fallThroughCases);
		}

		Map<BlockNode, List<Object>> newBlocksMap = new LinkedHashMap<>(blocksMap.size());
		for (BlockNode key : list) {
			newBlocksMap.put(key, blocksMap.get(key));
		}
		return newBlocksMap;
	}

	private static void addFallThroughChain(List<BlockNode> result, Set<BlockNode> added,
			BlockNode start, Map<BlockNode, BlockNode> fallThroughCases) {
		BlockNode current = start;
		while (current != null && added.add(current)) {
			result.add(current);
			current = fallThroughCases.get(current);
		}
	}

	private boolean insertContinueInSwitch(BlockNode switchBlock, BlockNode switchOut, BlockNode loopEnd) {
		if (hasExecutableLoopTail(loopEnd)) {
			return false;
		}
		boolean inserted = false;
		for (BlockNode caseBlock : switchBlock.getCleanSuccessors()) {
			if (caseBlock.getDomFrontier().get(loopEnd.getId()) && caseBlock != switchOut) {
				// search predecessor of loop end on path from this successor
				Set<BlockNode> list = new HashSet<>(BlockUtils.collectBlocksDominatedBy(mth, caseBlock, caseBlock));
				if (list.contains(switchOut) || switchOut.getPredecessors().stream().anyMatch(list::contains)) {
					// 'continue' not needed
				} else {
					for (BlockNode p : loopEnd.getPredecessors()) {
						if (list.contains(p) || p == caseBlock) {
							if (p.isSynthetic()) {
								p.getInstructions().add(new InsnNode(InsnType.CONTINUE, 0));
								inserted = true;
							}
							break;
						}
					}
				}
			}
		}
		return inserted;
	}

	private static boolean hasExecutableLoopTail(BlockNode loopEnd) {
		return loopEnd.getInstructions().size() > 1;
	}

	/**
	 * Add break to every exit edge from 'case' region.
	 * 'Break' optimizations (code duplication, unreachable, etc.) will be done at
	 * {@link SwitchBreakVisitor}
	 */
	private static void insertBreaksForCase(MethodNode mth, SwitchRegion switchRegion, IContainer caseContainer) {
		labelLoopBreaksInsideSwitch(mth, caseContainer);
		BlockSet caseBlocks = new BlockSet(mth);
		RegionUtils.visitBlockNodes(mth, caseContainer, caseBlocks::add);
		DepthRegionTraversal.traverse(mth, caseContainer, new AbstractRegionVisitor() {
			@Override
			public void leaveRegion(MethodNode mth, IRegion region) {
				boolean insertBreak = false;
				if (region == caseContainer) {
					// top region
					insertBreak = true;
				} else {
					IContainer lastContainer = ListUtils.last(region.getSubBlocks());
					if (lastContainer instanceof BlockNode) {
						BlockNode lastBlock = (BlockNode) lastContainer;
						for (BlockNode successor : lastBlock.getSuccessors()) {
							if (!caseBlocks.contains(successor)) {
								insertBreak = true;
								break;
							}
						}
					} else if (lastContainer instanceof TryCatchRegion
							&& hasImplicitCatchContinuationOutsideCase((TryCatchRegion) lastContainer, caseBlocks)) {
						insertBreak = true;
					}
				}
				if (insertBreak
						&& region instanceof Region
						&& canAppendBreak(region)
						&& !endsWithLoopContinue(region, caseBlocks)) {
					region.getSubBlocks().add(buildBreakContainer(switchRegion));
				}
			}
		});
	}

	private static boolean hasImplicitCatchContinuationOutsideCase(
			TryCatchRegion tryCatchRegion, BlockSet caseBlocks) {
		if (!RegionUtils.hasExitBlock(tryCatchRegion.getTryRegion())) {
			return false;
		}
		for (IContainer catchRegion : tryCatchRegion.getCatchRegions().values()) {
			if (RegionUtils.hasExitBlock(catchRegion)) {
				continue;
			}
			Set<IBlock> catchBlocks = new HashSet<>();
			RegionUtils.getAllRegionBlocks(catchRegion, catchBlocks);
			for (IBlock catchBlock : catchBlocks) {
				if (!(catchBlock instanceof BlockNode)) {
					continue;
				}
				BlockNode blockNode = (BlockNode) catchBlock;
				if (blockNode.getCleanSuccessors().size() < 2) {
					continue;
				}
				for (BlockNode successor : blockNode.getCleanSuccessors()) {
					if (!catchBlocks.contains(successor) && !caseBlocks.contains(successor)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private static void labelLoopBreaksInsideSwitch(MethodNode mth, IContainer caseContainer) {
		RegionUtils.visitBlocks(mth, caseContainer, block -> {
			for (InsnNode insn : block.getInstructions()) {
				if (insn.getType() != InsnType.BREAK || insn.contains(AType.LOOP_LABEL)) {
					continue;
				}
				List<LoopInfo> loops = insn.getAll(AType.LOOP);
				if (loops.size() == 1) {
					LoopInfo loop = loops.get(0);
					if (isTargetLoopEnclosedByCase(caseContainer, block, loop)) {
						continue;
					}
					LoopLabelAttr label = new LoopLabelAttr(loop);
					insn.addAttr(label);
					loop.getStart().addAttr(label);
				}
			}
		});
	}

	private static boolean isTargetLoopEnclosedByCase(
			IContainer caseContainer, IBlock breakBlock, LoopInfo targetLoop) {
		IContainer current = RegionUtils.getBlockContainer(caseContainer, breakBlock);
		while (current instanceof IRegion) {
			if (current instanceof LoopRegion
					&& ((LoopRegion) current).getInfo() == targetLoop) {
				return true;
			}
			if (current == caseContainer) {
				break;
			}
			current = ((IRegion) current).getParent();
		}
		return false;
	}

	private static boolean endsWithLoopContinue(IRegion region, BlockSet caseBlocks) {
		IBlock last = RegionUtils.getLastBlock(region);
		if (!(last instanceof BlockNode)) {
			return false;
		}
		BlockNode loopEnd = (BlockNode) last;
		if (!loopEnd.contains(AFlag.LOOP_END)) {
			return false;
		}
		for (BlockNode predecessor : loopEnd.getPredecessors()) {
			if (caseBlocks.contains(predecessor)) {
				InsnNode lastInsn = BlockUtils.getLastInsn(predecessor);
				if (lastInsn != null && lastInsn.getType() == InsnType.CONTINUE) {
					return true;
				}
			}
		}
		return false;
	}

	public static boolean canAppendBreak(IRegion region) {
		return !region.contains(AFlag.FALL_THROUGH)
				&& !RegionUtils.hasExitBlock(region)
				&& !RegionUtils.hasExitEdge(region);
	}

	public static InsnContainer buildBreakContainer(SwitchRegion switchRegion) {
		InsnNode breakInsn = new InsnNode(InsnType.BREAK, 0);
		breakInsn.add(AFlag.SYNTHETIC);
		breakInsn.addAttr(new RegionRefAttr(switchRegion));
		return new InsnContainer(breakInsn);
	}
}
