package jadx.core.dex.visitors.regions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jadx.core.dex.attributes.AFlag;
import jadx.core.dex.instructions.InsnType;
import jadx.core.dex.instructions.PhiInsn;
import jadx.core.dex.instructions.args.ArgType;
import jadx.core.dex.instructions.args.CodeVar;
import jadx.core.dex.instructions.args.InsnArg;
import jadx.core.dex.instructions.args.InsnWrapArg;
import jadx.core.dex.instructions.args.LiteralArg;
import jadx.core.dex.instructions.args.RegisterArg;
import jadx.core.dex.instructions.args.SSAVar;
import jadx.core.dex.instructions.mods.TernaryInsn;
import jadx.core.dex.nodes.BlockNode;
import jadx.core.dex.nodes.IContainer;
import jadx.core.dex.nodes.IRegion;
import jadx.core.dex.nodes.InsnNode;
import jadx.core.dex.nodes.MethodNode;
import jadx.core.dex.regions.Region;
import jadx.core.dex.regions.SwitchRegion;
import jadx.core.dex.regions.conditions.IfRegion;
import jadx.core.dex.visitors.ModVisitor;
import jadx.core.dex.visitors.ReplaceNewArray;
import jadx.core.dex.visitors.shrink.CodeShrinkVisitor;
import jadx.core.utils.BlockUtils;
import jadx.core.utils.InsnList;
import jadx.core.utils.InsnRemover;
import jadx.core.utils.RegionUtils;
import jadx.core.utils.exceptions.JadxException;

/**
 * Convert 'if' to ternary operation
 */
public class TernaryMod extends AbstractRegionVisitor implements IRegionIterativeVisitor {

	private static final TernaryMod INSTANCE = new TernaryMod();

	public static void process(MethodNode mth) {
		boolean changed = false;
		// convert all found ternary nodes in one iteration
		DepthRegionTraversal.traverse(mth, INSTANCE);
		if (mth.contains(AFlag.REQUEST_CODE_SHRINK)) {
			CodeShrinkVisitor.shrinkMethod(mth);
			changed = true;
		}
		if (changed && (mth.isConstructor()
				|| (mth.getMethodInfo().isClassInit() && mth.getParentClass().isEnum()))) {
			// aggressive mode to help code inline before super call in constructors and enum constants
			// iterative runs with shrink after each change
			DepthRegionTraversal.traverseIterative(mth, INSTANCE);
		}
	}

	@Override
	public boolean enterRegion(MethodNode mth, IRegion region) {
		if (processRegion(mth, region)) {
			mth.add(AFlag.REQUEST_CODE_SHRINK);
		}
		return true;
	}

	@Override
	public boolean visitRegion(MethodNode mth, IRegion region) {
		if (processRegion(mth, region)) {
			CodeShrinkVisitor.shrinkMethod(mth);
			return true;
		}
		return false;
	}

	private static boolean processRegion(MethodNode mth, IRegion region) {
		if (region instanceof IfRegion) {
			return makeTernaryInsn(mth, (IfRegion) region);
		}
		return false;
	}

	private static boolean makeTernaryInsn(MethodNode mth, IfRegion ifRegion) {
		if (isStringSwitchCase(ifRegion)) {
			return false;
		}
		boolean enumClassInit = mth.getMethodInfo().isClassInit() && mth.getParentClass().isEnum();
		if (ifRegion.contains(AFlag.ELSE_IF_CHAIN) && !enumClassInit) {
			return false;
		}
		IContainer thenRegion = ifRegion.getThenRegion();
		IContainer elseRegion = ifRegion.getElseRegion();
		if (thenRegion == null) {
			return false;
		}
		if (elseRegion == null) {
			return processOneBranchTernary(mth, ifRegion);
		}
		if (enumClassInit) {
			BlockNode rawThenBlock = getSingleBlock(thenRegion);
			BlockNode rawElseBlock = getSingleBlock(elseRegion);
			if (rawThenBlock != null && rawElseBlock != null) {
				boolean hoisted = hoistEqualBranchLiteralAssignments(mth, ifRegion, rawThenBlock, rawElseBlock);
				if (hoisted) {
					try {
						new ReplaceNewArray().visit(mth);
					} catch (JadxException e) {
						mth.addDebugComment("Failed to retry filled-array conversion after enum branch hoist: " + e);
					}
				}
			}
		}
		BlockNode tb = getTernaryInsnBlock(mth, thenRegion);
		BlockNode eb = getTernaryInsnBlock(mth, elseRegion);
		if (tb == null || eb == null) {
			return false;
		}
		if (tb.contains(AFlag.DUPLICATED) || eb.contains(AFlag.DUPLICATED)) {
			return false;
		}
		List<BlockNode> conditionBlocks = ifRegion.getConditionBlocks();
		if (conditionBlocks.isEmpty()) {
			return false;
		}

		BlockNode header = conditionBlocks.get(0);
		InsnNode thenInsn = tb.getInstructions().get(0);
		InsnNode elseInsn = eb.getInstructions().get(0);

		if (!enumClassInit && !verifyLineHints(mth, thenInsn, elseInsn)) {
			return false;
		}

		RegisterArg thenResArg = thenInsn.getResult();
		RegisterArg elseResArg = elseInsn.getResult();
		if (thenResArg != null && elseResArg != null) {
			PhiInsn thenPhi = thenResArg.getSVar().getOnlyOneUseInPhi();
			PhiInsn elsePhi = elseResArg.getSVar().getOnlyOneUseInPhi();
			if (thenPhi == null
					|| thenPhi != elsePhi
					|| thenResArg.getSVar().getUseCount() != 1
					|| elseResArg.getSVar().getUseCount() != 1) {
				return false;
			}
			if (!ifRegion.getParent().replaceSubBlock(ifRegion, header)) {
				return false;
			}
			InsnList.remove(tb, thenInsn);
			InsnList.remove(eb, elseInsn);

			RegisterArg resArg;
			if (thenPhi.getArgsCount() == 2) {
				resArg = thenPhi.getResult();
			} else {
				resArg = thenResArg;
				thenPhi.removeArg(elseResArg);
			}
			InsnArg thenArg = InsnArg.wrapInsnIntoArg(thenInsn.copyWithoutResult());
			InsnArg elseArg = InsnArg.wrapInsnIntoArg(elseInsn.copyWithoutResult());
			TernaryInsn ternInsn = new TernaryInsn(ifRegion.getCondition(), resArg.duplicate(), thenArg, elseArg);
			normalizeDynamicBooleanNumericBranches(ternInsn);
			int branchLine = Math.max(thenInsn.getSourceLine(), elseInsn.getSourceLine());
			ternInsn.setSourceLine(Math.max(ifRegion.getSourceLine(), branchLine));

			InsnRemover.unbindInsn(mth, thenInsn);
			InsnRemover.unbindInsn(mth, elseInsn);
			ternInsn.rebindArgs();
			if (thenPhi.getArgsCount() == 0) {
				InsnRemover.unbindResult(mth, thenPhi);
				InsnRemover.delistPhi(mth, thenPhi);
			}

			// remove 'if' instruction
			replaceConditionHeaderInsn(header, ternInsn);

			clearConditionBlocks(conditionBlocks, header);
			return true;
		}

		if (!mth.isVoidReturn()
				&& thenInsn.getType() == InsnType.RETURN
				&& elseInsn.getType() == InsnType.RETURN) {
			InsnArg thenArg = thenInsn.getArg(0);
			InsnArg elseArg = elseInsn.getArg(0);
			if (thenArg.isLiteral() != elseArg.isLiteral()) {
				// one arg is literal
				return false;
			}

			if (!ifRegion.getParent().replaceSubBlock(ifRegion, header)) {
				return false;
			}
			InsnList.remove(tb, thenInsn);
			InsnList.remove(eb, elseInsn);
			tb.remove(AFlag.RETURN);
			eb.remove(AFlag.RETURN);

			TernaryInsn ternInsn = new TernaryInsn(ifRegion.getCondition(), null, thenArg, elseArg);
			normalizeDynamicBooleanNumericBranches(ternInsn);
			InsnNode retInsn = new InsnNode(InsnType.RETURN, 1);
			InsnArg arg = InsnArg.wrapInsnIntoArg(ternInsn);
			arg.setType(thenArg.getType());
			retInsn.addArg(arg);

			header.getInstructions().clear();
			retInsn.rebindArgs();
			header.getInstructions().add(retInsn);
			header.add(AFlag.RETURN);

			clearConditionBlocks(conditionBlocks, header);
			return true;
		}
		return false;
	}

	private static boolean isStringSwitchCase(IfRegion ifRegion) {
		IRegion parent = ifRegion.getParent();
		while (parent != null) {
			if (parent instanceof SwitchRegion) {
				InsnNode switchInsn = BlockUtils.getLastInsnWithType(((SwitchRegion) parent).getHeader(), InsnType.SWITCH);
				if (switchInsn != null
						&& SwitchOverStringVisitor.getStrHashcodeInvokeInsn(switchInsn.getArg(0)) != null) {
					return true;
				}
			}
			parent = parent.getParent();
		}
		return false;
	}

	private static boolean verifyLineHints(MethodNode mth, InsnNode thenInsn, InsnNode elseInsn) {
		if (mth.contains(AFlag.USE_LINES_HINTS)
				&& thenInsn.getSourceLine() != elseInsn.getSourceLine()) {
			if (thenInsn.getSourceLine() != 0 && elseInsn.getSourceLine() != 0) {
				// sometimes source lines incorrect
				return checkLineStats(thenInsn, elseInsn);
			}
			// don't make nested ternary by default
			// TODO: add addition checks
			return !containsTernary(thenInsn) && !containsTernary(elseInsn);
		}
		return true;
	}

	private static void clearConditionBlocks(List<BlockNode> conditionBlocks, BlockNode header) {
		for (BlockNode block : conditionBlocks) {
			if (block != header) {
				block.getInstructions().clear();
				block.add(AFlag.REMOVE);
			}
		}
	}

	private static BlockNode getTernaryInsnBlock(MethodNode mth, IContainer thenRegion) {
		BlockNode block = getSingleBlock(thenRegion);
		if (block != null) {
			inlineBranchPureArgs(mth, block);
			if (block.getInstructions().size() == 1) {
				return block;
			}
		}
		return null;
	}

	private static BlockNode getSingleBlock(IContainer region) {
		if (region instanceof Region) {
			Region r = (Region) region;
			if (r.getSubBlocks().size() == 1) {
				IContainer container = r.getSubBlocks().get(0);
				if (container instanceof BlockNode) {
					return (BlockNode) container;
				}
			}
		}
		return null;
	}

	private static boolean hoistEqualBranchLiteralAssignments(
			MethodNode mth, IfRegion ifRegion, BlockNode thenBlock, BlockNode elseBlock) {
		boolean changed = false;
		int index = 0;
		while (index < thenBlock.getInstructions().size() - 1
				&& index < elseBlock.getInstructions().size() - 1) {
			InsnNode thenInsn = thenBlock.getInstructions().get(index);
			InsnNode elseInsn = elseBlock.getInstructions().get(index);
			if (hoistEqualBranchLiteralAssignment(mth, ifRegion, thenBlock, elseBlock, thenInsn, elseInsn)) {
				changed = true;
			} else {
				index++;
			}
		}
		return changed;
	}

	private static boolean hoistEqualBranchLiteralAssignment(
			MethodNode mth, IfRegion ifRegion, BlockNode thenBlock, BlockNode elseBlock,
			InsnNode thenInsn, InsnNode elseInsn) {
		InsnType assignType = thenInsn.getType();
		if (assignType != elseInsn.getType()
				|| (assignType != InsnType.CONST && assignType != InsnType.MOVE)
				|| thenInsn.getArgsCount() != 1
				|| elseInsn.getArgsCount() != 1
				|| !(thenInsn.getArg(0) instanceof LiteralArg)
				|| !(elseInsn.getArg(0) instanceof LiteralArg)) {
			return false;
		}
		LiteralArg thenLiteral = (LiteralArg) thenInsn.getArg(0);
		LiteralArg elseLiteral = (LiteralArg) elseInsn.getArg(0);
		if (thenLiteral.getLiteral() != elseLiteral.getLiteral()
				|| !thenLiteral.getType().equals(elseLiteral.getType())) {
			return false;
		}
		RegisterArg thenResult = thenInsn.getResult();
		RegisterArg elseResult = elseInsn.getResult();
		if (thenResult == null || elseResult == null
				|| thenResult.getSVar().getUseCount() != 1
				|| elseResult.getSVar().getUseCount() != 1) {
			return false;
		}
		PhiInsn thenPhi = thenResult.getSVar().getOnlyOneUseInPhi();
		PhiInsn elsePhi = elseResult.getSVar().getOnlyOneUseInPhi();
		if (thenPhi == null || thenPhi != elsePhi || thenPhi.getArgsCount() != 2 || thenPhi.getResult() == null) {
			return false;
		}

		InsnNode mergedConst = thenInsn.copyWithoutResult();
		mergedConst.setResult(thenPhi.getResult().duplicate());
		mergedConst.setSourceLine(Math.max(thenInsn.getSourceLine(), elseInsn.getSourceLine()));
		mergedConst.add(AFlag.SYNTHETIC);

		InsnRemover.unbindAllArgs(mth, thenPhi);
		InsnRemover.delistPhi(mth, thenPhi);
		InsnRemover.unbindResult(mth, thenPhi);
		removeBranchLiteralAssignment(mth, thenBlock, thenInsn, thenResult.getSVar());
		removeBranchLiteralAssignment(mth, elseBlock, elseInsn, elseResult.getSVar());

		BlockNode header = ifRegion.getConditionBlocks().get(0);
		List<InsnNode> headerInsns = header.getInstructions();
		int insertIndex = headerInsns.size();
		for (int i = 0; i < headerInsns.size(); i++) {
			if (headerInsns.get(i).getType() == InsnType.IF) {
				insertIndex = i;
				break;
			}
		}
		headerInsns.add(insertIndex, mergedConst);
		return true;
	}

	private static void removeBranchLiteralAssignment(MethodNode mth, BlockNode block, InsnNode insn, SSAVar ssaVar) {
		InsnRemover.unbindInsn(null, insn);
		InsnList.remove(block, insn);
		mth.removeSVar(ssaVar);
	}

	private static void replaceConditionHeaderInsn(BlockNode header, InsnNode replacement) {
		List<InsnNode> headerInsns = header.getInstructions();
		List<InsnNode> hoistedAssignments = new ArrayList<>();
		for (InsnNode insn : headerInsns) {
			if ((insn.getType() == InsnType.CONST || insn.getType() == InsnType.MOVE)
					&& insn.contains(AFlag.SYNTHETIC)
					&& insn.getArgsCount() == 1
					&& insn.getArg(0).isLiteral()) {
				hoistedAssignments.add(insn);
			}
		}
		headerInsns.clear();
		headerInsns.addAll(hoistedAssignments);
		headerInsns.add(replacement);
	}

	private static void inlineBranchPureArgs(MethodNode mth, BlockNode block) {
		List<InsnNode> insns = block.getInstructions();
		if (insns.size() < 2) {
			return;
		}
		InsnNode finalInsn = insns.get(insns.size() - 1);
		List<InsnNode> pureAssigns = new ArrayList<>(insns.subList(0, insns.size() - 1));
		Map<SSAVar, InsnNode> assignBySsaVar = new HashMap<>(pureAssigns.size());
		for (InsnNode assignInsn : pureAssigns) {
			RegisterArg result = assignInsn.getResult();
			if (result == null || result.getSVar() == null || !assignInsn.canReorder()) {
				return;
			}
			assignBySsaVar.put(result.getSVar(), assignInsn);
		}
		for (int i = 0; i < pureAssigns.size(); i++) {
			InsnNode assignInsn = pureAssigns.get(i);
			RegisterArg result = assignInsn.getResult();
			for (RegisterArg use : result.getSVar().getUseList()) {
				if (isInsnArgUse(finalInsn, use)) {
					continue;
				}
				boolean usedInLaterAssign = false;
				for (int j = i + 1; j < pureAssigns.size(); j++) {
					if (isInsnArgUse(pureAssigns.get(j), use)) {
						usedInLaterAssign = true;
						break;
					}
				}
				if (!usedInLaterAssign) {
					return;
				}
			}
		}
		List<RegisterArg> rootUses = new ArrayList<>();
		finalInsn.visitArgs(arg -> {
			if (arg.isRegister() && assignBySsaVar.containsKey(((RegisterArg) arg).getSVar())) {
				rootUses.add((RegisterArg) arg);
			}
		});
		if (rootUses.isEmpty()) {
			return;
		}
		for (RegisterArg rootUse : rootUses) {
			InsnNode assignInsn = assignBySsaVar.get(rootUse.getSVar());
			finalInsn.replaceArg(rootUse, buildPureArg(assignInsn, assignBySsaVar));
		}
		InsnRemover.removeAllAndUnbind(mth, block, pureAssigns);
	}

	private static InsnArg buildPureArg(InsnNode assignInsn, Map<SSAVar, InsnNode> assignBySsaVar) {
		InsnNode copy = assignInsn.copyWithoutResult();
		List<RegisterArg> dependencyUses = new ArrayList<>();
		copy.visitArgs(arg -> {
			if (arg.isRegister() && assignBySsaVar.containsKey(((RegisterArg) arg).getSVar())) {
				dependencyUses.add((RegisterArg) arg);
			}
		});
		for (RegisterArg dependencyUse : dependencyUses) {
			InsnNode dependencyAssign = assignBySsaVar.get(dependencyUse.getSVar());
			copy.replaceArg(dependencyUse, buildPureArg(dependencyAssign, assignBySsaVar));
		}
		return InsnArg.wrapInsnIntoArg(copy);
	}

	private static boolean isInsnArgUse(InsnNode insn, RegisterArg use) {
		return insn.visitArgs(arg -> arg == use ? Boolean.TRUE : null) != null;
	}

	private static boolean containsTernary(InsnNode insn) {
		if (insn.getType() == InsnType.TERNARY) {
			return true;
		}
		for (int i = 0; i < insn.getArgsCount(); i++) {
			InsnArg arg = insn.getArg(i);
			if (arg.isInsnWrap()) {
				InsnNode wrapInsn = ((InsnWrapArg) arg).getWrapInsn();
				if (containsTernary(wrapInsn)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Return 'true' if there are several args with same source lines
	 */
	private static boolean checkLineStats(InsnNode t, InsnNode e) {
		if (t.getResult() == null || e.getResult() == null) {
			return false;
		}
		PhiInsn tPhi = t.getResult().getSVar().getOnlyOneUseInPhi();
		PhiInsn ePhi = e.getResult().getSVar().getOnlyOneUseInPhi();
		if (ePhi == null || tPhi != ePhi) {
			return false;
		}
		Map<Integer, Integer> map = new HashMap<>(tPhi.getArgsCount());
		for (InsnArg arg : tPhi.getArguments()) {
			if (!arg.isRegister()) {
				continue;
			}
			InsnNode assignInsn = ((RegisterArg) arg).getAssignInsn();
			if (assignInsn == null) {
				continue;
			}
			int sourceLine = assignInsn.getSourceLine();
			if (sourceLine != 0) {
				map.merge(sourceLine, 1, Integer::sum);
			}
		}
		for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
			if (entry.getValue() >= 2) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Convert one variable change with only 'then' branch:
	 * 'if (c) {r = a;}' to 'r = c ? a : r'
	 * Convert if 'r' used only once
	 */
	private static boolean processOneBranchTernary(MethodNode mth, IfRegion ifRegion) {
		IContainer thenRegion = ifRegion.getThenRegion();
		BlockNode block = getTernaryInsnBlock(mth, thenRegion);
		if (block != null) {
			InsnNode insn = block.getInstructions().get(0);
			RegisterArg result = insn.getResult();
			if (result != null) {
				if (!mth.isConstructor()
						&& block.contains(AFlag.DUPLICATED)
						&& !insertSharedBranchDefault(mth, ifRegion, block, insn)) {
					return false;
				}
				replaceWithTernary(mth, ifRegion, block, insn);
			}
		}
		return false;
	}

	private static boolean insertSharedBranchDefault(
			MethodNode mth, IfRegion ifRegion, BlockNode block, InsnNode insn) {
		if (insn.getType() != InsnType.MOVE
				|| insn.getArgsCount() != 1
				|| !insn.getArg(0).isRegister()
				|| block.getPredecessors().size() < 2) {
			return false;
		}
		RegisterArg branchResult = insn.getResult();
		PhiInsn phiInsn = branchResult.getSVar().getOnlyOneUseInPhi();
		if (phiInsn == null || phiInsn.getArgsCount() != 2 || phiInsn.getResult() == null) {
			return false;
		}
		IfRegion outerIf = findEnclosingSharedBranchIf(ifRegion, block);
		if (outerIf == null || outerIf.getConditionBlocks().isEmpty()) {
			return false;
		}
		BlockNode outerHeader = outerIf.getConditionBlocks().get(0);
		List<BlockNode> predecessors = outerHeader.getPredecessors();
		BlockNode insertBlock;
		if (predecessors.size() == 1) {
			insertBlock = predecessors.get(0);
		} else {
			BlockNode conditionDominator = BlockUtils.getCommonDominator(mth, predecessors);
			if (conditionDominator == null || conditionDominator.getPredecessors().size() != 1) {
				return false;
			}
			insertBlock = conditionDominator.getPredecessors().get(0);
		}
		RegisterArg sourceArg = (RegisterArg) insn.getArg(0);
		InsnNode sourceAssign = sourceArg.getAssignInsn();
		if (sourceAssign != null) {
			BlockNode sourceBlock = BlockUtils.getBlockByInsn(mth, sourceAssign);
			if (sourceBlock == null || sourceBlock != insertBlock && !insertBlock.isDominator(sourceBlock)) {
				return false;
			}
		}

		RegisterArg phiResult = phiInsn.getResult();
		RegisterArg defaultResult = mth.makeSyntheticRegArg(phiResult.getType());
		SSAVar defaultVar = defaultResult.getSVar();
		defaultVar.setCodeVar(phiResult.getSVar().getCodeVar());
		phiResult.getSVar().getCodeVar().addSsaVar(defaultVar);

		InsnNode defaultMove = new InsnNode(InsnType.MOVE, 1);
		defaultMove.setResult(defaultResult);
		defaultMove.addArg(sourceArg.duplicate());
		defaultMove.add(AFlag.SYNTHETIC);
		defaultMove.setSourceLine(outerIf.getSourceLine());
		defaultMove.rebindArgs();
		if (!insn.replaceArg(sourceArg, defaultResult.duplicate())) {
			return false;
		}

		insertBlock.getInstructions().add(defaultMove);
		return true;
	}

	private static IfRegion findEnclosingSharedBranchIf(IfRegion ifRegion, BlockNode sharedBlock) {
		IRegion parent = ifRegion.getParent();
		while (parent != null) {
			if (parent instanceof IfRegion) {
				IfRegion parentIf = (IfRegion) parent;
				IContainer elseRegion = parentIf.getElseRegion();
				return elseRegion != null && RegionUtils.isRegionContainsBlock(elseRegion, sharedBlock)
						? parentIf
						: null;
			}
			parent = parent.getParent();
		}
		return null;
	}

	@SuppressWarnings("StatementWithEmptyBody")
	private static void replaceWithTernary(MethodNode mth, IfRegion ifRegion, BlockNode block, InsnNode insn) {
		RegisterArg resArg = insn.getResult();
		if (resArg.getSVar().getUseList().size() != 1) {
			return;
		}
		PhiInsn phiInsn = resArg.getSVar().getOnlyOneUseInPhi();
		if (phiInsn == null || phiInsn.getArgsCount() != 2) {
			return;
		}
		RegisterArg phiResult = phiInsn.getResult();
		if (phiResult == null || phiResult.getSVar() == null) {
			return;
		}
		RegisterArg otherArg = null;
		for (InsnArg arg : phiInsn.getArguments()) {
			if (!resArg.sameRegAndSVar(arg)) {
				otherArg = (RegisterArg) arg;
				break;
			}
		}
		if (otherArg == null) {
			return;
		}
		BlockNode otherBlock = phiInsn.getBlockByArg(otherArg);
		if (otherBlock != null && otherBlock.contains(AFlag.DUPLICATED)) {
			// This edge can also be reached outside the current one-branch IF.
			// Consuming the PHI here would drop the assignment for that external path.
			return;
		}
		InsnNode elseAssign = otherArg.getAssignInsn();
		if (mth.isConstructor() || (mth.getParentClass().isEnum() && mth.getMethodInfo().isClassInit())) {
			// forcing ternary inline for constructors (will help in moving super call to the top) and enums
			// skip code style checks
		} else {
			if (elseAssign != null && elseAssign.isConstInsn() && elseAssign.getResult() != null) {
				if (!verifyLineHints(mth, insn, elseAssign)) {
					return;
				}
			} else {
				if (insn.getResult().sameCodeVar(otherArg)) {
					// don't use same variable in else branch to prevent: l = (l == 0) ? 1 : l
					return;
				}
			}
		}
		if (elseAssign != null && elseAssign.isConstInsn() && elseAssign.getResult() != null) {
			SSAVar elseVar = elseAssign.getResult().getSVar();
			BlockNode elseBlock = BlockUtils.getBlockByInsn(mth, elseAssign);
			if (elseVar.getUseCount() == 1
					&& elseVar.getOnlyOneUseInPhi() == phiInsn
					&& elseBlock != null
					&& elseBlock.contains(AFlag.DUPLICATED)) {
				// Removing this assignment would update only one duplicated region copy.
				// Keep the original if instead of producing inconsistent duplicated code.
				return;
			}
		}
		// all checks passed
		BlockNode header = ifRegion.getConditionBlocks().get(0);
		if (!ifRegion.getParent().replaceSubBlock(ifRegion, header)) {
			return;
		}
		InsnArg elseArg;
		if (elseAssign != null && elseAssign.isConstInsn() && elseAssign.getResult() != null) {
			// inline constant
			elseArg = InsnArg.wrapInsnIntoArg(elseAssign.copyWithoutResult());
			SSAVar elseVar = elseAssign.getResult().getSVar();
			if (elseVar.getUseCount() == 1 && elseVar.getOnlyOneUseInPhi() == phiInsn) {
				InsnRemover.remove(mth, elseAssign);
			}
		} else {
			elseArg = otherArg.duplicate();
		}
		InsnArg thenArg = InsnArg.wrapInsnIntoArg(insn);
		RegisterArg resultArg = phiResult.duplicate();
		TernaryInsn ternInsn = new TernaryInsn(ifRegion.getCondition(), resultArg, thenArg, elseArg);
		normalizeDynamicBooleanNumericBranches(ternInsn);
		ternInsn.simplifyCondition();

		InsnRemover.unbindAllArgs(mth, phiInsn);
		InsnRemover.delistPhi(mth, phiInsn);
		InsnRemover.unbindResult(mth, insn);
		InsnList.remove(block, insn);
		header.getInstructions().clear();
		ternInsn.rebindArgs();
		header.getInstructions().add(ternInsn);

		clearConditionBlocks(ifRegion.getConditionBlocks(), header);

		// shrink method again
		CodeShrinkVisitor.shrinkMethod(mth);
	}

	static void normalizeDynamicBooleanNumericBranches(TernaryInsn ternInsn) {
		RegisterArg result = ternInsn.getResult();
		if (result == null || result.getSVar() == null || ternInsn.getArgsCount() != 2) {
			return;
		}
		CodeVar codeVar = result.getSVar().getCodeVar();
		ArgType codeVarType = codeVar.getType();
		if (codeVarType != null && codeVarType.isTypeKnown()) {
			return;
		}
		InsnArg first = ternInsn.getArg(0);
		InsnArg second = ternInsn.getArg(1);
		ArgType firstType = getBranchType(first);
		ArgType secondType = getBranchType(second);
		InsnArg booleanBranch;
		if (ArgType.INT.equals(firstType) && ArgType.BOOLEAN.equals(secondType)) {
			booleanBranch = second;
		} else if (ArgType.BOOLEAN.equals(firstType) && ArgType.INT.equals(secondType)) {
			booleanBranch = first;
		} else {
			return;
		}
		if (!booleanBranch.isRegister()) {
			return;
		}
		RegisterArg booleanArg = ((RegisterArg) booleanBranch).duplicate();
		TernaryInsn convertInsn = ModVisitor.makeBooleanConvertInsn(null, booleanArg, ArgType.INT);
		convertInsn.add(AFlag.SYNTHETIC);
		InsnArg convertArg = InsnArg.wrapArg(convertInsn);
		convertArg.setType(ArgType.INT);
		if (!ternInsn.replaceArg(booleanBranch, convertArg)) {
			return;
		}
		codeVar.setType(ArgType.INT);
		for (SSAVar var : codeVar.getSsaVars()) {
			var.setType(ArgType.INT);
			if (var.getAssign() != null) {
				var.getAssign().forceSetInitType(ArgType.INT);
			}
			for (RegisterArg use : var.getUseList()) {
				use.forceSetInitType(ArgType.INT);
			}
		}
	}

	private static ArgType getBranchType(InsnArg arg) {
		if (arg.isRegister()) {
			ArgType initType = ((RegisterArg) arg).getInitType();
			if (initType != null && initType.isTypeKnown()) {
				return initType;
			}
		}
		ArgType type = arg.getType();
		return type != null && type.isTypeKnown() ? type : null;
	}

	private TernaryMod() {
	}
}
