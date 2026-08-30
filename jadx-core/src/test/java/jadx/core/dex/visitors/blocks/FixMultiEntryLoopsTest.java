package jadx.core.dex.visitors.blocks;

import java.util.List;

import org.junit.jupiter.api.Test;

import jadx.api.JadxArgs;
import jadx.core.dex.attributes.AFlag;
import jadx.core.dex.attributes.nodes.SpecialEdgeAttr;
import jadx.core.dex.attributes.nodes.SpecialEdgeAttr.SpecialEdgeType;
import jadx.core.dex.info.ClassInfo;
import jadx.core.dex.info.FieldInfo;
import jadx.core.dex.info.MethodInfo;
import jadx.core.dex.instructions.IfNode;
import jadx.core.dex.instructions.IfOp;
import jadx.core.dex.instructions.IndexInsnNode;
import jadx.core.dex.instructions.InsnType;
import jadx.core.dex.instructions.InvokeNode;
import jadx.core.dex.instructions.InvokeType;
import jadx.core.dex.instructions.SwitchInsn;
import jadx.core.dex.instructions.args.ArgType;
import jadx.core.dex.instructions.args.InsnArg;
import jadx.core.dex.instructions.args.RegisterArg;
import jadx.core.dex.nodes.BlockNode;
import jadx.core.dex.nodes.InsnNode;
import jadx.core.dex.nodes.RootNode;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

class FixMultiEntryLoopsTest {
	@Test
	void failedProcessResultStopsAllFollowUpTransforms() throws Exception {
		Class<?> resultClass = Class.forName(FixMultiEntryLoops.class.getName() + "$ProcessResult");
		var field = resultClass.getDeclaredField("FAILED_MAY_HAVE_CHANGED");
		field.setAccessible(true);
		FixMultiEntryLoops.ProcessResult result = (FixMultiEntryLoops.ProcessResult) field.get(null);

		assertThat(result.isFailed()).isTrue();
		assertThat(result.isChanged()).isFalse();
		assertThat(result.processAdditionalCoroutinePasses()).isFalse();
	}

	@Test
	void testRejectSharedDirectPathFromSingleRewriteFastPath() {
		BlockNode suspendCheck = block(0);
		BlockNode resumeEntry = block(1);
		BlockNode directPath = block(2);
		connect(suspendCheck, directPath);

		assertThat(FixMultiEntryLoops.hasExclusivePredecessor(
				directPath, suspendCheck)).isTrue();

		connect(resumeEntry, directPath);

		assertThat(FixMultiEntryLoops.hasExclusivePredecessor(
				directPath, suspendCheck)).isFalse();
	}

	@Test
	void testIgnoreExceptionHandlerBackEdge() {
		BlockNode header = block(0);
		BlockNode loopEnd = block(1);
		header.getPredecessors().add(block(2));
		header.getPredecessors().add(loopEnd);
		connect(header, loopEnd);
		SpecialEdgeAttr backEdge = new SpecialEdgeAttr(SpecialEdgeType.BACK_EDGE, loopEnd, header);

		assertThat(FixMultiEntryLoops.isMultiEntryLoop(backEdge)).isTrue();
		loopEnd.add(AFlag.EXC_BOTTOM_SPLITTER);
		assertThat(FixMultiEntryLoops.isMultiEntryLoop(backEdge)).isFalse();
	}

	@Test
	void testDetectCycleThroughExceptionSplitterForStructuralSplit() {
		BlockNode header = block(0);
		BlockNode loopEnd = block(1);
		BlockNode exceptionSplitter = block(2);
		BlockNode handler = block(3);
		header.getPredecessors().add(block(4));
		header.getPredecessors().add(loopEnd);
		exceptionSplitter.add(AFlag.EXC_BOTTOM_SPLITTER);
		connect(header, exceptionSplitter);
		connect(exceptionSplitter, handler);
		connect(handler, loopEnd);
		SpecialEdgeAttr backEdge = new SpecialEdgeAttr(SpecialEdgeType.BACK_EDGE, loopEnd, header);

		assertThat(FixMultiEntryLoops.isMultiEntryLoop(backEdge)).isTrue();
		assertThat(FixMultiEntryLoops.isExceptionOnlyCycle(backEdge)).isTrue();

		BlockNode normalPath = block(5);
		connect(header, normalPath);
		connect(normalPath, loopEnd);
		assertThat(FixMultiEntryLoops.isMultiEntryLoop(backEdge)).isTrue();
		assertThat(FixMultiEntryLoops.isExceptionOnlyCycle(backEdge)).isFalse();
	}

	@Test
	void testDetectPureCoroutineBranchJoinPath() {
		BlockNode entry = block(0);
		BlockNode path = block(1);
		BlockNode join = block(2);
		BlockNode thenBlock = block(3);
		BlockNode elseBlock = block(4);
		path.getInstructions().add(new InsnNode(InsnType.MOVE, 0));
		InsnArg value = InsnArg.reg(0, ArgType.INT);
		InsnArg zero = InsnArg.lit(0, ArgType.INT);
		join.getInstructions().add(new IfNode(IfOp.EQ, -1, value, zero));
		connect(entry, join);
		connect(path, join);
		connect(join, thenBlock);
		connect(join, elseBlock);
		SpecialEdgeAttr backEdge = new SpecialEdgeAttr(SpecialEdgeType.BACK_EDGE, path, join);

		assertThat(FixMultiEntryLoops.isPureCoroutineJoinPath(backEdge)).isTrue();
	}

	@Test
	void testRejectEffectfulCoroutineJoinPath() {
		BlockNode entry = block(0);
		BlockNode path = block(1);
		BlockNode join = block(2);
		BlockNode exit = block(3);
		path.getInstructions().add(new InsnNode(InsnType.CONST, 0));
		join.getInstructions().add(new InsnNode(InsnType.INVOKE, 0));
		connect(entry, join);
		connect(path, join);
		connect(join, exit);
		SpecialEdgeAttr backEdge = new SpecialEdgeAttr(SpecialEdgeType.BACK_EDGE, path, join);

		assertThat(FixMultiEntryLoops.isPureCoroutineJoinPath(backEdge)).isFalse();
	}

	@Test
	void testDetectPureCoroutineConstantJoinPath() {
		BlockNode entry = block(0);
		BlockNode path = block(1);
		BlockNode join = block(2);
		BlockNode exit = block(3);
		path.getInstructions().add(new InsnNode(InsnType.MOVE, 0));
		join.getInstructions().add(new InsnNode(InsnType.CONST, 0));
		connect(entry, join);
		connect(path, join);
		connect(join, exit);
		SpecialEdgeAttr backEdge = new SpecialEdgeAttr(SpecialEdgeType.BACK_EDGE, path, join);

		assertThat(FixMultiEntryLoops.isPureCoroutineJoinPath(backEdge)).isTrue();
	}

	@Test
	void testDetectBoundedCoroutineMoveJoinPath() {
		BlockNode entry = block(0);
		BlockNode path = block(1);
		BlockNode join = block(2);
		BlockNode exit = block(3);
		path.getInstructions().add(new InsnNode(InsnType.MOVE, 0));
		join.getInstructions().add(new InsnNode(InsnType.MOVE, 0));
		join.getInstructions().add(new InsnNode(InsnType.MOVE, 0));
		join.getInstructions().add(new InsnNode(InsnType.MOVE, 0));
		join.getInstructions().add(new InsnNode(InsnType.CONST, 0));
		connect(entry, join);
		connect(path, join);
		connect(join, exit);
		SpecialEdgeAttr backEdge = new SpecialEdgeAttr(SpecialEdgeType.BACK_EDGE, path, join);

		assertThat(FixMultiEntryLoops.isPureConcreteCoroutineJoinPath(backEdge)).isTrue();

		join.getInstructions().add(new InsnNode(InsnType.MOVE, 0));
		assertThat(FixMultiEntryLoops.isPureConcreteCoroutineJoinPath(backEdge)).isFalse();
	}

	@Test
	void testDetectPureCoroutineCheckCastProjectionBlock() {
		BlockNode projection = block(0);
		IndexInsnNode cast = new IndexInsnNode(
				InsnType.CHECK_CAST, ArgType.object("test.Result"), 1);
		cast.setResult(InsnArg.reg(1, ArgType.object("test.Result")));
		cast.addArg(InsnArg.reg(0, ArgType.OBJECT));
		projection.getInstructions().add(cast);
		IndexInsnNode instanceOf = new IndexInsnNode(
				InsnType.INSTANCE_OF, ArgType.object("test.Error"), 1);
		instanceOf.setResult(InsnArg.reg(2, ArgType.BOOLEAN));
		instanceOf.addArg(InsnArg.reg(1, ArgType.object("test.Result")));
		projection.getInstructions().add(instanceOf);

		assertThat(FixMultiEntryLoops.isPureCoroutineProjectionBlock(
				projection, 8)).isTrue();

		instanceOf.setArg(0, InsnArg.reg(3, ArgType.object("test.Result")));
		assertThat(FixMultiEntryLoops.isPureCoroutineProjectionBlock(
				projection, 8)).isFalse();
	}

	@Test
	void testDetectCoroutineResumeMoveBridgeAfterTryTail() {
		RootNode root = new RootNode(new JadxArgs());
		ArgType continuationType = ArgType.object("test.ScrollContinuation");
		FieldInfo resultField = FieldInfo.from(
				root,
				ClassInfo.fromType(root, continuationType),
				"result",
				ArgType.OBJECT);
		BlockNode restore = block(0);
		BlockNode trySplitter = block(1);
		BlockNode tryTail = block(2);
		BlockNode resumeBridge = block(3);
		connect(restore, trySplitter);
		connect(trySplitter, tryTail);
		connect(tryTail, resumeBridge);
		trySplitter.add(AFlag.EXC_TOP_SPLITTER);

		IndexInsnNode resultGet = new IndexInsnNode(
				InsnType.IGET, resultField, 1);
		resultGet.setResult(InsnArg.reg(1, ArgType.OBJECT));
		resultGet.addArg(InsnArg.reg(0, continuationType));
		restore.getInstructions().add(resultGet);
		addInvoke(root, tryTail, "throwOnFailure");
		addMove(resumeBridge, 2, 3, ArgType.OBJECT);

		assertThat(FixMultiEntryLoops
				.isCoroutineResumeMoveBridgeAfterTryTail(resumeBridge))
						.isTrue();

		for (int i = 0; i < 11; i++) {
			addMove(resumeBridge, 4 + i, 20 + i, ArgType.OBJECT);
		}
		assertThat(FixMultiEntryLoops
				.isCoroutineResumeMoveBridgeAfterTryTail(resumeBridge))
						.isTrue();

		addMove(resumeBridge, 40, 41, ArgType.OBJECT);
		assertThat(FixMultiEntryLoops
				.isCoroutineResumeMoveBridgeAfterTryTail(resumeBridge))
						.isFalse();
		resumeBridge.getInstructions().remove(
				resumeBridge.getInstructions().size() - 1);
		resumeBridge.getInstructions().remove(
				resumeBridge.getInstructions().size() - 1);
		resumeBridge.getInstructions().add(
				new InsnNode(InsnType.CONST, 0));
		assertThat(FixMultiEntryLoops
				.isCoroutineResumeMoveBridgeAfterTryTail(resumeBridge))
						.isFalse();
	}

	@Test
	void testFindLabelPutAcrossLinearTryEntry() {
		RootNode root = new RootNode(new JadxArgs());
		ArgType continuationType = ArgType.object("test.ScrollContinuation");
		FieldInfo labelField = FieldInfo.from(
				root,
				ClassInfo.fromType(root, continuationType),
				"label",
				ArgType.INT);
		BlockNode suspendSetup = block(0);
		BlockNode moveBridge = block(1);
		BlockNode trySplitter = block(2);
		BlockNode suspendCall = block(3);
		connect(suspendSetup, moveBridge);
		connect(moveBridge, trySplitter);
		connect(trySplitter, suspendCall);
		trySplitter.add(AFlag.EXC_TOP_SPLITTER);
		addMove(moveBridge, 1, 2, continuationType);

		IndexInsnNode labelPut = new IndexInsnNode(
				InsnType.IPUT, labelField, 2);
		labelPut.addArg(InsnArg.lit(1, ArgType.INT));
		labelPut.addArg(InsnArg.reg(0, continuationType));
		suspendSetup.getInstructions().add(labelPut);

		assertThat(FixMultiEntryLoops
				.findLabelPutOnLinearPredecessorPath(suspendCall, 4))
						.isSameAs(labelPut);
		assertThat(FixMultiEntryLoops
				.findLabelPutOnLinearPredecessorPath(suspendCall, 3))
						.isNull();
	}

	@Test
	void testDetectCoroutineDirectBridgeMoveJoinAcrossTryEntries() {
		RootNode root = new RootNode(new JadxArgs());
		ArgType continuationType = ArgType.object("test.ScrollContinuation");
		ClassInfo continuationClass = ClassInfo.fromType(
				root, continuationType);
		FieldInfo labelField = FieldInfo.from(
				root, continuationClass, "label", ArgType.INT);
		FieldInfo resultField = FieldInfo.from(
				root, continuationClass, "result", ArgType.OBJECT);
		BlockNode moveJoin = block(0);
		BlockNode suspendSetup = block(1);
		BlockNode setupMove = block(2);
		BlockNode callTrySplitter = block(3);
		BlockNode suspendCall = block(4);
		BlockNode suspendCheck = block(5);
		BlockNode directBridge = block(6);
		BlockNode suspendedReturn = block(7);
		BlockNode resumeRestore = block(8);
		BlockNode resumeTrySplitter = block(9);
		BlockNode resumeTryTail = block(10);
		BlockNode resumeBridge = block(11);
		connect(moveJoin, suspendSetup);
		connect(suspendSetup, setupMove);
		connect(setupMove, callTrySplitter);
		connect(callTrySplitter, suspendCall);
		connect(suspendCall, suspendCheck);
		connect(suspendCheck, directBridge);
		connect(suspendCheck, suspendedReturn);
		connect(directBridge, moveJoin);
		connect(resumeRestore, resumeTrySplitter);
		connect(resumeTrySplitter, resumeTryTail);
		connect(resumeTryTail, resumeBridge);
		connect(resumeBridge, moveJoin);
		callTrySplitter.add(AFlag.EXC_TOP_SPLITTER);
		resumeTrySplitter.add(AFlag.EXC_TOP_SPLITTER);

		addMove(moveJoin, 0, 1, ArgType.INT);
		addMove(setupMove, 2, 3, continuationType);
		for (int i = 0; i < 7; i++) {
			addMove(directBridge, 10 + i, 20 + i, ArgType.OBJECT);
		}
		for (int i = 0; i < 9; i++) {
			addMove(resumeBridge, 30 + i, 50 + i, ArgType.OBJECT);
		}

		IndexInsnNode labelPut = new IndexInsnNode(
				InsnType.IPUT, labelField, 2);
		labelPut.addArg(InsnArg.lit(1, ArgType.INT));
		labelPut.addArg(InsnArg.reg(2, continuationType));
		suspendSetup.getInstructions().add(labelPut);
		MethodInfo suspendInfo = MethodInfo.fromDetails(
				root,
				ClassInfo.fromName(root, "test.Animation"),
				"animateTo",
				List.of(continuationType),
				ArgType.OBJECT);
		InvokeNode suspendInvoke = new InvokeNode(
				suspendInfo, InvokeType.STATIC, 1);
		suspendInvoke.addArg(InsnArg.reg(2, continuationType));
		RegisterArg suspendResult = InsnArg.reg(4, ArgType.OBJECT);
		suspendInvoke.setResult(suspendResult);
		suspendCall.getInstructions().add(suspendInvoke);
		IfNode suspended = new IfNode(
				IfOp.NE,
				directBridge.getStartOffset(),
				suspendResult.duplicate(),
				InsnArg.reg(5, ArgType.OBJECT));
		suspendCheck.getInstructions().add(suspended);
		suspended.initBlocks(suspendCheck);
		suspendedReturn.getInstructions().add(
				new InsnNode(InsnType.RETURN, 0));
		suspendedReturn.add(AFlag.RETURN);

		IndexInsnNode resultGet = new IndexInsnNode(
				InsnType.IGET, resultField, 1);
		resultGet.setResult(InsnArg.reg(6, ArgType.OBJECT));
		resultGet.addArg(InsnArg.reg(2, continuationType));
		resumeRestore.getInstructions().add(resultGet);
		addInvoke(root, resumeTryTail, "throwOnFailure");

		SpecialEdgeAttr backEdge = new SpecialEdgeAttr(
				SpecialEdgeType.BACK_EDGE, directBridge, moveJoin);
		assertThat(FixMultiEntryLoops
				.isCoroutineDirectBridgeMoveJoinPath(backEdge))
						.isTrue();

		BlockNode sideExit = block(12);
		connect(setupMove, sideExit);
		assertThat(FixMultiEntryLoops
				.isCoroutineDirectBridgeMoveJoinPath(backEdge))
						.isFalse();
	}

	@Test
	void testDetectCoroutineDirectOuterFieldAccessorJoinPath() {
		RootNode root = new RootNode(new JadxArgs());
		ArgType continuationType = ArgType.object("test.AwaitContinuation");
		ArgType outerType = ArgType.object("test.Outer");
		ClassInfo continuationClass = ClassInfo.fromType(
				root, continuationType);
		ClassInfo outerClass = ClassInfo.fromType(root, outerType);
		FieldInfo labelField = FieldInfo.from(
				root, continuationClass, "label", ArgType.INT);
		FieldInfo resultField = FieldInfo.from(
				root, continuationClass, "result", ArgType.OBJECT);
		FieldInfo outerField = FieldInfo.from(
				root, continuationClass, "this$0", outerType);
		BlockNode projectionJoin = block(0);
		BlockNode suspendCall = block(1);
		BlockNode suspendCheck = block(2);
		BlockNode suspendedReturn = block(3);
		BlockNode resume = block(4);
		connect(projectionJoin, suspendCall);
		connect(suspendCall, suspendCheck);
		connect(suspendCheck, projectionJoin);
		connect(suspendCheck, suspendedReturn);
		connect(resume, projectionJoin);

		IndexInsnNode labelPut = new IndexInsnNode(
				InsnType.IPUT, labelField, 2);
		labelPut.addArg(InsnArg.lit(1, ArgType.INT));
		labelPut.addArg(InsnArg.reg(0, continuationType));
		suspendCall.getInstructions().add(labelPut);
		MethodInfo awaitInfo = MethodInfo.fromDetails(
				root,
				outerClass,
				"awaitWork",
				List.of(continuationType),
				ArgType.OBJECT);
		InvokeNode awaitInvoke = new InvokeNode(
				awaitInfo, InvokeType.STATIC, 1);
		awaitInvoke.addArg(InsnArg.reg(0, continuationType));
		RegisterArg awaitResult = InsnArg.reg(1, ArgType.OBJECT);
		awaitInvoke.setResult(awaitResult);
		suspendCall.getInstructions().add(awaitInvoke);

		IfNode suspended = new IfNode(
				IfOp.NE,
				projectionJoin.getStartOffset(),
				awaitResult.duplicate(),
				InsnArg.reg(2, ArgType.OBJECT));
		suspendCheck.getInstructions().add(suspended);
		suspended.initBlocks(suspendCheck);
		suspendedReturn.getInstructions().add(
				new InsnNode(InsnType.RETURN, 0));
		suspendedReturn.add(AFlag.RETURN);

		IndexInsnNode outerGet = new IndexInsnNode(
				InsnType.IGET, outerField, 1);
		RegisterArg outerResult = InsnArg.reg(3, outerType);
		outerGet.setResult(outerResult);
		outerGet.addArg(InsnArg.reg(0, continuationType));
		projectionJoin.getInstructions().add(outerGet);
		MethodInfo accessorInfo = MethodInfo.fromDetails(
				root,
				outerClass,
				"access$getStateLock$p",
				List.of(outerType),
				ArgType.OBJECT);
		InvokeNode accessor = new InvokeNode(
				accessorInfo, InvokeType.STATIC, 1);
		accessor.addArg(outerResult.duplicate());
		accessor.setResult(InsnArg.reg(4, ArgType.OBJECT));
		projectionJoin.getInstructions().add(accessor);

		IndexInsnNode resultGet = new IndexInsnNode(
				InsnType.IGET, resultField, 1);
		resultGet.setResult(InsnArg.reg(5, ArgType.OBJECT));
		resultGet.addArg(InsnArg.reg(0, continuationType));
		resume.getInstructions().add(resultGet);
		addInvoke(root, resume, "throwOnFailure");

		SpecialEdgeAttr backEdge = new SpecialEdgeAttr(
				SpecialEdgeType.BACK_EDGE, suspendCheck, projectionJoin);
		assertThat(FixMultiEntryLoops
				.isCoroutineDirectOuterFieldAccessorJoinPath(backEdge))
						.isTrue();

		outerGet.setArg(0, InsnArg.reg(0, ArgType.object("test.Other")));
		assertThat(FixMultiEntryLoops
				.isCoroutineDirectOuterFieldAccessorJoinPath(backEdge))
						.isFalse();
		outerGet.setArg(0, InsnArg.reg(0, continuationType));

		accessor.addArg(InsnArg.reg(6, outerType));
		assertThat(FixMultiEntryLoops
				.isCoroutineDirectOuterFieldAccessorJoinPath(backEdge))
						.isFalse();
	}

	@Test
	void testDetectCoroutineResumeEffectArithmeticLatchPath() {
		RootNode root = new RootNode(new JadxArgs());
		ArgType continuationType =
				ArgType.object("test.IndexedLoopContinuation");
		ClassInfo continuationClass =
				ClassInfo.fromType(root, continuationType);
		FieldInfo labelField = FieldInfo.from(
				root, continuationClass, "label", ArgType.INT);
		FieldInfo resultField = FieldInfo.from(
				root, continuationClass, "result", ArgType.OBJECT);
		BlockNode initial = block(0);
		BlockNode loopHeader = block(1);
		BlockNode suspendCall = block(2);
		BlockNode suspendCheck = block(3);
		BlockNode directBridge = block(4);
		BlockNode suspendedReturn = block(5);
		BlockNode effectLatch = block(6);
		BlockNode resume = block(7);
		BlockNode loopExit = block(8);
		connect(initial, loopHeader);
		connect(loopHeader, suspendCall);
		connect(loopHeader, loopExit);
		connect(suspendCall, suspendCheck);
		connect(suspendCheck, directBridge);
		connect(suspendCheck, suspendedReturn);
		connect(directBridge, effectLatch);
		connect(resume, effectLatch);
		connect(effectLatch, loopHeader);

		IfNode loopCondition = new IfNode(
				IfOp.LT,
				suspendCall.getStartOffset(),
				InsnArg.reg(0, ArgType.INT),
				InsnArg.reg(1, ArgType.INT));
		loopHeader.getInstructions().add(loopCondition);
		loopCondition.initBlocks(loopHeader);

		IndexInsnNode labelPut = new IndexInsnNode(
				InsnType.IPUT, labelField, 2);
		labelPut.addArg(InsnArg.lit(2, ArgType.INT));
		labelPut.addArg(InsnArg.reg(2, continuationType));
		suspendCall.getInstructions().add(labelPut);
		MethodInfo suspendInfo = MethodInfo.fromDetails(
				root,
				ClassInfo.fromName(root, "test.Actions"),
				"awaitAction",
				List.of(continuationType),
				ArgType.OBJECT);
		InvokeNode suspendInvoke = new InvokeNode(
				suspendInfo, InvokeType.STATIC, 1);
		suspendInvoke.addArg(InsnArg.reg(2, continuationType));
		RegisterArg suspendResult = InsnArg.reg(3, ArgType.OBJECT);
		suspendInvoke.setResult(suspendResult);
		suspendCall.getInstructions().add(suspendInvoke);
		IfNode suspended = new IfNode(
				IfOp.NE,
				directBridge.getStartOffset(),
				suspendResult.duplicate(),
				InsnArg.reg(4, ArgType.OBJECT));
		suspendCheck.getInstructions().add(suspended);
		suspended.initBlocks(suspendCheck);
		suspendedReturn.getInstructions().add(
				new InsnNode(InsnType.RETURN, 0));
		suspendedReturn.add(AFlag.RETURN);
		loopExit.getInstructions().add(
				new InsnNode(InsnType.RETURN, 0));
		loopExit.add(AFlag.RETURN);

		for (int i = 0; i < 6; i++) {
			addMove(
					directBridge,
					10 + i,
					20 + i,
					ArgType.INT);
		}
		MethodInfo getterInfo = MethodInfo.fromDetails(
				root,
				ClassInfo.fromName(root, "test.Item"),
				"getState",
				List.of(),
				ArgType.OBJECT);
		InvokeNode getter = new InvokeNode(
				getterInfo, InvokeType.VIRTUAL, 0);
		getter.setResult(InsnArg.reg(30, ArgType.OBJECT));
		effectLatch.getInstructions().add(getter);
		InsnNode constant = new InsnNode(InsnType.CONST, 1);
		constant.setResult(InsnArg.reg(31, ArgType.BOOLEAN));
		constant.addArg(InsnArg.lit(1, ArgType.BOOLEAN));
		effectLatch.getInstructions().add(constant);
		addInvoke(root, effectLatch, "setState");
		InsnNode increment = new InsnNode(InsnType.ARITH, 1);
		increment.setResult(InsnArg.reg(32, ArgType.INT));
		increment.addArg(InsnArg.reg(33, ArgType.INT));
		effectLatch.getInstructions().add(increment);
		addMove(effectLatch, 34, 35, ArgType.INT);

		IndexInsnNode resultGet = new IndexInsnNode(
				InsnType.IGET, resultField, 1);
		resultGet.setResult(InsnArg.reg(36, ArgType.OBJECT));
		resultGet.addArg(InsnArg.reg(2, continuationType));
		resume.getInstructions().add(resultGet);
		addInvoke(root, resume, "throwOnFailure");

		SpecialEdgeAttr backEdge = new SpecialEdgeAttr(
				SpecialEdgeType.BACK_EDGE, directBridge, effectLatch);
		assertThat(FixMultiEntryLoops
				.isCoroutineResumeEffectArithmeticLatchPath(backEdge))
						.isTrue();

		effectLatch.getInstructions().add(
				new InsnNode(InsnType.ARITH, 0));
		assertThat(FixMultiEntryLoops
				.isCoroutineResumeEffectArithmeticLatchPath(backEdge))
						.isFalse();
	}

	@Test
	void testDetectCoroutineEffectResetJoinPath() {
		RootNode root = new RootNode(new JadxArgs());
		BlockNode initial = block(0);
		BlockNode loopHeader = block(1);
		BlockNode effect = block(2);
		BlockNode reset = block(3);
		connect(initial, loopHeader);
		connect(loopHeader, effect);
		connect(effect, reset);
		connect(reset, loopHeader);
		MethodInfo getterInfo = MethodInfo.fromDetails(
				root,
				ClassInfo.fromName(root, "test.PendingSlot"),
				"value",
				List.of(),
				ArgType.INT);
		InvokeNode getter = new InvokeNode(
				getterInfo, InvokeType.VIRTUAL, 0);
		getter.setResult(InsnArg.reg(3, ArgType.INT));
		effect.getInstructions().add(getter);
		addInvoke(root, effect, "close");
		InsnNode move = new InsnNode(InsnType.MOVE, 1);
		move.setResult(InsnArg.reg(1, ArgType.INT));
		move.addArg(InsnArg.reg(0, ArgType.INT));
		reset.getInstructions().add(move);
		InsnNode constant = new InsnNode(InsnType.CONST, 1);
		constant.setResult(InsnArg.reg(2, ArgType.INT));
		constant.addArg(InsnArg.lit(0, ArgType.INT));
		reset.getInstructions().add(constant);
		SpecialEdgeAttr backEdge = new SpecialEdgeAttr(
				SpecialEdgeType.BACK_EDGE, effect, reset);

		assertThat(FixMultiEntryLoops
				.isCoroutineEffectResetJoinPath(backEdge))
						.isTrue();

		effect.getInstructions().add(
				new InsnNode(InsnType.ARITH, 0));
		assertThat(FixMultiEntryLoops
				.isCoroutineEffectResetJoinPath(backEdge))
						.isFalse();
	}

	@Test
	void testDetectCoroutineConditionalNoEffectResumeTailPath() {
		RootNode root = new RootNode(new JadxArgs());
		ArgType continuationType = ArgType.object("test.EmitContinuation");
		FieldInfo labelField = FieldInfo.from(
				root,
				ClassInfo.fromType(root, continuationType),
				"label",
				ArgType.INT);
		FieldInfo resultField = FieldInfo.from(
				root,
				ClassInfo.fromType(root, continuationType),
				"result",
				ArgType.OBJECT);
		BlockNode initial = block(0);
		BlockNode loopHeader = block(1);
		BlockNode condition = block(2);
		BlockNode effectSetup = block(3);
		BlockNode suspendCheck = block(4);
		BlockNode directTail = block(5);
		BlockNode exit = block(6);
		BlockNode resume = block(7);
		BlockNode resumeJoin = block(8);
		BlockNode resultTail = block(9);
		connect(initial, loopHeader);
		connect(loopHeader, condition);
		connect(condition, effectSetup);
		connect(condition, resumeJoin);
		connect(effectSetup, suspendCheck);
		connect(suspendCheck, directTail);
		connect(suspendCheck, exit);
		connect(directTail, loopHeader);
		connect(resume, resumeJoin);
		connect(resumeJoin, resultTail);
		connect(resultTail, loopHeader);

		IfNode optionalEffect = new IfNode(
				IfOp.EQ,
				resumeJoin.getStartOffset(),
				InsnArg.reg(0, ArgType.OBJECT),
				InsnArg.lit(0, ArgType.OBJECT));
		condition.getInstructions().add(optionalEffect);
		optionalEffect.initBlocks(condition);

		IndexInsnNode labelPut = new IndexInsnNode(
				InsnType.IPUT, labelField, 2);
		labelPut.addArg(InsnArg.lit(1, ArgType.INT));
		labelPut.addArg(InsnArg.reg(1, continuationType));
		effectSetup.getInstructions().add(labelPut);
		MethodInfo suspendInfo = MethodInfo.fromDetails(
				root,
				ClassInfo.fromName(root, "test.Store"),
				"set",
				List.of(ArgType.object("kotlin.coroutines.Continuation")),
				ArgType.OBJECT);
		InvokeNode suspendInvoke = new InvokeNode(
				suspendInfo, InvokeType.VIRTUAL, 0);
		RegisterArg suspendResult = InsnArg.reg(2, ArgType.OBJECT);
		suspendInvoke.setResult(suspendResult);
		effectSetup.getInstructions().add(suspendInvoke);

		IfNode suspended = new IfNode(
				IfOp.EQ,
				exit.getStartOffset(),
				suspendResult.duplicate(),
				InsnArg.reg(3, ArgType.OBJECT));
		suspendCheck.getInstructions().add(suspended);
		suspended.initBlocks(suspendCheck);
		InsnNode returnInsn = new InsnNode(InsnType.RETURN, 1);
		returnInsn.addArg(InsnArg.reg(3, ArgType.OBJECT));
		exit.getInstructions().add(returnInsn);

		IndexInsnNode resultGet = new IndexInsnNode(
				InsnType.IGET, resultField, 1);
		resultGet.setResult(InsnArg.reg(4, ArgType.OBJECT));
		resultGet.addArg(InsnArg.reg(1, continuationType));
		resume.getInstructions().add(resultGet);
		addInvoke(root, resume, "throwOnFailure");
		addMove(resumeJoin, 5, 6, ArgType.OBJECT);
		addMove(resultTail, 6, 5, ArgType.OBJECT);

		SpecialEdgeAttr backEdge = new SpecialEdgeAttr(
				SpecialEdgeType.BACK_EDGE, condition, resumeJoin);
		assertThat(FixMultiEntryLoops
				.isCoroutineConditionalNoEffectResumeTailPath(backEdge))
						.isTrue();

		resultTail.getInstructions().add(
				new InsnNode(InsnType.CONST, 0));
		assertThat(FixMultiEntryLoops
				.isCoroutineConditionalNoEffectResumeTailPath(backEdge))
						.isFalse();
	}

	@Test
	void testFindLabelLoadConsumedBySameSwitchBlock() {
		RootNode root = new RootNode(new JadxArgs());
		ArgType stateType = ArgType.object("test.InflateContinuation");
		FieldInfo labelField = FieldInfo.from(
				root, ClassInfo.fromType(root, stateType), "label", ArgType.INT);
		BlockNode dispatch = block(0);
		RegisterArg labelArg = InsnArg.reg(1, ArgType.INT);
		IndexInsnNode labelGet = new IndexInsnNode(InsnType.IGET, labelField, 1);
		labelGet.setResult(labelArg);
		labelGet.addArg(InsnArg.reg(0, stateType));
		dispatch.getInstructions().add(labelGet);
		dispatch.getInstructions().add(new SwitchInsn(labelArg.duplicate(), 0, true));

		assertThat(FixMultiEntryLoops.findCoroutineStateLoadBeforeSwitch(
				dispatch, labelField)).isSameAs(dispatch);
	}

	@Test
	void testRejectLabelLoadNotConsumedBySwitch() {
		RootNode root = new RootNode(new JadxArgs());
		ArgType stateType = ArgType.object("test.InflateContinuation");
		FieldInfo labelField = FieldInfo.from(
				root, ClassInfo.fromType(root, stateType), "label", ArgType.INT);
		BlockNode dispatch = block(0);
		RegisterArg labelArg = InsnArg.reg(1, ArgType.INT);
		IndexInsnNode labelGet = new IndexInsnNode(InsnType.IGET, labelField, 1);
		labelGet.setResult(labelArg);
		labelGet.addArg(InsnArg.reg(0, stateType));
		dispatch.getInstructions().add(labelGet);
		dispatch.getInstructions().add(new SwitchInsn(
				InsnArg.reg(2, ArgType.INT), 0, true));

		assertThat(FixMultiEntryLoops.findCoroutineStateLoadBeforeSwitch(
				dispatch, labelField)).isNull();
	}

	@Test
	void testDetectProtectedCoroutineDirectMoveBridgeShape() {
		BlockNode suspendCheck = block(0);
		BlockNode directBridge = block(1);
		BlockNode resume = block(2);
		BlockNode resultJoin = block(3);
		directBridge.getInstructions().add(
				new InsnNode(InsnType.MOVE, 0));
		connect(suspendCheck, directBridge);
		connect(directBridge, resultJoin);
		connect(resume, resultJoin);
		SpecialEdgeAttr backEdge = new SpecialEdgeAttr(
				SpecialEdgeType.BACK_EDGE, directBridge, resultJoin);

		assertThat(FixMultiEntryLoops
				.isProtectedCoroutineDirectMoveBridgeShape(backEdge))
						.isTrue();

		directBridge.getInstructions().add(
				new InsnNode(InsnType.INVOKE, 0));
		assertThat(FixMultiEntryLoops
				.isProtectedCoroutineDirectMoveBridgeShape(backEdge))
						.isFalse();
	}

	@Test
	void testTypedRestorePathIgnoresExceptionSuccessor() {
		RootNode root = new RootNode(new JadxArgs());
		BlockNode restore = block(0);
		BlockNode trySplitter = block(1);
		BlockNode resume = block(2);
		BlockNode handler = block(3);
		BlockNode join = block(4);
		restore.getInstructions().add(
				new InsnNode(InsnType.IGET, 0));
		MethodInfo throwOnFailure = MethodInfo.fromDetails(
				root,
				ClassInfo.fromName(root, "kotlin.ResultKt"),
				"throwOnFailure",
				List.of(ArgType.OBJECT),
				ArgType.VOID);
		resume.getInstructions().add(
				new InvokeNode(
						throwOnFailure, InvokeType.STATIC, 1));
		handler.add(AFlag.EXC_BOTTOM_SPLITTER);
		connect(restore, trySplitter);
		connect(trySplitter, resume);
		connect(trySplitter, handler);
		connect(resume, join);

		assertThat(FixMultiEntryLoops.isTypedRestoreTryPath(
				restore, resume, join, 4)).isTrue();

		resume.getInstructions().add(
				new InsnNode(InsnType.CONST, 0));
		assertThat(FixMultiEntryLoops.isTypedRestoreTryPath(
				restore, resume, join, 4)).isTrue();

		resume.getInstructions().add(
				new InsnNode(InsnType.ARITH, 0));
		assertThat(FixMultiEntryLoops.isTypedRestoreTryPath(
				restore, resume, join, 4)).isFalse();
	}

	@Test
	void testDetectCoroutinePendingSlotResumeTail() {
		RootNode root = new RootNode(new JadxArgs());
		BlockNode restore = block(0);
		BlockNode resume = block(1);
		BlockNode loopHeader = block(2);
		BlockNode takePending = block(3);
		BlockNode takeDecision = block(4);
		BlockNode awaitPending = block(5);
		BlockNode awaitDecision = block(6);
		BlockNode suspendedReturn = block(7);
		restore.getInstructions().add(new InsnNode(InsnType.IGET, 0));
		addInvoke(root, resume, "throwOnFailure");
		addInvoke(root, loopHeader, "get");
		addInvoke(root, takePending, "takePending");
		takeDecision.getInstructions().add(
				new IfNode(IfOp.EQ, -1, InsnArg.reg(0, ArgType.BOOLEAN), InsnArg.lit(0, ArgType.BOOLEAN)));
		addInvoke(root, awaitPending, "awaitPending");
		awaitDecision.getInstructions().add(
				new IfNode(IfOp.EQ, -1, InsnArg.reg(1, ArgType.OBJECT), InsnArg.reg(2, ArgType.OBJECT)));
		suspendedReturn.getInstructions().add(new InsnNode(InsnType.RETURN, 0));
		connect(restore, resume);
		connect(resume, takePending);
		connect(loopHeader, takePending);
		connect(takePending, takeDecision);
		connect(takeDecision, loopHeader);
		connect(takeDecision, awaitPending);
		connect(awaitPending, awaitDecision);
		connect(awaitDecision, loopHeader);
		connect(awaitDecision, suspendedReturn);
		List<SpecialEdgeAttr> edges = List.of(
				new SpecialEdgeAttr(SpecialEdgeType.BACK_EDGE, takeDecision, loopHeader),
				new SpecialEdgeAttr(SpecialEdgeType.BACK_EDGE, awaitDecision, loopHeader));

		assertThat(FixMultiEntryLoops.isCoroutinePendingSlotResumeTail(edges)).isTrue();

		awaitPending.getInstructions().clear();
		addInvoke(root, awaitPending, "unrelated");
		assertThat(FixMultiEntryLoops.isCoroutinePendingSlotResumeTail(edges)).isFalse();
	}

	@Test
	void testDetectInlinedCoroutinePendingSlotResumeTail() {
		RootNode root = new RootNode(new JadxArgs());
		ArgType continuationType = ArgType.object("test.PendingContinuation");
		FieldInfo labelField = FieldInfo.from(
				root,
				ClassInfo.fromType(root, continuationType),
				"y",
				ArgType.INT);
		BlockNode restore = block(0);
		BlockNode resume = block(1);
		BlockNode loopHeader = block(2);
		BlockNode emitCall = block(3);
		BlockNode emitCheck = block(4);
		BlockNode tailEntry = block(5);
		BlockNode takePending = block(6);
		BlockNode takeDecision = block(7);
		BlockNode awaitSetup = block(8);
		BlockNode compareAndSet = block(9);
		BlockNode awaitDecision = block(10);
		BlockNode suspendedReturn = block(11);
		BlockNode exit = block(12);

		restore.getInstructions().add(new InsnNode(InsnType.IGET, 0));
		addInvoke(root, resume, "throwOnFailure");
		addInvoke(root, loopHeader, "get");
		IndexInsnNode emitLabelPut = new IndexInsnNode(
				InsnType.IPUT, labelField, 2);
		emitLabelPut.addArg(InsnArg.reg(1, ArgType.INT));
		emitLabelPut.addArg(InsnArg.reg(7, continuationType));
		emitCall.getInstructions().add(emitLabelPut);
		MethodInfo emitInfo = MethodInfo.fromDetails(
				root,
				ClassInfo.fromName(root, "test.PendingCollector"),
				"emit",
				List.of(continuationType),
				ArgType.OBJECT);
		InvokeNode emitInvoke = new InvokeNode(emitInfo, InvokeType.STATIC, 1);
		emitInvoke.setResult(InsnArg.reg(2, ArgType.OBJECT));
		emitInvoke.addArg(InsnArg.reg(7, continuationType));
		emitCall.getInstructions().add(emitInvoke);
		emitCheck.getInstructions().add(
				new IfNode(
						IfOp.NE,
						-1,
						InsnArg.reg(2, ArgType.OBJECT),
						InsnArg.reg(3, ArgType.OBJECT)));
		addMove(tailEntry, 4, 5, ArgType.OBJECT);
		addInvoke(root, takePending, "getAndSet");
		takeDecision.getInstructions().add(
				new IfNode(
						IfOp.EQ,
						-1,
						InsnArg.reg(6, ArgType.OBJECT),
						InsnArg.reg(8, ArgType.OBJECT)));
		IndexInsnNode awaitLabelPut = new IndexInsnNode(
				InsnType.IPUT, labelField, 2);
		awaitLabelPut.addArg(InsnArg.reg(9, ArgType.INT));
		awaitLabelPut.addArg(InsnArg.reg(7, continuationType));
		awaitSetup.getInstructions().add(awaitLabelPut);
		addInvoke(root, compareAndSet, "compareAndSet");
		awaitDecision.getInstructions().add(
				new IfNode(
						IfOp.NE,
						-1,
						InsnArg.reg(10, ArgType.OBJECT),
						InsnArg.reg(3, ArgType.OBJECT)));
		InsnNode returnInsn = new InsnNode(InsnType.RETURN, 1);
		returnInsn.addArg(InsnArg.reg(3, ArgType.OBJECT));
		suspendedReturn.getInstructions().add(returnInsn);
		suspendedReturn.add(AFlag.RETURN);

		connect(restore, resume);
		connect(resume, tailEntry);
		connect(loopHeader, emitCall);
		connect(emitCall, emitCheck);
		connect(emitCheck, tailEntry);
		connect(emitCheck, suspendedReturn);
		connect(tailEntry, takePending);
		connect(takePending, takeDecision);
		connect(takeDecision, loopHeader);
		connect(takeDecision, awaitSetup);
		connect(awaitSetup, compareAndSet);
		connect(compareAndSet, awaitDecision);
		connect(awaitDecision, loopHeader);
		connect(awaitDecision, suspendedReturn);
		connect(suspendedReturn, exit);
		List<BlockNode> blocks = List.of(
				restore,
				resume,
				loopHeader,
				emitCall,
				emitCheck,
				tailEntry,
				takePending,
				takeDecision,
				awaitSetup,
				compareAndSet,
				awaitDecision,
				suspendedReturn,
				exit);
		List<SpecialEdgeAttr> edges = List.of(
				new SpecialEdgeAttr(
						SpecialEdgeType.BACK_EDGE, takeDecision, loopHeader),
				new SpecialEdgeAttr(
						SpecialEdgeType.BACK_EDGE, awaitDecision, loopHeader));

		assertThat(FixMultiEntryLoops.isInlinedCoroutinePendingSlotResumeTail(
				blocks, exit, edges)).isTrue();

		compareAndSet.getInstructions().clear();
		addInvoke(root, compareAndSet, "unrelated");
		assertThat(FixMultiEntryLoops.isInlinedCoroutinePendingSlotResumeTail(
				blocks, exit, edges)).isFalse();
	}

	@Test
	void testDetectCoroutineResumeNullableResultDecisionShape() {
		RootNode root = new RootNode(new JadxArgs());
		BlockNode suspendCall = block(0);
		BlockNode suspendCheck = block(1);
		BlockNode resume = block(2);
		BlockNode projection = block(3);
		BlockNode condition = block(4);
		BlockNode loopHeader = block(5);
		BlockNode completedExit = block(6);
		BlockNode suspendedReturn = block(7);
		BlockNode otherSuspendCheck = block(8);
		BlockNode exceptionHandler = block(9);
		RegisterArg suspendResult = InsnArg.reg(0, ArgType.OBJECT);
		suspendCheck.getInstructions().add(
				new IfNode(IfOp.EQ, -1, suspendResult.duplicate(), InsnArg.reg(1, ArgType.OBJECT)));
		addInvoke(root, resume, "throwOnFailure");
		IndexInsnNode cast = new IndexInsnNode(InsnType.CHECK_CAST, ArgType.OBJECT, 1);
		RegisterArg castResult = InsnArg.reg(0, ArgType.OBJECT);
		cast.setResult(castResult);
		cast.addArg(suspendResult.duplicate());
		projection.getInstructions().add(cast);
		condition.getInstructions().add(
				new IfNode(IfOp.NE, -1, castResult.duplicate(), InsnArg.lit(0, ArgType.OBJECT)));
		completedExit.getInstructions().add(new InsnNode(InsnType.RETURN, 0));
		completedExit.add(AFlag.RETURN);
		suspendedReturn.getInstructions().add(new InsnNode(InsnType.RETURN, 0));
		suspendedReturn.add(AFlag.RETURN);
		connect(suspendCall, suspendCheck);
		connect(suspendCheck, projection);
		connect(suspendCheck, suspendedReturn);
		connect(resume, projection);
		connect(projection, condition);
		exceptionHandler.getInstructions().add(new InsnNode(InsnType.MOVE_EXCEPTION, 0));
		connect(projection, exceptionHandler);
		connect(condition, loopHeader);
		connect(condition, completedExit);
		connect(loopHeader, suspendCall);
		connect(otherSuspendCheck, suspendedReturn);
		SpecialEdgeAttr backEdge = new SpecialEdgeAttr(
				SpecialEdgeType.BACK_EDGE, suspendCheck, projection);

		assertThat(FixMultiEntryLoops
				.isCoroutineResumeNullableResultDecisionShape(backEdge))
						.isTrue();

		projection.getInstructions().add(new InsnNode(InsnType.INVOKE, 0));
		assertThat(FixMultiEntryLoops
				.isCoroutineResumeNullableResultDecisionShape(backEdge))
						.isFalse();
	}

	@Test
	void testMatchBooleanGetterResultToTrueSuccessor() {
		RootNode root = new RootNode(new JadxArgs());
		MethodInfo getterInfo = MethodInfo.fromDetails(
				root,
				ClassInfo.fromName(root, "io.ktor.utils.io.ByteReadChannel"),
				"isClosedForRead",
				List.of(),
				ArgType.BOOLEAN);
		InvokeNode getter = new InvokeNode(
				getterInfo, InvokeType.INTERFACE, 1);
		getter.addArg(InsnArg.reg(
				0, ArgType.object("io.ktor.utils.io.ByteReadChannel")));
		RegisterArg result = InsnArg.reg(1, ArgType.BOOLEAN);
		getter.setResult(result);

		BlockNode condition = block(0);
		BlockNode closed = block(1);
		BlockNode open = block(2);
		IfNode ifInsn = new IfNode(
				IfOp.NE,
				closed.getStartOffset(),
				result.duplicate(),
				InsnArg.lit(0, ArgType.BOOLEAN));
		condition.getInstructions().add(ifInsn);
		connect(condition, closed);
		connect(condition, open);
		ifInsn.initBlocks(condition);

		assertThat(FixMultiEntryLoops.isGetterBooleanTrueSuccessor(
				getter, ifInsn, closed)).isTrue();
		assertThat(FixMultiEntryLoops.isGetterBooleanTrueSuccessor(
				getter, ifInsn, open)).isFalse();

		ifInsn.setArg(0, InsnArg.reg(2, ArgType.BOOLEAN));
		assertThat(FixMultiEntryLoops.isGetterBooleanTrueSuccessor(
				getter, ifInsn, closed)).isFalse();
	}

	private static void addInvoke(RootNode root, BlockNode block, String name) {
		MethodInfo method = MethodInfo.fromDetails(
				root,
				ClassInfo.fromName(root, "test.PendingSlot"),
				name,
				List.of(),
				ArgType.VOID);
		block.getInstructions().add(new InvokeNode(method, InvokeType.VIRTUAL, 0));
	}

	private static void addMove(
			BlockNode block, int resultReg, int sourceReg, ArgType type) {
		InsnNode move = new InsnNode(InsnType.MOVE, 1);
		move.setResult(InsnArg.reg(resultReg, type));
		move.addArg(InsnArg.reg(sourceReg, type));
		block.getInstructions().add(move);
	}

	private static void connect(BlockNode source, BlockNode target) {
		source.getSuccessors().add(target);
		target.getPredecessors().add(source);
	}

	private static BlockNode block(int id) {
		return new BlockNode(id, id, id);
	}
}
