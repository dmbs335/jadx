package jadx.core.dex.visitors.regions;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import jadx.core.dex.attributes.AFlag;
import jadx.core.dex.instructions.InsnType;
import jadx.core.dex.instructions.args.InsnArg;
import jadx.core.dex.nodes.BlockNode;
import jadx.core.dex.nodes.IContainer;
import jadx.core.dex.nodes.IRegion;
import jadx.core.dex.nodes.InsnContainer;
import jadx.core.dex.nodes.InsnNode;
import jadx.core.dex.regions.Region;
import jadx.core.dex.regions.SwitchRegion;
import jadx.core.dex.regions.TryCatchRegion;
import jadx.core.dex.regions.conditions.IfRegion;
import jadx.core.dex.trycatch.CatchAttr;
import jadx.core.dex.trycatch.ExceptionHandler;
import jadx.core.dex.trycatch.TryCatchBlockAttr;
import jadx.core.utils.BlockUtils;

import static jadx.core.dex.visitors.blocks.BlockSplitter.connect;
import static org.assertj.core.api.Assertions.assertThat;

class ProcessTryCatchRegionsTest {
	private static final AtomicInteger BLOCK_IDS = new AtomicInteger();

	@Test
	void distinguishesConditionalRetryFromTerminalCleanup() {
		BlockNode rethrow = terminal(InsnType.THROW);
		Region body = new Region(null);
		IfRegion condition = new IfRegion(body);
		condition.setThenRegion(rethrow);
		body.add(condition);
		TryCatchBlockAttr tb = handlerWithBody(body);
		tb.getHandlers().get(0).getBlocks().add(rethrow);
		assertThat(ProcessTryCatchRegions.hasMixedHandlerExits(tb)).isTrue();
		body.add(rethrow);
		assertThat(ProcessTryCatchRegions.hasMixedHandlerExits(tb)).isFalse();
	}

	@Test
	void recognizesRethrowOrMaterializedLoopTransfer() {
		for (InsnType transfer : List.of(InsnType.CONTINUE, InsnType.BREAK)) {
			BlockNode rethrow = terminal(InsnType.THROW);
			Region body = new Region(null);
			IfRegion condition = new IfRegion(body);
			condition.setThenRegion(rethrow);
			condition.setElseRegion(terminal(transfer));
			body.add(condition);
			TryCatchBlockAttr tb = handlerWithBody(body);
			tb.getHandlers().get(0).getBlocks().add(rethrow);
			assertThat(ProcessTryCatchRegions.hasMixedHandlerExits(tb)).isTrue();
			assertThat(ProcessTryCatchRegions.hasRepeatableRethrowHandlers(tb)).isFalse();
		}
	}

	@Test
	void recognizesLoopTransferAfterConditionalRethrow() {
		BlockNode rethrow = terminal(InsnType.THROW);
		Region body = new Region(null);
		IfRegion condition = new IfRegion(body);
		condition.setThenRegion(rethrow);
		body.add(condition);
		body.add(terminal(InsnType.CONTINUE));
		TryCatchBlockAttr tb = handlerWithBody(body);
		tb.getHandlers().get(0).getBlocks().add(rethrow);
		assertThat(ProcessTryCatchRegions.hasMixedHandlerExits(tb)).isTrue();
		assertThat(ProcessTryCatchRegions.hasRepeatableRethrowHandlers(tb)).isFalse();
	}

	@Test
	void keepsScopeBoundaryForRethrowOrReturnWithoutAllowingHandlerCopies() {
		BlockNode rethrow = terminal(InsnType.THROW);
		BlockNode returned = terminal(InsnType.RETURN);
		Region body = new Region(null);
		IfRegion condition = new IfRegion(body);
		condition.setThenRegion(rethrow);
		condition.setElseRegion(returned);
		body.add(condition);
		TryCatchBlockAttr tb = handlerWithBody(body);
		tb.getHandlers().get(0).getBlocks().addAll(List.of(rethrow, returned));
		assertThat(ProcessTryCatchRegions.hasMixedHandlerExits(tb)).isTrue();
		assertThat(ProcessTryCatchRegions.hasRepeatableRethrowHandlers(tb)).isFalse();
	}

	@Test
	void detectsUnprotectedCallEvenWhenAllProtectedBlocksAreCovered() {
		BlockNode protectedCall = terminal(InsnType.INVOKE);
		TryCatchBlockAttr tb = new TryCatchBlockAttr(0, List.of(), List.of(protectedCall));
		Region body = new Region(null);
		body.add(terminal(InsnType.INVOKE));
		body.add(protectedCall);
		Region root = new Region(null);
		root.add(wrap(root, tb, body));
		assertThat(ProcessTryCatchRegions.containsUnwrappedProtectedOccurrence(root, tb)).isFalse();
		assertThat(ProcessTryCatchRegions.hasOverextendedProtectedScope(root, tb)).isTrue();
	}

	@Test
	void leavesSingleExitHandlersToExistingReconstruction() {
		for (InsnType exit : List.of(InsnType.RETURN, InsnType.CONTINUE, InsnType.BREAK, InsnType.INVOKE)) {
			Region body = new Region(null);
			body.add(terminal(exit));
			TryCatchBlockAttr tb = handlerWithBody(body);
			assertThat(ProcessTryCatchRegions.hasMixedHandlerExits(tb)).isFalse();
			assertThat(ProcessTryCatchRegions.hasRepeatableRethrowHandlers(tb)).isFalse();
			tb.getHandlers().get(0).setFinally(true);
			assertThat(ProcessTryCatchRegions.hasMixedHandlerExits(tb)).isFalse();
		}
	}

	@Test
	void doesNotGuessAnExitFromUnstructuredOrCyclicHandlers() {
		assertThat(ProcessTryCatchRegions.hasMixedHandlerExits(handlerWithBody(null))).isFalse();
		Region hidden = new Region(null);
		hidden.add(AFlag.DONT_GENERATE);
		assertThat(ProcessTryCatchRegions.hasMixedHandlerExits(handlerWithBody(hidden))).isFalse();
		for (InsnType exit : List.of(InsnType.GOTO, InsnType.SWITCH, InsnType.IF)) {
			Region body = new Region(null);
			body.add(terminal(exit));
			assertThat(ProcessTryCatchRegions.hasMixedHandlerExits(handlerWithBody(body))).isFalse();
		}
		Region cycle = new Region(null);
		cycle.add(cycle);
		assertThat(ProcessTryCatchRegions.hasMixedHandlerExits(handlerWithBody(cycle))).isFalse();
	}

	@Test
	void preservesBoundaryForReturnOrRetryWithoutRethrow() {
		Region body = new Region(null);
		IfRegion branch = new IfRegion(body);
		branch.setThenRegion(terminal(InsnType.RETURN));
		branch.setElseRegion(terminal(InsnType.CONTINUE));
		body.add(branch);
		TryCatchBlockAttr tb = handlerWithBody(body);
		assertThat(ProcessTryCatchRegions.hasMixedHandlerExits(tb)).isTrue();
		assertThat(ProcessTryCatchRegions.hasRepeatableRethrowHandlers(tb)).isFalse();
		tb.getHandlers().get(0).setFinally(true);
		assertThat(ProcessTryCatchRegions.hasMixedHandlerExits(tb)).isFalse();
	}

	@Test
	void doesNotSplitSharedImplicitRetryWithoutTerminalPath() {
		Region body = new Region(null);
		IfRegion branch = new IfRegion(body);
		branch.setThenRegion(terminal(InsnType.CONTINUE));
		body.add(branch);
		TryCatchBlockAttr tb = handlerWithBody(body);
		assertThat(ProcessTryCatchRegions.hasMixedHandlerExits(tb)).isFalse();
		assertThat(ProcessTryCatchRegions.hasRepeatableRethrowHandlers(tb)).isFalse();
	}

	@Test
	void ignoresHandlerCallsAndUnprotectedCallsOutsideTry() {
		BlockNode protectedCall = terminal(InsnType.INVOKE);
		Region handler = new Region(null);
		handler.add(terminal(InsnType.INVOKE));
		handler.add(terminal(InsnType.THROW));
		TryCatchBlockAttr tb = new TryCatchBlockAttr(0, handlerWithBody(handler).getHandlers(), List.of(protectedCall));
		Region root = new Region(null);
		root.add(terminal(InsnType.INVOKE));
		root.add(wrap(root, tb, protectedCall));
		root.add(terminal(InsnType.INVOKE));
		assertThat(ProcessTryCatchRegions.hasOverextendedProtectedScope(root, tb)).isFalse();
	}

	@Test
	void checksEachProtectedCopyAndTerminatesOnSharedRegions() {
		BlockNode protectedCall = terminal(InsnType.INVOKE);
		TryCatchBlockAttr tb = new TryCatchBlockAttr(0, List.of(), List.of(protectedCall));
		Region root = new Region(null);
		TryCatchRegion first = wrap(root, tb, protectedCall);
		root.add(first);
		root.add(first);
		root.add(root);
		assertThat(ProcessTryCatchRegions.hasOverextendedProtectedScope(root, tb)).isFalse();
		Region second = new Region(root);
		second.add(wrap(second, tb, terminal(InsnType.INVOKE)));
		root.add(second);
		assertThat(ProcessTryCatchRegions.hasOverextendedProtectedScope(root, tb)).isTrue();
	}

	@Test
	void findsProtectedSwitchCasesWithoutRecursingThroughRegionCycles() {
		BlockNode first = terminal(InsnType.INVOKE);
		BlockNode second = terminal(InsnType.INVOKE);
		TryCatchBlockAttr tb = new TryCatchBlockAttr(0, List.of(), List.of(first, second));
		Region root = new Region(null);
		SwitchRegion switchRegion = new SwitchRegion(root, terminal(InsnType.SWITCH), null);
		Region firstCase = new Region(switchRegion);
		firstCase.add(first);
		firstCase.add(firstCase);
		switchRegion.addCase(List.of(1), firstCase);
		switchRegion.addCase(List.of(2), second);
		root.add(root);
		root.add(switchRegion);
		assertThat(ProcessTryCatchRegions.hasProtectedBlocksInDifferentSwitchCases(root, tb)).isTrue();

		TryCatchBlockAttr oneCase = new TryCatchBlockAttr(1, List.of(), List.of(first));
		assertThat(ProcessTryCatchRegions.hasProtectedBlocksInDifferentSwitchCases(root, oneCase)).isFalse();
	}

	@Test
	void anchorsMultipleProtectedBlocksInEmittedOrder() {
		BlockNode first = terminal(InsnType.INVOKE);
		BlockNode second = terminal(InsnType.INVOKE);
		Region handler = new Region(null);
		handler.add(terminal(InsnType.THROW));
		TryCatchBlockAttr tb = new TryCatchBlockAttr(0, handlerWithBody(handler).getHandlers(), List.of(second, first));
		Region body = new Region(null);
		body.add(first);
		body.add(second);

		assertThat(ProcessTryCatchRegions.findDirectProtectedAnchor(body, tb)).isSameAs(first);
		body.add(terminal(InsnType.INVOKE));
		assertThat(ProcessTryCatchRegions.findDirectProtectedAnchor(body, tb)).isNull();
	}

	@Test
	void rejectsMultipleBlockAnchorForRetryHandler() {
		BlockNode first = terminal(InsnType.INVOKE);
		BlockNode second = terminal(InsnType.INVOKE);
		Region handler = new Region(null);
		handler.add(terminal(InsnType.RETURN));
		TryCatchBlockAttr tb = new TryCatchBlockAttr(0, handlerWithBody(handler).getHandlers(), List.of(first, second));
		Region body = new Region(null);
		body.add(first);
		body.add(second);
		assertThat(ProcessTryCatchRegions.findDirectProtectedAnchor(body, tb)).isNull();
	}

	@Test
	void acceptsNonThrowingUnprotectedCodeAndSharedProtectedRegion() {
		BlockNode call = terminal(InsnType.INVOKE);
		TryCatchBlockAttr tb = new TryCatchBlockAttr(0, List.of(), List.of(call));
		Region shared = new Region(null);
		shared.add(call);
		Region body = new Region(null);
		body.add(terminal(InsnType.CONST));
		body.add(shared);
		body.add(shared);
		assertThat(isProtected(body, tb)).isTrue();
		body.add(body);
		assertThat(isProtected(body, tb)).isFalse();
	}

	@Test
	void rejectsWrappedUnprotectedCallEvenWhenOuterInsnDoesNotThrow() {
		InsnNode move = new InsnNode(InsnType.MOVE, 1);
		move.addArg(InsnArg.wrapArg(new InsnNode(InsnType.INVOKE, 0)));
		BlockNode block = new BlockNode(1, 0, 0);
		block.getInstructions().add(move);
		assertThat(isProtected(block, new TryCatchBlockAttr(0, List.of(), List.of()))).isFalse();
	}

	@Test
	void rejectsDifferentInstructionCatchEvenInsideProtectedBlock() {
		BlockNode call = terminal(InsnType.INVOKE);
		TryCatchBlockAttr tb = new TryCatchBlockAttr(0, List.of(), List.of(call));
		TryCatchBlockAttr other = handlerWithBody(new Region(null));
		call.getInstructions().get(0).addAttr(CatchAttr.build(new ArrayList<>(other.getHandlers())));
		assertThat(isProtected(call, tb)).isFalse();
		assertThat(isProtected(call, other)).isTrue();
	}

	@Test
	void requiresTheFullHandlerSetRegardlessOfOffsetOrder() {
		ExceptionHandler first = ExceptionHandler.build(null, 1, null);
		ExceptionHandler second = ExceptionHandler.build(null, 2, null);
		ExceptionHandler third = ExceptionHandler.build(null, 3, null);
		BlockNode call = terminal(InsnType.INVOKE);
		TryCatchBlockAttr tb = new TryCatchBlockAttr(0, List.of(first, second), List.of(call));
		call.getInstructions().get(0).addAttr(CatchAttr.build(new ArrayList<>(List.of(first))));
		assertThat(isProtected(call, tb)).isFalse();
		call.getInstructions().get(0).addAttr(CatchAttr.build(new ArrayList<>(List.of(first, third))));
		assertThat(isProtected(call, tb)).isFalse();
		call.getInstructions().get(0).addAttr(CatchAttr.build(new ArrayList<>(List.of(first, second))));
		assertThat(isProtected(call, tb)).isTrue();
		TryCatchBlockAttr reversed = new TryCatchBlockAttr(1, List.of(second, first), List.of(call));
		assertThat(isProtected(call, reversed)).isTrue();
	}

	@Test
	void matchesNestedHandlersRegardlessOfOffsetOrder() {
		ExceptionHandler innerFirst = ExceptionHandler.build(null, 3, null);
		ExceptionHandler innerSecond = ExceptionHandler.build(null, 1, null);
		ExceptionHandler outerHandler = ExceptionHandler.build(null, 2, null);
		BlockNode call = terminal(InsnType.INVOKE);
		TryCatchBlockAttr outer = new TryCatchBlockAttr(0, List.of(outerHandler), List.of());
		TryCatchBlockAttr inner = new TryCatchBlockAttr(1, List.of(innerFirst, innerSecond), List.of(call));
		inner.setOuterTryBlock(outer);
		call.getInstructions().get(0).addAttr(CatchAttr.build(
				new ArrayList<>(List.of(innerFirst, outerHandler, innerSecond))));
		assertThat(isProtected(call, inner)).isTrue();
		call.getInstructions().get(0).addAttr(CatchAttr.build(new ArrayList<>(List.of(innerFirst, outerHandler))));
		assertThat(isProtected(call, inner)).isFalse();
	}

	@Test
	void rejectsNestedTryRegionToAvoidCatchingHandlerRethrow() {
		BlockNode call = terminal(InsnType.INVOKE);
		TryCatchBlockAttr tb = new TryCatchBlockAttr(0, List.of(), List.of(call));
		assertThat(isProtected(wrap(null, tb, call), tb)).isFalse();
	}

	@Test
	void checksThrowingConditionBlocks() {
		Region body = new Region(null);
		IfRegion branch = new IfRegion(body);
		BlockNode condition = terminal(InsnType.INVOKE);
		branch.updateCondition(null, List.of(condition));
		body.add(branch);
		assertThat(isProtected(body, new TryCatchBlockAttr(0, List.of(), List.of()))).isFalse();
		assertThat(isProtected(body, new TryCatchBlockAttr(0, List.of(), List.of(condition)))).isTrue();
	}

	private static boolean isProtected(IContainer container, TryCatchBlockAttr tb) {
		return ProcessTryCatchRegions.hasOnlyProtectedThrowingCode(container, tb, new IdentityHashMap<>());
	}

	@Test
	void syntheticContinueCannotThrowIntoCatch() {
		TryCatchBlockAttr tb = new TryCatchBlockAttr(0, List.of(), List.of());
		BlockNode transfer = terminal(InsnType.CONTINUE);
		transfer.getInstructions().get(0).add(AFlag.SYNTHETIC);
		assertThat(isProtected(transfer, tb)).isTrue();
	}

	@Test
	void plansSplitBranchesWithUnprotectedConditionAndPrefix() {
		BlockNode protectedCall = terminal(InsnType.INVOKE);
		TryCatchBlockAttr tb = new TryCatchBlockAttr(0, List.of(), List.of(protectedCall));
		IfRegion branch = new IfRegion(null);
		branch.updateCondition(null, List.of(terminal(InsnType.INVOKE)));
		Region thenRegion = new Region(branch);
		thenRegion.add(terminal(InsnType.INVOKE));
		thenRegion.add(protectedCall);
		branch.setThenRegion(thenRegion);
		assertThat(ProcessTryCatchRegions.canSplitProtectedScope(branch, tb)).isTrue();
	}

	@Test
	void leavesProtectedConditionWithUnprotectedCleanupToExistingReconstruction() {
		BlockNode protectedCondition = terminal(InsnType.INVOKE);
		TryCatchBlockAttr tb = new TryCatchBlockAttr(0, List.of(), List.of(protectedCondition));
		IfRegion branch = new IfRegion(null);
		branch.updateCondition(null, List.of(protectedCondition));
		branch.setThenRegion(terminal(InsnType.INVOKE));
		assertThat(ProcessTryCatchRegions.canSplitProtectedScope(branch, tb)).isFalse();
	}

	@Test
	void rejectsPartiallyProtectedSingleInstructionBlock() {
		BlockNode mixed = terminal(InsnType.INVOKE);
		TryCatchBlockAttr tb = handlerWithBody(new Region(null));
		mixed.getInstructions().get(0).addAttr(CatchAttr.build(new ArrayList<>(tb.getHandlers())));
		mixed.getInstructions().add(new InsnNode(InsnType.INVOKE, 0));
		assertThat(ProcessTryCatchRegions.canSplitProtectedScope(mixed, tb)).isFalse();
	}

	@Test
	void excludesEarlierLoopSiblingAndUnprotectedTailFromExactWrapper() {
		BlockNode prefix = terminal(InsnType.INVOKE);
		BlockNode first = terminal(InsnType.INVOKE);
		BlockNode second = terminal(InsnType.INVOKE);
		BlockNode tail = terminal(InsnType.INVOKE);
		List<BlockNode> blocks = List.of(prefix, first, second, tail);
		for (int i = 0; i < blocks.size(); i++) {
			connect(blocks.get(i), blocks.get((i + 1) % blocks.size()));
			blocks.get(i).updateCleanSuccessors();
		}
		Region root = new Region(null);
		blocks.forEach(root::add);
		TryCatchBlockAttr tb = new TryCatchBlockAttr(0, List.of(), List.of(first, second));

		assertThat(BlockUtils.isPathExists(first, prefix)).isTrue();
		assertThat(ProcessTryCatchRegions.wrapBlocks(root, tb, first, true)).isTrue();
		assertThat(root.getSubBlocks()).hasSize(3);
		assertThat(root.getSubBlocks().get(0)).isSameAs(prefix);
		assertThat(root.getSubBlocks().get(2)).isSameAs(tail);
		TryCatchRegion wrapper = (TryCatchRegion) root.getSubBlocks().get(1);
		assertThat(((IRegion) wrapper.getTryRegion()).getSubBlocks()).containsExactly(first, second);
	}

	@Test
	void sharedCatchExitMayPassOnlyThroughEmptyLinearJoins() {
		BlockNode caseBlock = terminal(InsnType.INVOKE);
		BlockNode emptyJoin = new BlockNode(BLOCK_IDS.incrementAndGet(), 0, 0);
		BlockNode sharedExit = terminal(InsnType.RETURN);
		BlockNode methodExit = new BlockNode(BLOCK_IDS.incrementAndGet(), 0, 0);
		BlockNode handlerBlock = terminal(InsnType.INVOKE);
		connect(caseBlock, emptyJoin);
		connect(emptyJoin, sharedExit);
		connect(handlerBlock, sharedExit);
		connect(sharedExit, methodExit);
		for (BlockNode block : List.of(caseBlock, emptyJoin, handlerBlock)) {
			block.updateCleanSuccessors();
		}
		Region caseRegion = new Region(null);
		caseRegion.add(caseBlock);
		Region handlerRegion = new Region(null);
		handlerRegion.add(handlerBlock);
		TryCatchBlockAttr tb = handlerWithBody(handlerRegion);
		assertThat(handlerBlock.getCleanSuccessors()).containsExactly(sharedExit);
		assertThat(RestoreExceptionHandlerLoopExits.collectBoundaryTargets(java.util.Set.of(handlerBlock)))
				.containsExactly(sharedExit);
		assertThat(caseBlock.getCleanSuccessors()).containsExactly(emptyJoin);
		assertThat(ProcessTryCatchRegions.hasSharedCatchExit(caseRegion, tb)).isTrue();

		emptyJoin.getInstructions().add(new InsnNode(InsnType.INVOKE, 0));
		assertThat(ProcessTryCatchRegions.hasSharedCatchExit(caseRegion, tb)).isFalse();
		emptyJoin.getInstructions().clear();
		connect(emptyJoin, methodExit);
		emptyJoin.updateCleanSuccessors();
		assertThat(ProcessTryCatchRegions.hasSharedCatchExit(caseRegion, tb)).isFalse();
	}

	@Test
	void recognizesCatchJoinBeforeNormalCaseExit() {
		BlockNode caseBlock = terminal(InsnType.INVOKE);
		BlockNode handlerBlock = terminal(InsnType.INVOKE);
		BlockNode emptyJoin = new BlockNode(BLOCK_IDS.incrementAndGet(), 0, 0);
		BlockNode sharedExit = terminal(InsnType.RETURN);
		BlockNode methodExit = new BlockNode(BLOCK_IDS.incrementAndGet(), 0, 0);
		connect(caseBlock, sharedExit);
		connect(handlerBlock, emptyJoin);
		connect(emptyJoin, sharedExit);
		connect(sharedExit, methodExit);
		for (BlockNode block : List.of(caseBlock, handlerBlock, emptyJoin, sharedExit)) {
			block.updateCleanSuccessors();
		}
		Region caseRegion = new Region(null);
		caseRegion.add(caseBlock);
		Region handlerRegion = new Region(null);
		handlerRegion.add(handlerBlock);
		TryCatchBlockAttr tb = handlerWithBody(handlerRegion);
		assertThat(ProcessTryCatchRegions.hasSharedCatchExit(caseRegion, tb)).isTrue();

		caseRegion.add(emptyJoin);
		caseRegion.add(sharedExit);
		assertThat(ProcessTryCatchRegions.hasTerminalCatchJoin(caseRegion, tb)).isTrue();
	}

	@Test
	void splitsSharedSwitchCatchWithTerminalCaseAndConditionalRethrow() {
		BlockNode protectedCall = terminal(InsnType.INVOKE);
		BlockNode suspendedReturn = terminal(InsnType.RETURN);
		BlockNode sharedReturn = terminal(InsnType.RETURN);
		BlockNode methodExit = new BlockNode(BLOCK_IDS.incrementAndGet(), 0, 0);
		connect(protectedCall, sharedReturn);
		connect(suspendedReturn, methodExit);
		protectedCall.updateCleanSuccessors();
		suspendedReturn.updateCleanSuccessors();
		Region caseRegion = new Region(null);
		caseRegion.add(protectedCall);
		caseRegion.add(suspendedReturn);

		BlockNode handlerEntry = terminal(InsnType.INVOKE);
		BlockNode rethrow = terminal(InsnType.THROW);
		connect(handlerEntry, sharedReturn);
		handlerEntry.updateCleanSuccessors();
		Region handlerRegion = new Region(null);
		handlerRegion.add(handlerEntry);
		IfRegion condition = new IfRegion(handlerRegion);
		condition.setThenRegion(rethrow);
		handlerRegion.add(condition);
		handlerRegion.add(new InsnContainer(BlockUtils.getLastInsn(sharedReturn)));
		TryCatchBlockAttr tb = handlerWithBody(handlerRegion);

		assertThat(ProcessTryCatchRegions.hasSharedCatchExit(caseRegion, tb)).isFalse();
		assertThat(ProcessTryCatchRegions.hasSharedSwitchCatchExit(caseRegion, tb)).isTrue();
		BlockNode secondEntry = terminal(InsnType.INVOKE);
		connect(secondEntry, sharedReturn);
		secondEntry.updateCleanSuccessors();
		Region secondRegion = new Region(null);
		secondRegion.add(secondEntry);
		ExceptionHandler secondHandler = ExceptionHandler.build(null, 0, null);
		secondHandler.setHandlerRegion(secondRegion);
		TryCatchBlockAttr twoHandlers = new TryCatchBlockAttr(0,
				List.of(tb.getHandlers().get(0), secondHandler), List.of());
		assertThat(ProcessTryCatchRegions.hasSharedSwitchCatchExit(caseRegion, twoHandlers)).isTrue();
		connect(secondEntry, methodExit);
		secondEntry.updateCleanSuccessors();
		assertThat(ProcessTryCatchRegions.hasSharedSwitchCatchExit(caseRegion, twoHandlers)).isFalse();

		handlerRegion.getSubBlocks().remove(handlerRegion.getSubBlocks().size() - 1);
		handlerRegion.add(new InsnContainer(new InsnNode(InsnType.RETURN, 0)));
		assertThat(ProcessTryCatchRegions.hasSharedSwitchCatchExit(caseRegion, tb)).isFalse();
	}

	@Test
	void splitProtectedPrefixDoesNotReparentMixedBranch() {
		BlockNode first = terminal(InsnType.INVOKE);
		BlockNode second = terminal(InsnType.INVOKE);
		BlockNode unprotected = terminal(InsnType.INVOKE);
		connect(first, second);
		connect(second, unprotected);
		first.updateCleanSuccessors();
		second.updateCleanSuccessors();
		unprotected.updateCleanSuccessors();
		Region root = new Region(null);
		IfRegion branch = new IfRegion(root);
		Region body = new Region(branch);
		body.add(second);
		body.add(unprotected);
		branch.setThenRegion(body);
		root.add(first);
		root.add(branch);
		TryCatchBlockAttr tb = new TryCatchBlockAttr(0, List.of(), List.of(first, second));

		assertThat(ProcessTryCatchRegions.wrapBlocks(root, tb, first, true)).isTrue();
		assertThat(root.getSubBlocks()).hasSize(2);
		assertThat(root.getSubBlocks().get(0)).isInstanceOf(TryCatchRegion.class);
		TryCatchRegion wrapped = (TryCatchRegion) root.getSubBlocks().get(0);
		assertThat(((Region) wrapped.getTryRegion()).getSubBlocks()).containsExactly(first);
		assertThat(root.getSubBlocks().get(1)).isSameAs(branch);
		assertThat(branch.getParent()).isSameAs(root);
		assertThat(body.getParent()).isSameAs(branch);
	}

	@Test
	void missingAnchorDoesNotSplitMixedHandlerContinuation() {
		BlockNode protectedExit = terminal(InsnType.RETURN);
		Region root = new Region(null);
		IfRegion branch = new IfRegion(root);
		Region protectedBody = new Region(branch);
		protectedBody.add(protectedExit);
		Region continuation = new Region(branch);
		continuation.add(terminal(InsnType.RETURN));
		branch.setThenRegion(protectedBody);
		branch.setElseRegion(continuation);
		root.add(branch);

		Region handlerBody = new Region(null);
		IfRegion handlerBranch = new IfRegion(handlerBody);
		handlerBranch.setThenRegion(terminal(InsnType.THROW));
		handlerBranch.setElseRegion(terminal(InsnType.RETURN));
		handlerBody.add(handlerBranch);
		ExceptionHandler handler = ExceptionHandler.build(null, 0, null);
		handler.setHandlerRegion(handlerBody);
		TryCatchBlockAttr tb = new TryCatchBlockAttr(0, List.of(handler), List.of(protectedExit));
		assertThat(ProcessTryCatchRegions.hasMixedHandlerExits(tb)).isTrue();

		assertThat(ProcessTryCatchRegions.wrapBlocks(root, tb, terminal(InsnType.INVOKE), true)).isFalse();
		assertThat(root.getSubBlocks()).containsExactly(branch);
		assertThat(branch.getElseRegion()).isSameAs(continuation);
		assertThat(continuation.getParent()).isSameAs(branch);
	}

	@Test
	void doesNotCopyConditionalRethrowWithSharedRetryContinuation() {
		BlockNode rethrow = new BlockNode(1, 0, 0);
		rethrow.getInstructions().add(new InsnNode(InsnType.THROW, 0));
		Region body = new Region(null);
		IfRegion branch = new IfRegion(body);
		branch.setThenRegion(rethrow);
		body.add(branch);
		// The other branch falls through to a shared retry continuation. Copying
		// this handler before a successful-response consumer would change its target.
		ExceptionHandler handler = ExceptionHandler.build(null, 0, null);
		handler.addBlock(rethrow);
		handler.setHandlerRegion(body);
		TryCatchBlockAttr tryBlock = new TryCatchBlockAttr(0, List.of(handler), List.of());

		assertThat(ProcessTryCatchRegions.hasRepeatableRethrowHandlers(tryBlock)).isFalse();
		// Moving the rethrow onto edges hides its original instruction. That must
		// not hide an uncovered protected fragment from the coverage diagnostic.
		rethrow.getInstructions().get(0).add(AFlag.DONT_GENERATE);
		assertThat(ProcessTryCatchRegions.hasRethrowWithoutReturn(tryBlock)).isTrue();
	}

	@Test
	void acceptsSharedTerminalThrowOutsideHandlerBlockInventory() {
		BlockNode terminal = terminal(InsnType.THROW);
		Region body = new Region(null);
		body.add(terminal);
		assertThat(ProcessTryCatchRegions.hasRepeatableRethrowHandlers(handlerWithBody(body))).isTrue();
	}

	@Test
	void acceptsBothBranchesThrowingAtSharedTerminal() {
		Region body = new Region(null);
		IfRegion branch = new IfRegion(body);
		BlockNode shared = terminal(InsnType.THROW);
		branch.setThenRegion(shared);
		branch.setElseRegion(shared);
		body.add(branch);
		assertThat(ProcessTryCatchRegions.hasRepeatableRethrowHandlers(handlerWithBody(body))).isTrue();
	}

	@Test
	void rejectsReturnAndSuppressedThrow() {
		Region body = new Region(null);
		IfRegion branch = new IfRegion(body);
		branch.setThenRegion(terminal(InsnType.THROW));
		branch.setElseRegion(terminal(InsnType.RETURN));
		body.add(branch);
		assertThat(ProcessTryCatchRegions.hasRepeatableRethrowHandlers(handlerWithBody(body))).isFalse();

		BlockNode suppressed = terminal(InsnType.THROW);
		suppressed.getInstructions().get(0).add(AFlag.DONT_GENERATE);
		Region suppressedBody = new Region(null);
		suppressedBody.add(suppressed);
		assertThat(ProcessTryCatchRegions.hasRepeatableRethrowHandlers(handlerWithBody(suppressedBody))).isFalse();
	}

	@Test
	void rejectsCyclicHandlerRegion() {
		Region body = new Region(null);
		body.add(body);
		assertThat(ProcessTryCatchRegions.hasRepeatableRethrowHandlers(handlerWithBody(body))).isFalse();
	}

	@Test
	void rejectsEarlyReturnEvenWhenTheLastStatementThrows() {
		Region body = new Region(null);
		IfRegion branch = new IfRegion(body);
		branch.setThenRegion(terminal(InsnType.RETURN));
		body.add(branch);
		body.add(terminal(InsnType.THROW));
		assertThat(ProcessTryCatchRegions.hasRepeatableRethrowHandlers(handlerWithBody(body))).isFalse();
	}

	@Test
	void acceptsConditionalThrowFollowedByUnconditionalThrowAndEmptyTail() {
		Region body = new Region(null);
		IfRegion branch = new IfRegion(body);
		branch.setThenRegion(terminal(InsnType.THROW));
		body.add(branch);
		body.add(terminal(InsnType.THROW));
		body.add(new Region(body));
		assertThat(ProcessTryCatchRegions.hasRepeatableRethrowHandlers(handlerWithBody(body))).isTrue();
	}

	private static BlockNode terminal(InsnType type) {
		int id = BLOCK_IDS.incrementAndGet();
		BlockNode block = new BlockNode(id, id, 0);
		block.getInstructions().add(new InsnNode(type, 0));
		return block;
	}

	private static TryCatchBlockAttr handlerWithBody(Region body) {
		ExceptionHandler handler = ExceptionHandler.build(null, 0, null);
		handler.setHandlerRegion(body);
		return new TryCatchBlockAttr(0, List.of(handler), List.of());
	}

	@Test
	void detectsProtectedCopiesOutsideMatchingTryRegion() {
		BlockNode protectedBlock = new BlockNode(1, 0, 0);
		TryCatchBlockAttr tryBlock = new TryCatchBlockAttr(0, List.of(), List.of(protectedBlock));

		Region root = new Region(null);
		root.add(wrap(root, tryBlock, protectedBlock));
		Region unwrappedCopy = new Region(root);
		unwrappedCopy.add(protectedBlock);
		root.add(unwrappedCopy);

		assertThat(ProcessTryCatchRegions.containsUnwrappedProtectedOccurrence(root, tryBlock)).isTrue();
		assertThat(ProcessTryCatchRegions.countUnwrappedProtectedOccurrences(root, tryBlock)).isOne();

		root.replaceSubBlock(unwrappedCopy, wrap(root, tryBlock, protectedBlock));

		assertThat(ProcessTryCatchRegions.containsUnwrappedProtectedOccurrence(root, tryBlock)).isFalse();
		assertThat(ProcessTryCatchRegions.countUnwrappedProtectedOccurrences(root, tryBlock)).isZero();
	}

	@Test
	void countsSharedProtectedRegionAtEachEmissionSite() {
		BlockNode protectedBlock = terminal(InsnType.INVOKE);
		TryCatchBlockAttr tryBlock = new TryCatchBlockAttr(0, List.of(), List.of(protectedBlock));
		Region root = new Region(null);
		Region shared = new Region(root);
		shared.add(protectedBlock);
		root.add(shared);
		root.add(shared);

		assertThat(ProcessTryCatchRegions.countUnwrappedProtectedOccurrences(root, tryBlock)).isEqualTo(2);
		shared.add(shared); // Guard cycles without suppressing the second emission site.
		assertThat(ProcessTryCatchRegions.countUnwrappedProtectedOccurrences(root, tryBlock)).isEqualTo(2);
	}

	@Test
	void scansProtectedBlocksInCyclicRegionWithoutRecursion() {
		BlockNode protectedBlock = terminal(InsnType.INVOKE);
		TryCatchBlockAttr tryBlock = new TryCatchBlockAttr(0, List.of(), List.of(protectedBlock));
		Region cyclic = new Region(null);
		cyclic.add(cyclic);
		cyclic.add(protectedBlock);

		assertThat(ProcessTryCatchRegions.containsProtectedBlock(cyclic, tryBlock)).isTrue();
		assertThat(ProcessTryCatchRegions.containsProtectedBlock(cyclic,
				new TryCatchBlockAttr(1, List.of(), List.of()))).isFalse();
	}

	@Test
	void rejectsSplitProtectedScopeWithRegionCycle() {
		BlockNode protectedBlock = terminal(InsnType.INVOKE);
		TryCatchBlockAttr tryBlock = new TryCatchBlockAttr(0, List.of(), List.of(protectedBlock));
		Region cyclic = new Region(null);
		cyclic.add(protectedBlock);
		cyclic.add(cyclic);

		assertThat(ProcessTryCatchRegions.canSplitProtectedScope(cyclic, tryBlock)).isFalse();
	}

	@Test
	void stopsTrailingScopeTraversalAtRegionCycles() {
		Region cyclic = new Region(null);
		cyclic.add(cyclic);
		ProcessTryCatchRegions.splitTrailingUnprotectedRegions(
				cyclic, new TryCatchBlockAttr(0, List.of(), List.of()));
		assertThat(cyclic.getSubBlocks()).containsExactly(cyclic);
	}

	@Test
	void doesNotHideProtectedCopiesUnderUnrelatedTryRegion() {
		BlockNode protectedBlock = new BlockNode(1, 0, 0);
		TryCatchBlockAttr expectedTry = new TryCatchBlockAttr(0, List.of(), List.of(protectedBlock));
		TryCatchBlockAttr otherTry = new TryCatchBlockAttr(1, List.of(), List.of(protectedBlock));

		Region root = new Region(null);
		root.add(wrap(root, otherTry, protectedBlock));

		assertThat(ProcessTryCatchRegions.containsUnwrappedProtectedOccurrence(root, expectedTry)).isTrue();
	}

	private static TryCatchRegion wrap(
			IRegion parent, TryCatchBlockAttr tryBlock, IContainer protectedContainer) {
		Region tryRegion = new Region(null);
		tryRegion.add(protectedContainer);
		TryCatchRegion tryCatchRegion = new TryCatchRegion(parent, tryRegion);
		tryRegion.setParent(tryCatchRegion);
		tryCatchRegion.setTryCatchBlock(tryBlock);
		return tryCatchRegion;
	}
}
