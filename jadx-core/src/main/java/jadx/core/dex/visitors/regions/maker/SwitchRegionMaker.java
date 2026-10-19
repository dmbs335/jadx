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
import jadx.core.dex.nodes.IBranchRegion;
import jadx.core.dex.nodes.IContainer;
import jadx.core.dex.nodes.IRegion;
import jadx.core.dex.nodes.InsnContainer;
import jadx.core.dex.nodes.InsnNode;
import jadx.core.dex.nodes.MethodNode;
import jadx.core.dex.regions.Region;
import jadx.core.dex.regions.SwitchRegion;
import jadx.core.dex.regions.SwitchRegion.CaseInfo;
import jadx.core.dex.regions.TryCatchRegion;
import jadx.core.dex.regions.conditions.IfRegion;
import jadx.core.dex.regions.loops.LoopRegion;
import jadx.core.dex.trycatch.CatchAttr;
import jadx.core.dex.trycatch.ExceptionHandler;
import jadx.core.dex.visitors.kotlin.KtorCioRecovery;
import jadx.core.dex.visitors.regions.AbstractRegionVisitor;
import jadx.core.dex.visitors.regions.DepthRegionTraversal;
import jadx.core.dex.visitors.regions.ProcessTryCatchRegions;
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
		out = adjustOutForProtectedDefault(out, defCase);
		BlockNode originalOut = out;
		BlockNode commonNormalCaseOut = findCommonNormalCaseOut(out, blocksMap, stack);
		LoopInfo switchLoop = getInnermostLoop(block);
		if (commonNormalCaseOut != null) {
			if (switchLoop != null && hasCasePathAvoidingOut(blocksMap, commonNormalCaseOut, switchLoop)) {
				if (!canInsertContinueInSwitch(block, switchLoop)) {
					commonNormalCaseOut = null;
				} else if (!insertContinueInSwitch(block, commonNormalCaseOut, switchLoop)) {
					sw.setCaseLoopBackRequired(true);
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
		if (switchLoop != null
				&& commonNormalCaseOut == null
				&& out == defCase
				&& hasCasePathToLoopBackAvoidingOut(blocksMap, out, switchLoop)) {
			sw.setCaseLoopBackRequired(true);
		}
		stack.addExit(out);
		markAcyclicSharedCasePaths(out, blocksMap.keySet());

		addCases(sw, out, stack, blocksMap);
		removeEmptyCases(insn, sw, defCase, out);

		stack.pop();
		return out;
	}

	/**
	 * Some compilers lower a default switch edge directly into a protected cleanup block. The other
	 * cases duplicate that cleanup before an abrupt exit, so their dominance frontiers meet only
	 * after the surrounding catch/finally region. Using that later join as the switch exit moves the
	 * loop tail ahead of cleanup. Keep the exception-scope transition outside the switch instead.
	 */
	private @Nullable BlockNode adjustOutForProtectedDefault(
			@Nullable BlockNode out, @Nullable BlockNode defaultCase) {
		if (out == null
				|| defaultCase == null
				|| defaultCase == out
				|| defaultCase.getInstructions().isEmpty()
				|| !BlockUtils.isPathExists(defaultCase, out)) {
			return out;
		}
		if (defaultCase.contains(AFlag.TRY_LEAVE)
				&& leavesExceptionScope(defaultCase, out)) {
			return defaultCase;
		}
		return defaultCase.contains(AFlag.TRY_ENTER)
				&& defaultCase.contains(AFlag.TRY_LEAVE)
				&& hasResourceSuppressionHandler(defaultCase)
						? defaultCase
						: out;
	}

	private static boolean leavesExceptionScope(BlockNode from, BlockNode to) {
		Set<ExceptionHandler> fromHandlers = collectBlockHandlers(from);
		if (fromHandlers.isEmpty()) {
			return false;
		}
		Set<ExceptionHandler> toHandlers = collectBlockHandlers(to);
		return !fromHandlers.equals(toHandlers);
	}

	private static Set<ExceptionHandler> collectBlockHandlers(BlockNode block) {
		Set<ExceptionHandler> handlers = new HashSet<>();
		addHandlers(handlers, block.get(AType.EXC_CATCH));
		for (InsnNode insn : block.getInstructions()) {
			addHandlers(handlers, insn.get(AType.EXC_CATCH));
		}
		return handlers;
	}

	private static boolean hasResourceSuppressionHandler(BlockNode block) {
		Set<ExceptionHandler> handlers = new HashSet<>();
		addHandlers(handlers, block.get(AType.EXC_CATCH));
		for (InsnNode insn : block.getInstructions()) {
			addHandlers(handlers, insn.get(AType.EXC_CATCH));
		}
		return handlers.stream()
				.anyMatch(handler -> ProcessTryCatchRegions.containsResourceSuppressionTry(handler.getTryBlock()));
	}

	private static boolean hasResourceSuppressionHandler(MethodNode mth, IContainer container) {
		boolean[] found = { false };
		RegionUtils.visitBlockNodes(mth, container, block -> {
			if (!found[0] && hasResourceSuppressionHandler(block)) {
				found[0] = true;
			}
		});
		return found[0];
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
		boolean resourceCleanupSwitch = hasResourceSuppressionHandler(mth, sw);
		for (SwitchRegion.CaseInfo caseInfo : sw.getCases()) {
			insertBreaksForCase(mth, sw, caseInfo.getContainer(),
					resourceCleanupSwitch, sw.isCaseLoopBackRequired());
			removeRedundantSyntheticContinues(mth, caseInfo.getContainer());
		}
	}

	private static void removeRedundantSyntheticContinues(MethodNode mth, IContainer caseContainer) {
		BlockSet caseBlocks = new BlockSet(mth);
		RegionUtils.visitBlockNodes(mth, caseContainer, caseBlocks::add);
		RegionUtils.visitBlockNodes(mth, caseContainer, block -> {
			InsnNode continueInsn = BlockUtils.getLastInsn(block);
			if (continueInsn == null
					|| continueInsn.getType() != InsnType.CONTINUE
					|| !continueInsn.contains(AFlag.SYNTHETIC)) {
				return;
			}
			List<LoopInfo> targetLoops = continueInsn.getAll(AType.LOOP);
			if (targetLoops.size() != 1) {
				return;
			}
			LoopInfo targetLoop = targetLoops.get(0);
			BlockNode current = block;
			Set<BlockNode> visited = new HashSet<>();
			while (visited.size() < 16 && visited.add(current)) {
				List<BlockNode> successors = current.getCleanSuccessors().stream()
						.filter(caseBlocks::contains)
						.toList();
				if (successors.size() != 1) {
					return;
				}
				current = successors.get(0);
				InsnNode nextContinue = BlockUtils.getLastInsn(current);
				if (nextContinue != null
						&& nextContinue.getType() == InsnType.CONTINUE
						&& nextContinue.contains(AFlag.SYNTHETIC)
						&& nextContinue.getAll(AType.LOOP).contains(targetLoop)) {
					block.getInstructions().remove(continueInsn);
					return;
				}
				if (current.getInstructions().stream().anyMatch(insn -> !insn.contains(AFlag.DONT_GENERATE))) {
					return;
				}
			}
		});
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
			BlockNode next = fallThroughCases.get(caseBlock);
			BlockNode regionStart = resolveTerminalCaseEntry(caseBlock, out, next);
			Region caseRegion;
			if (regionStart != caseBlock) {
				// The target is an exclusively owned terminal block. It can also be registered as
				// an exit of an enclosing loop, in which case makeRegion would deliberately omit it.
				caseRegion = new Region(stack.peekRegion());
				caseRegion.add(regionStart);
			} else if (stack.containsExit(caseBlock)) {
				caseRegion = new Region(stack.peekRegion());
			} else {
				stack.addExit(next);
				caseRegion = regionMaker.makeRegion(regionStart);
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
	 * DEX producers can place an empty synthetic bridge between a switch target and an exclusively
	 * owned return/throw block. Starting region construction at the bridge can stop immediately at
	 * the switch exit and later turn the case into {@code break}, leaving the terminal instruction
	 * outside every region. Skip only a bounded, linear and single-owner bridge chain; fall-through,
	 * loop and exception-boundary paths must retain their original entry.
	 */
	static BlockNode resolveTerminalCaseEntry(
			BlockNode caseBlock, @Nullable BlockNode switchOut, @Nullable BlockNode fallThrough) {
		if (fallThrough != null || caseBlock == switchOut) {
			return caseBlock;
		}
		BlockNode current = caseBlock;
		for (int depth = 0; depth < 8; depth++) {
			if (!current.contains(AFlag.SYNTHETIC)
					|| !current.getInstructions().isEmpty()
					|| current.contains(AFlag.LOOP_START)
					|| current.contains(AFlag.LOOP_END)
					|| current.contains(AFlag.EXC_TOP_SPLITTER)
					|| current.contains(AFlag.EXC_BOTTOM_SPLITTER)
					|| current.contains(AFlag.TRY_ENTER)
					|| current.contains(AFlag.TRY_LEAVE)) {
				return caseBlock;
			}
			List<BlockNode> successors = current.getCleanSuccessors();
			if (successors.size() != 1) {
				return caseBlock;
			}
			BlockNode successor = successors.get(0);
			if (successor == switchOut || successor.getPredecessors().size() != 1) {
				return caseBlock;
			}
			if (BlockUtils.containsExitInsn(successor)) {
				return successor;
			}
			current = successor;
		}
		return caseBlock;
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
		BlockNode handlerJoin = null;
		int bestHandlerCount = 0;
		long bestHandlerDistance = Long.MAX_VALUE;
		boolean handlerJoinAmbiguous = false;
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
			if (invalid || joinedCases == 0) {
				continue;
			}
			if (!preservesHandlerJoin(candidate, caseHandlers, normalReach, enclosingExits)) {
				continue;
			}
			if (joinedCases >= 2) {
				if (joinedCases > bestJoinedCases || joinedCases == bestJoinedCases && distance < bestDistance) {
					best = candidate;
					bestJoinedCases = joinedCases;
					bestDistance = distance;
					ambiguous = false;
				} else if (joinedCases == bestJoinedCases && distance == bestDistance && candidate != best) {
					ambiguous = true;
				}
			} else if (!isPureLoopControlTail(candidate, enclosingExits)) {
				int handlerJoins = countHandlersReaching(caseHandlers, candidate, enclosingExits);
				if (handlerJoins == 0) {
					continue;
				}
				if (handlerJoins > bestHandlerCount
						|| handlerJoins == bestHandlerCount && distance < bestHandlerDistance) {
					handlerJoin = candidate;
					bestHandlerCount = handlerJoins;
					bestHandlerDistance = distance;
					handlerJoinAmbiguous = false;
				} else if (handlerJoins == bestHandlerCount
						&& distance == bestHandlerDistance && candidate != handlerJoin) {
					handlerJoinAmbiguous = true;
				}
			}
		}
		if (ambiguous) {
			return null;
		}
		if (best == null || handlerJoin == null || !isPureLoopControlTail(best, enclosingExits)) {
			return best;
		}
		return handlerJoinAmbiguous ? null : handlerJoin;
	}

	private int countHandlersReaching(
			Set<ExceptionHandler> handlers, BlockNode candidate, Set<BlockNode> exits) {
		int count = 0;
		for (ExceptionHandler handler : handlers) {
			BlockNode handlerBlock = handler.getHandlerBlock();
			if (handlerBlock != null && shortestPathDistance(handlerBlock, candidate, exits) >= 0) {
				count++;
			}
		}
		return count;
	}

	private boolean isPureLoopControlTail(BlockNode candidate, Set<BlockNode> enclosingExits) {
		if (mth.getAllLoopsForBlock(candidate).isEmpty()) {
			return false;
		}
		for (InsnNode insn : candidate.getInstructions()) {
			if (!insn.contains(AFlag.DONT_GENERATE)
					&& insn.getType() != InsnType.ARITH
					&& insn.getType() != InsnType.MOVE) {
				return false;
			}
		}
		BlockSet visited = BlockSet.empty(mth);
		ArrayDeque<BlockNode> queue = new ArrayDeque<>(candidate.getCleanSuccessors());
		boolean reachedExit = false;
		while (!queue.isEmpty() && visited.size() < 16) {
			BlockNode block = queue.removeFirst();
			if (enclosingExits.contains(block)) {
				reachedExit = true;
				continue;
			}
			if (visited.addChecked(block)) {
				continue;
			}
			for (InsnNode insn : block.getInstructions()) {
				if (!insn.contains(AFlag.DONT_GENERATE) && insn.getType() != InsnType.IF) {
					return false;
				}
			}
			List<BlockNode> successors = block.getCleanSuccessors();
			if (successors.isEmpty()) {
				return false;
			}
			queue.addAll(successors);
		}
		return reachedExit && queue.isEmpty();
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

	private boolean hasCasePathToLoopBackAvoidingOut(
			Map<BlockNode, List<Object>> blocksMap, BlockNode out, LoopInfo loop) {
		Set<BlockNode> loopBack = Set.of(loop.getStart(), loop.getEnd());
		for (BlockNode caseBlock : blocksMap.keySet()) {
			if (hasPathToAnyAvoiding(caseBlock, loopBack, out)) {
				return true;
			}
		}
		return false;
	}

	private boolean allCasesFlowThroughLoopEnd(BlockNode switchBlock, BlockNode switchOut, BlockNode loopEnd) {
		for (BlockNode caseBlock : switchBlock.getCleanSuccessors()) {
			if (!hasPathToAnyAvoiding(caseBlock, Set.of(loopEnd), switchOut)
					|| hasPathToAnyAvoiding(caseBlock, Set.of(switchOut), loopEnd)) {
				return false;
			}
		}
		return true;
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
					// When every normal case reaches an executable loop update before the other exit,
					// a synthetic continue would bypass that update and leave unreachable code in the case.
					boolean canInsertContinue = canInsertContinueInSwitch(block, loop)
							&& (possibleOut == null
									|| !hasAnyExecutableLoopTail(loopEnd)
									|| !allCasesFlowThroughLoopEnd(block, possibleOut, loopEnd));
					if (possibleOut != null && canInsertContinue && insertContinueInSwitch(block, possibleOut, loop)) {
						outs.clear(loopEnd.getId());
						out = possibleOut;
					} else if (possibleOut != null && !canInsertContinue) {
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

	private boolean insertContinueInSwitch(BlockNode switchBlock, BlockNode switchOut, LoopInfo loop) {
		BlockNode loopEnd = loop.getEnd();
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
								InsnNode continueInsn = new InsnNode(InsnType.CONTINUE, 0);
								continueInsn.add(AFlag.SYNTHETIC);
								continueInsn.addAttr(AType.LOOP, loop);
								addContinueLabelIfNeeded(switchBlock, loop, continueInsn);
								p.getInstructions().add(continueInsn);
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

	private boolean canInsertContinueInSwitch(BlockNode switchBlock, LoopInfo targetLoop) {
		BlockNode loopEnd = targetLoop.getEnd();
		if (hasExecutableLoopTail(loopEnd)) {
			return false;
		}
		return !needsContinueLabel(switchBlock, targetLoop) || !hasAnyExecutableLoopTail(loopEnd);
	}

	private void addContinueLabelIfNeeded(BlockNode switchBlock, LoopInfo targetLoop, InsnNode continueInsn) {
		if (needsContinueLabel(switchBlock, targetLoop)) {
			LoopLabelAttr labelAttr = new LoopLabelAttr(targetLoop);
			continueInsn.addAttr(labelAttr);
			targetLoop.getStart().addAttr(labelAttr);
		}
	}

	private boolean needsContinueLabel(BlockNode switchBlock, LoopInfo targetLoop) {
		return mth.getAllLoopsForBlock(switchBlock).stream()
				.anyMatch(loop -> loop != targetLoop && loop.hasParent(targetLoop));
	}

	private static boolean hasExecutableLoopTail(BlockNode loopEnd) {
		return loopEnd.getInstructions().size() > 1;
	}

	private static boolean hasAnyExecutableLoopTail(BlockNode loopEnd) {
		return loopEnd.getInstructions().stream()
				.anyMatch(insn -> !insn.contains(AFlag.DONT_GENERATE));
	}

	/**
	 * Add break to every exit edge from 'case' region.
	 * 'Break' optimizations (code duplication, unreachable, etc.) will be done at
	 * {@link SwitchBreakVisitor}
	 */
	private static void insertBreaksForCase(
			MethodNode mth, SwitchRegion switchRegion, IContainer caseContainer,
			boolean resourceCleanupSwitch, boolean structuredLoopBack) {
		labelLoopBreaksInsideSwitch(mth, caseContainer);
		BlockSet caseBlocks = new BlockSet(mth);
		RegionUtils.visitBlockNodes(mth, caseContainer, caseBlocks::add);
		DepthRegionTraversal.traverse(mth, caseContainer, new AbstractRegionVisitor() {
			@Override
			public void leaveRegion(MethodNode mth, IRegion region) {
				if (region instanceof IfRegion && region.getParent() == caseContainer) {
					insertMissingGuardBreak((IfRegion) region, caseBlocks, switchRegion);
				}
				if (region instanceof TryCatchRegion) {
					for (IContainer catchRegion : ((TryCatchRegion) region).getCatchRegions().values()) {
						if (catchRegion instanceof Region
								&& !RegionUtils.hasExitBlock(catchRegion)
								&& ((ProcessTryCatchRegions.hasSharedCatchExit(caseContainer,
										((TryCatchRegion) region).getTryCatchBlock())
										|| ProcessTryCatchRegions.hasTerminalCatchJoin(caseContainer,
												((TryCatchRegion) region).getTryCatchBlock()))
										|| (mth.getLoopsCount() == 0
												&& hasCatchContinuationAtSwitchOut(catchRegion, switchRegion)))) {
							((Region) catchRegion).getSubBlocks().add(buildBreakContainer(switchRegion));
						} else if (catchRegion instanceof Region && mth.getLoopsCount() == 0) {
							appendCatchTerminalReturn((TryCatchRegion) region, (Region) catchRegion);
						}
					}
				}
			}
		});
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
						// A catch edge is not a switch-case fall-through. Its splitter can be
						// outside the case even when the normal successor stays in the case.
						for (BlockNode successor : lastBlock.getCleanSuccessors()) {
							if (!caseBlocks.contains(successor)
									&& !isCaseLoopBackEdge(region, caseContainer, successor)) {
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
						&& canAppendBreak(region, switchRegion)) {
					if (reachesSharedCatchRethrow(region, caseContainer)) {
						return;
					}
					LoopInfo loopBack = resourceCleanupSwitch || structuredLoopBack
							? findOwnedLoopBack(mth, region, structuredLoopBack)
							: null;
					if (loopBack == null) {
						if (!endsWithLoopContinue(region, caseBlocks)) {
							region.getSubBlocks().add(buildBreakContainer(switchRegion));
						}
					} else {
						removePrematureSyntheticContinue(mth, region, loopBack);
						region.getSubBlocks().add(buildContinueContainer(mth, switchRegion, loopBack));
					}
				}
			}
		});
	}

	/**
	 * A catch with a path to the shared post-switch continuation must not fall through
	 * into the protected case's normal-success continuation. Other catch exits are not
	 * switch breaks, so require the single CFG boundary to match the next emitted block.
	 */
	private static boolean hasCatchContinuationAtSwitchOut(
			IContainer catchRegion, SwitchRegion switchRegion) {
		if (catchRegion.contains(AFlag.FALL_THROUGH)
				|| hasExitEdgeLeavingSwitch(catchRegion, switchRegion)) {
			return false;
		}
		BlockNode switchOut = findNextBlockAfterRegion(switchRegion);
		if (switchOut == null) {
			return false;
		}
		Set<BlockNode> exits = ProcessTryCatchRegions.getCatchBoundaryTargets(catchRegion);
		return exits.size() == 1
				&& BlockUtils.followEmptyPath(exits.iterator().next()) == BlockUtils.followEmptyPath(switchOut);
	}

	private static void appendCatchTerminalReturn(TryCatchRegion tryCatchRegion, Region catchRegion) {
		if (RegionUtils.hasExitBlock(catchRegion)) {
			return;
		}
		Set<BlockNode> exits = ProcessTryCatchRegions.getCatchBoundaryTargets(catchRegion);
		if (exits.size() != 1) {
			return;
		}
		BlockNode terminalReturn = ExcHandlersRegionMaker.findTerminalReturnTail(exits.iterator().next());
		if (terminalReturn == null || terminalReturn.contains(AType.PHI_LIST)) {
			return;
		}
		BlockNode pathBlock = exits.iterator().next();
		while (pathBlock != terminalReturn) {
			if (pathBlock.contains(AType.PHI_LIST)) {
				// A return through a value-merge needs case-local SSA reconstruction.
				return;
			}
			pathBlock = pathBlock.getCleanSuccessors().get(0);
		}
		BlockNode normalContinuation = findNextBlockAfterRegion(tryCatchRegion);
		if (normalContinuation == null
				|| BlockUtils.followEmptyPath(normalContinuation) == terminalReturn) {
			return;
		}
		InsnNode returnInsn = BlockUtils.getLastInsn(terminalReturn);
		// The original return instruction keeps its SSA argument ownership. The catch
		// reaches it through empty blocks, so no intermediate side effect is skipped.
		catchRegion.getSubBlocks().add(new InsnContainer(returnInsn));
	}

	private static @Nullable BlockNode findNextBlockAfterRegion(IRegion region) {
		IRegion child = region;
		while (child.getParent() != null) {
			IRegion parent = child.getParent();
			List<IContainer> siblings = parent.getSubBlocks();
			for (int i = siblings.indexOf(child) + 1; i > 0 && i < siblings.size(); i++) {
				BlockNode first = RegionUtils.getFirstBlockNode(siblings.get(i));
				if (first != null) {
					return first;
				}
			}
			child = parent;
		}
		return null;
	}

	private static boolean reachesSharedCatchRethrow(IRegion region, IContainer caseContainer) {
		IBlock lastBlock = RegionUtils.getLastBlock(region);
		if (!(lastBlock instanceof BlockNode)) {
			return false;
		}
		BlockNode block = (BlockNode) lastBlock;
		if (block.getCleanSuccessors().size() != 1
				|| !isEmptyPathToThrow(block.getCleanSuccessors().get(0))) {
			return false;
		}
		for (IRegion current = region; current != null && current != caseContainer; current = current.getParent()) {
			IRegion parent = current.getParent();
			if ((parent == null && current.contains(AType.EXC_HANDLER))
					|| (parent instanceof TryCatchRegion
							&& ((TryCatchRegion) parent).getCatchRegions().containsValue(current))) {
				IContainer tail = ListUtils.last(current.getSubBlocks());
				if (tail instanceof IBlock) {
					InsnNode terminal = BlockUtils.getLastInsn((IBlock) tail);
					return terminal != null && terminal.getType() == InsnType.THROW
							&& tail.contains(AFlag.SYNTHETIC);
				}
				return false;
			}
		}
		return false;
	}

	private static boolean isEmptyPathToThrow(BlockNode start) {
		Set<BlockNode> visited = new HashSet<>();
		BlockNode block = start;
		while (visited.size() < 8 && visited.add(block)) {
			List<InsnNode> insns = block.getInstructions().stream()
					.filter(insn -> !insn.contains(AFlag.DONT_GENERATE)).toList();
			if (!insns.isEmpty()) {
				return insns.size() == 1 && insns.get(0).getType() == InsnType.THROW;
			}
			if (block.getCleanSuccessors().size() != 1) {
				return false;
			}
			block = block.getCleanSuccessors().get(0);
		}
		return false;
	}

	private static void insertMissingGuardBreak(IfRegion ifRegion, BlockSet caseBlocks, SwitchRegion switchRegion) {
		IContainer thenRegion = ifRegion.getThenRegion();
		if (!(thenRegion instanceof Region) || !((Region) thenRegion).getSubBlocks().isEmpty()
				|| ifRegion.getElseRegion() != null) {
			return;
		}
		BlockNode conditionBlock = ListUtils.last(ifRegion.getConditionBlocks());
		if (conditionBlock == null || conditionBlock.getSuccessors().size() != 2) {
			return;
		}
		long outsideEdges = conditionBlock.getSuccessors().stream().filter(successor -> !caseBlocks.contains(successor)).count();
		if (outsideEdges == 1) {
			((Region) thenRegion).add(buildBreakContainer(switchRegion));
		}
	}

	/**
	 * A loop header (or its empty synthetic bridge) can be absent from the structured case
	 * block set even though the back edge stays inside a loop owned by this case.
	 * Such an edge must not become a switch break.
	 */
	private static boolean isCaseLoopBackEdge(IRegion region, IContainer caseContainer, BlockNode successor) {
		IRegion current = region;
		while (current != null && current != caseContainer) {
			if (current instanceof LoopRegion
					&& reachesLoopStartThroughEmptyBlocks(successor, ((LoopRegion) current).getInfo().getStart())) {
				return true;
			}
			current = current.getParent();
		}
		return false;
	}

	private static boolean reachesLoopStartThroughEmptyBlocks(BlockNode block, BlockNode loopStart) {
		Set<BlockNode> visited = new HashSet<>();
		while (visited.size() < 8 && visited.add(block)) {
			if (block == loopStart) {
				return true;
			}
			if (block.getInstructions().stream().anyMatch(insn -> !insn.contains(AFlag.DONT_GENERATE))
					|| block.getSuccessors().size() != 1) {
				return false;
			}
			block = block.getSuccessors().get(0);
		}
		return false;
	}

	private static @Nullable LoopInfo findOwnedLoopBack(
			MethodNode mth, IRegion region, boolean allowStructuredFooter) {
		IBlock last = RegionUtils.getLastBlock(region);
		if (!(last instanceof BlockNode)) {
			return null;
		}
		BlockNode lastBlock = (BlockNode) last;
		for (LoopInfo loop : mth.getAllLoopsForBlock(lastBlock)) {
			if (loop.getEnd() == lastBlock && lastBlock.getSuccessors().contains(loop.getStart())) {
				return loop;
			}
			if (allowStructuredFooter && reachesStructuredLoopEnd(lastBlock, loop)) {
				return loop;
			}
		}
		return null;
	}

	private static boolean reachesStructuredLoopEnd(BlockNode start, LoopInfo loop) {
		Set<BlockNode> loopBlocks = loop.getLoopBlocks();
		Set<BlockNode> visited = new HashSet<>();
		ArrayDeque<BlockNode> queue = new ArrayDeque<>(start.getCleanSuccessors());
		boolean reachedEnd = false;
		while (!queue.isEmpty() && visited.size() < 16) {
			BlockNode block = queue.removeFirst();
			if (block == loop.getEnd()) {
				reachedEnd = true;
				continue;
			}
			if (!loopBlocks.contains(block) || !visited.add(block)) {
				continue;
			}
			if (!block.contains(AFlag.ADDED_TO_REGION)
					&& block.getInstructions().stream().anyMatch(insn -> !insn.contains(AFlag.DONT_GENERATE))) {
				return false;
			}
			queue.addAll(block.getCleanSuccessors());
		}
		return reachedEnd && queue.isEmpty();
	}

	private static void removePrematureSyntheticContinue(MethodNode mth, IRegion region, LoopInfo loop) {
		RegionUtils.visitBlockNodes(mth, region, block -> block.getInstructions().removeIf(insn -> insn.getType() == InsnType.CONTINUE
				&& insn.contains(AFlag.SYNTHETIC)
				&& insn.getAll(AType.LOOP).contains(loop)));
	}

	private static InsnContainer buildContinueContainer(MethodNode mth, SwitchRegion switchRegion, LoopInfo loop) {
		InsnNode continueInsn = new InsnNode(InsnType.CONTINUE, 0);
		continueInsn.add(AFlag.SYNTHETIC);
		continueInsn.addAttr(AType.LOOP, loop);
		boolean crossesNestedLoop = mth.getAllLoopsForBlock(switchRegion.getHeader()).stream()
				.anyMatch(sourceLoop -> sourceLoop != loop && sourceLoop.hasParent(loop));
		if (crossesNestedLoop) {
			LoopLabelAttr labelAttr = new LoopLabelAttr(loop);
			continueInsn.addAttr(labelAttr);
			loop.getStart().addAttr(labelAttr);
		}
		return new InsnContainer(continueInsn);
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
		BlockNode current = (BlockNode) last;
		if (!current.contains(AFlag.LOOP_END)) {
			return false;
		}
		Set<BlockNode> visited = new HashSet<>();
		while (visited.size() < 16 && visited.add(current)) {
			List<BlockNode> casePredecessors = current.getPredecessors().stream()
					.filter(caseBlocks::contains)
					.toList();
			if (casePredecessors.size() != 1) {
				return false;
			}
			current = casePredecessors.get(0);
			InsnNode lastInsn = BlockUtils.getLastInsn(current);
			if (lastInsn != null && lastInsn.getType() == InsnType.CONTINUE) {
				return true;
			}
			if (current.getInstructions().stream().anyMatch(insn -> !insn.contains(AFlag.DONT_GENERATE))) {
				return false;
			}
		}
		return false;
	}

	public static boolean canAppendBreak(IRegion region) {
		return !region.contains(AFlag.FALL_THROUGH)
				&& !RegionUtils.hasExitBlock(region)
				&& !RegionUtils.hasExitEdge(region);
	}

	private static boolean canAppendBreak(IRegion region, SwitchRegion targetSwitch) {
		return !region.contains(AFlag.FALL_THROUGH)
				&& !RegionUtils.hasExitBlock(region)
				&& !hasExitEdgeLeavingSwitch(region, targetSwitch);
	}

	/**
	 * A break in a nested switch exits only that switch. Counting it as an exit from the enclosing
	 * case suppresses the enclosing switch's break and silently introduces case fall-through.
	 */
	private static boolean hasExitEdgeLeavingSwitch(IContainer container, SwitchRegion targetSwitch) {
		if (container instanceof IBlock) {
			InsnNode lastInsn = BlockUtils.getLastInsn((IBlock) container);
			if (lastInsn != null && lastInsn.getType() == InsnType.BREAK) {
				RegionRefAttr ref = lastInsn.get(AType.REGION_REF);
				if (ref != null && isNestedSwitch(ref.getRegion(), targetSwitch)) {
					return false;
				}
			}
			return BlockUtils.containsExitInsn((IBlock) container);
		}
		if (container instanceof IBranchRegion) {
			for (IContainer branch : ((IBranchRegion) container).getBranches()) {
				if (branch == null || !hasExitEdgeLeavingSwitch(branch, targetSwitch)) {
					return false;
				}
			}
			return true;
		}
		if (container instanceof IRegion) {
			IContainer last = ListUtils.last(((IRegion) container).getSubBlocks());
			return last != null && hasExitEdgeLeavingSwitch(last, targetSwitch);
		}
		return false;
	}

	private static boolean isNestedSwitch(IRegion region, SwitchRegion targetSwitch) {
		if (!(region instanceof SwitchRegion)) {
			return false;
		}
		IRegion parent = region.getParent();
		while (parent != null) {
			if (parent == targetSwitch) {
				return true;
			}
			parent = parent.getParent();
		}
		return false;
	}

	public static InsnContainer buildBreakContainer(SwitchRegion switchRegion) {
		InsnNode breakInsn = new InsnNode(InsnType.BREAK, 0);
		breakInsn.add(AFlag.SYNTHETIC);
		breakInsn.addAttr(new RegionRefAttr(switchRegion));
		return new InsnContainer(breakInsn);
	}
}
