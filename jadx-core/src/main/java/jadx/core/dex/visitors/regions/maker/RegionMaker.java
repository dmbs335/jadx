package jadx.core.dex.visitors.regions.maker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import jadx.core.dex.attributes.AFlag;
import jadx.core.dex.attributes.AType;
import jadx.core.dex.attributes.FieldInitInsnAttr;
import jadx.core.dex.attributes.nodes.CoroutineLoopPreHeaderAttr;
import jadx.core.dex.attributes.nodes.EdgeInsnAttr;
import jadx.core.dex.attributes.nodes.LoopInfo;
import jadx.core.dex.attributes.nodes.PhiListAttr;
import jadx.core.dex.info.ClassInfo;
import jadx.core.dex.info.FieldInfo;
import jadx.core.dex.info.MethodInfo;
import jadx.core.dex.instructions.IfNode;
import jadx.core.dex.instructions.IfOp;
import jadx.core.dex.instructions.IndexInsnNode;
import jadx.core.dex.instructions.InsnType;
import jadx.core.dex.instructions.InvokeNode;
import jadx.core.dex.instructions.PhiInsn;
import jadx.core.dex.instructions.SwitchInsn;
import jadx.core.dex.instructions.args.ArgType;
import jadx.core.dex.instructions.args.InsnArg;
import jadx.core.dex.instructions.args.InsnWrapArg;
import jadx.core.dex.instructions.args.LiteralArg;
import jadx.core.dex.instructions.args.RegisterArg;
import jadx.core.dex.instructions.mods.ConstructorInsn;
import jadx.core.dex.instructions.mods.TernaryInsn;
import jadx.core.dex.nodes.BlockNode;
import jadx.core.dex.nodes.ClassNode;
import jadx.core.dex.nodes.FieldNode;
import jadx.core.dex.nodes.IContainer;
import jadx.core.dex.nodes.IRegion;
import jadx.core.dex.nodes.InsnContainer;
import jadx.core.dex.nodes.InsnNode;
import jadx.core.dex.nodes.MethodNode;
import jadx.core.dex.regions.Region;
import jadx.core.dex.regions.conditions.IfRegion;
import jadx.core.dex.regions.loops.LoopRegion;
import jadx.core.dex.visitors.kotlin.CoroutineMethodUtils;
import jadx.core.dex.visitors.kotlin.KtorCioRecovery;
import jadx.core.utils.BlockUtils;
import jadx.core.utils.blocks.BlockSet;
import jadx.core.utils.exceptions.JadxOverflowException;

import static jadx.core.utils.BlockUtils.getNextBlock;

public class RegionMaker {
	private final MethodNode mth;
	private final RegionStack stack;

	private final IfRegionMaker ifMaker;
	private final LoopRegionMaker loopMaker;

	private final BlockSet processedBlocks;
	private final IfRegion[] firstIfRegionByCondition;
	private final Map<BlockNode, List<Set<BlockNode>>> activeRegionStates = new HashMap<>();
	private final BlockSet recursiveRegionBlocks;
	private final BlockSet traversalCycleBlocks;
	private final BlockSet loopCarryTailBlocks;
	private final BlockSet safeSwitchSharedBlocks;
	private final int regionsLimit;
	private final boolean allowSinglePredecessorTerminalDuplication;

	private int regionsCount;
	private int unsafeDuplicatedBlocksCount;
	private @Nullable BlockNode firstUnsafeDuplicatedBlock;
	private @Nullable Map<MethodInfo, Boolean> pureValueCallCache;
	private @Nullable Map<BlockNode, Boolean> balancedComposeTraceCache;

	public RegionMaker(MethodNode mth) {
		this.mth = mth;
		this.stack = new RegionStack(mth);
		this.ifMaker = new IfRegionMaker(mth, this);
		this.loopMaker = new LoopRegionMaker(mth, this, ifMaker);
		this.processedBlocks = BlockSet.empty(mth);
		this.firstIfRegionByCondition = new IfRegion[mth.getBasicBlocks().stream()
				.mapToInt(BlockNode::getId)
				.max()
				.orElse(-1) + 1];
		this.recursiveRegionBlocks = BlockSet.empty(mth);
		this.traversalCycleBlocks = BlockSet.empty(mth);
		this.loopCarryTailBlocks = BlockSet.empty(mth);
		this.safeSwitchSharedBlocks = BlockSet.empty(mth);
		this.regionsLimit = mth.getBasicBlocks().size() * 400;
		this.allowSinglePredecessorTerminalDuplication = KtorCioRecovery.isReadPacketMethod(mth);
	}

	void registerIfRegion(BlockNode block, IfRegion ifRegion) {
		int blockId = block.getId();
		IfRegion firstRegion = firstIfRegionByCondition[blockId];
		if (firstRegion == null) {
			firstIfRegionByCondition[blockId] = ifRegion;
		} else {
			// Conditions keep a view over the mutable IF instruction. If the same CFG block is
			// materialized in more than one region (for example, a coroutine retry entry), a
			// later branch-order inversion must not mutate the condition already owned by the
			// first region. Give both regions detached operator views before any region visitor
			// can reorder their branches.
			firstRegion.markConditionShared();
			ifRegion.markConditionShared();
		}
	}

	void registerSafeSwitchSharedDuplication(BlockNode block) {
		safeSwitchSharedBlocks.add(block);
	}

	public Region makeMthRegion() {
		Region region = makeRegion(mth.getEnterBlock());
		restoreLinearSyntheticMoveBlocks(region);
		if (unsafeDuplicatedBlocksCount != 0) {
			BlockNode firstBlock = Objects.requireNonNull(firstUnsafeDuplicatedBlock);
			addRegionFallbackComment("Code duplicated in " + unsafeDuplicatedBlocksCount
					+ " blocks, first: " + firstBlock + ' ' + firstBlock.getAttributesString());
		}
		return region;
	}

	private void restoreLinearSyntheticMoveBlocks(Region rootRegion) {
		for (BlockNode block : mth.getBasicBlocks()) {
			if (!block.contains(AFlag.SYNTHETIC)
					|| block.getPredecessors().size() != 1
					|| block.getCleanSuccessors().size() != 1
					|| block.getInstructions().isEmpty()
					|| block.getInstructions().stream().anyMatch(insn -> insn.getType() != InsnType.MOVE)
					|| containsContainer(rootRegion, block)) {
				continue;
			}
			insertAfterPredecessor(rootRegion, block.getPredecessors().get(0), block);
		}
	}

	private static boolean containsContainer(IRegion region, IContainer target) {
		for (IContainer container : region.getSubBlocks()) {
			if (container == target) {
				return true;
			}
			if (container instanceof IRegion && containsContainer((IRegion) container, target)) {
				return true;
			}
		}
		return false;
	}

	private static void insertAfterPredecessor(IRegion region, BlockNode predecessor, BlockNode block) {
		List<IContainer> subBlocks = region.getSubBlocks();
		if (region instanceof Region) {
			for (int i = subBlocks.size() - 1; i >= 0; i--) {
				IContainer container = subBlocks.get(i);
				if (container == predecessor
						|| container instanceof IfRegion
								&& ((IfRegion) container).getConditionBlocks().contains(predecessor)) {
					subBlocks.add(i + 1, block);
				}
			}
		}
		for (IContainer container : new ArrayList<>(subBlocks)) {
			if (container instanceof IRegion) {
				insertAfterPredecessor((IRegion) container, predecessor, block);
			}
		}
	}

	Region makeRegion(BlockNode startBlock) {
		Objects.requireNonNull(startBlock);
		Region region = new Region(stack.peekRegion());
		if (stack.containsExit(startBlock)) {
			insertEdgeInsns(region, startBlock);
			return region;
		}
		Set<BlockNode> exits = new HashSet<>();
		stack.getExits().forEach(exits::add);
		List<Set<BlockNode>> activeStates = activeRegionStates.computeIfAbsent(startBlock, k -> new ArrayList<>());
		if (activeStates.contains(exits)) {
			if (emitSafeRecursiveLeaf(region, startBlock)) {
				return region;
			}
			if (!recursiveRegionBlocks.addChecked(startBlock)
					&& !startBlock.contains(AType.EXPECTED_TRAVERSAL_CYCLE)) {
				addRegionFallbackComment("Recursive region processing prevented at block: " + startBlock);
			}
			return region;
		}
		activeStates.add(exits);
		try {
			if (processedBlocks.addChecked(startBlock)) {
				// Add block to multiple regions (duplicate the instructions in decompiled code)
				// and allow processing to continue
				if (!startBlock.contains(AFlag.DUPLICATED)) {
					if (!isSafeLoopDuplication(startBlock)) {
						if (firstUnsafeDuplicatedBlock == null) {
							firstUnsafeDuplicatedBlock = startBlock;
						}
						unsafeDuplicatedBlocksCount++;
					}
					startBlock.add(AFlag.DUPLICATED);
				}
			}
			BlockSet regionBlocks = BlockSet.empty(mth);
			BlockNode next = startBlock;
			while (next != null) {
				if (regionBlocks.addChecked(next)) {
					if (isNestedCoroutineOuterTraversalCycle(region, next)) {
						if (wrapTraversalCycleAsEndlessLoop(region, next)) {
							break;
						}
					}
					if (!traversalCycleBlocks.addChecked(next)
							&& !isExpectedLoopStartCycle(next, mth.getLoopForBlock(next))
							&& !next.contains(AType.EXPECTED_TRAVERSAL_CYCLE)) {
						addRegionFallbackComment("Region traversal cycle prevented at block: " + next);
					}
					break;
				}
				BlockNode current = next;
				next = traverse(region, current);
				regionsCount++;
				if (regionsCount > regionsLimit) {
					throw new JadxOverflowException("Regions count limit reached at block " + startBlock
							+ ", regions=" + regionsCount + ", limit=" + regionsLimit
							+ ", blocks=" + mth.getBasicBlocks().size() + ", stack=" + stack.size()
							+ ", activeStates=" + activeStates.size() + ", exits=" + exits.size());
				}
			}
			return region;
		} finally {
			activeStates.remove(exits);
			if (activeStates.isEmpty()) {
				activeRegionStates.remove(startBlock);
			}
		}
	}

	private boolean emitSafeRecursiveLeaf(Region region, BlockNode block) {
		// Re-emitting a processed local assignment leaf is safe only when no try/catch
		// boundary can be crossed and the block has one unambiguous continuation.
		boolean safeLeaf = isSafeRecursiveLeafDuplication(block);
		if (!processedBlocks.contains(block)
				|| (!mth.isNoExceptionHandlers() && isNearExceptionBoundary(block))
				|| !safeLeaf) {
			return false;
		}
		block.add(AFlag.DUPLICATED);
		region.add(block);
		return true;
	}

	static boolean isSafeRecursiveLeafDuplication(BlockNode block) {
		block.updateCleanSuccessors();
		BlockUtils.invalidatePathCache();
		return !block.contains(AType.EXPECTED_TRAVERSAL_CYCLE)
				&& block.getCleanSuccessors().size() == 1
				&& isSafeLocalAssignmentDuplication(block)
				&& block.getInstructions().stream().noneMatch(
						insn -> insn.getType() == InsnType.ARITH);
	}

	static boolean isNearExceptionBoundary(BlockNode block) {
		if (isExceptionBoundaryBlock(block)) {
			return true;
		}
		return hasExceptionBoundaryWithinOneMoreEdge(block, block.getPredecessors())
				|| hasExceptionBoundaryWithinOneMoreEdge(block, block.getSuccessors());
	}

	private static boolean hasExceptionBoundaryWithinOneMoreEdge(
			BlockNode origin, List<BlockNode> adjacentBlocks) {
		for (BlockNode adjacent : adjacentBlocks) {
			if (isExceptionBoundaryBlock(adjacent)) {
				return true;
			}
			for (BlockNode predecessor : adjacent.getPredecessors()) {
				if (predecessor != origin && isExceptionBoundaryBlock(predecessor)) {
					return true;
				}
			}
			for (BlockNode successor : adjacent.getSuccessors()) {
				if (successor != origin && isExceptionBoundaryBlock(successor)) {
					return true;
				}
			}
		}
		return false;
	}

	private static boolean isExceptionBoundaryBlock(BlockNode block) {
		return block.contains(AType.TRY_BLOCK)
				|| block.contains(AType.EXC_CATCH)
				|| block.contains(AFlag.EXC_TOP_SPLITTER)
				|| block.contains(AFlag.EXC_BOTTOM_SPLITTER)
				|| BlockUtils.isExceptionHandlerPath(block);
	}

	private boolean isNestedCoroutineOuterTraversalCycle(Region region, BlockNode loopStart) {
		if (isIteratorCoroutineTraversalCycle(region, loopStart)) {
			return true;
		}
		boolean invalidationDrain = isInvalidationDrainCoroutine();
		if (!(isSuspendLambdaMethod() || isCompiledSuspendMethod())
				|| mth.getAllLoopsForBlock(loopStart).size() != 0
				|| traversalCycleBlocks.contains(loopStart) && !invalidationDrain
				|| loopStart.contains(AType.EXPECTED_TRAVERSAL_CYCLE)
				|| region.getParent() != null && !(region.getParent() instanceof IfRegion)
				|| (invalidationDrain
						? loopStart.getPredecessors().size() < 2
								|| loopStart.getPredecessors().size() > 3
						: loopStart.getPredecessors().size() != 2)
				|| loopStart.getCleanSuccessors().size() != 1
				|| !containsSupportedCoroutineLoopSuspendInvoke()) {
			return false;
		}
		List<IContainer> subBlocks = region.getSubBlocks();
		int startIndex = subBlocks.indexOf(loopStart);
		if (startIndex == -1 || startIndex == subBlocks.size() - 1
				|| !containsLoopRegion(subBlocks.subList(startIndex + 1, subBlocks.size()))) {
			return false;
		}
		BlockNode conditionBlock = loopStart.getCleanSuccessors().get(0);
		if (!(BlockUtils.getLastInsn(conditionBlock) instanceof IfNode)) {
			return false;
		}
		BlockNode advanceBlock = findArithmeticAdvancePredecessor(loopStart);
		if (advanceBlock != null
				&& BlockUtils.isPathExists(conditionBlock, advanceBlock)) {
			return true;
		}
		CoroutineLoopPreHeaderAttr preHeaderAttr =
				mth.get(AType.COROUTINE_LOOP_PRE_HEADER);
		if (!mth.contains(AType.NORMALIZED_COROUTINE_LOOP)
				|| preHeaderAttr == null) {
			return false;
		}
		BlockNode cyclePredecessor = null;
		int cyclePredecessors = 0;
		int externalPredecessors = 0;
		for (BlockNode predecessor : loopStart.getPredecessors()) {
			if (BlockUtils.isPathExists(conditionBlock, predecessor)) {
				cyclePredecessors++;
				if (cyclePredecessor != null && !invalidationDrain) {
					return false;
				}
				cyclePredecessor = predecessor;
			} else {
				externalPredecessors++;
				if (externalPredecessors > 1 && !invalidationDrain) {
					return false;
				}
			}
		}
		BlockNode normalizedPreHeader = preHeaderAttr.getBlock();
		return cyclePredecessors != 0
				&& externalPredecessors != 0
				&& BlockUtils.isPathExists(loopStart, normalizedPreHeader)
				&& BlockUtils.isPathExists(normalizedPreHeader, cyclePredecessor);
	}

	private boolean isIteratorCoroutineTraversalCycle(Region region, BlockNode loopStart) {
		if (!isCompiledSuspendMethod()
				|| traversalCycleBlocks.contains(loopStart)
				|| loopStart.contains(AType.EXPECTED_TRAVERSAL_CYCLE)
				|| !loopStart.getInstructions().isEmpty()
				|| loopStart.getPredecessors().size() < 2
				|| loopStart.getCleanSuccessors().size() != 1) {
			return false;
		}
		List<LoopInfo> loops = mth.getAllLoopsForBlock(loopStart);
		// Multi-entry repair can consume the source iterator-loop metadata. Accept
		// either that form or a header nested in one coroutine dispatcher loop.
		if (loops.size() > 1
				|| (!loops.isEmpty() && loops.get(0).getStart() == loopStart)) {
			return false;
		}
		BlockNode conditionBlock = loopStart.getCleanSuccessors().get(0);
		InsnNode conditionInsn = BlockUtils.getLastInsn(conditionBlock);
		if (!(conditionInsn instanceof IfNode)
				|| !containsInvokeNamed(conditionInsn, "hasNext")) {
			return false;
		}
		BlockNode outerLoopStart = loops.isEmpty() ? null : loops.get(0).getStart();
		int externalPredecessors = 0;
		int cyclePredecessors = 0;
		for (BlockNode predecessor : loopStart.getPredecessors()) {
			boolean cyclePath = BlockUtils.isPathExists(
					conditionBlock,
					predecessor,
					block -> block != loopStart && block != outerLoopStart);
			if (cyclePath) {
				cyclePredecessors++;
			} else {
				externalPredecessors++;
			}
		}
		if (externalPredecessors != 1 || cyclePredecessors == 0) {
			return false;
		}
		List<IContainer> subBlocks = region.getSubBlocks();
		int startIndex = subBlocks.indexOf(loopStart);
		return startIndex != -1 && startIndex < subBlocks.size() - 1;
	}

	private boolean isCompiledSuspendMethod() {
		List<ArgType> argTypes = mth.getMethodInfo().getArgumentsTypes();
		return !argTypes.isEmpty()
				&& CoroutineMethodUtils.isKnownContinuationType(argTypes.get(argTypes.size() - 1))
				&& mth.referencesMethodNamed("getCOROUTINE_SUSPENDED")
				&& mth.referencesMethodNamed("throwOnFailure");
	}

	private static boolean containsInvokeNamed(InsnNode insn, String name) {
		if (insn instanceof InvokeNode
				&& ((InvokeNode) insn).getCallMth().getName().equals(name)) {
			return true;
		}
		for (InsnArg arg : insn.getArguments()) {
			if (arg instanceof InsnWrapArg
					&& containsInvokeNamed(((InsnWrapArg) arg).getWrapInsn(), name)) {
				return true;
			}
		}
		return false;
	}

	private boolean isSuspendLambdaMethod() {
		return CoroutineMethodUtils.isSuspendLambdaBody(mth);
	}

	private boolean containsSupportedCoroutineLoopSuspendInvoke() {
		if (containsYieldInvoke()) {
			return true;
		}
		if (!mth.contains(AType.NORMALIZED_COROUTINE_LOOP)
				|| mth.get(AType.COROUTINE_LOOP_PRE_HEADER) == null) {
			return false;
		}
		return containsChannelSendInvoke()
				|| isInvalidationDrainCoroutine();
	}

	private boolean isInvalidationDrainCoroutine() {
		return mth.referencesMethodNamed("compute")
				&& mth.referencesMethodNamed("compareAndSet")
				&& mth.referencesMethodNamed("postValue")
				&& mth.referencesMethodNamed("addWeakObserver");
	}

	private boolean containsYieldInvoke() {
		for (BlockNode block : mth.getBasicBlocks()) {
			for (InsnNode insn : block.getInstructions()) {
				if (containsYieldInvoke(insn)) {
					return true;
				}
			}
		}
		return false;
	}

	private static boolean containsYieldInvoke(InsnNode insn) {
		if (insn instanceof InvokeNode
				&& ((InvokeNode) insn).getCallMth().getName().equals("yield")) {
			return true;
		}
		for (InsnArg arg : insn.getArguments()) {
			if (arg instanceof InsnWrapArg
					&& containsYieldInvoke(((InsnWrapArg) arg).getWrapInsn())) {
				return true;
			}
		}
		return false;
	}

	private boolean containsChannelSendInvoke() {
		for (BlockNode block : mth.getBasicBlocks()) {
			for (InsnNode insn : block.getInstructions()) {
				if (containsChannelSendInvoke(insn)) {
					return true;
				}
			}
		}
		return false;
	}

	private static boolean containsChannelSendInvoke(InsnNode insn) {
		if (insn instanceof InvokeNode) {
			InvokeNode invoke = (InvokeNode) insn;
			if (invoke.getCallMth().getName().equals("send")
					&& invoke.getCallMth().getDeclClass().getFullName()
							.startsWith("kotlinx.coroutines.channels.")) {
				return true;
			}
		}
		for (InsnArg arg : insn.getArguments()) {
			if (arg instanceof InsnWrapArg
					&& containsChannelSendInvoke(((InsnWrapArg) arg).getWrapInsn())) {
				return true;
			}
		}
		return false;
	}

	private static @Nullable BlockNode findArithmeticAdvancePredecessor(BlockNode loopStart) {
		BlockNode advanceBlock = null;
		for (BlockNode predecessor : loopStart.getPredecessors()) {
			if (predecessor.getCleanSuccessors().size() != 1
					|| predecessor.getCleanSuccessors().get(0) != loopStart
					|| !isPureArithmeticAdvance(predecessor)) {
				continue;
			}
			if (advanceBlock != null) {
				return null;
			}
			advanceBlock = predecessor;
		}
		return advanceBlock;
	}

	private static boolean isPureArithmeticAdvance(BlockNode block) {
		boolean foundArithmetic = false;
		int generatedInsnCount = 0;
		for (InsnNode insn : block.getInstructions()) {
			if (insn.contains(AFlag.DONT_GENERATE)) {
				continue;
			}
			generatedInsnCount++;
			if (generatedInsnCount > 3
					|| insn.getType() != InsnType.MOVE && insn.getType() != InsnType.ARITH) {
				return false;
			}
			foundArithmetic |= insn.getType() == InsnType.ARITH;
		}
		return foundArithmetic;
	}

	private static boolean containsLoopRegion(List<IContainer> containers) {
		for (IContainer container : containers) {
			if (container instanceof LoopRegion) {
				return true;
			}
			if (container instanceof IRegion
					&& containsLoopRegion(((IRegion) container).getSubBlocks())) {
				return true;
			}
		}
		return false;
	}

	private static boolean wrapTraversalCycleAsEndlessLoop(Region region, BlockNode loopStart) {
		List<IContainer> subBlocks = region.getSubBlocks();
		int startIndex = subBlocks.indexOf(loopStart);
		if (startIndex == -1 || startIndex == subBlocks.size() - 1) {
			return false;
		}
		List<IContainer> loopContent = new ArrayList<>(subBlocks.subList(startIndex, subBlocks.size()));
		subBlocks.subList(startIndex, subBlocks.size()).clear();
		LoopInfo loopInfo = new LoopInfo(loopStart, loopStart, Set.of(loopStart));
		LoopRegion loopRegion = new LoopRegion(region, loopInfo, null, false);
		Region body = new Region(loopRegion);
		loopContent.forEach(body::add);
		loopRegion.setBody(body);
		region.add(loopRegion);
		return true;
	}

	static boolean isExpectedLoopStartCycle(BlockNode block, @Nullable LoopInfo loop) {
		return loop != null && loop.getStart() == block;
	}

	private void addRegionFallbackComment(String message) {
		if (mth.contains(AType.UNSUPPORTED_MULTI_ENTRY_LOOP)) {
			mth.addInfoComment(message + " (caused by unsupported multi-entry loop)");
		} else if (mth.contains(AType.NORMALIZED_COROUTINE_LOOP)) {
			mth.addInfoComment(message + " (inside normalized coroutine loop)");
		} else {
			mth.addWarnComment(message);
		}
	}

	private boolean isSafeLoopDuplication(BlockNode block) {
		if (block.contains(AType.SAFE_DUPLICATION)
				|| safeSwitchSharedBlocks.contains(block)
				|| hasNoGeneratedCode(block)
				|| isSafeLocalAssignmentDuplication(block)
				|| isSafeBooleanNumericConversionDuplication(block)
				|| isSafeStaticFinalSingletonReadDuplication(block)
				|| isSafeCoroutineStateInitDuplication(block)
				|| isSafeCoroutineResumeRestoreDuplication(block)
				|| isSafeImmutableValueConstructorDuplication(block)
				|| isSafeCompilerLambdaDuplication(block)
				|| isSafeComposeRememberLambdaDuplication(block)
				|| isSafeBalancedComposeTraceStartDuplication(block)
				|| isSafeComposeGroupedValueDuplication(block)
				|| isSafeSyntheticComposeColorUnwrapDuplication(block)
				|| isSafeComposeProtocolDuplication(block)
				|| isSafeCaughtConstantReturnDuplication(block)
				|| isSafeTerminalThrowDuplication(block)
				|| isSafePlainThrowDuplication(block)
				|| isSafePlainReturnDuplication(block)
				|| isSafeConditionDuplication(block)
				|| isSafeSwitchFallThroughDuplication(block)
				|| isSafeDistinctLiteralBranchDuplication(block)
				|| isSafeReadOnlyValueDuplication(block)
				|| isSafeChannelCancelConsumedDuplication(block)
				|| isSafeTerminalSideEffectDuplication(block)) {
			return true;
		}
		if (mth.getLoopForBlock(block) == null) {
			return false;
		}
		for (InsnNode insn : block.getInstructions()) {
			if (insn.contains(AFlag.DONT_GENERATE)) {
				continue;
			}
			switch (insn.getType()) {
				case MOVE:
				case CONST:
				case ARITH:
				case IGET:
				case SGET:
				case CAST:
				case CHECK_CAST:
					break;
				case INVOKE:
					InvokeNode invoke = (InvokeNode) insn;
					String declClass = invoke.getCallMth().getDeclClass().getFullName();
					String name = invoke.getCallMth().getName();
					if (!isSafeLoopValueCall(declClass, name, invoke.getArgsCount())
							&& !isPureValueInvoke(invoke)) {
						return false;
					}
					break;
				default:
					return false;
			}
		}
		return true;
	}

	private boolean isSafeImmutableValueConstructorDuplication(BlockNode block) {
		if (BlockUtils.isExceptionHandlerPath(block)
				|| block.contains(AType.EXC_CATCH)
				|| block.getCleanSuccessors().size() != 1) {
			return false;
		}
		List<InsnNode> generatedInsns = block.getInstructions().stream()
				.filter(insn -> !insn.contains(AFlag.DONT_GENERATE))
				.toList();
		if (generatedInsns.size() != 1 || !(generatedInsns.get(0) instanceof ConstructorInsn)) {
			return false;
		}
		ConstructorInsn ctrInsn = (ConstructorInsn) generatedInsns.get(0);
		if (!isKnownImmutableValueConstructor(ctrInsn)) {
			return false;
		}
		for (InsnArg arg : ctrInsn.getArguments()) {
			if (!isSafeValueArg(block, arg)) {
				return false;
			}
		}
		return true;
	}

	static boolean isKnownImmutableValueConstructor(ConstructorInsn ctrInsn) {
		if (!ctrInsn.isNewInstance()) {
			return false;
		}
		String className = ctrInsn.getClassType().getFullName();
		int argsCount = ctrInsn.getArgsCount();
		return className.equals("kotlin.Pair") && argsCount == 2
				|| className.equals("kotlin.Triple") && argsCount == 3;
	}

	private boolean isSafeStaticFinalSingletonReadDuplication(BlockNode block) {
		if (mth.getLoopForBlock(block) != null || block.getCleanSuccessors().size() != 1) {
			return false;
		}
		List<InsnNode> generatedInsns = block.getInstructions().stream()
				.filter(insn -> !insn.contains(AFlag.DONT_GENERATE))
				.toList();
		if (generatedInsns.size() != 1 || !(generatedInsns.get(0) instanceof IndexInsnNode)) {
			return false;
		}
		IndexInsnNode fieldRead = (IndexInsnNode) generatedInsns.get(0);
		return fieldRead.getType() == InsnType.SGET && isFinalSingletonField((FieldInfo) fieldRead.getIndex());
	}

	private boolean isFinalSingletonField(FieldInfo fieldInfo) {
		FieldNode field = mth.root().resolveField(fieldInfo);
		if (field == null) {
			return false;
		}
		FieldInitInsnAttr initAttr = field.get(AType.FIELD_INIT_INSN);
		return isFinalSingletonInit(field.getAccessFlags().isFinal(), initAttr == null ? null : initAttr.getInsn());
	}

	static boolean isFinalSingletonInit(boolean isFinal, @Nullable InsnNode initInsn) {
		return isFinal
				&& initInsn instanceof ConstructorInsn
				&& ((ConstructorInsn) initInsn).isNewInstance();
	}

	private boolean isSafeCaughtConstantReturnDuplication(BlockNode block) {
		if (!block.contains(AType.EXC_CATCH) || block.getCleanSuccessors().size() != 1) {
			return false;
		}
		List<InsnNode> generatedInsns = block.getInstructions().stream()
				.filter(insn -> !insn.contains(AFlag.DONT_GENERATE))
				.toList();
		if (generatedInsns.size() != 1 || !(generatedInsns.get(0) instanceof InvokeNode)) {
			return false;
		}
		InvokeNode invoke = (InvokeNode) generatedInsns.get(0);
		MethodNode callMth = mth.root().resolveMethod(invoke.getCallMth());
		return callMth != null
				&& callMth.contains(AType.CONSTANT_RETURN_METHOD)
				&& isKnownNonNullSingleton(invoke.getInstanceArg());
	}

	private boolean isKnownNonNullSingleton(@Nullable InsnArg instanceArg) {
		if (instanceArg == null) {
			return false;
		}
		InsnNode fieldRead;
		if (instanceArg instanceof InsnWrapArg) {
			fieldRead = ((InsnWrapArg) instanceArg).getWrapInsn();
		} else if (instanceArg instanceof RegisterArg) {
			RegisterArg reg = (RegisterArg) instanceArg;
			fieldRead = reg.getSVar() == null ? null : reg.getSVar().getAssignInsn();
		} else {
			return false;
		}
		if (!(fieldRead instanceof IndexInsnNode) || fieldRead.getType() != InsnType.SGET) {
			return false;
		}
		FieldNode field = mth.root().resolveField((FieldInfo) ((IndexInsnNode) fieldRead).getIndex());
		if (field == null) {
			return false;
		}
		FieldInitInsnAttr initAttr = field.get(AType.FIELD_INIT_INSN);
		return isFinalSingletonInit(field.getAccessFlags().isFinal(), initAttr == null ? null : initAttr.getInsn());
	}

	static boolean isSafeLoopValueCall(String declClass, String name, int argsCount) {
		return isPureValueCall(declClass, name, argsCount)
				|| declClass.equals("java.lang.Math") && (name.equals("min") || name.equals("max"));
	}

	private boolean isSafeBalancedComposeTraceStartDuplication(BlockNode block) {
		if (balancedComposeTraceCache != null) {
			Boolean cached = balancedComposeTraceCache.get(block);
			if (cached != null) {
				return cached;
			}
		}
		boolean result = checkBalancedComposeTraceStartDuplication(block);
		if (balancedComposeTraceCache == null) {
			balancedComposeTraceCache = new HashMap<>();
		}
		balancedComposeTraceCache.put(block, result);
		return result;
	}

	private boolean checkBalancedComposeTraceStartDuplication(BlockNode startBlock) {
		if (BlockUtils.isExceptionHandlerPath(startBlock)
				|| startBlock.contains(AType.EXC_CATCH)
				|| !isSingleComposeTraceCallBlock(startBlock, "traceEventStart")
				|| !hasComposerArgument()) {
			return false;
		}
		BlockNode bodyStart = getComposeTraceDiamondMerge(startBlock);
		if (bodyStart == null) {
			return false;
		}
		Set<BlockNode> traceCallBlocks = new HashSet<>();
		List<BlockNode> endBlocks = new ArrayList<>();
		for (BlockNode block : mth.getBasicBlocks()) {
			if (containsComposeTraceCall(block)) {
				traceCallBlocks.add(block);
				if (isSingleComposeTraceCallBlock(block, "traceEventEnd")) {
					endBlocks.add(block);
				}
			}
		}
		for (BlockNode endBlock : endBlocks) {
			BlockNode endGuard = getComposeTraceGuard(endBlock);
			if (endGuard != null
					&& allCleanPathsReach(bodyStart, endGuard, traceCallBlocks)) {
				return true;
			}
		}
		return false;
	}

	private boolean hasComposerArgument() {
		for (RegisterArg arg : mth.getArgRegs()) {
			if (arg.getType().toString().equals("androidx.compose.runtime.Composer")) {
				return true;
			}
		}
		return false;
	}

	private @Nullable BlockNode getComposeTraceDiamondMerge(BlockNode protocolBlock) {
		BlockNode guard = getComposeTraceGuard(protocolBlock);
		if (guard == null) {
			return null;
		}
		return protocolBlock.getCleanSuccessors().get(0);
	}

	private @Nullable BlockNode getComposeTraceGuard(BlockNode protocolBlock) {
		if (protocolBlock.getPredecessors().size() != 1
				|| protocolBlock.getCleanSuccessors().size() != 1) {
			return null;
		}
		BlockNode guard = protocolBlock.getPredecessors().get(0);
		if (!isComposeTraceProgressGuard(guard) || guard.getCleanSuccessors().size() != 2) {
			return null;
		}
		BlockNode merge = protocolBlock.getCleanSuccessors().get(0);
		return guard.getCleanSuccessors().contains(protocolBlock)
				&& guard.getCleanSuccessors().contains(merge)
						? guard
						: null;
	}

	private static boolean isComposeTraceProgressGuard(BlockNode block) {
		boolean foundIf = false;
		boolean foundGuardCall = false;
		for (InsnNode insn : block.getInstructions()) {
			if (insn.contains(AFlag.DONT_GENERATE)) {
				continue;
			}
			if (insn.getType() == InsnType.IF) {
				if (foundIf) {
					return false;
				}
				foundIf = true;
			}
			Boolean call = insn.visitInsns(innerInsn -> isComposeTraceCall(innerInsn, "isTraceInProgress")
					? Boolean.TRUE
					: null);
			if (call != null) {
				if (foundGuardCall) {
					return false;
				}
				foundGuardCall = true;
			}
		}
		return foundIf && foundGuardCall;
	}

	private static boolean isSingleComposeTraceCallBlock(BlockNode block, String name) {
		List<InsnNode> generatedInsns = block.getInstructions().stream()
				.filter(insn -> !insn.contains(AFlag.DONT_GENERATE))
				.toList();
		return generatedInsns.size() == 1 && isComposeTraceCall(generatedInsns.get(0), name);
	}

	private static boolean containsComposeTraceCall(BlockNode block) {
		for (InsnNode insn : block.getInstructions()) {
			Boolean found = insn.visitInsns(innerInsn -> isComposeTraceCall(innerInsn, "traceEventStart")
					|| isComposeTraceCall(innerInsn, "traceEventEnd")
							? Boolean.TRUE
							: null);
			if (found != null) {
				return true;
			}
		}
		return false;
	}

	private static boolean isComposeTraceCall(InsnNode insn, String name) {
		if (!(insn instanceof InvokeNode)) {
			return false;
		}
		InvokeNode invoke = (InvokeNode) insn;
		return invoke.getCallMth().getDeclClass().getFullName().equals("androidx.compose.runtime.ComposerKt")
				&& invoke.getCallMth().getName().equals(name);
	}

	static boolean allCleanPathsReach(BlockNode start, BlockNode target, Set<BlockNode> barriers) {
		return allCleanPathsReach(start, target, barriers, new HashMap<>(), new HashSet<>());
	}

	private static boolean allCleanPathsReach(BlockNode current, BlockNode target, Set<BlockNode> barriers,
			Map<BlockNode, Boolean> cache, Set<BlockNode> visiting) {
		if (current == target) {
			return true;
		}
		if (barriers.contains(current) || current.getCleanSuccessors().isEmpty() || !visiting.add(current)) {
			return false;
		}
		Boolean cached = cache.get(current);
		if (cached != null) {
			visiting.remove(current);
			return cached;
		}
		boolean result = true;
		for (BlockNode successor : current.getCleanSuccessors()) {
			if (!allCleanPathsReach(successor, target, barriers, cache, visiting)) {
				result = false;
				break;
			}
		}
		visiting.remove(current);
		cache.put(current, result);
		return result;
	}

	private boolean isSafeComposeGroupedValueDuplication(BlockNode block) {
		if (mth.getLoopForBlock(block) != null
				|| BlockUtils.isExceptionHandlerPath(block)
				|| block.contains(AType.EXC_CATCH)
				|| block.getCleanSuccessors().size() != 1
				|| !hasComposerArgument()) {
			return false;
		}
		List<InvokeNode> calls = collectReadOnlyInvokes(block);
		if (calls == null) {
			return false;
		}
		if (calls.size() != 4
				|| !isComposeGroupBoundary(calls.get(0), "startReplaceGroup", 2)
				|| !isComposeGroupBoundary(calls.get(2), "endReplaceGroup", 1)
				|| !isComposeColorBoxCall(calls.get(3))) {
			return false;
		}
		MethodInfo valueMth = calls.get(1).getCallMth();
		return valueMth.getDeclClass().getFullName().equals(mth.getParentClass().getFullName())
				&& valueMth.getArgumentsTypes().stream()
						.anyMatch(type -> type.toString().equals("androidx.compose.runtime.Composer"))
				&& valueMth.getReturnType().isPrimitive();
	}

	private boolean isSafeSyntheticComposeColorUnwrapDuplication(BlockNode block) {
		if (BlockUtils.isExceptionHandlerPath(block)
				|| block.contains(AType.EXC_CATCH)
				|| block.getCleanSuccessors().size() != 1
				|| !hasComposerArgument()) {
			return false;
		}
		List<InvokeNode> calls = collectReadOnlyNestedInvokes(block);
		if (calls == null || calls.size() != 1) {
			return false;
		}
		MethodInfo wrapperInfo = calls.get(0).getCallMth();
		if (!wrapperInfo.getReturnType().equals(ArgType.LONG)
				|| !wrapperInfo.getArgumentsTypes().stream()
						.anyMatch(type -> type.toString().equals("androidx.compose.runtime.Composer"))
				|| !wrapperInfo.getArgumentsTypes().stream()
						.anyMatch(type -> type.toString().equals("androidx.compose.ui.graphics.Color"))) {
			return false;
		}
		MethodNode wrapperMth = mth.root().resolveMethod(wrapperInfo);
		if (wrapperMth == null || !wrapperMth.getParentClass().getAccessFlags().isSynthetic()) {
			return false;
		}
		List<InvokeNode> wrapperCalls = collectLinearWrapperInvokes(wrapperMth.getBasicBlocks());
		if (wrapperCalls == null) {
			return false;
		}
		return wrapperCalls.size() == 3
				&& isComposeGroupBoundary(wrapperCalls.get(0), "startReplaceGroup", 2)
				&& isComposeGroupBoundary(wrapperCalls.get(1), "endReplaceGroup", 1)
				&& isComposeColorUnboxCall(wrapperCalls.get(2));
	}

	static @Nullable List<InvokeNode> collectLinearWrapperInvokes(@Nullable List<BlockNode> wrapperBlocks) {
		if (wrapperBlocks == null) {
			// A resolved wrapper can still be unprocessed in a parallel class task.
			// Without its CFG the duplication proof is unavailable, so keep the original loop shape.
			return null;
		}
		List<InvokeNode> wrapperCalls = new ArrayList<>();
		for (BlockNode wrapperBlock : wrapperBlocks) {
			if (wrapperBlock.getCleanSuccessors().size() > 1) {
				return null;
			}
			for (InsnNode insn : wrapperBlock.getInstructions()) {
				Boolean invalid = insn.visitInsns(innerInsn -> {
					if (innerInsn instanceof InvokeNode) {
						wrapperCalls.add((InvokeNode) innerInsn);
						return null;
					}
					InsnType type = innerInsn.getType();
					return type == InsnType.RETURN || isReadOnlyValueInsnType(type) ? null : Boolean.TRUE;
				});
				if (invalid != null) {
					return null;
				}
			}
		}
		return wrapperCalls;
	}

	private static @Nullable List<InvokeNode> collectReadOnlyNestedInvokes(BlockNode block) {
		List<InsnNode> generatedInsns = block.getInstructions().stream()
				.filter(insn -> !insn.contains(AFlag.DONT_GENERATE))
				.toList();
		if (generatedInsns.size() != 1) {
			return null;
		}
		List<InvokeNode> calls = new ArrayList<>();
		Boolean invalid = generatedInsns.get(0).visitInsns(innerInsn -> {
			if (innerInsn instanceof InvokeNode) {
				calls.add((InvokeNode) innerInsn);
				return null;
			}
			return isReadOnlyValueInsnType(innerInsn.getType()) ? null : Boolean.TRUE;
		});
		return invalid == null ? calls : null;
	}

	private static @Nullable List<InvokeNode> collectReadOnlyInvokes(BlockNode block) {
		List<InvokeNode> calls = new ArrayList<>();
		for (InsnNode insn : block.getInstructions()) {
			if (insn.contains(AFlag.DONT_GENERATE)) {
				continue;
			}
			Boolean invalid = insn.visitInsns(innerInsn -> {
				if (innerInsn instanceof InvokeNode) {
					calls.add((InvokeNode) innerInsn);
					return null;
				}
				return isReadOnlyValueInsnType(innerInsn.getType()) ? null : Boolean.TRUE;
			});
			if (invalid != null) {
				return null;
			}
		}
		return calls;
	}

	static boolean isComposeGroupBoundary(InvokeNode invoke, String name, int argsCount) {
		return invoke.getCallMth().getDeclClass().getFullName().equals("androidx.compose.runtime.Composer")
				&& invoke.getCallMth().getName().equals(name)
				&& invoke.getArgsCount() == argsCount;
	}

	static boolean isComposeColorBoxCall(InvokeNode invoke) {
		return invoke.getCallMth().getDeclClass().getFullName().equals("androidx.compose.ui.graphics.Color")
				&& invoke.getCallMth().getName().equals("box-impl")
				&& invoke.getArgsCount() == 1;
	}

	static boolean isComposeColorUnboxCall(InvokeNode invoke) {
		return invoke.getCallMth().getDeclClass().getFullName().equals("androidx.compose.ui.graphics.Color")
				&& invoke.getCallMth().getName().equals("unbox-impl")
				&& invoke.getArgsCount() == 1;
	}

	private boolean isSafeComposeProtocolDuplication(BlockNode block) {
		// Compose emits the same compiler protocol blocks inside and outside loops.
		if (BlockUtils.isExceptionHandlerPath(block)
				|| block.contains(AType.EXC_CATCH)) {
			return false;
		}
		boolean foundProtocolCall = false;
		for (InsnNode insn : block.getInstructions()) {
			if (insn.contains(AFlag.DONT_GENERATE)) {
				continue;
			}
			if (insn instanceof InvokeNode) {
				InvokeNode invoke = (InvokeNode) insn;
				String declClass = invoke.getCallMth().getDeclClass().getFullName();
				String name = invoke.getCallMth().getName();
				if (isKnownComposeProtocolCall(declClass, name, invoke.getArgsCount())) {
					foundProtocolCall = true;
				} else {
					if (!isPureValueCall(declClass, name, invoke.getArgsCount())) {
						return false;
					}
					for (InsnArg arg : invoke.getArguments()) {
						if (!isSafeValueArg(block, arg)) {
							return false;
						}
					}
				}
			} else if (!isSafeComposeLocalValueInsn(insn)) {
				return false;
			}
		}
		if (!foundProtocolCall) {
			return false;
		}
		return hasComposerArgument();
	}

	private static boolean isSafeComposeLocalValueInsn(InsnNode insn) {
		InsnType type = insn.getType();
		return (type == InsnType.CONST || type == InsnType.MOVE || type == InsnType.ARITH)
				&& insn.getResult() != null
				&& (type == InsnType.ARITH || insn.getArgsCount() == 1);
	}

	static boolean isKnownComposeProtocolCall(String declClass, String name, int argsCount) {
		if (declClass.equals("androidx.compose.runtime.Composer")) {
			return name.equals("useNode") && argsCount == 1
					|| name.equals("createNode") && argsCount == 2
					|| name.equals("rememberedValue") && argsCount == 1
					|| name.equals("skipToGroupEnd") && argsCount == 1
					|| name.equals("startReplaceGroup") && argsCount == 2
					|| name.equals("endReplaceGroup") && argsCount == 1;
		}
		if (declClass.equals("androidx.compose.runtime.ScopeUpdateScope")) {
			return name.equals("updateScope") && argsCount == 2;
		}
		if (declClass.equals("androidx.compose.runtime.ComposerKt")) {
			return name.equals("traceEventEnd") && argsCount == 0;
		}
		return declClass.equals("androidx.compose.runtime.ComposablesKt")
				&& name.equals("invalidApplier")
				&& argsCount == 0;
	}

	private boolean isSafeTerminalThrowDuplication(BlockNode block) {
		// A known terminal throw has no fall-through path, so loop membership does not affect duplication
		// safety.
		if (BlockUtils.isExceptionHandlerPath(block)
				|| block.contains(AType.EXC_CATCH)
				|| block.getCleanSuccessors().size() != 1) {
			return false;
		}
		boolean foundTerminalThrow = false;
		for (InsnNode insn : block.getInstructions()) {
			if (isKnownTerminalThrowInvoke(insn)) {
				if (foundTerminalThrow) {
					return false;
				}
				for (InsnArg arg : insn.getArguments()) {
					if (!isSafeValueArg(block, arg)) {
						return false;
					}
				}
				foundTerminalThrow = true;
			} else if (!isReadOnlyValueInsn(insn)) {
				return false;
			}
		}
		return foundTerminalThrow;
	}

	static boolean isSafePlainThrowDuplication(BlockNode block) {
		// A direct throw only transfers an already computed exception value to the method exit.
		// Repeating this leaf cannot repeat construction or another wrapped side effect.
		if (BlockUtils.isExceptionHandlerPath(block)
				|| block.contains(AType.EXC_CATCH)
				|| block.getCleanSuccessors().size() != 1
				|| !block.getCleanSuccessors().get(0).contains(AFlag.MTH_EXIT_BLOCK)
				|| block.getInstructions().size() != 1) {
			return false;
		}
		InsnNode throwInsn = block.getInstructions().get(0);
		return throwInsn.getType() == InsnType.THROW
				&& !throwInsn.contains(AFlag.DONT_GENERATE)
				&& throwInsn.getArgsCount() == 1
				&& throwInsn.getArg(0).isRegister();
	}

	private static boolean isKnownTerminalThrowInvoke(InsnNode insn) {
		if (!(insn instanceof InvokeNode)) {
			return false;
		}
		InvokeNode invoke = (InvokeNode) insn;
		return isKnownTerminalThrowCall(
				invoke.getCallMth().getDeclClass().getFullName(), invoke.getCallMth().getName());
	}

	static boolean isKnownTerminalThrowCall(String declClass, String name) {
		return (declClass.equals("kotlin.jvm.internal.Intrinsics")
				&& name.equals("throwUninitializedPropertyAccessException"))
				|| (declClass.startsWith("kotlin.collections.")
						&& (name.equals("throwIndexOverflow") || name.equals("throwCountOverflow")));
	}

	private boolean isSafeCoroutineResumeRestoreDuplication(BlockNode block) {
		// Kotlin resume branches restore spilled values and then validate the saved result.
		if (mth.getLoopForBlock(block) != null
				|| BlockUtils.isExceptionHandlerPath(block)
				|| block.contains(AType.EXC_CATCH)
				|| block.getCleanSuccessors().size() != 1) {
			return false;
		}
		boolean foundThrowOnFailure = false;
		for (InsnNode insn : block.getInstructions()) {
			if (isThrowOnFailureInvoke(insn)) {
				if (foundThrowOnFailure) {
					return false;
				}
				for (InsnArg arg : insn.getArguments()) {
					if (!isSafeValueArg(block, arg)) {
						return false;
					}
				}
				foundThrowOnFailure = true;
			} else if (!isReadOnlyValueInsn(insn)) {
				return false;
			}
		}
		return foundThrowOnFailure;
	}

	private boolean isSafeCompilerLambdaDuplication(BlockNode block) {
		if (mth.getLoopForBlock(block) != null
				|| BlockUtils.isExceptionHandlerPath(block)
				|| block.contains(AType.EXC_CATCH)
				|| block.getCleanSuccessors().size() != 1) {
			return false;
		}
		List<InsnNode> generatedInsns = block.getInstructions().stream()
				.filter(insn -> !insn.contains(AFlag.DONT_GENERATE))
				.toList();
		if (generatedInsns.size() != 1 || !(generatedInsns.get(0) instanceof ConstructorInsn)) {
			return false;
		}
		ConstructorInsn ctrInsn = (ConstructorInsn) generatedInsns.get(0);
		RegisterArg result = ctrInsn.getResult();
		ClassNode ctrCls = mth.root().resolveClass(ctrInsn.getClassType());
		if (!ctrInsn.isNewInstance()
				|| result == null
				|| !result.getType().toString().startsWith("kotlin.jvm.functions.Function")
				|| ctrCls == null
				|| !ctrCls.getAccessFlags().isSynthetic()) {
			return false;
		}
		for (InsnArg arg : ctrInsn.getArguments()) {
			if (!isSafeValueArg(block, arg)) {
				return false;
			}
		}
		return true;
	}

	static boolean isSafeLoopConstantDuplication(BlockNode block) {
		if (block.getInstructions().isEmpty()) {
			return false;
		}
		for (InsnNode insn : block.getInstructions()) {
			InsnType type = insn.getType();
			if (type != InsnType.CONST && type != InsnType.CONST_STR) {
				return false;
			}
		}
		return true;
	}

	private boolean isSafeComposeRememberLambdaDuplication(BlockNode block) {
		// Compose emits the same remember/update pair for synthetic lambdas inside and outside loops.
		if (BlockUtils.isExceptionHandlerPath(block)
				|| block.contains(AType.EXC_CATCH)
				|| block.getCleanSuccessors().size() != 1) {
			return false;
		}
		InsnNode firstInsn = null;
		InsnNode secondInsn = null;
		for (InsnNode insn : block.getInstructions()) {
			if (insn.contains(AFlag.DONT_GENERATE)) {
				continue;
			}
			if (firstInsn == null) {
				firstInsn = insn;
			} else if (secondInsn == null) {
				secondInsn = insn;
			} else {
				return false;
			}
		}
		if (!(firstInsn instanceof ConstructorInsn) || !(secondInsn instanceof InvokeNode)) {
			return false;
		}
		ConstructorInsn ctrInsn = (ConstructorInsn) firstInsn;
		InvokeNode updateInvoke = (InvokeNode) secondInsn;
		RegisterArg result = ctrInsn.getResult();
		ClassNode ctrCls = mth.root().resolveClass(ctrInsn.getClassType());
		if (!ctrInsn.isNewInstance()
				|| result == null
				|| ctrCls == null
				|| !ctrCls.getAccessFlags().isSynthetic()
				|| !isKotlinFunctionClass(ctrCls)
				|| !isKnownComposeRememberUpdateCall(
						updateInvoke.getCallMth().getDeclClass().getFullName(),
						updateInvoke.getCallMth().getName(),
						updateInvoke.getArgsCount())
				|| !hasSameRegisterArg(updateInvoke, result)) {
			return false;
		}
		for (InsnArg arg : ctrInsn.getArguments()) {
			if (!isSafeValueArg(block, arg)) {
				return false;
			}
		}
		return true;
	}

	private static boolean isKotlinFunctionClass(ClassNode cls) {
		for (ArgType type : cls.getInterfaces()) {
			if (type.toString().startsWith("kotlin.jvm.functions.Function")) {
				return true;
			}
		}
		return false;
	}

	static boolean isKnownComposeRememberUpdateCall(String declClass, String name, int argsCount) {
		return declClass.equals("androidx.compose.runtime.Composer")
				&& name.equals("updateRememberedValue")
				&& argsCount == 2;
	}

	private static boolean isThrowOnFailureInvoke(InsnNode insn) {
		if (!(insn instanceof InvokeNode)) {
			return false;
		}
		InvokeNode invoke = (InvokeNode) insn;
		return invoke.getCallMth().getDeclClass().getFullName().equals("kotlin.ResultKt")
				&& invoke.getCallMth().getName().equals("throwOnFailure");
	}

	private boolean isSafePlainReturnDuplication(BlockNode block) {
		// Region traversal can revisit a single-predecessor block with different exit stacks.
		// A plain return is safe to copy regardless of the CFG predecessor count.
		if (mth.getLoopForBlock(block) != null
				|| BlockUtils.isExceptionHandlerPath(block)
				|| block.contains(AType.EXC_CATCH)) {
			return false;
		}
		boolean foundReturn = false;
		for (InsnNode insn : block.getInstructions()) {
			if (insn.contains(AFlag.DONT_GENERATE)) {
				if (!isReadOnlyValueInsn(insn)) {
					return false;
				}
				continue;
			}
			if (foundReturn || insn.getType() != InsnType.RETURN) {
				return false;
			}
			foundReturn = true;
			for (InsnArg arg : insn.getArguments()) {
				if (!isSafeValueArg(block, arg)) {
					return false;
				}
			}
		}
		return foundReturn;
	}

	private boolean isSafeChannelCancelConsumedDuplication(BlockNode block) {
		if (!mth.getName().equals("lastOrNull")
				|| !mth.getParentClass().getFullName()
						.equals("kotlinx.coroutines.channels.ChannelsKt__DeprecatedKt")
				|| block.getPredecessors().size() != 1
				|| block.getCleanSuccessors().size() != 1
				|| !isDuplicatedChannelCompletionBranch(block)
				|| !isSafePlainReturnDuplication(block.getCleanSuccessors().get(0))) {
			return false;
		}
		List<InsnNode> generatedInsns = block.getInstructions().stream()
				.filter(insn -> !insn.contains(AFlag.DONT_GENERATE))
				.toList();
		if (generatedInsns.size() != 1 || !(generatedInsns.get(0) instanceof InvokeNode)) {
			return false;
		}
		InvokeNode invoke = (InvokeNode) generatedInsns.get(0);
		return invoke.getCallMth().getDeclClass().getFullName().equals("kotlinx.coroutines.channels.ChannelsKt")
				&& invoke.getCallMth().getName().equals("cancelConsumed")
				&& invoke.getArgsCount() == 2
				&& invoke.getArg(1).isLiteral()
				&& ((LiteralArg) invoke.getArg(1)).getLiteral() == 0;
	}

	private boolean isDuplicatedChannelCompletionBranch(BlockNode block) {
		BlockNode condition = block.getPredecessors().get(0);
		if (!(BlockUtils.getLastInsn(condition) instanceof IfNode)
				|| condition.getCleanSuccessors().size() != 2
				|| !condition.getCleanSuccessors().contains(block)) {
			return false;
		}
		return condition.contains(AFlag.DUPLICATED)
				|| condition.getPredecessors().size() == 1
						&& condition.getPredecessors().get(0).getPredecessors().size() >= 2;
	}

	private boolean isSafeTerminalSideEffectDuplication(BlockNode block) {
		if (block.getPredecessors().size() < 2) {
			if (!allowSinglePredecessorTerminalDuplication || !isDuplicatedTerminalBranch(block)) {
				return false;
			}
		}
		if (BlockUtils.containsExitInsn(block)) {
			return !hasSensitiveDuplicationFlow(block);
		}
		List<BlockNode> successors = block.getCleanSuccessors();
		if (successors.size() != 1) {
			return false;
		}
		BlockNode successor = successors.get(0);
		if (!BlockUtils.containsExitInsn(successor)) {
			return false;
		}
		return !hasSensitiveDuplicationFlow(block)
				&& !hasSensitiveDuplicationFlow(successor);
	}

	private boolean isDuplicatedTerminalBranch(BlockNode block) {
		if (block.getPredecessors().size() != 1) {
			return false;
		}
		BlockNode condition = block.getPredecessors().get(0);
		if (!(BlockUtils.getLastInsn(condition) instanceof IfNode)
				|| condition.getCleanSuccessors().size() != 2
				|| !condition.getCleanSuccessors().contains(block)
				|| hasSensitiveDuplicationFlow(condition)) {
			return false;
		}
		boolean duplicatedCondition = condition.contains(AFlag.DUPLICATED);
		boolean conditionAfterMerge = condition.getPredecessors().size() == 1
				&& condition.getPredecessors().get(0).getPredecessors().size() >= 2;
		if (!duplicatedCondition && !conditionAfterMerge) {
			return false;
		}
		BlockNode otherBranch = condition.getCleanSuccessors().get(0) == block
				? condition.getCleanSuccessors().get(1)
				: condition.getCleanSuccessors().get(0);
		return BlockUtils.containsExitInsn(otherBranch);
	}

	private boolean hasSensitiveDuplicationFlow(BlockNode block) {
		PhiListAttr phiList = block.get(AType.PHI_LIST);
		return mth.getLoopForBlock(block) != null
				|| BlockUtils.isExceptionHandlerPath(block)
				|| block.contains(AType.EXC_CATCH)
				|| block.contains(AType.TRY_BLOCK)
				|| phiList != null && !phiList.getList().isEmpty();
	}

	private boolean isSafeConditionDuplication(BlockNode block) {
		if (BlockUtils.isExceptionHandlerPath(block)
				|| block.contains(AType.EXC_CATCH)
				|| block.getCleanSuccessors().size() != 2) {
			return false;
		}
		boolean foundIf = false;
		for (InsnNode insn : block.getInstructions()) {
			if (insn.contains(AFlag.DONT_GENERATE)) {
				if (!isReadOnlyValueInsn(insn)) {
					return false;
				}
				continue;
			}
			if (foundIf || insn.getType() != InsnType.IF) {
				return false;
			}
			foundIf = true;
			for (InsnArg arg : insn.getArguments()) {
				if (!isSafeValueArg(block, arg)) {
					return false;
				}
			}
		}
		return foundIf;
	}

	private boolean isSafeSwitchFallThroughDuplication(BlockNode block) {
		if (!block.contains(AFlag.FALL_THROUGH)
				|| block.getPredecessors().size() < 2
				|| BlockUtils.isExceptionHandlerPath(block)
				|| block.contains(AType.EXC_CATCH)
				|| block.contains(AType.TRY_BLOCK)) {
			return false;
		}
		PhiListAttr phiList = block.get(AType.PHI_LIST);
		return phiList == null || phiList.getList().isEmpty();
	}

	private boolean isSafeDistinctLiteralBranchDuplication(BlockNode block) {
		if (block.getPredecessors().size() != 2
				|| block.getCleanSuccessors().size() != 1
				|| hasSensitiveDuplicationFlow(block)) {
			return false;
		}
		InvokeNode invoke = null;
		for (InsnNode insn : block.getInstructions()) {
			if (insn.contains(AFlag.DONT_GENERATE)) {
				continue;
			}
			if (invoke != null || !(insn instanceof InvokeNode) || insn.getResult() != null) {
				return false;
			}
			invoke = (InvokeNode) insn;
		}
		if (invoke == null) {
			return false;
		}
		RegisterArg discriminator = null;
		Long firstLiteral = null;
		for (BlockNode predecessor : block.getPredecessors()) {
			InsnNode lastInsn = BlockUtils.getLastInsn(predecessor);
			if (!(lastInsn instanceof IfNode)) {
				return false;
			}
			IfNode ifNode = (IfNode) lastInsn;
			if (ifNode.getOp() != IfOp.EQ || ifNode.getThenBlock() != block) {
				return false;
			}
			InsnArg firstArg = ifNode.getArg(0);
			InsnArg secondArg = ifNode.getArg(1);
			RegisterArg registerArg;
			LiteralArg literalArg;
			if (firstArg instanceof RegisterArg && secondArg instanceof LiteralArg) {
				registerArg = (RegisterArg) firstArg;
				literalArg = (LiteralArg) secondArg;
			} else if (firstArg instanceof LiteralArg && secondArg instanceof RegisterArg) {
				registerArg = (RegisterArg) secondArg;
				literalArg = (LiteralArg) firstArg;
			} else {
				return false;
			}
			if (registerArg.getSVar() == null) {
				return false;
			}
			if (discriminator == null) {
				discriminator = registerArg;
				firstLiteral = literalArg.getLiteral();
			} else if (registerArg.getSVar() != discriminator.getSVar()
					|| firstLiteral == literalArg.getLiteral()) {
				return false;
			}
		}
		return true;
	}

	private boolean isSafeReadOnlyValueDuplication(BlockNode block) {
		// CFG predecessor count is not a safety condition for side-effect-free instructions.
		if ((mth.getLoopForBlock(block) != null && !isSafeLoopConstantDuplication(block))
				|| BlockUtils.isExceptionHandlerPath(block)
				|| block.contains(AType.EXC_CATCH)
				|| block.getInstructions().isEmpty()) {
			return false;
		}
		for (InsnNode insn : block.getInstructions()) {
			if (!isReadOnlyValueInsn(insn)) {
				return false;
			}
		}
		return true;
	}

	private boolean isSafeValueArg(BlockNode block, InsnArg arg) {
		if (arg instanceof InsnWrapArg) {
			InsnNode wrapInsn = ((InsnWrapArg) arg).getWrapInsn();
			return isReadOnlyValueInsn(wrapInsn);
		}
		if (arg instanceof RegisterArg) {
			RegisterArg registerArg = (RegisterArg) arg;
			if (registerArg.getSVar() == null) {
				return true;
			}
			InsnNode assignInsn = registerArg.getSVar().getAssignInsn();
			if (assignInsn == null) {
				return true;
			}
			BlockNode assignBlock = BlockUtils.getBlockByInsn(mth, assignInsn);
			return assignBlock != block || isReadOnlyValueInsn(assignInsn);
		}
		return true;
	}

	private boolean isReadOnlyValueInsn(InsnNode insn) {
		Boolean invalid = insn.visitInsns(innerInsn -> {
			if (innerInsn.contains(AType.EXC_CATCH)) {
				return Boolean.TRUE;
			}
			if (innerInsn.getType() == InsnType.INVOKE) {
				return isPureValueInvoke((InvokeNode) innerInsn) ? null : Boolean.TRUE;
			}
			return isReadOnlyValueInsnType(innerInsn.getType()) ? null : Boolean.TRUE;
		});
		return invalid == null;
	}

	static boolean isReadOnlyValueInsnType(InsnType type) {
		switch (type) {
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
			case NEW_ARRAY:
			case FILLED_NEW_ARRAY:
			case AGET:
			case IGET:
			case SGET:
			case PHI:
				return true;
			default:
				return false;
		}
	}

	private boolean isPureValueInvoke(InvokeNode invoke) {
		MethodInfo callInfo = invoke.getCallMth();
		if (pureValueCallCache != null) {
			Boolean cached = pureValueCallCache.get(callInfo);
			if (cached != null) {
				return cached;
			}
		}
		boolean result = checkPureValueCall(callInfo, invoke.getArgsCount());
		if (pureValueCallCache == null) {
			pureValueCallCache = new HashMap<>();
		}
		pureValueCallCache.put(callInfo, result);
		return result;
	}

	private boolean checkPureValueCall(MethodInfo callInfo, int invokeArgsCount) {
		String declClass = callInfo.getDeclClass().getFullName();
		String name = callInfo.getName();
		if (isPureValueCall(declClass, name, invokeArgsCount)) {
			return true;
		}
		if (isPrimitiveStringValueOf(declClass, name, callInfo.getArgumentsTypes())) {
			return true;
		}
		MethodNode callMth = mth.root().resolveMethod(callInfo);
		if (callMth == null || !callMth.contains(AType.READ_ONLY_METHOD)) {
			return false;
		}
		ClassNode callerTopCls = mth.getTopParentClass();
		ClassNode calleeTopCls = callMth.getTopParentClass();
		return callerTopCls == calleeTopCls || callerTopCls.getDependencies().contains(calleeTopCls);
	}

	static boolean isPrimitiveStringValueOf(String declClass, String name, List<ArgType> argTypes) {
		return declClass.equals("java.lang.String")
				&& name.equals("valueOf")
				&& argTypes.size() == 1
				&& argTypes.get(0).isPrimitive();
	}

	static boolean isPureValueCall(String declClass, String name, int argsCount) {
		if (argsCount == 1) {
			if (declClass.equals("java.util.List")) {
				return name.equals("size");
			}
			if (declClass.equals("androidx.compose.ui.Alignment.Companion")) {
				return name.equals("getStart");
			}
			if (declClass.equals("androidx.compose.ui.unit.Dp.Companion")) {
				return name.equals("getUnspecified-D9Ej5fM");
			}
			if (declClass.equals("androidx.compose.ui.unit.Velocity.Companion")) {
				return name.equals("getZero-9UxMQ8M");
			}
			if (declClass.equals("androidx.compose.ui.text.style.TextAlign.Companion")) {
				return name.equals("getRight-e0LSkKk");
			}
			if (declClass.equals("kotlin.coroutines.jvm.internal.Boxing")) {
				return name.equals("boxBoolean")
						|| name.equals("boxByte")
						|| name.equals("boxChar")
						|| name.equals("boxShort")
						|| name.equals("boxInt")
						|| name.equals("boxLong")
						|| name.equals("boxFloat")
						|| name.equals("boxDouble");
			}
			if (declClass.equals("java.lang.Boolean")) {
				return name.equals("booleanValue");
			}
			if (declClass.equals("java.lang.Byte")) {
				return name.equals("byteValue");
			}
			if (declClass.equals("java.lang.Character")) {
				return name.equals("charValue");
			}
			if (declClass.equals("java.lang.Short")) {
				return name.equals("shortValue");
			}
			if (declClass.equals("java.lang.Integer")) {
				return name.equals("intValue");
			}
			if (declClass.equals("java.lang.Long")) {
				return name.equals("longValue");
			}
			if (declClass.equals("java.lang.Float")) {
				return name.equals("floatValue");
			}
			if (declClass.equals("java.lang.Double")) {
				return name.equals("doubleValue");
			}
			if (declClass.equals("retrofit2.Response")) {
				return name.equals("body")
						|| name.equals("code")
						|| name.equals("errorBody")
						|| name.equals("headers")
						|| name.equals("isSuccessful")
						|| name.equals("message")
						|| name.equals("raw");
			}
			if (name.equals("getValue")) {
				return declClass.equals("kotlinx.coroutines.flow.StateFlow")
						|| declClass.equals("kotlinx.coroutines.flow.MutableStateFlow");
			}
		}
		if (declClass.equals("kotlin.Result") && name.equals("constructor-impl") && argsCount == 1) {
			return true;
		}
		if (declClass.equals("androidx.compose.ui.unit.Dp") && name.equals("constructor-impl") && argsCount == 1) {
			return true;
		}
		if (declClass.equals("java.util.List") && name.equals("get") && argsCount == 2) {
			return true;
		}
		if (declClass.equals("kotlin.TuplesKt") && name.equals("to") && argsCount == 2) {
			return true;
		}
		if (declClass.startsWith("kotlin.collections.")) {
			return name.equals("emptyList") && argsCount == 0
					|| name.equals("arrayListOf") && argsCount == 1;
		}
		return false;
	}

	private boolean isSafeCoroutineStateInitDuplication(BlockNode block) {
		if (mth.getLoopForBlock(block) != null
				|| BlockUtils.isExceptionHandlerPath(block)
				|| block.getPredecessors().size() != 2
				|| block.getCleanSuccessors().size() != 1) {
			return false;
		}
		List<InsnNode> generatedInsns = block.getInstructions().stream()
				.filter(insn -> !insn.contains(AFlag.DONT_GENERATE))
				.toList();
		ConstructorInsn ctrInsn;
		if (generatedInsns.size() == 1 && generatedInsns.get(0) instanceof ConstructorInsn) {
			ctrInsn = (ConstructorInsn) generatedInsns.get(0);
		} else if (generatedInsns.size() == 2) {
			InsnNode firstInsn = generatedInsns.get(0);
			InsnNode secondInsn = generatedInsns.get(1);
			if (firstInsn instanceof ConstructorInsn && isPlainMove(secondInsn)) {
				ctrInsn = (ConstructorInsn) firstInsn;
			} else if (isPlainMove(firstInsn) && secondInsn instanceof ConstructorInsn) {
				ctrInsn = (ConstructorInsn) secondInsn;
			} else {
				return false;
			}
		} else {
			return false;
		}
		RegisterArg result = ctrInsn.getResult();
		boolean continuationClass = isCoroutineContinuationClass(ctrInsn.getClassType());
		if (!ctrInsn.isNewInstance()
				|| result == null
				|| !isInnerClassOf(ctrInsn.getClassType(), mth.getParentClass().getClassInfo())
						&& !continuationClass) {
			return false;
		}
		RegisterArg continuationArg = mth.getArgRegs().stream()
				.filter(arg -> CoroutineMethodUtils.isKnownContinuationType(arg.getType()))
				.findFirst()
				.orElse(null);
		if (continuationArg == null && continuationClass && !mth.getArgRegs().isEmpty()) {
			RegisterArg lastArg = mth.getArgRegs().get(mth.getArgRegs().size() - 1);
			if (hasSameRegisterArgOrMoveAlias(ctrInsn, lastArg)) {
				continuationArg = lastArg;
			}
		}
		if (continuationArg == null || !hasSameRegisterArgOrMoveAlias(ctrInsn, continuationArg)) {
			return false;
		}
		BlockNode firstPred = block.getPredecessors().get(0);
		BlockNode secondPred = block.getPredecessors().get(1);
		if (!(BlockUtils.getLastInsn(firstPred) instanceof IfNode)
				|| !(BlockUtils.getLastInsn(secondPred) instanceof IfNode)
				|| !BlockUtils.isPathExists(firstPred, secondPred)
						&& !BlockUtils.isPathExists(secondPred, firstPred)) {
			return false;
		}
		PhiListAttr phiList = block.getCleanSuccessors().get(0).get(AType.PHI_LIST);
		if (phiList == null) {
			return false;
		}
		for (PhiInsn phi : phiList.getList()) {
			RegisterArg blockArg = phi.getArgByBlock(block);
			if (blockArg != null && blockArg.sameCodeVar(result)) {
				return true;
			}
		}
		return false;
	}

	private static boolean isInnerClassOf(ClassInfo clsInfo, ClassInfo expectedParent) {
		if (clsInfo.makeRawFullName().startsWith(expectedParent.makeRawFullName() + '$')
				|| clsInfo.makeAliasRawFullName().startsWith(expectedParent.makeAliasRawFullName() + '$')) {
			return true;
		}
		ClassInfo parent = clsInfo.getParentClass();
		for (int depth = 0; parent != null && depth < 8; depth++) {
			if (parent.equals(expectedParent)) {
				return true;
			}
			parent = parent.getParentClass();
		}
		return false;
	}

	private boolean isCoroutineContinuationClass(ClassInfo clsInfo) {
		return ArgType.isInstanceOf(mth.root(), clsInfo.getType(),
				ArgType.object("kotlin.coroutines.jvm.internal.ContinuationImpl"));
	}

	private static boolean hasSameRegisterArg(InsnNode insn, RegisterArg expected) {
		for (InsnArg arg : insn.getArguments()) {
			if (arg instanceof RegisterArg && ((RegisterArg) arg).sameCodeVar(expected)) {
				return true;
			}
		}
		return false;
	}

	private static boolean isPlainMove(InsnNode insn) {
		return insn.getType() == InsnType.MOVE
				&& insn.getResult() != null
				&& insn.getArgsCount() == 1;
	}

	private static boolean hasSameRegisterArgOrMoveAlias(InsnNode insn, RegisterArg expected) {
		for (InsnArg arg : insn.getArguments()) {
			if (hasSameRegisterArgOrMoveAlias(arg, expected)) {
				return true;
			}
		}
		return false;
	}

	private static boolean hasSameRegisterArgOrMoveAlias(InsnArg arg, RegisterArg expected) {
		if (arg instanceof InsnWrapArg) {
			InsnNode wrapInsn = ((InsnWrapArg) arg).getWrapInsn();
			if ((wrapInsn.getType() == InsnType.CHECK_CAST || wrapInsn.getType() == InsnType.MOVE)
					&& wrapInsn.getArgsCount() == 1) {
				return hasSameRegisterArgOrMoveAlias(wrapInsn.getArg(0), expected);
			}
			return false;
		}
		if (!(arg instanceof RegisterArg)) {
			return false;
		}
		RegisterArg registerArg = (RegisterArg) arg;
		if (registerArg.sameCodeVar(expected)) {
			return true;
		}
		if (registerArg.getSVar() != null) {
			InsnNode assignInsn = registerArg.getSVar().getAssignInsn();
			if (assignInsn != null
					&& assignInsn.getType() == InsnType.MOVE
					&& assignInsn.getArgsCount() == 1) {
				return hasSameRegisterArgOrMoveAlias(assignInsn.getArg(0), expected);
			}
		}
		return false;
	}

	static boolean hasNoGeneratedCode(BlockNode block) {
		for (InsnNode insn : block.getInstructions()) {
			if (!insn.contains(AFlag.DONT_GENERATE)) {
				return false;
			}
		}
		return true;
	}

	static boolean isSafeLocalAssignmentDuplication(BlockNode block) {
		List<InsnNode> insns = block.getInstructions();
		if (insns.isEmpty()) {
			return false;
		}
		for (InsnNode insn : insns) {
			InsnType type = insn.getType();
			if ((type != InsnType.CONST && type != InsnType.MOVE && type != InsnType.ARITH)
					|| insn.getResult() == null
					|| (type != InsnType.ARITH && insn.getArgsCount() != 1)) {
				return false;
			}
		}
		return true;
	}

	static boolean isSafeBooleanNumericConversionDuplication(BlockNode block) {
		InsnNode conversion = null;
		for (InsnNode insn : block.getInstructions()) {
			if (insn.contains(AFlag.DONT_GENERATE)) {
				continue;
			}
			if (conversion != null || !(insn instanceof TernaryInsn)
					|| !insn.contains(AType.BOOLEAN_NUMERIC_CONVERSION)
					|| insn.getResult() == null
					|| !ArgType.INT.equals(insn.getResult().getInitType())
							&& !ArgType.INT.equals(insn.getResult().getType())
					|| insn.getArgsCount() != 2 || !insn.getArg(0).isLiteral() || !insn.getArg(1).isLiteral()
					|| ((LiteralArg) insn.getArg(0)).getLiteral() != 1
					|| ((LiteralArg) insn.getArg(1)).getLiteral() != 0) {
				return false;
			}
			TernaryInsn ternary = (TernaryInsn) insn;
			List<RegisterArg> conditionArgs = ternary.getCondition().getRegisterArgs();
			if (conditionArgs.size() != 1
					|| !ArgType.BOOLEAN.equals(conditionArgs.get(0).getInitType())
							&& !ArgType.BOOLEAN.equals(conditionArgs.get(0).getType())) {
				return false;
			}
			conversion = insn;
		}
		return conversion != null;
	}

	Region makeRegionAfterRemovingLoop(BlockNode startBlock) {
		List<Set<BlockNode>> outerStates = activeRegionStates.remove(startBlock);
		try {
			return makeRegion(startBlock);
		} finally {
			if (outerStates != null && !outerStates.isEmpty()) {
				activeRegionStates.put(startBlock, outerStates);
			}
		}
	}

	/**
	 * Recursively traverse all blocks from 'block' until block from 'exits'
	 */
	private @Nullable BlockNode traverse(Region r, BlockNode block) {
		if (block.contains(AFlag.MTH_EXIT_BLOCK)) {
			return null;
		}
		if (loopCarryTailBlocks.contains(block)) {
			// This register-only back-edge tail is already owned by its LoopRegion body.
			return null;
		}
		BlockNode next = null;
		boolean processed = false;

		List<LoopInfo> loops = block.getAll(AType.LOOP);
		int loopCount = loops.size();
		if (loopCount != 0 && block.contains(AFlag.LOOP_START)) {
			if (loopCount == 1) {
				next = loopMaker.process(r, loops.get(0), stack);
				processed = true;
			} else {
				for (LoopInfo loop : loops) {
					if (loop.getStart() == block) {
						next = loopMaker.process(r, loop, stack);
						processed = true;
						break;
					}
				}
			}
		}

		InsnNode insn = BlockUtils.getLastInsn(block);
		if (!processed && insn != null) {
			switch (insn.getType()) {
				case IF:
					next = ifMaker.process(r, block, (IfNode) insn, stack);
					processed = true;
					break;

				case SWITCH:
					SwitchRegionMaker switchMaker = new SwitchRegionMaker(mth, this);
					next = switchMaker.process(r, block, (SwitchInsn) insn, stack);
					processed = true;
					break;

				case MONITOR_ENTER:
					SynchronizedRegionMaker syncMaker = new SynchronizedRegionMaker(mth, this);
					next = syncMaker.process(r, block, insn, stack);
					processed = true;
					break;
			}
		}
		if (!processed) {
			r.add(block);
			next = getNextBlock(block);
		}
		if (next != null && !stack.containsExit(block) && !stack.containsExit(next)) {
			return next;
		}
		return null;
	}

	private void insertEdgeInsns(Region region, BlockNode exitBlock) {
		List<EdgeInsnAttr> edgeInsns = exitBlock.getAll(AType.EDGE_INSN);
		if (edgeInsns.isEmpty()) {
			return;
		}
		List<InsnNode> insns = new ArrayList<>(edgeInsns.size());
		addOneInsnOfType(insns, edgeInsns, InsnType.BREAK);
		addOneInsnOfType(insns, edgeInsns, InsnType.CONTINUE);
		region.add(new InsnContainer(insns));
	}

	private void addOneInsnOfType(List<InsnNode> insns, List<EdgeInsnAttr> edgeInsns, InsnType insnType) {
		for (EdgeInsnAttr edgeInsn : edgeInsns) {
			InsnNode insn = edgeInsn.getInsn();
			if (insn.getType() == insnType) {
				insns.add(insn);
				return;
			}
		}
	}

	RegionStack getStack() {
		return stack;
	}

	boolean isProcessed(BlockNode block) {
		return processedBlocks.contains(block);
	}

	void clearBlockProcessedState(BlockNode block) {
		processedBlocks.remove(block);
	}

	void registerLoopCarryTail(BlockNode block) {
		loopCarryTailBlocks.add(block);
	}
}
