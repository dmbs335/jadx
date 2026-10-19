package jadx.core.dex.visitors.regions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jadx.api.ICodeWriter;
import jadx.api.impl.SimpleCodeWriter;
import jadx.core.Consts;
import jadx.core.codegen.InsnGen;
import jadx.core.codegen.MethodGen;
import jadx.core.dex.attributes.AFlag;
import jadx.core.dex.attributes.AType;
import jadx.core.dex.instructions.InsnType;
import jadx.core.dex.instructions.PhiInsn;
import jadx.core.dex.instructions.args.ArgType;
import jadx.core.dex.instructions.args.InsnArg;
import jadx.core.dex.instructions.args.RegisterArg;
import jadx.core.dex.instructions.args.SSAVar;
import jadx.core.dex.nodes.BlockNode;
import jadx.core.dex.nodes.IBlock;
import jadx.core.dex.nodes.IRegion;
import jadx.core.dex.nodes.InsnNode;
import jadx.core.dex.nodes.MethodNode;
import jadx.core.dex.regions.loops.LoopRegion;
import jadx.core.dex.visitors.AbstractVisitor;
import jadx.core.utils.BlockUtils;
import jadx.core.utils.InsnRemover;
import jadx.core.utils.RegionUtils;
import jadx.core.utils.exceptions.CodegenException;
import jadx.core.utils.exceptions.JadxException;

public class CheckRegions extends AbstractVisitor {
	private static final Logger LOG = LoggerFactory.getLogger(CheckRegions.class);

	@Override
	public void visit(MethodNode mth) throws JadxException {
		if (mth.isNoCode()
				|| mth.getRegion() == null
				|| mth.getBasicBlocks().isEmpty()
				|| mth.contains(AType.JADX_ERROR)) {
			return;
		}

		// check if all blocks included in regions
		Set<BlockNode> blocksInRegions = new HashSet<>();
		Map<BlockNode, Integer> regionBlockUses = mth.contains(AType.UNSAFE_REGION_DUPLICATION)
				? new HashMap<>()
				: null;
		DepthRegionTraversal.traverse(mth, new AbstractRegionVisitor() {
			@Override
			public void processBlock(MethodNode mth, IBlock container) {
				if (!(container instanceof BlockNode)) {
					return;
				}
				BlockNode block = (BlockNode) container;
				if (regionBlockUses != null) {
					regionBlockUses.merge(block, 1, Integer::sum);
				}
				if (blocksInRegions.add(block)) {
					return;
				}
				if (Consts.DEBUG_RESTRUCTURE
						&& LOG.isDebugEnabled()
						&& !block.contains(AFlag.RETURN)
						&& !block.contains(AFlag.REMOVE)
						&& !block.contains(AFlag.SYNTHETIC)
						&& !block.getInstructions().isEmpty()) {
					LOG.debug("Duplicated block: {} - {}", mth, block);
				}
			}
		});
		if (regionBlockUses != null) {
			reportRemainingUnsafeDuplications(mth, regionBlockUses);
		}
		if (mth.getBasicBlocks().size() != blocksInRegions.size()) {
			for (BlockNode block : mth.getBasicBlocks()) {
				if (!blocksInRegions.contains(block) && inlineOrphanPhiProjectionMove(mth, block)) {
					continue;
				}
				if (!blocksInRegions.contains(block) && inlineOrphanDominatedProjectionMoves(mth, block)) {
					continue;
				}
				if (!blocksInRegions.contains(block) && inlineSyntheticSameRegisterMove(mth, block)) {
					continue;
				}
				if (!blocksInRegions.contains(block)
						&& !block.getInstructions().isEmpty()
						&& !block.contains(AFlag.ADDED_TO_REGION)
						&& !block.contains(AFlag.DONT_GENERATE)
						&& !block.contains(AFlag.REMOVE)
						&& !isImplicitVoidReturn(mth, block, blocksInRegions)
						&& !isImplicitRemovedHandlerRethrow(mth, block)
						&& !isGeneratedSyntheticDuplicate(block, blocksInRegions)) {
					String blockCode = getBlockInsnStr(mth, block).replace("*/", "*\\/");
					mth.addError("Code restructure failed: missing block: " + block + ", code lost:" + blockCode, null);
				}
			}
		}

		DepthRegionTraversal.traverse(mth, new AbstractRegionVisitor() {
			@Override
			public boolean enterRegion(MethodNode mth, IRegion region) {
				if (region instanceof LoopRegion) {
					// check loop conditions
					BlockNode loopHeader = ((LoopRegion) region).getHeader();
					if (hasInvalidLoopCondition(loopHeader)) {
						mth.addError("Incorrect condition in loop: " + loopHeader, null);
					}
				}
				return true;
			}
		});
		// Splitting a receiver PHI can expose a missing loop continuation in the region tree.
		// Other normalized coroutine loops may intentionally be represented by an endless loop,
		// which has no separate exit block and is not by itself a codegen failure.
		if (mth.contains(AType.NORMALIZED_COROUTINE_LOOP)
				&& mth.contains(AType.UNRESOLVED_REGION_TRAVERSAL_CYCLE)
				&& mth.getSVars().stream().anyMatch(var -> var.isCodeVarSet() && var.getCodeVar().isInitFromThis())
				&& !ArgType.VOID.equals(mth.getReturnType())
				&& !RegionUtils.hasExitBlock(mth.getRegion())) {
			mth.addError("Code restructure failed: non-void coroutine has a fall-through path after a region traversal cycle", null);
		}
		if (!mth.contains(AType.JADX_ERROR)) {
			List<String> unresolved = new ArrayList<>(3);
			if (mth.contains(AType.UNSUPPORTED_MULTI_ENTRY_LOOP)) {
				unresolved.add("unsupported multi-entry loop");
			}
			if (mth.contains(AType.UNRESOLVED_REGION_RECURSION)) {
				unresolved.add("recursive region traversal");
			}
			// A traversal-cycle marker alone can remain after a usable loop region is
			// constructed. It is not a proof of lost code without another unresolved
			// restructuring failure (the non-void fall-through check above is separate).
			if (!unresolved.isEmpty() && mth.contains(AType.UNRESOLVED_REGION_TRAVERSAL_CYCLE)) {
				unresolved.add("region traversal cycle");
			}
			if (!unresolved.isEmpty()) {
				mth.addError("Unresolved control-flow restructuring: " + String.join(", ", unresolved), null);
			}
		}
	}

	static boolean hasInvalidLoopCondition(BlockNode loopHeader) {
		return loopHeader != null
				&& !loopHeader.contains(AFlag.ALLOW_MULTIPLE_INSNS_LOOP_COND)
				&& loopHeader.getInstructions().size() != 1;
	}

	private static void reportRemainingUnsafeDuplications(MethodNode mth, Map<BlockNode, Integer> regionBlockUses) {
		var unsafeAttr = mth.get(AType.UNSAFE_REGION_DUPLICATION);
		if (unsafeAttr == null) {
			return;
		}
		List<BlockNode> remaining = findRemainingUnsafeDuplications(unsafeAttr.getList(), regionBlockUses);
		if (remaining.isEmpty()) {
			return;
		}
		BlockNode firstBlock = remaining.get(0);
		String message = "Unsafe code duplication in " + remaining.size()
				+ " blocks, first: " + firstBlock + ' ' + firstBlock.getAttributesString();
		// Region simplification can eliminate a temporary duplicate. Report only blocks still
		// emitted more than once, after CleanRegions has finalized the region tree.
		if (mth.contains(AType.UNRESOLVED_REGION_RECURSION)
				&& !mth.isNoExceptionHandlers()
				&& remaining.stream().anyMatch(CheckRegions::hasCatchProtection)) {
			// A recursive region was truncated, and a protected block is still emitted twice.
			// Unlike a temporary duplicate removed by CleanRegions, this cannot be trusted
			// to preserve both the normal and exceptional continuation.
			mth.addWarn(message);
		} else if (mth.contains(AType.NORMALIZED_COROUTINE_LOOP)
				&& mth.contains(AType.COROUTINE_LOOP_PRE_HEADER)
				&& mth.isNoExceptionHandlers()) {
			mth.addWarn(message);
		} else if (mth.contains(AType.UNSUPPORTED_MULTI_ENTRY_LOOP)) {
			mth.addInfoComment(message + " (caused by unsupported multi-entry loop)");
		} else if (mth.contains(AType.NORMALIZED_COROUTINE_LOOP)) {
			mth.addInfoComment(message + " (inside normalized coroutine loop)");
		} else {
			mth.addWarnComment(message);
		}
	}

	private static boolean hasCatchProtection(BlockNode block) {
		return block.contains(AFlag.TRY_ENTER)
				|| block.contains(AType.EXC_CATCH)
				|| block.getInstructions().stream().anyMatch(insn -> insn.get(AType.EXC_CATCH) != null);
	}

	static List<BlockNode> findRemainingUnsafeDuplications(
			List<BlockNode> candidates, Map<BlockNode, Integer> regionBlockUses) {
		return candidates.stream()
				.filter(block -> regionBlockUses.getOrDefault(block, 0) > 1)
				.toList();
	}

	/**
	 * Structured synchronized/finally regions implicitly rethrow the active exception. Once their
	 * synthetic catch handler is removed, the original bytecode rethrow can remain as an orphan
	 * block. Ignore it only when every incoming edge was removed and every SSA source of the thrown
	 * value is a MOVE_EXCEPTION from a removed handler; an application-authored throw cannot satisfy
	 * this proof.
	 */
	static boolean isImplicitRemovedHandlerRethrow(MethodNode mth, BlockNode block) {
		if (block.getInstructions().size() != 1
				|| block.getPredecessors().isEmpty()
				|| block.getPredecessors().stream().anyMatch(pred -> !pred.contains(AFlag.REMOVE))) {
			return false;
		}
		InsnNode throwInsn = block.getInstructions().get(0);
		if (throwInsn.getType() != InsnType.THROW
				|| throwInsn.getArgsCount() != 1
				|| !throwInsn.getArg(0).isRegister()) {
			return false;
		}
		Set<SSAVar> visited = new HashSet<>();
		List<RegisterArg> pending = new ArrayList<>();
		pending.add((RegisterArg) throwInsn.getArg(0));
		boolean moveExceptionFound = false;
		for (int pos = 0; pos < pending.size(); pos++) {
			RegisterArg arg = pending.get(pos);
			var ssaVar = arg.getSVar();
			if (ssaVar == null || !visited.add(ssaVar)) {
				continue;
			}
			InsnNode assignInsn = ssaVar.getAssignInsn();
			if (assignInsn == null) {
				return false;
			}
			if (assignInsn.getType() == InsnType.MOVE_EXCEPTION) {
				BlockNode assignBlock = BlockUtils.getBlockByInsn(mth, assignInsn);
				if (assignBlock == null || !assignBlock.contains(AFlag.REMOVE)) {
					return false;
				}
				moveExceptionFound = true;
				continue;
			}
			if (assignInsn.getType() != InsnType.MOVE && assignInsn.getType() != InsnType.PHI) {
				return false;
			}
			for (InsnArg sourceArg : assignInsn.getArguments()) {
				if (!sourceArg.isRegister()) {
					return false;
				}
				pending.add((RegisterArg) sourceArg);
			}
		}
		return moveExceptionFound;
	}

	/**
	 * A shared return-void block may remain outside the region tree after all live predecessors have
	 * been represented. Falling through the Java method brace is exactly equivalent; instructions in
	 * any predecessor still outside the tree keep the normal lost-code warning.
	 */
	static boolean isImplicitVoidReturn(MethodNode mth, BlockNode block, Set<BlockNode> blocksInRegions) {
		if (!mth.getReturnType().equals(jadx.core.dex.instructions.args.ArgType.VOID)
				|| block.getInstructions().size() != 1) {
			return false;
		}
		InsnNode insn = block.getInstructions().get(0);
		if (insn.getType() != InsnType.RETURN || insn.getArgsCount() != 0
				|| block.getPredecessors().isEmpty()) {
			return false;
		}
		return block.getPredecessors().stream().allMatch(predecessor -> blocksInRegions.contains(predecessor)
				|| predecessor.contains(AFlag.DONT_GENERATE)
				|| predecessor.contains(AFlag.REMOVE));
	}

	static boolean isGeneratedSyntheticDuplicate(BlockNode block, Set<BlockNode> blocksInRegions) {
		return block.contains(AFlag.SYNTHETIC)
				&& blocksInRegions.stream().anyMatch(generated -> BlockUtils.isDuplicateBlockPath(block, generated));
	}

	/**
	 * Region simplification can inline every effective instruction from a branch block and leave only
	 * a synthetic SSA bridge behind. A bridge between two versions of the same physical DEX register
	 * with the exact same known type does not represent a runtime write.
	 *
	 * Redirecting its Java consumers to the reaching source avoids both a lost-code warning and an
	 * uninitialized split local. Keep this proof deliberately narrow: one synthetic register MOVE,
	 * the same register number, the same known type, and only attached consumers.
	 */
	static boolean isSyntheticSameRegisterMoveBlock(BlockNode block) {
		if (block.getInstructions().size() != 1) {
			return false;
		}
		InsnNode insn = block.getInstructions().get(0);
		if (insn.getType() != InsnType.MOVE || insn.getArgsCount() != 1 || !insn.contains(AFlag.SYNTHETIC)) {
			return false;
		}
		RegisterArg result = insn.getResult();
		InsnArg sourceArg = insn.getArg(0);
		if (result == null || result.getSVar() == null || !sourceArg.isRegister()) {
			return false;
		}
		RegisterArg source = (RegisterArg) sourceArg;
		return source.getSVar() != null
				&& result.getRegNum() == source.getRegNum()
				&& result.getType().isTypeKnown()
				&& result.getType().equals(source.getType())
				&& result.getSVar().getUseList().stream().allMatch(use -> use.getParentInsn() != null);
	}

	private static boolean inlineSyntheticSameRegisterMove(MethodNode mth, BlockNode block) {
		if (!isSyntheticSameRegisterMoveBlock(block)) {
			return false;
		}
		InsnNode move = block.getInstructions().get(0);
		RegisterArg source = (RegisterArg) move.getArg(0);
		for (RegisterArg use : new ArrayList<>(move.getResult().getSVar().getUseList())) {
			use.getSVar().removeUse(use);
			source.getSVar().use(use);
		}
		InsnRemover.remove(mth, block, move);
		return true;
	}

	/**
	 * A register allocator can leave a one-instruction projection block on one incoming edge of a
	 * finally PHI. If finally extraction already represents the edge, that block has no legal place
	 * in the region tree even though the projected value is still valid at the PHI. Redirect the PHI
	 * input to the dominating source and remove the bytecode-only register copy.
	 */
	static boolean isOrphanPhiProjectionMoveBlock(BlockNode block) {
		if (block.getInstructions().isEmpty()) {
			return false;
		}
		return block.getInstructions().stream().allMatch(move -> isPhiProjectionMove(block, move));
	}

	private static boolean isPhiProjectionMove(BlockNode block, InsnNode move) {
		if (move.getType() != InsnType.MOVE || move.getArgsCount() != 1 || !move.getArg(0).isRegister()) {
			return false;
		}
		RegisterArg result = move.getResult();
		RegisterArg source = (RegisterArg) move.getArg(0);
		if (result == null || result.getSVar() == null || source.getSVar() == null
				|| result.getSVar().getUseList().isEmpty()
				|| result.getType().isTypeKnown() && source.getType().isTypeKnown()
						&& !result.getType().equals(source.getType())) {
			return false;
		}
		for (RegisterArg use : result.getSVar().getUseList()) {
			if (!(use.getParentInsn() instanceof PhiInsn)
					|| ((PhiInsn) use.getParentInsn()).getBlockByArg(use) != block) {
				return false;
			}
		}
		return true;
	}

	private static boolean inlineOrphanPhiProjectionMove(MethodNode mth, BlockNode block) {
		if (!isOrphanPhiProjectionMoveBlock(block)) {
			return false;
		}
		List<InsnNode> moves = new ArrayList<>(block.getInstructions());
		for (InsnNode move : moves) {
			RegisterArg source = (RegisterArg) move.getArg(0);
			InsnNode sourceAssign = source.getSVar().getAssignInsn();
			if (sourceAssign == null) {
				if (!source.contains(AFlag.METHOD_ARGUMENT) && !source.contains(AFlag.THIS)) {
					return false;
				}
			} else {
				BlockNode sourceBlock = BlockUtils.getBlockByInsn(mth, sourceAssign);
				if (sourceBlock == null || sourceBlock != block && !block.isDominator(sourceBlock)) {
					return false;
				}
				// A MOVE fed by another MOVE in this same orphan bundle would retain a reference
				// to an instruction removed below. PHI definitions in the block are safe sources.
				if (sourceBlock == block && sourceAssign.getType() == InsnType.MOVE
						&& moves.contains(sourceAssign)) {
					return false;
				}
			}
		}
		for (InsnNode move : moves) {
			RegisterArg source = (RegisterArg) move.getArg(0);
			List<RegisterArg> uses = new ArrayList<>(move.getResult().getSVar().getUseList());
			for (RegisterArg use : uses) {
				RegisterArg replacement = source.duplicate(use.getInitType());
				replacement.copyAttributesFrom(use);
				if (!use.getParentInsn().replaceArg(use, replacement)) {
					return false;
				}
			}
		}
		for (InsnNode move : moves) {
			InsnRemover.remove(mth, block, move);
		}
		return true;
	}

	/**
	 * A join immediately before a structured condition can be absorbed into that condition while a
	 * register-to-register projection remains outside the region tree. At this late stage SSA copy
	 * propagation is semantics preserving when the source reaches the projection and its definition
	 * also dominates every rewritten use (including loop-carried uses). Require known compatible types
	 * and a block containing only PHIs and MOVEs so casts, stores and user
	 * computations cannot be erased by this recovery. Type compatibility is checked with the class
	 * hierarchy before rewriting, allowing a reference value to be projected to a wider result type.
	 * PHIs can share the join block, but are metadata rather than emitted assignments and stay intact.
	 */
	static boolean isOrphanProjectionMoveBundle(BlockNode block) {
		if (block.getInstructions().isEmpty()) {
			return false;
		}
		boolean moveFound = false;
		for (InsnNode move : block.getInstructions()) {
			if (move.getType() == InsnType.PHI) {
				continue;
			}
			if (move.getType() != InsnType.MOVE || move.getArgsCount() != 1 || !move.getArg(0).isRegister()) {
				return false;
			}
			moveFound = true;
			RegisterArg result = move.getResult();
			RegisterArg source = (RegisterArg) move.getArg(0);
			if (result == null || result.getSVar() == null || source.getSVar() == null
					|| result.getSVar().getUseList().isEmpty()
					|| !result.getType().isTypeKnown()
					|| !source.getType().isTypeKnown()) {
				return false;
			}
		}
		return moveFound;
	}

	private static boolean inlineOrphanDominatedProjectionMoves(MethodNode mth, BlockNode block) {
		if (!isOrphanProjectionMoveBundle(block)) {
			return false;
		}
		List<InsnNode> moves = block.getInstructions().stream()
				.filter(insn -> insn.getType() == InsnType.MOVE)
				.toList();
		for (InsnNode move : moves) {
			RegisterArg source = (RegisterArg) move.getArg(0);
			if (!isSafeProjectionType(mth, source.getType(), move.getResult().getType())) {
				return false;
			}
			InsnNode sourceAssign = source.getSVar().getAssignInsn();
			BlockNode sourceBlock = null;
			if (sourceAssign == null) {
				if (!source.contains(AFlag.METHOD_ARGUMENT) && !source.contains(AFlag.THIS)) {
					return false;
				}
			} else {
				sourceBlock = BlockUtils.getBlockByInsn(mth, sourceAssign);
				if (sourceBlock == null || sourceBlock != block && !block.isDominator(sourceBlock)
						|| sourceBlock == block && sourceAssign.getType() == InsnType.MOVE && moves.contains(sourceAssign)) {
					return false;
				}
			}
			for (RegisterArg use : move.getResult().getSVar().getUseList()) {
				InsnNode parentInsn = use.getParentInsn();
				BlockNode useBlock;
				if (parentInsn instanceof PhiInsn) {
					useBlock = ((PhiInsn) parentInsn).getBlockByArg(use);
				} else {
					useBlock = BlockUtils.getBlockByInsn(mth, parentInsn);
				}
				if (useBlock == null
						|| sourceBlock != null && !isAssignmentAvailableAtUse(sourceBlock, sourceAssign, useBlock, parentInsn)) {
					return false;
				}
			}
		}
		for (InsnNode move : moves) {
			RegisterArg source = (RegisterArg) move.getArg(0);
			for (RegisterArg use : new ArrayList<>(move.getResult().getSVar().getUseList())) {
				RegisterArg replacement = source.duplicate(use.getInitType());
				replacement.copyAttributesFrom(use);
				if (!use.getParentInsn().replaceArg(use, replacement)) {
					return false;
				}
			}
		}
		for (InsnNode move : moves) {
			InsnRemover.remove(mth, block, move);
		}
		return true;
	}

	private static boolean isAssignmentAvailableAtUse(
			BlockNode assignBlock, InsnNode assignInsn, BlockNode useBlock, InsnNode useInsn) {
		if (assignBlock != useBlock) {
			return useBlock.isDominator(assignBlock);
		}
		int assignPos = assignBlock.getInstructions().indexOf(assignInsn);
		int usePos = useBlock.getInstructions().indexOf(useInsn);
		return assignPos != -1 && usePos != -1 && assignPos < usePos;
	}

	private static boolean isSafeProjectionType(MethodNode mth, ArgType sourceType, ArgType resultType) {
		if (sourceType.equals(resultType)) {
			return true;
		}
		if (!sourceType.isObject() || !resultType.isObject()) {
			return false;
		}
		// java.lang.Object is always a valid widening target, including for library classes absent
		// from the loaded classpath where TypeCompare intentionally returns UNKNOWN.
		if (resultType.equals(ArgType.OBJECT)) {
			return true;
		}
		return !ArgType.isCastNeeded(mth.root(), sourceType, resultType);
	}

	private static String getBlockInsnStr(MethodNode mth, IBlock block) {
		ICodeWriter code = new SimpleCodeWriter();
		code.incIndent();
		code.newLine();
		MethodGen mg = MethodGen.getFallbackMethodGen(mth);
		InsnGen ig = new InsnGen(mg, true);
		for (InsnNode insn : block.getInstructions()) {
			try {
				ig.makeInsn(insn, code);
			} catch (CodegenException e) {
				// ignore
			}
		}
		code.newLine();
		return code.getCodeStr();
	}
}
