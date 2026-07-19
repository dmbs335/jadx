package jadx.core.dex.visitors.blocks;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jadx.api.plugins.input.data.attributes.IJadxAttrType;
import jadx.api.plugins.input.data.attributes.IJadxAttribute;
import jadx.core.dex.attributes.AFlag;
import jadx.core.dex.attributes.AType;
import jadx.core.dex.attributes.nodes.CodeFeaturesAttr;
import jadx.core.dex.attributes.nodes.LoopInfo;
import jadx.core.dex.instructions.IfNode;
import jadx.core.dex.instructions.IfOp;
import jadx.core.dex.instructions.InsnType;
import jadx.core.dex.instructions.InvokeNode;
import jadx.core.dex.instructions.args.ArgType;
import jadx.core.dex.instructions.args.InsnArg;
import jadx.core.dex.instructions.args.LiteralArg;
import jadx.core.dex.instructions.args.RegisterArg;
import jadx.core.dex.nodes.BlockNode;
import jadx.core.dex.nodes.Edge;
import jadx.core.dex.nodes.InsnNode;
import jadx.core.dex.nodes.MethodNode;
import jadx.core.dex.trycatch.ExcHandlerAttr;
import jadx.core.dex.trycatch.TryCatchBlockAttr;
import jadx.core.dex.visitors.AbstractVisitor;
import jadx.core.utils.BlockUtils;
import jadx.core.utils.Utils;
import jadx.core.utils.exceptions.JadxRuntimeException;

import static jadx.core.dex.visitors.blocks.BlockSplitter.connect;

public class BlockProcessor extends AbstractVisitor {
	private static final Logger LOG = LoggerFactory.getLogger(BlockProcessor.class);

	private static final boolean DEBUG_MODS = false;

	@Override
	public void visit(MethodNode mth) {
		if (mth.isNoCode() || mth.getBasicBlocks().isEmpty()) {
			return;
		}
		processBlocksTree(mth);
	}

	private static void processBlocksTree(MethodNode mth) {
		removeUnreachableBlocks(mth);

		computeDominators(mth);
		if (FixMultiEntryLoops.hasMultiEntryLoops(mth) && simplifyConstantIfs(mth)) {
			removeUnreachableBlocks(mth);
			computeDominators(mth);
		}
		if (independentBlockTreeMod(mth)) {
			checkForUnreachableBlocks(mth);
			computeDominators(mth);
		}
		if (FixMultiEntryLoops.process(mth)) {
			computeDominators(mth);
		}
		updateCleanSuccessors(mth);

		int blocksCount = mth.getBasicBlocks().size();
		int modLimit = Math.max(100, blocksCount);
		if (DEBUG_MODS) {
			mth.addAttr(new DebugModAttr());
		}
		int i = 0;
		while (modifyBlocksTree(mth)) {
			computeDominators(mth);
			if (i++ > modLimit) {
				mth.addWarn("CFG modification limit reached, blocks count: " + blocksCount);
				break;
			}
		}
		if (DEBUG_MODS && i != 0) {
			String stats = "CFG modifications count: " + i
					+ ", blocks count: " + blocksCount + '\n'
					+ mth.get(DebugModAttr.TYPE).formatStats() + '\n';
			mth.addDebugComment(stats);
			LOG.debug("Method: {}\n{}", mth, stats);
			mth.remove(DebugModAttr.TYPE);
		}
		checkForUnreachableBlocks(mth);

		DominatorTree.computeDominanceFrontier(mth);
		registerLoops(mth);
		processNestedLoops(mth);

		PostDominatorTree.compute(mth);

		updateCleanSuccessors(mth);
	}

	/**
	 * Remove an IF branch if both operands resolve to literal assignments on the same straight-line
	 * path.
	 * This runs before SSA and only follows single-predecessor/single-successor edges.
	 */
	private static boolean simplifyConstantIfs(MethodNode mth) {
		boolean changed = false;
		for (BlockNode block : mth.getBasicBlocks()) {
			InsnNode lastInsn = BlockUtils.getLastInsn(block);
			if (lastInsn == null || lastInsn.getType() != InsnType.IF) {
				continue;
			}
			IfNode ifInsn = (IfNode) lastInsn;
			Long first = resolveLiteralInBlock(block, ifInsn.getArg(0), lastInsn);
			Long second = resolveLiteralInBlock(block, ifInsn.getArg(1), lastInsn);
			if (first == null || second == null) {
				continue;
			}
			boolean condition = evaluate(ifInsn.getOp(), first, second);
			BlockNode liveBranch = condition ? ifInsn.getThenBlock() : ifInsn.getElseBlock();
			BlockNode deadBranch = condition ? ifInsn.getElseBlock() : ifInsn.getThenBlock();
			if (liveBranch == deadBranch) {
				continue;
			}
			block.getInstructions().remove(block.getInstructions().size() - 1);
			BlockSplitter.removeConnection(block, deadBranch);
			mth.addDebugComment("Simplified constant IF in block: " + block);
			changed = true;
		}
		return changed;
	}

	private static @Nullable Long resolveLiteralInBlock(BlockNode block, InsnArg arg, InsnNode beforeInsn) {
		if (arg.isLiteral()) {
			return ((LiteralArg) arg).getLiteral();
		}
		if (!arg.isRegister()) {
			return null;
		}
		int regNum = ((RegisterArg) arg).getRegNum();
		return resolveRegisterLiteral(block, regNum, block.getInstructions().indexOf(beforeInsn), 0);
	}

	private static @Nullable Long resolveRegisterLiteral(BlockNode block, int regNum, int end, int depth) {
		List<InsnNode> insns = block.getInstructions();
		for (int i = end - 1; i >= 0; i--) {
			InsnNode insn = insns.get(i);
			RegisterArg result = insn.getResult();
			if (result != null && result.getRegNum() == regNum) {
				if (insn.getType() == InsnType.CONST && insn.getArgsCount() == 1 && insn.getArg(0).isLiteral()) {
					return ((LiteralArg) insn.getArg(0)).getLiteral();
				}
				return null;
			}
		}
		if (depth < 4 && block.getPredecessors().size() == 1) {
			BlockNode predecessor = block.getPredecessors().get(0);
			if (predecessor.getSuccessors().size() == 1) {
				return resolveRegisterLiteral(predecessor, regNum, predecessor.getInstructions().size(), depth + 1);
			}
		}
		return null;
	}

	private static boolean evaluate(IfOp op, long first, long second) {
		switch (op) {
			case EQ:
				return first == second;
			case NE:
				return first != second;
			case LT:
				return first < second;
			case LE:
				return first <= second;
			case GT:
				return first > second;
			case GE:
				return first >= second;
			default:
				throw new JadxRuntimeException("Unexpected IF operation: " + op);
		}
	}

	/**
	 * Recalculate all additional info attached to blocks:
	 *
	 * <pre>
	 * - dominators
	 * - dominance frontier
	 * - post dominators (only if {@link AFlag#COMPUTE_POST_DOM} added to method)
	 * - loops and nested loop info
	 * </pre>
	 * <p>
	 * This method should be called after changing a block tree in custom passes added before
	 * {@link BlockFinisher}.
	 */
	public static void updateBlocksData(MethodNode mth) {
		clearBlocksState(mth);
		DominatorTree.compute(mth);
		markLoops(mth);

		DominatorTree.computeDominanceFrontier(mth);
		registerLoops(mth);
		processNestedLoops(mth);

		PostDominatorTree.compute(mth);

		updateCleanSuccessors(mth);
	}

	static void updateCleanSuccessors(MethodNode mth) {
		mth.getBasicBlocks().forEach(BlockNode::updateCleanSuccessors);
	}

	private static void checkForUnreachableBlocks(MethodNode mth) {
		while (true) {
			boolean fixed = false;
			for (BlockNode block : mth.getBasicBlocks()) {
				if (block.getPredecessors().isEmpty() && block != mth.getEnterBlock()) {
					// Sometimes a split cross block will have all it's predecessors moved elsewhere after it's been
					// created. This is usually detected at the time of it's creation, but in certain edge cases it
					// is difficult to do so. In those cases it will be cleanly removed here, along with the associated
					// bottom splitter.
					if (block.contains(AType.EXC_SPLIT_CROSS) && fixUnreachableSplitCross(mth, block)) {
						mth.addInfoComment("Removed unreachable split cross block " + block);
						fixed = true;
						break;
					}
					throw new JadxRuntimeException("Unreachable block: " + block);
				}
			}
			if (!fixed) {
				break;
			}
		}
	}

	/**
	 * Attempts to remove an unreachable synthetic split cross block that has been added previously,
	 * along with the associated bottom splitter.
	 *
	 * @param mth        the method containing the unreachable block
	 * @param splitCross the unreachable block
	 * @return true if the operation was successful, false if a precondition was not satisfied and no
	 *         changes were made.
	 */
	private static boolean fixUnreachableSplitCross(MethodNode mth, BlockNode splitCross) {
		BlockNode bottomSplitter = null;
		for (BlockNode succ : splitCross.getSuccessors()) {
			if (succ.contains(AFlag.EXC_BOTTOM_SPLITTER)) {
				bottomSplitter = succ;
				break;
			}
		}
		if (bottomSplitter == null || bottomSplitter.getPredecessors().size() != 1) {
			return false;
		}
		Set<BlockNode> removeSet = new HashSet<>();
		removeSet.add(bottomSplitter);
		removeSet.add(splitCross);
		removeFromMethod(removeSet, mth);
		return true;
	}

	private static boolean deduplicateBlockInsns(MethodNode mth, BlockNode block) {
		if (block.contains(AFlag.LOOP_START) || block.contains(AFlag.LOOP_END)) {
			// search for same instruction at end of all predecessors blocks
			List<BlockNode> predecessors = block.getPredecessors();
			int predsCount = predecessors.size();
			if (predsCount > 1) {
				InsnNode lastInsn = BlockUtils.getLastInsn(block);
				if (lastInsn != null && lastInsn.getType() == InsnType.IF) {
					return false;
				}
				if (BlockUtils.checkFirstInsn(block, insn -> insn.contains(AType.EXC_HANDLER))) {
					return false;
				}
				// TODO: implement insn extraction into separate block for partial predecessors
				int sameInsnCount = getSameLastInsnCount(predecessors);
				if (sameInsnCount > 0) {
					List<InsnNode> insns = getLastInsns(predecessors.get(0), sameInsnCount);
					insertAtStart(block, insns);
					predecessors.forEach(pred -> getLastInsns(pred, sameInsnCount).clear());
					mth.addDebugComment("Move duplicate insns, count: " + sameInsnCount + " to block " + block);
					return true;
				}
			}
		}
		return false;
	}

	private static List<InsnNode> getLastInsns(BlockNode blockNode, int sameInsnCount) {
		List<InsnNode> instructions = blockNode.getInstructions();
		int size = instructions.size();
		return instructions.subList(size - sameInsnCount, size);
	}

	private static void insertAtStart(BlockNode block, List<InsnNode> insns) {
		List<InsnNode> blockInsns = block.getInstructions();

		List<InsnNode> newInsnList = new ArrayList<>(insns.size() + blockInsns.size());
		newInsnList.addAll(insns);
		newInsnList.addAll(blockInsns);

		blockInsns.clear();
		blockInsns.addAll(newInsnList);
	}

	private static int getSameLastInsnCount(List<BlockNode> predecessors) {
		int sameInsnCount = 0;
		while (true) {
			InsnNode insn = null;
			for (BlockNode pred : predecessors) {
				InsnNode curInsn = getInsnsFromEnd(pred, sameInsnCount);
				if (curInsn == null) {
					return sameInsnCount;
				}
				if (insn == null) {
					insn = curInsn;
				} else {
					if (!isSame(insn, curInsn)) {
						return sameInsnCount;
					}
				}
			}
			sameInsnCount++;
		}
	}

	private static boolean isSame(InsnNode insn, InsnNode curInsn) {
		return isInsnsEquals(insn, curInsn) && insn.canReorder();
	}

	private static boolean isInsnsEquals(InsnNode insn, InsnNode otherInsn) {
		if (insn == otherInsn) {
			return true;
		}
		if (insn.isSame(otherInsn)
				&& sameArgs(insn.getResult(), otherInsn.getResult())) {
			int argsCount = insn.getArgsCount();
			for (int i = 0; i < argsCount; i++) {
				if (!sameArgs(insn.getArg(i), otherInsn.getArg(i))) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	private static boolean sameArgs(@Nullable InsnArg arg, @Nullable InsnArg otherArg) {
		if (arg == otherArg) {
			return true;
		}
		if (arg == null || otherArg == null) {
			return false;
		}
		if (arg.getClass().equals(otherArg.getClass())) {
			if (arg.isRegister()) {
				return ((RegisterArg) arg).getRegNum() == ((RegisterArg) otherArg).getRegNum();
			}
			if (arg.isLiteral()) {
				return ((LiteralArg) arg).getLiteral() == ((LiteralArg) otherArg).getLiteral();
			}
			throw new JadxRuntimeException("Unexpected InsnArg types: " + arg + " and " + otherArg);
		}
		return false;
	}

	private static InsnNode getInsnsFromEnd(BlockNode block, int number) {
		List<InsnNode> instructions = block.getInstructions();
		int insnCount = instructions.size();
		if (insnCount <= number) {
			return null;
		}
		return instructions.get(insnCount - number - 1);
	}

	private static void computeDominators(MethodNode mth) {
		clearBlocksState(mth);
		DominatorTree.compute(mth);
		markLoops(mth);
	}

	private static void markLoops(MethodNode mth) {
		mth.getBasicBlocks().forEach(block -> {
			// Every successor that dominates its predecessor is a header of a loop,
			// block -> successor is a back edge.
			block.getSuccessors().forEach(successor -> {
				if (block.getDoms().get(successor.getId()) || block == successor) {
					successor.add(AFlag.LOOP_START);
					block.add(AFlag.LOOP_END);

					Set<BlockNode> loopBlocks = BlockUtils.getAllPathsBlocks(successor, block);
					LoopInfo loop = new LoopInfo(successor, block, loopBlocks);
					successor.addAttr(AType.LOOP, loop);
					block.addAttr(AType.LOOP, loop);
				}
			});
		});
	}

	private static void registerLoops(MethodNode mth) {
		mth.resetLoops();
		mth.getBasicBlocks().forEach(block -> {
			if (block.contains(AFlag.LOOP_START)) {
				block.getAll(AType.LOOP).forEach(mth::registerLoop);
			}
		});
	}

	private static void processNestedLoops(MethodNode mth) {
		if (mth.getLoopsCount() == 0) {
			return;
		}
		for (LoopInfo outLoop : mth.getLoops()) {
			for (LoopInfo innerLoop : mth.getLoops()) {
				if (outLoop == innerLoop) {
					continue;
				}
				if (outLoop.getLoopBlocks().containsAll(innerLoop.getLoopBlocks())) {
					LoopInfo parentLoop = innerLoop.getParentLoop();
					if (parentLoop != null) {
						if (parentLoop.getLoopBlocks().containsAll(outLoop.getLoopBlocks())) {
							outLoop.setParentLoop(parentLoop);
							innerLoop.setParentLoop(outLoop);
						} else {
							parentLoop.setParentLoop(outLoop);
						}
					} else {
						innerLoop.setParentLoop(outLoop);
					}
				}
			}
		}
	}

	private static boolean modifyBlocksTree(MethodNode mth) {
		for (BlockNode block : mth.getBasicBlocks()) {
			if (checkLoops(mth, block)) {
				return true;
			}
		}
		if (mergeConstReturn(mth)) {
			return true;
		}
		for (BlockNode basicBlock : mth.getBasicBlocks()) {
			if (duplicateLoopSharedMoveBlock(mth, basicBlock)) {
				return true;
			}
		}
		if (CodeFeaturesAttr.contains(mth, CodeFeaturesAttr.CodeFeature.SWITCH)) {
			for (BlockNode basicBlock : mth.getBasicBlocks()) {
				if (duplicateSharedStringIfDiamond(mth, basicBlock)) {
					return true;
				}
				if (duplicateSimpleMoveBlock(mth, basicBlock)) {
					return true;
				}
				if (duplicateSharedIfReturnBranch(mth, basicBlock)) {
					return true;
				}
				if (duplicateSharedIfTail(mth, basicBlock)) {
					return true;
				}
			}
		}
		return splitExitBlocks(mth);
	}

	private static boolean mergeConstReturn(MethodNode mth) {
		if (mth.isVoidReturn()) {
			return false;
		}
		boolean changed = false;
		for (BlockNode retBlock : new ArrayList<>(mth.getPreExitBlocks())) {
			BlockNode pred = Utils.getOne(retBlock.getPredecessors());
			if (pred != null) {
				InsnNode constInsn = Utils.getOne(pred.getInstructions());
				if (constInsn != null && constInsn.isConstInsn()) {
					RegisterArg constArg = constInsn.getResult();
					InsnNode returnInsn = BlockUtils.getLastInsn(retBlock);
					if (returnInsn != null && returnInsn.getType() == InsnType.RETURN) {
						InsnArg retArg = returnInsn.getArg(0);
						if (constArg.sameReg(retArg)) {
							mergeConstAndReturnBlocks(mth, retBlock, pred);
							changed = true;
						}
					}
				}
			}
		}
		if (changed) {
			removeMarkedBlocks(mth);
			if (DEBUG_MODS) {
				mth.get(DebugModAttr.TYPE).addEvent("Merge const return");
			}
		}
		return changed;
	}

	private static void mergeConstAndReturnBlocks(MethodNode mth, BlockNode retBlock, BlockNode pred) {
		pred.getInstructions().addAll(retBlock.getInstructions());
		pred.copyAttributesFrom(retBlock);
		BlockSplitter.removeConnection(pred, retBlock);
		retBlock.getInstructions().clear();
		retBlock.add(AFlag.REMOVE);
		BlockNode exitBlock = mth.getExitBlock();
		BlockSplitter.removeConnection(retBlock, exitBlock);
		BlockSplitter.connect(pred, exitBlock);
		pred.updateCleanSuccessors();
	}

	private static boolean independentBlockTreeMod(MethodNode mth) {
		boolean changed = false;
		List<BlockNode> basicBlocks = mth.getBasicBlocks();
		if (mergeEquivalentIfBranches(mth, basicBlocks)) {
			changed = true;
			// Exception processing below needs dominators for the updated CFG.
			computeDominators(mth);
		}
		for (BlockNode basicBlock : basicBlocks) {
			if (deduplicateBlockInsns(mth, basicBlock)) {
				changed = true;
			}
		}
		if (BlockExceptionHandler.process(mth)) {
			changed = true;
		}
		for (BlockNode basicBlock : basicBlocks) {
			if (BlockSplitter.removeEmptyBlock(basicBlock)) {
				changed = true;
			}
		}
		if (BlockSplitter.removeEmptyDetachedBlocks(mth)) {
			changed = true;
		}
		return changed;
	}

	private static boolean mergeEquivalentIfBranches(MethodNode mth, List<BlockNode> blocks) {
		boolean changed = false;
		for (BlockNode root : new ArrayList<>(blocks)) {
			InsnNode rootInsn = BlockUtils.getLastInsn(root);
			if (!(rootInsn instanceof IfNode)) {
				continue;
			}
			BlockNode thenEnd = followLinearBranchToIf(((IfNode) rootInsn).getThenBlock());
			BlockNode elseEnd = followLinearBranchToIf(((IfNode) rootInsn).getElseBlock());
			if (thenEnd == null || elseEnd == null || thenEnd == elseEnd) {
				continue;
			}
			IfNode thenIf = (IfNode) BlockUtils.getLastInsn(thenEnd);
			IfNode elseIf = (IfNode) BlockUtils.getLastInsn(elseEnd);
			if (!areEquivalentIfs(thenIf, elseIf)
					|| !haveCompatibleEquivalentIfInputTypes(thenEnd, elseEnd, thenIf, elseIf)
					|| BlockUtils.isExceptionHandlerPath(thenEnd)
					|| BlockUtils.isExceptionHandlerPath(elseEnd)) {
				continue;
			}
			BlockNode thenTarget = thenIf.getThenBlock();
			BlockNode elseTarget = thenIf.getElseBlock();
			BlockNode join = BlockSplitter.startNewBlock(mth, thenEnd.getStartOffset());
			join.add(AFlag.SYNTHETIC);
			join.getInstructions().add(thenIf.copyWithoutSsa());
			thenEnd.getInstructions().remove(thenEnd.getInstructions().size() - 1);
			elseEnd.getInstructions().remove(elseEnd.getInstructions().size() - 1);
			for (BlockNode target : new ArrayList<>(thenEnd.getSuccessors())) {
				BlockSplitter.removeConnection(thenEnd, target);
			}
			for (BlockNode target : new ArrayList<>(elseEnd.getSuccessors())) {
				BlockSplitter.removeConnection(elseEnd, target);
			}
			BlockSplitter.connect(thenEnd, join);
			BlockSplitter.connect(elseEnd, join);
			BlockSplitter.connect(join, thenTarget);
			BlockSplitter.connect(join, elseTarget);
			thenEnd.updateCleanSuccessors();
			elseEnd.updateCleanSuccessors();
			join.updateCleanSuccessors();
			mth.addDebugComment("Merge equivalent branch IF blocks: " + thenEnd + " and " + elseEnd);
			changed = true;
		}
		return changed;
	}

	private static @Nullable BlockNode followLinearBranchToIf(BlockNode start) {
		BlockNode block = start;
		Set<BlockNode> visited = new HashSet<>();
		while (block != null && visited.size() < 8 && visited.add(block)) {
			InsnNode lastInsn = BlockUtils.getLastInsn(block);
			if (lastInsn instanceof IfNode) {
				return block;
			}
			if (block.getSuccessors().size() != 1
					|| block.contains(AFlag.LOOP_START)
					|| block.contains(AFlag.LOOP_END)) {
				return null;
			}
			block = block.getSuccessors().get(0);
		}
		return null;
	}

	private static boolean areEquivalentIfs(IfNode first, IfNode second) {
		return first.getThenBlock() == second.getThenBlock()
				&& first.getElseBlock() == second.getElseBlock()
				&& first.getOp() == second.getOp()
				&& first.getArguments().equals(second.getArguments());
	}

	static boolean haveCompatibleEquivalentIfInputTypes(
			BlockNode firstBlock, BlockNode secondBlock, IfNode firstIf, IfNode secondIf) {
		for (int i = 0; i < firstIf.getArgsCount(); i++) {
			InsnArg firstArg = firstIf.getArg(i);
			InsnArg secondArg = secondIf.getArg(i);
			if (!firstArg.isRegister() || !secondArg.isRegister()) {
				continue;
			}
			int firstReg = ((RegisterArg) firstArg).getRegNum();
			int secondReg = ((RegisterArg) secondArg).getRegNum();
			if (firstReg != secondReg) {
				continue;
			}
			ArgType firstType = resolveRegisterType(
					firstBlock, firstReg, firstBlock.getInstructions().indexOf(firstIf), 0);
			ArgType secondType = resolveRegisterType(
					secondBlock, secondReg, secondBlock.getInstructions().indexOf(secondIf), 0);
			if (firstType != null && firstType.isTypeKnown()
					&& secondType != null && secondType.isTypeKnown()
					&& firstType.isPrimitive() != secondType.isPrimitive()) {
				return false;
			}
		}
		return true;
	}

	private static @Nullable ArgType resolveRegisterType(BlockNode block, int regNum, int end, int depth) {
		List<InsnNode> insns = block.getInstructions();
		for (int i = end - 1; i >= 0; i--) {
			RegisterArg result = insns.get(i).getResult();
			if (result != null && result.getRegNum() == regNum) {
				return result.getInitType();
			}
		}
		if (depth < 4 && block.getPredecessors().size() == 1) {
			BlockNode predecessor = block.getPredecessors().get(0);
			if (predecessor.getSuccessors().size() == 1) {
				return resolveRegisterType(predecessor, regNum, predecessor.getInstructions().size(), depth + 1);
			}
		}
		return null;
	}

	/**
	 * Duplicate block if it contains only one 'move' insn and all predecessors are 'switch' and 'if'.
	 * This will help to resolve switch cases order and fallthrough detection
	 * because such move blocks can be deduplicated by compiler.
	 */
	private static boolean duplicateSimpleMoveBlock(MethodNode mth, BlockNode block) {
		List<InsnNode> insns = block.getInstructions();
		if (insns.size() == 1 && block.getSuccessors().size() == 1) {
			InsnNode insn = insns.get(0);
			if (insn.getType() == InsnType.MOVE) {
				List<BlockNode> preds = block.getPredecessors();
				int predSize = preds.size();
				if (predSize >= 3 && onlySwitchAndIfInLastInsns(preds)) {
					// confirmed, duplicate block
					BlockNode successor = block.getSuccessors().get(0);
					List<BlockNode> predsCopy = new ArrayList<>(preds);
					for (int i = 1; i < predSize; i++) {
						BlockNode pred = predsCopy.get(i);
						BlockNode newBlock = BlockSplitter.startNewBlock(mth, -1);
						newBlock.add(AFlag.SYNTHETIC);
						for (InsnNode oldInsn : block.getInstructions()) {
							InsnNode copyInsn = oldInsn.copyWithoutSsa();
							copyInsn.add(AFlag.SYNTHETIC);
							newBlock.getInstructions().add(copyInsn);
						}
						newBlock.copyAttributesFrom(block);
						BlockSplitter.replaceConnection(pred, block, newBlock);
						BlockSplitter.connect(newBlock, successor);
					}
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Duplicate a move-only loop branch shared by several IF exits.
	 * Optimizers can merge identical assignments from several direction checks,
	 * but a region can only own the shared block once.
	 */
	private static boolean duplicateLoopSharedMoveBlock(MethodNode mth, BlockNode block) {
		List<InsnNode> insns = block.getInstructions();
		if (isCoroutineMethod(mth)
				|| !isInMarkedLoop(mth, block)
				|| insns.size() != 1
				|| block.getSuccessors().size() != 1
				|| insns.get(0).getType() != InsnType.MOVE) {
			return false;
		}
		List<BlockNode> preds = block.getPredecessors();
		if (preds.size() < 3
				|| !onlyIfInLastInsns(preds)
				|| (!isMarkedLoopSharedMove(block) && !hasLoopSharedMoveCompanion(mth, block))) {
			return false;
		}
		BlockNode successor = block.getSuccessors().get(0);
		List<BlockNode> predsCopy = new ArrayList<>(preds);
		for (int i = 1; i < predsCopy.size(); i++) {
			BlockNode pred = predsCopy.get(i);
			BlockNode newBlock = BlockSplitter.startNewBlock(mth, -1);
			newBlock.add(AFlag.SYNTHETIC);
			InsnNode copyInsn = insns.get(0).copyWithoutSsa();
			copyInsn.add(AFlag.SYNTHETIC);
			newBlock.getInstructions().add(copyInsn);
			newBlock.copyAttributesFrom(block);
			BlockSplitter.replaceConnection(pred, block, newBlock);
			BlockSplitter.connect(newBlock, successor);
			InsnNode predInsn = BlockUtils.getLastInsn(pred);
			if (predInsn instanceof IfNode) {
				((IfNode) predInsn).replaceTargetBlock(block, newBlock);
			}
			pred.updateCleanSuccessors();
			newBlock.updateCleanSuccessors();
		}
		block.add(AFlag.SYNTHETIC);
		block.getInstructions().get(0).add(AFlag.SYNTHETIC);
		return true;
	}

	private static boolean hasLoopSharedMoveCompanion(MethodNode mth, BlockNode block) {
		boolean continuation = isStraightLoopContinuation(mth, block);
		for (BlockNode other : mth.getBasicBlocks()) {
			if (other == block
					|| other.getInstructions().size() != 1
					|| other.getSuccessors().size() != 1
					|| other.getInstructions().get(0).getType() != InsnType.MOVE
					|| !isInMarkedLoop(mth, other)
					|| other.getPredecessors().size() < 3
					|| !onlyIfInLastInsns(other.getPredecessors())
					|| continuation == isStraightLoopContinuation(mth, other)
					|| !isInsnsEquals(block.getInstructions().get(0), other.getInstructions().get(0))) {
				continue;
			}
			long commonPreds = block.getPredecessors().stream()
					.filter(other.getPredecessors()::contains)
					.count();
			if (commonPreds >= 3) {
				other.add(AFlag.SYNTHETIC);
				other.getInstructions().get(0).add(AFlag.SYNTHETIC);
				return true;
			}
		}
		return false;
	}

	private static boolean isMarkedLoopSharedMove(BlockNode block) {
		return block.contains(AFlag.SYNTHETIC)
				&& block.getInstructions().size() == 1
				&& block.getInstructions().get(0).contains(AFlag.SYNTHETIC);
	}

	private static boolean isStraightLoopContinuation(MethodNode mth, BlockNode source) {
		for (BlockNode loopEndPoint : mth.getBasicBlocks()) {
			for (LoopInfo loop : loopEndPoint.getAll(AType.LOOP)) {
				if (!loop.getLoopBlocks().contains(source)) {
					continue;
				}
				BlockNode block = source.getSuccessors().get(0);
				Set<BlockNode> visited = new HashSet<>();
				while (block != null && visited.size() < 8 && visited.add(block)) {
					if (block == loop.getEnd()) {
						return true;
					}
					if (block.getInstructions().stream()
							.anyMatch(insn -> insn.getType() != InsnType.MOVE && insn.getType() != InsnType.CONST)
							|| block.getSuccessors().size() != 1) {
						break;
					}
					block = block.getSuccessors().get(0);
				}
			}
		}
		return false;
	}

	private static boolean isInMarkedLoop(MethodNode mth, BlockNode block) {
		for (BlockNode loopEndPoint : mth.getBasicBlocks()) {
			for (LoopInfo loop : loopEndPoint.getAll(AType.LOOP)) {
				if (loop.getLoopBlocks().contains(block)) {
					return true;
				}
			}
		}
		return false;
	}

	private static boolean onlySwitchAndIfInLastInsns(List<BlockNode> preds) {
		boolean hasSwitch = false;
		boolean hasIf = false;
		for (BlockNode pred : preds) {
			InsnNode lastInsn = BlockUtils.getLastInsn(pred);
			if (lastInsn == null) {
				return false;
			}
			InsnType insnType = lastInsn.getType();
			switch (insnType) {
				case SWITCH:
					hasSwitch = true;
					break;
				case IF:
					hasIf = true;
					break;
				default:
					return false;
			}
		}
		return hasSwitch && hasIf;
	}

	private static boolean onlyIfInLastInsns(List<BlockNode> preds) {
		for (BlockNode pred : preds) {
			InsnNode lastInsn = BlockUtils.getLastInsn(pred);
			if (lastInsn == null || lastInsn.getType() != InsnType.IF) {
				return false;
			}
		}
		return true;
	}

	private static boolean isStringEqualsIf(BlockNode ifBlock) {
		BlockNode block = ifBlock;
		for (int depth = 0; depth < 3 && block != null; depth++) {
			for (InsnNode insn : block.getInstructions()) {
				if (insn instanceof InvokeNode) {
					InvokeNode invoke = (InvokeNode) insn;
					if (invoke.getCallMth().getName().equals("equals")
							&& invoke.getInstanceArg() != null
							&& isStringArg(invoke.getInstanceArg())) {
						return true;
					}
				}
			}
			block = block.getPredecessors().size() == 1 ? block.getPredecessors().get(0) : null;
		}
		return false;
	}

	private static boolean isStringArg(InsnArg arg) {
		if (arg.getType().equals(ArgType.STRING)) {
			return true;
		}
		return arg instanceof RegisterArg && ((RegisterArg) arg).getInitType().equals(ArgType.STRING);
	}

	private static boolean duplicateSharedStringIfDiamond(MethodNode mth, BlockNode firstIfBlock) {
		InsnNode firstLastInsn = BlockUtils.getLastInsn(firstIfBlock);
		if (!(firstLastInsn instanceof IfNode) || !isStringEqualsIf(firstIfBlock)) {
			return false;
		}
		IfNode firstIf = (IfNode) firstLastInsn;
		BlockNode firstTarget = firstIf.getThenBlock();
		BlockNode secondTarget = firstIf.getElseBlock();
		BlockNode join = findStraightJoin(firstTarget, secondTarget, 8);
		if (join == null || join == firstTarget || join == secondTarget) {
			return false;
		}
		List<BlockNode> firstPath = collectStraightPath(firstTarget, join, 4);
		List<BlockNode> secondPath = collectStraightPath(secondTarget, join, 4);
		if (firstPath == null || secondPath == null) {
			return false;
		}
		for (BlockNode candidate : new ArrayList<>(firstTarget.getPredecessors())) {
			if (candidate == firstIfBlock || !secondTarget.getPredecessors().contains(candidate)) {
				continue;
			}
			InsnNode candidateLastInsn = BlockUtils.getLastInsn(candidate);
			if (!(candidateLastInsn instanceof IfNode)
					|| !isStringEqualsIf(candidate)) {
				continue;
			}
			IfNode candidateIf = (IfNode) candidateLastInsn;
			if (!Set.of(candidateIf.getThenBlock(), candidateIf.getElseBlock())
					.equals(Set.of(firstTarget, secondTarget))) {
				continue;
			}
			BlockNode firstCopy = copyStraightPath(mth, firstPath, join);
			BlockNode secondCopy = copyStraightPath(mth, secondPath, join);
			BlockSplitter.replaceConnection(candidate, firstTarget, firstCopy);
			BlockSplitter.replaceConnection(candidate, secondTarget, secondCopy);
			candidateIf.initBlocks(candidate);
			candidate.updateCleanSuccessors();
			return true;
		}
		return false;
	}

	private static @Nullable BlockNode findStraightJoin(BlockNode first, BlockNode second, int maxDepth) {
		Set<BlockNode> firstPath = new HashSet<>();
		BlockNode block = first;
		for (int depth = 0; depth < maxDepth && block != null; depth++) {
			firstPath.add(block);
			block = block.getSuccessors().size() == 1 ? block.getSuccessors().get(0) : null;
		}
		block = second;
		for (int depth = 0; depth < maxDepth && block != null; depth++) {
			if (firstPath.contains(block)) {
				return block;
			}
			block = block.getSuccessors().size() == 1 ? block.getSuccessors().get(0) : null;
		}
		return null;
	}

	private static BlockNode copyStraightPath(MethodNode mth, List<BlockNode> path, BlockNode exit) {
		BlockNode firstCopy = null;
		BlockNode previous = null;
		for (BlockNode oldBlock : path) {
			BlockNode copyBlock = BlockSplitter.startNewBlock(mth, oldBlock.getStartOffset());
			copyBlock.add(AFlag.SYNTHETIC);
			for (InsnNode oldInsn : oldBlock.getInstructions()) {
				InsnNode copyInsn = oldInsn.copyWithoutSsa();
				copyInsn.add(AFlag.SYNTHETIC);
				copyBlock.getInstructions().add(copyInsn);
			}
			copyBlock.copyAttributesFrom(oldBlock);
			if (firstCopy == null) {
				firstCopy = copyBlock;
			}
			if (previous != null) {
				BlockSplitter.connect(previous, copyBlock);
			}
			previous = copyBlock;
		}
		BlockSplitter.connect(previous, exit);
		return firstCopy;
	}

	private static boolean duplicateSharedIfReturnBranch(MethodNode mth, BlockNode body) {
		if (!mth.getMethodInfo().getArgumentsTypes().contains(ArgType.STRING)) {
			return false;
		}
		List<BlockNode> preds = body.getPredecessors();
		if (preds.size() < 2) {
			return false;
		}
		for (int firstIdx = 0; firstIdx < preds.size() - 1; firstIdx++) {
			BlockNode firstPred = preds.get(firstIdx);
			BlockNode commonExit = getOtherIfSuccessor(firstPred, body);
			if (commonExit == null) {
				continue;
			}
			for (int secondIdx = firstIdx + 1; secondIdx < preds.size(); secondIdx++) {
				BlockNode secondPred = preds.get(secondIdx);
				if (getOtherIfSuccessor(secondPred, body) != commonExit
						|| !hasCommonSwitchDominator(mth, firstPred, secondPred)
						|| !isStringEqualsIf(firstPred)
						|| !isStringEqualsIf(secondPred)) {
					continue;
				}
				Set<BlockNode> branch = collectReturnBranch(mth, body, commonExit, 32);
				if (branch == null) {
					continue;
				}
				Map<BlockNode, BlockNode> copies = new HashMap<>(branch.size());
				for (BlockNode oldBlock : branch) {
					BlockNode copyBlock = BlockSplitter.startNewBlock(mth, oldBlock.getStartOffset());
					copyBlock.add(AFlag.SYNTHETIC);
					for (InsnNode oldInsn : oldBlock.getInstructions()) {
						InsnNode copyInsn = oldInsn.copyWithoutSsa();
						copyInsn.add(AFlag.SYNTHETIC);
						copyBlock.getInstructions().add(copyInsn);
					}
					copyBlock.copyAttributesFrom(oldBlock);
					copies.put(oldBlock, copyBlock);
				}
				BlockSplitter.replaceConnection(secondPred, body, copies.get(body));
				for (BlockNode oldBlock : branch) {
					BlockNode copyBlock = copies.get(oldBlock);
					for (BlockNode successor : oldBlock.getSuccessors()) {
						BlockNode copySuccessor = copies.get(successor);
						BlockSplitter.connect(copyBlock, copySuccessor != null ? copySuccessor : successor);
					}
				}
				for (BlockNode copyBlock : copies.values()) {
					InsnNode lastInsn = BlockUtils.getLastInsn(copyBlock);
					if (lastInsn instanceof IfNode) {
						((IfNode) lastInsn).initBlocks(copyBlock);
					}
					copyBlock.updateCleanSuccessors();
				}
				return true;
			}
		}
		return false;
	}

	private static @Nullable Set<BlockNode> collectReturnBranch(MethodNode mth, BlockNode start,
			BlockNode commonExit, int maxBlocks) {
		Set<BlockNode> branch = new LinkedHashSet<>();
		ArrayDeque<BlockNode> queue = new ArrayDeque<>();
		queue.add(start);
		boolean hasReturn = false;
		while (!queue.isEmpty()) {
			BlockNode block = queue.removeFirst();
			if (block == commonExit || block == mth.getExitBlock()) {
				return null;
			}
			if (!branch.add(block)) {
				continue;
			}
			if (branch.size() > maxBlocks
					|| block.contains(AType.LOOP)
					|| BlockUtils.isExceptionHandlerPath(block)) {
				return null;
			}
			hasReturn |= block.isReturnBlock();
			for (BlockNode successor : block.getSuccessors()) {
				if (successor == commonExit) {
					return null;
				}
				if (successor != mth.getExitBlock()) {
					queue.add(successor);
				}
			}
		}
		if (!hasReturn) {
			return null;
		}
		for (BlockNode block : branch) {
			if (block != start && block.getPredecessors().stream().anyMatch(pred -> !branch.contains(pred))) {
				return null;
			}
			if (block.getSuccessors().stream()
					.anyMatch(successor -> successor != mth.getExitBlock() && !branch.contains(successor))) {
				return null;
			}
		}
		return branch;
	}

	/**
	 * Split a straight-line body shared by equivalent conditional exits.
	 *
	 * D8/R8 can fold switch cases such as a regular field and its oneof variant so
	 * both conditional case entries jump into the same size/calculation tail. A
	 * region can only own that tail once, leaving the other case condition outside
	 * the switch region. Keep the common exit, but give one condition its own copy
	 * of the short true branch.
	 */
	private static boolean duplicateSharedIfTail(MethodNode mth, BlockNode body) {
		if (isCoroutineMethod(mth)) {
			return false;
		}
		List<BlockNode> preds = body.getPredecessors();
		if (preds.size() < 2) {
			return false;
		}
		for (int firstIdx = 0; firstIdx < preds.size() - 1; firstIdx++) {
			BlockNode firstPred = preds.get(firstIdx);
			if (BlockUtils.getLastInsn(firstPred) == null
					|| BlockUtils.getLastInsn(firstPred).getType() != InsnType.IF) {
				continue;
			}
			BlockNode commonExit = getOtherIfSuccessor(firstPred, body);
			if (commonExit == null) {
				continue;
			}
			for (int secondIdx = firstIdx + 1; secondIdx < preds.size(); secondIdx++) {
				BlockNode secondPred = preds.get(secondIdx);
				if (getOtherIfSuccessor(secondPred, body) != commonExit) {
					continue;
				}
				if (!hasCommonSwitchDominator(mth, firstPred, secondPred)) {
					continue;
				}
				List<BlockNode> path = collectStraightPath(body, commonExit, 4);
				if (path == null) {
					continue;
				}
				BlockNode previous = secondPred;
				for (BlockNode oldBlock : path) {
					BlockNode copyBlock = BlockSplitter.startNewBlock(mth, -1);
					copyBlock.add(AFlag.SYNTHETIC);
					for (InsnNode oldInsn : oldBlock.getInstructions()) {
						InsnNode copyInsn = oldInsn.copyWithoutSsa();
						copyInsn.add(AFlag.SYNTHETIC);
						copyBlock.getInstructions().add(copyInsn);
					}
					copyBlock.copyAttributesFrom(oldBlock);
					if (previous == secondPred) {
						BlockSplitter.replaceConnection(secondPred, body, copyBlock);
					} else {
						BlockSplitter.connect(previous, copyBlock);
					}
					previous = copyBlock;
				}
				BlockSplitter.connect(previous, commonExit);
				return true;
			}
		}
		return false;
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

	private static boolean hasCommonSwitchDominator(MethodNode mth, BlockNode first, BlockNode second) {
		BlockNode commonDominator = BlockUtils.getCommonDominator(mth, List.of(first, second));
		for (BlockNode block = commonDominator; block != null; block = block.getIDom()) {
			InsnNode lastInsn = BlockUtils.getLastInsn(block);
			if (lastInsn != null && lastInsn.getType() == InsnType.SWITCH) {
				return true;
			}
		}
		return false;
	}

	private static BlockNode getOtherIfSuccessor(BlockNode ifBlock, BlockNode body) {
		InsnNode lastInsn = BlockUtils.getLastInsn(ifBlock);
		if (lastInsn == null || lastInsn.getType() != InsnType.IF || ifBlock.getSuccessors().size() != 2) {
			return null;
		}
		if (ifBlock.getSuccessors().get(0) == body) {
			return ifBlock.getSuccessors().get(1);
		}
		if (ifBlock.getSuccessors().get(1) == body) {
			return ifBlock.getSuccessors().get(0);
		}
		return null;
	}

	private static List<BlockNode> collectStraightPath(BlockNode start, BlockNode exit, int maxLength) {
		List<BlockNode> path = new ArrayList<>(maxLength);
		BlockNode current = start;
		while (current != exit && path.size() < maxLength) {
			if (current.getSuccessors().size() != 1
					|| current.contains(AFlag.LOOP_START)
					|| current.contains(AFlag.LOOP_END)) {
				return null;
			}
			InsnNode lastInsn = BlockUtils.getLastInsn(current);
			if (lastInsn != null && (lastInsn.getType() == InsnType.IF || lastInsn.getType() == InsnType.SWITCH)) {
				return null;
			}
			path.add(current);
			current = current.getSuccessors().get(0);
		}
		return current == exit && !path.isEmpty() ? path : null;
	}

	private static boolean simplifyLoopEnd(MethodNode mth, LoopInfo loop) {
		BlockNode loopEnd = loop.getEnd();
		if (loopEnd.getSuccessors().size() <= 1) {
			return false;
		}
		// make loop end a simple path block
		BlockNode newLoopEnd = BlockSplitter.startNewBlock(mth, -1);
		newLoopEnd.add(AFlag.SYNTHETIC);
		newLoopEnd.add(AFlag.LOOP_END);
		BlockNode loopStart = loop.getStart();
		BlockSplitter.replaceConnection(loopEnd, loopStart, newLoopEnd);
		BlockSplitter.connect(newLoopEnd, loopStart);
		if (DEBUG_MODS) {
			mth.get(DebugModAttr.TYPE).addEvent("Simplify loop end");
		}
		return true;
	}

	private static boolean checkLoops(MethodNode mth, BlockNode block) {
		if (!block.contains(AFlag.LOOP_START)) {
			return false;
		}
		List<LoopInfo> loops = block.getAll(AType.LOOP);
		int loopsCount = loops.size();
		if (loopsCount == 0) {
			return false;
		}
		for (LoopInfo loop : loops) {
			if (insertBlocksForBreak(mth, loop)) {
				return true;
			}
		}
		if (loopsCount > 1 && splitLoops(mth, block, loops)) {
			return true;
		}
		if (loopsCount == 1) {
			LoopInfo loop = loops.get(0);
			return insertBlocksForContinue(mth, loop)
					|| insertPreHeader(mth, loop)
					|| simplifyLoopEnd(mth, loop);
		}
		return false;
	}

	/**
	 * Insert simple path block before loop header
	 */
	private static boolean insertPreHeader(MethodNode mth, LoopInfo loop) {
		BlockNode start = loop.getStart();
		List<BlockNode> preds = start.getPredecessors();
		int predsCount = preds.size() - 1; // don't count back edge
		if (predsCount == 1) {
			return false;
		}
		if (predsCount == 0) {
			if (!start.contains(AFlag.MTH_ENTER_BLOCK)) {
				mth.addWarnComment("Unexpected block without predecessors: " + start);
			}
			BlockNode newEnterBlock = BlockSplitter.startNewBlock(mth, -1);
			newEnterBlock.add(AFlag.SYNTHETIC);
			newEnterBlock.add(AFlag.MTH_ENTER_BLOCK);
			mth.setEnterBlock(newEnterBlock);
			start.remove(AFlag.MTH_ENTER_BLOCK);
			BlockSplitter.connect(newEnterBlock, start);
		} else {
			// multiple predecessors
			BlockNode preHeader = BlockSplitter.startNewBlock(mth, -1);
			preHeader.add(AFlag.SYNTHETIC);
			BlockNode loopEnd = loop.getEnd();
			for (BlockNode pred : new ArrayList<>(preds)) {
				if (pred != loopEnd) {
					BlockSplitter.replaceConnection(pred, start, preHeader);
				}
			}
			BlockSplitter.connect(preHeader, start);
		}
		if (DEBUG_MODS) {
			mth.get(DebugModAttr.TYPE).addEvent("Insert loop pre header");
		}
		return true;
	}

	/**
	 * Insert additional blocks for possible 'break' insertion
	 */
	private static boolean insertBlocksForBreak(MethodNode mth, LoopInfo loop) {
		boolean change = false;
		List<Edge> edges = loop.getExitEdges();
		if (!edges.isEmpty()) {
			for (Edge edge : edges) {
				BlockNode target = edge.getTarget();
				BlockNode source = edge.getSource();
				if (!target.contains(AFlag.SYNTHETIC) && !source.contains(AFlag.SYNTHETIC)) {
					BlockSplitter.insertBlockBetween(mth, source, target);
					change = true;
				}
			}
		}
		if (DEBUG_MODS && change) {
			mth.get(DebugModAttr.TYPE).addEvent("Insert loop break blocks");
		}
		return change;
	}

	/**
	 * Insert additional blocks for possible 'continue' insertion
	 */
	private static boolean insertBlocksForContinue(MethodNode mth, LoopInfo loop) {
		BlockNode loopEnd = loop.getEnd();
		boolean change = false;
		List<BlockNode> preds = loopEnd.getPredecessors();
		if (preds.size() > 1) {
			for (BlockNode pred : new ArrayList<>(preds)) {
				if (!pred.contains(AFlag.SYNTHETIC)) {
					BlockSplitter.insertBlockBetween(mth, pred, loopEnd);
					change = true;
				}
			}
		}
		if (DEBUG_MODS && change) {
			mth.get(DebugModAttr.TYPE).addEvent("Insert loop continue block");
		}
		return change;
	}

	private static boolean splitLoops(MethodNode mth, BlockNode block, List<LoopInfo> loops) {
		boolean oneHeader = true;
		for (LoopInfo loop : loops) {
			if (loop.getStart() != block) {
				oneHeader = false;
				break;
			}
		}
		if (!oneHeader) {
			return false;
		}
		// several back edges connected to one loop header => make additional block
		BlockNode newLoopEnd = BlockSplitter.startNewBlock(mth, block.getStartOffset());
		newLoopEnd.add(AFlag.SYNTHETIC);
		connect(newLoopEnd, block);
		for (LoopInfo la : loops) {
			BlockSplitter.replaceConnection(la.getEnd(), block, newLoopEnd);
		}
		if (DEBUG_MODS) {
			mth.get(DebugModAttr.TYPE).addEvent("Split loops");
		}
		return true;
	}

	private static boolean splitExitBlocks(MethodNode mth) {
		boolean changed = false;
		for (BlockNode preExitBlock : mth.getPreExitBlocks()) {
			if (splitReturn(mth, preExitBlock)) {
				changed = true;
			} else if (splitThrow(mth, preExitBlock)) {
				changed = true;
			}
		}
		if (changed) {
			updateExitBlockConnections(mth);
			if (DEBUG_MODS) {
				mth.get(DebugModAttr.TYPE).addEvent("Split exit block");
			}
		}
		return changed;
	}

	private static void updateExitBlockConnections(MethodNode mth) {
		BlockNode exitBlock = mth.getExitBlock();
		BlockSplitter.removePredecessors(exitBlock);
		for (BlockNode block : mth.getBasicBlocks()) {
			if (block != exitBlock
					&& block.getSuccessors().isEmpty()
					&& !block.contains(AFlag.REMOVE)) {
				BlockSplitter.connect(block, exitBlock);
			}
		}
	}

	/**
	 * Splice return block if several predecessors presents
	 */
	private static boolean splitReturn(MethodNode mth, BlockNode returnBlock) {
		if (returnBlock.contains(AFlag.SYNTHETIC)
				|| returnBlock.contains(AFlag.ORIG_RETURN)
				|| returnBlock.contains(AType.EXC_HANDLER)) {
			return false;
		}
		List<BlockNode> preds = returnBlock.getPredecessors();
		if (preds.size() < 2) {
			return false;
		}
		InsnNode returnInsn = BlockUtils.getLastInsn(returnBlock);
		if (returnInsn == null) {
			return false;
		}
		if (returnInsn.getArgsCount() == 1
				&& returnBlock.getInstructions().size() == 1
				&& !isArgAssignInPred(preds, returnInsn.getArg(0))) {
			return false;
		}

		boolean first = true;
		for (BlockNode pred : new ArrayList<>(preds)) {
			if (first) {
				returnBlock.add(AFlag.ORIG_RETURN);
				first = false;
			} else {
				BlockNode newRetBlock = BlockSplitter.startNewBlock(mth, -1);
				newRetBlock.add(AFlag.SYNTHETIC);
				newRetBlock.add(AFlag.RETURN);
				for (InsnNode oldInsn : returnBlock.getInstructions()) {
					InsnNode copyInsn = oldInsn.copyWithoutSsa();
					copyInsn.add(AFlag.SYNTHETIC);
					newRetBlock.getInstructions().add(copyInsn);
				}
				BlockSplitter.replaceConnection(pred, returnBlock, newRetBlock);
			}
		}
		return true;
	}

	private static boolean splitThrow(MethodNode mth, BlockNode exitBlock) {
		if (exitBlock.contains(AFlag.IGNORE_THROW_SPLIT)) {
			return false;
		}
		List<BlockNode> preds = exitBlock.getPredecessors();
		if (preds.size() < 2) {
			return false;
		}
		InsnNode throwInsn = BlockUtils.getLastInsn(exitBlock);
		if (throwInsn == null || throwInsn.getType() != InsnType.THROW) {
			return false;
		}
		// split only for several exception handlers
		// traverse predecessors to exception handler
		Map<BlockNode, ExcHandlerAttr> handlersMap = new HashMap<>(preds.size());
		Set<BlockNode> handlers = new HashSet<>(preds.size());
		for (BlockNode pred : preds) {
			BlockUtils.visitPredecessorsUntil(mth, pred, block -> {
				ExcHandlerAttr excHandlerAttr = block.get(AType.EXC_HANDLER);
				if (excHandlerAttr == null) {
					return false;
				}
				boolean correctHandler = excHandlerAttr.getHandler().getBlocks().contains(block);
				if (correctHandler && isArgAssignInPred(Collections.singletonList(block), throwInsn.getArg(0))) {
					handlersMap.put(pred, excHandlerAttr);
					handlers.add(block);
				}
				return correctHandler;
			});
		}
		if (handlers.size() == 1) {
			exitBlock.add(AFlag.IGNORE_THROW_SPLIT);
			return false;
		}

		boolean first = true;
		for (BlockNode pred : new ArrayList<>(preds)) {
			if (first) {
				first = false;
			} else {
				BlockNode newThrowBlock = BlockSplitter.startNewBlock(mth, -1);
				newThrowBlock.add(AFlag.SYNTHETIC);
				for (InsnNode oldInsn : exitBlock.getInstructions()) {
					InsnNode copyInsn = oldInsn.copyWithoutSsa();
					copyInsn.add(AFlag.SYNTHETIC);
					newThrowBlock.getInstructions().add(copyInsn);
				}
				newThrowBlock.copyAttributesFrom(exitBlock);
				ExcHandlerAttr excHandlerAttr = handlersMap.get(pred);
				if (excHandlerAttr != null) {
					excHandlerAttr.getHandler().addBlock(newThrowBlock);
				}
				BlockSplitter.replaceConnection(pred, exitBlock, newThrowBlock);
			}
		}
		return true;
	}

	private static boolean isArgAssignInPred(List<BlockNode> preds, InsnArg arg) {
		if (arg.isRegister()) {
			int regNum = ((RegisterArg) arg).getRegNum();
			for (BlockNode pred : preds) {
				for (InsnNode insnNode : pred.getInstructions()) {
					RegisterArg result = insnNode.getResult();
					if (result != null && result.getRegNum() == regNum) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public static void removeMarkedBlocks(MethodNode mth) {
		boolean removed = mth.getBasicBlocks().removeIf(block -> {
			if (block.contains(AFlag.REMOVE)) {
				if (!block.getPredecessors().isEmpty() || !block.getSuccessors().isEmpty()) {
					LOG.warn("Block {} not deleted, method: {}", block, mth);
				} else {
					TryCatchBlockAttr tryBlockAttr = block.get(AType.TRY_BLOCK);
					if (tryBlockAttr != null) {
						tryBlockAttr.removeBlock(block);
					}
					return true;
				}
			}
			return false;
		});
		if (removed) {
			mth.updateBlockPositions();
		}
	}

	private static void removeUnreachableBlocks(MethodNode mth) {
		Set<BlockNode> toRemove = new LinkedHashSet<>();
		for (BlockNode block : mth.getBasicBlocks()) {
			computeUnreachableFromBlock(toRemove, block, mth);
		}
		removeFromMethod(toRemove, mth);
	}

	public static void removeUnreachableBlock(BlockNode blockToRemove, MethodNode mth) {
		Set<BlockNode> toRemove = new LinkedHashSet<>();
		computeUnreachableFromBlock(toRemove, blockToRemove, mth);
		removeFromMethod(toRemove, mth);
	}

	private static void computeUnreachableFromBlock(Set<BlockNode> toRemove, BlockNode block, MethodNode mth) {
		if (block.getPredecessors().isEmpty() && block != mth.getEnterBlock()) {
			BlockSplitter.collectSuccessors(block, mth.getEnterBlock(), toRemove);
		}
	}

	private static void removeFromMethod(Set<BlockNode> toRemove, MethodNode mth) {
		if (toRemove.isEmpty()) {
			return;
		}

		long notEmptyBlocks = toRemove.stream().filter(block -> !block.getInstructions().isEmpty()).count();
		if (notEmptyBlocks != 0) {
			int insnsCount = toRemove.stream().mapToInt(block -> block.getInstructions().size()).sum();
			mth.addWarnComment("Unreachable blocks removed: " + notEmptyBlocks + ", instructions: " + insnsCount);
		}

		toRemove.forEach(BlockSplitter::detachBlock);
		mth.getBasicBlocks().removeAll(toRemove);
		mth.updateBlockPositions();
	}

	private static void clearBlocksState(MethodNode mth) {
		mth.getBasicBlocks().forEach(block -> {
			block.remove(AType.LOOP);
			block.remove(AFlag.LOOP_START);
			block.remove(AFlag.LOOP_END);
			block.setDoms(null);
			block.setIDom(null);
			block.setDomFrontier(null);
			block.getDominatesOn().clear();
		});
	}

	private static final class DebugModAttr implements IJadxAttribute {
		static final IJadxAttrType<DebugModAttr> TYPE = IJadxAttrType.create("DebugModAttr");

		private final Map<String, Integer> statMap = new HashMap<>();

		public void addEvent(String name) {
			statMap.merge(name, 1, Integer::sum);
		}

		public String formatStats() {
			return statMap.entrySet().stream()
					.map(entry -> " " + entry.getKey() + ": " + entry.getValue())
					.collect(Collectors.joining("\n"));
		}

		@Override
		public IJadxAttrType<DebugModAttr> getAttrType() {
			return TYPE;
		}
	}
}
