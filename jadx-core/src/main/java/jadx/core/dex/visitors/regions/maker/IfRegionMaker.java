package jadx.core.dex.visitors.regions.maker;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jadx.api.plugins.input.data.annotations.EncodedValue;
import jadx.api.plugins.input.data.annotations.IAnnotation;
import jadx.api.plugins.input.data.attributes.JadxAttrType;
import jadx.api.plugins.input.data.attributes.types.AnnotationsAttr;
import jadx.core.Consts;
import jadx.core.dex.attributes.AFlag;
import jadx.core.dex.attributes.AType;
import jadx.core.dex.attributes.nodes.CoroutineLoopPreHeaderAttr;
import jadx.core.dex.attributes.nodes.EdgeInsnAttr;
import jadx.core.dex.attributes.nodes.LoopInfo;
import jadx.core.dex.attributes.nodes.PhiListAttr;
import jadx.core.dex.info.FieldInfo;
import jadx.core.dex.instructions.ArithNode;
import jadx.core.dex.instructions.ArithOp;
import jadx.core.dex.instructions.IfNode;
import jadx.core.dex.instructions.IfOp;
import jadx.core.dex.instructions.IndexInsnNode;
import jadx.core.dex.instructions.InsnType;
import jadx.core.dex.instructions.InvokeNode;
import jadx.core.dex.instructions.PhiInsn;
import jadx.core.dex.instructions.args.ArgType;
import jadx.core.dex.instructions.args.InsnArg;
import jadx.core.dex.instructions.args.InsnWrapArg;
import jadx.core.dex.instructions.args.LiteralArg;
import jadx.core.dex.instructions.args.RegisterArg;
import jadx.core.dex.instructions.args.SSAVar;
import jadx.core.dex.nodes.BlockNode;
import jadx.core.dex.nodes.Edge;
import jadx.core.dex.nodes.FieldNode;
import jadx.core.dex.nodes.IContainer;
import jadx.core.dex.nodes.IRegion;
import jadx.core.dex.nodes.InsnContainer;
import jadx.core.dex.nodes.InsnNode;
import jadx.core.dex.nodes.MethodNode;
import jadx.core.dex.regions.Region;
import jadx.core.dex.regions.SwitchRegion;
import jadx.core.dex.regions.conditions.IfCondition;
import jadx.core.dex.regions.conditions.IfInfo;
import jadx.core.dex.regions.conditions.IfRegion;
import jadx.core.dex.regions.loops.LoopRegion;
import jadx.core.dex.trycatch.ExcHandlerAttr;
import jadx.core.dex.visitors.kotlin.CoroutineMethodUtils;
import jadx.core.dex.visitors.kotlin.KtorCioRecovery;
import jadx.core.utils.BlockUtils;
import jadx.core.utils.ListUtils;
import jadx.core.utils.blocks.BlockSet;
import jadx.core.utils.exceptions.JadxRuntimeException;

import static jadx.core.utils.BlockUtils.bitSetToBlocks;
import static jadx.core.utils.BlockUtils.bitSetToOneBlock;
import static jadx.core.utils.BlockUtils.followEmptyPath;
import static jadx.core.utils.BlockUtils.getBottomBlock;
import static jadx.core.utils.BlockUtils.getPathCross;
import static jadx.core.utils.BlockUtils.isEqualPaths;
import static jadx.core.utils.BlockUtils.isEqualReturnBlocks;
import static jadx.core.utils.BlockUtils.isPathExists;
import static jadx.core.utils.BlockUtils.newBlocksBitSet;

final class IfRegionMaker {
	private static final Logger LOG = LoggerFactory.getLogger(IfRegionMaker.class);
	private static final ThreadLocal<ArrayDeque<TerminalSubgraphState>> TERMINAL_SUBGRAPH_STATE_POOL =
			ThreadLocal.withInitial(ArrayDeque::new);
	private static final ThreadLocal<ArrayDeque<CoroutineRestoreScanState>> COROUTINE_RESTORE_STATE_POOL =
			ThreadLocal.withInitial(ArrayDeque::new);

	private static final class TerminalSubgraphState {
		private final BitSet visiting = new BitSet();
		private final BitSet terminal = new BitSet();

		private void clear() {
			visiting.clear();
			terminal.clear();
		}
	}

	private static final class CoroutineRestoreScanState {
		private final BitSet objectRestoreSources = new BitSet();
		private boolean hasRestore;

		private void clear() {
			objectRestoreSources.clear();
			hasRestore = false;
		}
	}

	private final MethodNode mth;
	private final RegionMaker regionMaker;
	private final Map<BlockNode, Set<BlockNode>> dualInheritedExitBranches = new HashMap<>();
	private @Nullable Boolean coroutineSuspensionPoints;
	private @Nullable Boolean byteContinuationClassifier;

	IfRegionMaker(MethodNode mth, RegionMaker regionMaker) {
		this.mth = mth;
		this.regionMaker = regionMaker;
	}

	@Nullable
	BlockNode process(IRegion currentRegion, BlockNode block, IfNode ifnode, RegionStack stack) {
		if (block.contains(AFlag.ADDED_TO_REGION)) {
			// block already included in other 'if' region
			return ifnode.getThenBlock();
		}
		BlockNode normalizedOut = processSharedTerminalDiscriminator(currentRegion, block, stack);
		if (normalizedOut != null) {
			return normalizedOut;
		}
		IfInfo currentIf = makeIfInfo(mth, block);
		if (currentIf == null) {
			return null;
		}
		IfInfo mergedIf = block.contains(AType.STANDALONE_IF_REGION)
				? null
				: mergeNestedIfNodes(currentIf);
		if (mergedIf != null) {
			currentIf = mergedIf;
		} else if (!block.contains(AFlag.DONT_INVERT)) {
			// Compiler often emits a jump over the then branch. Mutate the CFG condition only
			// once because shared continuations can revisit the same IF block.
			currentIf = IfInfo.invert(currentIf);
			block.add(AFlag.DONT_INVERT);
		}
		IfInfo modifiedIf = restructureIf(block, currentIf);
		if (modifiedIf != null) {
			currentIf = modifiedIf;
		} else {
			if (currentIf.getMergedBlocks().size() <= 1) {
				return null;
			}
			currentIf = makeIfInfo(mth, block);
			currentIf = restructureIf(block, currentIf);
			if (currentIf == null) {
				// all attempts failed
				return null;
			}
		}
		confirmMerge(currentIf);
		IfRegion ifRegion = new IfRegion(currentRegion);
		ifRegion.updateCondition(currentIf);
		regionMaker.registerIfRegion(block, ifRegion);
		currentRegion.getSubBlocks().add(ifRegion);

		BlockNode outBlock = currentIf.getOutBlock();
		stack.push(ifRegion);
		stack.addExit(outBlock);
		Set<BlockNode> releasedExits = dualInheritedExitBranches.remove(block);
		if (releasedExits != null) {
			for (BlockNode releasedExit : releasedExits) {
				stack.removeExit(releasedExit);
			}
		}

		BlockNode thenBlock = currentIf.getThenBlock();
		if (thenBlock == null) {
			// empty then block, not normal, but maybe correct
			ifRegion.setThenRegion(new Region(ifRegion));
		} else {
			IContainer edgeBranch = makeDirectEdgeBranch(ifRegion, currentIf, thenBlock);
			ifRegion.setThenRegion(edgeBranch != null
					? edgeBranch
					: makeBranchRegion(currentIf, thenBlock, stack));
		}
		BlockNode elseBlock = currentIf.getElseBlock();
		if (elseBlock == null || stack.containsExit(elseBlock)) {
			ifRegion.setElseRegion(null);
		} else {
			IContainer edgeBranch = makeDirectEdgeBranch(ifRegion, currentIf, elseBlock);
			ifRegion.setElseRegion(edgeBranch != null
					? edgeBranch
					: makeBranchRegion(currentIf, elseBlock, stack));
		}

		// insert edge insns in new 'else' branch
		if (ifRegion.getElseRegion() == null && outBlock != null) {
			List<EdgeInsnAttr> edgeInsnAttrs = outBlock.getAll(AType.EDGE_INSN);
			if (!edgeInsnAttrs.isEmpty()) {
				List<InsnNode> instructions = new ArrayList<>();
				for (EdgeInsnAttr edgeInsnAttr : edgeInsnAttrs) {
					if (edgeInsnAttr.getEnd().equals(outBlock)) {
						if (currentIf.getMergedBlocks().contains(followEmptyPath(edgeInsnAttr.getStart(), true))) {
							instructions.add(edgeInsnAttr.getInsn());
						}
					}
				}

				if (!instructions.isEmpty()) {
					Region elseRegion = new Region(ifRegion);
					InsnContainer newBlock = new InsnContainer(instructions);
					elseRegion.add(newBlock);
					ifRegion.setElseRegion(elseRegion);
				}
			}
		}

		stack.pop();
		return outBlock;
	}

	private IContainer makeBranchRegion(IfInfo currentIf, BlockNode branchBlock, RegionStack stack) {
		if (!isDirectLoopContinueBranch(currentIf, branchBlock) || stack.containsExit(branchBlock)) {
			return regionMaker.makeRegion(branchBlock);
		}
		stack.addExit(branchBlock);
		try {
			return regionMaker.makeRegion(branchBlock);
		} finally {
			stack.removeExit(branchBlock);
		}
	}

	private boolean isDirectLoopContinueBranch(IfInfo currentIf, BlockNode branchBlock) {
		if (!branchBlock.contains(AFlag.SYNTHETIC)
				|| !branchBlock.getInstructions().isEmpty()
				|| branchBlock.getPredecessors().size() != 1
				|| branchBlock.getSuccessors().size() != 1
				|| !branchBlock.getSuccessors().get(0).contains(AFlag.LOOP_END)) {
			return false;
		}
		for (EdgeInsnAttr edgeInsnAttr : branchBlock.getAll(AType.EDGE_INSN)) {
			List<LoopInfo> sourceLoops = mth.getAllLoopsForBlock(edgeInsnAttr.getStart());
			List<LoopInfo> targetLoops = mth.getAllLoopsForBlock(branchBlock.getSuccessors().get(0));
			if (edgeInsnAttr.getStart() == branchBlock.getPredecessors().get(0)
					&& !edgeInsnAttr.getStart().contains(AFlag.ADDED_TO_REGION)
					&& edgeInsnAttr.getEnd() == branchBlock
					&& edgeInsnAttr.getInsn().getType() == InsnType.CONTINUE
					&& !sourceLoops.isEmpty()
					&& sourceLoops.equals(targetLoops)
					&& currentIf.getMergedBlocks().contains(followEmptyPath(edgeInsnAttr.getStart(), true))) {
				return true;
			}
		}
		return false;
	}

	private static @Nullable IContainer makeDirectEdgeBranch(
			IfRegion ifRegion, IfInfo currentIf, BlockNode branchBlock) {
		if (!branchBlock.contains(AFlag.SYNTHETIC)
				|| !branchBlock.getInstructions().isEmpty()) {
			return null;
		}
		List<InsnNode> instructions = new ArrayList<>();
		for (EdgeInsnAttr edgeInsnAttr : branchBlock.getAll(AType.EDGE_INSN)) {
			if (edgeInsnAttr.getEnd() == branchBlock
					&& currentIf.getMergedBlocks().contains(followEmptyPath(edgeInsnAttr.getStart(), true))
					&& edgeInsnAttr.getInsn().getType() == InsnType.BREAK) {
				instructions.add(edgeInsnAttr.getInsn());
			}
		}
		if (instructions.isEmpty()) {
			return null;
		}
		Region edgeRegion = new Region(ifRegion);
		edgeRegion.add(new InsnContainer(instructions));
		return edgeRegion;
	}

	private @Nullable BlockNode processSharedTerminalDiscriminator(
			IRegion currentRegion, BlockNode block, RegionStack stack) {
		SharedTerminalDiscriminator normalized = buildSharedTerminalDiscriminator(block);
		if (normalized == null) {
			return null;
		}
		confirmMerge(normalized.unsupportedInfo);

		IfRegion invalidRegion = new IfRegion(currentRegion);
		invalidRegion.updateCondition(normalized.invalidInfo);
		currentRegion.getSubBlocks().add(invalidRegion);
		stack.push(invalidRegion);
		invalidRegion.setThenRegion(regionMaker.makeRegion(normalized.throwBlock));
		invalidRegion.setElseRegion(null);
		stack.pop();

		IfRegion unsupportedRegion = new IfRegion(currentRegion);
		unsupportedRegion.updateCondition(normalized.unsupportedInfo);
		currentRegion.getSubBlocks().add(unsupportedRegion);
		stack.push(unsupportedRegion);
		stack.addExit(normalized.continuationBlock);
		unsupportedRegion.setThenRegion(regionMaker.makeRegion(normalized.terminalBlock));
		unsupportedRegion.setElseRegion(null);
		stack.pop();
		return normalized.continuationBlock;
	}

	private @Nullable SharedTerminalDiscriminator buildSharedTerminalDiscriminator(BlockNode firstBlock) {
		if (firstBlock.contains(AType.LOOP) || mth.getLoopForBlock(firstBlock) != null) {
			return null;
		}
		for (BlockNode successor : firstBlock.getCleanSuccessors()) {
			if (isPathExists(successor, firstBlock)) {
				return null;
			}
		}
		IfNode firstInsn = getIfInsn(firstBlock);
		BlockNode secondBlock = firstInsn == null ? null : findLinearIfBlock(firstInsn.getElseBlock());
		IfNode secondInsn = secondBlock == null ? null : getIfInsn(secondBlock);
		BlockNode thirdBlock = secondInsn == null ? null : findLinearIfBlock(secondInsn.getElseBlock());
		IfNode thirdInsn = thirdBlock == null ? null : getIfInsn(thirdBlock);
		BlockNode lastBlock = thirdInsn == null ? null : findLinearIfBlock(thirdInsn.getElseBlock());
		IfNode lastInsn = lastBlock == null ? null : getIfInsn(lastBlock);
		RegisterArg discriminator = firstInsn == null || secondInsn == null || thirdInsn == null || lastInsn == null
				? null
				: findSharedDiscriminator(firstInsn, secondInsn, thirdInsn, lastInsn);
		if (discriminator == null
				|| discriminator.getSVar().getAssignInsn() == null
				|| discriminator.getSVar().getAssignInsn().getType() != InsnType.AGET) {
			return null;
		}
		IfInfo first = makeIfInfo(mth, firstBlock);
		if (first == null) {
			return null;
		}
		IfInfo second = makeLinearIfInfo(first.getElseBlock());
		IfInfo third = second == null ? null : makeLinearIfInfo(second.getElseBlock());
		IfInfo last = third == null ? null : makeLinearIfInfo(third.getElseBlock());
		if (second == null || third == null || last == null) {
			return null;
		}

		BlockNode throwBlock;
		BlockNode terminalBlock;
		IfCondition validLastCondition;
		if (isLinearThrowPath(last.getThenBlock())) {
			throwBlock = last.getThenBlock();
			terminalBlock = last.getElseBlock();
			validLastCondition = notCopy(last.getCondition());
		} else if (isLinearThrowPath(last.getElseBlock())) {
			throwBlock = last.getElseBlock();
			terminalBlock = last.getThenBlock();
			validLastCondition = copyCondition(last.getCondition());
		} else {
			return null;
		}
		if (terminalBlock == null || isLinearThrowPath(terminalBlock)) {
			return null;
		}

		IfInfo firstGuard = makeSharedTerminalGuard(first.getThenBlock(), terminalBlock);
		IfInfo secondGuard = makeSharedTerminalGuard(second.getThenBlock(), terminalBlock);
		IfInfo thirdGuard = makeSharedTerminalGuard(third.getThenBlock(), terminalBlock);
		if (firstGuard == null || secondGuard == null || thirdGuard == null) {
			return null;
		}
		BlockNode continuationBlock = getOtherBranch(firstGuard, terminalBlock);
		if (continuationBlock == null
				|| getOtherBranch(secondGuard, terminalBlock) != continuationBlock
				|| getOtherBranch(thirdGuard, terminalBlock) != continuationBlock) {
			return null;
		}

		IfCondition firstCond = first.getCondition();
		IfCondition secondCond = second.getCondition();
		IfCondition thirdCond = third.getCondition();
		IfCondition invalidCondition = and(
				and(notCopy(firstCond), notCopy(secondCond)),
				and(notCopy(thirdCond), notCopy(validLastCondition)));

		IfCondition firstUnsupported = and(copyCondition(firstCond), guardToTerminal(firstGuard, terminalBlock));
		IfCondition secondUnsupported = and(
				and(notCopy(firstCond), copyCondition(secondCond)),
				guardToTerminal(secondGuard, terminalBlock));
		IfCondition thirdUnsupported = and(
				and(and(notCopy(firstCond), notCopy(secondCond)), copyCondition(thirdCond)),
				guardToTerminal(thirdGuard, terminalBlock));
		IfCondition lastTerminal = and(
				and(notCopy(firstCond), notCopy(secondCond)),
				and(notCopy(thirdCond), copyCondition(validLastCondition)));
		IfCondition unsupportedCondition = or(
				or(firstUnsupported, secondUnsupported),
				or(thirdUnsupported, lastTerminal));

		IfInfo invalidInfo = new IfInfo(mth, invalidCondition, throwBlock, null);
		invalidInfo.merge(first, second, third, last);
		IfInfo unsupportedInfo = new IfInfo(mth, unsupportedCondition, terminalBlock, null);
		unsupportedInfo.setOutBlock(continuationBlock);
		unsupportedInfo.merge(first, second, third, last, firstGuard, secondGuard, thirdGuard);
		return new SharedTerminalDiscriminator(
				invalidInfo, unsupportedInfo, throwBlock, terminalBlock, continuationBlock);
	}

	private static @Nullable BlockNode findLinearIfBlock(BlockNode start) {
		BlockNode block = start;
		for (int i = 0; i < 3 && block != null; i++) {
			if (getIfInsn(block) != null) {
				return block;
			}
			if (block.getInstructions().stream()
					.anyMatch(insn -> insn.getType() != InsnType.CONST && insn.getType() != InsnType.MOVE)
					|| block.getCleanSuccessors().size() != 1) {
				return null;
			}
			block = block.getCleanSuccessors().get(0);
		}
		return null;
	}

	private static @Nullable RegisterArg findSharedDiscriminator(IfNode... ifNodes) {
		for (InsnArg arg : ifNodes[0].getArguments()) {
			if (!(arg instanceof RegisterArg) || ((RegisterArg) arg).getSVar() == null) {
				continue;
			}
			boolean shared = true;
			for (int i = 1; i < ifNodes.length && shared; i++) {
				shared = false;
				for (InsnArg other : ifNodes[i].getArguments()) {
					if (other instanceof RegisterArg
							&& ((RegisterArg) other).getSVar() == ((RegisterArg) arg).getSVar()) {
						shared = true;
						break;
					}
				}
			}
			if (shared) {
				return (RegisterArg) arg;
			}
		}
		return null;
	}

	private @Nullable IfInfo makeLinearIfInfo(BlockNode start) {
		BlockNode block = start;
		for (int i = 0; i < 3 && block != null; i++) {
			IfInfo info = makeIfInfo(mth, block);
			if (info != null) {
				return info;
			}
			if (block.getInstructions().stream()
					.anyMatch(insn -> insn.getType() != InsnType.CONST && insn.getType() != InsnType.MOVE)
					|| block.getCleanSuccessors().size() != 1) {
				return null;
			}
			block = block.getCleanSuccessors().get(0);
		}
		return null;
	}

	private @Nullable IfInfo makeSharedTerminalGuard(BlockNode start, BlockNode terminalBlock) {
		IfInfo guard = makeIfInfo(mth, start);
		if (guard == null) {
			return null;
		}
		IfInfo merged = mergeNestedIfNodes(guard);
		if (merged != null) {
			guard = merged;
		}
		return getOtherBranch(guard, terminalBlock) != null ? guard : null;
	}

	private static @Nullable BlockNode getOtherBranch(IfInfo info, BlockNode branch) {
		if (isEqualPaths(info.getThenBlock(), branch)) {
			return info.getElseBlock();
		}
		if (isEqualPaths(info.getElseBlock(), branch)) {
			return info.getThenBlock();
		}
		return null;
	}

	private static IfCondition guardToTerminal(IfInfo guard, BlockNode terminalBlock) {
		return isEqualPaths(guard.getThenBlock(), terminalBlock)
				? copyCondition(guard.getCondition())
				: notCopy(guard.getCondition());
	}

	private static IfCondition notCopy(IfCondition condition) {
		return IfCondition.not(copyCondition(condition));
	}

	private static IfCondition copyCondition(IfCondition condition) {
		switch (condition.getMode()) {
			case COMPARE:
				IfNode source = condition.getCompare().getInsn();
				return IfCondition.fromIfNode(new IfNode(source.getOp(), -1,
						source.getArg(0).duplicate(), source.getArg(1).duplicate()));
			case NOT:
				return IfCondition.not(copyCondition(condition.first()));
			case TERNARY:
				return IfCondition.ternary(
						copyCondition(condition.first()),
						copyCondition(condition.second()),
						copyCondition(condition.third()));
			case AND:
			case OR:
				List<IfCondition> args = condition.getArgs();
				IfCondition result = copyCondition(args.get(0));
				for (int i = 1; i < args.size(); i++) {
					result = IfCondition.merge(condition.getMode(), result, copyCondition(args.get(i)));
				}
				return result;
			default:
				throw new JadxRuntimeException("Unexpected condition mode: " + condition.getMode());
		}
	}

	private static IfCondition and(IfCondition first, IfCondition second) {
		return IfCondition.merge(IfCondition.Mode.AND, first, second);
	}

	private static IfCondition or(IfCondition first, IfCondition second) {
		return IfCondition.merge(IfCondition.Mode.OR, first, second);
	}

	private static boolean isLinearThrowPath(BlockNode start) {
		BlockNode block = start;
		Set<BlockNode> visited = new HashSet<>();
		while (block != null && visited.size() < 8 && visited.add(block)) {
			InsnNode lastInsn = BlockUtils.getLastInsn(block);
			if (lastInsn != null && lastInsn.getType() == InsnType.THROW) {
				return true;
			}
			if (block.getCleanSuccessors().size() != 1) {
				return false;
			}
			block = block.getCleanSuccessors().get(0);
		}
		return false;
	}

	private static final class SharedTerminalDiscriminator {
		private final IfInfo invalidInfo;
		private final IfInfo unsupportedInfo;
		private final BlockNode throwBlock;
		private final BlockNode terminalBlock;
		private final BlockNode continuationBlock;

		private SharedTerminalDiscriminator(IfInfo invalidInfo, IfInfo unsupportedInfo,
				BlockNode throwBlock, BlockNode terminalBlock, BlockNode continuationBlock) {
			this.invalidInfo = invalidInfo;
			this.unsupportedInfo = unsupportedInfo;
			this.throwBlock = throwBlock;
			this.terminalBlock = terminalBlock;
			this.continuationBlock = continuationBlock;
		}
	}

	@NotNull
	IfInfo buildIfInfo(LoopRegion loopRegion) {
		IfInfo condInfo = makeIfInfo(mth, loopRegion.getHeader());
		condInfo = searchNestedIf(condInfo);
		confirmMerge(condInfo);
		return condInfo;
	}

	@Nullable
	static IfInfo makeIfInfo(MethodNode mth, BlockNode ifBlock) {
		InsnNode lastInsn = BlockUtils.getLastInsn(ifBlock);
		if (lastInsn == null || lastInsn.getType() != InsnType.IF) {
			return null;
		}
		IfNode ifNode = (IfNode) lastInsn;
		IfCondition condition = IfCondition.fromIfNode(ifNode);
		IfInfo info = new IfInfo(mth, condition, ifNode.getThenBlock(), ifNode.getElseBlock());
		info.getMergedBlocks().add(ifBlock);
		return info;
	}

	static IfInfo searchNestedIf(IfInfo info) {
		IfInfo next = mergeNestedIfNodes(info);
		if (next != null) {
			return next;
		}
		return info;
	}

	IfInfo restructureIf(BlockNode block, IfInfo info) {
		BlockNode thenBlock = info.getThenBlock();
		BlockNode elseBlock = info.getElseBlock();

		if (Objects.equals(thenBlock, elseBlock)) {
			IfInfo ifInfo = new IfInfo(info, null, null);
			ifInfo.setOutBlock(thenBlock);
			return ifInfo;
		}
		// select 'then', 'else' and 'exit' blocks
		if (isBranchReturn(thenBlock) && isBranchReturn(elseBlock)) {
			info.setOutBlock(null);
			return info;
		}
		IfInfo coroutineSuspendIf = restructureCoroutineSuspendReturn(info, thenBlock, elseBlock);
		if (coroutineSuspendIf != null) {
			return coroutineSuspendIf;
		}
		IfInfo sharedGuardFailure = restructureSharedGuardFailure(info, thenBlock, elseBlock);
		if (sharedGuardFailure != null) {
			return sharedGuardFailure;
		}
		IfInfo sharedTerminalDiscriminator = restructureSharedTerminalDiscriminator(info, thenBlock, elseBlock);
		if (sharedTerminalDiscriminator != null) {
			return sharedTerminalDiscriminator;
		}
		BlockNode structuralOut = findOutBlock(mth, thenBlock, elseBlock);
		BlockNode earlierResumeJoin = findEarlierDirectCoroutineResumeJoin(
				structuralOut, thenBlock, elseBlock);
		if (earlierResumeJoin != null) {
			structuralOut = earlierResumeJoin;
		}
		if (structuralOut != null
				&& mth.getLoopsCount() != 0
				&& isSharedValueCarrierBlock(structuralOut)
				&& hasByteContinuationClassifier()) {
			structuralOut = moveOutPastSharedBypass(thenBlock, elseBlock, structuralOut);
		}
		if (structuralOut == null
				&& info.getMergedBlocks().size() > 1
				&& hasByteContinuationClassifier()) {
			structuralOut = findReadOnlyBranchJoin(thenBlock, elseBlock);
		}
		structuralOut = moveOutPastPartiallyBypassedBranchAction(
				thenBlock, elseBlock, structuralOut);
		BlockNode loopContinueOut = findLoopContinueSharedOut(thenBlock, elseBlock, structuralOut);
		if (loopContinueOut != null) {
			structuralOut = loopContinueOut;
		}
		boolean directBranchJoin = isDirectBranchJoin(structuralOut, thenBlock, elseBlock);
		boolean suspendLambda = isSuspendLambdaMethod();
		// A suspend lambda without suspension points is often only a compiler-generated wrapper.
		// In this case a branch start selected as the structural out is the shared tail, not a
		// terminal branch. Keep real resume state machines on the coroutine-specific path below.
		// A coroutine branch can enter a loop while the other branch jumps directly to that loop's
		// single exit. Treating the reachable join as a terminal branch duplicates the shared tail.
		// Requiring one loop and one exit target keeps this narrower than all coroutine direct joins,
		// which regresses large inlined state machines with several loop exits.
		boolean preserveDirectBranchJoin = isCoroutineSingleLoopExitJoin(
				structuralOut, thenBlock, elseBlock)
				|| directBranchJoin
						&& suspendLambda
						&& !hasCoroutineSuspensionPoints();
		boolean preserveCoroutineResumeJoin = earlierResumeJoin != null
				&& isDirectCoroutineResumeJoin(structuralOut, thenBlock, elseBlock);
		info.setOutBlock(structuralOut);
		IfInfo switchExceptionExit = restructureSwitchFallThroughExceptionExit(info, thenBlock, elseBlock);
		if (switchExceptionExit != null) {
			return switchExceptionExit;
		}
		IfInfo inheritedLoopBreak = restructureInheritedLoopBreak(info, thenBlock, elseBlock);
		if (inheritedLoopBreak != null) {
			return inheritedLoopBreak;
		}
		IfInfo directScopeExit = restructureDeepDirectScopeExit(info, thenBlock, elseBlock);
		if (directScopeExit != null) {
			return directScopeExit;
		}
		IfInfo directTerminalIf = !preserveDirectBranchJoin
				&& !preserveCoroutineResumeJoin
				&& (isCoroutineMethod()
						|| isFirstCoroutineResumeLabelBranch(block, thenBlock, elseBlock)
						|| mth.contains(AType.TRY_PROTECTED_ITERATOR_BOOLEAN_ACTION)
						|| suspendLambda
						|| structuralOut == null)
								? restructureAcyclicTerminalBranch(info, thenBlock, elseBlock)
								: null;
		if (directTerminalIf != null) {
			return directTerminalIf;
		}
		// init outblock, which will be used in isBadBranchBlock to compare with branch block
		info.setOutBlock(structuralOut);
		BlockNode sharedContinuation = preserveCoroutineResumeJoin
				? null
				: findSharedContinuationPastTerminal(info.getOutBlock(), thenBlock, elseBlock);
		if (sharedContinuation != null) {
			IfInfo terminalBranch = restructureTerminalBeforeSharedContinuation(
					info, thenBlock, elseBlock, sharedContinuation);
			if (terminalBranch != null) {
				return terminalBranch;
			}
			info.setOutBlock(sharedContinuation);
		}
		if (!preserveCoroutineResumeJoin) {
			BlockNode coroutineLoopJoin = findEarlierCoroutineLoopJoin(
					block, info.getOutBlock(), thenBlock, elseBlock);
			if (coroutineLoopJoin != null) {
				info.setOutBlock(coroutineLoopJoin);
			} else {
				BlockNode coroutineOut = findDeeperCoroutinePhiJoin(
						block, info.getOutBlock(), thenBlock, elseBlock);
				if (coroutineOut != null) {
					info.setOutBlock(coroutineOut);
				}
			}
		}
		BlockNode ktorCioReadTail = findKtorCioReadTailJoin(
				block, info.getOutBlock(), thenBlock, elseBlock);
		if (ktorCioReadTail != null) {
			info.setOutBlock(ktorCioReadTail);
		}

		boolean badThen = isBadBranchBlock(info, thenBlock);
		boolean badElse = isBadBranchBlock(info, elseBlock);
		IfInfo loopContinueIf = restructureSyntheticLoopContinuation(info, thenBlock, elseBlock);
		if (loopContinueIf != null) {
			return loopContinueIf;
		}
		if (badThen != badElse) {
			IfInfo scopeExitIf = restructureDeepDirectScopeExit(info, thenBlock, elseBlock);
			if (scopeExitIf != null) {
				return scopeExitIf;
			}
		}
		IfInfo inheritedExitIf = restructureInheritedExit(info, thenBlock, elseBlock, badThen, badElse);
		if (inheritedExitIf != null) {
			return inheritedExitIf;
		}
		if (badThen && badElse) {
			IfInfo sharedSwitchAction = restructureMergedSwitchSharedAction(block, info, thenBlock, elseBlock);
			if (sharedSwitchAction != null) {
				return sharedSwitchAction;
			}
			IfInfo dualInheritedExitIf = restructureDualInheritedPhiBranches(block, info, thenBlock, elseBlock);
			if (dualInheritedExitIf != null) {
				return dualInheritedExitIf;
			}
			IfInfo phiAwareIf = restructurePhiAwareBranch(info, thenBlock, elseBlock);
			if (phiAwareIf != null) {
				return phiAwareIf;
			}
			IfInfo scopeExitIf = restructureDirectScopeExit(info, thenBlock, elseBlock);
			if (scopeExitIf != null) {
				return scopeExitIf;
			}
			if (isLinearTerminalPath(thenBlock) && isLinearTerminalPath(elseBlock)) {
				info.setOutBlock(null);
				return info;
			}
			IfInfo sharedOutIf = restructureSharedOut(info, thenBlock, elseBlock);
			if (sharedOutIf != null) {
				return sharedOutIf;
			}
			IfInfo sharedReturnIf = restructureSharedReturn(info, thenBlock, elseBlock);
			if (sharedReturnIf != null) {
				return sharedReturnIf;
			}
			if (Consts.DEBUG_RESTRUCTURE) {
				LOG.debug("Stop processing blocks after 'if': {}, method: {}", info.getMergedBlocks(), mth);
			}
			return null;
		}
		if (badElse) {
			info = new IfInfo(info, thenBlock, null);
			info.setOutBlock(elseBlock);
		} else if (badThen) {
			info = IfInfo.invert(info);
			info = new IfInfo(info, elseBlock, null);
			info.setOutBlock(thenBlock);
		}

		// getPathCross may not find outBlock (e.g. one branch has return, outBlock definitely is
		// null), so should check further
		if (info.getOutBlock() == null) {
			BlockNode scopeOutBlockThen = findScopeOutBlock(info.getThenBlock());
			BlockNode scopeOutBlockElse = findScopeOutBlock(info.getElseBlock());
			if (scopeOutBlockThen == null && scopeOutBlockElse != null) {
				info.setOutBlock(scopeOutBlockElse);
			} else if (scopeOutBlockThen != null && scopeOutBlockElse == null) {
				info.setOutBlock(scopeOutBlockThen);
			} else if (scopeOutBlockThen != null && scopeOutBlockThen == scopeOutBlockElse) {
				info.setOutBlock(scopeOutBlockThen);
			}
		}

		if (BlockUtils.isBackEdge(block, info.getOutBlock())) {
			info.setOutBlock(null);
		}
		return info;
	}

	/**
	 * A fall-through switch case can contain a conditional throwing path whose handler continues at
	 * the switch exit. The ordinary post-dominator is then the handler continuation, but using it as
	 * the local IF out also puts that continuation on the non-throwing fall-through path. Keep the
	 * next case as the IF boundary and render the exception-owned path as the selected branch.
	 */
	private @Nullable IfInfo restructureSwitchFallThroughExceptionExit(
			IfInfo info, BlockNode thenBlock, BlockNode elseBlock) {
		if (!isInsideSwitchRegion(regionMaker.getStack().peekRegion())) {
			return null;
		}
		IfInfo result = trySwitchFallThroughExceptionExit(info, thenBlock, elseBlock, true);
		return result != null
				? result
				: trySwitchFallThroughExceptionExit(info, elseBlock, thenBlock, false);
	}

	private @Nullable IfInfo trySwitchFallThroughExceptionExit(
			IfInfo info, BlockNode inheritedExit, BlockNode exceptionBranch, boolean exitIsThen) {
		RegionStack stack = regionMaker.getStack();
		if (inheritedExit == null
				|| exceptionBranch == null
				|| !stack.containsExit(inheritedExit)
				|| stack.containsExit(exceptionBranch)
				|| isPathExists(exceptionBranch, inheritedExit)
				|| !startsExceptionProtectedPath(exceptionBranch)
				|| !reachesOtherInheritedExit(exceptionBranch, inheritedExit, stack)) {
			return null;
		}
		IfInfo selected = exitIsThen ? IfInfo.invert(info) : info;
		IfInfo result = new IfInfo(selected, exceptionBranch, null);
		result.setOutBlock(inheritedExit);
		return result;
	}

	private static boolean startsExceptionProtectedPath(BlockNode block) {
		BlockNode current = block;
		Set<BlockNode> visited = new HashSet<>();
		while (current != null && visited.size() < 4 && visited.add(current)) {
			if (current.contains(AFlag.EXC_TOP_SPLITTER)
					|| current.contains(AType.EXC_CATCH)) {
				return true;
			}
			List<BlockNode> successors = current.getCleanSuccessors();
			current = current.isSynthetic() && successors.size() == 1 ? successors.get(0) : null;
		}
		return false;
	}

	private static boolean reachesOtherInheritedExit(
			BlockNode start, BlockNode excludedExit, RegionStack stack) {
		for (BlockNode exit : stack.getExits()) {
			if (exit != excludedExit && isPathExists(start, exit)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Structure a short-circuit guard ladder around its shared failure continuation. Compilers often
	 * emit every failed guard as a jump to the next decision case while the successful path jumps to
	 * an inherited join. Treating that inherited join as this IF's out duplicates the complete next
	 * case for every failed guard. Using the shared failure as the local out keeps the positive guards
	 * nested and emits the next case once.
	 */
	private @Nullable IfInfo restructureSharedGuardFailure(
			IfInfo info, BlockNode thenBlock, BlockNode elseBlock) {
		int blocksCount = mth.getBasicBlocks().size();
		boolean largeGeneratedLambda = blocksCount >= 800
				&& mth.getName().equals("invoke");
		if ((blocksCount >= 800 && !largeGeneratedLambda)
				|| thenBlock == null
				|| elseBlock == null
				|| !largeGeneratedLambda
						&& thenBlock.getPredecessors().size() < 4
						&& elseBlock.getPredecessors().size() < 4) {
			return null;
		}
		IfInfo result = trySharedGuardFailure(info, thenBlock, elseBlock, true);
		return result != null ? result : trySharedGuardFailure(info, elseBlock, thenBlock, false);
	}

	private @Nullable IfInfo trySharedGuardFailure(
			IfInfo info, BlockNode failure, BlockNode guardedPath, boolean failureIsThen) {
		int minFailurePredecessors = mth.getBasicBlocks().size() >= 800
				&& mth.getName().equals("invoke") ? 2 : 4;
		if (failure == null
				|| guardedPath == null
				|| failure.getPredecessors().size() < minFailurePredecessors
				|| isAcyclicTerminalSubgraph(failure)
				|| regionMaker.getStack().containsExit(failure)
				|| failure.contains(AFlag.LOOP_START)
				|| failure.contains(AFlag.LOOP_END)
				|| BlockUtils.isExceptionHandlerPath(failure)
				|| !isPathExists(guardedPath, failure)) {
			return null;
		}
		int guardEdges = 0;
		for (BlockNode predecessor : failure.getPredecessors()) {
			if (info.getMergedBlocks().contains(predecessor)
					|| isPathExists(guardedPath, predecessor)) {
				guardEdges++;
			}
		}
		if (guardEdges < 2) {
			return null;
		}
		Set<BlockNode> inheritedExits = new HashSet<>();
		for (BlockNode exit : regionMaker.getStack().getExits()) {
			if (exit != failure
					&& isPathExists(failure, exit)
					&& isPathExistsAvoiding(guardedPath, exit, failure)) {
				inheritedExits.add(exit);
			}
		}
		if (inheritedExits.isEmpty()
				|| !allPathsReachGuardBoundary(
						guardedPath, failure, inheritedExits, new HashSet<>(), new HashMap<>())) {
			return null;
		}
		IfInfo normalized = failureIsThen ? IfInfo.invert(info) : info;
		IfInfo result = new IfInfo(normalized, guardedPath, null);
		result.setOutBlock(failure);
		return result;
	}

	/**
	 * String/hash switches and guard chains often end every failed equality check at one shared
	 * default handler while a successful check enters a distinct terminal handler. Both branches
	 * terminate, so a post-dominator does not exist; treating both as bad drops the equality check.
	 * Keep the highly shared PHI handler as the out and emit only the selected terminal handler here.
	 */
	private @Nullable IfInfo restructureSharedTerminalDiscriminator(
			IfInfo info, BlockNode thenBlock, BlockNode elseBlock) {
		IfInfo result = trySharedTerminalDiscriminator(info, thenBlock, elseBlock, true);
		return result != null ? result : trySharedTerminalDiscriminator(info, elseBlock, thenBlock, false);
	}

	private static @Nullable IfInfo trySharedTerminalDiscriminator(
			IfInfo info, BlockNode sharedBranch, BlockNode selectedBranch, boolean sharedIsThen) {
		int sharedPredecessors = sharedBranch.getPredecessors().size();
		// Keep this normalization for strongly shared generated dispatch tails. A small
		// string switch can have the same local shape, but selecting its default as the
		// IF out destroys the higher-level switch reconstruction.
		if (sharedPredecessors < 8
				|| sharedPredecessors < selectedBranch.getPredecessors().size() + 2
				|| !hasPhiCarriers(sharedBranch)
				|| !isAcyclicTerminalSubgraph(sharedBranch, 32)
				|| !isAcyclicTerminalSubgraph(selectedBranch, 32)
				|| isPathExists(sharedBranch, selectedBranch)
				|| isPathExists(selectedBranch, sharedBranch)) {
			return null;
		}
		IfInfo selectedInfo = sharedIsThen ? IfInfo.invert(info) : info;
		IfInfo result = new IfInfo(selectedInfo, selectedBranch, null);
		result.setOutBlock(sharedBranch);
		return result;
	}

	private boolean allPathsReachGuardBoundary(
			BlockNode block, BlockNode failure, Set<BlockNode> inheritedExits,
			Set<BlockNode> visiting, Map<BlockNode, Boolean> resolved) {
		if (block == failure || inheritedExits.contains(block)) {
			return true;
		}
		Boolean cached = resolved.get(block);
		if (cached != null) {
			return cached;
		}
		if (block.contains(AFlag.LOOP_START)
				|| block.contains(AFlag.LOOP_END)
				|| BlockUtils.isExceptionHandlerPath(block)
				|| !visiting.add(block)) {
			resolved.put(block, false);
			return false;
		}
		List<BlockNode> successors = block.getCleanSuccessors();
		if (successors.isEmpty()) {
			visiting.remove(block);
			boolean reachesBoundary = BlockUtils.isExitBlock(mth, block);
			resolved.put(block, reachesBoundary);
			return reachesBoundary;
		}
		for (BlockNode successor : successors) {
			if (!allPathsReachGuardBoundary(successor, failure, inheritedExits, visiting, resolved)) {
				visiting.remove(block);
				resolved.put(block, false);
				return false;
			}
		}
		visiting.remove(block);
		resolved.put(block, true);
		return true;
	}

	/**
	 * A nested condition can branch directly to two different inherited scope exits: one edge exits
	 * the current loop while the other resumes the enclosing branch continuation. Neither target is
	 * a local join, so the regular bad-branch checks reject both and drop the condition. Preserve the
	 * loop exit as an explicit branch and use the other inherited exit as this region's continuation.
	 */
	private @Nullable IfInfo restructureInheritedLoopBreak(
			IfInfo info, BlockNode thenBlock, BlockNode elseBlock) {
		if (info.getOutBlock() != null
				|| !regionMaker.getStack().containsExit(thenBlock)
				|| !regionMaker.getStack().containsExit(elseBlock)) {
			return null;
		}
		boolean thenBreak = hasDirectBreakEdge(info, thenBlock);
		boolean elseBreak = hasDirectBreakEdge(info, elseBlock);
		if (thenBreak == elseBreak) {
			return null;
		}
		if (elseBreak) {
			info = IfInfo.invert(info);
			BlockNode tmp = thenBlock;
			thenBlock = elseBlock;
			elseBlock = tmp;
		}
		IfInfo result = new IfInfo(info, thenBlock, null);
		result.setOutBlock(elseBlock);
		return result;
	}

	private static boolean hasDirectBreakEdge(IfInfo info, BlockNode target) {
		for (EdgeInsnAttr edgeInsnAttr : target.getAll(AType.EDGE_INSN)) {
			if (edgeInsnAttr.getEnd() == target
					&& edgeInsnAttr.getInsn().getType() == InsnType.BREAK
					&& info.getMergedBlocks().contains(followEmptyPath(edgeInsnAttr.getStart(), true))) {
				return true;
			}
		}
		return false;
	}

	static boolean isDirectBranchJoin(
			@Nullable BlockNode structuralOut, BlockNode thenBlock, BlockNode elseBlock) {
		return structuralOut == thenBlock || structuralOut == elseBlock;
	}

	static boolean isReachableDirectBranchJoin(
			@Nullable BlockNode structuralOut, BlockNode thenBlock, BlockNode elseBlock) {
		if (structuralOut == thenBlock) {
			return isPathExists(elseBlock, structuralOut);
		}
		if (structuralOut == elseBlock) {
			return isPathExists(thenBlock, structuralOut);
		}
		return false;
	}

	private boolean isCoroutineSingleLoopExitJoin(
			@Nullable BlockNode structuralOut, BlockNode thenBlock, BlockNode elseBlock) {
		if (!isCoroutineMethod()
				|| !isReachableDirectBranchJoin(structuralOut, thenBlock, elseBlock)) {
			return false;
		}
		BlockNode loopBranch = structuralOut == thenBlock ? elseBlock : thenBlock;
		LoopInfo loop = mth.getLoopForBlock(loopBranch);
		if (loop == null || loop.getLoopBlocks().contains(structuralOut)) {
			return false;
		}
		List<Edge> exitEdges = loop.getExitEdges();
		return !exitEdges.isEmpty()
				&& exitEdges.stream().allMatch(edge -> {
					BlockNode target = edge.getTarget();
					return target == structuralOut || followEmptyPath(target) == structuralOut;
				});
	}

	private boolean hasCoroutineSuspensionPoints() {
		if (coroutineSuspensionPoints != null) {
			return coroutineSuspensionPoints;
		}
		coroutineSuspensionPoints = detectCoroutineSuspensionPoints();
		return coroutineSuspensionPoints;
	}

	private boolean detectCoroutineSuspensionPoints() {
		AnnotationsAttr annotations = mth.getParentClass().get(JadxAttrType.ANNOTATION_LIST);
		if (annotations == null) {
			return false;
		}
		IAnnotation annotation = annotations.get("Lkotlin/coroutines/jvm/internal/DebugMetadata;");
		if (annotation == null) {
			return false;
		}
		EncodedValue lines = annotation.getValue("l");
		return lines != null
				&& lines.getValue() instanceof List
				&& !((List<?>) lines.getValue()).isEmpty();
	}

	private static @Nullable IfInfo restructureAcyclicTerminalBranch(
			IfInfo info, BlockNode thenBlock, BlockNode elseBlock) {
		boolean thenTerminal = isAcyclicTerminalSubgraph(thenBlock);
		boolean elseTerminal = isAcyclicTerminalSubgraph(elseBlock);
		if (thenTerminal == elseTerminal) {
			return null;
		}
		if (elseTerminal) {
			info = IfInfo.invert(info);
			BlockNode tmp = thenBlock;
			thenBlock = elseBlock;
			elseBlock = tmp;
		}
		IfInfo result = new IfInfo(info, thenBlock, null);
		result.setOutBlock(elseBlock);
		return result;
	}

	private static @Nullable IfNode getIfInsn(BlockNode block) {
		InsnNode lastInsn = BlockUtils.getLastInsn(block);
		return lastInsn instanceof IfNode ? (IfNode) lastInsn : null;
	}

	private @Nullable BlockNode findSharedContinuationPastTerminal(
			@Nullable BlockNode currentOut, BlockNode thenBlock, BlockNode elseBlock) {
		if (currentOut == null
				|| !isAcyclicTerminalSubgraph(currentOut)
				|| isDirectCoroutineResumeJoin(currentOut, thenBlock, elseBlock)) {
			return null;
		}
		boolean currentOutIsCommonPostDominator = isCommonPostDominator(mth, thenBlock, elseBlock, currentOut);
		BlockNode best = null;
		for (BlockNode candidate : mth.getBasicBlocks()) {
			if (candidate == currentOut
					|| candidate.getPredecessors().size() < 2
					|| BlockUtils.isExceptionHandlerPath(candidate)
					|| isAcyclicTerminalSubgraph(candidate)
					|| isPathExists(currentOut, candidate)
					|| !isPathExists(thenBlock, candidate)
					|| !isPathExists(elseBlock, candidate)) {
				continue;
			}
			if (best == null || isPathExists(candidate, best)) {
				best = candidate;
			}
		}
		// Don't move an already valid structural join back to a block bypassed by some branch paths.
		if (currentOutIsCommonPostDominator
				&& best != null
				&& !isCommonPostDominator(mth, thenBlock, elseBlock, best)) {
			return null;
		}
		return best;
	}

	/**
	 * A post-dominator search can select the final coroutine response join and skip an earlier
	 * two-input PHI where the initial invocation and a resume-label restore actually meet. Keep the
	 * first structurally proven resume join as the local out so the restore block remains represented
	 * and later suspension tails are emitted only once.
	 */
	private @Nullable BlockNode findEarlierDirectCoroutineResumeJoin(
			@Nullable BlockNode currentOut, BlockNode thenBlock, BlockNode elseBlock) {
		if (currentOut == null) {
			return null;
		}
		BlockNode match = null;
		for (BlockNode candidate : mth.getBasicBlocks()) {
			PhiListAttr phiList = candidate.get(AType.PHI_LIST);
			if (candidate == currentOut
					|| phiList == null
					|| phiList.getList().isEmpty()
					|| !isPathExists(candidate, currentOut)
					|| !isDirectCoroutineResumeJoin(candidate, thenBlock, elseBlock)
					|| countCoroutineSavedLocalRestores(candidate) < 3) {
				continue;
			}
			if (match == null || isPathExists(candidate, match)) {
				match = candidate;
			}
		}
		return match;
	}

	private static int countCoroutineSavedLocalRestores(BlockNode join) {
		for (BlockNode predecessor : join.getPredecessors()) {
			if (!isCoroutineResumeRestoreBlock(predecessor)) {
				continue;
			}
			Set<FieldInfo> restoredFields = new HashSet<>();
			for (InsnNode insn : predecessor.getInstructions()) {
				insn.visitInsns(innerInsn -> {
					if (innerInsn instanceof IndexInsnNode
							&& innerInsn.getType() == InsnType.IGET
							&& ((IndexInsnNode) innerInsn).getIndex() instanceof FieldInfo) {
						FieldInfo field = (FieldInfo) ((IndexInsnNode) innerInsn).getIndex();
						if (field.getType().isObject()) {
							restoredFields.add(field);
						}
					}
					return null;
				});
			}
			return restoredFields.size();
		}
		return 0;
	}

	private static @Nullable IfInfo restructureTerminalBeforeSharedContinuation(
			IfInfo info, BlockNode thenBlock, BlockNode elseBlock, BlockNode sharedContinuation) {
		boolean thenTerminal = isAcyclicTerminalSubgraph(thenBlock);
		boolean elseTerminal = isAcyclicTerminalSubgraph(elseBlock);
		if (thenTerminal == elseTerminal) {
			return null;
		}
		// The regular terminal proof is intentionally shallow. A long acyclic branch can exceed
		// that budget while still terminating, especially in a coroutine state-machine entry path.
		// Treating only the shorter resume dispatch as terminal then moves the out block into the
		// longer branch and drops every path that bypasses that selected continuation. If both
		// branches terminate with the larger proof budget, preserve both complete branches.
		BlockNode shallowProofMiss = thenTerminal ? elseBlock : thenBlock;
		if (isAcyclicTerminalSubgraph(shallowProofMiss, 32)) {
			IfInfo result = new IfInfo(info, thenBlock, elseBlock);
			result.setOutBlock(null);
			return result;
		}
		if (elseTerminal) {
			info = IfInfo.invert(info);
			BlockNode tmp = thenBlock;
			thenBlock = elseBlock;
			elseBlock = tmp;
		}
		IfInfo result = new IfInfo(info, thenBlock, null);
		result.setOutBlock(sharedContinuation);
		return result;
	}

	private @Nullable IfInfo restructureCoroutineSuspendReturn(
			IfInfo info, BlockNode thenBlock, BlockNode elseBlock) {
		boolean thenSuspendReturn = isComparedValueReturn(info, thenBlock);
		boolean elseSuspendReturn = isComparedValueReturn(info, elseBlock);
		if (thenSuspendReturn == elseSuspendReturn) {
			return null;
		}
		BlockNode returnBlock = thenSuspendReturn ? thenBlock : elseBlock;
		BlockNode continuation = thenSuspendReturn ? elseBlock : thenBlock;
		IfInfo condition = thenSuspendReturn ? info : IfInfo.invert(info);
		IfInfo result = new IfInfo(condition, returnBlock, null);
		result.setOutBlock(continuation);
		return result;
	}

	private boolean isComparedValueReturn(IfInfo info, BlockNode block) {
		if (!isCoroutineMethod() || !block.isReturnBlock()) {
			return false;
		}
		InsnNode returnInsn = BlockUtils.getLastInsn(block);
		if (returnInsn == null || returnInsn.getArgsCount() != 1 || !(returnInsn.getArg(0) instanceof RegisterArg)) {
			return false;
		}
		RegisterArg returnArg = (RegisterArg) returnInsn.getArg(0);
		if (returnArg.getSVar() == null) {
			return false;
		}
		for (BlockNode conditionBlock : info.getMergedBlocks()) {
			InsnNode conditionInsn = BlockUtils.getLastInsn(conditionBlock);
			if (!(conditionInsn instanceof IfNode)) {
				continue;
			}
			for (InsnArg arg : conditionInsn.getArguments()) {
				if (arg instanceof RegisterArg && ((RegisterArg) arg).getSVar() == returnArg.getSVar()) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean isCoroutineMethod() {
		return CoroutineMethodUtils.hasContinuationParameter(mth);
	}

	private @Nullable BlockNode findLoopContinueSharedOut(
			BlockNode thenBlock, BlockNode elseBlock, @Nullable BlockNode currentOut) {
		if (currentOut == null || !currentOut.contains(AFlag.LOOP_END)) {
			return null;
		}
		LoopInfo loop = mth.getLoopForBlock(currentOut);
		if (loop == null) {
			return null;
		}
		BlockNode best = null;
		for (BlockNode candidate : mth.getBasicBlocks()) {
			PhiListAttr phiList = candidate.get(AType.PHI_LIST);
			if (candidate == currentOut
					|| candidate.contains(AFlag.LOOP_START)
					|| candidate.contains(AFlag.LOOP_END)
					|| mth.getLoopForBlock(candidate) != loop
					|| candidate.getPredecessors().size() < 2
					|| phiList == null
					|| phiList.getList().isEmpty()
					|| BlockUtils.isExceptionHandlerPath(candidate)
					|| !isPathExists(thenBlock, candidate)
					|| !isPathExists(elseBlock, candidate)
					|| !isPathExists(candidate, currentOut)
					|| !allPathsReachJoinOrContinue(thenBlock, candidate, currentOut)
					|| !allPathsReachJoinOrContinue(elseBlock, candidate, currentOut)
					|| !hasDirectContinueBypass(thenBlock, candidate, currentOut, new HashSet<>())
					|| !hasDirectContinueBypass(elseBlock, candidate, currentOut, new HashSet<>())) {
				continue;
			}
			if (best == null || isPathExists(candidate, best)) {
				best = candidate;
			}
		}
		return best;
	}

	static boolean allPathsReachJoinOrContinue(BlockNode block, BlockNode join, BlockNode loopEnd) {
		return allPathsReachJoinOrContinue(block, join, loopEnd, new HashSet<>(), new HashMap<>());
	}

	private static boolean allPathsReachJoinOrContinue(
			BlockNode block, BlockNode join, BlockNode loopEnd,
			Set<BlockNode> visiting, Map<BlockNode, Boolean> memo) {
		if (block == join || isDirectContinueBridge(block, join, loopEnd)) {
			return true;
		}
		Boolean cached = memo.get(block);
		if (cached != null) {
			return cached;
		}
		if (block == loopEnd || !visiting.add(block)) {
			return false;
		}
		List<BlockNode> successors = block.getCleanSuccessors();
		if (successors.isEmpty()) {
			visiting.remove(block);
			memo.put(block, false);
			return false;
		}
		for (BlockNode successor : successors) {
			if (!allPathsReachJoinOrContinue(successor, join, loopEnd, visiting, memo)) {
				visiting.remove(block);
				memo.put(block, false);
				return false;
			}
		}
		visiting.remove(block);
		memo.put(block, true);
		return true;
	}

	private static boolean hasDirectContinueBypass(
			BlockNode block, BlockNode join, BlockNode loopEnd, Set<BlockNode> visited) {
		if (block == join || block == loopEnd || !visited.add(block)) {
			return false;
		}
		if (isDirectContinueBridge(block, join, loopEnd)) {
			return true;
		}
		for (BlockNode successor : block.getCleanSuccessors()) {
			if (hasDirectContinueBypass(successor, join, loopEnd, visited)) {
				return true;
			}
		}
		return false;
	}

	private static boolean isDirectContinueBridge(BlockNode block, BlockNode join, BlockNode loopEnd) {
		if (!block.contains(AFlag.SYNTHETIC)
				|| !block.getInstructions().isEmpty()
				|| block.getPredecessors().size() != 1
				|| block.getCleanSuccessors().size() != 1
				|| block.getCleanSuccessors().get(0) != loopEnd) {
			return false;
		}
		BlockNode predecessor = block.getPredecessors().get(0);
		if (!(BlockUtils.getLastInsn(predecessor) instanceof IfNode)) {
			return false;
		}
		for (BlockNode successor : predecessor.getCleanSuccessors()) {
			if (successor != block && isPathExists(successor, join)) {
				return true;
			}
		}
		return false;
	}

	private @Nullable BlockNode findDeeperCoroutinePhiJoin(
			BlockNode ifBlock, @Nullable BlockNode currentOut, BlockNode thenBlock, BlockNode elseBlock) {
		if (currentOut == null
				|| currentOut.get(AType.PHI_LIST) == null
				|| !isCoroutineLabelIf(ifBlock)) {
			return null;
		}
		if (isDirectCoroutineResumeJoin(currentOut, thenBlock, elseBlock)) {
			return null;
		}
		BlockNode best = currentOut;
		for (BlockNode candidate : mth.getBasicBlocks()) {
			PhiListAttr phiList = candidate.get(AType.PHI_LIST);
			if (candidate == currentOut
					|| phiList == null
					|| phiList.getList().isEmpty()
					|| BlockUtils.isExceptionHandlerPath(candidate)
					|| !isPathExists(currentOut, candidate)
					|| !isPathExists(thenBlock, candidate)
					|| !isPathExists(elseBlock, candidate)) {
				continue;
			}
			if (best == currentOut || isPathExists(best, candidate)) {
				best = candidate;
			}
		}
		return best == currentOut ? null : best;
	}

	private static boolean isFirstCoroutineResumeLabelBranch(
			BlockNode ifBlock, BlockNode thenBlock, BlockNode elseBlock) {
		return isCoroutineLabelIf(ifBlock)
				&& (isFirstCoroutineResumeBranch(thenBlock)
						|| isFirstCoroutineResumeBranch(elseBlock));
	}

	private static boolean isFirstCoroutineResumeBranch(BlockNode resumeRestore) {
		if (!isCoroutineResumeRestoreBlock(resumeRestore)) {
			return false;
		}
		BlockNode join = resumeRestore.getCleanSuccessors().get(0);
		if (join.getPredecessors().size() != 2) {
			return false;
		}
		BlockNode directJoinPredecessor = join.getPredecessors().get(0) == resumeRestore
				? join.getPredecessors().get(1)
				: join.getPredecessors().get(0);
		BlockNode directCheck = findCoroutineSuspendCheck(directJoinPredecessor, join);
		if (directCheck == null) {
			return false;
		}
		BlockNode suspendedReturn = ListUtils.filterOnlyOne(
				directCheck.getCleanSuccessors(),
				successor -> !isPathExists(successor, join));
		return suspendedReturn != null && isAcyclicTerminalSubgraph(suspendedReturn);
	}

	/**
	 * The first suspend result is shared by the initial invocation and one resume-label branch.
	 * Other label branches resume after later suspension points and terminate without visiting this
	 * join. Moving the out block to a deeper PHI duplicates the whole result-processing tail and can
	 * make region generation revisit its conditions with a different orientation.
	 */
	static boolean isDirectCoroutineResumeJoin(
			BlockNode join, BlockNode thenBlock, BlockNode elseBlock) {
		if (join.getPredecessors().size() != 2) {
			return false;
		}
		BlockNode resumeRestore = null;
		for (BlockNode predecessor : join.getPredecessors()) {
			if (isCoroutineResumeRestoreBlock(predecessor)) {
				if (resumeRestore != null) {
					return false;
				}
				resumeRestore = predecessor;
			}
		}
		if (resumeRestore == null) {
			return false;
		}
		BlockNode directJoinPredecessor = join.getPredecessors().get(0) == resumeRestore
				? join.getPredecessors().get(1)
				: join.getPredecessors().get(0);
		BlockNode directCheck = findCoroutineSuspendCheck(directJoinPredecessor, join);
		if (directCheck == null) {
			return false;
		}
		BlockNode suspendedReturn = ListUtils.filterOnlyOne(
				directCheck.getCleanSuccessors(),
				successor -> !isPathExists(successor, join));
		if (suspendedReturn == null || !isAcyclicTerminalSubgraph(suspendedReturn)) {
			return false;
		}
		boolean resumeFromThen = isPathExists(thenBlock, resumeRestore);
		boolean resumeFromElse = isPathExists(elseBlock, resumeRestore);
		boolean directFromThen = isPathExists(thenBlock, directCheck);
		boolean directFromElse = isPathExists(elseBlock, directCheck);
		return resumeFromThen != resumeFromElse
				&& directFromThen != directFromElse
				&& resumeFromThen != directFromThen;
	}

	private static @Nullable BlockNode findCoroutineSuspendCheck(
			BlockNode directJoinPredecessor, BlockNode join) {
		BlockNode block = directJoinPredecessor;
		BlockNode pathSuccessor = join;
		for (int depth = 0; depth < 4; depth++) {
			if (BlockUtils.getLastInsn(block) instanceof IfNode
					&& block.getCleanSuccessors().contains(pathSuccessor)) {
				return block;
			}
			if (block.getCleanSuccessors().size() != 1
					|| block.getCleanSuccessors().get(0) != pathSuccessor
					|| block.getPredecessors().size() != 1
					|| block.getInstructions().stream().anyMatch(insn -> !isReadOnlyInsn(insn))) {
				return null;
			}
			pathSuccessor = block;
			block = block.getPredecessors().get(0);
		}
		return null;
	}

	private static boolean isCoroutineResumeRestoreBlock(BlockNode block) {
		if (block.getCleanSuccessors().size() != 1) {
			return false;
		}
		ArrayDeque<CoroutineRestoreScanState> pool = COROUTINE_RESTORE_STATE_POOL.get();
		CoroutineRestoreScanState state = pool.pollLast();
		if (state == null) {
			state = new CoroutineRestoreScanState();
		}
		try {
			boolean hasFailureCheck = false;
			for (InsnNode insn : block.getInstructions()) {
				if (!state.hasRestore) {
					scanCoroutineRestoreInsns(insn, state);
				}
				if (insn instanceof InvokeNode
						&& insn.getResult() == null
						&& insn.getArgsCount() == 1) {
					hasFailureCheck = true;
					if (state.hasRestore) {
						return true;
					}
				}
			}
			return state.hasRestore && hasFailureCheck;
		} finally {
			state.clear();
			pool.addLast(state);
		}
	}

	private static void scanCoroutineRestoreInsns(InsnNode insn, CoroutineRestoreScanState state) {
		if (insn instanceof IndexInsnNode
				&& insn.getType() == InsnType.IGET
				&& ((IndexInsnNode) insn).getIndex() instanceof FieldInfo) {
			FieldInfo field = (FieldInfo) ((IndexInsnNode) insn).getIndex();
			if (field.getName().startsWith("L$")) {
				state.hasRestore = true;
				return;
			}
			if (field.getType().isObject()
					&& insn.getArgsCount() == 1
					&& insn.getArg(0).isRegister()) {
				int sourceReg = ((RegisterArg) insn.getArg(0)).getRegNum();
				if (state.objectRestoreSources.get(sourceReg)) {
					state.hasRestore = true;
					return;
				}
				state.objectRestoreSources.set(sourceReg);
			}
		}
		int argsCount = insn.getArgsCount();
		for (int i = 0; i < argsCount; i++) {
			InsnArg arg = insn.getArg(i);
			if (arg.isInsnWrap()) {
				scanCoroutineRestoreInsns(((InsnWrapArg) arg).getWrapInsn(), state);
				if (state.hasRestore) {
					return;
				}
			}
		}
	}

	/**
	 * The pooled Ktor CIO reader joins its timeout and no-timeout read loops before deciding
	 * whether to close or refill the channel. Coroutine label branches can otherwise consume this
	 * join independently and duplicate the side-effecting close/recycle/socket-shutdown tail.
	 *
	 * Select only the unique two-input PHI join reached by both label branches, with the timeout
	 * stop call on one incoming edge. The current structural out can be before or after this join
	 * depending on whether coroutine completion normalization formed an outer dispatch loop.
	 */
	private @Nullable BlockNode findKtorCioReadTailJoin(
			BlockNode ifBlock, @Nullable BlockNode currentOut, BlockNode thenBlock, BlockNode elseBlock) {
		boolean targetMethod = KtorCioRecovery.isReadStateMachine(mth);
		if (currentOut == null
				|| !targetMethod
				|| !isCoroutineLabelIf(ifBlock)) {
			return null;
		}
		BlockNode match = null;
		for (BlockNode candidate : mth.getBasicBlocks()) {
			PhiListAttr phiList = candidate.get(AType.PHI_LIST);
			if (phiList == null
					|| phiList.getList().isEmpty()
					|| candidate.getPredecessors().size() != 2
					|| candidate.getCleanSuccessors().size() != 1
					|| BlockUtils.isExceptionHandlerPath(candidate)
					|| !hasTimeoutStopPredecessor(candidate)
					|| !isPathExists(thenBlock, candidate)
					|| !isPathExists(elseBlock, candidate)
					|| (!isPathExists(candidate, currentOut)
							&& !isPathExists(currentOut, candidate))) {
				continue;
			}
			if (match != null) {
				return null;
			}
			match = candidate;
		}
		return match;
	}

	private static boolean hasTimeoutStopPredecessor(BlockNode block) {
		for (BlockNode predecessor : block.getPredecessors()) {
			for (InsnNode insn : predecessor.getInstructions()) {
				if (insn instanceof InvokeNode
						&& KtorCioRecovery.isTimeoutStopInvoke((InvokeNode) insn)) {
					return true;
				}
			}
		}
		return false;
	}

	private @Nullable BlockNode findEarlierCoroutineLoopJoin(
			BlockNode ifBlock, @Nullable BlockNode currentOut, BlockNode thenBlock, BlockNode elseBlock) {
		CoroutineLoopPreHeaderAttr preHeaderAttr = mth.get(AType.COROUTINE_LOOP_PRE_HEADER);
		if (preHeaderAttr == null
				|| !isCoroutineLabelIf(ifBlock)
						&& !mth.contains(AType.TRY_PROTECTED_ITERATOR_BOOLEAN_ACTION)) {
			return null;
		}
		BlockNode candidate = preHeaderAttr.getBlock();
		PhiListAttr phiList = candidate.get(AType.PHI_LIST);
		if (phiList == null
				|| phiList.getList().isEmpty()
				|| candidate.getPredecessors().size() < 2
				|| candidate.getCleanSuccessors().size() != 1
				|| mth.getLoopForBlock(candidate) != null
				|| BlockUtils.isExceptionHandlerPath(candidate)
				|| !isPathExists(thenBlock, candidate)
				|| !isPathExists(elseBlock, candidate)
				|| currentOut != null && !isPathExists(candidate, currentOut)) {
			return null;
		}
		BlockNode loopStart = BlockUtils.followEmptyPath(candidate.getCleanSuccessors().get(0));
		return loopStart.contains(AFlag.LOOP_START) ? candidate : null;
	}

	private static boolean isCoroutineLabelIf(BlockNode block) {
		InsnNode lastInsn = BlockUtils.getLastInsn(block);
		if (!(lastInsn instanceof IfNode)) {
			return false;
		}
		for (InsnArg arg : lastInsn.getArguments()) {
			if (!(arg instanceof RegisterArg)) {
				continue;
			}
			RegisterArg registerArg = (RegisterArg) arg;
			if (registerArg.getSVar() == null) {
				continue;
			}
			InsnNode assignInsn = registerArg.getSVar().getAssignInsn();
			if (assignInsn instanceof IndexInsnNode
					&& assignInsn.getType() == InsnType.IGET
					&& ((IndexInsnNode) assignInsn).getIndex() instanceof FieldInfo
					&& ((FieldInfo) ((IndexInsnNode) assignInsn).getIndex()).getName().equals("label")) {
				return true;
			}
		}
		return false;
	}

	private @Nullable IfInfo restructurePhiAwareBranch(
			IfInfo info, BlockNode thenBlock, BlockNode elseBlock) {
		IfInfo sharedJoin = restructureMergedShortCircuitSharedJoin(info, thenBlock, elseBlock);
		if (sharedJoin != null) {
			return sharedJoin;
		}
		if ((isPhiNeutralLoopContinuation(info, thenBlock)
				|| isReadOnlySelectedLoopContinuation(thenBlock))
				&& regionMaker.getStack().containsExit(elseBlock)) {
			addContinueEdgesPreservingLoopCarry(info, thenBlock);
			IfInfo result = new IfInfo(info, thenBlock, null);
			result.setOutBlock(elseBlock);
			return result;
		}
		if ((isPhiNeutralLoopContinuation(info, elseBlock)
				|| isReadOnlySelectedLoopContinuation(elseBlock))
				&& regionMaker.getStack().containsExit(thenBlock)) {
			addContinueEdgesPreservingLoopCarry(info, elseBlock);
			IfInfo inverted = IfInfo.invert(info);
			IfInfo result = new IfInfo(inverted, elseBlock, null);
			result.setOutBlock(thenBlock);
			return result;
		}
		return null;
	}

	/**
	 * Keep a merged short-circuit condition when its successful side enters a PHI join shared by
	 * sibling switch paths and its other side is an inherited case boundary. The normal out-block
	 * search rejects both starts: the join has foreign predecessors, while the boundary belongs to
	 * the enclosing region. It is nevertheless safe to duplicate the join branch when all incoming
	 * PHI values from this condition are identical; the inherited boundary can remain the local out.
	 */
	private @Nullable IfInfo restructureMergedShortCircuitSharedJoin(
			IfInfo info, BlockNode thenBlock, BlockNode elseBlock) {
		if (info.getMergedBlocks().size() < 2
				|| !isInsideSwitchRegion(regionMaker.getStack().peekRegion())) {
			return null;
		}
		if (isSharedPhiNeutralJoin(info, thenBlock)
				&& !regionMaker.getStack().containsExit(thenBlock)
				&& regionMaker.getStack().containsExit(elseBlock)) {
			IfInfo result = new IfInfo(info, thenBlock, null);
			result.setOutBlock(elseBlock);
			return result;
		}
		if (isSharedPhiNeutralJoin(info, elseBlock)
				&& !regionMaker.getStack().containsExit(elseBlock)
				&& regionMaker.getStack().containsExit(thenBlock)) {
			IfInfo inverted = IfInfo.invert(info);
			IfInfo result = new IfInfo(inverted, elseBlock, null);
			result.setOutBlock(thenBlock);
			return result;
		}
		return null;
	}

	private static boolean isSharedPhiNeutralJoin(IfInfo info, BlockNode block) {
		if (!isPhiNeutralJoin(info, block)) {
			return false;
		}
		long mergedPredecessors = block.getPredecessors().stream()
				.filter(info.getMergedBlocks()::contains)
				.count();
		return block.getPredecessors().size() > mergedPredecessors;
	}

	/**
	 * Preserve a value-selecting IF inside a switch case when both successors are inherited case
	 * boundaries. The successors are PHI carrier joins which converge again at the enclosing switch
	 * exit. Keeping them as boundaries drops the condition; releasing only the direct carrier exits
	 * lets both bounded paths be represented under the IF while the common switch exit remains owned
	 * by the enclosing region.
	 */
	private @Nullable IfInfo restructureDualInheritedPhiBranches(
			BlockNode block, IfInfo info, BlockNode thenBlock, BlockNode elseBlock) {
		RegionStack stack = regionMaker.getStack();
		if (!isInsideSwitchRegion(stack.peekRegion())
				|| !hasPhiCarriers(thenBlock)
				|| !hasPhiCarriers(elseBlock)) {
			return null;
		}
		BlockNode commonOut = findCommonPostDominator(mth, thenBlock, elseBlock);
		if (commonOut == null
				|| commonOut == thenBlock
				|| commonOut == elseBlock
				|| !stack.containsExit(commonOut)) {
			return null;
		}
		Set<BlockNode> releasedExits = new HashSet<>();
		if (stack.containsExit(thenBlock)) {
			releasedExits.add(thenBlock);
		}
		if (stack.containsExit(elseBlock)) {
			releasedExits.add(elseBlock);
		}
		if (!allPathsReachOwnedOut(thenBlock, commonOut, releasedExits, stack,
				new HashSet<>(), new HashSet<>(), 0)
				|| !allPathsReachOwnedOut(elseBlock, commonOut, releasedExits, stack,
						new HashSet<>(), new HashSet<>(), 0)) {
			return null;
		}
		info.setOutBlock(commonOut);
		dualInheritedExitBranches.put(block, releasedExits);
		return info;
	}

	/**
	 * Preserve a merged short-circuit condition when its selected branch is an action shared with a
	 * sibling switch case and the other branch is the common case out. Switch region construction
	 * owns both boundaries, so the regular IF restructuring otherwise rolls the merge back and drops
	 * the second condition block.
	 *
	 * Releasing only inherited boundaries on the selected-to-out subgraph is equivalent to spelling
	 * the shared action in each mutually exclusive case. Require an external predecessor to prove the
	 * action is genuinely shared, and require every path to reach the same owned out before marking
	 * those source-level copies safe.
	 */
	private @Nullable IfInfo restructureMergedSwitchSharedAction(
			BlockNode block, IfInfo info, BlockNode thenBlock, BlockNode elseBlock) {
		if (info.getMergedBlocks().size() < 2
				|| !isInsideSwitchRegion(regionMaker.getStack().peekRegion())) {
			return null;
		}
		IfInfo result = tryMergedSwitchSharedAction(block, info, thenBlock, elseBlock, false);
		return result != null
				? result
				: tryMergedSwitchSharedAction(block, info, elseBlock, thenBlock, true);
	}

	private @Nullable IfInfo tryMergedSwitchSharedAction(
			BlockNode block, IfInfo info, BlockNode action, BlockNode out, boolean invert) {
		RegionStack stack = regionMaker.getStack();
		if (action == null
				|| out == null
				|| action == out
				|| !stack.containsExit(out)
				|| !isPathExists(action, out)
				|| isPathExists(out, action)
				|| !hasSharedCaseSideEffect(action, out, new HashSet<>())
				|| action.getPredecessors().stream().noneMatch(pred -> !info.getMergedBlocks().contains(pred))) {
			return null;
		}
		Set<BlockNode> releasedExits = new HashSet<>();
		for (BlockNode exit : stack.getExits()) {
			if (exit != out && isPathExists(action, exit) && isPathExists(exit, out)) {
				releasedExits.add(exit);
			}
		}
		if (!allPathsReachOwnedOut(action, out, releasedExits, stack,
				new HashSet<>(), new HashSet<>(), 0)) {
			return null;
		}
		Set<BlockNode> duplicatedPath = new HashSet<>();
		collectPathBeforeOut(action, out, duplicatedPath);
		for (BlockNode pathBlock : duplicatedPath) {
			regionMaker.registerSafeSwitchSharedDuplication(pathBlock);
		}
		IfInfo selectedInfo = invert ? IfInfo.invert(info) : info;
		IfInfo normalized = new IfInfo(selectedInfo, action, null);
		normalized.setOutBlock(out);
		if (!releasedExits.isEmpty()) {
			dualInheritedExitBranches.put(block, releasedExits);
		}
		return normalized;
	}

	private static boolean hasSharedCaseSideEffect(
			BlockNode block, BlockNode out, Set<BlockNode> visited) {
		if (block == out || !visited.add(block)) {
			return false;
		}
		for (InsnNode insn : block.getInstructions()) {
			switch (insn.getType()) {
				case IPUT:
				case SPUT:
				case APUT:
					return true;

				default:
					break;
			}
		}
		for (BlockNode successor : block.getCleanSuccessors()) {
			if (hasSharedCaseSideEffect(successor, out, visited)) {
				return true;
			}
		}
		return false;
	}

	private static void collectPathBeforeOut(BlockNode block, BlockNode out, Set<BlockNode> collected) {
		if (block == out || !collected.add(block)) {
			return;
		}
		for (BlockNode successor : block.getCleanSuccessors()) {
			collectPathBeforeOut(successor, out, collected);
		}
	}

	private static boolean isInsideSwitchRegion(@Nullable IRegion region) {
		IRegion current = region;
		while (current != null) {
			if (current instanceof SwitchRegion) {
				return true;
			}
			current = current.getParent();
		}
		return false;
	}

	private static boolean hasPhiCarriers(BlockNode block) {
		PhiListAttr phiList = block.get(AType.PHI_LIST);
		return phiList != null && !phiList.getList().isEmpty();
	}

	private static boolean allPathsReachOwnedOut(
			BlockNode block, BlockNode commonOut, Set<BlockNode> releasedExits, RegionStack stack,
			Set<BlockNode> visiting, Set<BlockNode> verified, int depth) {
		if (block == commonOut || verified.contains(block)) {
			return true;
		}
		if (depth > 64
				|| stack.containsExit(block) && !releasedExits.contains(block)
				|| block.contains(AFlag.LOOP_START)
				|| block.contains(AFlag.LOOP_END)
				|| !visiting.add(block)) {
			return false;
		}
		List<BlockNode> successors = block.getCleanSuccessors();
		if (successors.isEmpty()) {
			visiting.remove(block);
			return false;
		}
		for (BlockNode successor : successors) {
			if (!allPathsReachOwnedOut(successor, commonOut, releasedExits, stack,
					visiting, verified, depth + 1)) {
				visiting.remove(block);
				return false;
			}
		}
		visiting.remove(block);
		verified.add(block);
		return true;
	}

	/**
	 * A selection branch can carry only local values to a boolean PHI whose selected value
	 * immediately chooses the loop-continuation path. Emitting the read-only carrier blocks as a
	 * shared branch is impossible when they also have predecessors from sibling conditions. In that
	 * case the incoming edge itself is equivalent to {@code continue}.
	 */
	static boolean isReadOnlySelectedLoopContinuation(BlockNode start) {
		BlockNode block = start;
		BlockNode predecessor = null;
		Set<BlockNode> visited = new HashSet<>();
		while (block != null && visited.size() < 8 && visited.add(block)) {
			InsnNode lastInsn = BlockUtils.getLastInsn(block);
			if (lastInsn instanceof IfNode) {
				if (predecessor == null) {
					return false;
				}
				for (InsnNode insn : block.getInstructions()) {
					if (insn != lastInsn && !isReadOnlyInsn(insn)) {
						return false;
					}
				}
				BlockNode selected = selectBooleanPhiBranch(block, predecessor, (IfNode) lastInsn);
				if (selected == null) {
					return false;
				}
				BlockNode other = selected == ((IfNode) lastInsn).getThenBlock()
						? ((IfNode) lastInsn).getElseBlock()
						: ((IfNode) lastInsn).getThenBlock();
				return isSyntheticLoopContinuation(selected)
						&& !isSyntheticLoopContinuation(other);
			}
			for (InsnNode insn : block.getInstructions()) {
				if (!isReadOnlyInsn(insn)) {
					return false;
				}
			}
			List<BlockNode> successors = block.getCleanSuccessors();
			if (successors.size() != 1) {
				return false;
			}
			predecessor = block;
			block = successors.get(0);
		}
		return false;
	}

	private static @Nullable BlockNode selectBooleanPhiBranch(
			BlockNode join, BlockNode predecessor, IfNode ifInsn) {
		if (ifInsn.getOp() != IfOp.EQ && ifInsn.getOp() != IfOp.NE) {
			return null;
		}
		PhiListAttr phiList = join.get(AType.PHI_LIST);
		if (phiList == null) {
			return null;
		}
		for (int registerArgIndex = 0; registerArgIndex < 2; registerArgIndex++) {
			InsnArg registerArg = ifInsn.getArg(registerArgIndex);
			InsnArg literalArg = ifInsn.getArg(1 - registerArgIndex);
			if (!registerArg.isRegister() || !literalArg.isLiteral()
					|| !(registerArg instanceof RegisterArg)) {
				continue;
			}
			RegisterArg phiResult = (RegisterArg) registerArg;
			InsnNode assignInsn = phiResult.getSVar() == null
					? null
					: phiResult.getSVar().getAssignInsn();
			if (!(assignInsn instanceof PhiInsn)
					|| !phiList.getList().contains(assignInsn)) {
				continue;
			}
			PhiInsn phi = (PhiInsn) assignInsn;
			RegisterArg incoming = phi.getArgByBlock(predecessor);
			Boolean incomingValue = incoming == null
					? null
					: resolveBooleanLiteral(incoming, new HashSet<>());
			Boolean comparedValue = literalArg.isTrue()
					? Boolean.TRUE
					: literalArg.isFalse() ? Boolean.FALSE : null;
			if (incomingValue == null || comparedValue == null
					|| !isConditionOnlyPhi(phiResult, ifInsn)) {
				continue;
			}
			boolean equals = incomingValue.equals(comparedValue);
			boolean condition = ifInsn.getOp() == IfOp.EQ ? equals : !equals;
			return condition ? ifInsn.getThenBlock() : ifInsn.getElseBlock();
		}
		return null;
	}

	private static boolean isConditionOnlyPhi(RegisterArg phiResult, IfNode ifInsn) {
		for (RegisterArg use : phiResult.getSVar().getUseList()) {
			InsnNode parentInsn = use.getParentInsn();
			if (parentInsn != ifInsn && (parentInsn == null || !parentInsn.contains(AFlag.DONT_GENERATE))) {
				return false;
			}
		}
		return true;
	}

	private static @Nullable Boolean resolveBooleanLiteral(RegisterArg arg, Set<SSAVar> visited) {
		SSAVar var = arg.getSVar();
		if (var == null || !visited.add(var)) {
			return null;
		}
		InsnNode assignInsn = var.getAssignInsn();
		if (assignInsn == null) {
			return null;
		}
		if ((assignInsn.getType() == InsnType.CONST || assignInsn.getType() == InsnType.MOVE)
				&& assignInsn.getArgsCount() == 1) {
			InsnArg source = assignInsn.getArg(0);
			if (source.isTrue()) {
				return Boolean.TRUE;
			}
			if (source.isFalse()) {
				return Boolean.FALSE;
			}
			if (source.isRegister()) {
				return resolveBooleanLiteral((RegisterArg) source, visited);
			}
		}
		return null;
	}

	private @Nullable IfInfo restructureSyntheticLoopContinuation(
			IfInfo info, BlockNode thenBlock, BlockNode elseBlock) {
		boolean thenContinuation = isSyntheticLoopContinuation(thenBlock);
		boolean elseContinuation = isSyntheticLoopContinuation(elseBlock);
		if (thenContinuation == elseContinuation) {
			return null;
		}
		BlockNode continuation = thenContinuation ? thenBlock : elseBlock;
		BlockNode out = thenContinuation ? elseBlock : thenBlock;
		BlockNode carryTail = findSyntheticLoopContinuationTail(continuation);
		boolean loopPhiCarry = carryTail != null
				&& carryTail.getSuccessors().size() == 1
				&& (carryTail.contains(AType.COROUTINE_LOOP_CARRY_TAIL)
						|| isMoveOnlyLoopPhiCarry(carryTail, carryTail.getSuccessors().get(0)));
		if (carryTail == null || carryTail.getPredecessors().size() < 3 && !loopPhiCarry) {
			// A two-input ordinary latch is already handled by the regular region builder.
			// Treating it as an explicit continue drops the sibling condition body. Require either
			// a genuinely shared (3+ input) latch or an SSA-proven loop-carried assignment.
			return null;
		}
		if (carryTail != null
				&& !carryTail.getInstructions().isEmpty()
				&& !carryTail.contains(AType.COROUTINE_LOOP_CARRY_TAIL)
				&& !loopPhiCarry
				&& (carryTail.getInstructions().size() > 1
						|| hasOnlyConditionalLoopEndBridges(carryTail)
						|| isBranchValueJoin(continuation, out, carryTail))) {
			// A direct continue inserted before an ordinary instructionful latch skips its
			// shared side effects. Preserve the regular join only when every entry is a
			// conditional bridge, or when both branches contribute to a PHI consumed there.
			return null;
		}
		addContinueEdgesPreservingLoopCarry(info, continuation);
		IfInfo condition = thenContinuation ? info : IfInfo.invert(info);
		IfInfo result = new IfInfo(condition, continuation, null);
		result.setOutBlock(out);
		return result;
	}

	private static boolean hasOnlyConditionalLoopEndBridges(BlockNode loopEnd) {
		List<BlockNode> predecessors = loopEnd.getPredecessors();
		if (predecessors.size() < 2) {
			return false;
		}
		for (BlockNode predecessor : predecessors) {
			if (!predecessor.contains(AFlag.SYNTHETIC)
					|| !predecessor.getInstructions().isEmpty()
					|| predecessor.getPredecessors().size() != 1
					|| predecessor.getCleanSuccessors().size() != 1
					|| predecessor.getCleanSuccessors().get(0) != loopEnd
					|| !(BlockUtils.getLastInsn(predecessor.getPredecessors().get(0)) instanceof IfNode)) {
				return false;
			}
		}
		return true;
	}

	private boolean isBranchValueJoin(
			BlockNode continuation, BlockNode sibling, BlockNode join) {
		if (!hasIteratorSubtypeBranchReusePattern()) {
			return false;
		}
		PhiListAttr phiList = join.get(AType.PHI_LIST);
		if (phiList == null || phiList.getList().isEmpty()
				|| !isPathExists(continuation, join)
				|| !isPathExists(sibling, join)) {
			return false;
		}
		BlockNode continuationPred = findDirectJoinPredecessor(continuation, join);
		BlockNode siblingPred = findDirectJoinPredecessor(sibling, join);
		if (continuationPred == null || siblingPred == null || continuationPred == siblingPred) {
			return false;
		}
		for (PhiInsn phi : phiList.getList()) {
			if (phi.getArgByBlock(continuationPred) != null
					&& phi.getArgByBlock(siblingPred) != null) {
				return true;
			}
		}
		return false;
	}

	private boolean hasIteratorSubtypeBranchReusePattern() {
		Set<ArgType> iterableElementTypes = new HashSet<>();
		for (ArgType argType : mth.getArgTypes()) {
			List<ArgType> genericTypes = argType.getGenericTypes();
			if (genericTypes != null && genericTypes.size() == 1) {
				iterableElementTypes.add(genericTypes.get(0));
			}
		}
		if (iterableElementTypes.isEmpty()) {
			return false;
		}
		Map<Integer, Set<ArgType>> castsBySourceReg = new HashMap<>();
		boolean[] iteratorNext = { false };
		for (BlockNode block : mth.getBasicBlocks()) {
			for (InsnNode insn : block.getInstructions()) {
				insn.visitInsns(inner -> {
					if (inner instanceof InvokeNode) {
						InvokeNode invoke = (InvokeNode) inner;
						if (invoke.getCallMth().getName().equals("next")
								&& invoke.getCallMth().getDeclClass().getFullName()
										.equals("java.util.Iterator")) {
							iteratorNext[0] = true;
						}
					}
					if (inner instanceof IndexInsnNode
							&& inner.getType() == InsnType.CHECK_CAST
							&& inner.getArgsCount() == 1
							&& inner.getArg(0).isRegister()) {
						int regNum = ((RegisterArg) inner.getArg(0)).getRegNum();
						castsBySourceReg.computeIfAbsent(regNum, key -> new HashSet<>())
								.add(((IndexInsnNode) inner).getIndexAsType());
					}
					return null;
				});
			}
		}
		if (!iteratorNext[0]) {
			return false;
		}
		for (Set<ArgType> castTypes : castsBySourceReg.values()) {
			if (castTypes.size() >= 2) {
				return true;
			}
		}
		return false;
	}

	private static @Nullable BlockNode findDirectJoinPredecessor(BlockNode start, BlockNode join) {
		BlockNode block = start;
		Set<BlockNode> visited = new HashSet<>();
		while (block != join && visited.size() < 8 && visited.add(block)) {
			List<BlockNode> successors = block.getCleanSuccessors();
			if (successors.size() != 1) {
				return null;
			}
			BlockNode next = successors.get(0);
			if (next == join) {
				return block;
			}
			block = next;
		}
		return null;
	}

	private static boolean isSyntheticLoopContinuation(BlockNode start) {
		return findSyntheticLoopContinuationTail(start) != null;
	}

	private static @Nullable BlockNode findSyntheticLoopContinuationTail(BlockNode start) {
		if (start.getInstructions().stream().anyMatch(insn -> insn.getType() != InsnType.MOVE)) {
			return null;
		}
		BlockNode block = start;
		Set<BlockNode> visited = new HashSet<>();
		while (block != null && visited.size() < 8 && visited.add(block)) {
			if (block.contains(AFlag.LOOP_END)) {
				return block;
			}
			if (block.getInstructions().stream().anyMatch(insn -> insn.getType() != InsnType.MOVE)
					|| block.getSuccessors().size() != 1) {
				return null;
			}
			block = block.getSuccessors().get(0);
		}
		return null;
	}

	private static boolean isPhiNeutralLoopContinuation(IfInfo info, BlockNode start) {
		if (!isPhiNeutralJoin(info, start)) {
			return false;
		}
		BlockNode block = start;
		Set<BlockNode> visited = new HashSet<>();
		while (block != null && visited.size() < 12 && visited.add(block)) {
			if (block.contains(AFlag.LOOP_END)) {
				return true;
			}
			for (InsnNode insn : block.getInstructions()) {
				if (!isReadOnlyInsn(insn)) {
					return false;
				}
			}
			List<BlockNode> successors = block.getCleanSuccessors();
			if (successors.size() != 1) {
				return false;
			}
			block = successors.get(0);
		}
		return false;
	}

	private static void addContinueEdges(IfInfo info, BlockNode continuation) {
		for (BlockNode pred : continuation.getPredecessors()) {
			if (info.getMergedBlocks().contains(pred)) {
				EdgeInsnAttr.addEdgeInsn(pred, continuation, new InsnNode(InsnType.CONTINUE, 0));
			}
		}
	}

	private static void addContinueEdgesPreservingLoopCarry(IfInfo info, BlockNode continuation) {
		BlockNode carryTail = findSyntheticLoopContinuationTail(continuation);
		if (carryTail != null
				&& carryTail.getSuccessors().size() == 1
				&& (carryTail.contains(AType.COROUTINE_LOOP_CARRY_TAIL)
						|| isMoveOnlyLoopPhiCarry(carryTail, carryTail.getSuccessors().get(0)))) {
			// Keep loop-carried assignments before the synthetic continue. Inserting it on an
			// incoming bridge skips the values consumed by the loop-header PHI.
			EdgeInsnAttr.addEdgeInsn(
					carryTail,
					carryTail.getSuccessors().get(0),
					new InsnNode(InsnType.CONTINUE, 0));
			return;
		}
		addContinueEdges(info, continuation);
	}

	private static boolean isMoveOnlyLoopPhiCarry(BlockNode carryTail, BlockNode loopHeader) {
		if (carryTail.getInstructions().isEmpty()
				|| carryTail.getInstructions().stream().anyMatch(insn -> insn.getType() != InsnType.MOVE)) {
			return false;
		}
		PhiListAttr phiList = loopHeader.get(AType.PHI_LIST);
		if (phiList == null || phiList.getList().isEmpty()) {
			return false;
		}
		for (InsnNode moveInsn : carryTail.getInstructions()) {
			RegisterArg result = moveInsn.getResult();
			if (result == null) {
				return false;
			}
			boolean feedsLoopPhi = phiList.getList().stream()
					.map(phi -> phi.getArgByBlock(carryTail))
					.anyMatch(arg -> arg != null && result.sameRegAndSVar(arg));
			if (!feedsLoopPhi) {
				return false;
			}
		}
		return true;
	}

	private static boolean isPhiNeutralJoin(IfInfo info, BlockNode join) {
		PhiListAttr phiList = join.get(AType.PHI_LIST);
		if (phiList == null || phiList.getList().isEmpty()) {
			return false;
		}
		List<BlockNode> mergedPreds = join.getPredecessors().stream()
				.filter(info.getMergedBlocks()::contains)
				.toList();
		if (mergedPreds.size() < 2) {
			return false;
		}
		for (PhiInsn phi : phiList.getList()) {
			RegisterArg firstArg = phi.getArgByBlock(mergedPreds.get(0));
			if (firstArg == null) {
				return false;
			}
			for (int i = 1; i < mergedPreds.size(); i++) {
				RegisterArg arg = phi.getArgByBlock(mergedPreds.get(i));
				if (arg == null || arg.getSVar() != firstArg.getSVar()) {
					return false;
				}
			}
		}
		return true;
	}

	private @Nullable IfInfo restructureInheritedExit(
			IfInfo info, BlockNode thenBlock, BlockNode elseBlock, boolean badThen, boolean badElse) {
		BlockNode localOut = info.getOutBlock();
		if (localOut == null) {
			return null;
		}
		BlockNode nestedBranch;
		BlockNode terminalBranch;
		if (badElse && elseBlock == localOut && isAcyclicTerminalSubgraph(elseBlock)) {
			nestedBranch = thenBlock;
			terminalBranch = elseBlock;
		} else if (badThen && thenBlock == localOut && isAcyclicTerminalSubgraph(thenBlock)) {
			nestedBranch = elseBlock;
			terminalBranch = thenBlock;
		} else {
			return null;
		}
		for (BlockNode exit : regionMaker.getStack().getExits()) {
			if (exit != localOut
					&& BlockUtils.isPathExists(nestedBranch, exit)
					&& !BlockUtils.isPathExists(terminalBranch, exit)) {
				info.setOutBlock(exit);
				return info;
			}
		}
		return null;
	}

	private @Nullable IfInfo restructureDirectScopeExit(IfInfo info, BlockNode thenBlock, BlockNode elseBlock) {
		return restructureDirectScopeExit(info, thenBlock, elseBlock, 12, false);
	}

	private @Nullable IfInfo restructureDeepDirectScopeExit(IfInfo info, BlockNode thenBlock, BlockNode elseBlock) {
		return restructureDirectScopeExit(info, thenBlock, elseBlock, 32, true);
	}

	private @Nullable IfInfo restructureDirectScopeExit(
			IfInfo info, BlockNode thenBlock, BlockNode elseBlock, int maxDepth, boolean deepOnly) {
		for (BlockNode exit : regionMaker.getStack().getExits()) {
			if (exit == info.getOutBlock()) {
				// This is the local join selected for this if. Prefer an inherited scope exit;
				// otherwise the shared terminal branch is incorrectly emitted after an empty if.
				continue;
			}
			if (thenBlock == exit && isDirectScopeTerminal(elseBlock, maxDepth, deepOnly)) {
				IfInfo inverted = IfInfo.invert(info);
				IfInfo result = new IfInfo(inverted, elseBlock, null);
				result.setOutBlock(exit);
				return result;
			}
			if (elseBlock == exit && isDirectScopeTerminal(thenBlock, maxDepth, deepOnly)) {
				IfInfo result = new IfInfo(info, thenBlock, null);
				result.setOutBlock(exit);
				return result;
			}
		}
		return null;
	}

	private static boolean isDirectScopeTerminal(BlockNode block, int maxDepth, boolean deepOnly) {
		return (!deepOnly || !isAcyclicTerminalSubgraph(block))
				&& isAcyclicTerminalSubgraph(block, maxDepth);
	}

	private static boolean isLinearTerminalPath(BlockNode startBlock) {
		BlockNode block = startBlock;
		Set<BlockNode> visited = new HashSet<>();
		while (block != null && visited.size() < 8 && visited.add(block)) {
			if (block.contains(AFlag.LOOP_START) || block.contains(AFlag.LOOP_END)) {
				return false;
			}
			if (BlockUtils.containsExitInsn(block)) {
				return true;
			}
			List<BlockNode> successors = block.getCleanSuccessors();
			if (successors.size() != 1) {
				return false;
			}
			block = successors.get(0);
		}
		return false;
	}

	private static boolean isAcyclicTerminalSubgraph(BlockNode startBlock) {
		return isAcyclicTerminalSubgraph(startBlock, 12);
	}

	static boolean isAcyclicTerminalSubgraph(BlockNode startBlock, int maxDepth) {
		ArrayDeque<TerminalSubgraphState> pool = TERMINAL_SUBGRAPH_STATE_POOL.get();
		TerminalSubgraphState state = pool.pollLast();
		if (state == null) {
			state = new TerminalSubgraphState();
		}
		try {
			return isAcyclicTerminalSubgraph(startBlock, state.visiting, state.terminal, 0, maxDepth);
		} finally {
			state.clear();
			pool.addLast(state);
		}
	}

	private static boolean isAcyclicTerminalSubgraph(
			BlockNode block, BitSet visiting, BitSet terminal, int depth, int maxDepth) {
		int blockPos = block.getPos();
		if (terminal.get(blockPos)) {
			return true;
		}
		if (depth >= maxDepth
				|| block.contains(AFlag.LOOP_START)
				|| block.contains(AFlag.LOOP_END)
				|| visiting.get(blockPos)) {
			return false;
		}
		visiting.set(blockPos);
		if (BlockUtils.containsExitInsn(block)) {
			visiting.clear(blockPos);
			terminal.set(blockPos);
			return true;
		}
		List<BlockNode> successors = block.getCleanSuccessors();
		int successorsCount = successors.size();
		if (successorsCount == 0) {
			visiting.clear(blockPos);
			return false;
		}
		if (successorsCount == 1) {
			BlockNode successor = successors.get(0);
			if (!isAcyclicTerminalSubgraph(successor, visiting, terminal, depth + 1, maxDepth)) {
				visiting.clear(blockPos);
				return false;
			}
		} else {
			for (BlockNode successor : successors) {
				if (!isAcyclicTerminalSubgraph(successor, visiting, terminal, depth + 1, maxDepth)) {
					visiting.clear(blockPos);
					return false;
				}
			}
		}
		visiting.clear(blockPos);
		terminal.set(blockPos);
		return true;
	}

	/**
	 * Both branches can have external predecessors in a state machine: the return block is shared by
	 * several suspension checks and the other branch is also a resume target. This is still a regular
	 * early-return condition and can be represented without traversing the shared continuation as a
	 * branch.
	 */
	private @Nullable IfInfo restructureSharedReturn(IfInfo info, BlockNode thenBlock, BlockNode elseBlock) {
		boolean allowTerminalSubgraph = isSuspendLambdaMethod() && isBooleanCondition(info);
		boolean thenReturn = isBranchReturn(thenBlock)
				|| isLinearTerminalPath(thenBlock)
				|| allowTerminalSubgraph && isAcyclicTerminalSubgraph(thenBlock);
		boolean elseReturn = isBranchReturn(elseBlock)
				|| isLinearTerminalPath(elseBlock)
				|| allowTerminalSubgraph && isAcyclicTerminalSubgraph(elseBlock);
		if (thenReturn == elseReturn) {
			return null;
		}
		if (elseReturn) {
			info = IfInfo.invert(info);
			BlockNode tmp = thenBlock;
			thenBlock = elseBlock;
			elseBlock = tmp;
		}
		IfInfo result = new IfInfo(info, thenBlock, null);
		result.setOutBlock(elseBlock);
		return result;
	}

	private static boolean isBooleanCondition(IfInfo info) {
		InsnNode lastInsn = BlockUtils.getLastInsn(info.getFirstIfBlock());
		return lastInsn instanceof IfNode
				&& lastInsn.getArgsCount() != 0
				&& ArgType.BOOLEAN.equals(lastInsn.getArg(0).getType());
	}

	private static @Nullable IfInfo restructureSharedOut(IfInfo info, BlockNode thenBlock, BlockNode elseBlock) {
		BlockNode outBlock = info.getOutBlock();
		boolean exceptionJoin = isExceptionJoin(info);
		if (outBlock == null) {
			if (isSharedOutPath(thenBlock, elseBlock, exceptionJoin)) {
				outBlock = elseBlock;
			} else if (isSharedOutPath(elseBlock, thenBlock, exceptionJoin)) {
				outBlock = thenBlock;
			} else {
				return null;
			}
		}
		if (thenBlock == outBlock && isSharedOutPath(elseBlock, outBlock, exceptionJoin)) {
			info = IfInfo.invert(info);
			BlockNode tmp = thenBlock;
			thenBlock = elseBlock;
			elseBlock = tmp;
		}
		if (elseBlock != outBlock || !isSharedOutPath(thenBlock, outBlock, exceptionJoin)) {
			return null;
		}
		IfInfo result = new IfInfo(info, thenBlock, null);
		result.setOutBlock(outBlock);
		return result;
	}

	private static boolean isExceptionJoin(IfInfo info) {
		for (BlockNode block : info.getMergedBlocks()) {
			for (BlockNode predecessor : block.getPredecessors()) {
				if (BlockUtils.isExceptionHandlerPath(predecessor)) {
					return true;
				}
			}
		}
		return false;
	}

	private static boolean isSharedOutPath(BlockNode startBlock, BlockNode outBlock, boolean allowSideEffects) {
		return allowSideEffects ? isLinearPath(startBlock, outBlock) : isLinearReadOnlyPath(startBlock, outBlock);
	}

	private static boolean isLinearPath(BlockNode startBlock, BlockNode outBlock) {
		BlockNode block = startBlock;
		Set<BlockNode> visited = new HashSet<>();
		while (block != outBlock && block != null && visited.size() < 8 && visited.add(block)) {
			if (block.contains(AFlag.LOOP_START) || block.contains(AFlag.LOOP_END)) {
				return false;
			}
			List<BlockNode> successors = block.getCleanSuccessors();
			if (successors.size() != 1) {
				return false;
			}
			block = successors.get(0);
		}
		return block == outBlock;
	}

	private static boolean isLinearReadOnlyPath(BlockNode startBlock, BlockNode outBlock) {
		BlockNode block = startBlock;
		Set<BlockNode> visited = new HashSet<>();
		while (block != outBlock && block != null && visited.size() < 8 && visited.add(block)) {
			if (block.contains(AFlag.LOOP_START) || block.contains(AFlag.LOOP_END)) {
				return false;
			}
			for (InsnNode insn : block.getInstructions()) {
				if (!isReadOnlyInsn(insn)) {
					return false;
				}
			}
			List<BlockNode> successors = block.getCleanSuccessors();
			if (successors.size() != 1) {
				return false;
			}
			block = successors.get(0);
		}
		return block == outBlock;
	}

	private static @Nullable BlockNode findReadOnlyBranchJoin(BlockNode firstBlock, BlockNode secondBlock) {
		List<BlockNode> firstReachable = collectReadOnlyReachable(firstBlock);
		List<BlockNode> secondReachable = collectReadOnlyReachable(secondBlock);
		BlockNode best = null;
		int bestMaxDistance = Integer.MAX_VALUE;
		int bestTotalDistance = Integer.MAX_VALUE;
		for (int firstDistance = 0; firstDistance < firstReachable.size(); firstDistance++) {
			BlockNode candidate = firstReachable.get(firstDistance);
			int secondDistance = secondReachable.indexOf(candidate);
			if (secondDistance == -1
					|| !allReadOnlyPathsReach(firstBlock, candidate)
					|| !allReadOnlyPathsReach(secondBlock, candidate)) {
				continue;
			}
			int maxDistance = Math.max(firstDistance, secondDistance);
			int totalDistance = firstDistance + secondDistance;
			if (maxDistance < bestMaxDistance
					|| maxDistance == bestMaxDistance && totalDistance < bestTotalDistance) {
				best = candidate;
				bestMaxDistance = maxDistance;
				bestTotalDistance = totalDistance;
			}
		}
		return best;
	}

	private boolean hasByteContinuationClassifier() {
		if (byteContinuationClassifier != null) {
			return byteContinuationClassifier;
		}
		if (!mth.getMethodInfo().getArgumentsTypes().contains(ArgType.array(ArgType.BYTE))) {
			byteContinuationClassifier = Boolean.FALSE;
			return false;
		}
		int maskCount = 0;
		for (BlockNode block : mth.getBasicBlocks()) {
			for (InsnNode insn : block.getInstructions()) {
				Boolean hasMask = insn.visitInsns(innerInsn -> {
					if (!(innerInsn instanceof ArithNode)
							|| ((ArithNode) innerInsn).getOp() != ArithOp.AND) {
						return null;
					}
					for (InsnArg arg : innerInsn.getArguments()) {
						if (arg.isLiteral() && ((LiteralArg) arg).getLiteral() == 192) {
							return Boolean.TRUE;
						}
					}
					return null;
				});
				if (hasMask != null && ++maskCount >= 2) {
					byteContinuationClassifier = Boolean.TRUE;
					return true;
				}
			}
		}
		byteContinuationClassifier = Boolean.FALSE;
		return false;
	}

	private static boolean isSharedValueCarrierBlock(BlockNode block) {
		boolean hasCarrier = false;
		for (InsnNode insn : block.getInstructions()) {
			if (insn.contains(AFlag.DONT_GENERATE)) {
				continue;
			}
			InsnType type = insn.getType();
			if (type != InsnType.MOVE
					&& type != InsnType.MOVE_MULTI
					&& type != InsnType.CONST
					&& type != InsnType.PHI) {
				return false;
			}
			hasCarrier = true;
		}
		return hasCarrier;
	}

	private static BlockNode moveOutPastSharedBypass(
			BlockNode thenBlock, BlockNode elseBlock, BlockNode outBlock) {
		BlockNode current = outBlock;
		Set<BlockNode> visited = new HashSet<>();
		while (visited.size() < 8
				&& visited.add(current)
				&& isReadOnlyControlBlock(current)) {
			List<BlockNode> successors = current.getCleanSuccessors();
			if (successors.size() != 1) {
				break;
			}
			BlockNode successor = successors.get(0);
			if (RegionMaker.allCleanPathsReach(thenBlock, successor, Set.of())
					&& RegionMaker.allCleanPathsReach(elseBlock, successor, Set.of())
					&& (isPathExistsAvoiding(thenBlock, successor, outBlock)
							|| isPathExistsAvoiding(elseBlock, successor, outBlock))) {
				return successor;
			}
			if (successor.contains(AFlag.LOOP_START) || successor.contains(AFlag.LOOP_END)) {
				break;
			}
			current = successor;
		}
		return outBlock;
	}

	/**
	 * {@link #findOutBlock(MethodNode, BlockNode, BlockNode)} can select an optional action as the
	 * first cross-path block when one branch enters that action and another can either enter it or
	 * bypass it. The action's single successor is the actual common tail. Keeping the action as the
	 * IF out makes region construction emit the successor once after the action and again on the
	 * bypass path, which is especially harmful when that successor starts an inner loop.
	 *
	 * Move the boundary only when the successor is a pre-header for a common inner loop and with a
	 * complete path proof: both branches must reach that pre-header and at least one must do so
	 * without crossing the selected out block. Side effects in the optional action remain inside
	 * their original branch; no instruction or edge is changed.
	 */
	private static @Nullable BlockNode moveOutPastPartiallyBypassedBranchAction(
			BlockNode thenBlock, BlockNode elseBlock, @Nullable BlockNode outBlock) {
		if (outBlock == null
				|| outBlock.contains(AFlag.LOOP_START)
				|| outBlock.contains(AFlag.LOOP_END)
				|| BlockUtils.isExceptionHandlerPath(outBlock)) {
			return outBlock;
		}
		List<BlockNode> successors = outBlock.getCleanSuccessors();
		if (successors.size() != 1) {
			return outBlock;
		}
		BlockNode successor = successors.get(0);
		if (successor.getPredecessors().size() < 2
				|| successor.contains(AFlag.LOOP_START)
				|| successor.contains(AFlag.LOOP_END)
				|| BlockUtils.isExceptionHandlerPath(successor)
				|| successor.getCleanSuccessors().size() != 1
				|| !successor.getCleanSuccessors().get(0).contains(AFlag.LOOP_START)
				|| !RegionMaker.allCleanPathsReach(thenBlock, successor, Set.of())
				|| !RegionMaker.allCleanPathsReach(elseBlock, successor, Set.of())) {
			return outBlock;
		}
		return isPathExistsAvoiding(thenBlock, successor, outBlock)
				|| isPathExistsAvoiding(elseBlock, successor, outBlock)
						? successor
						: outBlock;
	}

	private static boolean isPathExistsAvoiding(
			BlockNode startBlock, BlockNode targetBlock, BlockNode excludedBlock) {
		if (startBlock == excludedBlock) {
			return false;
		}
		List<BlockNode> stack = new ArrayList<>();
		Set<BlockNode> visited = new HashSet<>();
		stack.add(startBlock);
		while (!stack.isEmpty() && visited.size() < 128) {
			BlockNode block = stack.remove(stack.size() - 1);
			if (block == targetBlock) {
				return true;
			}
			if (block == excludedBlock || !visited.add(block)) {
				continue;
			}
			stack.addAll(block.getCleanSuccessors());
		}
		return false;
	}

	private static List<BlockNode> collectReadOnlyReachable(BlockNode startBlock) {
		List<BlockNode> reachable = new ArrayList<>();
		reachable.add(startBlock);
		for (int i = 0; i < reachable.size() && reachable.size() < 24; i++) {
			BlockNode block = reachable.get(i);
			if (!isReadOnlyControlBlock(block)) {
				continue;
			}
			for (BlockNode successor : block.getCleanSuccessors()) {
				if (!reachable.contains(successor)) {
					reachable.add(successor);
					if (reachable.size() == 24) {
						break;
					}
				}
			}
		}
		return reachable;
	}

	private static boolean allReadOnlyPathsReach(BlockNode startBlock, BlockNode outBlock) {
		return allReadOnlyPathsReach(startBlock, outBlock, new HashSet<>(), new HashSet<>(), new int[] { 0 });
	}

	private static boolean allReadOnlyPathsReach(
			BlockNode block, BlockNode outBlock,
			Set<BlockNode> visiting, Set<BlockNode> verified, int[] visitedCount) {
		if (block == outBlock || verified.contains(block)) {
			return true;
		}
		if (visitedCount[0]++ == 24 || !visiting.add(block) || !isReadOnlyControlBlock(block)) {
			return false;
		}
		List<BlockNode> successors = block.getCleanSuccessors();
		if (successors.isEmpty()) {
			visiting.remove(block);
			return false;
		}
		for (BlockNode successor : successors) {
			if (!allReadOnlyPathsReach(successor, outBlock, visiting, verified, visitedCount)) {
				visiting.remove(block);
				return false;
			}
		}
		visiting.remove(block);
		verified.add(block);
		return true;
	}

	private static boolean isReadOnlyControlBlock(BlockNode block) {
		if (block.contains(AFlag.LOOP_START) || block.contains(AFlag.LOOP_END)) {
			return false;
		}
		for (InsnNode insn : block.getInstructions()) {
			InsnType type = insn.getType();
			if (type != InsnType.IF && type != InsnType.SWITCH && !isReadOnlyInsn(insn)) {
				return false;
			}
		}
		return true;
	}

	private static boolean isReadOnlyInsn(InsnNode insn) {
		Boolean invalid = insn.visitInsns(innerInsn -> isReadOnlyInsnType(innerInsn) ? null : Boolean.TRUE);
		return invalid == null;
	}

	private static boolean isReadOnlyInsnType(InsnNode insn) {
		if (insn.contains(AFlag.DONT_GENERATE)) {
			return true;
		}
		switch (insn.getType()) {
			case CONST:
			case CONST_STR:
			case CONST_CLASS:
			case ARITH:
			case NEG:
			case NOT:
			case MOVE:
			case MOVE_MULTI:
			case CAST:
			case CHECK_CAST:
			case INSTANCE_OF:
			case ARRAY_LENGTH:
			case AGET:
			case IGET:
			case SGET:
			case PHI:
				return true;
			default:
				return false;
		}
	}

	private boolean isBranchReturn(BlockNode block) {
		return block.contains(AFlag.RETURN) || isSuspendLambdaMethod() && isReturnPath(block);
	}

	private boolean isSuspendLambdaMethod() {
		return CoroutineMethodUtils.isSuspendLambdaBody(mth);
	}

	private static boolean isReturnPath(@Nullable BlockNode startBlock) {
		BlockNode block = startBlock;
		Set<BlockNode> visited = new HashSet<>();
		while (block != null && visited.add(block)) {
			if (block.contains(AFlag.RETURN)) {
				return true;
			}
			for (InsnNode insn : block.getInstructions()) {
				if (!insn.contains(AFlag.DONT_GENERATE)) {
					return false;
				}
			}
			List<BlockNode> successors = block.getCleanSuccessors();
			if (successors.size() != 1) {
				return false;
			}
			block = successors.get(0);
		}
		return false;
	}

	static @Nullable BlockNode findOutBlock(MethodNode mth, BlockNode thenBlock, BlockNode elseBlock) {
		if (thenBlock == elseBlock) {
			return thenBlock;
		}
		if (thenBlock == null || elseBlock == null) {
			return null;
		}

		BitSet thenDomFrontier = newBlocksBitSet(mth);
		thenDomFrontier.or(thenBlock.getDomFrontier());
		thenDomFrontier.set(thenBlock.getPos());

		BitSet elseDomFrontier = newBlocksBitSet(mth);
		elseDomFrontier.or(elseBlock.getDomFrontier());
		elseDomFrontier.set(elseBlock.getPos());

		BitSet intersection = newBlocksBitSet(mth);
		intersection.or(thenDomFrontier);
		intersection.and(elseDomFrontier);

		intersection.clear(mth.getExitBlock().getPos());
		BlockNode oneBlock = bitSetToOneBlock(mth, intersection);

		// Attempt one: there's a unique block in the intersection of dom frontiers, and no path from
		// then->else or else->then
		if (oneBlock != null) {
			boolean methodWithLoops = mth.getLoopsCount() != 0;
			if (methodWithLoops
					&& (!isPrimitiveValueMethod(mth)
							|| !hasBooleanPhi(oneBlock.get(AType.PHI_LIST)))) {
				return oneBlock;
			}
			if (isCommonPostDominator(mth, thenBlock, elseBlock, oneBlock)) {
				return oneBlock;
			}
			BlockNode deeperOut = findCommonPostDominator(mth, thenBlock, elseBlock);
			PhiListAttr deeperPhi = deeperOut == null ? null : deeperOut.get(AType.PHI_LIST);
			if (deeperOut != null
					&& (!methodWithLoops || hasBooleanPhi(deeperPhi))) {
				return deeperOut;
			}
			if (methodWithLoops) {
				return oneBlock;
			}
		}

		BitSet union = newBlocksBitSet(mth);
		union.or(thenBlock.getDomFrontier());
		union.or(elseBlock.getDomFrontier());
		union.clear(mth.getExitBlock().getPos());

		// Attempt two: look for a suitable block in the union.
		BitSet candidates = newBlocksBitSet(mth);
		for (BlockNode candidate : bitSetToBlocks(mth, union)) {
			if (isCandidateForOutBlock(mth, thenBlock, elseBlock, candidate)) {
				candidates.set(candidate.getPos());
			}
		}

		BlockNode bottom = getBottomBlock(bitSetToBlocks(mth, candidates), true);
		if (bottom != null) {
			return bottom;
		}

		// Attempt three: fallback to path cross again
		return getPathCross(mth, thenBlock, elseBlock);
	}

	private static boolean isPrimitiveValueMethod(MethodNode mth) {
		ArgType returnType = mth.getReturnType();
		return returnType.isPrimitive() && !ArgType.VOID.equals(returnType);
	}

	private static boolean hasBooleanPhi(@Nullable PhiListAttr phiList) {
		return phiList != null
				&& phiList.getList().stream()
						.anyMatch(phiInsn -> ArgType.BOOLEAN.equals(phiInsn.getResult().getType()));
	}

	static boolean isCommonPostDominator(
			BlockNode thenBlock, BlockNode elseBlock, BlockNode candidate) {
		return isCommonPostDominator(null, thenBlock, elseBlock, candidate);
	}

	private static boolean isCommonPostDominator(
			@Nullable MethodNode mth, BlockNode thenBlock, BlockNode elseBlock, BlockNode candidate) {
		// A branch starting at the join passes through it by definition. A terminal path in the
		// opposite branch must not push a normal continuation to a later, unrelated join.
		if (candidate == thenBlock || candidate == elseBlock) {
			return true;
		}
		BlockNode exitBlock = mth == null ? null : mth.getExitBlock();
		return !hasTerminalPathAvoiding(thenBlock, candidate, exitBlock)
				&& !hasTerminalPathAvoiding(elseBlock, candidate, exitBlock);
	}

	private static boolean hasTerminalPathAvoiding(
			BlockNode start, BlockNode excluded, @Nullable BlockNode exitBlock) {
		if (start == excluded) {
			return false;
		}
		BitSet visited = new BitSet();
		List<BlockNode> stack = new ArrayList<>();
		stack.add(start);
		while (!stack.isEmpty()) {
			BlockNode block = stack.remove(stack.size() - 1);
			if (block == excluded || visited.get(block.getPos())) {
				continue;
			}
			visited.set(block.getPos());
			List<BlockNode> successors = block.getCleanSuccessors();
			if (block == exitBlock || successors.isEmpty()) {
				return true;
			}
			int successorsCount = successors.size();
			if (successorsCount == 1) {
				stack.add(successors.get(0));
			} else {
				stack.addAll(successors);
			}
		}
		return false;
	}

	private static @Nullable BlockNode findCommonPostDominator(
			MethodNode mth, BlockNode thenBlock, BlockNode elseBlock) {
		List<BlockNode> common = new ArrayList<>();
		for (BlockNode candidate : mth.getBasicBlocks()) {
			if (candidate == mth.getExitBlock()
					|| candidate.getPredecessors().size() < 2
					|| !isPathExists(thenBlock, candidate)
					|| !isPathExists(elseBlock, candidate)) {
				continue;
			}
			if (isCommonPostDominator(mth, thenBlock, elseBlock, candidate)) {
				common.add(candidate);
			}
		}
		for (BlockNode candidate : common) {
			boolean first = true;
			for (BlockNode other : common) {
				if (candidate != other && !isPathExists(candidate, other)) {
					first = false;
					break;
				}
			}
			if (first) {
				return candidate;
			}
		}
		return null;
	}

	static boolean isCandidateForOutBlock(MethodNode mth, BlockNode thenBlock, BlockNode elseBlock, BlockNode candidate) {
		// a candidate block requires:
		// - >1 predecessor
		// - each predecessor has a clean path from elseBlock or thenBlock, and there exist predecessors
		// covering both cases
		// - inside the union of the two dom frontiers

		if (candidate.getPredecessors().size() < 2) {
			return false; // block has only one pred, and so can't be the outblock
		}

		BitSet coverageThenPreds = newBlocksBitSet(mth);
		BitSet coverageElsePreds = newBlocksBitSet(mth);

		if (candidate == elseBlock) {
			coverageElsePreds.set(candidate.getPos());
		}
		if (candidate == thenBlock) {
			coverageThenPreds.set(candidate.getPos());
		}

		for (BlockNode pred : candidate.getPredecessors()) {
			if (isPathExists(thenBlock, pred)) {
				coverageThenPreds.set(pred.getPos());
			}

			if (isPathExists(elseBlock, pred)) {
				coverageElsePreds.set(pred.getPos());
			}
		}
		if (coverageElsePreds.cardinality() == 0 || coverageThenPreds.cardinality() == 0) {
			return false; // block has no path to both the then and else blocks
		}

		BlockNode coverageElsePred = bitSetToOneBlock(mth, coverageElsePreds);
		BlockNode coverageThenPred = bitSetToOneBlock(mth, coverageThenPreds);
		if (coverageElsePred != null && coverageElsePred == coverageThenPred) {
			return false; // the only paths from else and then go through the same block
		}

		return true;
	}

	private static boolean isBadBranchBlock(IfInfo info, BlockNode block) {
		// check if block at end of loop edge
		if (block.contains(AFlag.LOOP_START) && block.getPredecessors().size() == 1) {
			BlockNode pred = block.getPredecessors().get(0);
			if (pred.contains(AFlag.LOOP_END)) {
				List<LoopInfo> startLoops = block.getAll(AType.LOOP);
				List<LoopInfo> endLoops = pred.getAll(AType.LOOP);
				// search for same loop
				for (LoopInfo startLoop : startLoops) {
					for (LoopInfo endLoop : endLoops) {
						if (startLoop == endLoop) {
							return true;
						}
					}
				}
			}
		}
		// if branch block itself is outblock
		if (info.getOutBlock() != null) {
			return block == info.getOutBlock();
		}
		return !allPathsFromIf(block, info);
	}

	private static boolean allPathsFromIf(BlockNode block, IfInfo info) {
		List<BlockNode> preds = block.getPredecessors();
		BlockSet ifBlocks = info.getMergedBlocks();
		for (BlockNode pred : preds) {
			if (pred.contains(AFlag.LOOP_END)) {
				// ignore loop back edge
				continue;
			}
			BlockNode top = BlockUtils.skipSyntheticPredecessor(pred);
			if (!ifBlocks.contains(top)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * if startBlock is in a (try) scope, find the scope end as outBlock
	 */
	private @Nullable BlockNode findScopeOutBlock(BlockNode startBlock) {
		if (startBlock == null) {
			return null;
		}
		List<BlockNode> domFrontiers = BlockUtils.bitSetToBlocks(mth, startBlock.getDomFrontier());
		BlockNode scopeOutBlock = null;

		// find handler from domFrontier(could be scope end), if domFrontier is handler
		// and its topSplitter dominates branch block, then branch should end
		for (BlockNode domFrontier : domFrontiers) {
			ExcHandlerAttr handler = domFrontier.get(AType.EXC_HANDLER);
			if (handler == null) {
				continue;
			}
			BlockNode topSplitter = handler.getTryBlock().getTopSplitter();
			if (startBlock.isDominator(topSplitter)) {
				scopeOutBlock = BlockUtils.getTryAndHandlerCrossBlock(mth, handler.getHandler());
				break;
			}
		}
		if (scopeOutBlock != null) {
			// check if out block still inside scope limited by 'exit' blocks
			for (BlockNode exit : regionMaker.getStack().getExits()) {
				if (BlockUtils.isPathExists(exit, scopeOutBlock)) {
					return null;
				}
			}
		}
		return scopeOutBlock;
	}

	static IfInfo mergeNestedIfNodes(IfInfo currentIf) {
		BlockNode curThen = currentIf.getThenBlock();
		BlockNode curElse = currentIf.getElseBlock();
		if (curThen == curElse) {
			return null;
		}
		IfInfo diamondMerge = mergeBooleanDiamondBranch(currentIf, curThen, true);
		if (diamondMerge == null) {
			diamondMerge = mergeBooleanDiamondBranch(currentIf, curElse, false);
		}
		if (diamondMerge != null) {
			return searchNestedIf(diamondMerge);
		}
		if (BlockUtils.isFollowBackEdge(curThen)
				|| BlockUtils.isFollowBackEdge(curElse)) {
			return null;
		}
		boolean followThenBranch;
		IfInfo nextIf = getNextIf(currentIf, curThen);
		if (nextIf != null) {
			followThenBranch = true;
		} else {
			nextIf = getNextIf(currentIf, curElse);
			if (nextIf != null) {
				followThenBranch = false;
			} else {
				return null;
			}
		}

		boolean assignInlineNeeded = !nextIf.getForceInlineInsns().isEmpty();
		if (assignInlineNeeded) {
			for (BlockNode mergedBlock : currentIf.getMergedBlocks()) {
				if (mergedBlock.contains(AFlag.LOOP_START)) {
					// don't inline assigns into loop condition
					return currentIf;
				}
			}
		}

		if (isInversionNeeded(currentIf, nextIf)) {
			// invert current node for match pattern
			nextIf = IfInfo.invert(nextIf);
		}
		boolean thenPathSame = isEqualPaths(curThen, nextIf.getThenBlock());
		boolean elsePathSame = isEqualPaths(curElse, nextIf.getElseBlock());
		if (!thenPathSame && !elsePathSame) {
			// complex condition, run additional checks
			if (checkConditionBranches(curThen, curElse)
					|| checkConditionBranches(curElse, curThen)) {
				return null;
			}
			BlockNode otherBranchBlock = followThenBranch ? curElse : curThen;
			otherBranchBlock = BlockUtils.followEmptyPath(otherBranchBlock);
			if (!isPathExists(nextIf.getFirstIfBlock(), otherBranchBlock)) {
				return checkForTernaryInCondition(currentIf);
			}

			// this is nested conditions with different mode (i.e (a && b) || c),
			// search next condition for merge, get null if failed
			IfInfo tmpIf = mergeNestedIfNodes(nextIf);
			if (tmpIf != null) {
				nextIf = tmpIf;
				if (isInversionNeeded(currentIf, nextIf)) {
					nextIf = IfInfo.invert(nextIf);
				}
				if (!canMerge(currentIf, nextIf, followThenBranch)) {
					return currentIf;
				}
			} else {
				return currentIf;
			}
		} else {
			if (assignInlineNeeded) {
				boolean sameOuts = (thenPathSame && !followThenBranch) || (elsePathSame && followThenBranch);
				if (!sameOuts) {
					// don't inline assigns inside simple condition
					currentIf.resetForceInlineInsns();
					return currentIf;
				}
			}
		}

		IfInfo result = mergeIfInfo(currentIf, nextIf, followThenBranch);
		// search next nested if block
		return searchNestedIf(result);
	}

	private static @Nullable IfInfo mergeBooleanDiamondBranch(
			IfInfo currentIf, BlockNode branch, boolean followThenBranch) {
		InsnNode branchInsn = BlockUtils.getLastInsn(branch);
		if (!(branchInsn instanceof IfNode)) {
			return null;
		}
		BlockNode join = getBooleanDiamondJoin((IfNode) branchInsn);
		if (join == null) {
			return null;
		}
		IfInfo nextIf = makeBooleanDiamondIfInfo(currentIf.getMth(), (IfNode) branchInsn, join);
		if (nextIf == null) {
			return null;
		}
		if (isInversionNeeded(currentIf, nextIf)) {
			nextIf = IfInfo.invert(nextIf);
		}
		if (!canMerge(currentIf, nextIf, followThenBranch)) {
			return null;
		}
		nextIf.getMergedBlocks().add(branch);
		nextIf.getMergedBlocks().add(((IfNode) branchInsn).getThenBlock());
		nextIf.getMergedBlocks().add(((IfNode) branchInsn).getElseBlock());
		return mergeIfInfo(currentIf, nextIf, followThenBranch);
	}

	private static @Nullable IfInfo makeBooleanDiamondIfInfo(MethodNode mth, IfNode diamondIf, BlockNode join) {
		InsnNode joinInsn = BlockUtils.getLastInsn(join);
		if (!(joinInsn instanceof IfNode)) {
			return null;
		}
		PhiListAttr phiList = join.get(AType.PHI_LIST);
		if (phiList == null) {
			return null;
		}
		InsnNode thenConst = getSingleGeneratedInsn(diamondIf.getThenBlock());
		InsnNode elseConst = getSingleGeneratedInsn(diamondIf.getElseBlock());
		if (thenConst == null || elseConst == null) {
			return null;
		}
		PhiInsn conditionPhi = null;
		for (PhiInsn phi : phiList.getList()) {
			RegisterArg result = phi.getResult();
			if (result != null && ((IfNode) joinInsn).containsVar(result)) {
				conditionPhi = phi;
				break;
			}
		}
		if (conditionPhi == null) {
			return null;
		}
		RegisterArg thenArg = conditionPhi.getArgByBlock(diamondIf.getThenBlock());
		RegisterArg elseArg = conditionPhi.getArgByBlock(diamondIf.getElseBlock());
		if (thenArg == null || elseArg == null) {
			return null;
		}
		IfCondition thenCondition = replacePhiInCondition((IfNode) joinInsn, conditionPhi, thenConst.getArg(0));
		IfCondition elseCondition = replacePhiInCondition((IfNode) joinInsn, conditionPhi, elseConst.getArg(0));
		if (thenCondition == null || elseCondition == null) {
			return null;
		}
		IfCondition condition = IfCondition.ternary(
				IfCondition.fromIfNode(diamondIf), thenCondition, elseCondition);
		IfInfo info = new IfInfo(mth, condition,
				((IfNode) joinInsn).getThenBlock(), ((IfNode) joinInsn).getElseBlock());
		info.getMergedBlocks().add(join);
		return info;
	}

	private static @Nullable IfCondition replacePhiInCondition(
			IfNode joinIf, PhiInsn phi, InsnArg replacement) {
		RegisterArg phiResult = phi.getResult();
		InsnArg first = replacePhiArg(joinIf.getArg(0), phiResult, replacement);
		InsnArg second = replacePhiArg(joinIf.getArg(1), phiResult, replacement);
		if (first == null && second == null) {
			return null;
		}
		IfNode copy = new IfNode(joinIf.getOp(), -1,
				first != null ? first : joinIf.getArg(0).duplicate(),
				second != null ? second : joinIf.getArg(1).duplicate());
		return IfCondition.fromIfNode(copy);
	}

	private static @Nullable InsnArg replacePhiArg(
			InsnArg arg, RegisterArg phiResult, InsnArg replacement) {
		return arg instanceof RegisterArg
				&& ((RegisterArg) arg).getSVar() == phiResult.getSVar()
						? replacement.duplicate()
						: null;
	}

	private static @Nullable BlockNode getBooleanDiamondJoin(IfNode ifNode) {
		BlockNode thenBlock = ifNode.getThenBlock();
		BlockNode elseBlock = ifNode.getElseBlock();
		if (thenBlock == null || elseBlock == null
				|| thenBlock.getCleanSuccessors().size() != 1
				|| elseBlock.getCleanSuccessors().size() != 1) {
			return null;
		}
		BlockNode join = thenBlock.getCleanSuccessors().get(0);
		if (join != elseBlock.getCleanSuccessors().get(0)
				|| !(BlockUtils.getLastInsn(join) instanceof IfNode)) {
			return null;
		}
		InsnNode thenConst = getSingleGeneratedInsn(thenBlock);
		InsnNode elseConst = getSingleGeneratedInsn(elseBlock);
		if (thenConst == null || elseConst == null
				|| thenConst.getType() != InsnType.CONST
				|| elseConst.getType() != InsnType.CONST
				|| thenConst.getResult() == null
				|| elseConst.getResult() == null
				|| thenConst.getResult().getRegNum() != elseConst.getResult().getRegNum()
				|| thenConst.getArgsCount() != 1
				|| elseConst.getArgsCount() != 1
				|| !(thenConst.getArg(0).isTrue() && elseConst.getArg(0).isFalse()
						|| thenConst.getArg(0).isFalse() && elseConst.getArg(0).isTrue())) {
			return null;
		}
		return join;
	}

	private static @Nullable InsnNode getSingleGeneratedInsn(BlockNode block) {
		List<InsnNode> insns = block.getInstructions();
		return insns.size() == 1 ? insns.get(0) : null;
	}

	private static IfInfo checkForTernaryInCondition(IfInfo currentIf) {
		IfInfo nextThen = getNextIf(currentIf, currentIf.getThenBlock());
		IfInfo nextElse = getNextIf(currentIf, currentIf.getElseBlock());
		if (nextThen == null || nextElse == null) {
			return null;
		}
		if (!nextThen.getFirstIfBlock().getDomFrontier().equals(nextElse.getFirstIfBlock().getDomFrontier())) {
			return null;
		}
		nextThen = searchNestedIf(nextThen);
		nextElse = searchNestedIf(nextElse);
		if (nextThen.getThenBlock() == nextElse.getThenBlock()
				&& nextThen.getElseBlock() == nextElse.getElseBlock()) {
			return mergeTernaryConditions(currentIf, nextThen, nextElse);
		}
		if (nextThen.getThenBlock() == nextElse.getElseBlock()
				&& nextThen.getElseBlock() == nextElse.getThenBlock()) {
			nextElse = IfInfo.invert(nextElse);
			return mergeTernaryConditions(currentIf, nextThen, nextElse);
		}
		return null;
	}

	private static IfInfo mergeTernaryConditions(IfInfo currentIf, IfInfo nextThen, IfInfo nextElse) {
		IfCondition newCondition = IfCondition.ternary(currentIf.getCondition(),
				nextThen.getCondition(), nextElse.getCondition());
		IfInfo result = new IfInfo(currentIf.getMth(), newCondition, nextThen.getThenBlock(), nextThen.getElseBlock());
		result.merge(currentIf, nextThen, nextElse);
		return result;
	}

	private static boolean isInversionNeeded(IfInfo currentIf, IfInfo nextIf) {
		return isEqualPaths(currentIf.getElseBlock(), nextIf.getThenBlock())
				|| isEqualPaths(currentIf.getThenBlock(), nextIf.getElseBlock());
	}

	private static boolean canMerge(IfInfo a, IfInfo b, boolean followThenBranch) {
		if (followThenBranch) {
			return isEqualPaths(a.getElseBlock(), b.getElseBlock());
		} else {
			return isEqualPaths(a.getThenBlock(), b.getThenBlock());
		}
	}

	private static boolean checkConditionBranches(BlockNode from, BlockNode to) {
		return from.getCleanSuccessors().size() == 1 && from.getCleanSuccessors().contains(to);
	}

	static IfInfo mergeIfInfo(IfInfo first, IfInfo second, boolean followThenBranch) {
		MethodNode mth = first.getMth();
		Set<BlockNode> skipBlocks = first.getSkipBlocks();
		BlockNode thenBlock;
		BlockNode elseBlock;
		if (followThenBranch) {
			thenBlock = second.getThenBlock();
			elseBlock = getBranchBlock(first.getElseBlock(), second.getElseBlock(), skipBlocks, mth);
		} else {
			thenBlock = getBranchBlock(first.getThenBlock(), second.getThenBlock(), skipBlocks, mth);
			elseBlock = second.getElseBlock();
		}
		IfCondition.Mode mergeOperation = followThenBranch ? IfCondition.Mode.AND : IfCondition.Mode.OR;
		IfCondition condition = IfCondition.merge(mergeOperation, first.getCondition(), second.getCondition());
		IfInfo result = new IfInfo(mth, condition, thenBlock, elseBlock);
		result.merge(first, second);
		return result;
	}

	private static BlockNode getBranchBlock(BlockNode first, BlockNode second, Set<BlockNode> skipBlocks, MethodNode mth) {
		if (first == second) {
			return second;
		}
		if (isEqualReturnBlocks(first, second)) {
			skipBlocks.add(first);
			return second;
		}
		if (BlockUtils.isDuplicateBlockPath(first, second)) {
			first.add(AFlag.REMOVE);
			BlockUtils.invalidatePathCache();
			skipBlocks.add(first);
			return second;
		}
		BlockNode cross = BlockUtils.getPathCross(mth, first, second);
		if (cross != null) {
			BlockUtils.visitBlocksOnPath(mth, first, cross, skipBlocks::add);
			BlockUtils.visitBlocksOnPath(mth, second, cross, skipBlocks::add);
			skipBlocks.remove(cross);
			return cross;
		}
		BlockNode firstSkip = BlockUtils.followEmptyPath(first);
		BlockNode secondSkip = BlockUtils.followEmptyPath(second);
		if (firstSkip.equals(secondSkip) || isEqualReturnBlocks(firstSkip, secondSkip)) {
			skipBlocks.add(first);
			skipBlocks.add(second);
			BlockUtils.visitBlocksOnEmptyPath(first, skipBlocks::add);
			BlockUtils.visitBlocksOnEmptyPath(second, skipBlocks::add);
			return secondSkip;
		}
		throw new JadxRuntimeException("Unexpected merge pattern");
	}

	static void confirmMerge(IfInfo info) {
		if (info.getMergedBlocks().size() > 1) {
			for (BlockNode block : info.getMergedBlocks()) {
				if (block != info.getFirstIfBlock()) {
					block.add(AFlag.ADDED_TO_REGION);
				}
			}
		}
		if (!info.getSkipBlocks().isEmpty()) {
			for (BlockNode block : info.getSkipBlocks()) {
				block.add(AFlag.ADDED_TO_REGION);
			}
			info.getSkipBlocks().clear();
		}
		for (InsnNode forceInlineInsn : info.getForceInlineInsns()) {
			forceInlineInsn.add(AFlag.FORCE_ASSIGN_INLINE);
		}
	}

	private static IfInfo getNextIf(IfInfo info, BlockNode block) {
		if (!canSelectNext(info, block)) {
			return null;
		}
		return getNextIfNodeInfo(info, block);
	}

	private static boolean canSelectNext(IfInfo info, BlockNode block) {
		if (block.getPredecessors().size() == 1) {
			return true;
		}
		return info.getMergedBlocks().containsAll(block.getPredecessors());
	}

	private static IfInfo getNextIfNodeInfo(IfInfo info, BlockNode block) {
		if (block == null
				|| block.contains(AType.LOOP)
				|| block.contains(AFlag.ADDED_TO_REGION)
				|| block.contains(AType.STANDALONE_IF_REGION)) {
			return null;
		}
		InsnNode lastInsn = BlockUtils.getLastInsn(block);
		if (lastInsn != null && lastInsn.getType() == InsnType.IF) {
			return makeIfInfo(info.getMth(), block);
		}
		BlockNode next = getNextBlockInIfSuccessorChain(block);
		if (next == null) {
			return null;
		}
		if (next.getPredecessors().size() != 1
				|| next.contains(AFlag.ADDED_TO_REGION)
				|| next.contains(AType.STANDALONE_IF_REGION)) {
			return null;
		}
		List<InsnNode> forceInlineInsns = new ArrayList<>();
		if (!checkInsnsInline(info.getMth(), block, next, forceInlineInsns)) {
			return null;
		}
		IfInfo nextInfo = makeIfInfo(info.getMth(), next);
		if (nextInfo == null) {
			return getNextIfNodeInfo(info, next);
		}
		nextInfo.addInsnsForForcedInline(forceInlineInsns);
		return nextInfo;
	}

	/**
	 * Allow singular successor to block or 2 successors where one is a EXC_BOTTOM_SPLITTER
	 */
	private static @Nullable BlockNode getNextBlockInIfSuccessorChain(BlockNode block) {

		// skip this block and search in successors chain
		List<BlockNode> successors = block.getSuccessors();
		if (successors.size() > 2 || successors.size() == 0) {
			return null;
		}
		// We might have the next IF and a EXC_BOTTOM_SPLITTER block to delimit a try region
		BlockNode first = successors.get(0);
		if (successors.size() == 1) {
			return first;
		}
		BlockNode second = successors.get(1);
		boolean firstIsHandlerPath = first.contains(AFlag.EXC_BOTTOM_SPLITTER);
		boolean secondIsHandlerPath = second.contains(AFlag.EXC_BOTTOM_SPLITTER);
		if (!firstIsHandlerPath && !secondIsHandlerPath) {
			// unknown case
			return null;
		}
		if (firstIsHandlerPath && secondIsHandlerPath) {
			// unknown case
			return null;
		}
		BlockNode candidate = firstIsHandlerPath ? second : first;

		// Continue to recurse through blocks as long as none of them have any instructions
		if (candidate.getInstructions().isEmpty()) {
			return getNextBlockInIfSuccessorChain(candidate);
		}

		return candidate;
	}

	/**
	 * Check that all instructions can be inlined
	 */
	private static boolean checkInsnsInline(
			MethodNode mth, BlockNode block, BlockNode next, List<InsnNode> forceInlineInsns) {
		List<InsnNode> insns = block.getInstructions();
		if (insns.isEmpty()) {
			return true;
		}
		boolean safeShortCircuitPrelude = isSafeShortCircuitPrelude(mth, insns);
		boolean pass = true;
		for (InsnNode insn : insns) {
			RegisterArg res = insn.getResult();
			if (res == null) {
				return false;
			}
			boolean nextEntersTry = next.getSuccessors().stream()
					.anyMatch(successor -> successor.contains(AFlag.EXC_TOP_SPLITTER));
			if (nextEntersTry) {
				// Forced inline can lose a self-overwriting assignment at a try boundary.
				InsnArg overwrittenInput = insn.visitArgs(arg -> arg.isRegister()
						&& ((RegisterArg) arg).getRegNum() == res.getRegNum() ? arg : null);
				if (overwrittenInput != null) {
					return false;
				}
			}
			List<RegisterArg> useList = res.getSVar().getUseList();
			int useCount = useList.size();
			if (useCount > 1 && insn.visitArgs(arg -> arg.isRegister()
					&& ((RegisterArg) arg).getRegNum() == res.getRegNum() ? arg : null) != null) {
				// A condition merge duplicates uses before FORCE_ASSIGN_INLINE is applied. The
				// shrink pass deliberately keeps a self-overwriting assignment (rX = rX.field),
				// so consuming its block here would leave the copied condition uses undefined.
				return false;
			}
			boolean usedInPhi = useList.stream()
					.map(RegisterArg::getParentInsn)
					.anyMatch(useInsn -> useInsn.getType() == InsnType.PHI);
			if (nextEntersTry && usedInPhi) {
				// Keep the assignment as a statement: its value flows through a join after
				// the following try/catch region and can't be represented only in the condition.
				return false;
			}
			if (useCount == 0) {
				// TODO?
				return false;
			}
			InsnArg arg = useList.get(0);
			InsnNode usePlace = arg.getParentInsn();
			if (!BlockUtils.blockContains(block, usePlace)
					&& !BlockUtils.blockContains(next, usePlace)
					&& (!safeShortCircuitPrelude
							|| !blockDeepContains(block, usePlace)
									&& !blockDeepContains(next, usePlace))) {
				return false;
			}
			if (useCount > 1) {
				forceInlineInsns.add(insn);
			} else if (!safeShortCircuitPrelude) {
				// allow only forced assign inline
				pass = false;
			}
		}
		return pass;
	}

	private static boolean blockDeepContains(BlockNode block, InsnNode target) {
		for (InsnNode insn : block.getInstructions()) {
			if (insn == target
					|| insn.visitInsns(innerInsn -> innerInsn == target ? Boolean.TRUE : null) != null) {
				return true;
			}
		}
		return false;
	}

	/**
	 * A compiler can materialize primitive bit conversions and reads of immutable fields between
	 * two conditions. Keeping those single-use values as statements prevents an otherwise regular
	 * short-circuit condition from being merged and can duplicate the entire following branch
	 * ladder. Inlining this narrow prelude preserves lazy evaluation and cannot change state.
	 */
	private static boolean isSafeShortCircuitPrelude(MethodNode mth, List<InsnNode> insns) {
		RegisterArg thisArg = mth.getThisArg();
		if (thisArg == null) {
			return false;
		}
		boolean hasPrimitiveBitsConversion = false;
		for (InsnNode insn : insns) {
			if (isPrimitiveBitsConversion(insn)) {
				hasPrimitiveBitsConversion = true;
				continue;
			}
			if (insn.getType() != InsnType.IGET
					|| !(insn instanceof IndexInsnNode)
					|| !(((IndexInsnNode) insn).getIndex() instanceof FieldInfo)
					|| insn.getArgsCount() != 1
					|| !insn.getArg(0).isSameVar(thisArg)) {
				return false;
			}
			FieldNode field = mth.root().resolveField((FieldInfo) ((IndexInsnNode) insn).getIndex());
			if (field == null
					|| !field.getAccessFlags().isFinal()
					|| field.getAccessFlags().isVolatile()) {
				return false;
			}
		}
		// A lone field read is already handled safely by the regular assignment-inline path.
		// Restrict this exception to the conversion prelude it was introduced for: otherwise a
		// shallow boolean-return ladder can absorb the field's defining block and lose its value.
		return hasPrimitiveBitsConversion;
	}

	private static boolean isPrimitiveBitsConversion(InsnNode insn) {
		if (!(insn instanceof InvokeNode)) {
			return false;
		}
		InvokeNode invoke = (InvokeNode) insn;
		if (invoke.getArgsCount() != 1) {
			return false;
		}
		String declClass = invoke.getCallMth().getDeclClass().getFullName();
		String name = invoke.getCallMth().getName();
		if (declClass.equals("java.lang.Float")) {
			return name.equals("intBitsToFloat")
					|| name.equals("floatToIntBits")
					|| name.equals("floatToRawIntBits");
		}
		if (declClass.equals("java.lang.Double")) {
			return name.equals("longBitsToDouble")
					|| name.equals("doubleToLongBits")
					|| name.equals("doubleToRawLongBits");
		}
		return false;
	}
}
