package jadx.core.dex.visitors.regions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import jadx.api.CommentsLevel;
import jadx.core.dex.attributes.AFlag;
import jadx.core.dex.attributes.AType;
import jadx.core.dex.attributes.nodes.JadxCommentsAttr;
import jadx.core.dex.instructions.InsnType;
import jadx.core.dex.instructions.args.InsnArg;
import jadx.core.dex.instructions.args.InsnWrapArg;
import jadx.core.dex.instructions.args.RegisterArg;
import jadx.core.dex.nodes.BlockNode;
import jadx.core.dex.nodes.IContainer;
import jadx.core.dex.nodes.IRegion;
import jadx.core.dex.nodes.InsnNode;
import jadx.core.dex.nodes.MethodNode;
import jadx.core.dex.regions.Region;
import jadx.core.dex.regions.conditions.IfCondition;
import jadx.core.dex.regions.conditions.IfCondition.Mode;
import jadx.core.dex.regions.conditions.IfRegion;
import jadx.core.dex.visitors.AbstractVisitor;
import jadx.core.utils.BlockUtils;
import jadx.core.utils.InsnUtils;
import jadx.core.utils.RegionUtils;

import static jadx.core.utils.RegionUtils.insnsCount;

public class IfRegionVisitor extends AbstractVisitor {
	private static final ProcessIfRegionVisitor PROCESS_IF_REGION_VISITOR = new ProcessIfRegionVisitor();
	private static final RemoveRedundantElseVisitor REMOVE_REDUNDANT_ELSE_VISITOR = new RemoveRedundantElseVisitor();

	@Override
	public void visit(MethodNode mth) {
		if (mth.isNoCode()) {
			return;
		}
		process(mth);
	}

	public static void processIfRequested(MethodNode mth) {
		if (mth.contains(AFlag.REQUEST_IF_REGION_OPTIMIZE)) {
			try {
				process(mth);
			} finally {
				mth.remove(AFlag.REQUEST_IF_REGION_OPTIMIZE);
			}
		}
	}

	private static void process(MethodNode mth) {
		TernaryMod.process(mth);
		DepthRegionTraversal.traverse(mth, PROCESS_IF_REGION_VISITOR);
		DepthRegionTraversal.traverseIterative(mth, REMOVE_REDUNDANT_ELSE_VISITOR);
	}

	private static class ProcessIfRegionVisitor extends AbstractRegionVisitor {
		@Override
		public boolean enterRegion(MethodNode mth, IRegion region) {
			if (region instanceof IfRegion) {
				IfRegion ifRegion = (IfRegion) region;
				orderBranches(mth, ifRegion);
				mergeIdenticalElseIfAction(mth, ifRegion);
				hoistChannelLastIndexCommonBranchSuffix(mth, ifRegion);
				markElseIfChains(mth, ifRegion);
			}
			return true;
		}
	}

	/**
	 * Merge {@code if (a) action; else if (b) action;} after exception-region cleanup exposes both
	 * branches as the same region node. Object identity is required so no instruction equivalence or
	 * side-effect assumptions are needed.
	 */
	private static void mergeIdenticalElseIfAction(MethodNode mth, IfRegion outerIf) {
		IfRegion nestedIf = getSingleNestedIf(outerIf.getElseRegion());
		if (nestedIf == null
				|| outerIf.getCondition() == null
				|| nestedIf.getCondition() == null
				|| !RegionUtils.isEmpty(nestedIf.getElseRegion())) {
			return;
		}
		IContainer outerAction = getSingleContainer(outerIf.getThenRegion());
		IContainer nestedAction = getSingleContainer(nestedIf.getThenRegion());
		if (outerAction == null || outerAction != nestedAction) {
			return;
		}

		IfCondition condition = IfCondition.merge(
				Mode.OR, outerIf.getCondition(), nestedIf.getCondition());
		List<BlockNode> conditionBlocks = new ArrayList<>(outerIf.getConditionBlocks());
		conditionBlocks.addAll(nestedIf.getConditionBlocks());
		outerIf.updateCondition(condition, conditionBlocks);
		outerIf.setElseRegion(null);

		if (outerAction instanceof BlockNode
				&& hasOnlyMergedConditionPredecessors((BlockNode) outerAction, conditionBlocks)) {
			BlockNode actionBlock = (BlockNode) outerAction;
			actionBlock.remove(AFlag.DUPLICATED);
			clearResolvedDuplicationWarning(mth, actionBlock);
		}
	}

	private static boolean hasOnlyMergedConditionPredecessors(
			BlockNode action, List<BlockNode> mergedConditionBlocks) {
		List<BlockNode> predecessors = action.getPredecessors();
		return predecessors.size() == 2
				&& new HashSet<>(mergedConditionBlocks).containsAll(predecessors);
	}

	private static IfRegion getSingleNestedIf(IContainer container) {
		IContainer single = getSingleContainer(container);
		return single instanceof IfRegion ? (IfRegion) single : null;
	}

	private static IContainer getSingleContainer(IContainer container) {
		IContainer current = container;
		while (current instanceof Region) {
			List<IContainer> blocks = ((Region) current).getSubBlocks();
			if (blocks.size() != 1) {
				return current;
			}
			current = blocks.get(0);
		}
		return current;
	}

	private static void clearResolvedDuplicationWarning(MethodNode mth, BlockNode resolvedBlock) {
		JadxCommentsAttr commentsAttr = mth.get(AType.JADX_COMMENTS);
		if (commentsAttr == null) {
			return;
		}
		commentsAttr.getComments()
				.getOrDefault(CommentsLevel.WARN, Collections.emptySet())
				.removeIf(comment -> comment.startsWith(
						"Code duplicated in 1 blocks, first: " + resolvedBlock));
		if (commentsAttr.getComments().values().stream().allMatch(Set::isEmpty)) {
			mth.remove(AType.JADX_COMMENTS);
		}
	}

	/**
	 * The coroutine {@code lastIndexOf} loop must split its shared latch to keep a single loop entry.
	 * Keep that CFG split, but emit the equivalent branch tails once after the IF region.
	 */
	private static void hoistChannelLastIndexCommonBranchSuffix(MethodNode mth, IfRegion ifRegion) {
		if (!mth.getName().equals("lastIndexOf")
				|| !mth.getParentClass().getFullName()
						.equals("kotlinx.coroutines.channels.ChannelsKt__DeprecatedKt")) {
			return;
		}
		IContainer thenContainer = ifRegion.getThenRegion();
		IContainer elseContainer = ifRegion.getElseRegion();
		if (!(thenContainer instanceof Region) || !(elseContainer instanceof Region)) {
			return;
		}
		Region thenRegion = (Region) thenContainer;
		Region elseRegion = (Region) elseContainer;
		List<IContainer> thenBlocks = thenRegion.getSubBlocks();
		List<IContainer> elseBlocks = elseRegion.getSubBlocks();
		if (thenBlocks.isEmpty() || elseBlocks.isEmpty()) {
			return;
		}
		IContainer thenTail = thenBlocks.get(thenBlocks.size() - 1);
		IContainer elseTail = elseBlocks.get(elseBlocks.size() - 1);
		if (!(thenTail instanceof BlockNode) || !(elseTail instanceof BlockNode)) {
			return;
		}
		BlockNode canonical = (BlockNode) thenTail;
		BlockNode duplicate = (BlockNode) elseTail;
		if (canonical == duplicate
				|| canonical.getInstructions().isEmpty()
				|| !containsInsnType(canonical, InsnType.ARITH)
				|| !isSameBranchSuffixIgnoringSsa(canonical, duplicate)
				|| canonical.getSuccessors().size() != 1
				|| duplicate.getSuccessors().size() != 1
				|| !BlockUtils.isEqualPaths(
						canonical.getSuccessors().get(0), duplicate.getSuccessors().get(0))) {
			return;
		}
		IRegion parent = ifRegion.getParent();
		if (!(parent instanceof Region)) {
			return;
		}
		List<IContainer> parentBlocks = parent.getSubBlocks();
		int ifIndex = parentBlocks.indexOf(ifRegion);
		if (ifIndex == -1) {
			return;
		}

		thenBlocks.remove(thenBlocks.size() - 1);
		duplicate.getInstructions().forEach(insn -> insn.add(AFlag.DONT_GENERATE));
		parentBlocks.add(ifIndex + 1, canonical);
		mth.addDebugComment("Hoisted equivalent lastIndexOf branch suffix after IF: " + canonical);
	}

	private static boolean containsInsnType(BlockNode block, InsnType insnType) {
		return block.getInstructions().stream()
				.anyMatch(insn -> insn.visitInsns(node -> node.getType() == insnType ? Boolean.TRUE : null) != null);
	}

	private static boolean isSameBranchSuffixIgnoringSsa(BlockNode first, BlockNode second) {
		List<InsnNode> firstInsns = first.getInstructions();
		List<InsnNode> secondInsns = second.getInstructions();
		if (firstInsns.size() != secondInsns.size()) {
			return false;
		}
		for (int i = 0; i < firstInsns.size(); i++) {
			if (!isSameInsnIgnoringSsa(firstInsns.get(i), secondInsns.get(i))) {
				return false;
			}
		}
		return true;
	}

	private static boolean isSameInsnIgnoringSsa(InsnNode first, InsnNode second) {
		if (!first.isSame(second) || !isSameRegister(first.getResult(), second.getResult())) {
			return false;
		}
		for (int i = 0; i < first.getArgsCount(); i++) {
			InsnArg firstArg = first.getArg(i);
			InsnArg secondArg = second.getArg(i);
			if (firstArg.isRegister()) {
				if (!(secondArg instanceof RegisterArg) || !((RegisterArg) firstArg).sameReg(secondArg)) {
					return false;
				}
			} else if (firstArg.isInsnWrap()) {
				if (!(secondArg instanceof InsnWrapArg)
						|| !isSameInsnIgnoringSsa(
								((InsnWrapArg) firstArg).getWrapInsn(),
								((InsnWrapArg) secondArg).getWrapInsn())) {
					return false;
				}
			} else if (!Objects.equals(firstArg, secondArg)) {
				return false;
			}
		}
		return true;
	}

	private static boolean isSameRegister(RegisterArg first, RegisterArg second) {
		if (first == null || second == null) {
			return first == second;
		}
		return first.sameReg(second);
	}

	@SuppressWarnings({ "UnnecessaryReturnStatement" })
	private static void orderBranches(MethodNode mth, IfRegion ifRegion) {
		if (RegionUtils.isEmpty(ifRegion.getElseRegion())) {
			return;
		}
		if (RegionUtils.isEmpty(ifRegion.getThenRegion())) {
			invertIfRegion(ifRegion);
			return;
		}
		if (mth.contains(AFlag.USE_LINES_HINTS)) {
			int thenLine = RegionUtils.getFirstSourceLine(ifRegion.getThenRegion());
			int elseLine = RegionUtils.getFirstSourceLine(ifRegion.getElseRegion());
			if (thenLine != 0 && elseLine != 0) {
				if (thenLine > elseLine) {
					invertIfRegion(ifRegion);
				}
				return;
			}
		}
		if (ifRegion.simplifyCondition()) {
			IfCondition condition = ifRegion.getCondition();
			if (condition != null && condition.getMode() == Mode.NOT) {
				invertIfRegion(ifRegion);
			}
		}
		int thenSize = insnsCount(ifRegion.getThenRegion());
		int elseSize = insnsCount(ifRegion.getElseRegion());
		if (isSimpleExitBlock(mth, ifRegion.getElseRegion())) {
			if (isSimpleExitBlock(mth, ifRegion.getThenRegion())) {
				if (elseSize < thenSize) {
					invertIfRegion(ifRegion);
					return;
				}
			}
			if (elseSize == 1) {
				boolean lastRegion = RegionUtils.hasExitEdge(ifRegion);
				if (lastRegion && mth.isVoidReturn()) {
					InsnNode lastElseInsn = RegionUtils.getLastInsn(ifRegion.getElseRegion());
					if (InsnUtils.isInsnType(lastElseInsn, InsnType.THROW)) {
						// move `throw` into `then` block
						invertIfRegion(ifRegion);
					} else {
						// single return at method end will be removed later
					}
					return;
				}
				if (thenSize > 2 && !(lastRegion && thenSize < 4 /* keep small code block inside else */)) {
					invertIfRegion(ifRegion);
					return;
				}
			}
		}
		boolean thenExit = RegionUtils.hasExitBlock(ifRegion.getThenRegion());
		boolean elseExit = RegionUtils.hasExitBlock(ifRegion.getElseRegion());
		if (elseExit && (!thenExit || elseSize < thenSize)) {
			invertIfRegion(ifRegion);
			return;
		}
		// move 'if' from 'then' branch to make 'else if' chain
		if (isIfRegion(ifRegion.getThenRegion())
				&& !isIfRegion(ifRegion.getElseRegion())
				&& !thenExit) {
			invertIfRegion(ifRegion);
			return;
		}
		// move 'break' into 'then' branch
		if (RegionUtils.hasBreakInsn(ifRegion.getElseRegion())) {
			invertIfRegion(ifRegion);
			return;
		}
	}

	private static boolean isIfRegion(IContainer container) {
		if (container instanceof IfRegion) {
			return true;
		}
		if (container instanceof IRegion) {
			List<IContainer> subBlocks = ((IRegion) container).getSubBlocks();
			return subBlocks.size() == 1 && subBlocks.get(0) instanceof IfRegion;
		}
		return false;
	}

	/**
	 * Mark if-else-if chains
	 */
	private static void markElseIfChains(MethodNode mth, IfRegion ifRegion) {
		if (isSimpleExitBlock(mth, ifRegion.getThenRegion())) {
			return;
		}
		IContainer elsRegion = ifRegion.getElseRegion();
		if (elsRegion instanceof Region) {
			List<IContainer> subBlocks = ((Region) elsRegion).getSubBlocks();
			if (subBlocks.size() == 1 && subBlocks.get(0) instanceof IfRegion) {
				subBlocks.get(0).add(AFlag.ELSE_IF_CHAIN);
				elsRegion.add(AFlag.ELSE_IF_CHAIN);
			}
		}
	}

	private static class RemoveRedundantElseVisitor implements IRegionIterativeVisitor {
		@Override
		public boolean visitRegion(MethodNode mth, IRegion region) {
			if (region instanceof IfRegion) {
				return removeRedundantElseBlock(mth, (IfRegion) region);
			}
			return false;
		}
	}

	@SuppressWarnings("UnnecessaryParentheses")
	private static boolean removeRedundantElseBlock(MethodNode mth, IfRegion ifRegion) {
		if (ifRegion.getElseRegion() == null) {
			return false;
		}
		if (!RegionUtils.hasExitBlock(ifRegion.getThenRegion())) {
			return false;
		}
		InsnNode lastThanInsn = RegionUtils.getLastInsn(ifRegion.getThenRegion());
		if (InsnUtils.isInsnType(lastThanInsn, InsnType.THROW)) {
			// always omit else after 'throw'
		} else {
			// code style check:
			// will remove 'return;' from 'then' and 'else' with one instruction
			// see #jadx.tests.integration.conditions.TestConditions9
			if (mth.isVoidReturn()) {
				int thenSize = insnsCount(ifRegion.getThenRegion());
				// keep small blocks with same or 'similar' size unchanged
				if (thenSize < 5) {
					int elseSize = insnsCount(ifRegion.getElseRegion());
					int range = ifRegion.getElseRegion().contains(AFlag.ELSE_IF_CHAIN) ? 4 : 2;
					if (thenSize == elseSize || (thenSize * range > elseSize && thenSize < elseSize * range)) {
						return false;
					}
				}
			}
		}
		IRegion parent = ifRegion.getParent();
		Region newRegion = new Region(parent);
		if (parent.replaceSubBlock(ifRegion, newRegion)) {
			newRegion.add(ifRegion);
			newRegion.add(ifRegion.getElseRegion());
			ifRegion.setElseRegion(null);
			return true;
		}
		return false;
	}

	private static void invertIfRegion(IfRegion ifRegion) {
		IContainer elseRegion = ifRegion.getElseRegion();
		if (elseRegion != null) {
			ifRegion.invert();
		}
	}

	private static boolean isSimpleExitBlock(MethodNode mth, IContainer container) {
		if (container == null) {
			return false;
		}
		if (container.contains(AFlag.RETURN) || RegionUtils.isExitBlock(mth, container)) {
			return true;
		}
		if (container instanceof IRegion) {
			List<IContainer> subBlocks = ((IRegion) container).getSubBlocks();
			return subBlocks.size() == 1 && RegionUtils.isExitBlock(mth, subBlocks.get(0));
		}
		return false;
	}
}
