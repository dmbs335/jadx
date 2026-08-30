package jadx.core.dex.visitors.regions.maker;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import jadx.api.JadxArgs;
import jadx.core.dex.attributes.AFlag;
import jadx.core.dex.attributes.nodes.PhiListAttr;
import jadx.core.dex.info.ClassInfo;
import jadx.core.dex.info.FieldInfo;
import jadx.core.dex.info.MethodInfo;
import jadx.core.dex.instructions.IfNode;
import jadx.core.dex.instructions.IfOp;
import jadx.core.dex.instructions.IndexInsnNode;
import jadx.core.dex.instructions.InsnType;
import jadx.core.dex.instructions.InvokeNode;
import jadx.core.dex.instructions.InvokeType;
import jadx.core.dex.instructions.PhiInsn;
import jadx.core.dex.instructions.args.ArgType;
import jadx.core.dex.instructions.args.CodeVar;
import jadx.core.dex.instructions.args.InsnArg;
import jadx.core.dex.instructions.args.LiteralArg;
import jadx.core.dex.instructions.args.RegisterArg;
import jadx.core.dex.instructions.args.SSAVar;
import jadx.core.dex.nodes.BlockNode;
import jadx.core.dex.nodes.InsnNode;
import jadx.core.dex.nodes.RootNode;

import static jadx.core.dex.visitors.blocks.BlockSplitter.connect;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

class IfRegionMakerTest {
	@Test
	void memoizeSharedPathsAndRejectCycles() {
		BlockNode start = new BlockNode(1, 0, 0);
		BlockNode current = start;
		List<BlockNode> blocks = new ArrayList<>();
		blocks.add(start);
		for (int i = 0; i < 32; i++) {
			int id = blocks.size() + 1;
			BlockNode left = new BlockNode(id, id, id);
			BlockNode right = new BlockNode(id + 1, id + 1, id + 1);
			BlockNode merge = new BlockNode(id + 2, id + 2, id + 2);
			connect(current, left);
			connect(current, right);
			connect(left, merge);
			connect(right, merge);
			blocks.add(left);
			blocks.add(right);
			blocks.add(merge);
			current = merge;
		}
		BlockNode join = new BlockNode(200, 200, 200);
		BlockNode loopEnd = new BlockNode(201, 201, 201);
		connect(current, join);
		blocks.add(join);
		blocks.add(loopEnd);
		blocks.forEach(BlockNode::updateCleanSuccessors);

		assertTimeoutPreemptively(Duration.ofSeconds(1),
				() -> assertThat(IfRegionMaker.allPathsReachJoinOrContinue(start, join, loopEnd)).isTrue());

		BlockNode cycleStart = new BlockNode(300, 300, 300);
		BlockNode cycleNext = new BlockNode(301, 301, 301);
		connect(cycleStart, cycleNext);
		connect(cycleNext, cycleStart);
		cycleStart.updateCleanSuccessors();
		cycleNext.updateCleanSuccessors();
		assertThat(IfRegionMaker.allPathsReachJoinOrContinue(cycleStart, join, loopEnd)).isFalse();
	}

	@Test
	void preserveFirstCoroutineResumeJoinBeforeDeeperPhi() {
		RootNode root = new RootNode(new JadxArgs());
		ArgType continuationType = ArgType.object("test.ProcessContinuation");
		BlockNode labelBranch = new BlockNode(1, 0, 0);
		BlockNode initialBranch = new BlockNode(2, 1, 1);
		BlockNode resumeRestore = new BlockNode(3, 2, 2);
		BlockNode directCheck = new BlockNode(4, 3, 3);
		BlockNode resultJoin = new BlockNode(5, 4, 4);
		BlockNode suspendedReturn = new BlockNode(6, 5, 5);

		FieldInfo savedLocal = FieldInfo.from(
				root, ClassInfo.fromType(root, continuationType), "L$0", ArgType.OBJECT);
		IndexInsnNode localGet = new IndexInsnNode(InsnType.IGET, savedLocal, 1);
		localGet.setResult(InsnArg.reg(1, ArgType.OBJECT));
		localGet.addArg(InsnArg.reg(0, continuationType));
		IndexInsnNode localCast = new IndexInsnNode(InsnType.CHECK_CAST, ArgType.OBJECT, 1);
		localCast.setResult(InsnArg.reg(2, ArgType.OBJECT));
		localCast.addArg(InsnArg.wrapInsnIntoArg(localGet));
		resumeRestore.getInstructions().add(localCast);
		MethodInfo failureCheck = MethodInfo.fromDetails(
				root,
				ClassInfo.fromName(root, "kotlin.ResultKt"),
				"throwOnFailure",
				List.of(ArgType.OBJECT),
				ArgType.VOID);
		InvokeNode failureInvoke = new InvokeNode(failureCheck, InvokeType.STATIC, 1);
		failureInvoke.addArg(InsnArg.reg(3, ArgType.OBJECT));
		resumeRestore.getInstructions().add(failureInvoke);

		IfNode suspendedCheck = new IfNode(
				IfOp.NE,
				resultJoin.getStartOffset(),
				InsnArg.reg(4, ArgType.OBJECT),
				InsnArg.reg(5, ArgType.OBJECT));
		directCheck.getInstructions().add(suspendedCheck);
		suspendedReturn.getInstructions().add(new InsnNode(InsnType.RETURN, 0));
		suspendedReturn.add(AFlag.RETURN);

		connect(labelBranch, resumeRestore);
		connect(initialBranch, directCheck);
		connect(resumeRestore, resultJoin);
		connect(directCheck, resultJoin);
		connect(directCheck, suspendedReturn);
		labelBranch.updateCleanSuccessors();
		initialBranch.updateCleanSuccessors();
		resumeRestore.updateCleanSuccessors();
		directCheck.updateCleanSuccessors();
		resultJoin.updateCleanSuccessors();
		suspendedReturn.updateCleanSuccessors();
		suspendedCheck.initBlocks(directCheck);

		assertThat(IfRegionMaker.isDirectCoroutineResumeJoin(
				resultJoin, labelBranch, initialBranch)).isTrue();

		resumeRestore.getInstructions().remove(localCast);
		assertThat(IfRegionMaker.isDirectCoroutineResumeJoin(
				resultJoin, labelBranch, initialBranch)).isFalse();

		FieldInfo obfuscatedLocalC = FieldInfo.from(
				root, ClassInfo.fromType(root, continuationType), "c", ArgType.OBJECT);
		IndexInsnNode obfuscatedGetC = new IndexInsnNode(InsnType.IGET, obfuscatedLocalC, 1);
		obfuscatedGetC.setResult(InsnArg.reg(6, ArgType.OBJECT));
		obfuscatedGetC.addArg(InsnArg.reg(0, continuationType));
		FieldInfo obfuscatedLocalD = FieldInfo.from(
				root, ClassInfo.fromType(root, continuationType), "d", ArgType.OBJECT);
		IndexInsnNode obfuscatedGetD = new IndexInsnNode(InsnType.IGET, obfuscatedLocalD, 1);
		obfuscatedGetD.setResult(InsnArg.reg(7, ArgType.OBJECT));
		obfuscatedGetD.addArg(InsnArg.reg(0, continuationType));
		resumeRestore.getInstructions().add(0, obfuscatedGetC);
		resumeRestore.getInstructions().add(1, obfuscatedGetD);

		assertThat(IfRegionMaker.isDirectCoroutineResumeJoin(
				resultJoin, labelBranch, initialBranch)).isTrue();

		resumeRestore.getInstructions().remove(obfuscatedGetD);
		assertThat(IfRegionMaker.isDirectCoroutineResumeJoin(
				resultJoin, labelBranch, initialBranch)).isFalse();
	}

	@Test
	void acceptBranchStartAsJoinWithoutInspectingTerminalPaths() {
		BlockNode thenBlock = new BlockNode(1, 0, 0);
		BlockNode elseBlock = new BlockNode(2, 1, 1);

		assertThat(IfRegionMaker.isCommonPostDominator(thenBlock, elseBlock, thenBlock)).isTrue();
		assertThat(IfRegionMaker.isCommonPostDominator(thenBlock, elseBlock, elseBlock)).isTrue();
	}

	@Test
	void identifyDirectBranchJoin() {
		BlockNode thenBlock = new BlockNode(1, 0, 0);
		BlockNode elseBlock = new BlockNode(2, 1, 1);
		BlockNode laterJoin = new BlockNode(3, 2, 2);

		assertThat(IfRegionMaker.isDirectBranchJoin(thenBlock, thenBlock, elseBlock)).isTrue();
		assertThat(IfRegionMaker.isDirectBranchJoin(elseBlock, thenBlock, elseBlock)).isTrue();
		assertThat(IfRegionMaker.isDirectBranchJoin(laterJoin, thenBlock, elseBlock)).isFalse();
		assertThat(IfRegionMaker.isDirectBranchJoin(null, thenBlock, elseBlock)).isFalse();
	}

	@Test
	void distinguishReachableDirectJoinFromTerminalBranch() {
		BlockNode thenBlock = new BlockNode(1, 0, 0);
		BlockNode elseBlock = new BlockNode(2, 1, 1);
		connect(elseBlock, thenBlock);
		elseBlock.updateCleanSuccessors();
		thenBlock.updateCleanSuccessors();

		assertThat(IfRegionMaker.isReachableDirectBranchJoin(thenBlock, thenBlock, elseBlock)).isTrue();
		assertThat(IfRegionMaker.isReachableDirectBranchJoin(elseBlock, thenBlock, elseBlock)).isFalse();
		assertThat(IfRegionMaker.isReachableDirectBranchJoin(null, thenBlock, elseBlock)).isFalse();
	}

	@Test
	void distinguishCommonJoinFromPartiallyBypassedCandidate() {
		BlockNode thenBlock = new BlockNode(1, 0, 0);
		BlockNode elseBlock = new BlockNode(2, 1, 1);
		BlockNode bypassedCandidate = new BlockNode(3, 2, 2);
		BlockNode commonJoin = new BlockNode(4, 3, 3);
		connect(thenBlock, bypassedCandidate);
		connect(thenBlock, commonJoin);
		connect(elseBlock, bypassedCandidate);
		connect(bypassedCandidate, commonJoin);
		thenBlock.updateCleanSuccessors();
		elseBlock.updateCleanSuccessors();
		bypassedCandidate.updateCleanSuccessors();
		commonJoin.updateCleanSuccessors();

		assertThat(IfRegionMaker.isCommonPostDominator(thenBlock, elseBlock, commonJoin)).isTrue();
		assertThat(IfRegionMaker.isCommonPostDominator(thenBlock, elseBlock, bypassedCandidate)).isFalse();
	}

	@Test
	void detectReadOnlyBooleanPhiPathSelectingLoopContinuation() {
		BlockNode falsePath = new BlockNode(1, 0, 0);
		BlockNode truePath = new BlockNode(2, 1, 1);
		BlockNode join = new BlockNode(3, 2, 2);
		BlockNode continuation = new BlockNode(4, 3, 3);
		BlockNode update = new BlockNode(5, 4, 4);
		BlockNode loopEnd = new BlockNode(6, 5, 5);
		loopEnd.add(AFlag.LOOP_END);

		CodeVar falseCodeVar = new CodeVar();
		falseCodeVar.setType(ArgType.BOOLEAN);
		RegisterArg falseAssign = InsnArg.reg(0, ArgType.BOOLEAN);
		SSAVar falseVar = new SSAVar(0, 0, falseAssign);
		falseVar.setCodeVar(falseCodeVar);
		InsnNode falseConst = new InsnNode(InsnType.CONST, 1);
		falseConst.setResult(falseAssign);
		falseConst.addArg(LiteralArg.litFalse());
		falsePath.getInstructions().add(falseConst);

		CodeVar trueCodeVar = new CodeVar();
		trueCodeVar.setType(ArgType.BOOLEAN);
		RegisterArg trueAssign = InsnArg.reg(0, ArgType.BOOLEAN);
		SSAVar trueVar = new SSAVar(0, 1, trueAssign);
		trueVar.setCodeVar(trueCodeVar);
		InsnNode trueConst = new InsnNode(InsnType.CONST, 1);
		trueConst.setResult(trueAssign);
		trueConst.addArg(LiteralArg.litTrue());
		truePath.getInstructions().add(trueConst);

		CodeVar phiCodeVar = new CodeVar();
		phiCodeVar.setType(ArgType.BOOLEAN);
		RegisterArg phiAssign = InsnArg.reg(0, ArgType.BOOLEAN);
		SSAVar phiVar = new SSAVar(0, 2, phiAssign);
		phiVar.setCodeVar(phiCodeVar);
		PhiInsn phi = new PhiInsn(0, 2);
		phi.setResult(phiAssign);
		RegisterArg falseInput = falseAssign.duplicate();
		falseVar.use(falseInput);
		phi.bindArg(falseInput, falsePath);
		RegisterArg trueInput = trueAssign.duplicate();
		trueVar.use(trueInput);
		phi.bindArg(trueInput, truePath);
		PhiListAttr phiList = new PhiListAttr();
		phiList.getList().add(phi);
		join.addAttr(phiList);

		RegisterArg conditionArg = phiAssign.duplicate();
		phiVar.use(conditionArg);
		IfNode condition = new IfNode(IfOp.EQ, continuation.getStartOffset(),
				conditionArg, LiteralArg.litFalse());
		join.getInstructions().add(condition);
		InsnNode updateReturn = new InsnNode(InsnType.RETURN, 0);
		update.getInstructions().add(updateReturn);

		connect(falsePath, join);
		connect(truePath, join);
		connect(join, continuation);
		connect(join, update);
		connect(continuation, loopEnd);
		falsePath.updateCleanSuccessors();
		truePath.updateCleanSuccessors();
		join.updateCleanSuccessors();
		continuation.updateCleanSuccessors();
		update.updateCleanSuccessors();
		loopEnd.updateCleanSuccessors();
		condition.initBlocks(join);

		assertThat(IfRegionMaker.isReadOnlySelectedLoopContinuation(falsePath)).isTrue();

		falsePath.getInstructions().add(new InsnNode(InsnType.RETURN, 0));
		assertThat(IfRegionMaker.isReadOnlySelectedLoopContinuation(falsePath)).isFalse();
	}
}
