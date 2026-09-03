package jadx.core.dex.visitors.regions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jadx.core.dex.attributes.AFlag;
import jadx.core.dex.attributes.AType;
import jadx.core.dex.info.ClassInfo;
import jadx.core.dex.instructions.InsnType;
import jadx.core.dex.instructions.InvokeNode;
import jadx.core.dex.nodes.BlockNode;
import jadx.core.dex.nodes.IBlock;
import jadx.core.dex.nodes.IBranchRegion;
import jadx.core.dex.nodes.IContainer;
import jadx.core.dex.nodes.IRegion;
import jadx.core.dex.nodes.MethodNode;
import jadx.core.dex.regions.AbstractRegion;
import jadx.core.dex.regions.Region;
import jadx.core.dex.regions.TryCatchRegion;
import jadx.core.dex.regions.conditions.IfRegion;
import jadx.core.dex.regions.loops.LoopRegion;
import jadx.core.dex.trycatch.CatchAttr;
import jadx.core.dex.trycatch.ExceptionHandler;
import jadx.core.dex.trycatch.TryCatchBlockAttr;
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
		DepthRegionTraversal.traverseIncludingExcHandlers(mth, (regionMth, region) -> {
			boolean changed = checkAndWrap(regionMth, tryBlocks, region);
			return changed && !tryBlocks.isEmpty();
		});
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
		for (TryCatchBlockAttr outer = second.getOuterTryBlock(); outer != null; outer = outer.getOuterTryBlock()) {
			if (outer == first) {
				return true;
			}
		}
		return false;
	}

	private static boolean checkAndWrap(MethodNode mth, List<TryCatchBlockAttr> tryBlocks, IRegion region) {
		// search top splitter block in this region (don't need to go deeper)
		for (TryCatchBlockAttr tb : tryBlocks) {
			BlockNode topSplitter = tb.getTopSplitter();
			if (region.getSubBlocks().contains(topSplitter)) {
				if (!wrapBlocks(region, tb, topSplitter)) {
					mth.addWarn("Can't wrap try/catch for region: " + region);
				}
				tryBlocks.remove(tb);
				return true;
			}
			BlockNode protectedAnchor = findDirectProtectedAnchor(region, tb);
			if (protectedAnchor != null) {
				if (!wrapBlocks(region, tb, protectedAnchor)) {
					mth.addWarn("Can't wrap narrow try/catch for region: " + region);
				}
				tryBlocks.remove(tb);
				return true;
			}
		}
		return false;
	}

	/**
	 * A synthetic exception top-splitter can remain outside the structured branch which owns a
	 * one-block try body. In that case the exact protected block is a safe local anchor: requiring a
	 * single direct protected block prevents an arbitrary inner block from widening the try region.
	 */
	private static BlockNode findDirectProtectedAnchor(IRegion region, TryCatchBlockAttr tb) {
		BlockNode anchor = null;
		for (BlockNode block : tb.getBlocks()) {
			if (!region.getSubBlocks().contains(block)) {
				continue;
			}
			if (anchor != null) {
				return null;
			}
			anchor = block;
		}
		return anchor;
	}

	/**
	 * Extract all block dominated by 'dominator' to separate region and mark as try/catch block
	 */
	private static boolean wrapBlocks(IRegion replaceRegion, TryCatchBlockAttr tb, BlockNode dominator) {
		if (replaceRegion == null) {
			return false;
		}
		if (replaceRegion instanceof LoopRegion) {
			LoopRegion loop = (LoopRegion) replaceRegion;
			return wrapBlocks(loop.getBody(), tb, dominator);
		}
		if (replaceRegion instanceof IBranchRegion) {
			return wrapBlocks(replaceRegion.getParent(), tb, dominator);
		}

		Region tryRegion = new Region(replaceRegion);
		boolean splitProtectedTail = isResourceSuppressionTry(tb)
				|| hasFinallyTailBoundary(tb)
				|| hasFinallyControlOverride(tb);
		if (splitProtectedTail) {
			splitTrailingUnprotectedRegions(replaceRegion, tb);
		}
		List<IContainer> subBlocks = replaceRegion.getSubBlocks();
		int lastProtectedIndex = splitProtectedTail ? getLastProtectedContainerIndex(subBlocks, tb) : -1;
		// traverse the enclosing region for blocks that have a path from the dominator but don't have a
		// path from any of the exception handlers i.e. they are not before the end of the try block so
		// should be inside the try block.
		for (int index = 0; index < subBlocks.size(); index++) {
			IContainer cont = subBlocks.get(index);
			if (cont == dominator || RegionUtils.hasPathThroughBlock(dominator, cont)) {
				if (lastProtectedIndex != -1 && index > lastProtectedIndex) {
					break;
				}
				boolean containsTryBlock = containsProtectedBlock(cont, tb);
				if (!containsTryBlock && cont != dominator && isHandlerPath(tb, cont)) {
					// this block/region has a path from an exception handler so is after the end of the try block
					continue;
				}
				tryRegion.add(cont);
			}
		}
		if (tryRegion.getSubBlocks().isEmpty()) {
			return false;
		}

		TryCatchRegion tryCatchRegion = new TryCatchRegion(replaceRegion, tryRegion);
		tryRegion.setParent(tryCatchRegion);
		tryCatchRegion.setTryCatchBlock(tb);

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
			if (block.getSuccessors().stream().anyMatch(successor -> successor.contains(AFlag.SYNTHETIC))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Region making can group the end of a protected range and its normal continuation into one
	 * sequential container. Split only such plain sequential regions; branch semantics remain
	 * untouched and the continuation can then stay outside the generated try statement.
	 */
	private static void splitTrailingUnprotectedRegions(IRegion parent, TryCatchBlockAttr tb) {
		List<IContainer> children = new ArrayList<>(parent.getSubBlocks());
		for (IContainer child : children) {
			if (child instanceof IRegion) {
				splitTrailingUnprotectedRegions((IRegion) child, tb);
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
			if (isResourceSuppressionTry(tb)) {
				return false;
			}
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
		if (!isResourceSuppressionTry(tb)
				&& !containsExplicitThrow(List.of(continuation))) {
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
		if (!containsExplicitThrow(trailingBlocks)) {
			return false;
		}
		IContainer continuation = extractTrailingRegion((Region) mixedRegion, splitIndex);
		List<IContainer> parentBlocks = parent.getSubBlocks();
		int ifIndex = parentBlocks.indexOf(ifRegion);
		parentBlocks.add(ifIndex + 1, continuation);
		((IRegion) continuation).setParent(parent);
		return true;
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

	private static boolean containsProtectedBlock(IContainer container, TryCatchBlockAttr tb) {
		if (container instanceof IBlock) {
			IBlock block = (IBlock) container;
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
		if (container instanceof IRegion) {
			for (IContainer child : ((IRegion) container).getSubBlocks()) {
				if (containsProtectedBlock(child, tb)) {
					return true;
				}
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
