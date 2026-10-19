package jadx.core.dex.visitors.regions;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jadx.core.dex.attributes.AFlag;
import jadx.core.dex.attributes.AType;
import jadx.core.dex.attributes.nodes.LoopInfo;
import jadx.core.dex.info.ClassInfo;
import jadx.core.dex.instructions.IfNode;
import jadx.core.dex.instructions.InsnType;
import jadx.core.dex.instructions.InvokeNode;
import jadx.core.dex.instructions.InvokeType;
import jadx.core.dex.instructions.PhiInsn;
import jadx.core.dex.instructions.args.InsnArg;
import jadx.core.dex.instructions.args.InsnWrapArg;
import jadx.core.dex.instructions.args.RegisterArg;
import jadx.core.dex.instructions.args.SSAVar;
import jadx.core.dex.nodes.BlockNode;
import jadx.core.dex.nodes.IBlock;
import jadx.core.dex.nodes.IBranchRegion;
import jadx.core.dex.nodes.IContainer;
import jadx.core.dex.nodes.IRegion;
import jadx.core.dex.nodes.InsnContainer;
import jadx.core.dex.nodes.InsnNode;
import jadx.core.dex.nodes.MethodNode;
import jadx.core.dex.regions.AbstractRegion;
import jadx.core.dex.regions.Region;
import jadx.core.dex.regions.SwitchRegion;
import jadx.core.dex.regions.TryCatchRegion;
import jadx.core.dex.regions.conditions.IfRegion;
import jadx.core.dex.regions.loops.LoopRegion;
import jadx.core.dex.trycatch.CatchAttr;
import jadx.core.dex.trycatch.ExceptionHandler;
import jadx.core.dex.trycatch.TryCatchBlockAttr;
import jadx.core.dex.visitors.finaly.SameInstructionsStrategy;
import jadx.core.dex.visitors.finaly.SameInstructionsStrategyImpl;
import jadx.core.dex.visitors.kotlin.CoroutineMethodUtils;
import jadx.core.utils.BlockUtils;
import jadx.core.utils.RegionUtils;

/**
 * Extract blocks to separate try/catch region
 */
public class ProcessTryCatchRegions extends AbstractRegionVisitor {

	public static void process(MethodNode mth) {
		if (mth.isNoCode() || mth.isNoExceptionHandlers()) {
			return;
		}
		List<TryCatchBlockAttr> tryBlocks = collectTryCatchBlocks(mth);
		if (tryBlocks.isEmpty()) {
			return;
		}
		TryCatchBlockAttr splitTryBlock = tryBlocks.size() == 1 ? tryBlocks.get(0) : null;
		boolean repeatableRetryHandler = splitTryBlock != null
				&& hasRepeatableCoroutineRetryHandler(mth, splitTryBlock);
		Map<TryCatchBlockAttr, Integer> wrapBudgets = buildDuplicateWrapBudgets(
				mth, tryBlocks, repeatableRetryHandler);
		boolean peeledProtectedEntries = mth.contains(AType.PROTECTED_COROUTINE_ENTRY_SPLIT)
				&& hasIndependentProtectedEntryCatches(tryBlocks);
		if (peeledProtectedEntries) {
			for (TryCatchBlockAttr tryBlock : tryBlocks) {
				Set<BlockNode> protectedBlocks = Collections.newSetFromMap(new IdentityHashMap<>());
				protectedBlocks.addAll(tryBlock.getBlocks());
				Set<IRegion> active = Collections.newSetFromMap(new IdentityHashMap<>());
				int count = countProtectedBlockOccurrences(mth.getRegion(), protectedBlocks, active);
				wrapBudgets.put(tryBlock, Math.max(1, count));
			}
		}
		boolean repeatedNoOpHandler = splitTryBlock != null && hasDuplicatedNoOpHandler(mth, splitTryBlock);
		boolean repeatedStableReturnHandler = splitTryBlock != null
				&& hasDuplicatedStableReturnHandler(mth, splitTryBlock);
		boolean sharedSwitchCatch = splitTryBlock != null && hasSplittableSharedSwitchCatch(mth, splitTryBlock);
		boolean sharedCoroutineRethrow = splitTryBlock != null
				&& hasRepeatableSharedCoroutineRethrow(mth, splitTryBlock);
		boolean protectedLoopBody = splitTryBlock != null
				&& hasTerminalReturnHandlers(splitTryBlock)
				&& hasSplittableProtectedLoopBodies(mth.getRegion(), splitTryBlock);
		boolean terminalReturnHandler = splitTryBlock != null
				&& (splitTryBlock.getBlocks().size() == 1
						|| hasSimpleSharedProtectedTail(mth, splitTryBlock))
				&& hasTerminalReturnHandlers(splitTryBlock)
				&& (protectedLoopBody || canSplitProtectedScope(mth.getRegion(), splitTryBlock));
		boolean exactScope = splitTryBlock != null
				&& (hasRepeatableRethrowHandlers(splitTryBlock) || sharedCoroutineRethrow || repeatedNoOpHandler
						|| repeatedStableReturnHandler || terminalReturnHandler || sharedSwitchCatch
						|| repeatableRetryHandler)
				&& (protectedLoopBody || canSplitProtectedScope(mth.getRegion(), splitTryBlock)
						|| sharedSwitchCatch);
		Map<TryCatchBlockAttr, Boolean> exactScopes = new IdentityHashMap<>();
		// Preserve the per-descriptor proof before wrapping an outer try mutates the region tree.
		for (TryCatchBlockAttr tryBlock : tryBlocks) {
			exactScopes.put(tryBlock, exactScope || isRepeatableSplitCatchAll(mth, tryBlock)
					|| isSharedNestedSwitchCatch(mth, tryBlock)
					|| isRepeatedSingleSiteSwitchCatch(mth, tryBlock)
					|| (canSplitProtectedScope(mth.getRegion(), tryBlock)
							&& (isSimpleNestedSingleSiteCatch(tryBlock)
									|| hasIndependentRepeatableRethrow(mth, tryBlocks, tryBlock))));
		}
		if (peeledProtectedEntries) {
			for (TryCatchBlockAttr tryBlock : tryBlocks) {
				exactScopes.put(tryBlock, true);
			}
		}
		// Preserve the proof from the original region tree. Each successful wrap changes that tree,
		// so recomputing it mid-traversal can strand later protected fragments in another case.
		DepthRegionTraversal.traverseIncludingExcHandlers(mth, (regionMth, region) -> {
			boolean changed = checkAndWrap(regionMth, tryBlocks, wrapBudgets, region, exactScopes,
					repeatedStableReturnHandler);
			return changed && !tryBlocks.isEmpty();
		});
		restoreNestedCatchOrder(mth, mth.getRegion());
		// Evaluate coverage only after wrapping is finished. Nested descriptors may
		// still be covered by an outer region during the traversal.
		if (splitTryBlock != null
				&& (repeatedNoOpHandler || repeatedStableReturnHandler || terminalReturnHandler || sharedSwitchCatch
						|| repeatableRetryHandler || sharedCoroutineRethrow
						|| hasRethrowWithoutReturn(splitTryBlock) || hasMixedHandlerExits(splitTryBlock))) {
			if (containsUnwrappedProtectedOccurrence(mth.getRegion(), splitTryBlock)) {
				mth.addError("Incomplete try/catch coverage: protected fragments remain outside handler; "
						+ "protected region reconstruction is incomplete", null);
			}
			if ((repeatedNoOpHandler || repeatedStableReturnHandler || terminalReturnHandler || sharedSwitchCatch
					|| repeatableRetryHandler || sharedCoroutineRethrow || hasMixedHandlerExits(splitTryBlock))
					&& hasOverextendedProtectedScope(mth.getRegion(), splitTryBlock)) {
				mth.addError("Incorrect try/catch scope: unprotected throwing code was included in handler scope", null);
			}
		}
	}

	/**
	 * A catch-all protecting a typed try's body may be wrapped around the typed catch when its
	 * synthetic entry splitter is nested one level deeper. That reverses exception-table order:
	 * the typed handler then executes before the cleanup. Move the catch-all inside its declared
	 * outer typed descriptor, leaving the unprotected normal tail after both catches.
	 */
	private static void restoreNestedCatchOrder(MethodNode mth, IRegion region) {
		for (IContainer child : new ArrayList<>(region.getSubBlocks())) {
			if (child instanceof IRegion) {
				restoreNestedCatchOrder(mth, (IRegion) child);
			}
		}
		if (!(region instanceof Region)) {
			return;
		}
		List<IContainer> children = region.getSubBlocks();
		for (int index = 0; index < children.size(); index++) {
			IContainer child = children.get(index);
			if (!(child instanceof TryCatchRegion)) {
				continue;
			}
			TryCatchRegion catchAll = (TryCatchRegion) child;
			TryCatchBlockAttr catchAllBlock = catchAll.getTryCatchBlock();
			if (catchAllBlock != null && catchAllBlock.isAllHandler()
					&& catchAll.getFinallyRegion() == null
					&& region.getParent() instanceof TryCatchRegion) {
				TryCatchRegion enclosing = (TryCatchRegion) region.getParent();
				TryCatchBlockAttr enclosingBlock = enclosing.getTryCatchBlock();
				if (enclosing.getTryRegion() == region
						&& enclosingBlock == catchAllBlock.getOuterTryBlock()
						&& !hasOnlyProtectedThrowingCodeAcrossNestedCatches(
								catchAll.getTryRegion(), catchAllBlock, new IdentityHashMap<>())
						&& (factorRecoveringSingleInsnFinally(catchAll, catchAllBlock, enclosingBlock)
								|| factorRecoveringConditionalFinally(catchAll, catchAllBlock, enclosingBlock))) {
					catchAll.setTryCatchBlock(catchAllBlock);
				}
			}
			if (catchAllBlock == null || !catchAllBlock.isAllHandler()
					|| catchAllBlock.getHandlersCount() != 1
					|| containsResourceSuppressionTry(catchAllBlock)
					|| !(catchAll.getTryRegion() instanceof Region)) {
				continue;
			}
			List<IContainer> innerChildren = ((Region) catchAll.getTryRegion()).getSubBlocks();
			int typedIndex = 0;
			while (typedIndex < innerChildren.size()
					&& innerChildren.get(typedIndex) instanceof BlockNode
					&& ((BlockNode) innerChildren.get(typedIndex)).getInstructions().isEmpty()) {
				typedIndex++;
			}
			if (typedIndex >= innerChildren.size()
					|| !(innerChildren.get(typedIndex) instanceof TryCatchRegion)) {
				continue;
			}
			TryCatchRegion typedCatch = (TryCatchRegion) innerChildren.get(typedIndex);
			TryCatchBlockAttr typedBlock = typedCatch.getTryCatchBlock();
			if (typedBlock == null || catchAllBlock.getOuterTryBlock() != typedBlock
					|| containsResourceSuppressionTry(typedBlock)
					|| typedCatch.getFinallyRegion() != null
					|| hasCatchAllHandler(typedBlock)
					|| !typedBlock.getBlocks().containsAll(catchAllBlock.getBlocks())
					|| typedCatch.getCatchRegions().size() != typedBlock.getHandlersCount()
					|| (catchAll.getFinallyRegion() == null
							? catchAll.getCatchRegions().size() != 1
							: !catchAll.getCatchRegions().isEmpty())) {
				continue;
			}
			boolean safeTail = true;
			for (int tailIndex = typedIndex + 1; tailIndex < innerChildren.size(); tailIndex++) {
				IContainer tail = innerChildren.get(tailIndex);
				if (containsProtectedBlock(tail, typedBlock) || containsProtectedBlock(tail, catchAllBlock)) {
					safeTail = false;
					break;
				}
			}
			if (!safeTail) {
				continue;
			}
			Region normalTail = null;
			if (!hasOnlyProtectedThrowingCodeAcrossNestedCatches(typedCatch.getTryRegion(), catchAllBlock,
					new IdentityHashMap<>())) {
				normalTail = extractNormalTailOutsideNestedCatch(typedCatch, catchAllBlock, typedBlock);
				if (normalTail == null) {
					if (!factorRecoveringSingleInsnFinally(typedCatch, catchAllBlock, typedBlock)
							&& !factorRecoveringConditionalFinally(typedCatch, catchAllBlock, typedBlock)) {
						// The generated catch nesting would run the typed handler before this
						// descriptor's cleanup. A throwing inline cleanup cannot simply be
						// moved under the catch-all: that would execute it twice on failure.
						// Reject the unsafe high-level output until the complete finally CFG
						// can be factored and its exception scope proven equivalent.
						mth.addError("Incorrect try/catch order: nested catch-all cleanup requires CFG factoring", null);
						continue;
					}
				}
			}
			Region typedTryBody = new Region(null);
			Region catchAllBody = new Region(null);
			for (int prefixIndex = 0; prefixIndex < typedIndex; prefixIndex++) {
				catchAllBody.add(innerChildren.get(prefixIndex));
			}
			catchAllBody.add(typedCatch.getTryRegion());
			TryCatchRegion relocatedCatchAll = new TryCatchRegion(typedTryBody, catchAllBody);
			relocatedCatchAll.setTryCatchBlock(catchAllBlock);
			catchAllBody.setParent(relocatedCatchAll);
			typedTryBody.add(relocatedCatchAll);
			if (normalTail != null) {
				typedTryBody.add(normalTail);
			}
			TryCatchRegion relocatedTypedCatch = new TryCatchRegion(region, typedTryBody);
			relocatedTypedCatch.setTryCatchBlock(typedBlock);
			typedTryBody.setParent(relocatedTypedCatch);
			children.set(index, relocatedTypedCatch);
			for (int tailIndex = typedIndex + 1; tailIndex < innerChildren.size(); tailIndex++) {
				IContainer tail = innerChildren.get(tailIndex);
				children.add(++index, tail);
				if (tail instanceof IRegion) {
					((IRegion) tail).setParent(region);
				}
			}
		}
	}

	private static final class ConditionalThrowPath {
		private final Set<BlockNode> blocks;
		private final InsnNode thrown;
		private final InsnNode constructor;
		private final boolean fresh;

		private ConditionalThrowPath(Set<BlockNode> blocks, InsnNode thrown, InsnNode constructor, boolean fresh) {
			this.blocks = blocks;
			this.thrown = thrown;
			this.constructor = constructor;
			this.fresh = fresh;
		}
	}

	private static final class DuplicateReturnPlan {
		private final Region catchRegion;
		private final InsnContainer inlineReturn;

		private DuplicateReturnPlan(Region catchRegion, InsnContainer inlineReturn) {
			this.catchRegion = catchRegion;
			this.inlineReturn = inlineReturn;
		}
	}

	/**
	 * Factor a duplicated cleanup with a conditional fresh throw. Unlike the single-instruction
	 * case, both the fresh-throw arm and the original-rethrow arm must be matched before changing
	 * any region: moving just the call would change which exception reaches an enclosing catch.
	 */
	private static boolean factorRecoveringConditionalFinally(
			TryCatchRegion catchAllRegion, TryCatchBlockAttr catchAllBlock, TryCatchBlockAttr typedBlock) {
		ExceptionHandler handler = catchAllBlock.getHandlers().get(0);
		InsnNode caught = null;
		InsnNode cleanup = null;
		IfNode condition = null;
		for (BlockNode block : handler.getBlocks()) {
			for (InsnNode insn : block.getInstructions()) {
				if (insn.getType() == InsnType.MOVE_EXCEPTION && caught == null) {
					caught = insn;
				} else if (insn.getType() == InsnType.INVOKE && cleanup == null && insn.getResult() == null) {
					cleanup = insn;
				} else if (insn instanceof IfNode && condition == null) {
					condition = (IfNode) insn;
				} else if (insn.getType() != InsnType.THROW && insn.getType() != InsnType.GOTO) {
					return false;
				}
			}
		}
		if (caught == null || caught.getResult() == null || cleanup == null || condition == null
				|| condition.getArgsCount() != 2 || handler.getHandlerRegion() == null) {
			return false;
		}
		if (!isLinearHandlerPrefix(handler.getHandlerBlock(), caught, cleanup, condition)) {
			return false;
		}
		ConditionalThrowPath handlerThen = traceConditionalThrowPath(condition.getThenBlock());
		ConditionalThrowPath handlerElse = traceConditionalThrowPath(condition.getElseBlock());
		ConditionalThrowPath handlerFresh;
		boolean freshOnThen;
		if (isOriginalRethrow(handlerElse, caught) && isFreshThrow(handlerThen)) {
			handlerFresh = handlerThen;
			freshOnThen = true;
		} else if (isOriginalRethrow(handlerThen, caught) && isFreshThrow(handlerElse)) {
			handlerFresh = handlerElse;
			freshOnThen = false;
		} else {
			return false;
		}
		ConditionalThrowPath handlerRethrow = freshOnThen ? handlerElse : handlerThen;
		if (!isCoveredByOuterCatch(cleanup, typedBlock)
				|| !isCoveredByOuterCatch(handlerFresh.thrown, typedBlock)
				|| !isCoveredByOuterCatch(handlerFresh.constructor, typedBlock)
				|| !isCoveredByOuterCatch(handlerRethrow.thrown, typedBlock)) {
			return false;
		}
		Set<BlockNode> matchedHandlerBlocks = new HashSet<>(handlerFresh.blocks);
		matchedHandlerBlocks.addAll(handlerRethrow.blocks);
		BlockNode prefix = handler.getHandlerBlock();
		while (prefix != null && matchedHandlerBlocks.add(prefix)) {
			if (prefix.getInstructions().contains(condition)) {
				break;
			}
			List<BlockNode> successors = prefix.getCleanSuccessors();
			prefix = successors.size() == 1 ? successors.get(0) : null;
		}
		if (!matchedHandlerBlocks.containsAll(handler.getBlocks())) {
			return false;
		}
		SameInstructionsStrategy matching = new SameInstructionsStrategyImpl();
		Set<BlockNode> protectedBlocks = new HashSet<>(catchAllBlock.getBlocks());
		Set<BlockNode> handlerBlocks = new HashSet<>(handler.getBlocks());
		handlerBlocks.addAll(handlerFresh.blocks);
		Map<BlockNode, Set<BlockNode>> copies = new IdentityHashMap<>();
		for (BlockNode block : typedBlock.getBlocks()) {
			if (protectedBlocks.contains(block) || handlerBlocks.contains(block)
					|| block.getInstructions().size() != 1
					|| !matching.sameInsns(block.getInstructions().get(0), cleanup)) {
				continue;
			}
			if (block.getCleanSuccessors().size() != 1) {
				return false;
			}
			BlockNode conditionBlock = block.getCleanSuccessors().get(0);
			if (conditionBlock.getInstructions().size() != 1
					|| !(conditionBlock.getInstructions().get(0) instanceof IfNode)
					|| conditionBlock.getPredecessors().stream().anyMatch(pred -> pred != block)) {
				return false;
			}
			IfNode candidateIf = (IfNode) conditionBlock.getInstructions().get(0);
			if (candidateIf.getArgsCount() != 2
					|| !matching.isSameArgs(candidateIf.getArg(0), condition.getArg(0))
					|| !matching.isSameArgs(candidateIf.getArg(1), condition.getArg(1))) {
				return false;
			}
			ConditionalThrowPath thenPath = traceConditionalThrowPath(candidateIf.getThenBlock());
			ConditionalThrowPath elsePath = traceConditionalThrowPath(candidateIf.getElseBlock());
			ConditionalThrowPath candidateFresh;
			boolean candidateFreshOnThen;
			if (isFreshThrow(thenPath) && !isFreshThrow(elsePath)) {
				candidateFresh = thenPath;
				candidateFreshOnThen = true;
			} else if (isFreshThrow(elsePath) && !isFreshThrow(thenPath)) {
				candidateFresh = elsePath;
				candidateFreshOnThen = false;
			} else {
				return false;
			}
			if (candidateFreshOnThen == freshOnThen
					? candidateIf.getOp() != condition.getOp()
					: candidateIf.getOp() != condition.getOp().invert()) {
				return false;
			}
			if (!matching.sameInsns(candidateFresh.thrown, handlerFresh.thrown)
					|| candidateFresh.constructor == null || handlerFresh.constructor == null
					|| !matching.sameInsns(candidateFresh.constructor, handlerFresh.constructor)
					|| !isCoveredByOuterCatch(candidateFresh.thrown, typedBlock)
					|| !isCoveredByOuterCatch(candidateFresh.constructor, typedBlock)
					|| !hasExclusiveConditionalThrowPath(candidateFresh.blocks, conditionBlock)) {
				return false;
			}
			Set<BlockNode> copyBlocks = new HashSet<>(candidateFresh.blocks);
			copyBlocks.add(block);
			copyBlocks.add(conditionBlock);
			copies.put(block, copyBlocks);
		}
		if (copies.isEmpty()) {
			return false;
		}
		Set<IContainer> excluded = Collections.newSetFromMap(new IdentityHashMap<>());
		for (Set<BlockNode> copyBlocks : copies.values()) {
			excluded.addAll(copyBlocks);
		}
		DuplicateReturnPlan duplicateReturn = null;
		if (!hasOnlyProtectedThrowingCodeAcrossNestedCatches(catchAllRegion.getTryRegion(), catchAllBlock,
				new IdentityHashMap<>(), excluded)) {
			duplicateReturn = findDuplicateUnprotectedReturn(catchAllRegion, catchAllBlock, typedBlock, matching);
			if (duplicateReturn == null) {
				return false;
			}
			excluded.add(duplicateReturn.inlineReturn);
			if (!hasOnlyProtectedThrowingCodeAcrossNestedCatches(catchAllRegion.getTryRegion(), catchAllBlock,
					new IdentityHashMap<>(), excluded)) {
				return false;
			}
		}
		Set<BlockNode> reached = Collections.newSetFromMap(new IdentityHashMap<>());
		for (BlockNode block : protectedBlocks) {
			InsnNode last = BlockUtils.getLastInsn(block);
			if (last != null && last.getType() == InsnType.THROW) {
				continue;
			}
			if (block.getCleanSuccessors().isEmpty()) {
				return false;
			}
			for (BlockNode successor : block.getCleanSuccessors()) {
				if (!protectedBlocks.contains(successor)
						&& !allPathsReachFinallyCopy(successor, protectedBlocks, copies.keySet(), reached)) {
					return false;
				}
			}
		}
		if (!reached.equals(copies.keySet())) {
			return false;
		}
		for (BlockNode copyRoot : copies.keySet()) {
			for (BlockNode successor : copyRoot.getCleanSuccessors()) {
				if (canReachProtectedAfterCopy(successor, protectedBlocks)) {
					return false;
				}
			}
		}
		for (Set<BlockNode> copyBlocks : copies.values()) {
			for (BlockNode block : copyBlocks) {
				block.add(AFlag.DONT_GENERATE);
				for (InsnNode insn : block.getInstructions()) {
					insn.add(AFlag.DONT_GENERATE);
				}
			}
		}
		if (duplicateReturn != null) {
			duplicateReturn.catchRegion.getSubBlocks().remove(duplicateReturn.inlineReturn);
		}
		caught.add(AFlag.DONT_GENERATE);
		handlerRethrow.thrown.add(AFlag.DONT_GENERATE);
		handler.setFinally(true);
		catchAllRegion.setTryCatchBlock(catchAllBlock);
		return true;
	}

	/** The inline catch return must be an unprotected duplicate of the normal tail to remove it. */
	private static DuplicateReturnPlan findDuplicateUnprotectedReturn(TryCatchRegion region,
			TryCatchBlockAttr catchAllBlock, TryCatchBlockAttr typedBlock, SameInstructionsStrategy matching) {
		if (!(region.getParent() instanceof Region) || !(region.getTryRegion() instanceof Region)) {
			return null;
		}
		Region parentRegion = (Region) region.getParent();
		BlockNode normalReturnBlock = getSoleReturnBlock(nextSoleSibling(parentRegion, region));
		if (normalReturnBlock == null && parentRegion.getParent() instanceof TryCatchRegion) {
			TryCatchRegion enclosing = (TryCatchRegion) parentRegion.getParent();
			if (enclosing.getTryRegion() == parentRegion
					&& (enclosing.getTryCatchBlock() == catchAllBlock
							|| enclosing.getTryCatchBlock() == typedBlock)
					&& enclosing.getParent() instanceof Region) {
				normalReturnBlock = getSoleReturnBlock(nextSoleSibling((Region) enclosing.getParent(), enclosing));
			}
		}
		if (normalReturnBlock == null || typedBlock.getBlocks().contains(normalReturnBlock)
				|| catchAllBlock.getBlocks().contains(normalReturnBlock)) {
			return null;
		}
		InsnNode tailReturn = normalReturnBlock.getInstructions().get(0);
		if (!isUnprotectedReturn(tailReturn)) {
			return null;
		}
		TryCatchRegion innerCatch = null;
		for (IContainer child : ((Region) region.getTryRegion()).getSubBlocks()) {
			if (child instanceof TryCatchRegion) {
				if (innerCatch != null) {
					return null;
				}
				innerCatch = (TryCatchRegion) child;
			}
		}
		if (innerCatch == null || innerCatch.getCatchRegions().size() != 1) {
			return null;
		}
		IContainer innerCatchBody = innerCatch.getCatchRegions().values().iterator().next();
		if (!(innerCatchBody instanceof Region)) {
			return null;
		}
		Region catchRegion = (Region) innerCatchBody;
		List<IContainer> catchChildren = catchRegion.getSubBlocks();
		if (catchChildren.isEmpty() || !(catchChildren.get(catchChildren.size() - 1) instanceof InsnContainer)) {
			return null;
		}
		InsnContainer inlineReturn = (InsnContainer) catchChildren.get(catchChildren.size() - 1);
		if (inlineReturn.getInstructions().size() != 1) {
			return null;
		}
		InsnNode inlineInsn = inlineReturn.getInstructions().get(0);
		if (!isUnprotectedReturn(inlineInsn) || !sameReturnInsn(inlineInsn, tailReturn, matching)) {
			return null;
		}
		return new DuplicateReturnPlan(catchRegion, inlineReturn);
	}

	private static boolean sameReturnInsn(InsnNode inline, InsnNode tail, SameInstructionsStrategy matching) {
		if (!matching.sameInsns(inline, tail) || inline.getArgsCount() != tail.getArgsCount()) {
			return false;
		}
		for (int i = 0; i < inline.getArgsCount(); i++) {
			InsnArg inlineArg = inline.getArg(i);
			InsnArg tailArg = tail.getArg(i);
			if (inlineArg instanceof InsnWrapArg || tailArg instanceof InsnWrapArg) {
				if (!(inlineArg instanceof InsnWrapArg) || !(tailArg instanceof InsnWrapArg)
						|| !sameReturnInsn(((InsnWrapArg) inlineArg).getWrapInsn(),
								((InsnWrapArg) tailArg).getWrapInsn(), matching)) {
					return false;
				}
			}
		}
		return true;
	}

	private static IContainer nextSoleSibling(Region parent, IContainer child) {
		List<IContainer> children = parent.getSubBlocks();
		int index = children.indexOf(child);
		return index >= 0 && index == children.size() - 2 ? children.get(index + 1) : null;
	}

	private static BlockNode getSoleReturnBlock(IContainer container) {
		while (container instanceof Region) {
			List<IContainer> children = ((Region) container).getSubBlocks();
			if (children.size() != 1) {
				return null;
			}
			container = children.get(0);
		}
		if (!(container instanceof BlockNode) || ((BlockNode) container).getInstructions().size() != 1) {
			return null;
		}
		return (BlockNode) container;
	}

	private static boolean isUnprotectedReturn(InsnNode insn) {
		return insn.getType() == InsnType.RETURN
				&& insn.visitInsns(inner -> inner.get(AType.EXC_CATCH) == null ? null : Boolean.TRUE) == null;
	}

	private static boolean isFreshThrow(ConditionalThrowPath path) {
		return path != null && path.fresh && path.constructor != null;
	}

	private static boolean isCoveredByOuterCatch(InsnNode insn, TryCatchBlockAttr typedBlock) {
		CatchAttr attr = insn.get(AType.EXC_CATCH);
		return attr != null && attr.getHandlers().containsAll(typedBlock.getHandlers());
	}

	private static boolean hasExclusiveConditionalThrowPath(Set<BlockNode> path, BlockNode conditionBlock) {
		for (BlockNode block : path) {
			for (BlockNode predecessor : block.getPredecessors()) {
				if (predecessor != conditionBlock && !path.contains(predecessor)) {
					return false;
				}
			}
		}
		return true;
	}

	private static boolean isLinearHandlerPrefix(BlockNode entry, InsnNode caught, InsnNode cleanup,
			IfNode condition) {
		InsnNode[] expected = { caught, cleanup, condition };
		int index = 0;
		Set<BlockNode> visited = Collections.newSetFromMap(new IdentityHashMap<>());
		BlockNode block = entry;
		while (block != null && visited.add(block) && visited.size() <= 12) {
			for (InsnNode insn : block.getInstructions()) {
				if (insn.getType() == InsnType.GOTO) {
					continue;
				}
				if (index >= expected.length || insn != expected[index++]) {
					return false;
				}
			}
			if (index == expected.length) {
				return true;
			}
			List<BlockNode> successors = block.getCleanSuccessors();
			block = successors.size() == 1 ? successors.get(0) : null;
		}
		return false;
	}

	private static boolean isOriginalRethrow(ConditionalThrowPath path, InsnNode caught) {
		return path != null && !path.fresh && path.thrown.getArgsCount() == 1
				&& path.thrown.getArg(0).equals(caught.getResult());
	}

	private static ConditionalThrowPath traceConditionalThrowPath(BlockNode start) {
		Set<BlockNode> blocks = Collections.newSetFromMap(new IdentityHashMap<>());
		BlockNode block = start;
		InsnNode constructor = null;
		while (block != null && blocks.add(block) && blocks.size() <= 12) {
			for (InsnNode insn : block.getInstructions()) {
				if (insn.getType() == InsnType.CONSTRUCTOR && constructor == null) {
					constructor = insn;
				} else if (insn.getType() == InsnType.THROW && insn.getArgsCount() == 1) {
					InsnArg arg = insn.getArg(0);
					boolean fresh = arg instanceof InsnWrapArg
							&& ((InsnWrapArg) arg).getWrapInsn().getType() == InsnType.CONSTRUCTOR;
					if (fresh) {
						constructor = ((InsnWrapArg) arg).getWrapInsn();
					}
					fresh |= constructor != null && arg.equals(constructor.getResult());
					return new ConditionalThrowPath(blocks, insn, constructor, fresh);
				} else if (insn.getType() != InsnType.GOTO) {
					return null;
				}
			}
			List<BlockNode> successors = block.getCleanSuccessors();
			block = successors.size() == 1 ? successors.get(0) : null;
		}
		return null;
	}

	/**
	 * A recovering typed catch can leave one inline copy of a simple finally operation on each
	 * normal path. The catch-all protects the code before those copies, not the copies themselves.
	 * Factor them into a finally only when every clean exit from the protected range reaches a
	 * matching copy and the catch-all merely runs that operation before rethrowing its input.
	 */
	private static boolean factorRecoveringSingleInsnFinally(
			TryCatchRegion typedCatch, TryCatchBlockAttr catchAllBlock, TryCatchBlockAttr typedBlock) {
		ExceptionHandler handler = catchAllBlock.getHandlers().get(0);
		InsnNode caught = null;
		InsnNode cleanup = null;
		InsnNode rethrow = null;
		for (BlockNode block : handler.getBlocks()) {
			for (InsnNode insn : block.getInstructions()) {
				if (insn.contains(AFlag.DONT_GENERATE)) {
					continue;
				}
				switch (insn.getType()) {
					case MOVE_EXCEPTION:
						if (caught != null) {
							return false;
						}
						caught = insn;
						break;
					case INVOKE:
						if (cleanup != null || insn.getResult() != null) {
							return false;
						}
						cleanup = insn;
						break;
					case THROW:
						if (rethrow != null) {
							return false;
						}
						rethrow = insn;
						break;
					default:
						return false;
				}
			}
		}
		if (caught == null || cleanup == null || rethrow == null
				|| caught.getResult() == null || rethrow.getArgsCount() != 1
				|| !rethrow.getArg(0).equals(caught.getResult())) {
			return false;
		}
		Set<BlockNode> handlerBlocks = new HashSet<>(handler.getBlocks());
		Set<BlockNode> visitedHandlerBlocks = new HashSet<>();
		BlockNode currentHandlerBlock = handler.getHandlerBlock();
		int handlerStep = 0;
		while (currentHandlerBlock != null && visitedHandlerBlocks.add(currentHandlerBlock)) {
			if (!handlerBlocks.contains(currentHandlerBlock)) {
				return false;
			}
			for (InsnNode insn : currentHandlerBlock.getInstructions()) {
				if (insn.contains(AFlag.DONT_GENERATE)) {
					continue;
				}
				if ((handlerStep == 0 && insn == caught)
						|| (handlerStep == 1 && insn == cleanup)
						|| (handlerStep == 2 && insn == rethrow)) {
					handlerStep++;
				} else {
					return false;
				}
			}
			if (handlerStep == 3) {
				break;
			}
			List<BlockNode> continuation = currentHandlerBlock.getCleanSuccessors().stream()
					.filter(handlerBlocks::contains).toList();
			if (continuation.size() != 1) {
				return false;
			}
			currentHandlerBlock = continuation.get(0);
		}
		if (handlerStep != 3 || visitedHandlerBlocks.size() != handlerBlocks.size()) {
			return false;
		}
		boolean cleanupCoveredByOuter = false;
		boolean rethrowCoveredByOuter = false;
		for (BlockNode block : handlerBlocks) {
			if (block.getInstructions().contains(cleanup)) {
				cleanupCoveredByOuter = typedBlock.getBlocks().contains(block)
						&& !catchAllBlock.getBlocks().contains(block);
			}
			if (block.getInstructions().contains(rethrow)) {
				rethrowCoveredByOuter = typedBlock.getBlocks().contains(block);
			}
		}
		if (!cleanupCoveredByOuter || !rethrowCoveredByOuter) {
			return false;
		}

		SameInstructionsStrategy matching = new SameInstructionsStrategyImpl();
		Set<BlockNode> protectedBlocks = new HashSet<>(catchAllBlock.getBlocks());
		Map<BlockNode, InsnNode> copies = new IdentityHashMap<>();
		for (BlockNode block : typedBlock.getBlocks()) {
			if (protectedBlocks.contains(block) || handlerBlocks.contains(block)) {
				continue;
			}
			for (InsnNode insn : block.getInstructions()) {
				if (!matching.sameInsns(insn, cleanup)) {
					continue;
				}
				if (copies.putIfAbsent(block, insn) != null
						|| block.getInstructions().stream()
								.anyMatch(other -> other != insn && other.getType() != InsnType.GOTO)) {
					return false;
				}
			}
		}
		if (copies.isEmpty()) {
			return false;
		}
		Set<IContainer> excluded = Collections.newSetFromMap(new IdentityHashMap<>());
		excluded.addAll(copies.keySet());
		if (!hasOnlyProtectedThrowingCodeAcrossNestedCatches(typedCatch.getTryRegion(), catchAllBlock,
				new IdentityHashMap<>(), excluded)) {
			return false;
		}
		Set<BlockNode> reachedCopies = Collections.newSetFromMap(new IdentityHashMap<>());
		for (BlockNode block : protectedBlocks) {
			InsnNode lastInsn = BlockUtils.getLastInsn(block);
			if (lastInsn != null && lastInsn.getType() == InsnType.THROW) {
				continue;
			}
			List<BlockNode> successors = block.getCleanSuccessors();
			if (successors.isEmpty()) {
				return false;
			}
			for (BlockNode successor : successors) {
				if (!protectedBlocks.contains(successor)
						&& !allPathsReachFinallyCopy(successor, protectedBlocks, copies.keySet(), reachedCopies)) {
					return false;
				}
			}
		}
		if (reachedCopies.size() != copies.size()) {
			return false;
		}
		for (BlockNode copyBlock : copies.keySet()) {
			for (BlockNode successor : copyBlock.getCleanSuccessors()) {
				if (canReachProtectedAfterCopy(successor, protectedBlocks)) {
					return false;
				}
			}
		}
		for (InsnNode copy : copies.values()) {
			copy.add(AFlag.DONT_GENERATE);
		}
		caught.add(AFlag.DONT_GENERATE);
		rethrow.add(AFlag.DONT_GENERATE);
		handler.setFinally(true);
		return true;
	}

	private static boolean canReachProtectedAfterCopy(BlockNode start, Set<BlockNode> protectedBlocks) {
		Set<BlockNode> visited = new HashSet<>();
		ArrayDeque<BlockNode> pending = new ArrayDeque<>();
		pending.add(start);
		while (!pending.isEmpty()) {
			BlockNode block = pending.removeFirst();
			if (protectedBlocks.contains(block)) {
				return true;
			}
			if (visited.add(block)) {
				pending.addAll(block.getCleanSuccessors());
			}
		}
		return false;
	}

	private static boolean allPathsReachFinallyCopy(BlockNode start, Set<BlockNode> protectedBlocks,
			Set<BlockNode> copies, Set<BlockNode> reachedCopies) {
		Set<BlockNode> visited = new HashSet<>();
		ArrayDeque<BlockNode> pending = new ArrayDeque<>();
		pending.add(start);
		while (!pending.isEmpty()) {
			BlockNode block = pending.removeFirst();
			if (protectedBlocks.contains(block)) {
				continue;
			}
			if (copies.contains(block)) {
				reachedCopies.add(block);
				continue;
			}
			if (!visited.add(block)) {
				continue;
			}
			for (InsnNode insn : block.getInstructions()) {
				if (insn.canThrowException() || insn.getType() == InsnType.RETURN || insn.getType() == InsnType.THROW) {
					return false;
				}
			}
			List<BlockNode> successors = block.getCleanSuccessors();
			if (successors.isEmpty()) {
				return false;
			}
			pending.addAll(successors);
		}
		return true;
	}

	/**
	 * An inline normal finally copy may be nested under conditional branches inside an inner
	 * typed catch, while bytecode protects it only with the outer typed catch. Hoist it only when
	 * every alternate branch and every inner catch exits abruptly, and all other throwing code is
	 * covered by the catch-all being relocated.
	 */
	private static Region extractNormalTailOutsideNestedCatch(TryCatchRegion typedCatch,
			TryCatchBlockAttr catchAllBlock, TryCatchBlockAttr typedBlock) {
		if (!(typedCatch.getTryRegion() instanceof Region)) {
			return null;
		}
		Region typedBody = (Region) typedCatch.getTryRegion();
		if (typedBody.getSubBlocks().size() != 1 || !(typedBody.getSubBlocks().get(0) instanceof TryCatchRegion)) {
			return null;
		}
		TryCatchRegion nestedCatch = (TryCatchRegion) typedBody.getSubBlocks().get(0);
		if (nestedCatch.getTryCatchBlock() == null
				|| nestedCatch.getTryCatchBlock().getOuterTryBlock() != catchAllBlock
				|| nestedCatch.getFinallyRegion() != null
				|| !(nestedCatch.getTryRegion() instanceof Region)
				|| nestedCatch.getCatchRegions().isEmpty()
				|| nestedCatch.getCatchRegions().values().stream().anyMatch(c -> !RegionUtils.hasExitBlock(c))) {
			return null;
		}
		Region nestedBody = (Region) nestedCatch.getTryRegion();
		List<Region> candidates = new ArrayList<>();
		collectMixedNormalTailRegions(nestedBody, catchAllBlock, typedBlock, candidates);
		if (candidates.size() != 1) {
			return null;
		}
		Region current = candidates.get(0);
		int splitIndex = getTrailingUnprotectedSplitIndex(current, catchAllBlock);
		List<IContainer> tail = current.getSubBlocks().subList(splitIndex, current.getSubBlocks().size());
		Set<IContainer> excluded = Collections.newSetFromMap(new IdentityHashMap<>());
		excluded.addAll(tail);
		if (!hasOnlyProtectedThrowingCodeAcrossNestedCatches(nestedCatch, catchAllBlock,
				new IdentityHashMap<>(), excluded)) {
			return null;
		}
		List<Region> path = new ArrayList<>();
		while (current != nestedBody) {
			if (!(current.getParent() instanceof IfRegion)) {
				return null;
			}
			IfRegion branch = (IfRegion) current.getParent();
			IContainer sibling = branch.getThenRegion() == current ? branch.getElseRegion() : branch.getThenRegion();
			if (sibling == null || !RegionUtils.hasExitBlock(sibling)
					|| !(branch.getParent() instanceof Region)) {
				return null;
			}
			Region parent = (Region) branch.getParent();
			List<IContainer> siblings = parent.getSubBlocks();
			if (siblings.indexOf(branch) != siblings.size() - 1) {
				return null;
			}
			path.add(current);
			current = parent;
		}
		for (Region branchBody : path) {
			IfRegion branch = (IfRegion) branchBody.getParent();
			Region parent = (Region) branch.getParent();
			Region continuation = extractTrailingRegion(branchBody,
					getTrailingUnprotectedSplitIndex(branchBody, catchAllBlock));
			parent.getSubBlocks().add(parent.getSubBlocks().indexOf(branch) + 1, continuation);
			continuation.setParent(parent);
		}
		return extractTrailingRegion(nestedBody, getTrailingUnprotectedSplitIndex(nestedBody, catchAllBlock));
	}

	private static void collectMixedNormalTailRegions(IContainer container, TryCatchBlockAttr catchAllBlock,
			TryCatchBlockAttr typedBlock, List<Region> candidates) {
		if (!(container instanceof IRegion)) {
			return;
		}
		if (container instanceof Region) {
			Region region = (Region) container;
			int splitIndex = getTrailingUnprotectedSplitIndex(region, catchAllBlock);
			if (splitIndex != -1) {
				List<IContainer> tail = region.getSubBlocks().subList(splitIndex, region.getSubBlocks().size());
				if (tail.stream().allMatch(c -> hasOnlyProtectedThrowingCodeAcrossNestedCatches(c,
						typedBlock, new IdentityHashMap<>()))) {
					candidates.add(region);
				}
			}
		}
		for (IContainer child : ((IRegion) container).getSubBlocks()) {
			collectMixedNormalTailRegions(child, catchAllBlock, typedBlock, candidates);
		}
	}

	/** Check all emitted branches of an inner catch before enclosing it with a relocated catch-all. */
	private static boolean hasOnlyProtectedThrowingCodeAcrossNestedCatches(IContainer container,
			TryCatchBlockAttr tb, Map<IContainer, Boolean> cache) {
		return hasOnlyProtectedThrowingCodeAcrossNestedCatches(container, tb, cache, Collections.emptySet());
	}

	private static boolean hasOnlyProtectedThrowingCodeAcrossNestedCatches(IContainer container,
			TryCatchBlockAttr tb, Map<IContainer, Boolean> cache, Set<IContainer> excluded) {
		if (excluded.contains(container)) {
			return true;
		}
		Boolean cached = cache.get(container);
		if (cached != null) {
			return cached;
		}
		if (container instanceof IBlock) {
			boolean protectedBlock = container instanceof BlockNode && tb.getBlocks().contains(container);
			for (InsnNode insn : ((IBlock) container).getInstructions()) {
				Boolean unsafe = insn.visitInsns(inner -> {
					if (inner.contains(AFlag.DONT_GENERATE)
							|| (inner.getType() == InsnType.CONTINUE && inner.contains(AFlag.SYNTHETIC))
							|| !inner.canThrowException()) {
						return null;
					}
					CatchAttr attr = inner.get(AType.EXC_CATCH);
					return (attr == null ? protectedBlock : attr.getHandlers().containsAll(tb.getHandlers()))
							? null
							: Boolean.TRUE;
				});
				if (unsafe != null) {
					return false;
				}
			}
			cache.put(container, Boolean.TRUE);
			return true;
		}
		if (!(container instanceof IRegion)) {
			return false;
		}
		cache.put(container, Boolean.FALSE);
		for (IContainer child : ((IRegion) container).getSubBlocks()) {
			if (!hasOnlyProtectedThrowingCodeAcrossNestedCatches(child, tb, cache, excluded)) {
				return false;
			}
		}
		cache.put(container, Boolean.TRUE);
		return true;
	}

	private static boolean hasIndependentProtectedEntryCatches(List<TryCatchBlockAttr> tryBlocks) {
		if (tryBlocks.size() != 2) {
			return false;
		}
		TryCatchBlockAttr first = tryBlocks.get(0);
		TryCatchBlockAttr second = tryBlocks.get(1);
		return first.getOuterTryBlock() == null && second.getOuterTryBlock() == null
				&& first.getInnerTryBlocks().isEmpty() && second.getInnerTryBlocks().isEmpty()
				&& Collections.disjoint(first.getBlocks(), second.getBlocks())
				&& first.getHandlers().stream().noneMatch(ExceptionHandler::isFinally)
				&& second.getHandlers().stream().noneMatch(ExceptionHandler::isFinally);
	}

	/**
	 * Coverage is bidirectional: enclosing all protected blocks is insufficient if the generated
	 * try also catches exceptions from previously unprotected operations. In particular, the common
	 * dominator of multiple entries can precede an unprotected initialization branch. This check
	 * permits repeated retry handlers only after the entry-dispatch and single-loop proofs hold.
	 */
	static boolean hasOverextendedProtectedScope(IRegion root, TryCatchBlockAttr tb) {
		Set<IRegion> visited = Collections.newSetFromMap(new IdentityHashMap<>());
		ArrayDeque<IRegion> pending = new ArrayDeque<>();
		pending.add(root);
		while (!pending.isEmpty()) {
			IRegion region = pending.removeLast();
			if (!visited.add(region)) {
				continue;
			}
			if (region instanceof TryCatchRegion && ((TryCatchRegion) region).getTryCatchBlock() == tb) {
				// Catch code is intentionally outside its own protected scope.
				if (!hasOnlyProtectedThrowingCode(((TryCatchRegion) region).getTryRegion(), tb,
						new IdentityHashMap<>())) {
					return true;
				}
				continue;
			}
			for (IContainer child : region.getSubBlocks()) {
				if (child instanceof IRegion) {
					pending.add((IRegion) child);
				}
			}
		}
		return false;
	}

	/**
	 * The CFG pass has routed an unprotected retry suspension back through the entry label
	 * dispatcher. With a single natural loop, each protected call can now own a copy of the
	 * shared catch: its non-throwing handler path continues in that same loop, while the other
	 * path rethrows. Never repeat a handler that exits elsewhere or crosses another loop.
	 */
	private static boolean hasRepeatableCoroutineRetryHandler(MethodNode mth, TryCatchBlockAttr tryBlock) {
		if (mth.get(AType.COROUTINE_RETRY_CATCH_DISPATCH) == null
				|| mth.getLoopsCount() != 1
				|| tryBlock.getBlocks().size() < 2
				|| tryBlock.getHandlers().size() != 1
				|| !canSplitProtectedScope(mth.getRegion(), tryBlock)) {
			return false;
		}
		ExceptionHandler handler = tryBlock.getHandlers().get(0);
		if (handler.isFinally() || handler.getHandlerRegion() == null
				|| handlerExitKinds(handler.getHandlerRegion(), new IdentityHashMap<>(),
						Collections.newSetFromMap(new IdentityHashMap<>())) != (HANDLER_THROW | HANDLER_FALL_THROUGH)) {
			return false;
		}
		List<LoopInfo> handlerLoops = mth.getAllLoopsForBlock(handler.getHandlerBlock());
		if (handlerLoops.size() != 1) {
			return false;
		}
		LoopInfo retryLoop = handlerLoops.get(0);
		for (BlockNode protectedBlock : tryBlock.getBlocks()) {
			List<LoopInfo> loops = mth.getAllLoopsForBlock(protectedBlock);
			if (loops.size() != 1 || loops.get(0) != retryLoop) {
				return false;
			}
		}
		return true;
	}

	private static boolean hasRepeatableSharedCoroutineRethrow(MethodNode mth, TryCatchBlockAttr tryBlock) {
		if (!CoroutineMethodUtils.isStateMachineBody(mth) || mth.getLoopsCount() != 0
				|| tryBlock.getBlocks().size() < 2 || tryBlock.getHandlers().size() != 1
				|| !canSplitProtectedScope(mth.getRegion(), tryBlock)) {
			return false;
		}
		ExceptionHandler handler = tryBlock.getHandlers().get(0);
		IRegion body = handler.getHandlerRegion();
		if (handler.isFinally() || body == null || body.getSubBlocks().isEmpty()) {
			return false;
		}
		IContainer tail = body.getSubBlocks().get(body.getSubBlocks().size() - 1);
		if (!(tail instanceof IBlock) || ((IBlock) tail).getInstructions().size() != 1) {
			return false;
		}
		InsnNode last = ((IBlock) tail).getInstructions().get(0);
		return last.getType() == InsnType.THROW && tail.contains(AFlag.SYNTHETIC)
				&& handlerExitKinds(body, new IdentityHashMap<>(),
						Collections.newSetFromMap(new IdentityHashMap<>())) == (HANDLER_THROW | HANDLER_RETURN)
				&& hasStableHandlerReturn(mth, body, tryBlock);
	}

	/** A copied suspension return may not depend on a value produced by one protected case. */
	private static boolean hasStableHandlerReturn(MethodNode mth, IRegion body, TryCatchBlockAttr tryBlock) {
		Set<IBlock> handlerBlocks = new HashSet<>();
		RegionUtils.getAllRegionBlocks(body, handlerBlocks);
		boolean found = false;
		for (IBlock block : handlerBlocks) {
			for (InsnNode insn : block.getInstructions()) {
				if (insn.getType() != InsnType.RETURN) {
					continue;
				}
				found = true;
				if (insn.getArgsCount() != 1 || !(insn.getArg(0) instanceof RegisterArg)) {
					return false;
				}
				InsnNode assign = ((RegisterArg) insn.getArg(0)).getAssignInsn();
				BlockNode source = assign == null ? null : BlockUtils.getBlockByInsn(mth, assign);
				if (source == null || handlerBlocks.contains(source) || tryBlock.getBlocks().contains(source)) {
					return false;
				}
				for (BlockNode protectedBlock : tryBlock.getBlocks()) {
					if (!protectedBlock.getDoms().get(source.getId())) {
						return false;
					}
				}
			}
		}
		return found;
	}

	private static Map<TryCatchBlockAttr, Integer> buildDuplicateWrapBudgets(
			MethodNode mth, List<TryCatchBlockAttr> tryBlocks, boolean repeatableRetryHandler) {
		Map<TryCatchBlockAttr, Integer> budgets = new IdentityHashMap<>();
		for (TryCatchBlockAttr tryBlock : tryBlocks) {
			boolean singleDescriptor = tryBlocks.size() == 1
					&& (hasSplittableSharedSwitchCatch(mth, tryBlock)
							|| hasRepeatableSharedCoroutineRethrow(mth, tryBlock)
							|| hasRepeatableRethrowHandlers(tryBlock)
							|| hasDuplicatedNoOpHandler(mth, tryBlock)
							|| hasDuplicatedStableReturnHandler(mth, tryBlock)
							|| repeatableRetryHandler);
			boolean independentRethrow = hasIndependentRepeatableRethrow(mth, tryBlocks, tryBlock)
					|| isRepeatableSplitCatchAll(mth, tryBlock);
			boolean sharedNestedSwitchCatch = isSharedNestedSwitchCatch(mth, tryBlock);
			if (!singleDescriptor && !independentRethrow && !sharedNestedSwitchCatch
					&& !isRepeatedSingleSiteSwitchCatch(mth, tryBlock)) {
				// Nested/overlapping handlers may contain another emitted catch; do not copy them.
				budgets.put(tryBlock, 1);
				continue;
			}
			Set<BlockNode> protectedBlocks = Collections.newSetFromMap(new IdentityHashMap<>());
			protectedBlocks.addAll(tryBlock.getBlocks());
			Set<IRegion> active = Collections.newSetFromMap(new IdentityHashMap<>());
			int budget = countProtectedBlockOccurrences(mth.getRegion(), protectedBlocks, active);
			for (ExceptionHandler handler : mth.getExceptionHandlers()) {
				IContainer handlerRegion = handler.getHandlerRegion();
				if (handlerRegion instanceof IRegion) {
					budget += countProtectedBlockOccurrences((IRegion) handlerRegion, protectedBlocks, active);
				}
			}
			budgets.put(tryBlock, Math.max(1, budget));
		}
		return budgets;
	}

	private static boolean hasIndependentRepeatableRethrow(
			MethodNode mth, List<TryCatchBlockAttr> tryBlocks, TryCatchBlockAttr tryBlock) {
		if (tryBlocks.size() <= 1
				|| tryBlock.getOuterTryBlock() != null || !tryBlock.getInnerTryBlocks().isEmpty()
				|| containsResourceSuppressionTry(tryBlock)
				|| !hasRepeatableRethrowHandlers(tryBlock)
				|| !canSplitProtectedScope(mth.getRegion(), tryBlock)) {
			return false;
		}
		for (TryCatchBlockAttr other : tryBlocks) {
			if (other != tryBlock && !Collections.disjoint(tryBlock.getBlocks(), other.getBlocks())) {
				// Sibling descriptors can still overlap without a recorded nesting relation.
				return false;
			}
		}
		return true;
	}

	private static boolean isRepeatableSplitCatchAll(MethodNode mth, TryCatchBlockAttr tryBlock) {
		return mth.getLoopsCount() == 0 && tryBlock.isAllHandler()
				&& tryBlock.getBlocks().size() > 1
				&& !tryBlock.getInnerTryBlocks().isEmpty()
				&& hasProtectedBlocksInDifferentSwitchCases(mth.getRegion(), tryBlock)
				&& hasRepeatableRethrowHandlers(tryBlock);
	}

	private static boolean isSharedNestedSwitchCatch(MethodNode mth, TryCatchBlockAttr tryBlock) {
		TryCatchBlockAttr outer = tryBlock.getOuterTryBlock();
		return mth.getLoopsCount() == 0 && outer != null && outer.isAllHandler()
				&& outer.getBlocks().containsAll(tryBlock.getBlocks())
				&& tryBlock.getBlocks().size() > 1
				&& tryBlock.getHandlers().size() == 1 && !tryBlock.getHandlers().get(0).isCatchAll()
				&& hasProtectedBlocksInDifferentSwitchCases(mth.getRegion(), tryBlock)
				&& canSplitProtectedScope(mth.getRegion(), tryBlock);
	}

	private static boolean isRepeatedSingleSiteSwitchCatch(MethodNode mth, TryCatchBlockAttr tryBlock) {
		return mth.getLoopsCount() == 0 && tryBlock.getBlocks().size() == 1
				&& tryBlock.getHandlers().size() > 1
				&& tryBlock.getHandlers().stream().noneMatch(ExceptionHandler::isFinally)
				&& hasProtectedBlocksInDifferentSwitchCases(mth.getRegion(), tryBlock)
				&& canSplitProtectedScope(mth.getRegion(), tryBlock);
	}

	static boolean hasProtectedBlocksInDifferentSwitchCases(IRegion region, TryCatchBlockAttr tryBlock) {
		Set<IRegion> active = Collections.newSetFromMap(new IdentityHashMap<>());
		Set<BlockNode> protectedBlocks = Collections.newSetFromMap(new IdentityHashMap<>());
		protectedBlocks.addAll(tryBlock.getBlocks());
		return hasProtectedBlocksInDifferentSwitchCases(region, protectedBlocks, active);
	}

	private static boolean hasProtectedBlocksInDifferentSwitchCases(
			IRegion region, Set<BlockNode> protectedBlocks, Set<IRegion> active) {
		if (!active.add(region)) {
			return false;
		}
		try {
			if (region instanceof SwitchRegion) {
				int protectedCases = 0;
				Set<IContainer> countedCases = Collections.newSetFromMap(new IdentityHashMap<>());
				for (SwitchRegion.CaseInfo caseInfo : ((SwitchRegion) region).getCases()) {
					IContainer caseContainer = caseInfo.getContainer();
					if (countedCases.add(caseContainer) && containsProtectedCaseBlock(caseContainer, protectedBlocks)
							&& ++protectedCases > 1) {
						return true;
					}
				}
			}
			for (IContainer child : region.getSubBlocks()) {
				if (child instanceof IRegion
						&& hasProtectedBlocksInDifferentSwitchCases((IRegion) child, protectedBlocks, active)) {
					return true;
				}
			}
			return false;
		} finally {
			active.remove(region);
		}
	}

	private static boolean containsProtectedCaseBlock(IContainer container, Set<BlockNode> protectedBlocks) {
		Set<IContainer> visited = Collections.newSetFromMap(new IdentityHashMap<>());
		ArrayDeque<IContainer> pending = new ArrayDeque<>();
		pending.add(container);
		while (!pending.isEmpty()) {
			IContainer current = pending.removeLast();
			if (!visited.add(current)) {
				continue;
			}
			if (current instanceof BlockNode && protectedBlocks.contains(current)) {
				return true;
			}
			if (current instanceof IRegion) {
				pending.addAll(((IRegion) current).getSubBlocks());
			}
		}
		return false;
	}

	private static boolean hasSplittableSharedSwitchCatch(MethodNode mth, TryCatchBlockAttr tryBlock) {
		boolean splittable = mth.getLoopsCount() == 0
				? canSplitProtectedScope(mth.getRegion(), tryBlock)
				: hasSplittableProtectedLoopBodies(mth.getRegion(), tryBlock);
		return splittable && hasSharedSwitchCaseCatchExit(mth, tryBlock);
	}

	/**
	 * Split a repeated catch inside switch cases only when each protected case reaches the
	 * handler's shared post-switch block or contains that same terminal return. At least one
	 * protected case must expose the shared exit outside the switch, so copied catches can
	 * break without executing unprotected statements after their protected fragments.
	 */
	private static boolean hasSharedSwitchCaseCatchExit(MethodNode mth, TryCatchBlockAttr tryBlock) {
		if (tryBlock.getBlocks().size() < 2 || tryBlock.getHandlers().isEmpty()) {
			return false;
		}
		Set<BlockNode> protectedBlocks = Collections.newSetFromMap(new IdentityHashMap<>());
		protectedBlocks.addAll(tryBlock.getBlocks());
		int total = countProtectedBlockOccurrences(mth.getRegion(), protectedBlocks,
				Collections.newSetFromMap(new IdentityHashMap<>()));
		return total >= 2 && hasSharedSwitchCaseCatchExit(mth.getRegion(), tryBlock, protectedBlocks, total,
				Collections.newSetFromMap(new IdentityHashMap<>()));
	}

	private static boolean hasSharedSwitchCaseCatchExit(IRegion region, TryCatchBlockAttr tryBlock,
			Set<BlockNode> protectedBlocks, int total, Set<IRegion> active) {
		if (!active.add(region)) {
			return false;
		}
		try {
			if (region instanceof SwitchRegion) {
				int covered = 0;
				boolean sharedExitCase = false;
				for (IContainer caseContainer : ((SwitchRegion) region).getCaseContainers()) {
					if (caseContainer instanceof IRegion) {
						int inCase = countProtectedBlockOccurrences((IRegion) caseContainer, protectedBlocks,
								Collections.newSetFromMap(new IdentityHashMap<>()));
						if (inCase != 0) {
							boolean sharedExit = hasSharedSwitchCatchExit(caseContainer, tryBlock);
							if (!sharedExit && !hasTerminalCatchJoin(caseContainer, tryBlock)) {
								covered = -1;
								break;
							}
							sharedExitCase |= sharedExit;
							covered += inCase;
						}
					}
				}
				if (covered == total && sharedExitCase) {
					return true;
				}
			}
			for (IContainer child : region.getSubBlocks()) {
				if (child instanceof IRegion && hasSharedSwitchCaseCatchExit((IRegion) child,
						tryBlock, protectedBlocks, total, active)) {
					return true;
				}
			}
			return false;
		} finally {
			active.remove(region);
		}
	}

	public static boolean hasSharedCatchExit(IContainer caseContainer, TryCatchBlockAttr tryBlock) {
		return hasSharedCatchExit(caseContainer, getSharedLinearHandlerExit(tryBlock), false);
	}

	public static Set<BlockNode> getCatchBoundaryTargets(IContainer catchRegion) {
		Set<IBlock> catchBlocks = new HashSet<>();
		RegionUtils.getAllRegionBlocks(catchRegion, catchBlocks);
		return RestoreExceptionHandlerLoopExits.collectBoundaryTargets(catchBlocks);
	}

	static boolean hasSharedSwitchCatchExit(IContainer caseContainer, TryCatchBlockAttr tryBlock) {
		BlockNode handlerExit = getSharedLinearHandlerExit(tryBlock);
		if (handlerExit == null && hasMixedHandlerExits(tryBlock)) {
			handlerExit = getSharedSwitchHandlerExit(tryBlock);
		}
		return hasSharedCatchExit(caseContainer, handlerExit, true);
	}

	private static boolean hasSharedCatchExit(IContainer caseContainer, BlockNode handlerExit,
			boolean ignoreTerminalEdges) {
		if (handlerExit == null) {
			return false;
		}
		Set<IBlock> caseBlocks = new HashSet<>();
		RegionUtils.getAllRegionBlocks(caseContainer, caseBlocks);
		Set<BlockNode> normalExits = new HashSet<>();
		for (IBlock block : caseBlocks) {
			if (block instanceof BlockNode && !BlockUtils.isExceptionHandlerPath((BlockNode) block)
					&& (!ignoreTerminalEdges || !BlockUtils.containsExitInsn(block))) {
				for (BlockNode successor : ((BlockNode) block).getCleanSuccessors()) {
					if (!caseBlocks.contains(successor)) {
						normalExits.add(successor);
					}
				}
			}
		}
		return normalExits.size() == 1
				&& (normalExits.contains(handlerExit)
						|| reachesSharedExitThroughEmptyBlocks(normalExits.iterator().next(), handlerExit)
						|| reachesSharedExitThroughEmptyBlocks(handlerExit, normalExits.iterator().next()));
	}

	/** Synthetic empty joins can sit on either side of the shared case exit. */
	private static boolean reachesSharedExitThroughEmptyBlocks(BlockNode start, BlockNode target) {
		Set<BlockNode> visited = Collections.newSetFromMap(new IdentityHashMap<>());
		BlockNode block = start;
		while (block != target && visited.add(block)) {
			if (!block.getInstructions().isEmpty() || block.getCleanSuccessors().size() != 1) {
				return false;
			}
			block = block.getCleanSuccessors().get(0);
		}
		return block == target;
	}

	private static boolean isSimpleNestedSingleSiteCatch(TryCatchBlockAttr tryBlock) {
		return tryBlock.getOuterTryBlock() != null
				&& tryBlock.getBlocks().size() == 1
				&& tryBlock.getHandlers().stream().noneMatch(ExceptionHandler::isFinally)
				&& !containsResourceSuppressionTry(tryBlock.getOuterTryBlock());
	}

	public static boolean hasTerminalCatchJoin(IContainer caseContainer, TryCatchBlockAttr tryBlock) {
		BlockNode handlerExit = getSharedLinearHandlerExit(tryBlock);
		if (handlerExit == null) {
			return false;
		}
		Set<IBlock> caseBlocks = new HashSet<>();
		RegionUtils.getAllRegionBlocks(caseContainer, caseBlocks);
		Set<BlockNode> visited = Collections.newSetFromMap(new IdentityHashMap<>());
		BlockNode terminal = handlerExit;
		while (caseBlocks.contains(terminal) && visited.add(terminal)
				&& terminal.getInstructions().isEmpty() && terminal.getCleanSuccessors().size() == 1) {
			terminal = terminal.getCleanSuccessors().get(0);
		}
		if (!caseBlocks.contains(terminal) || BlockUtils.getLastInsn(terminal) == null
				|| BlockUtils.getLastInsn(terminal).getType() != InsnType.RETURN) {
			return false;
		}
		for (IBlock block : caseBlocks) {
			if (block != terminal && BlockUtils.containsExitInsn(block)) {
				return false;
			}
			if (block != terminal && block instanceof BlockNode
					&& !BlockUtils.isExceptionHandlerPath((BlockNode) block)) {
				for (BlockNode successor : ((BlockNode) block).getCleanSuccessors()) {
					if (!caseBlocks.contains(successor) && successor != terminal) {
						return false;
					}
				}
			}
		}
		return true;
	}

	private static BlockNode getSharedLinearHandlerExit(TryCatchBlockAttr tryBlock) {
		BlockNode sharedExit = null;
		for (ExceptionHandler handler : tryBlock.getHandlers()) {
			IContainer handlerRegion = handler.getHandlerRegion();
			if (handler.isFinally() || !(handlerRegion instanceof Region)
					|| RegionUtils.hasExitBlock(handlerRegion)) {
				return null;
			}
			for (IContainer child : ((Region) handlerRegion).getSubBlocks()) {
				if (!(child instanceof IBlock) || BlockUtils.containsExitInsn((IBlock) child)) {
					return null;
				}
			}
			Set<IBlock> handlerBlocks = new HashSet<>();
			RegionUtils.getAllRegionBlocks(handlerRegion, handlerBlocks);
			Set<BlockNode> handlerExits = RestoreExceptionHandlerLoopExits.collectBoundaryTargets(handlerBlocks);
			if (handlerExits.size() != 1) {
				return null;
			}
			BlockNode exit = handlerExits.iterator().next();
			if (sharedExit != null && sharedExit != exit) {
				return null;
			}
			sharedExit = exit;
		}
		return sharedExit;
	}

	/**
	 * A switch continuation can share a terminal return with a conditional rethrowing handler.
	 * Unlike a linear handler, only a return already present at the shared CFG exit may be
	 * copied into each protected case; a distinct handler return can depend on case-local state.
	 */
	private static BlockNode getSharedSwitchHandlerExit(TryCatchBlockAttr tryBlock) {
		BlockNode sharedExit = null;
		for (ExceptionHandler handler : tryBlock.getHandlers()) {
			IContainer handlerRegion = handler.getHandlerRegion();
			if (handler.isFinally() || !(handlerRegion instanceof Region)) {
				return null;
			}
			int exits = handlerExitKinds(handlerRegion, new IdentityHashMap<>(),
					Collections.newSetFromMap(new IdentityHashMap<>()));
			if ((exits & (HANDLER_OTHER_EXIT | HANDLER_LOOP_EXIT)) != 0) {
				return null;
			}
			Set<IBlock> handlerBlocks = new HashSet<>();
			RegionUtils.getAllRegionBlocks(handlerRegion, handlerBlocks);
			Set<BlockNode> boundaryTargets = RestoreExceptionHandlerLoopExits.collectBoundaryTargets(handlerBlocks);
			if (boundaryTargets.size() != 1) {
				return null;
			}
			BlockNode exit = boundaryTargets.iterator().next();
			if (sharedExit != null && sharedExit != exit) {
				return null;
			}
			if ((exits & HANDLER_RETURN) != 0 && !hasOnlySharedExitReturn(handlerRegion, exit,
					Collections.newSetFromMap(new IdentityHashMap<>()))) {
				return null;
			}
			sharedExit = exit;
		}
		return sharedExit;
	}

	private static boolean hasOnlySharedExitReturn(IContainer container, BlockNode sharedExit,
			Set<IContainer> active) {
		if (!active.add(container)) {
			return false;
		}
		try {
			if (container instanceof IBlock) {
				for (InsnNode insn : ((IBlock) container).getInstructions()) {
					if (insn.getType() == InsnType.RETURN && insn != BlockUtils.getLastInsn(sharedExit)) {
						return false;
					}
				}
			}
			if (container instanceof IRegion) {
				for (IContainer child : ((IRegion) container).getSubBlocks()) {
					if (!hasOnlySharedExitReturn(child, sharedExit, active)) {
						return false;
					}
				}
			}
			return true;
		} finally {
			active.remove(container);
		}
	}

	static boolean hasRepeatableRethrowHandlers(TryCatchBlockAttr tryBlock) {
		for (ExceptionHandler handler : tryBlock.getHandlers()) {
			// Inspect the emitted region, not the handler block inventory: a shared
			// rethrow can be outside that inventory, and a conditional throw does not
			// prove that the other branch preserves its continuation after copying.
			if (handlerExitKinds(handler.getHandlerRegion(), new IdentityHashMap<>(),
					Collections.newSetFromMap(new IdentityHashMap<>())) != HANDLER_THROW) {
				return false;
			}
		}
		return !tryBlock.getHandlers().isEmpty();
	}

	private static boolean hasTerminalReturnHandlers(TryCatchBlockAttr tryBlock) {
		if (tryBlock.getHandlers().isEmpty()) {
			return false;
		}
		for (ExceptionHandler handler : tryBlock.getHandlers()) {
			if (handler.isFinally()
					|| handlerExitKinds(handler.getHandlerRegion(), new IdentityHashMap<>(),
							Collections.newSetFromMap(new IdentityHashMap<>())) != HANDLER_RETURN) {
				return false;
			}
		}
		return true;
	}

	/**
	 * A linear catch ending in a literal or pre-protection SSA value has no continuation state to
	 * merge. Its preceding side effects execute only on the exceptional path, so the whole catch
	 * can be copied to each protected branch without widening the scope. A branch-dependent
	 * return can also be copied when its exception PHI binds the value available before
	 * every protected throwing instruction. Branching handlers still need separate proof.
	 */
	private static boolean hasDuplicatedStableReturnHandler(MethodNode mth, TryCatchBlockAttr tryBlock) {
		if (tryBlock.getBlocks().size() < 2
				|| !(mth.getLoopsCount() == 0 && canSplitProtectedScope(mth.getRegion(), tryBlock)
						|| hasSplittableProtectedLoopBodies(mth.getRegion(), tryBlock))
				|| tryBlock.getHandlers().isEmpty()) {
			return false;
		}
		for (ExceptionHandler handler : tryBlock.getHandlers()) {
			if (handler.isFinally() || !hasLinearStableReturn(mth, tryBlock, handler.getHandlerRegion())) {
				return false;
			}
		}
		return true;
	}

	private static boolean hasLinearStableReturn(MethodNode mth, TryCatchBlockAttr tryBlock, IContainer container) {
		if (!(container instanceof Region)) {
			return false;
		}
		boolean foundReturn = false;
		Set<SSAVar> assignedInHandler = Collections.newSetFromMap(new IdentityHashMap<>());
		for (IContainer child : ((Region) container).getSubBlocks()) {
			if (!(child instanceof IBlock)) {
				return false;
			}
			for (InsnNode insn : ((IBlock) child).getInstructions()) {
				if (insn.contains(AFlag.DONT_GENERATE)) {
					continue;
				}
				if (foundReturn || insn.getType() == InsnType.THROW
						|| insn.getType() == InsnType.BREAK || insn.getType() == InsnType.CONTINUE) {
					return false;
				}
				if (insn.getType() == InsnType.RETURN) {
					if (insn.getArgsCount() > 1
							|| insn.getArgsCount() == 1 && !insn.getArg(0).isLiteral()
									&& !isHandlerLocalStaticReturn(insn.getArg(0))
									&& (!(insn.getArg(0) instanceof RegisterArg)
											|| !assignedInHandler.contains(((RegisterArg) insn.getArg(0)).getSVar())
													&& !isStableReturnRegister(mth, tryBlock, (RegisterArg) insn.getArg(0),
															Collections.newSetFromMap(new IdentityHashMap<>())))) {
						return false;
					}
					foundReturn = true;
				}
				RegisterArg result = insn.getResult();
				if (result != null && result.getSVar() != null) {
					assignedInHandler.add(result.getSVar());
				}
			}
		}
		return foundReturn;
	}

	private static boolean isHandlerLocalStaticReturn(InsnArg arg) {
		if (!(arg instanceof InsnWrapArg)) {
			return false;
		}
		InsnNode wrapped = ((InsnWrapArg) arg).getWrapInsn();
		return wrapped instanceof InvokeNode
				&& ((InvokeNode) wrapped).getInvokeType() == InvokeType.STATIC
				&& wrapped.getArgsCount() == 0;
	}

	private static boolean isStableReturnRegister(MethodNode mth, TryCatchBlockAttr tryBlock,
			RegisterArg register, Set<SSAVar> seen) {
		SSAVar sVar = register.getSVar();
		if (sVar == null || !seen.add(sVar)) {
			return false;
		}
		for (RegisterArg arg : mth.getAllArgRegs()) {
			if (arg.getSVar() == sVar) {
				return true;
			}
		}
		InsnNode assign = sVar.getAssign().getParentInsn();
		if (assign == null) {
			return false;
		}
		if (assign.getType() == InsnType.MOVE && assign.getArgsCount() == 1
				&& assign.getArg(0) instanceof RegisterArg) {
			return isStableReturnRegister(mth, tryBlock, (RegisterArg) assign.getArg(0), seen);
		}
		if (assign instanceof PhiInsn) {
			return isPreProtectedReturnPhi(mth, tryBlock, (PhiInsn) assign);
		}
		BlockNode assignBlock = BlockUtils.getBlockByInsn(mth, assign);
		if (assignBlock == null) {
			return false;
		}
		for (BlockNode protectedBlock : tryBlock.getBlocks()) {
			if (assignBlock == protectedBlock || !protectedBlock.isDominator(assignBlock)) {
				return false;
			}
		}
		return true;
	}

	private static boolean isPreProtectedReturnPhi(MethodNode mth, TryCatchBlockAttr tryBlock, PhiInsn phi) {
		List<BlockNode> protectedBlocks = tryBlock.getBlocks();
		if (phi.getArgsCount() < protectedBlocks.size()) {
			return false;
		}
		Map<BlockNode, List<InsnNode>> throwSites = new IdentityHashMap<>();
		Map<BlockNode, Integer> nextSite = new IdentityHashMap<>();
		for (int i = 0; i < phi.getArgsCount(); i++) {
			BlockNode source = phi.getBlockByArgIndex(i);
			if (!protectedBlocks.contains(source)) {
				return false;
			}
			List<InsnNode> sites = throwSites.computeIfAbsent(source,
					block -> collectProtectedThrowSites(mth, tryBlock, block));
			int siteIndex = nextSite.getOrDefault(source, 0);
			if (siteIndex >= sites.size()) {
				return false;
			}
			nextSite.put(source, siteIndex + 1);
			SSAVar input = phi.getArg(i).getSVar();
			if (input == null) {
				return false;
			}
			if (mth.getAllArgRegs().stream().anyMatch(arg -> arg.getSVar() == input)) {
				continue;
			}
			if (input.getAssignInsn() == null) {
				return false;
			}
			InsnNode assign = input.getAssignInsn();
			BlockNode assignBlock = BlockUtils.getBlockByInsn(mth, assign);
			if (assignBlock == null || assign.getType() == InsnType.PHI) {
				return false;
			}
			if (assignBlock == source) {
				List<InsnNode> instructions = source.getInstructions();
				int assignIndex = instructions.indexOf(assign);
				if (assignIndex < 0 || assignIndex >= instructions.indexOf(sites.get(siteIndex))) {
					return false;
				}
			} else if (protectedBlocks.contains(assignBlock) || !source.isDominator(assignBlock)) {
				return false;
			}
		}
		for (BlockNode protectedBlock : protectedBlocks) {
			List<InsnNode> sites = throwSites.get(protectedBlock);
			if (sites == null || nextSite.get(protectedBlock) != sites.size()) {
				return false;
			}
		}
		return true;
	}

	private static List<InsnNode> collectProtectedThrowSites(
			MethodNode mth, TryCatchBlockAttr tryBlock, BlockNode source) {
		List<InsnNode> sites = new ArrayList<>();
		for (InsnNode insn : source.getInstructions()) {
			if (!insn.canThrowException()) {
				continue;
			}
			CatchAttr catchAttr = BlockUtils.getCatchAttrForInsn(mth, insn);
			if (catchAttr != null && catchAttr.getHandlers().equals(tryBlock.getHandlers())) {
				sites.add(insn);
			}
		}
		return sites;
	}

	/**
	 * A regular loop can contain protected calls separated by unprotected operations. Its condition
	 * is not part of the catch, so split only direct body blocks around those calls; never widen
	 * the catch to the loop header or move a loop transfer across the boundary. Follow a single
	 * protected root path into the loop tree, and split protected blocks in each body,
	 * including nested loops and branches with unprotected conditions or selectors.
	 */
	private static boolean hasSplittableProtectedLoopBodies(IRegion root, TryCatchBlockAttr tryBlock) {
		if (tryBlock.getBlocks().isEmpty()) {
			return false;
		}
		return hasDirectProtectedLoopBodyOnPath(root, tryBlock);
	}

	private static boolean hasDirectProtectedLoopBodyOnPath(IRegion region, TryCatchBlockAttr tryBlock) {
		if (!(region instanceof Region)) {
			return false;
		}
		IContainer protectedChild = null;
		for (IContainer child : region.getSubBlocks()) {
			if (!containsProtectedBlock(child, tryBlock)) {
				continue;
			}
			if (protectedChild != null) {
				return false;
			}
			protectedChild = child;
		}
		if (!(protectedChild instanceof LoopRegion)) {
			return protectedChild instanceof Region
					&& hasDirectProtectedLoopBodyOnPath((IRegion) protectedChild, tryBlock);
		}
		return hasSplittableProtectedLoop((LoopRegion) protectedChild, tryBlock);
	}

	private static boolean hasSplittableProtectedLoop(LoopRegion protectedLoop, TryCatchBlockAttr tryBlock) {
		if (protectedLoop.isEndless()) {
			return false;
		}
		for (BlockNode condition : protectedLoop.getConditionBlocks()) {
			if (containsProtectedBlock(condition, tryBlock)) {
				return false;
			}
		}
		IRegion body = protectedLoop.getBody();
		if (body instanceof Region && body.getSubBlocks().contains(tryBlock.getTopSplitter())
				&& body.getSubBlocks().containsAll(tryBlock.getBlocks())) {
			return canSplitProtectedScope(body, tryBlock);
		}
		if (!(body instanceof Region)) {
			return false;
		}
		boolean localProtectedFragment = false;
		boolean nestedProtectedLoop = false;
		for (IContainer child : body.getSubBlocks()) {
			if (!containsProtectedBlock(child, tryBlock)) {
				continue;
			}
			if (child instanceof BlockNode && tryBlock.getBlocks().contains(child)
					&& canSplitProtectedScope(child, tryBlock)) {
				localProtectedFragment = true;
			} else if ((child instanceof IfRegion || child instanceof SwitchRegion)
					&& canSplitProtectedScope(child, tryBlock)) {
				localProtectedFragment = true;
			} else if (child instanceof LoopRegion && hasSplittableProtectedLoop((LoopRegion) child, tryBlock)) {
				nestedProtectedLoop = true;
			} else {
				return false;
			}
		}
		return localProtectedFragment || nestedProtectedLoop;
	}

	/**
	 * Permit exact-scope wrapping of a split protected range only when its final throwing
	 * block is a shared continuation after a branch. A pair of protected calls in separate
	 * branches needs separate handler copies and is not covered by this shape.
	 */
	private static boolean hasSimpleSharedProtectedTail(MethodNode mth, TryCatchBlockAttr tryBlock) {
		if (mth.getLoopsCount() != 0 || !(mth.getRegion() instanceof Region)) {
			return false;
		}
		List<IContainer> blocks = mth.getRegion().getSubBlocks();
		int start = blocks.indexOf(tryBlock.getTopSplitter());
		int end = getLastProtectedContainerIndex(blocks, tryBlock);
		if (start == -1 || end <= start) {
			return false;
		}
		boolean sharedProtectedBlock = false;
		for (int index = start; index <= end; index++) {
			IContainer block = blocks.get(index);
			if (block instanceof BlockNode && tryBlock.getBlocks().contains(block)) {
				sharedProtectedBlock = true;
			}
			if (!hasOnlyProtectedThrowingCode(block, tryBlock, new IdentityHashMap<>())) {
				return false;
			}
		}
		return sharedProtectedBlock;
	}

	private static boolean hasDuplicatedNoOpHandler(MethodNode mth, TryCatchBlockAttr tryBlock) {
		if (!hasNoOpHandlers(tryBlock) || !canSplitProtectedScope(mth.getRegion(), tryBlock)) {
			return false;
		}
		Set<BlockNode> protectedBlocks = Collections.newSetFromMap(new IdentityHashMap<>());
		protectedBlocks.addAll(tryBlock.getBlocks());
		return countProtectedBlockOccurrences(mth.getRegion(), protectedBlocks,
				Collections.newSetFromMap(new IdentityHashMap<>())) > protectedBlocks.size();
	}

	/**
	 * A catch with no emitted body can be repeated across duplicated protected occurrences:
	 * each copy still suppresses the same exception and continues from its own branch.
	 * Keep this proof deliberately narrow; even a single assignment in the handler can
	 * depend on the original shared continuation and must not be copied here.
	 */
	private static boolean hasNoOpHandlers(TryCatchBlockAttr tryBlock) {
		if (tryBlock.getHandlers().isEmpty()) {
			return false;
		}
		for (ExceptionHandler handler : tryBlock.getHandlers()) {
			if (handler.isFinally()) {
				return false;
			}
			IContainer body = handler.getHandlerRegion();
			if (!(body instanceof Region) || ((Region) body).getSubBlocks().size() != 1) {
				return false;
			}
			IContainer block = ((Region) body).getSubBlocks().get(0);
			if (!(block instanceof BlockNode)) {
				return false;
			}
			for (InsnNode insn : ((BlockNode) block).getInstructions()) {
				if (!insn.contains(AFlag.DONT_GENERATE)) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Preserve the protected boundary for handlers with a terminal path and a different exit.
	 * No explicit rethrow is required: a retry handler may return on exhaustion instead.
	 * A break can be represented by an inlined tail ending in return, so the block inventory's
	 * absence-of-return test cannot decide this. This is not a proof that the handler can be copied.
	 * Single-exit handlers retain the existing reconstruction: their shared normal/exceptional
	 * tails and compiler-moved instructions need a separate scope proof.
	 * A loop-transfer/fall-through pair alone is not enough: both paths can rejoin the retry
	 * header, and moving the success tail can turn that implicit retry into a normal return.
	 */
	static boolean hasMixedHandlerExits(TryCatchBlockAttr tryBlock) {
		if (tryBlock.getHandlers().stream().anyMatch(ExceptionHandler::isFinally)) {
			return false;
		}
		for (ExceptionHandler handler : tryBlock.getHandlers()) {
			IContainer body = handler.getHandlerRegion();
			if (body == null || body.contains(AFlag.DONT_GENERATE)) {
				continue;
			}
			int exits = handlerExitKinds(body, new IdentityHashMap<>(),
					Collections.newSetFromMap(new IdentityHashMap<>()));
			int structuredExits = exits & (HANDLER_THROW | HANDLER_FALL_THROUGH | HANDLER_LOOP_EXIT | HANDLER_RETURN);
			if ((structuredExits & (HANDLER_THROW | HANDLER_RETURN)) != 0
					&& Integer.bitCount(structuredExits) > 1) {
				return true;
			}
		}
		return false;
	}

	private static final int HANDLER_FALL_THROUGH = 1;
	private static final int HANDLER_THROW = 2;
	private static final int HANDLER_OTHER_EXIT = 4;
	private static final int HANDLER_LOOP_EXIT = 8;
	private static final int HANDLER_RETURN = 16;

	private static int handlerExitKinds(
			IContainer container, Map<IContainer, Integer> cache, Set<IContainer> active) {
		if (container == null || container.contains(AFlag.DONT_GENERATE)) {
			return HANDLER_FALL_THROUGH;
		}
		Integer cached = cache.get(container);
		if (cached != null) {
			return cached;
		}
		if (!active.add(container)) {
			return HANDLER_OTHER_EXIT;
		}
		try {
			int exits = HANDLER_OTHER_EXIT;
			if (container instanceof IBlock) {
				exits = HANDLER_FALL_THROUGH;
				for (InsnNode insn : ((IBlock) container).getInstructions()) {
					if (insn.contains(AFlag.DONT_GENERATE)) {
						continue;
					}
					switch (insn.getType()) {
						case THROW:
							exits = HANDLER_THROW;
							break;
						case BREAK:
						case CONTINUE:
							// A structured loop transfer still leaves the protected operation's
							// handler. Keep its scope boundary, but never classify it as safe
							// to duplicate like an unconditional rethrow.
							exits = HANDLER_LOOP_EXIT;
							break;
						case RETURN:
							exits = HANDLER_RETURN;
							break;
						case GOTO:
						case IF:
						case SWITCH:
							exits = HANDLER_OTHER_EXIT;
							break;
						default:
							break;
					}
					if (exits != HANDLER_FALL_THROUGH) {
						break;
					}
				}
			} else if (container instanceof IfRegion) {
				IfRegion branch = (IfRegion) container;
				exits = handlerExitKinds(branch.getThenRegion(), cache, active)
						| handlerExitKinds(branch.getElseRegion(), cache, active);
			} else if (container instanceof Region) {
				exits = HANDLER_FALL_THROUGH;
				for (IContainer block : ((Region) container).getSubBlocks()) {
					if ((exits & HANDLER_FALL_THROUGH) == 0) {
						break;
					}
					exits = (exits & ~HANDLER_FALL_THROUGH) | handlerExitKinds(block, cache, active);
				}
			}
			// Loops and nested try/finally need their own exit/exception proof.
			cache.put(container, exits);
			return exits;
		} finally {
			active.remove(container);
		}
	}

	static boolean hasRethrowWithoutReturn(TryCatchBlockAttr tryBlock) {
		for (ExceptionHandler handler : tryBlock.getHandlers()) {
			if (handler.isFinally()) {
				// Finally extraction owns its implicit rethrow and protected cleanup copies.
				return false;
			}
			boolean throwFound = false;
			for (BlockNode block : handler.getBlocks()) {
				for (InsnNode insn : block.getInstructions()) {
					if (insn.getType() == InsnType.RETURN) {
						return false;
					}
					// A shared rethrow may already be materialized as edge instructions;
					// its original block is then suppressed, but still proves a throw path.
					if (insn.getType() == InsnType.THROW) {
						throwFound = true;
					}
				}
			}
			if (!throwFound) {
				return false;
			}
		}
		return !tryBlock.getHandlers().isEmpty();
	}

	private static int countProtectedBlockOccurrences(
			IRegion region, Set<BlockNode> protectedBlocks, Set<IRegion> active) {
		// A shared region can be emitted from multiple parents. Track only the current
		// recursion path so each emission is budgeted while cycles remain bounded.
		if (!active.add(region)) {
			return 0;
		}
		try {
			int count = 0;
			for (IContainer container : region.getSubBlocks()) {
				if (container instanceof BlockNode && protectedBlocks.contains(container)) {
					count++;
				}
				if (container instanceof IRegion) {
					count += countProtectedBlockOccurrences((IRegion) container, protectedBlocks, active);
				}
			}
			return count;
		} finally {
			active.remove(region);
		}
	}

	private static List<TryCatchBlockAttr> collectTryCatchBlocks(MethodNode mth) {
		List<TryCatchBlockAttr> list = mth.getAll(AType.TRY_BLOCKS_LIST);
		if (list.isEmpty()) {
			return Collections.emptyList();
		}
		List<TryCatchBlockAttr> tryBlocks = new ArrayList<>(list);
		mergeEquivalentTryBlocks(mth, tryBlocks);
		return orderTryBlocksForWrapping(tryBlocks);
	}

	/**
	 * Some bytecode producers split handlers for one protected range into several exception-table
	 * entries. Building a nested try region for each entry changes catch precedence whenever a
	 * subtype and its supertype land in different entries. Merge exact sibling ranges and restore
	 * Java's narrow-to-wide handler order before region construction.
	 */
	private static void mergeEquivalentTryBlocks(MethodNode mth, List<TryCatchBlockAttr> tryBlocks) {
		for (int i = 0; i < tryBlocks.size(); i++) {
			TryCatchBlockAttr target = tryBlocks.get(i);
			for (int j = tryBlocks.size() - 1; j > i; j--) {
				TryCatchBlockAttr candidate = tryBlocks.get(j);
				if (!hasEquivalentProtectedRange(target, candidate)) {
					continue;
				}
				for (ExceptionHandler handler : candidate.getHandlers()) {
					handler.setTryBlock(target);
					target.getHandlers().add(handler);
				}
				for (TryCatchBlockAttr inner : new ArrayList<>(candidate.getInnerTryBlocks())) {
					inner.setOuterTryBlock(target);
					target.addInnerTryBlock(inner);
				}
				TryCatchBlockAttr outer = candidate.getOuterTryBlock();
				if (outer != null) {
					outer.getInnerTryBlocks().remove(candidate);
				}
				candidate.setMerged(true);
				tryBlocks.remove(j);
			}
			orderHandlersBySpecificity(mth, target.getHandlers());
		}
	}

	private static boolean hasEquivalentProtectedRange(TryCatchBlockAttr first, TryCatchBlockAttr second) {
		return first.getOuterTryBlock() == second.getOuterTryBlock()
				&& first.getTopSplitter() == second.getTopSplitter()
				&& first.getBlocks().equals(second.getBlocks());
	}

	private static void orderHandlersBySpecificity(MethodNode mth, List<ExceptionHandler> handlers) {
		if (handlers.size() < 2) {
			return;
		}
		List<ExceptionHandler> remaining = new ArrayList<>(handlers);
		List<ExceptionHandler> ordered = new ArrayList<>(handlers.size());
		while (!remaining.isEmpty()) {
			int selected = -1;
			for (int i = 0; i < remaining.size(); i++) {
				ExceptionHandler candidate = remaining.get(i);
				boolean hasNarrowerHandler = false;
				for (ExceptionHandler other : remaining) {
					if (other != candidate && isCatchDomainNarrower(mth, other, candidate)) {
						hasNarrowerHandler = true;
						break;
					}
				}
				if (!hasNarrowerHandler) {
					selected = i;
					break;
				}
			}
			if (selected == -1) {
				selected = 0;
			}
			ordered.add(remaining.remove(selected));
		}
		handlers.clear();
		handlers.addAll(ordered);
	}

	private static boolean isCatchDomainNarrower(
			MethodNode mth, ExceptionHandler first, ExceptionHandler second) {
		if (first.isCatchAll()) {
			return false;
		}
		if (second.isCatchAll()) {
			return true;
		}
		boolean strict = false;
		for (ClassInfo firstType : first.getCatchTypes()) {
			boolean covered = false;
			for (ClassInfo secondType : second.getCatchTypes()) {
				var comparison = mth.root().getTypeCompare().compareTypes(firstType, secondType);
				if (comparison.isNarrowOrEqual()) {
					covered = true;
					strict |= comparison.isNarrow();
					break;
				}
			}
			if (!covered) {
				return false;
			}
		}
		return strict;
	}

	/**
	 * Wrapping several try blocks around the same structured region reverses their effective
	 * nesting order, so preserve exception-table precedence by consuming the input in reverse
	 * order. The exception is an outer synthetic range with the exact same entry splitter: it
	 * must be installed before its child so a catch-all/finally wrapper stays outside typed
	 * catches. Use an explicit topological selection instead of a non-transitive comparator.
	 */
	private static List<TryCatchBlockAttr> orderTryBlocksForWrapping(List<TryCatchBlockAttr> input) {
		List<TryCatchBlockAttr> remaining = new ArrayList<>(input);
		Collections.reverse(remaining);
		List<TryCatchBlockAttr> ordered = new ArrayList<>(input.size());
		while (!remaining.isEmpty()) {
			int selected = -1;
			for (int i = 0; i < remaining.size(); i++) {
				TryCatchBlockAttr candidate = remaining.get(i);
				boolean hasRequiredPredecessor = false;
				for (TryCatchBlockAttr other : remaining) {
					if (other != candidate && mustWrapBefore(other, candidate)) {
						hasRequiredPredecessor = true;
						break;
					}
				}
				if (!hasRequiredPredecessor) {
					selected = i;
					break;
				}
			}
			if (selected == -1) {
				// Defensive fallback for malformed cyclic metadata.
				selected = 0;
			}
			ordered.add(remaining.remove(selected));
		}
		return ordered;
	}

	private static boolean mustWrapBefore(TryCatchBlockAttr first, TryCatchBlockAttr second) {
		if (first.getTopSplitter() != second.getTopSplitter()) {
			return false;
		}
		if (hasCatchAllHandler(first)
				&& !hasCatchAllHandler(second)
				&& first.getBlocks().size() > second.getBlocks().size()
				&& first.getBlocks().containsAll(second.getBlocks())) {
			// A catch-all range which also covers the typed handler is the compiler's
			// unextracted finally wrapper. Install it first so the typed catch remains
			// inside and an exception is not cleaned up before it reaches that catch.
			return true;
		}
		for (TryCatchBlockAttr outer = second.getOuterTryBlock(); outer != null; outer = outer.getOuterTryBlock()) {
			if (outer == first) {
				return true;
			}
		}
		return false;
	}

	private static boolean hasCatchAllHandler(TryCatchBlockAttr tryBlock) {
		return tryBlock.getHandlers().stream().anyMatch(ExceptionHandler::isCatchAll);
	}

	private static boolean checkAndWrap(
			MethodNode mth,
			List<TryCatchBlockAttr> tryBlocks,
			Map<TryCatchBlockAttr, Integer> wrapBudgets,
			IRegion region, Map<TryCatchBlockAttr, Boolean> exactScopes, boolean repeatedStableReturnHandler) {
		// search top splitter block in this region (don't need to go deeper)
		for (TryCatchBlockAttr tb : tryBlocks) {
			if (tb.getOuterTryBlock() != null && tryBlocks.contains(tb.getOuterTryBlock())
					&& hasCatchAllHandler(tb) && !hasCatchAllHandler(tb.getOuterTryBlock())) {
				// An inner catch-all must sit inside its typed parent. Other child
				// descriptors can need their original ordering for shared-handler CFGs.
				continue;
			}
			boolean tbExactScope = exactScopes.getOrDefault(tb, false);
			if (isInsideTryCatchRegion(region, tb)) {
				continue;
			}
			BlockNode topSplitter = tb.getTopSplitter();
			if (region.getSubBlocks().contains(topSplitter) && containsProtectedBlock(region, tb)) {
				int unwrappedBefore = countAllUnwrappedProtectedOccurrences(mth, tb);
				if (wrapBlocks(region, tb, topSplitter, tbExactScope)) {
					if (!keepForDuplicateWrap(mth, tb, wrapBudgets, unwrappedBefore)) {
						tryBlocks.remove(tb);
					}
					return true;
				}
				if (!tbExactScope) {
					mth.addError("Can't wrap try/catch for region: " + region, null);
					tryBlocks.remove(tb);
					return true;
				}
				// A common CFG dominator can precede unprotected code. Try a local
				// protected anchor, or descend into the separate branch fragments.
			}
			BlockNode protectedAnchor = findDirectProtectedAnchor(region, tb, tbExactScope,
					repeatedStableReturnHandler);
			if (protectedAnchor != null) {
				int unwrappedBefore = countAllUnwrappedProtectedOccurrences(mth, tb);
				if (!wrapBlocks(region, tb, protectedAnchor, tbExactScope)) {
					if (tbExactScope) {
						continue;
					}
					mth.addError("Can't wrap narrow try/catch for region: " + region, null);
					tryBlocks.remove(tb);
				} else if (!keepForDuplicateWrap(mth, tb, wrapBudgets, unwrappedBefore)) {
					tryBlocks.remove(tb);
				}
				return true;
			}
		}
		return false;
	}

	/**
	 * Region reconstruction can split a protected bytecode range when separate coroutine resume
	 * states enter it. Consuming the try descriptor after the first structured fragment leaves the
	 * other fragment outside its handler. For handlers proven safe to repeat, keep wrapping until
	 * every occurrence is either under the matching try/catch region or absent from the region tree.
	 */
	private static boolean keepForDuplicateWrap(
			MethodNode mth,
			TryCatchBlockAttr tb,
			Map<TryCatchBlockAttr, Integer> wrapBudgets,
			int unwrappedBefore) {
		int remainingBudget = wrapBudgets.compute(tb, (key, value) -> value == null ? 0 : value - 1);
		if (remainingBudget <= 0) {
			return false;
		}
		int unwrappedAfter = countAllUnwrappedProtectedOccurrences(mth, tb);
		return unwrappedAfter > 0 && unwrappedAfter < unwrappedBefore;
	}

	/** Count occurrences in the main region and in handler trees not yet attached to it. */
	private static int countAllUnwrappedProtectedOccurrences(MethodNode mth, TryCatchBlockAttr tb) {
		IRegion mainRoot = mth.getRegion();
		int count = countUnwrappedProtectedOccurrences(mainRoot, tb);
		Set<IRegion> covered = Collections.newSetFromMap(new IdentityHashMap<>());
		collectRegionIdentities(mainRoot, covered);
		for (ExceptionHandler handler : mth.getExceptionHandlers()) {
			IContainer handlerRegion = handler.getHandlerRegion();
			if (!(handlerRegion instanceof IRegion) || covered.contains(handlerRegion)) {
				continue;
			}
			IRegion root = getRegionTreeRoot((IRegion) handlerRegion);
			if (covered.contains(root)) {
				continue;
			}
			count += countUnwrappedProtectedOccurrences(root, tb);
			collectRegionIdentities(root, covered);
		}
		return count;
	}

	private static void collectRegionIdentities(IRegion region, Set<IRegion> collected) {
		if (!collected.add(region)) {
			return;
		}
		for (IContainer child : region.getSubBlocks()) {
			if (child instanceof IRegion) {
				collectRegionIdentities((IRegion) child, collected);
			}
		}
	}

	private static IRegion getRegionTreeRoot(IRegion region) {
		IRegion treeRoot = region;
		while (treeRoot.getParent() != null) {
			treeRoot = treeRoot.getParent();
		}
		return treeRoot;
	}

	static boolean containsUnwrappedProtectedOccurrence(IRegion region, TryCatchBlockAttr tb) {
		return countUnwrappedProtectedOccurrences(region, tb) != 0;
	}

	static int countUnwrappedProtectedOccurrences(IRegion region, TryCatchBlockAttr tb) {
		Set<IRegion> active = Collections.newSetFromMap(new IdentityHashMap<>());
		Set<BlockNode> protectedBlocks = Collections.newSetFromMap(new IdentityHashMap<>());
		protectedBlocks.addAll(tb.getBlocks());
		return countUnwrappedProtectedOccurrences(region, tb, protectedBlocks, active);
	}

	private static int countUnwrappedProtectedOccurrences(
			IRegion region,
			TryCatchBlockAttr tb,
			Set<BlockNode> protectedBlocks,
			Set<IRegion> active) {
		if (!active.add(region)) {
			return 0;
		}
		try {
			if (region instanceof TryCatchRegion
					&& ((TryCatchRegion) region).getTryCatchBlock() == tb) {
				return 0;
			}
			int count = 0;
			for (IContainer container : region.getSubBlocks()) {
				if (container instanceof BlockNode && protectedBlocks.contains(container)) {
					count++;
				}
				if (container instanceof IRegion) {
					count += countUnwrappedProtectedOccurrences((IRegion) container, tb, protectedBlocks, active);
				}
			}
			return count;
		} finally {
			active.remove(region);
		}
	}

	private static boolean isInsideTryCatchRegion(IRegion region, TryCatchBlockAttr tb) {
		for (IRegion current = region; current != null; current = current.getParent()) {
			if (current instanceof TryCatchRegion
					&& ((TryCatchRegion) current).getTryCatchBlock() == tb) {
				return true;
			}
		}
		return false;
	}

	/**
	 * A synthetic exception top-splitter can remain outside the structured branch which owns a
	 * protected fragment. Use its first direct protected block in emitted region order, not the
	 * descriptor's bytecode order. Multiple blocks require a terminal handler and a scope check
	 * to avoid catching operations which were outside the original exception range.
	 */
	static BlockNode findDirectProtectedAnchor(IRegion region, TryCatchBlockAttr tb) {
		return findDirectProtectedAnchor(region, tb, false, false);
	}

	private static BlockNode findDirectProtectedAnchor(IRegion region, TryCatchBlockAttr tb,
			boolean exactScope, boolean repeatedStableReturnHandler) {
		BlockNode anchor = null;
		for (IContainer child : region.getSubBlocks()) {
			if (!(child instanceof BlockNode) || !tb.getBlocks().contains(child)) {
				continue;
			}
			if (anchor != null) {
				// A split protected fragment can contain several direct blocks (for example a
				// coroutine loop body). Only reuse a terminal handler if the entire local region
				// can be enclosed without catching any previously unprotected throwing operation.
				// An already-proven exact scope can start here even when this parent is mixed:
				// wrapBlocks narrows to protected children and validates the resulting try region.
				return (hasRepeatableRethrowHandlers(tb) || repeatedStableReturnHandler || exactScope)
						&& (exactScope || hasOnlyProtectedThrowingCode(region, tb, new IdentityHashMap<>())) ? anchor : null;
			}
			anchor = (BlockNode) child;
		}
		return anchor;
	}

	static boolean hasOnlyProtectedThrowingCode(IContainer container, TryCatchBlockAttr tb,
			Map<IContainer, Boolean> cache) {
		Boolean cached = cache.get(container);
		if (cached != null) {
			return cached;
		}
		cache.put(container, Boolean.FALSE); // reject region cycles
		if (container instanceof TryCatchRegion) {
			return false; // never enclose another emitted catch and catch its rethrow again
		}
		if (container instanceof IBlock) {
			boolean protectedBlock = container instanceof BlockNode && tb.getBlocks().contains(container);
			for (InsnNode insn : ((IBlock) container).getInstructions()) {
				Boolean unprotected = insn.visitInsns(inner -> {
					// Generated loop transfers have no bytecode throw site. Keep this exception
					// local to scope validation: changing the global throw predicate also
					// changes how other protected loop regions are reconstructed.
					if (inner.contains(AFlag.DONT_GENERATE)
							|| (inner.getType() == InsnType.CONTINUE && inner.contains(AFlag.SYNTHETIC))
							|| !inner.canThrowException()) {
						return null;
					}
					CatchAttr attr = inner.get(AType.EXC_CATCH);
					boolean matches = attr == null
							? protectedBlock
							: hasExpectedHandlers(attr, tb);
					return matches ? null : Boolean.TRUE;
				});
				if (unprotected != null) {
					return false;
				}
			}
		} else if (container instanceof IRegion) {
			for (IContainer child : ((IRegion) container).getSubBlocks()) {
				if (!hasOnlyProtectedThrowingCode(child, tb, cache)) {
					return false;
				}
			}
		} else {
			return false;
		}
		cache.put(container, Boolean.TRUE);
		return true;
	}

	private static boolean hasExpectedHandlers(CatchAttr attr, TryCatchBlockAttr tb) {
		List<ExceptionHandler> actual = attr.getHandlers();
		List<ExceptionHandler> expected = tb.getHandlers();
		// CatchAttr is sorted by handler bytecode offset, while the try descriptor
		// keeps catch precedence order. Scope validation compares membership, not
		// emission order (which is handled when constructing the catch region).
		if (hasSameHandlerMembers(actual, expected)) {
			return true;
		}
		// Nested protected instructions also carry their enclosing handlers. Accept
		// only those known outer handlers, without assuming bytecode-offset order.
		if (expected.isEmpty() || tb.getOuterTryBlock() == null) {
			return false;
		}
		Set<ExceptionHandler> outerHandlers = new HashSet<>();
		for (TryCatchBlockAttr outer = tb.getOuterTryBlock(); outer != null; outer = outer.getOuterTryBlock()) {
			outerHandlers.addAll(outer.getHandlers());
		}
		List<ExceptionHandler> unmatched = new ArrayList<>(expected);
		for (ExceptionHandler handler : actual) {
			if (unmatched.remove(handler)) {
				continue;
			}
			if (!outerHandlers.contains(handler) && !handler.isCatchAll()) {
				return false;
			}
		}
		return unmatched.isEmpty();
	}

	private static boolean hasSameHandlerMembers(List<ExceptionHandler> actual, List<ExceptionHandler> expected) {
		if (actual.size() != expected.size()) {
			return false;
		}
		List<ExceptionHandler> unmatched = new ArrayList<>(expected);
		for (ExceptionHandler handler : actual) {
			if (!unmatched.remove(handler)) {
				return false;
			}
		}
		return unmatched.isEmpty();
	}

	/**
	 * Only split along existing sequence/branch boundaries. A protected condition or selector
	 * with an unprotected branch would require restructuring the branch itself; do not partially
	 * rewrite
	 * such a tree. Existing finally/cleanup reconstruction still owns those cases.
	 */
	static boolean canSplitProtectedScope(IContainer container, TryCatchBlockAttr tb) {
		Set<IContainer> active = Collections.newSetFromMap(new IdentityHashMap<>());
		return canSplitProtectedScope(container, tb, active);
	}

	private static boolean canSplitProtectedScope(
			IContainer container, TryCatchBlockAttr tb, Set<IContainer> active) {
		if (!active.add(container)) {
			// A cyclic region tree has no finite sequence/branch split proof.
			return false;
		}
		try {
			if (!containsProtectedBlock(container, tb)
					|| hasOnlyProtectedThrowingCode(container, tb, new IdentityHashMap<>())) {
				return true;
			}
			if (container instanceof IfRegion) {
				for (BlockNode condition : ((IfRegion) container).getConditionBlocks()) {
					if (containsProtectedBlock(condition, tb)) {
						return false;
					}
				}
			} else if (container instanceof SwitchRegion) {
				if (containsProtectedBlock(((SwitchRegion) container).getHeader(), tb)) {
					return false;
				}
			} else if (container instanceof LoopRegion) {
				if (!((LoopRegion) container).isEndless()) {
					return false;
				}
			} else if (!(container instanceof Region)) {
				return false;
			}
			for (IContainer child : ((IRegion) container).getSubBlocks()) {
				if (!canSplitProtectedScope(child, tb, active)) {
					return false;
				}
			}
			return true;
		} finally {
			active.remove(container);
		}
	}

	/**
	 * Extract all block dominated by 'dominator' to separate region and mark as try/catch block
	 */
	static boolean wrapBlocks(IRegion replaceRegion, TryCatchBlockAttr tb, BlockNode dominator, boolean exactScope) {
		if (replaceRegion == null) {
			return false;
		}
		if (replaceRegion instanceof LoopRegion) {
			LoopRegion loop = (LoopRegion) replaceRegion;
			return wrapBlocks(loop.getBody(), tb, dominator, exactScope);
		}
		if (replaceRegion instanceof IBranchRegion) {
			return wrapBlocks(replaceRegion.getParent(), tb, dominator, exactScope);
		}
		// Tail splitting mutates the region tree. Reject an anchor outside this direct
		// sequence before splitting, otherwise a failed speculative wrap leaves moved
		// continuations behind for subsequent region traversal.
		if (exactScope && !replaceRegion.getSubBlocks().contains(dominator)) {
			return false;
		}

		Region tryRegion = new Region(replaceRegion);
		boolean narrowNestedTail = !exactScope && hasSingleProtectedNestedTryTail(replaceRegion, tb, dominator);
		boolean adjacentForeignTry = hasFollowingUnprotectedTry(replaceRegion, tb, dominator);
		boolean splitProtectedTail = isResourceSuppressionTry(tb)
				|| hasFinallyTailBoundary(tb)
				|| hasFinallyControlOverride(tb)
				|| hasMixedHandlerExits(tb)
				|| narrowNestedTail
				|| adjacentForeignTry;
		if (splitProtectedTail) {
			splitTrailingUnprotectedRegions(replaceRegion, tb);
		}
		List<IContainer> subBlocks = replaceRegion.getSubBlocks();
		int startIndex = exactScope || narrowNestedTail ? subBlocks.indexOf(dominator) : 0;
		if (startIndex < 0) {
			return false;
		}
		int lastProtectedIndex = splitProtectedTail ? getLastProtectedContainerIndex(subBlocks, tb) : -1;
		// traverse the enclosing region for blocks that have a path from the dominator but don't have a
		// path from any of the exception handlers i.e. they are not before the end of the try block so
		// should be inside the try block.
		// In split-scope mode, a loop back edge must not pull an earlier emitted
		// sibling into the wrapper just because it is reachable in the next iteration.
		for (int index = startIndex; index < subBlocks.size(); index++) {
			IContainer cont = subBlocks.get(index);
			if (cont == dominator || RegionUtils.hasPathThroughBlock(dominator, cont)) {
				if (lastProtectedIndex != -1 && index > lastProtectedIndex) {
					break;
				}
				boolean containsTryBlock = containsProtectedBlock(cont, tb);
				if (exactScope && !hasOnlyProtectedThrowingCode(cont, tb, new IdentityHashMap<>())) {
					// A mixed branch must be split recursively. Do not absorb its protected
					// arm into a preceding protected sequence and widen the catch over
					// unprotected throwing code in the other arm.
					if (!tryRegion.getSubBlocks().isEmpty()) {
						break;
					}
					continue;
				}
				if (!containsTryBlock && cont != dominator && isHandlerPath(tb, cont)) {
					// this block/region has a path from an exception handler so is after the end of the try block
					continue;
				}
				// Do not reparent candidates until the scope check and replacement succeed.
				tryRegion.getSubBlocks().add(cont);
			}
		}
		if (tryRegion.getSubBlocks().isEmpty()) {
			return false;
		}
		if (exactScope && (!containsProtectedBlock(tryRegion, tb)
				|| !hasOnlyProtectedThrowingCode(tryRegion, tb, new IdentityHashMap<>()))) {
			return false;
		}

		TryCatchRegion tryCatchRegion = new TryCatchRegion(replaceRegion, tryRegion);
		tryRegion.setParent(tryCatchRegion);
		tryCatchRegion.setTryCatchBlock(tb);
		TryCatchBlockAttr outer = tb.getOuterTryBlock();
		if (exactScope && outer != null && outer.isAllHandler()
				&& tb.getHandlers().stream().noneMatch(ExceptionHandler::isCatchAll)
				&& hasProtectedBlocksInDifferentSwitchCases(getRegionTreeRoot(replaceRegion), tb)
				&& outer.getBlocks().containsAll(tb.getBlocks())) {
			// These instructions are protected by both descriptors. The outer catch-all
			// must be a sibling after the typed catch for this exact protected fragment;
			// nesting it around the typed handler would catch its unprotected continuation.
			tryCatchRegion.addCatchHandler(outer.getHandlers().get(0));
		}

		// replace first node by region
		IContainer firstNode = tryRegion.getSubBlocks().get(0);
		if (!replaceRegion.replaceSubBlock(firstNode, tryCatchRegion)) {
			return false;
		}
		subBlocks.removeAll(tryRegion.getSubBlocks());

		// fix parents for tryRegion sub blocks
		for (IContainer cont : tryRegion.getSubBlocks()) {
			if (cont instanceof AbstractRegion) {
				AbstractRegion aReg = (AbstractRegion) cont;
				aReg.setParent(tryRegion);
			}
		}
		return true;
	}

	/**
	 * A following sibling catch-all site is not protected by this descriptor just because it is
	 * reachable.
	 */
	private static boolean hasFollowingUnprotectedTry(
			IRegion region, TryCatchBlockAttr tb, BlockNode dominator) {
		if (!tb.isAllHandler() || tb.getBlocks().size() != 1) {
			return false;
		}
		List<IContainer> children = region.getSubBlocks();
		int start = children.indexOf(dominator);
		int lastProtected = getLastProtectedContainerIndex(children, tb);
		if (start < 0 || lastProtected < start) {
			return false;
		}
		for (int index = lastProtected + 1; index < children.size(); index++) {
			IContainer child = children.get(index);
			if (child instanceof TryCatchRegion && !containsProtectedBlock(child, tb)) {
				return true;
			}
			if (child instanceof BlockNode) {
				TryCatchBlockAttr followingTry = ((BlockNode) child).get(AType.TRY_BLOCK);
				if (followingTry != null && followingTry.isAllHandler()
						&& followingTry.getBlocks().size() == 1
						&& !isTryNestedIn(followingTry, tb)) {
					return true;
				}
			}
		}
		return false;
	}

	/** Keep an unprotected throwing continuation outside a nested single-site catch. */
	private static boolean hasSingleProtectedNestedTryTail(
			IRegion region, TryCatchBlockAttr tb, BlockNode dominator) {
		if (!isSimpleNestedSingleSiteCatch(tb)) {
			return false;
		}
		List<IContainer> children = region.getSubBlocks();
		int start = children.indexOf(dominator);
		int lastProtected = getLastProtectedContainerIndex(children, tb);
		if (start < 0 || lastProtected < start || lastProtected == children.size() - 1) {
			return false;
		}
		for (int index = start; index < lastProtected; index++) {
			if (!hasOnlyProtectedThrowingCode(children.get(index), tb, new IdentityHashMap<>())) {
				return false;
			}
		}
		for (int index = lastProtected + 1; index < children.size(); index++) {
			if (!hasOnlyProtectedThrowingCode(children.get(index), tb, new IdentityHashMap<>())) {
				return true;
			}
		}
		return false;
	}

	public static boolean isResourceSuppressionTry(TryCatchBlockAttr tb) {
		for (ExceptionHandler handler : tb.getHandlers()) {
			for (BlockNode block : handler.getBlocks()) {
				for (var insn : block.getInstructions()) {
					Boolean found = insn.visitInsns(innerInsn -> {
						if (innerInsn.getType() != InsnType.INVOKE) {
							return null;
						}
						InvokeNode invoke = (InvokeNode) innerInsn;
						return invoke.getCallMth().getName().equals("addSuppressed")
								&& invoke.getCallMth().getDeclClass().getFullName().equals("java.lang.Throwable")
										? Boolean.TRUE
										: null;
					});
					if (found != null) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public static boolean containsResourceSuppressionTry(TryCatchBlockAttr tb) {
		if (isResourceSuppressionTry(tb)) {
			return true;
		}
		for (TryCatchBlockAttr innerTryBlock : tb.getInnerTryBlocks()) {
			if (containsResourceSuppressionTry(innerTryBlock)) {
				return true;
			}
		}
		return false;
	}

	private static boolean hasFinallyControlOverride(TryCatchBlockAttr tb) {
		for (ExceptionHandler handler : tb.getHandlers()) {
			if (!handler.isCatchAll() || handler.isFinally()) {
				continue;
			}
			int throwCount = 0;
			boolean returnFound = false;
			for (BlockNode block : handler.getBlocks()) {
				for (var insn : block.getInstructions()) {
					if (insn.getType() == InsnType.RETURN) {
						returnFound = true;
					}
					if (insn.getType() == InsnType.THROW) {
						throwCount++;
					}
				}
			}
			if (throwCount > 1 || (returnFound && throwCount != 0)) {
				return true;
			}
		}
		return false;
	}

	private static boolean hasFinallyTailBoundary(TryCatchBlockAttr tb) {
		if (tb.getHandlers().stream().noneMatch(ExceptionHandler::isFinally)) {
			return false;
		}
		for (BlockNode block : tb.getBlocks()) {
			var lastInsn = BlockUtils.getLastInsn(block);
			if (lastInsn == null
					|| !lastInsn.contains(AFlag.TRY_LEAVE)
					|| !lastInsn.contains(AFlag.DONT_GENERATE)) {
				continue;
			}
			if (block.getSuccessors().stream().anyMatch(successor -> successor.contains(AFlag.SYNTHETIC))
					|| hasTerminalReturnContinuation(block)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * A duplicated finally copy can be followed directly by a shared conditional return tail. It is
	 * safe to keep that tail outside the generated try only when the continuation cannot cycle back
	 * to the copy and every reachable terminal block returns from the method.
	 */
	private static boolean hasTerminalReturnContinuation(BlockNode finallyCopyBlock) {
		List<BlockNode> successors = finallyCopyBlock.getCleanSuccessors();
		if (successors.isEmpty()
				|| successors.stream().anyMatch(successor -> BlockUtils.isPathExists(successor, finallyCopyBlock))) {
			return false;
		}
		boolean returnFound = false;
		List<BlockNode> pending = new ArrayList<>(successors);
		Set<BlockNode> visited = new HashSet<>();
		for (int pos = 0; pos < pending.size(); pos++) {
			BlockNode block = pending.get(pos);
			if (visited.contains(block)) {
				continue;
			}
			visited.add(block);
			InsnNode lastInsn = BlockUtils.getLastInsn(block);
			if (lastInsn != null && lastInsn.getType() == InsnType.RETURN) {
				returnFound = true;
				continue;
			}
			List<BlockNode> next = block.getCleanSuccessors();
			if (next.isEmpty()) {
				return false;
			}
			pending.addAll(next);
		}
		return returnFound;
	}

	/**
	 * Region making can group the end of a protected range and its normal continuation into one
	 * sequential container. Split only such plain sequential regions; branch semantics remain
	 * untouched and the continuation can then stay outside the generated try statement.
	 */
	static void splitTrailingUnprotectedRegions(IRegion parent, TryCatchBlockAttr tb) {
		Set<IRegion> active = Collections.newSetFromMap(new IdentityHashMap<>());
		splitTrailingUnprotectedRegions(parent, tb, active);
	}

	private static void splitTrailingUnprotectedRegions(
			IRegion parent, TryCatchBlockAttr tb, Set<IRegion> active) {
		if (!active.add(parent)) {
			return;
		}
		try {
			List<IContainer> children = new ArrayList<>(parent.getSubBlocks());
			for (IContainer child : children) {
				if (child instanceof IRegion) {
					splitTrailingUnprotectedRegions((IRegion) child, tb, active);
				}
				if (parent instanceof Region && child instanceof IfRegion
						&& splitTerminalUnprotectedBranch((Region) parent, (IfRegion) child, tb)) {
					continue;
				}
				if (!(parent instanceof Region) || !(child instanceof Region)) {
					continue;
				}
				Region childRegion = (Region) child;
				List<IContainer> childBlocks = childRegion.getSubBlocks();
				int lastProtectedIndex = getLastProtectedContainerIndex(childBlocks, tb);
				if (lastProtectedIndex == -1 || lastProtectedIndex == childBlocks.size() - 1) {
					continue;
				}
				List<IContainer> trailingView = childBlocks.subList(lastProtectedIndex + 1, childBlocks.size());
				if (!isResourceSuppressionTry(tb)
						&& !hasFinallyTailBoundary(tb)
						&& !hasMixedHandlerExits(tb)
						&& !containsExplicitThrow(trailingView)) {
					continue;
				}
				Region trailingRegion = new Region(parent);
				List<IContainer> trailingBlocks = new ArrayList<>(trailingView);
				childBlocks.subList(lastProtectedIndex + 1, childBlocks.size()).clear();
				for (IContainer trailingBlock : trailingBlocks) {
					trailingRegion.add(trailingBlock);
				}
				List<IContainer> parentBlocks = parent.getSubBlocks();
				int childIndex = parentBlocks.indexOf(child);
				parentBlocks.add(childIndex + 1, trailingRegion);
			}
		} finally {
			active.remove(parent);
		}
	}

	private static boolean splitTerminalUnprotectedBranch(
			Region parent, IfRegion ifRegion, TryCatchBlockAttr tb) {
		IContainer thenRegion = ifRegion.getThenRegion();
		IContainer elseRegion = ifRegion.getElseRegion();
		if (thenRegion == null || elseRegion == null) {
			return false;
		}
		boolean thenProtected = containsProtectedBlock(thenRegion, tb);
		boolean elseProtected = containsProtectedBlock(elseRegion, tb);
		if (thenProtected && elseProtected) {
			return splitMixedProtectedBranch(parent, ifRegion, tb);
		}
		if (!thenProtected && !elseProtected) {
			return false;
		}
		IContainer protectedRegion = thenProtected ? thenRegion : elseRegion;
		if (!RegionUtils.hasExitBlock(protectedRegion)) {
			return false;
		}
		IContainer continuation = thenProtected ? elseRegion : thenRegion;
		boolean resourceSuppressionTry = isResourceSuppressionTry(tb);
		boolean continuationThrows = containsExplicitThrow(List.of(continuation));
		boolean terminalContinuation = hasMixedHandlerExits(tb) && RegionUtils.hasExitBlock(continuation);
		if (resourceSuppressionTry ? continuationThrows : !continuationThrows && !terminalContinuation) {
			// A TWR body throw can sit outside a compiler-split protected range while still
			// requiring the generated cleanup path. Moving it after the catch skips cleanup.
			return false;
		}
		if (!thenProtected) {
			ifRegion.invert();
		}
		ifRegion.setElseRegion(null);
		List<IContainer> parentBlocks = parent.getSubBlocks();
		int ifIndex = parentBlocks.indexOf(ifRegion);
		parentBlocks.add(ifIndex + 1, continuation);
		if (continuation instanceof IRegion) {
			((IRegion) continuation).setParent(parent);
		}
		return true;
	}

	/**
	 * A compiler can append a normal continuation to one protected branch while the other protected
	 * branch exits. Move that unprotected suffix after the condition so it is not accidentally
	 * widened into a surrounding catch-all range.
	 */
	private static boolean splitMixedProtectedBranch(
			Region parent, IfRegion ifRegion, TryCatchBlockAttr tb) {
		IContainer thenRegion = ifRegion.getThenRegion();
		IContainer elseRegion = ifRegion.getElseRegion();
		int thenSplitIndex = getTrailingUnprotectedSplitIndex(thenRegion, tb);
		int elseSplitIndex = getTrailingUnprotectedSplitIndex(elseRegion, tb);
		if ((thenSplitIndex == -1) == (elseSplitIndex == -1)) {
			return false;
		}
		IContainer sibling = thenSplitIndex == -1 ? thenRegion : elseRegion;
		if (!RegionUtils.hasExitBlock(sibling) && !containsAbruptExit(List.of(sibling))) {
			return false;
		}
		IContainer mixedRegion = thenSplitIndex != -1 ? thenRegion : elseRegion;
		int splitIndex = thenSplitIndex != -1 ? thenSplitIndex : elseSplitIndex;
		List<IContainer> mixedBlocks = ((Region) mixedRegion).getSubBlocks();
		List<IContainer> trailingBlocks = mixedBlocks.subList(splitIndex, mixedBlocks.size());
		if (!containsExplicitThrow(trailingBlocks)
				&& !(hasMixedHandlerExits(tb) && RegionUtils.hasExitBlock(mixedRegion))
				&& !(isResourceSuppressionTry(tb) && containsAbruptExit(trailingBlocks))
				&& !(hasFinallyTailBoundary(tb)
						&& !isInsideLoop(parent)
						&& containsAbruptExit(trailingBlocks))) {
			return false;
		}
		IContainer continuation = extractTrailingRegion((Region) mixedRegion, splitIndex);
		List<IContainer> parentBlocks = parent.getSubBlocks();
		int ifIndex = parentBlocks.indexOf(ifRegion);
		parentBlocks.add(ifIndex + 1, continuation);
		((IRegion) continuation).setParent(parent);
		return true;
	}

	private static boolean isInsideLoop(IRegion region) {
		for (IRegion current = region; current != null; current = current.getParent()) {
			if (current instanceof LoopRegion) {
				return true;
			}
		}
		return false;
	}

	private static boolean containsExplicitThrow(List<IContainer> containers) {
		for (IContainer container : containers) {
			if (container instanceof BlockNode) {
				boolean hasThrow = ((BlockNode) container).getInstructions().stream()
						.anyMatch(insn -> insn.getType() == InsnType.THROW);
				if (hasThrow) {
					return true;
				}
			}
			if (container instanceof IRegion
					&& containsExplicitThrow(((IRegion) container).getSubBlocks())) {
				return true;
			}
		}
		return false;
	}

	private static boolean containsAbruptExit(List<IContainer> containers) {
		for (IContainer container : containers) {
			if (container instanceof IBlock) {
				boolean hasAbruptExit = ((IBlock) container).getInstructions().stream()
						.anyMatch(insn -> insn.getType() == InsnType.RETURN || insn.getType() == InsnType.THROW);
				if (hasAbruptExit) {
					return true;
				}
			}
			if (container instanceof IRegion
					&& containsAbruptExit(((IRegion) container).getSubBlocks())) {
				return true;
			}
		}
		return false;
	}

	private static int getTrailingUnprotectedSplitIndex(IContainer container, TryCatchBlockAttr tb) {
		if (!(container instanceof Region)) {
			return -1;
		}
		List<IContainer> blocks = ((Region) container).getSubBlocks();
		int lastProtectedIndex = getLastProtectedContainerIndex(blocks, tb);
		if (lastProtectedIndex == -1 || lastProtectedIndex == blocks.size() - 1) {
			return -1;
		}
		return lastProtectedIndex + 1;
	}

	private static Region extractTrailingRegion(Region region, int splitIndex) {
		List<IContainer> blocks = region.getSubBlocks();
		Region trailingRegion = new Region(region.getParent());
		List<IContainer> trailingBlocks = new ArrayList<>(blocks.subList(splitIndex, blocks.size()));
		blocks.subList(splitIndex, blocks.size()).clear();
		for (IContainer trailingBlock : trailingBlocks) {
			trailingRegion.add(trailingBlock);
		}
		return trailingRegion;
	}

	static boolean containsProtectedBlock(IContainer container, TryCatchBlockAttr tb) {
		Set<IContainer> visited = Collections.newSetFromMap(new IdentityHashMap<>());
		ArrayDeque<IContainer> pending = new ArrayDeque<>();
		pending.add(container);
		while (!pending.isEmpty()) {
			IContainer current = pending.removeLast();
			if (!visited.add(current)) {
				continue;
			}
			if (current instanceof IBlock) {
				IBlock block = (IBlock) current;
				if (block instanceof BlockNode && containsTryBlock(tb, (BlockNode) block)) {
					return true;
				}
				for (var insn : block.getInstructions()) {
					Boolean protectedInsn = insn.visitInsns(innerInsn -> {
						CatchAttr catchAttr = innerInsn.get(AType.EXC_CATCH);
						if (catchAttr == null) {
							return null;
						}
						return catchAttr.getHandlers().stream()
								.anyMatch(handler -> isTryNestedIn(handler.getTryBlock(), tb))
										? Boolean.TRUE
										: null;
					});
					if (protectedInsn != null) {
						return true;
					}
				}
			}
			if (current instanceof IRegion) {
				pending.addAll(((IRegion) current).getSubBlocks());
			}
		}
		return false;
	}

	private static boolean containsTryBlock(TryCatchBlockAttr tb, BlockNode block) {
		if (tb.getBlocks().contains(block)) {
			return true;
		}
		for (TryCatchBlockAttr inner : tb.getInnerTryBlocks()) {
			if (containsTryBlock(inner, block)) {
				return true;
			}
		}
		return false;
	}

	private static boolean isTryNestedIn(TryCatchBlockAttr candidate, TryCatchBlockAttr expectedOuter) {
		for (TryCatchBlockAttr current = candidate; current != null; current = current.getOuterTryBlock()) {
			if (current == expectedOuter) {
				return true;
			}
		}
		return false;
	}

	private static int getLastProtectedContainerIndex(List<IContainer> containers, TryCatchBlockAttr tb) {
		for (int index = containers.size() - 1; index >= 0; index--) {
			IContainer container = containers.get(index);
			if (containsProtectedBlock(container, tb)) {
				return index;
			}
		}
		return -1;
	}

	private static boolean isHandlerPath(TryCatchBlockAttr tb, IContainer container) {
		for (ExceptionHandler h : tb.getHandlers()) {
			BlockNode handlerBlock = h.getHandlerBlock();
			if (handlerBlock != null
					&& !handlerBlock.contains(AFlag.REMOVE)
					&& (tb.isNarrowRegion() || RegionUtils.getFirstBlockNode(container) == null
							? RegionUtils.hasPathThroughBlock(handlerBlock, container)
							: RegionUtils.isPathExists(handlerBlock, container))) {
				return true;
			}
		}
		return false;
	}
}
