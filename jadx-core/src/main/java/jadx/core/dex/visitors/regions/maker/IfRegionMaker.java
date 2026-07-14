package jadx.core.dex.visitors.regions.maker;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jadx.core.Consts;
import jadx.core.dex.attributes.AFlag;
import jadx.core.dex.attributes.AType;
import jadx.core.dex.attributes.nodes.EdgeInsnAttr;
import jadx.core.dex.attributes.nodes.LoopInfo;
import jadx.core.dex.attributes.nodes.PhiListAttr;
import jadx.core.dex.info.FieldInfo;
import jadx.core.dex.instructions.IfNode;
import jadx.core.dex.instructions.IndexInsnNode;
import jadx.core.dex.instructions.InsnType;
import jadx.core.dex.instructions.PhiInsn;
import jadx.core.dex.instructions.args.ArgType;
import jadx.core.dex.instructions.args.InsnArg;
import jadx.core.dex.instructions.args.RegisterArg;
import jadx.core.dex.nodes.BlockNode;
import jadx.core.dex.nodes.IContainer;
import jadx.core.dex.nodes.IRegion;
import jadx.core.dex.nodes.InsnContainer;
import jadx.core.dex.nodes.InsnNode;
import jadx.core.dex.nodes.MethodNode;
import jadx.core.dex.regions.Region;
import jadx.core.dex.regions.conditions.IfCondition;
import jadx.core.dex.regions.conditions.IfInfo;
import jadx.core.dex.regions.conditions.IfRegion;
import jadx.core.dex.regions.loops.LoopRegion;
import jadx.core.dex.trycatch.ExcHandlerAttr;
import jadx.core.utils.BlockUtils;
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
	private final MethodNode mth;
	private final RegionMaker regionMaker;

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
		IfInfo mergedIf = mergeNestedIfNodes(currentIf);
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
		currentRegion.getSubBlocks().add(ifRegion);

		BlockNode outBlock = currentIf.getOutBlock();
		stack.push(ifRegion);
		stack.addExit(outBlock);

		BlockNode thenBlock = currentIf.getThenBlock();
		if (thenBlock == null) {
			// empty then block, not normal, but maybe correct
			ifRegion.setThenRegion(new Region(ifRegion));
		} else {
			IContainer edgeBranch = makeDirectEdgeBranch(ifRegion, currentIf, thenBlock);
			ifRegion.setThenRegion(edgeBranch != null ? edgeBranch : regionMaker.makeRegion(thenBlock));
		}
		BlockNode elseBlock = currentIf.getElseBlock();
		if (elseBlock == null || stack.containsExit(elseBlock)) {
			ifRegion.setElseRegion(null);
		} else {
			IContainer edgeBranch = makeDirectEdgeBranch(ifRegion, currentIf, elseBlock);
			ifRegion.setElseRegion(edgeBranch != null ? edgeBranch : regionMaker.makeRegion(elseBlock));
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
		BlockNode structuralOut = findOutBlock(mth, thenBlock, elseBlock);
		IfInfo directTerminalIf = isCoroutineMethod() || isSuspendLambdaMethod() || structuralOut == null
				? restructureAcyclicTerminalBranch(info, thenBlock, elseBlock)
				: null;
		if (directTerminalIf != null) {
			return directTerminalIf;
		}
		// init outblock, which will be used in isBadBranchBlock to compare with branch block
		info.setOutBlock(structuralOut);
		BlockNode sharedContinuation = findSharedContinuationPastTerminal(
				info.getOutBlock(), thenBlock, elseBlock);
		if (sharedContinuation != null) {
			IfInfo terminalBranch = restructureTerminalBeforeSharedContinuation(
					info, thenBlock, elseBlock, sharedContinuation);
			if (terminalBranch != null) {
				return terminalBranch;
			}
			info.setOutBlock(sharedContinuation);
		}
		BlockNode coroutineOut = findDeeperCoroutinePhiJoin(block, info.getOutBlock(), thenBlock, elseBlock);
		if (coroutineOut != null) {
			info.setOutBlock(coroutineOut);
		}

		boolean badThen = isBadBranchBlock(info, thenBlock);
		boolean badElse = isBadBranchBlock(info, elseBlock);
		IfInfo loopContinueIf = restructureSyntheticLoopContinuation(info, thenBlock, elseBlock);
		if (loopContinueIf != null) {
			return loopContinueIf;
		}
		IfInfo inheritedExitIf = restructureInheritedExit(info, thenBlock, elseBlock, badThen, badElse);
		if (inheritedExitIf != null) {
			return inheritedExitIf;
		}
		if (badThen && badElse) {
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
		if (currentOut == null || !isAcyclicTerminalSubgraph(currentOut)) {
			return null;
		}
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
		return best;
	}

	private static @Nullable IfInfo restructureTerminalBeforeSharedContinuation(
			IfInfo info, BlockNode thenBlock, BlockNode elseBlock, BlockNode sharedContinuation) {
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
		return mth.getMethodInfo().getArgumentsTypes().stream()
				.anyMatch(type -> type.toString().startsWith("kotlin.coroutines.Continuation"));
	}

	private @Nullable BlockNode findDeeperCoroutinePhiJoin(
			BlockNode ifBlock, @Nullable BlockNode currentOut, BlockNode thenBlock, BlockNode elseBlock) {
		if (currentOut == null
				|| currentOut.get(AType.PHI_LIST) == null
				|| !isCoroutineLabelIf(ifBlock)) {
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
		if (isPhiNeutralLoopContinuation(info, thenBlock)
				&& regionMaker.getStack().containsExit(elseBlock)) {
			addContinueEdges(info, thenBlock);
			IfInfo result = new IfInfo(info, thenBlock, null);
			result.setOutBlock(elseBlock);
			return result;
		}
		if (isPhiNeutralLoopContinuation(info, elseBlock)
				&& regionMaker.getStack().containsExit(thenBlock)) {
			addContinueEdges(info, elseBlock);
			IfInfo inverted = IfInfo.invert(info);
			IfInfo result = new IfInfo(inverted, elseBlock, null);
			result.setOutBlock(thenBlock);
			return result;
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
		addContinueEdges(info, continuation);
		IfInfo condition = thenContinuation ? info : IfInfo.invert(info);
		IfInfo result = new IfInfo(condition, continuation, null);
		result.setOutBlock(out);
		return result;
	}

	private static boolean isSyntheticLoopContinuation(BlockNode start) {
		if (start.getInstructions().stream().anyMatch(insn -> insn.getType() != InsnType.MOVE)) {
			return false;
		}
		BlockNode block = start;
		Set<BlockNode> visited = new HashSet<>();
		while (block != null && visited.size() < 8 && visited.add(block)) {
			if (block.contains(AFlag.LOOP_END)) {
				return true;
			}
			if (block.getInstructions().stream().anyMatch(insn -> insn.getType() != InsnType.MOVE)
					|| block.getSuccessors().size() != 1) {
				return false;
			}
			block = block.getSuccessors().get(0);
		}
		return false;
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
		for (BlockNode exit : regionMaker.getStack().getExits()) {
			if (exit == info.getOutBlock()) {
				// This is the local join selected for this if. Prefer an inherited scope exit;
				// otherwise the shared terminal branch is incorrectly emitted after an empty if.
				continue;
			}
			if (thenBlock == exit && isAcyclicTerminalSubgraph(elseBlock)) {
				IfInfo inverted = IfInfo.invert(info);
				IfInfo result = new IfInfo(inverted, elseBlock, null);
				result.setOutBlock(exit);
				return result;
			}
			if (elseBlock == exit && isAcyclicTerminalSubgraph(thenBlock)) {
				IfInfo result = new IfInfo(info, thenBlock, null);
				result.setOutBlock(exit);
				return result;
			}
		}
		return null;
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
		return isAcyclicTerminalSubgraph(startBlock, new HashSet<>(), new HashSet<>());
	}

	private static boolean isAcyclicTerminalSubgraph(BlockNode block, Set<BlockNode> visiting, Set<BlockNode> terminal) {
		if (terminal.contains(block)) {
			return true;
		}
		if (visiting.size() >= 12
				|| block.contains(AFlag.LOOP_START)
				|| block.contains(AFlag.LOOP_END)
				|| !visiting.add(block)) {
			return false;
		}
		if (BlockUtils.containsExitInsn(block)) {
			visiting.remove(block);
			terminal.add(block);
			return true;
		}
		List<BlockNode> successors = block.getCleanSuccessors();
		if (successors.isEmpty()) {
			visiting.remove(block);
			return false;
		}
		for (BlockNode successor : successors) {
			if (!isAcyclicTerminalSubgraph(successor, visiting, terminal)) {
				visiting.remove(block);
				return false;
			}
		}
		visiting.remove(block);
		terminal.add(block);
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
		if (!mth.getName().equals("invokeSuspend")) {
			return false;
		}
		ArgType superClass = mth.getParentClass().getSuperClass();
		return superClass != null
				&& superClass.getObject().endsWith("SuspendLambda");
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
			if (mth.getLoopsCount() != 0
					|| isCommonPostDominator(mth, thenBlock, elseBlock, oneBlock)) {
				return oneBlock;
			}
			BlockNode deeperOut = findCommonPostDominator(mth, thenBlock, elseBlock);
			if (deeperOut != null) {
				return deeperOut;
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

	static boolean isCommonPostDominator(
			MethodNode mth, BlockNode thenBlock, BlockNode elseBlock, BlockNode candidate) {
		// A branch starting at the join passes through it by definition. A terminal path in the
		// opposite branch must not push a normal continuation to a later, unrelated join.
		if (candidate == thenBlock || candidate == elseBlock) {
			return true;
		}
		return !hasTerminalPathAvoiding(mth, thenBlock, candidate)
				&& !hasTerminalPathAvoiding(mth, elseBlock, candidate);
	}

	private static boolean hasTerminalPathAvoiding(MethodNode mth, BlockNode start, BlockNode excluded) {
		if (start == excluded) {
			return false;
		}
		BitSet visited = newBlocksBitSet(mth);
		List<BlockNode> stack = new ArrayList<>();
		stack.add(start);
		while (!stack.isEmpty()) {
			BlockNode block = stack.remove(stack.size() - 1);
			if (block == excluded || visited.get(block.getPos())) {
				continue;
			}
			visited.set(block.getPos());
			List<BlockNode> successors = block.getCleanSuccessors();
			if (block == mth.getExitBlock() || successors.isEmpty()) {
				return true;
			}
			stack.addAll(successors);
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
		if (block == null || block.contains(AType.LOOP) || block.contains(AFlag.ADDED_TO_REGION)) {
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
		if (next.getPredecessors().size() != 1 || next.contains(AFlag.ADDED_TO_REGION)) {
			return null;
		}
		List<InsnNode> forceInlineInsns = new ArrayList<>();
		if (!checkInsnsInline(block, next, forceInlineInsns)) {
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
	private static boolean checkInsnsInline(BlockNode block, BlockNode next, List<InsnNode> forceInlineInsns) {
		List<InsnNode> insns = block.getInstructions();
		if (insns.isEmpty()) {
			return true;
		}
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
					&& !BlockUtils.blockContains(next, usePlace)) {
				return false;
			}
			if (useCount > 1) {
				forceInlineInsns.add(insn);
			} else {
				// allow only forced assign inline
				pass = false;
			}
		}
		return pass;
	}
}
