package jadx.core.dex.visitors.typeinference;

import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import jadx.api.CommentsLevel;
import jadx.api.JadxArgs;
import jadx.core.dex.attributes.AFlag;
import jadx.core.dex.attributes.nodes.JadxCommentsAttr;
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
import jadx.core.dex.instructions.args.PrimitiveType;
import jadx.core.dex.instructions.args.RegisterArg;
import jadx.core.dex.instructions.args.SSAVar;
import jadx.core.dex.instructions.mods.TernaryInsn;
import jadx.core.dex.nodes.BlockNode;
import jadx.core.dex.nodes.InsnNode;
import jadx.core.dex.nodes.RootNode;
import jadx.core.dex.regions.conditions.IfCondition;

import static org.assertj.core.api.Assertions.assertThat;

class FinishTypeInferenceTest {
	@Test
	void castCoroutineStateAndCleanupReceiversAcrossRoundTripRelay() {
		RootNode root = new RootNode(new JadxArgs());
		ArgType stateType = ArgType.object("test.TryLockContinuation");
		ArgType mutexType = ArgType.object("kotlinx.coroutines.sync.Mutex");
		CodeVar sharedCodeVar = new CodeVar();
		sharedCodeVar.setDeclared(true);

		RegisterArg stateRootAssign = InsnArg.reg(0, ArgType.UNKNOWN_OBJECT);
		SSAVar stateRootVar = new SSAVar(0, 0, stateRootAssign);
		stateRootVar.setCodeVar(sharedCodeVar);
		InsnNode stateRootMove = new InsnNode(InsnType.MOVE, 1);
		stateRootMove.setResult(stateRootAssign);
		stateRootMove.addArg(makeKnownSource(1, stateType).duplicate());
		stateRootMove.add(AFlag.SYNTHETIC);

		RegisterArg statePhiAssign = InsnArg.reg(0, ArgType.UNKNOWN_OBJECT);
		SSAVar statePhiVar = new SSAVar(0, 1, statePhiAssign);
		statePhiVar.setCodeVar(sharedCodeVar);
		PhiInsn statePhi = new PhiInsn(0, 1);
		statePhi.setResult(statePhiAssign);
		statePhi.bindArg(stateRootAssign.duplicate(), new BlockNode(0, 0, 0));

		RegisterArg fieldReceiver = statePhiAssign.duplicate();
		fieldReceiver.forceSetInitType(stateType);
		statePhiVar.use(fieldReceiver);
		FieldInfo labelField = FieldInfo.from(root, ClassInfo.fromType(root, stateType), "label", ArgType.INT);
		IndexInsnNode fieldPut = new IndexInsnNode(InsnType.IPUT, labelField, 2);
		fieldPut.addArg(InsnArg.lit(1, ArgType.INT));
		fieldPut.addArg(fieldReceiver);

		CodeVar relayCodeVar = new CodeVar();
		RegisterArg relayAssign = InsnArg.reg(2, ArgType.UNKNOWN_OBJECT);
		SSAVar relayVar = new SSAVar(2, 0, relayAssign);
		relayVar.setCodeVar(relayCodeVar);
		RegisterArg relaySource = statePhiAssign.duplicate();
		statePhiVar.use(relaySource);
		InsnNode relayMove = new InsnNode(InsnType.MOVE, 1);
		relayMove.setResult(relayAssign);
		relayMove.addArg(relaySource);

		RegisterArg bridgeAssign = InsnArg.reg(0, ArgType.UNKNOWN_OBJECT);
		SSAVar bridgeVar = new SSAVar(0, 2, bridgeAssign);
		bridgeVar.setCodeVar(sharedCodeVar);
		RegisterArg bridgeSource = relayAssign.duplicate();
		relayVar.use(bridgeSource);
		InsnNode bridgeMove = new InsnNode(InsnType.MOVE, 1);
		bridgeMove.setResult(bridgeAssign);
		bridgeMove.addArg(bridgeSource);

		RegisterArg cleanupAssign = InsnArg.reg(0, ArgType.UNKNOWN_OBJECT);
		SSAVar cleanupVar = new SSAVar(0, 3, cleanupAssign);
		cleanupVar.setCodeVar(sharedCodeVar);
		InsnNode cleanupMove = new InsnNode(InsnType.MOVE, 1);
		cleanupMove.setResult(cleanupAssign);
		cleanupMove.addArg(makeKnownSource(3, mutexType).duplicate());
		RegisterArg cleanupReceiver = cleanupAssign.duplicate();
		cleanupReceiver.forceSetInitType(mutexType);
		cleanupVar.use(cleanupReceiver);
		MethodInfo unlockMth = MethodInfo.fromDetails(
				root, ClassInfo.fromType(root, mutexType), "unlock", List.of(ArgType.OBJECT), ArgType.VOID);
		InvokeNode unlock = new InvokeNode(unlockMth, InvokeType.INTERFACE, 2);
		unlock.addArg(cleanupReceiver);
		unlock.addArg(LiteralArg.make(0, ArgType.OBJECT));

		List<SSAVar> sharedVars = List.of(stateRootVar, statePhiVar, bridgeVar, cleanupVar);
		sharedCodeVar.setSsaVars(sharedVars);
		relayCodeVar.setSsaVars(List.of(relayVar));
		Map<CodeVar, List<SSAVar>> groups = new IdentityHashMap<>();
		groups.put(sharedCodeVar, sharedVars);
		groups.put(relayCodeVar, List.of(relayVar));

		FinishTypeInference.repairCoroutineStateCleanupPathCasts(null, groups);

		assertThat(sharedCodeVar.getType()).isEqualTo(ArgType.OBJECT);
		assertThat(relayCodeVar.getType()).isEqualTo(ArgType.OBJECT);
		assertThat(fieldPut.getArg(1).isInsnWrap()).isTrue();
		assertThat(unlock.getArg(0).isInsnWrap()).isTrue();
		assertThat(relayMove.getArg(0).isRegister()).isTrue();
		assertThat(bridgeMove.getArg(0).isRegister()).isTrue();
	}

	@Test
	void rejectCoroutineStateCleanupFlowWithoutExactReferenceRoot() {
		RootNode root = new RootNode(new JadxArgs());
		ArgType stateType = ArgType.object("test.TryLockContinuation");
		ArgType mutexType = ArgType.object("kotlinx.coroutines.sync.Mutex");
		CodeVar sharedCodeVar = new CodeVar();

		RegisterArg primitiveRootAssign = InsnArg.reg(0, ArgType.UNKNOWN);
		SSAVar primitiveRootVar = new SSAVar(0, 0, primitiveRootAssign);
		primitiveRootVar.setCodeVar(sharedCodeVar);
		InsnNode primitiveRootMove = new InsnNode(InsnType.MOVE, 1);
		primitiveRootMove.setResult(primitiveRootAssign);
		primitiveRootMove.addArg(makeKnownSource(1, ArgType.INT).duplicate());
		primitiveRootMove.add(AFlag.SYNTHETIC);

		RegisterArg statePhiAssign = InsnArg.reg(0, ArgType.UNKNOWN_OBJECT);
		SSAVar statePhiVar = new SSAVar(0, 1, statePhiAssign);
		statePhiVar.setCodeVar(sharedCodeVar);
		PhiInsn statePhi = new PhiInsn(0, 1);
		statePhi.setResult(statePhiAssign);
		statePhi.bindArg(primitiveRootAssign.duplicate(), new BlockNode(0, 0, 0));
		RegisterArg fieldReceiver = statePhiAssign.duplicate();
		fieldReceiver.forceSetInitType(stateType);
		statePhiVar.use(fieldReceiver);
		FieldInfo labelField = FieldInfo.from(root, ClassInfo.fromType(root, stateType), "label", ArgType.INT);
		IndexInsnNode fieldPut = new IndexInsnNode(InsnType.IPUT, labelField, 2);
		fieldPut.addArg(InsnArg.lit(1, ArgType.INT));
		fieldPut.addArg(fieldReceiver);

		RegisterArg cleanupAssign = InsnArg.reg(0, ArgType.UNKNOWN_OBJECT);
		SSAVar cleanupVar = new SSAVar(0, 2, cleanupAssign);
		cleanupVar.setCodeVar(sharedCodeVar);
		InsnNode cleanupMove = new InsnNode(InsnType.MOVE, 1);
		cleanupMove.setResult(cleanupAssign);
		cleanupMove.addArg(makeKnownSource(2, mutexType).duplicate());
		RegisterArg cleanupReceiver = cleanupAssign.duplicate();
		cleanupReceiver.forceSetInitType(mutexType);
		cleanupVar.use(cleanupReceiver);
		MethodInfo unlockMth = MethodInfo.fromDetails(
				root, ClassInfo.fromType(root, mutexType), "unlock", List.of(), ArgType.VOID);
		InvokeNode unlock = new InvokeNode(unlockMth, InvokeType.INTERFACE, 1);
		unlock.addArg(cleanupReceiver);

		RegisterArg structuralAssign = InsnArg.reg(0, ArgType.UNKNOWN_OBJECT);
		SSAVar structuralVar = new SSAVar(0, 3, structuralAssign);
		structuralVar.setCodeVar(sharedCodeVar);
		InsnNode structuralMove = new InsnNode(InsnType.MOVE, 1);
		structuralMove.setResult(structuralAssign);
		structuralMove.addArg(statePhiAssign.duplicate());

		List<SSAVar> sharedVars = List.of(primitiveRootVar, statePhiVar, cleanupVar, structuralVar);
		sharedCodeVar.setSsaVars(sharedVars);
		Map<CodeVar, List<SSAVar>> groups = new IdentityHashMap<>();
		groups.put(sharedCodeVar, sharedVars);

		FinishTypeInference.repairCoroutineStateCleanupPathCasts(null, groups);

		assertThat(sharedCodeVar.getType()).isNull();
		assertThat(fieldPut.getArg(1).isRegister()).isTrue();
		assertThat(unlock.getArg(0).isRegister()).isTrue();
	}

	@Test
	void castPathSensitiveCoroutineContinuationUses() {
		RootNode root = new RootNode(new JadxArgs());
		ArgType stateType = ArgType.object("test.DragContinuation");
		ArgType continuationType = ArgType.object("kotlin.coroutines.Continuation");
		CodeVar sharedCodeVar = new CodeVar();

		RegisterArg localAssign = InsnArg.reg(0, ArgType.UNKNOWN_OBJECT);
		SSAVar localVar = new SSAVar(0, 0, localAssign);
		localVar.setCodeVar(sharedCodeVar);
		InsnNode localMove = new InsnNode(InsnType.MOVE, 1);
		localMove.setResult(localAssign);
		localMove.addArg(InsnArg.reg(1, ArgType.OBJECT));

		RegisterArg phiAssign = InsnArg.reg(0, ArgType.UNKNOWN_OBJECT);
		SSAVar phiVar = new SSAVar(0, 1, phiAssign);
		phiVar.setCodeVar(sharedCodeVar);
		PhiInsn phiInsn = new PhiInsn(0, 1);
		phiInsn.setResult(phiAssign);
		phiInsn.bindArg(localAssign.duplicate(), new BlockNode(0, 0, 0));

		RegisterArg fieldReceiver = phiAssign.duplicate();
		fieldReceiver.forceSetInitType(stateType);
		phiVar.use(fieldReceiver);
		FieldInfo labelField = FieldInfo.from(root, ClassInfo.fromType(root, stateType), "label", ArgType.INT);
		IndexInsnNode fieldPut = new IndexInsnNode(InsnType.IPUT, labelField, 2);
		fieldPut.addArg(InsnArg.reg(2, ArgType.INT));
		fieldPut.addArg(fieldReceiver);

		RegisterArg continuationUse = phiAssign.duplicate();
		continuationUse.forceSetInitType(continuationType);
		phiVar.use(continuationUse);
		InsnNode awaitCall = new InsnNode(InsnType.INVOKE, 1);
		awaitCall.addArg(continuationUse);

		RegisterArg moveUse = phiAssign.duplicate();
		moveUse.forceSetInitType(ArgType.UNKNOWN_OBJECT);
		phiVar.use(moveUse);
		InsnNode passThroughMove = new InsnNode(InsnType.MOVE, 1);
		passThroughMove.addArg(moveUse);

		sharedCodeVar.setSsaVars(List.of(localVar, phiVar));
		Map<CodeVar, List<SSAVar>> groups = new IdentityHashMap<>();
		groups.put(sharedCodeVar, List.of(localVar, phiVar));

		FinishTypeInference.repairCoroutineContinuationPathCasts(null, groups);

		assertThat(sharedCodeVar.getType()).isEqualTo(ArgType.OBJECT);
		assertThat(fieldPut.getArg(1).isInsnWrap()).isTrue();
		assertThat(awaitCall.getArg(0).isInsnWrap()).isTrue();
		assertThat(passThroughMove.getArg(0).isRegister()).isTrue();
	}

	@Test
	void repairLateReferenceNullPhiFlow() {
		CodeVar flowCodeVar = new CodeVar();
		RegisterArg objectSource = makeKnownSource(1, ArgType.OBJECT);
		RegisterArg stringSource = makeKnownSource(2, ArgType.STRING);

		RegisterArg firstAssign = InsnArg.reg(0, ArgType.UNKNOWN_OBJECT);
		SSAVar firstVar = new SSAVar(0, 0, firstAssign);
		firstVar.setCodeVar(flowCodeVar);
		InsnNode firstMove = new InsnNode(InsnType.MOVE, 1);
		firstMove.setResult(firstAssign);
		firstMove.addArg(objectSource.duplicate());

		RegisterArg secondAssign = InsnArg.reg(0, ArgType.UNKNOWN_OBJECT);
		SSAVar secondVar = new SSAVar(0, 1, secondAssign);
		secondVar.setCodeVar(flowCodeVar);
		InsnNode secondMove = new InsnNode(InsnType.MOVE, 1);
		secondMove.setResult(secondAssign);
		secondMove.addArg(stringSource.duplicate());

		RegisterArg phiAssign = InsnArg.reg(0, ArgType.UNKNOWN);
		SSAVar phiVar = new SSAVar(0, 2, phiAssign);
		phiVar.setCodeVar(flowCodeVar);
		PhiInsn phiInsn = new PhiInsn(0, 2);
		phiInsn.setResult(phiAssign);
		phiInsn.bindArg(firstAssign.duplicate(), new BlockNode(0, 0, 0));
		phiInsn.bindArg(secondAssign.duplicate(), new BlockNode(1, 0, 0));
		new IfNode(IfOp.EQ, -1, phiAssign.duplicate(), LiteralArg.make(0, ArgType.INT));

		FinishTypeInference.repairLateReferenceNullFlows(List.of(firstVar, secondVar, phiVar));

		assertThat(flowCodeVar.getType()).isEqualTo(ArgType.OBJECT);
	}

	@Test
	void keepPrimitivePhiComparedWithZeroUnresolved() {
		CodeVar flowCodeVar = new CodeVar();
		RegisterArg intSource = makeKnownSource(1, ArgType.INT);

		RegisterArg moveAssign = InsnArg.reg(0, ArgType.UNKNOWN);
		SSAVar moveVar = new SSAVar(0, 0, moveAssign);
		moveVar.setCodeVar(flowCodeVar);
		InsnNode move = new InsnNode(InsnType.MOVE, 1);
		move.setResult(moveAssign);
		move.addArg(intSource.duplicate());

		RegisterArg phiAssign = InsnArg.reg(0, ArgType.UNKNOWN);
		SSAVar phiVar = new SSAVar(0, 1, phiAssign);
		phiVar.setCodeVar(flowCodeVar);
		PhiInsn phiInsn = new PhiInsn(0, 1);
		phiInsn.setResult(phiAssign);
		phiInsn.bindArg(moveAssign.duplicate(), new BlockNode(0, 0, 0));
		new IfNode(IfOp.EQ, -1, phiAssign.duplicate(), LiteralArg.make(0, ArgType.INT));

		FinishTypeInference.repairLateReferenceNullFlows(List.of(moveVar, phiVar));

		assertThat(flowCodeVar.getType()).isNull();
	}

	@Test
	void inferNullablePhiFromSyntheticMoveTarget() {
		ArgType schemaType = ArgType.object("test.Schema");
		CodeVar flowCodeVar = new CodeVar();

		RegisterArg nullAssign = InsnArg.reg(0, ArgType.UNKNOWN);
		SSAVar nullVar = new SSAVar(0, 0, nullAssign);
		nullVar.setCodeVar(flowCodeVar);
		InsnNode nullInsn = new InsnNode(InsnType.CONST, 1);
		nullInsn.setResult(nullAssign);
		nullInsn.addArg(LiteralArg.make(0, ArgType.UNKNOWN));

		RegisterArg schemaSource = makeKnownSource(1, schemaType);
		RegisterArg schemaAssign = InsnArg.reg(0, ArgType.UNKNOWN);
		SSAVar schemaVar = new SSAVar(0, 1, schemaAssign);
		schemaVar.setCodeVar(flowCodeVar);
		InsnNode schemaMove = new InsnNode(InsnType.MOVE, 1);
		schemaMove.setResult(schemaAssign);
		schemaMove.addArg(schemaSource.duplicate());

		RegisterArg phiAssign = InsnArg.reg(0, ArgType.UNKNOWN);
		SSAVar phiVar = new SSAVar(0, 2, phiAssign);
		phiVar.setCodeVar(flowCodeVar);
		PhiInsn phiInsn = new PhiInsn(0, 2);
		phiInsn.setResult(phiAssign);
		phiInsn.bindArg(nullAssign.duplicate(), new BlockNode(0, 0, 0));
		phiInsn.bindArg(schemaAssign.duplicate(), new BlockNode(1, 0, 0));
		IfNode nullCheck = new IfNode(IfOp.EQ, -1, phiAssign.duplicate(), LiteralArg.make(0, ArgType.INT));

		RegisterArg targetAssign = InsnArg.reg(2, schemaType);
		SSAVar targetVar = new SSAVar(2, 0, targetAssign);
		CodeVar targetCodeVar = new CodeVar();
		targetCodeVar.setType(schemaType);
		targetVar.setCodeVar(targetCodeVar);
		InsnNode targetMove = new InsnNode(InsnType.MOVE, 1);
		targetMove.setResult(targetAssign);
		targetMove.addArg(phiAssign.duplicate());
		targetMove.add(AFlag.SYNTHETIC);
		RegisterArg objectUse = phiAssign.duplicate();
		objectUse.forceSetInitType(ArgType.OBJECT);
		InsnNode erasedGenericUse = new InsnNode(InsnType.RETURN, 1);
		erasedGenericUse.addArg(objectUse);

		FinishTypeInference.repairLateReferenceNullFlows(List.of(nullVar, schemaVar, phiVar, targetVar));

		assertThat(flowCodeVar.getType()).isEqualTo(schemaType);
		assertThat(nullVar.getTypeInfo().getType()).isEqualTo(schemaType);
		assertThat(nullAssign.getInitType()).isEqualTo(schemaType);
		assertThat(nullInsn.getArg(0).getType()).isEqualTo(schemaType);
		assertThat(nullCheck.getArg(1).getType()).isEqualTo(schemaType);
		assertThat(phiVar.getUseList()).allMatch(use -> use.getInitType().equals(schemaType));
	}

	@Test
	void inferNullableArrayPhiFromArrayLengthUse() {
		ArgType arrayType = ArgType.array(ArgType.object("test.ObserveOp"));
		CodeVar flowCodeVar = new CodeVar();

		RegisterArg nullAssign = InsnArg.reg(0, ArgType.UNKNOWN);
		SSAVar nullVar = new SSAVar(0, 0, nullAssign);
		nullVar.setCodeVar(flowCodeVar);
		InsnNode nullInsn = new InsnNode(InsnType.CONST, 1);
		nullInsn.setResult(nullAssign);
		nullInsn.addArg(LiteralArg.make(0, ArgType.UNKNOWN));

		RegisterArg arraySource = makeKnownSource(1, arrayType);
		RegisterArg arrayAssign = InsnArg.reg(0, ArgType.UNKNOWN_OBJECT);
		SSAVar arrayVar = new SSAVar(0, 1, arrayAssign);
		arrayVar.setCodeVar(flowCodeVar);
		InsnNode arrayMove = new InsnNode(InsnType.MOVE, 1);
		arrayMove.setResult(arrayAssign);
		arrayMove.addArg(arraySource.duplicate());

		RegisterArg phiAssign = InsnArg.reg(0, ArgType.UNKNOWN);
		SSAVar phiVar = new SSAVar(0, 2, phiAssign);
		phiVar.setCodeVar(flowCodeVar);
		PhiInsn phiInsn = new PhiInsn(0, 2);
		phiInsn.setResult(phiAssign);
		phiInsn.bindArg(nullAssign.duplicate(), new BlockNode(0, 0, 0));
		phiInsn.bindArg(arrayAssign.duplicate(), new BlockNode(1, 0, 0));
		IfNode nullCheck = new IfNode(IfOp.NE, -1, phiAssign.duplicate(), LiteralArg.make(0, ArgType.INT));
		InsnNode arrayLength = new InsnNode(InsnType.ARRAY_LENGTH, 1);
		arrayLength.addArg(phiAssign.duplicate());

		FinishTypeInference.repairLateReferenceNullFlows(List.of(nullVar, arrayVar, phiVar));

		assertThat(flowCodeVar.getType()).isEqualTo(arrayType);
		assertThat(nullInsn.getArg(0).getType()).isEqualTo(arrayType);
		assertThat(nullCheck.getArg(1).getType()).isEqualTo(arrayType);
		assertThat(arrayLength.getArg(0).getType()).isEqualTo(arrayType);
	}

	@Test
	void preserveExactArrayAcrossErasedGenericUse() {
		ArgType arrayType = ArgType.array(ArgType.BYTE);
		CodeVar codeVar = new CodeVar();
		RegisterArg arrayAssign = InsnArg.reg(0, arrayType);
		SSAVar arrayVar = new SSAVar(0, 0, arrayAssign);
		arrayVar.setCodeVar(codeVar);
		InsnNode newArray = new InsnNode(InsnType.NEW_ARRAY, 1);
		newArray.setResult(arrayAssign);
		newArray.addArg(InsnArg.lit(16, ArgType.INT));

		RegisterArg exactUse = arrayAssign.duplicate();
		exactUse.forceSetInitType(arrayType);
		InsnNode arrayLength = new InsnNode(InsnType.ARRAY_LENGTH, 1);
		arrayLength.addArg(exactUse);
		RegisterArg erasedUse = arrayAssign.duplicate();
		erasedUse.forceSetInitType(ArgType.OBJECT);
		InsnNode erasedGenericUse = new InsnNode(InsnType.RETURN, 1);
		erasedGenericUse.addArg(erasedUse);

		FinishTypeInference.repairLateExactArrayFlows(List.of(arrayVar));

		assertThat(codeVar.getType()).isEqualTo(arrayType);
		assertThat(arrayVar.getUseList()).allMatch(use -> use.getInitType().equals(arrayType));
	}

	@Test
	void recoverLateIntPhiGroupFromExactFieldLoad() {
		RootNode root = new RootNode(new JadxArgs());
		CodeVar codeVar = new CodeVar();

		ArgType stateType = ArgType.object("test.State");
		RegisterArg receiver = makeKnownSource(1, stateType);
		RegisterArg fieldAssign = InsnArg.reg(0, ArgType.INT);
		SSAVar fieldVar = new SSAVar(0, 0, fieldAssign);
		fieldVar.setCodeVar(codeVar);
		FieldInfo spillField = FieldInfo.from(root, ClassInfo.fromType(root, stateType), "I$0", ArgType.INT);
		IndexInsnNode fieldGet = new IndexInsnNode(InsnType.IGET, spillField, 1);
		fieldGet.setResult(fieldAssign);
		fieldGet.addArg(receiver.duplicate());

		RegisterArg phiAssign = InsnArg.reg(0, ArgType.UNKNOWN);
		SSAVar phiVar = new SSAVar(0, 1, phiAssign);
		phiVar.setCodeVar(codeVar);
		PhiInsn phiInsn = new PhiInsn(0, 1);
		phiInsn.setResult(phiAssign);
		phiInsn.bindArg(fieldAssign.duplicate(), new BlockNode(0, 0, 0));

		RegisterArg moveAssign = InsnArg.reg(0, ArgType.UNKNOWN);
		SSAVar moveVar = new SSAVar(0, 2, moveAssign);
		moveVar.setCodeVar(codeVar);
		InsnNode move = new InsnNode(InsnType.MOVE, 1);
		move.setResult(moveAssign);
		move.addArg(phiAssign.duplicate());
		codeVar.setSsaVars(List.of(fieldVar, phiVar, moveVar));

		FinishTypeInference.repairLateCoroutineIntSpillFlows(List.of(fieldVar, phiVar, moveVar));

		assertThat(codeVar.getType()).isEqualTo(ArgType.INT);
		assertThat(List.of(fieldVar, phiVar, moveVar))
				.allMatch(var -> var.getTypeInfo().getType().equals(ArgType.INT));
	}

	@Test
	void rejectLatePrimitivePhiGroupWithConflictingExactType() {
		CodeVar codeVar = new CodeVar();
		RegisterArg intAssign = InsnArg.reg(0, ArgType.INT);
		SSAVar intVar = new SSAVar(0, 0, intAssign);
		intVar.setType(ArgType.INT);
		intVar.setCodeVar(codeVar);
		RegisterArg floatAssign = InsnArg.reg(0, ArgType.FLOAT);
		SSAVar floatVar = new SSAVar(0, 1, floatAssign);
		floatVar.setType(ArgType.FLOAT);
		floatVar.setCodeVar(codeVar);

		FinishTypeInference.repairLateCoroutineIntSpillFlows(List.of(intVar, floatVar));

		assertThat(codeVar.getType()).isNull();
	}

	@Test
	void detectBooleanMergedWithCoroutineIntSpill() {
		RootNode root = new RootNode(new JadxArgs());
		ArgType stateType = ArgType.object("test.State");
		RegisterArg receiver = makeKnownSource(3, stateType);

		RegisterArg spillAssign = InsnArg.reg(1, ArgType.INT);
		SSAVar spillVar = new SSAVar(1, 0, spillAssign);
		CodeVar spillCodeVar = new CodeVar();
		spillCodeVar.setType(ArgType.INT);
		spillVar.setCodeVar(spillCodeVar);
		FieldInfo spillField = FieldInfo.from(root, ClassInfo.fromType(root, stateType), "I$0", ArgType.INT);
		IndexInsnNode fieldGet = new IndexInsnNode(InsnType.IGET, spillField, 1);
		fieldGet.setResult(spillAssign);
		fieldGet.addArg(receiver.duplicate());

		RegisterArg booleanAssign = InsnArg.reg(2, ArgType.BOOLEAN);
		SSAVar booleanVar = new SSAVar(2, 0, booleanAssign);
		CodeVar booleanCodeVar = new CodeVar();
		booleanCodeVar.setType(ArgType.BOOLEAN);
		booleanVar.setCodeVar(booleanCodeVar);
		InsnNode booleanCall = new InsnNode(InsnType.INVOKE, 0);
		booleanCall.setResult(booleanAssign);

		CodeVar mergedCodeVar = new CodeVar();
		RegisterArg intMoveAssign = InsnArg.reg(0, ArgType.UNKNOWN);
		SSAVar intMoveVar = new SSAVar(0, 0, intMoveAssign);
		intMoveVar.setCodeVar(mergedCodeVar);
		InsnNode intMove = new InsnNode(InsnType.MOVE, 1);
		intMove.setResult(intMoveAssign);
		intMove.addArg(spillAssign.duplicate());
		RegisterArg booleanMoveAssign = InsnArg.reg(0, ArgType.UNKNOWN);
		SSAVar booleanMoveVar = new SSAVar(0, 1, booleanMoveAssign);
		booleanMoveVar.setCodeVar(mergedCodeVar);
		InsnNode booleanMove = new InsnNode(InsnType.MOVE, 1);
		booleanMove.setResult(booleanMoveAssign);
		booleanMove.addArg(booleanAssign.duplicate());
		booleanMove.add(AFlag.SYNTHETIC);
		RegisterArg phiAssign = InsnArg.reg(0, ArgType.UNKNOWN);
		SSAVar phiVar = new SSAVar(0, 2, phiAssign);
		phiVar.setCodeVar(mergedCodeVar);
		PhiInsn phiInsn = new PhiInsn(0, 2);
		phiInsn.setResult(phiAssign);
		phiInsn.bindArg(intMoveAssign.duplicate(), new BlockNode(0, 0, 0));
		phiInsn.bindArg(booleanMoveAssign.duplicate(), new BlockNode(1, 0, 0));
		new IfNode(IfOp.NE, -1, phiAssign.duplicate(), LiteralArg.make(0, ArgType.UNKNOWN));
		mergedCodeVar.setSsaVars(List.of(intMoveVar, booleanMoveVar, phiVar));

		assertThat(FinishTypeInference.collectCoroutineBooleanIntSpillMoves(
				null, mergedCodeVar, List.of(intMoveVar, booleanMoveVar, phiVar)))
						.containsExactly(booleanMove);
	}

	@Test
	void detectBooleanLoopWithIntegerBoundaries() {
		RegisterArg intSource = makeKnownSource(3, ArgType.INT);
		RegisterArg objectSource = makeKnownSource(4, ArgType.OBJECT);
		CodeVar mergedCodeVar = new CodeVar();

		RegisterArg booleanAssign = InsnArg.reg(0, ArgType.UNKNOWN);
		SSAVar booleanVar = new SSAVar(0, 0, booleanAssign);
		booleanVar.setCodeVar(mergedCodeVar);
		IndexInsnNode instanceOf = new IndexInsnNode(InsnType.INSTANCE_OF, ArgType.STRING, 1);
		instanceOf.setResult(booleanAssign);
		instanceOf.addArg(objectSource.duplicate());

		RegisterArg inputAssign = InsnArg.reg(0, ArgType.UNKNOWN);
		SSAVar inputVar = new SSAVar(0, 1, inputAssign);
		inputVar.setCodeVar(mergedCodeVar);
		InsnNode intInputMove = new InsnNode(InsnType.MOVE, 1);
		intInputMove.setResult(inputAssign);
		intInputMove.addArg(intSource.duplicate());
		intInputMove.add(AFlag.SYNTHETIC);

		RegisterArg phiAssign = InsnArg.reg(0, ArgType.UNKNOWN);
		SSAVar phiVar = new SSAVar(0, 2, phiAssign);
		phiVar.setCodeVar(mergedCodeVar);
		PhiInsn phiInsn = new PhiInsn(0, 2);
		phiInsn.setResult(phiAssign);
		phiInsn.bindArg(booleanAssign.duplicate(), new BlockNode(0, 0, 0));
		phiInsn.bindArg(inputAssign.duplicate(), new BlockNode(1, 0, 0));
		new IfNode(IfOp.NE, -1, phiAssign.duplicate(), LiteralArg.make(0, ArgType.UNKNOWN));

		RegisterArg outputAssign = InsnArg.reg(1, ArgType.INT);
		SSAVar outputVar = new SSAVar(1, 0, outputAssign);
		CodeVar outputCodeVar = new CodeVar();
		outputCodeVar.setType(ArgType.INT);
		outputVar.setCodeVar(outputCodeVar);
		InsnNode intOutputMove = new InsnNode(InsnType.MOVE, 1);
		intOutputMove.setResult(outputAssign);
		intOutputMove.addArg(phiAssign.duplicate());
		intOutputMove.add(AFlag.SYNTHETIC);
		mergedCodeVar.setSsaVars(List.of(booleanVar, inputVar, phiVar));

		FinishTypeInference.LateBooleanIntLoopConversions conversions =
				FinishTypeInference.collectLateBooleanIntLoopConversions(
						null, mergedCodeVar, List.of(booleanVar, inputVar, phiVar));

		assertThat(conversions).isNotNull();
		assertThat(conversions.intInputs).containsExactly(intInputMove);
		assertThat(conversions.intOutputs).containsExactly(intOutputMove);
	}

	@Test
	void detectBooleanPhiWithClosedZeroOneIntegerLoopInput() {
		ClosedBooleanIntPhiFixture fixture = new ClosedBooleanIntPhiFixture(0);

		assertThat(FinishTypeInference.isClosedZeroOneIntFlow(fixture.intLoopResult.getAssign())).isTrue();
		assertThat(FinishTypeInference.collectClosedBooleanIntPhiInput(
				null, fixture.mergedCodeVar, fixture.mergedCodeVar.getSsaVars()))
						.isSameAs(fixture.intInputMove);
	}

	@Test
	void rejectBooleanPhiWithNonBooleanIntegerLoopInput() {
		ClosedBooleanIntPhiFixture fixture = new ClosedBooleanIntPhiFixture(2);

		assertThat(FinishTypeInference.isClosedZeroOneIntFlow(fixture.intLoopResult.getAssign())).isFalse();
		assertThat(FinishTypeInference.collectClosedBooleanIntPhiInput(
				null, fixture.mergedCodeVar, fixture.mergedCodeVar.getSsaVars())).isNull();
	}

	@Test
	void splitBooleanContinuationSpillFromReferenceLifetime() {
		RootNode root = new RootNode(new JadxArgs());
		ArgType continuationType = ArgType.object("test.ReadScopeContinuation");
		ArgType functionType = ArgType.object("kotlin.jvm.functions.Function3");
		RegisterArg continuation = makeKnownSource(2, continuationType);

		CodeVar referenceCodeVar = new CodeVar();
		referenceCodeVar.setType(functionType);
		RegisterArg referenceAssign = InsnArg.reg(0, functionType);
		SSAVar referenceVar = new SSAVar(0, 0, referenceAssign);
		referenceVar.setCodeVar(referenceCodeVar);

		RegisterArg fieldResult = InsnArg.reg(1, ArgType.BOOLEAN);
		FieldInfo spillField = FieldInfo.from(
				root, ClassInfo.fromType(root, continuationType), "Z$0", ArgType.BOOLEAN);
		IndexInsnNode fieldGet = new IndexInsnNode(InsnType.IGET, spillField, 1);
		fieldGet.setResult(fieldResult);
		fieldGet.addArg(continuation.duplicate());

		RegisterArg spillAssign = InsnArg.reg(0, ArgType.BOOLEAN);
		SSAVar spillVar = new SSAVar(0, 1, spillAssign);
		spillVar.setCodeVar(referenceCodeVar);
		IndexInsnNode invalidCast = new IndexInsnNode(InsnType.CHECK_CAST, functionType, 1);
		invalidCast.setResult(spillAssign);
		invalidCast.addArg(InsnArg.wrapInsnIntoArg(fieldGet));
		referenceCodeVar.setSsaVars(List.of(referenceVar, spillVar));

		CodeVar booleanFlowCodeVar = new CodeVar();
		RegisterArg flowAssign = InsnArg.reg(1, ArgType.UNKNOWN);
		SSAVar flowVar = new SSAVar(1, 0, flowAssign);
		flowVar.setCodeVar(booleanFlowCodeVar);
		InsnNode spillMove = new InsnNode(InsnType.MOVE, 1);
		spillMove.setResult(flowAssign);
		spillMove.addArg(spillAssign.duplicate());
		spillMove.add(AFlag.SYNTHETIC);
		new IfNode(IfOp.NE, -1, flowAssign.duplicate(), LiteralArg.make(0, ArgType.UNKNOWN));
		booleanFlowCodeVar.setSsaVars(List.of(flowVar));

		int splitCount = FinishTypeInference.splitCoroutineBooleanSpillCasts(
				Map.of(referenceCodeVar, List.of(referenceVar, spillVar)));
		FinishTypeInference.repairLateBooleanBitFlows(List.of(flowVar));

		assertThat(splitCount).isOne();
		assertThat(spillVar.getCodeVar()).isNotSameAs(referenceCodeVar);
		assertThat(spillVar.getCodeVar().getType()).isEqualTo(ArgType.BOOLEAN);
		assertThat(invalidCast.getIndex()).isEqualTo(ArgType.BOOLEAN);
		assertThat(referenceCodeVar.getSsaVars()).containsExactly(referenceVar);
		assertThat(booleanFlowCodeVar.getType()).isEqualTo(ArgType.BOOLEAN);
	}

	@Test
	void repairLateBooleanTernaryFlow() {
		RegisterArg condition = InsnArg.reg(1, ArgType.BOOLEAN);
		SSAVar conditionVar = new SSAVar(1, 0, condition);
		CodeVar conditionCodeVar = new CodeVar();
		conditionCodeVar.setType(ArgType.BOOLEAN);
		conditionVar.setCodeVar(conditionCodeVar);

		RegisterArg result = InsnArg.reg(0, ArgType.UNKNOWN);
		SSAVar resultVar = new SSAVar(0, 0, result);
		CodeVar resultCodeVar = new CodeVar();
		resultVar.setCodeVar(resultCodeVar);

		IfNode conditionInsn = new IfNode(IfOp.EQ, -1, condition.duplicate(), LiteralArg.litTrue());
		new TernaryInsn(IfCondition.fromIfNode(conditionInsn), result, LiteralArg.litTrue(), LiteralArg.litFalse());
		new IfNode(IfOp.NE, -1, result.duplicate(), LiteralArg.make(0, ArgType.INT));

		FinishTypeInference.repairLateBooleanBitFlows(List.of(resultVar));

		assertThat(resultCodeVar.getType()).isEqualTo(ArgType.BOOLEAN);
	}

	@Test
	void detectTerminalCoroutineBooleanTernary() {
		RegisterArg condition = InsnArg.reg(1, ArgType.BOOLEAN);
		SSAVar conditionVar = new SSAVar(1, 0, condition);
		CodeVar conditionCodeVar = new CodeVar();
		conditionCodeVar.setType(ArgType.BOOLEAN);
		conditionVar.setCodeVar(conditionCodeVar);

		RegisterArg result = InsnArg.reg(0, ArgType.UNKNOWN);
		SSAVar resultVar = new SSAVar(0, 0, result);
		resultVar.setCodeVar(new CodeVar());
		IfNode conditionInsn = new IfNode(IfOp.EQ, -1, condition.duplicate(), LiteralArg.litTrue());
		new TernaryInsn(IfCondition.fromIfNode(conditionInsn), result, LiteralArg.litTrue(), LiteralArg.litFalse());
		new IfNode(IfOp.NE, -1, result.duplicate(), LiteralArg.make(0, ArgType.UNKNOWN));

		assertThat(FinishTypeInference.getExactBooleanTernaryTerminalType(resultVar)).isEqualTo(ArgType.BOOLEAN);

		CodeVar mixedCodeVar = resultVar.getCodeVar();
		mixedCodeVar.setDeclared(true);
		RegisterArg floatAssign = InsnArg.reg(0, ArgType.FLOAT);
		SSAVar floatVar = new SSAVar(0, 1, floatAssign);
		floatVar.setCodeVar(mixedCodeVar);
		InsnNode floatMove = new InsnNode(InsnType.MOVE, 1);
		floatMove.setResult(floatAssign);
		floatMove.addArg(InsnArg.reg(2, ArgType.FLOAT));
		mixedCodeVar.setSsaVars(List.of(resultVar, floatVar));
		Map<CodeVar, List<SSAVar>> groups = new IdentityHashMap<>();
		groups.put(mixedCodeVar, List.of(resultVar, floatVar));

		assertThat(FinishTypeInference.splitTerminalCoroutineBooleanCodeVars(groups)).isOne();
		assertThat(resultVar.getCodeVar().getType()).isEqualTo(ArgType.BOOLEAN);
		assertThat(resultVar.getCodeVar().isDeclared()).isFalse();
		assertThat(mixedCodeVar.isDeclared()).isTrue();
		assertThat(mixedCodeVar.getSsaVars()).containsExactly(floatVar);
	}

	@Test
	void recoverCoroutineObjectResultAfterTerminalBooleanSplit() {
		RootNode root = new RootNode(new JadxArgs());
		RegisterArg resumedObject = makeKnownSource(4, ArgType.OBJECT);
		RegisterArg booleanValue = makeKnownSource(5, ArgType.BOOLEAN);

		RegisterArg suspendedAssign = InsnArg.reg(3, ArgType.OBJECT);
		SSAVar suspendedVar = new SSAVar(3, 0, suspendedAssign);
		CodeVar suspendedCodeVar = new CodeVar();
		suspendedCodeVar.setType(ArgType.OBJECT);
		suspendedVar.setCodeVar(suspendedCodeVar);
		MethodInfo suspendedMth = MethodInfo.fromDetails(
				root, ClassInfo.fromName(root, "kotlin.coroutines.intrinsics.IntrinsicsKt"),
				"getCOROUTINE_SUSPENDED", List.of(), ArgType.OBJECT);
		InvokeNode suspendedCall = new InvokeNode(suspendedMth, InvokeType.STATIC, 0);
		suspendedCall.setResult(suspendedAssign);

		CodeVar objectCodeVar = new CodeVar();
		RegisterArg moveAssign = InsnArg.reg(0, ArgType.UNKNOWN);
		SSAVar moveVar = new SSAVar(0, 0, moveAssign);
		moveVar.setCodeVar(objectCodeVar);
		InsnNode objectMove = new InsnNode(InsnType.MOVE, 1);
		objectMove.setResult(moveAssign);
		objectMove.addArg(resumedObject.duplicate());

		RegisterArg firstPhiAssign = InsnArg.reg(0, ArgType.UNKNOWN);
		SSAVar firstPhiVar = new SSAVar(0, 1, firstPhiAssign);
		firstPhiVar.setCodeVar(objectCodeVar);
		PhiInsn firstPhi = new PhiInsn(0, 1);
		firstPhi.setResult(firstPhiAssign);
		firstPhi.bindArg(moveAssign.duplicate(), new BlockNode(0, 0, 0));
		firstPhi.bindArg(booleanValue.duplicate(), new BlockNode(1, 0, 0));
		firstPhi.add(AFlag.DONT_GENERATE);

		RegisterArg invokeAssign = InsnArg.reg(0, ArgType.OBJECT);
		SSAVar invokeVar = new SSAVar(0, 2, invokeAssign);
		invokeVar.setCodeVar(objectCodeVar);
		MethodInfo awaitMth = MethodInfo.fromDetails(
				root, ClassInfo.fromName(root, "test.AwaitScope"), "await", List.of(), ArgType.OBJECT);
		InvokeNode awaitCall = new InvokeNode(awaitMth, InvokeType.STATIC, 0);
		awaitCall.setResult(invokeAssign);
		new IfNode(IfOp.EQ, -1, invokeAssign.duplicate(), suspendedAssign.duplicate());

		RegisterArg finalPhiAssign = InsnArg.reg(0, ArgType.UNKNOWN);
		SSAVar finalPhiVar = new SSAVar(0, 3, finalPhiAssign);
		finalPhiVar.setCodeVar(objectCodeVar);
		PhiInsn finalPhi = new PhiInsn(0, 3);
		finalPhi.setResult(finalPhiAssign);
		finalPhi.bindArg(firstPhiAssign.duplicate(), new BlockNode(2, 0, 0));
		finalPhi.bindArg(invokeAssign.duplicate(), new BlockNode(3, 0, 0));
		finalPhi.add(AFlag.DONT_GENERATE);
		IndexInsnNode pointerCast = new IndexInsnNode(
				InsnType.CHECK_CAST, ArgType.object("test.PointerEvent"), 1);
		pointerCast.addArg(finalPhiAssign.duplicate());
		objectCodeVar.setSsaVars(List.of(moveVar, firstPhiVar, invokeVar, finalPhiVar));

		FinishTypeInference.repairLateCoroutineObjectResultFlows(
				List.of(moveVar, firstPhiVar, invokeVar, finalPhiVar));

		assertThat(objectCodeVar.getType()).isEqualTo(ArgType.OBJECT);
		assertThat(finalPhiAssign.getInitType()).isEqualTo(ArgType.OBJECT);
	}

	@Test
	void recoverCoroutineObjectResultFromContinuationResultField() {
		RootNode root = new RootNode(new JadxArgs());
		ArgType continuationType = ArgType.object("test.AwaitContinuation");
		RegisterArg continuation = makeKnownSource(4, continuationType);
		RegisterArg booleanValue = makeKnownSource(5, ArgType.BOOLEAN);

		RegisterArg suspendedAssign = InsnArg.reg(3, ArgType.OBJECT);
		SSAVar suspendedVar = new SSAVar(3, 0, suspendedAssign);
		CodeVar suspendedCodeVar = new CodeVar();
		suspendedCodeVar.setType(ArgType.OBJECT);
		suspendedVar.setCodeVar(suspendedCodeVar);
		MethodInfo suspendedMth = MethodInfo.fromDetails(
				root, ClassInfo.fromName(root, "kotlin.coroutines.intrinsics.IntrinsicsKt"),
				"getCOROUTINE_SUSPENDED", List.of(), ArgType.OBJECT);
		InvokeNode suspendedCall = new InvokeNode(suspendedMth, InvokeType.STATIC, 0);
		suspendedCall.setResult(suspendedAssign);

		CodeVar objectCodeVar = new CodeVar();
		RegisterArg resultAssign = InsnArg.reg(0, ArgType.OBJECT);
		SSAVar resultVar = new SSAVar(0, 0, resultAssign);
		resultVar.setCodeVar(objectCodeVar);
		FieldInfo resultField = FieldInfo.from(
				root, ClassInfo.fromType(root, continuationType), "result", ArgType.OBJECT);
		IndexInsnNode resultGet = new IndexInsnNode(InsnType.IGET, resultField, 1);
		resultGet.setResult(resultAssign);
		resultGet.addArg(continuation.duplicate());
		MethodInfo throwMth = MethodInfo.fromDetails(
				root, ClassInfo.fromName(root, "kotlin.ResultKt"), "throwOnFailure",
				List.of(ArgType.OBJECT), ArgType.VOID);
		RegisterArg throwUse = resultAssign.duplicate();
		throwUse.forceSetInitType(ArgType.OBJECT);
		new InvokeNode(throwMth, InvokeType.STATIC, 1).addArg(throwUse);

		RegisterArg firstPhiAssign = InsnArg.reg(0, ArgType.UNKNOWN);
		SSAVar firstPhiVar = new SSAVar(0, 1, firstPhiAssign);
		firstPhiVar.setCodeVar(objectCodeVar);
		PhiInsn firstPhi = new PhiInsn(0, 1);
		firstPhi.setResult(firstPhiAssign);
		firstPhi.bindArg(resultAssign.duplicate(), new BlockNode(0, 0, 0));
		firstPhi.bindArg(booleanValue.duplicate(), new BlockNode(1, 0, 0));
		firstPhi.add(AFlag.DONT_GENERATE);

		RegisterArg invokeAssign = InsnArg.reg(0, ArgType.OBJECT);
		SSAVar invokeVar = new SSAVar(0, 2, invokeAssign);
		invokeVar.setCodeVar(objectCodeVar);
		MethodInfo awaitMth = MethodInfo.fromDetails(
				root, ClassInfo.fromName(root, "test.AwaitScope"), "await", List.of(), ArgType.OBJECT);
		InvokeNode awaitCall = new InvokeNode(awaitMth, InvokeType.STATIC, 0);
		awaitCall.setResult(invokeAssign);
		new IfNode(IfOp.EQ, -1, invokeAssign.duplicate(), suspendedAssign.duplicate());

		RegisterArg finalPhiAssign = InsnArg.reg(0, ArgType.UNKNOWN);
		SSAVar finalPhiVar = new SSAVar(0, 3, finalPhiAssign);
		finalPhiVar.setCodeVar(objectCodeVar);
		PhiInsn finalPhi = new PhiInsn(0, 3);
		finalPhi.setResult(finalPhiAssign);
		finalPhi.bindArg(firstPhiAssign.duplicate(), new BlockNode(2, 0, 0));
		finalPhi.bindArg(invokeAssign.duplicate(), new BlockNode(3, 0, 0));
		finalPhi.add(AFlag.DONT_GENERATE);
		IndexInsnNode pointerCast = new IndexInsnNode(
				InsnType.CHECK_CAST, ArgType.object("test.PointerEvent"), 1);
		pointerCast.addArg(finalPhiAssign.duplicate());
		objectCodeVar.setSsaVars(List.of(resultVar, firstPhiVar, invokeVar, finalPhiVar));

		FinishTypeInference.repairLateCoroutineObjectResultFlows(
				List.of(resultVar, firstPhiVar, invokeVar, finalPhiVar));

		assertThat(objectCodeVar.getType()).isEqualTo(ArgType.OBJECT);
		assertThat(resultAssign.getInitType()).isEqualTo(ArgType.OBJECT);
		assertThat(finalPhiAssign.getInitType()).isEqualTo(ArgType.OBJECT);
	}

	@Test
	void recoverObjectForPhiMergingDifferentArrayTypes() {
		RegisterArg objectArray = makeKnownSource(1, ArgType.array(ArgType.OBJECT));
		RegisterArg intArray = makeKnownSource(2, ArgType.array(ArgType.INT));
		CodeVar mixedCodeVar = new CodeVar();

		RegisterArg objectMoveAssign = InsnArg.reg(0, ArgType.UNKNOWN_OBJECT);
		SSAVar objectMoveVar = new SSAVar(0, 0, objectMoveAssign);
		objectMoveVar.setCodeVar(mixedCodeVar);
		InsnNode objectMove = new InsnNode(InsnType.MOVE, 1);
		objectMove.setResult(objectMoveAssign);
		objectMove.addArg(objectArray.duplicate());

		RegisterArg intMoveAssign = InsnArg.reg(0, ArgType.UNKNOWN_OBJECT);
		SSAVar intMoveVar = new SSAVar(0, 1, intMoveAssign);
		intMoveVar.setCodeVar(mixedCodeVar);
		InsnNode intMove = new InsnNode(InsnType.MOVE, 1);
		intMove.setResult(intMoveAssign);
		intMove.addArg(intArray.duplicate());

		RegisterArg phiAssign = InsnArg.reg(0, ArgType.UNKNOWN_OBJECT);
		SSAVar phiVar = new SSAVar(0, 2, phiAssign);
		phiVar.setCodeVar(mixedCodeVar);
		PhiInsn phi = new PhiInsn(0, 2);
		phi.setResult(phiAssign);
		phi.bindArg(objectMoveAssign.duplicate(), new BlockNode(0, 0, 0));
		phi.bindArg(intMoveAssign.duplicate(), new BlockNode(1, 0, 0));
		phi.add(AFlag.DONT_GENERATE);
		IndexInsnNode objectArrayCast = new IndexInsnNode(
				InsnType.CHECK_CAST, ArgType.array(ArgType.OBJECT), 1);
		objectArrayCast.addArg(phiAssign.duplicate());
		mixedCodeVar.setSsaVars(List.of(objectMoveVar, intMoveVar, phiVar));

		FinishTypeInference.repairLateMixedReferenceObjectFlows(
				List.of(objectMoveVar, intMoveVar, phiVar));

		assertThat(mixedCodeVar.getType()).isEqualTo(ArgType.OBJECT);
		assertThat(phiAssign.getInitType()).isEqualTo(ArgType.OBJECT);
	}

	@Test
	void ignoreUseInNonGeneratedInsn() {
		SSAVar var = makeVarWithUse(true);

		assertThat(FinishTypeInference.hasGeneratedUse(var)).isFalse();
	}

	@Test
	void replacePrimitiveCompareValueOnExceptionCleanupEdgeWithNull() {
		CodeVar compareCodeVar = new CodeVar();
		RegisterArg compareAssign = InsnArg.reg(0, ArgType.UNKNOWN);
		SSAVar compareVar = new SSAVar(0, 0, compareAssign);
		compareVar.setCodeVar(compareCodeVar);
		compareCodeVar.setSsaVars(List.of(compareVar));
		InsnNode compareInsn = new InsnNode(InsnType.CMP_L, 2);
		compareInsn.setResult(compareAssign);
		compareInsn.addArg(InsnArg.lit(1, ArgType.LONG));
		compareInsn.addArg(InsnArg.lit(0, ArgType.LONG));

		RegisterArg ifUse = compareAssign.duplicate();
		compareVar.use(ifUse);
		new IfNode(IfOp.NE, -1, ifUse, LiteralArg.make(0, ArgType.INT));

		ArgType closeableType = ArgType.object("java.io.Closeable");
		CodeVar closeableCodeVar = new CodeVar();
		closeableCodeVar.setType(closeableType);
		RegisterArg closeableAssign = InsnArg.reg(1, ArgType.UNKNOWN_OBJECT);
		SSAVar closeableVar = new SSAVar(1, 0, closeableAssign);
		closeableVar.setCodeVar(closeableCodeVar);
		closeableCodeVar.setSsaVars(List.of(closeableVar));
		InsnNode handlerMove = new InsnNode(InsnType.MOVE, 1);
		handlerMove.setResult(closeableAssign);
		RegisterArg handlerUse = compareAssign.duplicate();
		handlerUse.forceSetInitType(ArgType.UNKNOWN_OBJECT);
		compareVar.use(handlerUse);
		handlerMove.addArg(handlerUse);

		assertThat(FinishTypeInference.repairLateExceptionPrimitiveToReferenceMoves(
				List.of(compareVar, closeableVar), insn -> insn == handlerMove)).isOne();
		assertThat(compareCodeVar.getType()).isEqualTo(ArgType.INT);
		assertThat(ifUse.getInitType()).isEqualTo(ArgType.INT);
		assertThat(handlerMove.getArg(0).isZeroConst()).isTrue();
		assertThat(handlerMove.getArg(0).getType()).isEqualTo(closeableType);
	}

	@Test
	void replaceCoroutineLabelOnExceptionCleanupEdgeWithNull() {
		RootNode root = new RootNode(new JadxArgs());
		ArgType continuationType = ArgType.object("test.StateContinuation");
		CodeVar labelCodeVar = new CodeVar();
		RegisterArg labelAssign = InsnArg.reg(0, ArgType.UNKNOWN);
		SSAVar labelVar = new SSAVar(0, 0, labelAssign);
		labelVar.setCodeVar(labelCodeVar);
		labelCodeVar.setSsaVars(List.of(labelVar));
		FieldInfo labelField = FieldInfo.from(
				root, ClassInfo.fromType(root, continuationType), "label", ArgType.INT);
		IndexInsnNode labelGet = new IndexInsnNode(InsnType.IGET, labelField, 1);
		labelGet.setResult(labelAssign);
		labelGet.addArg(InsnArg.reg(2, continuationType));

		RegisterArg switchUse = labelAssign.duplicate();
		labelVar.use(switchUse);
		InsnNode switchInsn = new InsnNode(InsnType.SWITCH, 1);
		switchInsn.addArg(switchUse);

		ArgType stateType = ArgType.object("test.PagerState");
		CodeVar stateCodeVar = new CodeVar();
		stateCodeVar.setType(stateType);
		RegisterArg stateAssign = InsnArg.reg(1, ArgType.UNKNOWN_OBJECT);
		SSAVar stateVar = new SSAVar(1, 0, stateAssign);
		stateVar.setCodeVar(stateCodeVar);
		stateCodeVar.setSsaVars(List.of(stateVar));
		InsnNode handlerMove = new InsnNode(InsnType.MOVE, 1);
		handlerMove.setResult(stateAssign);
		RegisterArg handlerUse = labelAssign.duplicate();
		handlerUse.forceSetInitType(ArgType.UNKNOWN_OBJECT);
		labelVar.use(handlerUse);
		handlerMove.addArg(handlerUse);

		assertThat(FinishTypeInference.repairLateExceptionPrimitiveToReferenceMoves(
				List.of(labelVar, stateVar), insn -> insn == handlerMove)).isOne();
		assertThat(labelCodeVar.getType()).isEqualTo(ArgType.INT);
		assertThat(switchUse.getInitType()).isEqualTo(ArgType.INT);
		assertThat(handlerMove.getArg(0).isZeroConst()).isTrue();
		assertThat(handlerMove.getArg(0).getType()).isEqualTo(stateType);
	}

	@Test
	void reconnectCoroutineLabelStaticCleanupArgToCloseablePhi() {
		RootNode root = new RootNode(new JadxArgs());
		ArgType continuationType = ArgType.object("test.StateContinuation");
		CodeVar labelCodeVar = new CodeVar();
		RegisterArg labelAssign = InsnArg.reg(5, ArgType.UNKNOWN);
		SSAVar labelVar = new SSAVar(5, 0, labelAssign);
		labelVar.setCodeVar(labelCodeVar);
		labelCodeVar.setSsaVars(List.of(labelVar));
		FieldInfo labelField = FieldInfo.from(
				root, ClassInfo.fromType(root, continuationType), "label", ArgType.INT);
		IndexInsnNode labelGet = new IndexInsnNode(InsnType.IGET, labelField, 1);
		labelGet.setResult(labelAssign);
		labelGet.addArg(InsnArg.reg(2, continuationType));

		RegisterArg branchUse = labelAssign.duplicate();
		labelVar.use(branchUse);
		new IfNode(IfOp.NE, -1, branchUse, LiteralArg.make(0, ArgType.INT));

		ArgType closeableType = ArgType.object("java.io.Closeable");
		CodeVar closeableCodeVar = new CodeVar();
		closeableCodeVar.setType(closeableType);
		RegisterArg firstCloseableAssign = InsnArg.reg(5, closeableType);
		SSAVar firstCloseableVar = new SSAVar(5, 1, firstCloseableAssign);
		firstCloseableVar.setCodeVar(closeableCodeVar);
		RegisterArg resumedCloseableAssign = InsnArg.reg(5, closeableType);
		SSAVar resumedCloseableVar = new SSAVar(5, 2, resumedCloseableAssign);
		resumedCloseableVar.setCodeVar(closeableCodeVar);
		RegisterArg closeablePhiAssign = InsnArg.reg(5, closeableType);
		SSAVar closeablePhiVar = new SSAVar(5, 3, closeablePhiAssign);
		closeablePhiVar.setCodeVar(closeableCodeVar);
		PhiInsn closeablePhi = new PhiInsn(5, 2);
		closeablePhi.setResult(closeablePhiAssign);
		closeablePhi.bindArg(firstCloseableAssign.duplicate(), new BlockNode(0, 0, 0));
		closeablePhi.bindArg(resumedCloseableAssign.duplicate(), new BlockNode(1, 0, 0));
		closeableCodeVar.setSsaVars(List.of(firstCloseableVar, resumedCloseableVar, closeablePhiVar));

		ArgType throwableType = ArgType.THROWABLE;
		MethodInfo closeFinallyMth = MethodInfo.fromDetails(
				root, ClassInfo.fromName(root, "kotlin.io.CloseableKt"), "closeFinally",
				List.of(closeableType, throwableType), ArgType.VOID);
		RegisterArg normalCloseableUse = closeablePhiAssign.duplicate();
		closeablePhiVar.use(normalCloseableUse);
		InvokeNode normalClose = new InvokeNode(closeFinallyMth, InvokeType.STATIC, 2);
		normalClose.addArg(normalCloseableUse);
		normalClose.addArg(InsnArg.reg(0, throwableType));

		RegisterArg handlerLabelUse = labelAssign.duplicate();
		handlerLabelUse.forceSetInitType(closeableType);
		labelVar.use(handlerLabelUse);
		InvokeNode handlerClose = new InvokeNode(closeFinallyMth, InvokeType.STATIC, 2);
		handlerClose.addArg(handlerLabelUse);
		handlerClose.addArg(InsnArg.reg(1, throwableType));

		assertThat(FinishTypeInference.repairLateExceptionPrimitiveToReferenceMoves(
				List.of(labelVar, firstCloseableVar, resumedCloseableVar, closeablePhiVar),
				insn -> insn == handlerClose)).isOne();
		assertThat(labelCodeVar.getType()).isEqualTo(ArgType.INT);
		assertThat(branchUse.getInitType()).isEqualTo(ArgType.INT);
		assertThat(handlerClose.getArg(0)).isInstanceOf(RegisterArg.class);
		assertThat(((RegisterArg) handlerClose.getArg(0)).getSVar()).isSameAs(closeablePhiVar);
	}

	@Test
	void reconnectInstanceOfHandlerReceiverToReferenceCleanupPhi() {
		RootNode root = new RootNode(new JadxArgs());
		ArgType collectorType = ArgType.object("test.SafeCollector");
		CodeVar booleanCodeVar = new CodeVar();
		RegisterArg instanceOfAssign = InsnArg.reg(2, ArgType.UNKNOWN);
		SSAVar instanceOfVar = new SSAVar(2, 0, instanceOfAssign);
		instanceOfVar.setCodeVar(booleanCodeVar);
		booleanCodeVar.setSsaVars(List.of(instanceOfVar));
		IndexInsnNode instanceOfInsn = new IndexInsnNode(InsnType.INSTANCE_OF, collectorType, 1);
		instanceOfInsn.setResult(instanceOfAssign);
		instanceOfInsn.addArg(InsnArg.reg(3, ArgType.OBJECT));
		RegisterArg branchUse = instanceOfAssign.duplicate();
		instanceOfVar.use(branchUse);
		new IfNode(IfOp.NE, -1, branchUse, LiteralArg.make(0, ArgType.INT));

		CodeVar collectorCodeVar = new CodeVar();
		collectorCodeVar.setType(collectorType);
		RegisterArg firstCollectorAssign = InsnArg.reg(2, collectorType);
		SSAVar firstCollectorVar = new SSAVar(2, 1, firstCollectorAssign);
		firstCollectorVar.setCodeVar(collectorCodeVar);
		RegisterArg resumedCollectorAssign = InsnArg.reg(2, collectorType);
		SSAVar resumedCollectorVar = new SSAVar(2, 2, resumedCollectorAssign);
		resumedCollectorVar.setCodeVar(collectorCodeVar);
		RegisterArg collectorPhiAssign = InsnArg.reg(2, collectorType);
		SSAVar collectorPhiVar = new SSAVar(2, 3, collectorPhiAssign);
		collectorPhiVar.setCodeVar(collectorCodeVar);
		PhiInsn collectorPhi = new PhiInsn(2, 2);
		collectorPhi.setResult(collectorPhiAssign);
		collectorPhi.bindArg(firstCollectorAssign.duplicate(), new BlockNode(0, 0, 0));
		collectorPhi.bindArg(resumedCollectorAssign.duplicate(), new BlockNode(1, 0, 0));
		MethodInfo releaseMth = MethodInfo.fromDetails(
				root, ClassInfo.fromType(root, collectorType), "releaseIntercepted", List.of(), ArgType.VOID);
		RegisterArg normalReceiver = collectorPhiAssign.duplicate();
		collectorPhiVar.use(normalReceiver);
		InvokeNode normalRelease = new InvokeNode(releaseMth, InvokeType.VIRTUAL, 1);
		normalRelease.addArg(normalReceiver);
		collectorCodeVar.setSsaVars(List.of(firstCollectorVar, resumedCollectorVar, collectorPhiVar));

		RegisterArg handlerReceiver = instanceOfAssign.duplicate();
		handlerReceiver.forceSetInitType(collectorType);
		instanceOfVar.use(handlerReceiver);
		InvokeNode handlerRelease = new InvokeNode(releaseMth, InvokeType.VIRTUAL, 1);
		handlerRelease.addArg(handlerReceiver);

		assertThat(FinishTypeInference.repairLateExceptionPrimitiveToReferenceMoves(
				List.of(instanceOfVar, firstCollectorVar, resumedCollectorVar, collectorPhiVar),
				insn -> insn == handlerRelease)).isOne();
		assertThat(booleanCodeVar.getType()).isEqualTo(ArgType.BOOLEAN);
		assertThat(branchUse.getInitType()).isEqualTo(ArgType.BOOLEAN);
		assertThat(handlerRelease.getInstanceArg()).isInstanceOf(RegisterArg.class);
		assertThat(((RegisterArg) handlerRelease.getInstanceArg()).getSVar()).isSameAs(collectorPhiVar);
	}

	@Test
	void splitInstanceOfFromMixedHandlerPhiAndReconnectCleanupReceiver() {
		RootNode root = new RootNode(new JadxArgs());
		ArgType collectorType = ArgType.object("test.SafeCollector");
		CodeVar mixedCodeVar = new CodeVar();
		RegisterArg instanceOfAssign = InsnArg.reg(2, ArgType.UNKNOWN);
		SSAVar instanceOfVar = new SSAVar(2, 1, instanceOfAssign);
		instanceOfVar.setCodeVar(mixedCodeVar);
		IndexInsnNode instanceOfInsn = new IndexInsnNode(InsnType.INSTANCE_OF, collectorType, 1);
		instanceOfInsn.setResult(instanceOfAssign);
		instanceOfInsn.addArg(InsnArg.reg(3, ArgType.OBJECT));
		RegisterArg branchUse = instanceOfAssign.duplicate();
		instanceOfVar.use(branchUse);
		new IfNode(IfOp.NE, -1, branchUse, LiteralArg.make(0, ArgType.INT));

		RegisterArg stateAssign = InsnArg.reg(2, ArgType.INT);
		SSAVar stateVar = new SSAVar(2, 0, stateAssign);
		CodeVar stateCodeVar = new CodeVar();
		stateCodeVar.setType(ArgType.INT);
		stateVar.setCodeVar(stateCodeVar);
		stateCodeVar.setSsaVars(List.of(stateVar));
		RegisterArg handlerPhiAssign = InsnArg.reg(2, ArgType.UNKNOWN);
		SSAVar handlerPhiVar = new SSAVar(2, 2, handlerPhiAssign);
		handlerPhiVar.setCodeVar(mixedCodeVar);
		PhiInsn handlerPhi = new PhiInsn(2, 2);
		handlerPhi.setResult(handlerPhiAssign);
		RegisterArg stateUse = stateAssign.duplicate();
		stateVar.use(stateUse);
		handlerPhi.bindArg(stateUse, new BlockNode(0, 0, 0));
		RegisterArg instanceOfUse = instanceOfAssign.duplicate();
		instanceOfVar.use(instanceOfUse);
		handlerPhi.bindArg(instanceOfUse, new BlockNode(1, 0, 0));
		mixedCodeVar.setSsaVars(List.of(handlerPhiVar, instanceOfVar));

		CodeVar collectorCodeVar = new CodeVar();
		collectorCodeVar.setType(collectorType);
		RegisterArg firstCollectorAssign = InsnArg.reg(6, collectorType);
		SSAVar firstCollectorVar = new SSAVar(6, 3, firstCollectorAssign);
		firstCollectorVar.setCodeVar(collectorCodeVar);
		RegisterArg resumedCollectorAssign = InsnArg.reg(6, collectorType);
		SSAVar resumedCollectorVar = new SSAVar(6, 4, resumedCollectorAssign);
		resumedCollectorVar.setCodeVar(collectorCodeVar);
		RegisterArg collectorPhiAssign = InsnArg.reg(6, collectorType);
		SSAVar collectorPhiVar = new SSAVar(6, 5, collectorPhiAssign);
		collectorPhiVar.setCodeVar(collectorCodeVar);
		PhiInsn collectorPhi = new PhiInsn(6, 2);
		collectorPhi.setResult(collectorPhiAssign);
		collectorPhi.bindArg(firstCollectorAssign.duplicate(), new BlockNode(2, 0, 0));
		collectorPhi.bindArg(resumedCollectorAssign.duplicate(), new BlockNode(3, 0, 0));
		MethodInfo releaseMth = MethodInfo.fromDetails(
				root, ClassInfo.fromType(root, collectorType), "releaseIntercepted", List.of(), ArgType.VOID);
		RegisterArg normalReceiver = collectorPhiAssign.duplicate();
		collectorPhiVar.use(normalReceiver);
		new InvokeNode(releaseMth, InvokeType.VIRTUAL, 1).addArg(normalReceiver);
		collectorCodeVar.setSsaVars(List.of(firstCollectorVar, resumedCollectorVar, collectorPhiVar));

		RegisterArg handlerReceiver = handlerPhiAssign.duplicate();
		handlerReceiver.forceSetInitType(collectorType);
		handlerPhiVar.use(handlerReceiver);
		InvokeNode handlerRelease = new InvokeNode(releaseMth, InvokeType.VIRTUAL, 1);
		handlerRelease.addArg(handlerReceiver);

		assertThat(FinishTypeInference.repairLateExceptionPrimitiveToReferenceMoves(
				List.of(stateVar, handlerPhiVar, instanceOfVar,
						firstCollectorVar, resumedCollectorVar, collectorPhiVar),
				insn -> insn == handlerRelease)).isOne();
		assertThat(instanceOfVar.getCodeVar()).isSameAs(mixedCodeVar);
		assertThat(mixedCodeVar.getType()).isEqualTo(ArgType.BOOLEAN);
		assertThat(mixedCodeVar.getSsaVars()).containsExactly(instanceOfVar);
		assertThat(handlerPhiVar.getCodeVar()).isNotSameAs(mixedCodeVar);
		assertThat(((RegisterArg) handlerRelease.getInstanceArg()).getSVar()).isSameAs(collectorPhiVar);
	}

	@Test
	void replaceKnownIntSyntheticExceptionCleanupMoveWithNull() {
		CodeVar intCodeVar = new CodeVar();
		intCodeVar.setType(ArgType.INT);
		RegisterArg intAssign = InsnArg.reg(0, ArgType.INT);
		SSAVar intVar = new SSAVar(0, 0, intAssign);
		intVar.setCodeVar(intCodeVar);
		intCodeVar.setSsaVars(List.of(intVar));

		ArgType stateType = ArgType.object("test.PagerState");
		CodeVar stateCodeVar = new CodeVar();
		stateCodeVar.setType(stateType);
		RegisterArg stateAssign = InsnArg.reg(1, ArgType.UNKNOWN_OBJECT);
		SSAVar stateVar = new SSAVar(1, 0, stateAssign);
		stateVar.setCodeVar(stateCodeVar);
		stateCodeVar.setSsaVars(List.of(stateVar));
		InsnNode handlerMove = new InsnNode(InsnType.MOVE, 1);
		handlerMove.add(AFlag.SYNTHETIC);
		handlerMove.setResult(stateAssign);
		RegisterArg handlerUse = intAssign.duplicate();
		handlerUse.forceSetInitType(ArgType.UNKNOWN_OBJECT);
		intVar.use(handlerUse);
		handlerMove.addArg(handlerUse);

		assertThat(FinishTypeInference.repairLateExceptionPrimitiveToReferenceMoves(
				List.of(intVar, stateVar), insn -> insn == handlerMove)).isOne();
		assertThat(intCodeVar.getType()).isEqualTo(ArgType.INT);
		assertThat(handlerMove.getArg(0).isZeroConst()).isTrue();
		assertThat(handlerMove.getArg(0).getType()).isEqualTo(stateType);
	}

	@Test
	void reconnectCoroutineLabelHandlerCastToReferenceCleanupPhi() {
		RootNode root = new RootNode(new JadxArgs());
		ArgType continuationType = ArgType.object("test.WorkerContinuation");
		CodeVar labelCodeVar = new CodeVar();
		RegisterArg labelAssign = InsnArg.reg(0, ArgType.UNKNOWN);
		SSAVar labelVar = new SSAVar(0, 0, labelAssign);
		labelVar.setCodeVar(labelCodeVar);
		labelCodeVar.setSsaVars(List.of(labelVar));
		FieldInfo labelField = FieldInfo.from(
				root, ClassInfo.fromType(root, continuationType), "label", ArgType.INT);
		IndexInsnNode labelGet = new IndexInsnNode(InsnType.IGET, labelField, 1);
		labelGet.setResult(labelAssign);
		labelGet.addArg(InsnArg.reg(2, continuationType));
		RegisterArg zeroUse = labelAssign.duplicate();
		labelVar.use(zeroUse);
		new IfNode(IfOp.EQ, -1, zeroUse, LiteralArg.make(0, ArgType.INT));
		RegisterArg oneUse = labelAssign.duplicate();
		labelVar.use(oneUse);
		new IfNode(IfOp.NE, -1, oneUse, LiteralArg.make(1, ArgType.INT));

		ArgType jobType = ArgType.object("kotlinx.coroutines.Job");
		CodeVar jobCodeVar = new CodeVar();
		jobCodeVar.setType(jobType);
		RegisterArg firstJobAssign = InsnArg.reg(0, jobType);
		SSAVar firstJobVar = new SSAVar(0, 1, firstJobAssign);
		firstJobVar.setCodeVar(jobCodeVar);
		RegisterArg resumedJobAssign = InsnArg.reg(0, jobType);
		SSAVar resumedJobVar = new SSAVar(0, 2, resumedJobAssign);
		resumedJobVar.setCodeVar(jobCodeVar);
		RegisterArg jobPhiAssign = InsnArg.reg(0, jobType);
		SSAVar jobPhiVar = new SSAVar(0, 3, jobPhiAssign);
		jobPhiVar.setCodeVar(jobCodeVar);
		PhiInsn jobPhi = new PhiInsn(0, 2);
		jobPhi.setResult(jobPhiAssign);
		jobPhi.bindArg(firstJobAssign.duplicate(), new BlockNode(0, 0, 0));
		jobPhi.bindArg(resumedJobAssign.duplicate(), new BlockNode(1, 0, 0));
		RegisterArg normalJobUse = jobPhiAssign.duplicate();
		jobPhiVar.use(normalJobUse);
		new InsnNode(InsnType.INVOKE, 1).addArg(normalJobUse);
		jobCodeVar.setSsaVars(List.of(firstJobVar, resumedJobVar, jobPhiVar));

		RegisterArg handlerLabelUse = labelAssign.duplicate();
		handlerLabelUse.forceSetInitType(jobType);
		labelVar.use(handlerLabelUse);
		IndexInsnNode handlerCast = new IndexInsnNode(InsnType.CAST, jobType, 1);
		handlerCast.addArg(handlerLabelUse);

		assertThat(FinishTypeInference.repairLateExceptionPrimitiveToReferenceMoves(
				List.of(labelVar, firstJobVar, resumedJobVar, jobPhiVar), insn -> insn == handlerCast)).isOne();
		assertThat(labelCodeVar.getType()).isEqualTo(ArgType.INT);
		assertThat(handlerCast.getArg(0)).isInstanceOf(RegisterArg.class);
		assertThat(((RegisterArg) handlerCast.getArg(0)).getSVar().getCodeVar()).isSameAs(jobCodeVar);
	}

	@Test
	void reconnectCoroutineLabelHandlerPhiToReferenceMethodArg() {
		RootNode root = new RootNode(new JadxArgs());
		ArgType continuationType = ArgType.object("test.CopyContinuation");
		CodeVar labelCodeVar = new CodeVar();
		labelCodeVar.setType(ArgType.INT);
		RegisterArg labelAssign = InsnArg.reg(3, ArgType.UNKNOWN);
		SSAVar labelVar = new SSAVar(3, 0, labelAssign);
		labelVar.setCodeVar(labelCodeVar);
		labelCodeVar.setSsaVars(List.of(labelVar));
		FieldInfo labelField = FieldInfo.from(
				root, ClassInfo.fromType(root, continuationType), "label", ArgType.INT);
		IndexInsnNode labelGet = new IndexInsnNode(InsnType.IGET, labelField, 1);
		labelGet.setResult(labelAssign);
		labelGet.addArg(InsnArg.reg(2, continuationType));
		RegisterArg labelBranchUse = labelAssign.duplicate();
		labelVar.use(labelBranchUse);
		new IfNode(IfOp.EQ, -1, labelBranchUse, LiteralArg.make(0, ArgType.INT));

		ArgType channelType = ArgType.object("test.ByteWriteChannel");
		CodeVar methodArgCodeVar = new CodeVar();
		methodArgCodeVar.setType(channelType);
		RegisterArg methodArgAssign = InsnArg.reg(18, channelType);
		SSAVar methodArgVar = new SSAVar(18, 0, methodArgAssign);
		methodArgVar.setCodeVar(methodArgCodeVar);
		methodArgCodeVar.setSsaVars(List.of(methodArgVar));

		CodeVar cleanupCodeVar = new CodeVar();
		RegisterArg cleanupAssign = InsnArg.reg(3, ArgType.UNKNOWN_OBJECT);
		SSAVar cleanupVar = new SSAVar(3, 1, cleanupAssign);
		cleanupVar.setCodeVar(cleanupCodeVar);
		InsnNode cleanupMove = new InsnNode(InsnType.MOVE, 1);
		cleanupMove.setResult(cleanupAssign);
		RegisterArg methodArgUse = methodArgAssign.duplicate();
		methodArgVar.use(methodArgUse);
		cleanupMove.addArg(methodArgUse);

		RegisterArg handlerPhiAssign = InsnArg.reg(3, ArgType.UNKNOWN_OBJECT);
		SSAVar handlerPhiVar = new SSAVar(3, 2, handlerPhiAssign);
		handlerPhiVar.setCodeVar(cleanupCodeVar);
		PhiInsn handlerPhi = new PhiInsn(3, 2);
		handlerPhi.setResult(handlerPhiAssign);
		RegisterArg handlerLabelUse = labelAssign.duplicate();
		labelVar.use(handlerLabelUse);
		handlerPhi.bindArg(handlerLabelUse, new BlockNode(0, 0, 0));
		RegisterArg cleanupUse = cleanupAssign.duplicate();
		cleanupVar.use(cleanupUse);
		handlerPhi.bindArg(cleanupUse, new BlockNode(1, 0, 0));
		RegisterArg closeUse = handlerPhiAssign.duplicate();
		closeUse.forceSetInitType(channelType);
		handlerPhiVar.use(closeUse);
		new InsnNode(InsnType.INVOKE, 1).addArg(closeUse);
		cleanupCodeVar.setSsaVars(List.of(cleanupVar, handlerPhiVar));

		assertThat(FinishTypeInference.repairLateExceptionPrimitiveToReferenceMoves(
				List.of(labelVar, methodArgVar, cleanupVar, handlerPhiVar), insn -> insn == handlerPhi)).isOne();
		assertThat(labelCodeVar.getType()).isEqualTo(ArgType.INT);
		assertThat(handlerPhi.getArg(0).getSVar()).isSameAs(methodArgVar);
	}

	@Test
	void reconnectCoroutineLabelHandlerPhiToDirectTypedCleanupFlow() {
		RootNode root = new RootNode(new JadxArgs());
		ArgType continuationType = ArgType.object("test.DecodeContinuation");
		CodeVar labelCodeVar = new CodeVar();
		SSAVar labelVar = makeSsaVar(2, 0, ArgType.UNKNOWN, labelCodeVar);
		labelCodeVar.setSsaVars(List.of(labelVar));
		FieldInfo labelField = FieldInfo.from(
				root, ClassInfo.fromType(root, continuationType), "label", ArgType.INT);
		IndexInsnNode labelGet = new IndexInsnNode(InsnType.IGET, labelField, 1);
		labelGet.setResult(labelVar.getAssign());
		labelGet.addArg(InsnArg.reg(8, continuationType));
		RegisterArg switchUse = labelVar.getAssign().duplicate();
		labelVar.use(switchUse);
		new InsnNode(InsnType.SWITCH, 1).addArg(switchUse);

		ArgType builderType = ArgType.object("java.lang.StringBuilder");
		CodeVar handlerCodeVar = new CodeVar();
		SSAVar handlerInput = makeSsaVar(2, 1, ArgType.UNKNOWN_OBJECT, handlerCodeVar);
		bindMove(handlerInput, makeSsaVar(7, 0, builderType, typedCodeVar(builderType)));
		SSAVar handlerResult = makeSsaVar(2, 2, ArgType.UNKNOWN_OBJECT, handlerCodeVar);
		PhiInsn handlerPhi = makePhi(handlerResult);
		bindPhiArg(handlerPhi, labelVar, 2);
		bindPhiArg(handlerPhi, handlerInput, 3);
		CodeVar typedBoundaryCodeVar = typedCodeVar(builderType);
		SSAVar typedBoundary = makeSsaVar(2, 3, builderType, typedBoundaryCodeVar);
		bindMove(typedBoundary, handlerInput);
		typedBoundary.getAssignInsn().add(AFlag.SYNTHETIC);
		RegisterArg castUse = handlerResult.getAssign().duplicate();
		castUse.forceSetInitType(builderType);
		handlerResult.use(castUse);
		new IndexInsnNode(InsnType.CHECK_CAST, builderType, 1).addArg(castUse);
		handlerCodeVar.setSsaVars(List.of(handlerInput, handlerResult));

		assertThat(FinishTypeInference.repairLateExceptionPrimitiveToReferenceMoves(
				List.of(labelVar, handlerInput, handlerResult, typedBoundary),
				insn -> insn == handlerPhi)).isOne();
		assertThat(handlerPhi.getArg(0).getSVar()).isSameAs(handlerInput);
		assertThat(handlerCodeVar.getType()).isEqualTo(builderType);
	}

	@Test
	void rejectCoroutineLabelHandlerPhiWithConflictingTypedBoundary() {
		RootNode root = new RootNode(new JadxArgs());
		ArgType continuationType = ArgType.object("test.DecodeContinuation");
		CodeVar labelCodeVar = new CodeVar();
		SSAVar labelVar = makeSsaVar(2, 0, ArgType.UNKNOWN, labelCodeVar);
		labelCodeVar.setSsaVars(List.of(labelVar));
		FieldInfo labelField = FieldInfo.from(
				root, ClassInfo.fromType(root, continuationType), "label", ArgType.INT);
		IndexInsnNode labelGet = new IndexInsnNode(InsnType.IGET, labelField, 1);
		labelGet.setResult(labelVar.getAssign());
		labelGet.addArg(InsnArg.reg(8, continuationType));
		RegisterArg switchUse = labelVar.getAssign().duplicate();
		labelVar.use(switchUse);
		new InsnNode(InsnType.SWITCH, 1).addArg(switchUse);

		ArgType builderType = ArgType.object("java.lang.StringBuilder");
		CodeVar handlerCodeVar = new CodeVar();
		SSAVar handlerInput = makeSsaVar(2, 1, ArgType.UNKNOWN_OBJECT, handlerCodeVar);
		bindMove(handlerInput, makeSsaVar(7, 0, builderType, typedCodeVar(builderType)));
		SSAVar handlerResult = makeSsaVar(2, 2, ArgType.UNKNOWN_OBJECT, handlerCodeVar);
		PhiInsn handlerPhi = makePhi(handlerResult);
		bindPhiArg(handlerPhi, labelVar, 2);
		bindPhiArg(handlerPhi, handlerInput, 3);
		SSAVar typedBoundary = makeSsaVar(2, 3, builderType, typedCodeVar(builderType));
		bindMove(typedBoundary, handlerInput);
		typedBoundary.getAssignInsn().add(AFlag.SYNTHETIC);
		SSAVar conflictingBoundary = makeSsaVar(
				2, 4, ArgType.STRING, typedCodeVar(ArgType.STRING));
		bindMove(conflictingBoundary, handlerInput);
		conflictingBoundary.getAssignInsn().add(AFlag.SYNTHETIC);
		RegisterArg castUse = handlerResult.getAssign().duplicate();
		castUse.forceSetInitType(builderType);
		handlerResult.use(castUse);
		new IndexInsnNode(InsnType.CHECK_CAST, builderType, 1).addArg(castUse);
		handlerCodeVar.setSsaVars(List.of(handlerInput, handlerResult));

		assertThat(FinishTypeInference.repairLateExceptionPrimitiveToReferenceMoves(
				List.of(labelVar, handlerInput, handlerResult, typedBoundary, conflictingBoundary),
				insn -> insn == handlerPhi)).isZero();
		assertThat(handlerPhi.getArg(0).getSVar()).isSameAs(labelVar);
		assertThat(handlerCodeVar.getType()).isNull();
	}

	@Test
	void retainUseInGeneratedInsn() {
		SSAVar var = makeVarWithUse(false);

		assertThat(FinishTypeInference.hasGeneratedUse(var)).isTrue();
	}

	@Test
	void reportTypeFailureOncePerCodeVar() {
		SSAVar first = makeVarWithUse(false);
		SSAVar second = makeVarWithUse(false);
		CodeVar codeVar = new CodeVar();
		first.setCodeVar(codeVar);
		second.setCodeVar(codeVar);

		assertThat(FinishTypeInference.collectWarnVars(List.of(first, second)))
				.containsExactly(first);
	}

	@Test
	void skipTypeFailureAfterLastGeneratedUseIsRemoved() {
		SSAVar var = makeVarWithUse(false);
		var.setCodeVar(new CodeVar());
		assertThat(FinishTypeInference.collectWarnVars(List.of(var))).containsExactly(var);

		var.getUseList().get(0).getParentInsn().add(AFlag.DONT_GENERATE);

		assertThat(FinishTypeInference.collectWarnVars(List.of(var))).isEmpty();
	}

	@Test
	void removeOnlyTypeInferenceWarningsDuringFinalValidation() {
		JadxCommentsAttr comments = new JadxCommentsAttr();
		comments.add(CommentsLevel.WARN, "Type inference failed for: r0v0");
		comments.add(CommentsLevel.WARN, "Code duplicated in 1 blocks");
		comments.add(CommentsLevel.DEBUG, "debug detail");

		assertThat(ReportTypeInferenceWarnings.removeTypeInferenceWarnings(comments)).isTrue();
		assertThat(comments.getComments().get(CommentsLevel.WARN))
				.containsExactly("Code duplicated in 1 blocks");
		assertThat(comments.getComments().get(CommentsLevel.DEBUG)).containsExactly("debug detail");
	}

	@Test
	void detectNullLiteralCarriedByMove() {
		RegisterArg assign = InsnArg.reg(0, ArgType.UNKNOWN);
		SSAVar var = new SSAVar(0, 0, assign);
		InsnNode move = new InsnNode(InsnType.MOVE, 1);
		move.setResult(assign);
		move.addArg(InsnArg.lit(0, ArgType.UNKNOWN));

		assertThat(FinishTypeInference.isProvenNullValue(assign, new HashSet<>())).isTrue();
	}

	@Test
	void splitDefinitePrimitiveFromMixedReferenceCodeVar() {
		RegisterArg primitiveAssign = InsnArg.reg(0, ArgType.INT);
		SSAVar primitiveVar = new SSAVar(0, 0, primitiveAssign);
		primitiveVar.setType(ArgType.unknown(PrimitiveType.INT));
		InsnNode primitiveInsn = new InsnNode(InsnType.IGET, 0);
		primitiveInsn.setResult(primitiveAssign);

		RegisterArg objectAssign = InsnArg.reg(0, ArgType.OBJECT);
		SSAVar objectVar = new SSAVar(0, 1, objectAssign);
		objectVar.setType(ArgType.OBJECT);

		CodeVar mixedCodeVar = new CodeVar();
		primitiveVar.setCodeVar(mixedCodeVar);
		objectVar.setCodeVar(mixedCodeVar);
		Map<CodeVar, List<SSAVar>> groups = new IdentityHashMap<>();
		groups.put(mixedCodeVar, List.of(primitiveVar, objectVar));

		assertThat(FinishTypeInference.splitMixedPrimitiveCodeVars(
				groups, var -> var == primitiveVar ? ArgType.INT : null)).isOne();
		assertThat(primitiveVar.getCodeVar()).isNotSameAs(mixedCodeVar);
		assertThat(primitiveVar.getCodeVar().getType()).isEqualTo(ArgType.INT);
		assertThat(mixedCodeVar.getSsaVars()).containsExactly(objectVar);
	}

	@Test
	void splitTerminalCoroutineBooleanConditionFromSyntheticStringPhi() {
		RegisterArg booleanAssign = InsnArg.reg(0, ArgType.BOOLEAN);
		SSAVar booleanVar = new SSAVar(0, 0, booleanAssign);
		InsnNode booleanCall = new InsnNode(InsnType.INVOKE, 0);
		booleanCall.setResult(booleanAssign);
		RegisterArg conditionUse = booleanAssign.duplicate();
		conditionUse.forceSetInitType(ArgType.UNKNOWN);
		booleanVar.use(conditionUse);
		IfNode booleanCheck = new IfNode(IfOp.NE, -1, conditionUse, LiteralArg.make(0, ArgType.UNKNOWN));

		RegisterArg stringSourceAssign = InsnArg.reg(1, ArgType.STRING);
		SSAVar stringSourceVar = new SSAVar(1, 0, stringSourceAssign);
		CodeVar stringSourceCodeVar = new CodeVar();
		stringSourceCodeVar.setType(ArgType.STRING);
		stringSourceVar.setCodeVar(stringSourceCodeVar);

		RegisterArg stringAssign = InsnArg.reg(0, ArgType.UNKNOWN_OBJECT);
		SSAVar stringVar = new SSAVar(0, 1, stringAssign);
		InsnNode stringMove = new InsnNode(InsnType.MOVE, 1);
		stringMove.setResult(stringAssign);
		stringMove.addArg(stringSourceAssign.duplicate());

		RegisterArg phiAssign = InsnArg.reg(0, ArgType.UNKNOWN);
		SSAVar phiVar = new SSAVar(0, 2, phiAssign);
		PhiInsn phiInsn = new PhiInsn(0, 2);
		phiInsn.setResult(phiAssign);
		phiInsn.bindArg(booleanAssign.duplicate(), new BlockNode(0, 0, 0));
		phiInsn.bindArg(stringAssign.duplicate(), new BlockNode(1, 0, 0));
		phiInsn.add(AFlag.DONT_GENERATE);

		CodeVar mixedCodeVar = new CodeVar();
		booleanVar.setCodeVar(mixedCodeVar);
		stringVar.setCodeVar(mixedCodeVar);
		phiVar.setCodeVar(mixedCodeVar);
		mixedCodeVar.setSsaVars(List.of(booleanVar, stringVar, phiVar));
		Map<CodeVar, List<SSAVar>> groups = new IdentityHashMap<>();
		groups.put(mixedCodeVar, List.of(booleanVar, stringVar, phiVar));

		assertThat(FinishTypeInference.getExactBooleanTerminalType(booleanVar)).isEqualTo(ArgType.BOOLEAN);
		assertThat(FinishTypeInference.splitTerminalCoroutineBooleanCodeVars(groups)).isOne();
		assertThat(booleanVar.getCodeVar()).isNotSameAs(mixedCodeVar);
		assertThat(booleanVar.getCodeVar().getType()).isEqualTo(ArgType.BOOLEAN);
		assertThat(conditionUse.getInitType()).isEqualTo(ArgType.BOOLEAN);
		assertThat(booleanCheck.getArg(1).getType()).isEqualTo(ArgType.BOOLEAN);
		assertThat(mixedCodeVar.getSsaVars()).containsExactly(stringVar, phiVar);
	}

	@Test
	void splitTerminalCoroutineBooleanFieldFromObjectResultPhi() {
		RootNode root = new RootNode(new JadxArgs());
		ArgType ownerType = ArgType.object("test.Owner");
		RegisterArg booleanAssign = InsnArg.reg(0, ArgType.BOOLEAN);
		SSAVar booleanVar = new SSAVar(0, 0, booleanAssign);
		FieldInfo booleanField = FieldInfo.from(root, ClassInfo.fromType(root, ownerType), "focused", ArgType.BOOLEAN);
		IndexInsnNode booleanGet = new IndexInsnNode(InsnType.IGET, booleanField, 1);
		booleanGet.setResult(booleanAssign);
		booleanGet.addArg(InsnArg.reg(2, ownerType));

		RegisterArg conditionUse = booleanAssign.duplicate();
		conditionUse.forceSetInitType(ArgType.UNKNOWN);
		booleanVar.use(conditionUse);
		IfNode booleanCheck = new IfNode(IfOp.EQ, -1, conditionUse, LiteralArg.make(0, ArgType.UNKNOWN));

		RegisterArg objectSourceAssign = InsnArg.reg(1, ArgType.OBJECT);
		SSAVar objectSourceVar = new SSAVar(1, 0, objectSourceAssign);
		CodeVar objectSourceCodeVar = new CodeVar();
		objectSourceCodeVar.setType(ArgType.OBJECT);
		objectSourceVar.setCodeVar(objectSourceCodeVar);

		RegisterArg objectAssign = InsnArg.reg(0, ArgType.UNKNOWN_OBJECT);
		SSAVar objectVar = new SSAVar(0, 1, objectAssign);
		InsnNode objectMove = new InsnNode(InsnType.MOVE, 1);
		objectMove.setResult(objectAssign);
		RegisterArg objectSourceUse = objectSourceAssign.duplicate();
		objectMove.addArg(objectSourceUse);
		objectSourceVar.use(objectSourceUse);

		RegisterArg phiAssign = InsnArg.reg(0, ArgType.UNKNOWN);
		SSAVar phiVar = new SSAVar(0, 2, phiAssign);
		PhiInsn phiInsn = new PhiInsn(0, 2);
		phiInsn.setResult(phiAssign);
		RegisterArg booleanPhiUse = booleanAssign.duplicate();
		RegisterArg objectPhiUse = objectAssign.duplicate();
		phiInsn.bindArg(booleanPhiUse, new BlockNode(0, 0, 0));
		phiInsn.bindArg(objectPhiUse, new BlockNode(1, 0, 0));
		phiInsn.add(AFlag.DONT_GENERATE);
		booleanVar.use(booleanPhiUse);
		objectVar.use(objectPhiUse);

		CodeVar mixedCodeVar = new CodeVar();
		booleanVar.setCodeVar(mixedCodeVar);
		objectVar.setCodeVar(mixedCodeVar);
		phiVar.setCodeVar(mixedCodeVar);
		mixedCodeVar.setSsaVars(List.of(booleanVar, objectVar, phiVar));
		Map<CodeVar, List<SSAVar>> groups = new IdentityHashMap<>();
		groups.put(mixedCodeVar, List.of(booleanVar, objectVar, phiVar));

		assertThat(FinishTypeInference.getExactBooleanTerminalType(booleanVar)).isEqualTo(ArgType.BOOLEAN);
		assertThat(FinishTypeInference.splitTerminalCoroutineBooleanCodeVars(groups)).isOne();
		assertThat(booleanVar.getCodeVar()).isNotSameAs(mixedCodeVar);
		assertThat(booleanVar.getCodeVar().getType()).isEqualTo(ArgType.BOOLEAN);
		assertThat(conditionUse.getInitType()).isEqualTo(ArgType.BOOLEAN);
		assertThat(booleanCheck.getArg(1).getType()).isEqualTo(ArgType.BOOLEAN);
		assertThat(mixedCodeVar.getSsaVars()).containsExactly(objectVar, phiVar);
	}

	@Test
	void splitTerminalCoroutineReferenceCastFromObjectResultPhi() {
		ArgType eventType = ArgType.object("test.PointerEvent");
		RegisterArg objectSourceAssign = InsnArg.reg(1, ArgType.OBJECT);
		SSAVar objectSourceVar = new SSAVar(1, 0, objectSourceAssign);
		CodeVar objectSourceCodeVar = new CodeVar();
		objectSourceCodeVar.setType(ArgType.OBJECT);
		objectSourceVar.setCodeVar(objectSourceCodeVar);

		RegisterArg objectAssign = InsnArg.reg(0, ArgType.UNKNOWN_OBJECT);
		SSAVar objectVar = new SSAVar(0, 0, objectAssign);
		InsnNode objectMove = new InsnNode(InsnType.MOVE, 1);
		objectMove.setResult(objectAssign);
		RegisterArg objectSourceUse = objectSourceAssign.duplicate();
		objectMove.addArg(objectSourceUse);
		objectSourceVar.use(objectSourceUse);

		RegisterArg castAssign = InsnArg.reg(0, eventType);
		SSAVar castVar = new SSAVar(0, 1, castAssign);
		IndexInsnNode castInsn = new IndexInsnNode(InsnType.CHECK_CAST, eventType, 1);
		castInsn.setResult(castAssign);
		RegisterArg castSourceUse = objectAssign.duplicate();
		castInsn.addArg(castSourceUse);
		objectVar.use(castSourceUse);

		RegisterArg eventUse = castAssign.duplicate();
		eventUse.forceSetInitType(eventType);
		castVar.use(eventUse);
		InsnNode eventCall = new InsnNode(InsnType.INVOKE, 1);
		eventCall.addArg(eventUse);

		RegisterArg phiAssign = InsnArg.reg(0, ArgType.UNKNOWN);
		SSAVar phiVar = new SSAVar(0, 2, phiAssign);
		PhiInsn phiInsn = new PhiInsn(0, 2);
		phiInsn.setResult(phiAssign);
		RegisterArg objectPhiUse = objectAssign.duplicate();
		RegisterArg castPhiUse = castAssign.duplicate();
		phiInsn.bindArg(objectPhiUse, new BlockNode(0, 0, 0));
		phiInsn.bindArg(castPhiUse, new BlockNode(1, 0, 0));
		phiInsn.add(AFlag.DONT_GENERATE);
		objectVar.use(objectPhiUse);
		castVar.use(castPhiUse);

		CodeVar mixedCodeVar = new CodeVar();
		objectVar.setCodeVar(mixedCodeVar);
		castVar.setCodeVar(mixedCodeVar);
		phiVar.setCodeVar(mixedCodeVar);
		mixedCodeVar.setSsaVars(List.of(objectVar, castVar, phiVar));
		Map<CodeVar, List<SSAVar>> groups = new IdentityHashMap<>();
		groups.put(mixedCodeVar, List.of(objectVar, castVar, phiVar));

		assertThat(FinishTypeInference.splitTerminalCoroutineReferenceCastCodeVars(groups)).isOne();
		assertThat(castVar.getCodeVar()).isNotSameAs(mixedCodeVar);
		assertThat(castVar.getCodeVar().getType()).isEqualTo(eventType);
		assertThat(eventUse.getInitType()).isEqualTo(eventType);
		assertThat(mixedCodeVar.getSsaVars()).containsExactly(objectVar, phiVar);
	}

	@Test
	void splitTerminalCoroutineReferenceInvokeFromObjectResultPhi() {
		RootNode root = new RootNode(new JadxArgs());
		ArgType listType = ArgType.object("java.util.List");
		ArgType collectionType = ArgType.object("java.util.Collection");
		CodeVar mixedCodeVar = new CodeVar();

		RegisterArg objectAssign = InsnArg.reg(0, ArgType.OBJECT);
		SSAVar objectVar = new SSAVar(0, 0, objectAssign);
		objectVar.setCodeVar(mixedCodeVar);
		InsnNode objectMove = new InsnNode(InsnType.MOVE, 1);
		objectMove.setResult(objectAssign);
		objectMove.addArg(makeKnownSource(1, ArgType.OBJECT));

		RegisterArg listAssign = InsnArg.reg(0, listType);
		SSAVar listVar = new SSAVar(0, 1, listAssign);
		listVar.setCodeVar(mixedCodeVar);
		MethodInfo changesMth = MethodInfo.fromDetails(
				root, ClassInfo.fromName(root, "test.PointerEvent"), "getChanges", List.of(), listType);
		InvokeNode changesCall = new InvokeNode(changesMth, InvokeType.VIRTUAL, 1);
		changesCall.setResult(listAssign);
		changesCall.addArg(makeKnownSource(2, ArgType.object("test.PointerEvent")));
		RegisterArg listUse = listAssign.duplicate();
		listUse.forceSetInitType(listType);
		listVar.use(listUse);
		new InsnNode(InsnType.INVOKE, 1).addArg(listUse);
		RegisterArg collectionCastUse = listAssign.duplicate();
		collectionCastUse.forceSetInitType(ArgType.UNKNOWN_OBJECT);
		listVar.use(collectionCastUse);
		IndexInsnNode collectionCast = new IndexInsnNode(InsnType.CHECK_CAST, collectionType, 1);
		collectionCast.addArg(collectionCastUse);

		RegisterArg phiAssign = InsnArg.reg(0, ArgType.UNKNOWN);
		SSAVar phiVar = new SSAVar(0, 2, phiAssign);
		phiVar.setCodeVar(mixedCodeVar);
		PhiInsn phiInsn = new PhiInsn(0, 2);
		phiInsn.setResult(phiAssign);
		RegisterArg objectPhiUse = objectAssign.duplicate();
		RegisterArg listPhiUse = listAssign.duplicate();
		phiInsn.bindArg(objectPhiUse, new BlockNode(0, 0, 0));
		phiInsn.bindArg(listPhiUse, new BlockNode(1, 0, 0));
		phiInsn.add(AFlag.DONT_GENERATE);
		objectVar.use(objectPhiUse);
		listVar.use(listPhiUse);
		mixedCodeVar.setSsaVars(List.of(objectVar, listVar, phiVar));
		Map<CodeVar, List<SSAVar>> groups = new IdentityHashMap<>();
		groups.put(mixedCodeVar, List.of(objectVar, listVar, phiVar));

		assertThat(FinishTypeInference.splitTerminalCoroutineReferenceInvokeCodeVars(groups)).isOne();
		assertThat(listVar.getCodeVar()).isNotSameAs(mixedCodeVar);
		assertThat(listVar.getCodeVar().getType()).isEqualTo(listType);
		assertThat(listVar.getCodeVar().isDeclared()).isTrue();
		assertThat(changesCall.contains(AFlag.DECLARE_VAR)).isTrue();
		assertThat(listUse.getInitType()).isEqualTo(listType);
		assertThat(collectionCastUse.getInitType()).isEqualTo(listType);
		assertThat(mixedCodeVar.getSsaVars()).containsExactly(objectVar, phiVar);
	}

	@Test
	void splitExactReferenceMoveRootFromStructuralExceptionPhi() {
		ArgType refType = ArgType.object("test.Ref");
		ArgType drawContextType = ArgType.object("test.DrawContext");
		RegisterArg refSourceAssign = InsnArg.reg(1, refType);
		SSAVar refSourceVar = new SSAVar(1, 0, refSourceAssign);
		CodeVar refSourceCodeVar = new CodeVar();
		refSourceCodeVar.setType(refType);
		refSourceVar.setCodeVar(refSourceCodeVar);

		RegisterArg refAssign = InsnArg.reg(0, ArgType.UNKNOWN_OBJECT);
		SSAVar refVar = new SSAVar(0, 0, refAssign);
		InsnNode refMove = new InsnNode(InsnType.MOVE, 1);
		refMove.setResult(refAssign);
		RegisterArg refSourceUse = refSourceAssign.duplicate();
		refMove.addArg(refSourceUse);
		refSourceVar.use(refSourceUse);
		RegisterArg refUse = refAssign.duplicate();
		refUse.forceSetInitType(refType);
		refVar.use(refUse);
		InsnNode refCall = new InsnNode(InsnType.INVOKE, 1);
		refCall.addArg(refUse);

		RegisterArg drawSourceAssign = InsnArg.reg(2, drawContextType);
		SSAVar drawSourceVar = new SSAVar(2, 0, drawSourceAssign);
		CodeVar drawSourceCodeVar = new CodeVar();
		drawSourceCodeVar.setType(drawContextType);
		drawSourceVar.setCodeVar(drawSourceCodeVar);
		RegisterArg drawAssign = InsnArg.reg(0, ArgType.UNKNOWN_OBJECT);
		SSAVar drawVar = new SSAVar(0, 1, drawAssign);
		InsnNode drawMove = new InsnNode(InsnType.MOVE, 1);
		drawMove.setResult(drawAssign);
		RegisterArg drawSourceUse = drawSourceAssign.duplicate();
		drawMove.addArg(drawSourceUse);
		drawSourceVar.use(drawSourceUse);
		RegisterArg phiAssign = InsnArg.reg(0, ArgType.UNKNOWN);
		SSAVar phiVar = new SSAVar(0, 2, phiAssign);
		PhiInsn phiInsn = new PhiInsn(0, 2);
		phiInsn.setResult(phiAssign);
		RegisterArg refPhiUse = refAssign.duplicate();
		RegisterArg drawPhiUse = drawAssign.duplicate();
		phiInsn.bindArg(refPhiUse, new BlockNode(0, 0, 0));
		phiInsn.bindArg(drawPhiUse, new BlockNode(1, 0, 0));
		phiInsn.add(AFlag.DONT_GENERATE);
		refVar.use(refPhiUse);
		drawVar.use(drawPhiUse);
		RegisterArg drawUse = phiAssign.duplicate();
		drawUse.forceSetInitType(drawContextType);
		phiVar.use(drawUse);
		InsnNode drawCall = new InsnNode(InsnType.INVOKE, 1);
		drawCall.addArg(drawUse);

		CodeVar mixedCodeVar = new CodeVar();
		refVar.setCodeVar(mixedCodeVar);
		drawVar.setCodeVar(mixedCodeVar);
		phiVar.setCodeVar(mixedCodeVar);
		mixedCodeVar.setSsaVars(List.of(refVar, drawVar, phiVar));
		Map<CodeVar, List<SSAVar>> groups = new IdentityHashMap<>();
		groups.put(mixedCodeVar, List.of(refVar, drawVar, phiVar));

		assertThat(FinishTypeInference.splitStructuralReferenceMoveRootLifetimes(groups)).isOne();
		assertThat(refVar.getCodeVar()).isNotSameAs(mixedCodeVar);
		assertThat(refVar.getCodeVar().getType()).isEqualTo(refType);
		assertThat(mixedCodeVar.getType()).isEqualTo(drawContextType);
		assertThat(mixedCodeVar.getSsaVars()).containsExactly(drawVar, phiVar);
	}

	@Test
	void replaceIncompatibleCleanupPhiInputWithExistingNullSibling() {
		RootNode root = new RootNode(new JadxArgs());
		ArgType cleanupType = ArgType.object("test.Cleanup");
		ArgType otherType = ArgType.BOOLEAN;
		CodeVar cleanupCodeVar = new CodeVar();

		RegisterArg nullAssign = InsnArg.reg(0, ArgType.UNKNOWN_OBJECT);
		SSAVar nullVar = new SSAVar(0, 0, nullAssign);
		nullVar.setCodeVar(cleanupCodeVar);
		InsnNode nullInsn = new InsnNode(InsnType.CONST, 1);
		nullInsn.setResult(nullAssign);
		nullInsn.addArg(InsnArg.lit(0, ArgType.UNKNOWN_OBJECT));

		CodeVar sourceCodeVar = new CodeVar();
		sourceCodeVar.setType(cleanupType);
		RegisterArg sourceAssign = InsnArg.reg(2, cleanupType);
		SSAVar sourceVar = new SSAVar(2, 0, sourceAssign);
		sourceVar.setCodeVar(sourceCodeVar);
		sourceCodeVar.setSsaVars(List.of(sourceVar));
		PhiInsn relatedNullablePhi = new PhiInsn(4, 2);
		relatedNullablePhi.setResult(InsnArg.reg(4, ArgType.UNKNOWN_OBJECT));
		RegisterArg relatedNullUse = nullAssign.duplicate();
		nullVar.use(relatedNullUse);
		relatedNullablePhi.bindArg(relatedNullUse, new BlockNode(6, 0, 0));
		RegisterArg relatedSourceUse = sourceAssign.duplicate();
		sourceVar.use(relatedSourceUse);
		relatedNullablePhi.bindArg(relatedSourceUse, new BlockNode(7, 0, 0));

		RegisterArg cleanupAssign = InsnArg.reg(0, ArgType.UNKNOWN_OBJECT);
		SSAVar cleanupVar = new SSAVar(0, 1, cleanupAssign);
		cleanupVar.setCodeVar(cleanupCodeVar);
		InsnNode cleanupMove = new InsnNode(InsnType.MOVE, 1);
		cleanupMove.setResult(cleanupAssign);
		RegisterArg sourceUse = sourceAssign.duplicate();
		sourceVar.use(sourceUse);
		cleanupMove.addArg(sourceUse);

		CodeVar otherCodeVar = new CodeVar();
		otherCodeVar.setType(otherType);
		RegisterArg otherAssign = InsnArg.reg(3, otherType);
		SSAVar otherVar = new SSAVar(3, 0, otherAssign);
		otherVar.setCodeVar(otherCodeVar);
		otherCodeVar.setSsaVars(List.of(otherVar));
		RegisterArg otherIfUse = otherAssign.duplicate();
		otherVar.use(otherIfUse);
		IfNode otherIf = new IfNode(IfOp.NE, -1, otherIfUse, LiteralArg.make(0, ArgType.INT));

		RegisterArg handlerAssign = InsnArg.reg(0, ArgType.UNKNOWN_OBJECT);
		SSAVar handlerVar = new SSAVar(0, 2, handlerAssign);
		handlerVar.setCodeVar(cleanupCodeVar);
		PhiInsn handlerPhi = new PhiInsn(0, 2);
		handlerPhi.setResult(handlerAssign);
		RegisterArg nullUse = nullAssign.duplicate();
		nullVar.use(nullUse);
		handlerPhi.bindArg(nullUse, new BlockNode(0, 0, 0));
		RegisterArg otherUse = otherAssign.duplicate();
		otherVar.use(otherUse);
		handlerPhi.bindArg(otherUse, new BlockNode(1, 0, 0));

		RegisterArg terminalAssign = InsnArg.reg(0, ArgType.UNKNOWN_OBJECT);
		SSAVar terminalVar = new SSAVar(0, 3, terminalAssign);
		terminalVar.setCodeVar(cleanupCodeVar);
		PhiInsn terminalPhi = new PhiInsn(0, 2);
		terminalPhi.setResult(terminalAssign);
		RegisterArg cleanupUse = cleanupAssign.duplicate();
		cleanupVar.use(cleanupUse);
		terminalPhi.bindArg(cleanupUse, new BlockNode(2, 0, 0));
		RegisterArg handlerUse = handlerAssign.duplicate();
		handlerVar.use(handlerUse);
		terminalPhi.bindArg(handlerUse, new BlockNode(3, 0, 0));
		RegisterArg ifUse = terminalAssign.duplicate();
		terminalVar.use(ifUse);
		new IfNode(IfOp.NE, -1, ifUse, LiteralArg.make(0, ArgType.UNKNOWN_OBJECT));
		MethodInfo closeMth = MethodInfo.fromDetails(
				root, ClassInfo.fromType(root, cleanupType), "close", List.of(), ArgType.VOID);
		RegisterArg closeUse = terminalAssign.duplicate();
		closeUse.forceSetInitType(cleanupType);
		terminalVar.use(closeUse);
		new InvokeNode(closeMth, InvokeType.VIRTUAL, 1).addArg(closeUse);

		cleanupCodeVar.setSsaVars(List.of(nullVar, cleanupVar, handlerVar, terminalVar));
		Map<CodeVar, List<SSAVar>> groups = new IdentityHashMap<>();
		groups.put(cleanupCodeVar, cleanupCodeVar.getSsaVars());

		assertThat(FinishTypeInference.repairExceptionCleanupSiblingNullFlows(
				List.of(nullVar, cleanupVar, handlerVar, terminalVar, sourceVar, otherVar),
				groups, root.getTypeCompare(), insn -> insn == handlerPhi)).isOne();
		assertThat(handlerPhi.getArg(1).getSVar()).isSameAs(nullVar);
		assertThat(otherIf.getArg(1).getType()).isEqualTo(ArgType.BOOLEAN);
		assertThat(cleanupCodeVar.getType()).isEqualTo(cleanupType);
	}

	@Test
	void splitBooleanMethodArgFromFileCleanupLifetimeUsingRelatedNullSibling() {
		RootNode root = new RootNode(new JadxArgs());
		ArgType streamType = ArgType.object("test.FileStream");

		CodeVar streamSourceCodeVar = new CodeVar();
		streamSourceCodeVar.setType(streamType);
		RegisterArg streamSourceAssign = InsnArg.reg(2, streamType);
		SSAVar streamSourceVar = new SSAVar(2, 0, streamSourceAssign);
		streamSourceVar.setCodeVar(streamSourceCodeVar);
		streamSourceCodeVar.setSsaVars(List.of(streamSourceVar));

		CodeVar nullableCodeVar = new CodeVar();
		RegisterArg nullAssign = InsnArg.reg(0, ArgType.UNKNOWN_OBJECT);
		SSAVar nullVar = new SSAVar(0, 0, nullAssign);
		nullVar.setCodeVar(nullableCodeVar);
		InsnNode nullInsn = new InsnNode(InsnType.CONST, 1);
		nullInsn.setResult(nullAssign);
		nullInsn.addArg(InsnArg.lit(0, ArgType.UNKNOWN_OBJECT));
		RegisterArg nullableAssign = InsnArg.reg(0, ArgType.UNKNOWN_OBJECT);
		SSAVar nullableVar = new SSAVar(0, 1, nullableAssign);
		nullableVar.setCodeVar(nullableCodeVar);
		PhiInsn nullablePhi = new PhiInsn(0, 2);
		nullablePhi.setResult(nullableAssign);
		RegisterArg nullUse = nullAssign.duplicate();
		nullVar.use(nullUse);
		nullablePhi.bindArg(nullUse, new BlockNode(0, 0, 0));
		RegisterArg relatedStreamUse = streamSourceAssign.duplicate();
		streamSourceVar.use(relatedStreamUse);
		nullablePhi.bindArg(relatedStreamUse, new BlockNode(1, 0, 0));
		nullableCodeVar.setSsaVars(List.of(nullVar, nullableVar));

		CodeVar mixedCodeVar = new CodeVar();
		mixedCodeVar.setType(ArgType.BOOLEAN);
		RegisterArg booleanAssign = InsnArg.reg(5, ArgType.BOOLEAN);
		SSAVar booleanVar = new SSAVar(5, 0, booleanAssign);
		booleanVar.setCodeVar(mixedCodeVar);

		RegisterArg streamAssign = InsnArg.reg(5, ArgType.UNKNOWN_OBJECT);
		SSAVar streamVar = new SSAVar(5, 1, streamAssign);
		streamVar.setCodeVar(mixedCodeVar);
		InsnNode streamMove = new InsnNode(InsnType.MOVE, 1);
		streamMove.setResult(streamAssign);
		RegisterArg streamUse = streamSourceAssign.duplicate();
		streamSourceVar.use(streamUse);
		streamMove.addArg(streamUse);

		RegisterArg handlerAssign = InsnArg.reg(5, ArgType.UNKNOWN_OBJECT);
		SSAVar handlerVar = new SSAVar(5, 2, handlerAssign);
		handlerVar.setCodeVar(mixedCodeVar);
		PhiInsn handlerPhi = new PhiInsn(5, 2);
		handlerPhi.setResult(handlerAssign);
		RegisterArg booleanUse = booleanAssign.duplicate();
		booleanVar.use(booleanUse);
		handlerPhi.bindArg(booleanUse, new BlockNode(2, 0, 0));
		RegisterArg handlerStreamUse = streamAssign.duplicate();
		streamVar.use(handlerStreamUse);
		handlerPhi.bindArg(handlerStreamUse, new BlockNode(3, 0, 0));

		RegisterArg terminalAssign = InsnArg.reg(5, ArgType.UNKNOWN_OBJECT);
		SSAVar terminalVar = new SSAVar(5, 3, terminalAssign);
		terminalVar.setCodeVar(mixedCodeVar);
		PhiInsn terminalPhi = new PhiInsn(5, 2);
		terminalPhi.setResult(terminalAssign);
		RegisterArg directStreamUse = streamAssign.duplicate();
		streamVar.use(directStreamUse);
		terminalPhi.bindArg(directStreamUse, new BlockNode(4, 0, 0));
		RegisterArg handlerUse = handlerAssign.duplicate();
		handlerVar.use(handlerUse);
		terminalPhi.bindArg(handlerUse, new BlockNode(5, 0, 0));
		RegisterArg ifUse = terminalAssign.duplicate();
		terminalVar.use(ifUse);
		new IfNode(IfOp.NE, -1, ifUse, LiteralArg.make(0, ArgType.UNKNOWN_OBJECT));
		MethodInfo closeMth = MethodInfo.fromDetails(
				root, ClassInfo.fromType(root, streamType), "close", List.of(), ArgType.VOID);
		RegisterArg closeUse = terminalAssign.duplicate();
		closeUse.forceSetInitType(streamType);
		terminalVar.use(closeUse);
		new InvokeNode(closeMth, InvokeType.VIRTUAL, 1).addArg(closeUse);

		mixedCodeVar.setSsaVars(List.of(booleanVar, streamVar, handlerVar, terminalVar));
		Map<CodeVar, List<SSAVar>> groups = new IdentityHashMap<>();
		groups.put(mixedCodeVar, mixedCodeVar.getSsaVars());

		assertThat(FinishTypeInference.repairExceptionCleanupSiblingNullFlows(
				List.of(streamSourceVar, nullVar, nullableVar, booleanVar, streamVar, handlerVar, terminalVar),
				groups, root.getTypeCompare(), insn -> insn == handlerPhi)).isOne();
		assertThat(handlerPhi.getArg(0).getSVar()).isSameAs(nullVar);
		assertThat(nullInsn.getArg(0).getType()).isEqualTo(streamType);
		assertThat(booleanVar.getCodeVar()).isSameAs(mixedCodeVar);
		assertThat(mixedCodeVar.getType()).isEqualTo(ArgType.BOOLEAN);
		assertThat(streamVar.getCodeVar()).isSameAs(handlerVar.getCodeVar());
		assertThat(handlerVar.getCodeVar()).isSameAs(terminalVar.getCodeVar());
		assertThat(terminalVar.getCodeVar().getType()).isEqualTo(streamType);
	}

	@Test
	void splitExactStringLifetimeFromExceptionMergedArrayFlow() {
		ArgType arrayType = ArgType.array(ArgType.array(ArgType.BYTE));
		CodeVar mixedCodeVar = new CodeVar();

		RegisterArg stringAssign = InsnArg.reg(0, ArgType.STRING);
		SSAVar stringVar = new SSAVar(0, 0, stringAssign);
		stringVar.setCodeVar(mixedCodeVar);
		InsnNode stringCall = new InsnNode(InsnType.INVOKE, 0);
		stringCall.setResult(stringAssign);
		RegisterArg stringUse = stringAssign.duplicate();
		stringUse.forceSetInitType(ArgType.STRING);
		stringVar.use(stringUse);
		InsnNode stringConsumer = new InsnNode(InsnType.INVOKE, 1);
		stringConsumer.addArg(stringUse);

		RegisterArg arrayAssign = InsnArg.reg(0, arrayType);
		SSAVar arrayVar = new SSAVar(0, 1, arrayAssign);
		arrayVar.setCodeVar(mixedCodeVar);
		InsnNode arrayCall = new InsnNode(InsnType.INVOKE, 0);
		arrayCall.setResult(arrayAssign);
		RegisterArg unknownArrayMove = arrayAssign.duplicate();
		unknownArrayMove.forceSetInitType(ArgType.UNKNOWN);
		arrayVar.use(unknownArrayMove);
		InsnNode syntheticMove = new InsnNode(InsnType.MOVE, 1);
		syntheticMove.addArg(unknownArrayMove);

		RegisterArg nullAssign = InsnArg.reg(0, ArgType.UNKNOWN);
		SSAVar nullVar = new SSAVar(0, 2, nullAssign);
		nullVar.setCodeVar(mixedCodeVar);
		InsnNode nullInsn = new InsnNode(InsnType.CONST, 1);
		nullInsn.setResult(nullAssign);
		nullInsn.addArg(InsnArg.lit(0, ArgType.UNKNOWN));

		RegisterArg phiAssign = InsnArg.reg(0, ArgType.UNKNOWN);
		SSAVar phiVar = new SSAVar(0, 3, phiAssign);
		phiVar.setCodeVar(mixedCodeVar);
		PhiInsn phiInsn = new PhiInsn(0, 2);
		phiInsn.setResult(phiAssign);
		phiInsn.bindArg(stringAssign.duplicate(), new BlockNode(0, 0, 0));
		phiInsn.bindArg(nullAssign.duplicate(), new BlockNode(1, 0, 0));
		phiInsn.add(AFlag.DONT_GENERATE);

		mixedCodeVar.setSsaVars(List.of(stringVar, arrayVar, nullVar, phiVar));
		Map<CodeVar, List<SSAVar>> groups = new IdentityHashMap<>();
		groups.put(mixedCodeVar, List.of(stringVar, arrayVar, nullVar, phiVar));

		assertThat(FinishTypeInference.splitMixedReferenceLifetimes(groups)).isOne();
		assertThat(stringVar.getCodeVar()).isNotSameAs(mixedCodeVar);
		assertThat(stringVar.getCodeVar().getType()).isEqualTo(ArgType.STRING);
		assertThat(mixedCodeVar.getType()).isEqualTo(arrayType);
		assertThat(mixedCodeVar.getSsaVars()).containsExactly(arrayVar, nullVar, phiVar);
		assertThat(nullInsn.getArg(0).getType()).isEqualTo(arrayType);
	}

	@Test
	void splitArrayQueryArgsFromCursorLifetime() {
		ArgType arrayType = ArgType.array(ArgType.STRING);
		ArgType cursorType = ArgType.object("android.database.Cursor");
		CodeVar mixedCodeVar = new CodeVar();

		RegisterArg arrayAssign = InsnArg.reg(0, arrayType);
		SSAVar arrayVar = new SSAVar(0, 0, arrayAssign);
		arrayVar.setCodeVar(mixedCodeVar);
		InsnNode arrayInsn = new InsnNode(InsnType.FILLED_NEW_ARRAY, 1);
		arrayInsn.setResult(arrayAssign);
		arrayInsn.addArg(InsnArg.reg(1, ArgType.STRING));
		RegisterArg arrayUse = arrayAssign.duplicate();
		arrayUse.forceSetInitType(arrayType);
		arrayVar.use(arrayUse);
		InsnNode query = new InsnNode(InsnType.INVOKE, 1);
		query.addArg(arrayUse);

		RegisterArg cursorAssign = InsnArg.reg(0, cursorType);
		SSAVar cursorVar = new SSAVar(0, 1, cursorAssign);
		cursorVar.setCodeVar(mixedCodeVar);
		InsnNode cursorCall = new InsnNode(InsnType.INVOKE, 0);
		cursorCall.setResult(cursorAssign);
		RegisterArg cursorUse = cursorAssign.duplicate();
		cursorUse.forceSetInitType(cursorType);
		cursorVar.use(cursorUse);
		InsnNode close = new InsnNode(InsnType.INVOKE, 1);
		close.addArg(cursorUse);

		RegisterArg nullAssign = InsnArg.reg(0, ArgType.UNKNOWN_OBJECT);
		SSAVar nullVar = new SSAVar(0, 2, nullAssign);
		nullVar.setCodeVar(mixedCodeVar);
		InsnNode nullInsn = new InsnNode(InsnType.MOVE, 1);
		nullInsn.setResult(nullAssign);
		nullInsn.addArg(InsnArg.lit(0, ArgType.UNKNOWN_OBJECT));

		RegisterArg phiAssign = InsnArg.reg(0, ArgType.UNKNOWN_OBJECT);
		SSAVar phiVar = new SSAVar(0, 3, phiAssign);
		phiVar.setCodeVar(mixedCodeVar);
		PhiInsn phiInsn = new PhiInsn(0, 2);
		phiInsn.setResult(phiAssign);
		phiInsn.bindArg(nullAssign.duplicate(), new BlockNode(0, 0, 0));
		phiInsn.bindArg(cursorAssign.duplicate(), new BlockNode(1, 0, 0));
		phiInsn.add(AFlag.DONT_GENERATE);

		mixedCodeVar.setSsaVars(List.of(arrayVar, cursorVar, nullVar, phiVar));
		Map<CodeVar, List<SSAVar>> groups = new IdentityHashMap<>();
		groups.put(mixedCodeVar, List.of(arrayVar, cursorVar, nullVar, phiVar));

		assertThat(FinishTypeInference.splitMixedReferenceLifetimes(groups)).isOne();
		assertThat(arrayVar.getCodeVar()).isNotSameAs(mixedCodeVar);
		assertThat(arrayVar.getCodeVar().getType()).isEqualTo(arrayType);
		assertThat(mixedCodeVar.getType()).isEqualTo(cursorType);
		assertThat(mixedCodeVar.getSsaVars()).containsExactly(cursorVar, nullVar, phiVar);
		assertThat(nullInsn.getArg(0).getType()).isEqualTo(cursorType);
	}

	@Test
	void selectKotlinFunctionInterfaceForConcreteInvokeUse() {
		ArgType functionType = ArgType.object("kotlin.jvm.functions.Function3");
		ArgType concreteType = ArgType.object("test.SyntheticLambda");
		RegisterArg assign = InsnArg.reg(0, ArgType.UNKNOWN);
		SSAVar var = new SSAVar(0, 0, assign);
		var.getTypeInfo().getBounds().add(new TypeBoundConst(BoundEnum.ASSIGN, functionType));
		var.getTypeInfo().getBounds().add(new TypeBoundConst(BoundEnum.USE, concreteType));

		RegisterArg use = InsnArg.reg(0, concreteType);
		var.use(use);
		RootNode root = new RootNode(new JadxArgs());
		MethodInfo invokeMth = MethodInfo.fromDetails(
				root, ClassInfo.fromName(root, "test.SyntheticLambda"), "invoke", List.of(), ArgType.OBJECT);
		InvokeNode invoke = new InvokeNode(invokeMth, InvokeType.VIRTUAL, 1);
		invoke.addArg(use);

		assertThat(FinishTypeInference.selectKotlinFunctionAssignType(List.of(var))).isEqualTo(functionType);
	}

	@Test
	void selectMatchingAssignmentAndInvokeReceiverType() {
		ArgType serviceType = ArgType.object("test.ServiceImpl");
		RegisterArg assign = InsnArg.reg(0, ArgType.UNKNOWN);
		SSAVar var = new SSAVar(0, 0, assign);
		var.getTypeInfo().getBounds().add(new TypeBoundConst(BoundEnum.ASSIGN, serviceType));
		var.getTypeInfo().getBounds().add(new TypeBoundConst(BoundEnum.USE, ArgType.object("android.content.Context")));

		RegisterArg use = InsnArg.reg(0, serviceType);
		var.use(use);
		RootNode root = new RootNode(new JadxArgs());
		MethodInfo callMth = MethodInfo.fromDetails(
				root, ClassInfo.fromName(root, "test.ServiceImpl"), "run", List.of(), ArgType.VOID);
		InvokeNode invoke = new InvokeNode(callMth, InvokeType.VIRTUAL, 1);
		invoke.addArg(use);

		assertThat(FinishTypeInference.selectInvokeReceiverAssignType(List.of(var))).isEqualTo(serviceType);
	}

	@Test
	void selectGenericLocalForConcreteCollectionUse() {
		ArgType linkedHashMapType = ArgType.object("java.util.LinkedHashMap");
		ArgType mapType = ArgType.object("java.util.Map");
		ArgType genericType = ArgType.genericType("T");
		RegisterArg assign = InsnArg.reg(0, linkedHashMapType);
		SSAVar var = new SSAVar(0, 0, assign);
		var.getTypeInfo().getBounds().add(new TypeBoundConst(BoundEnum.USE, genericType));
		var.getTypeInfo().getBounds().add(new TypeBoundConst(BoundEnum.USE, mapType));
		InsnNode cast = new InsnNode(InsnType.CHECK_CAST, 1);
		cast.setResult(assign);
		cast.addArg(InsnArg.reg(1, ArgType.OBJECT));

		RegisterArg mapUse = InsnArg.reg(0, mapType);
		var.use(mapUse);
		InsnNode invoke = new InsnNode(InsnType.INVOKE, 1);
		invoke.addArg(mapUse);

		RegisterArg returnUse = InsnArg.reg(0, genericType);
		var.use(returnUse);
		InsnNode returnInsn = new InsnNode(InsnType.RETURN, 1);
		returnInsn.addArg(returnUse);

		assertThat(FinishTypeInference.selectGenericReturnType(List.of(var))).isEqualTo(genericType);
	}

	@Test
	void selectPrimitiveMoveSourceType() {
		RegisterArg sourceAssign = InsnArg.reg(1, ArgType.INT);
		SSAVar sourceVar = new SSAVar(1, 0, sourceAssign);
		CodeVar sourceCodeVar = new CodeVar();
		sourceCodeVar.setType(ArgType.INT);
		sourceVar.setCodeVar(sourceCodeVar);

		RegisterArg targetAssign = InsnArg.reg(0, ArgType.UNKNOWN);
		SSAVar targetVar = new SSAVar(0, 0, targetAssign);
		CodeVar targetCodeVar = new CodeVar();
		targetVar.setCodeVar(targetCodeVar);
		InsnNode move = new InsnNode(InsnType.MOVE, 1);
		move.setResult(targetAssign);
		move.addArg(sourceAssign.duplicate());

		assertThat(FinishTypeInference.selectMoveSourceType(List.of(targetVar), targetCodeVar))
				.isEqualTo(ArgType.INT);
	}

	@Test
	void allowLargeMethodBooleanPhiRepairOnlyForSingleIntArrayStore() {
		RegisterArg valueAssign = InsnArg.reg(0, ArgType.UNKNOWN);
		SSAVar valueVar = new SSAVar(0, 0, valueAssign);
		valueVar.setCodeVar(new CodeVar());

		ArgType intArray = ArgType.array(ArgType.INT);
		RegisterArg arrayAssign = InsnArg.reg(1, intArray);
		SSAVar arrayVar = new SSAVar(1, 0, arrayAssign);
		CodeVar arrayCodeVar = new CodeVar();
		arrayVar.setCodeVar(arrayCodeVar);
		arrayVar.setType(intArray);

		RegisterArg valueUse = valueAssign.duplicate();
		valueVar.use(valueUse);
		InsnNode arrayPut = new InsnNode(InsnType.APUT, 3);
		arrayPut.addArg(arrayAssign.duplicate());
		arrayPut.addArg(InsnArg.lit(0, ArgType.INT));
		arrayPut.addArg(valueUse);

		assertThat(FinishTypeInference.isSingleIntArrayStoreUse(valueVar)).isTrue();

		arrayVar.setType(ArgType.array(ArgType.FLOAT));
		assertThat(FinishTypeInference.isSingleIntArrayStoreUse(valueVar)).isFalse();
	}

	@Test
	void replaceNullOnlyPhiWithDifferentReferenceUses() {
		RegisterArg firstNullAssign = InsnArg.reg(0, ArgType.object("java.lang.reflect.Method"));
		SSAVar firstNullVar = new SSAVar(0, 0, firstNullAssign);
		InsnNode firstNull = new InsnNode(InsnType.CONST, 1);
		firstNull.setResult(firstNullAssign);
		firstNull.addArg(InsnArg.lit(0, ArgType.object("java.lang.reflect.Method")));

		RegisterArg secondNullAssign = InsnArg.reg(0, ArgType.object("java.lang.reflect.Method"));
		SSAVar secondNullVar = new SSAVar(0, 1, secondNullAssign);
		InsnNode secondNull = new InsnNode(InsnType.CONST, 1);
		secondNull.setResult(secondNullAssign);
		secondNull.addArg(InsnArg.lit(0, ArgType.object("java.lang.reflect.Method")));

		CodeVar sharedCodeVar = new CodeVar();
		RegisterArg phiAssign = InsnArg.reg(0, ArgType.UNKNOWN);
		SSAVar phiVar = new SSAVar(0, 2, phiAssign);
		phiVar.setCodeVar(sharedCodeVar);
		PhiInsn phiInsn = new PhiInsn(0, 2);
		phiInsn.setResult(phiAssign);
		phiInsn.bindArg(firstNullAssign.duplicate(), new BlockNode(0, 0, 0));
		phiInsn.bindArg(secondNullAssign.duplicate(), new BlockNode(1, 0, 0));

		addReferenceUse(phiAssign, ArgType.object("android.view.View"));
		addReferenceUse(phiAssign, ArgType.object("android.widget.TextView"));
		addReferenceUse(phiAssign, ArgType.object("android.view.ViewGroup"));

		RegisterArg passAssign = InsnArg.reg(1, ArgType.UNKNOWN_OBJECT);
		SSAVar passVar = new SSAVar(1, 0, passAssign);
		passVar.setCodeVar(new CodeVar());
		InsnNode passMove = new InsnNode(InsnType.MOVE, 1);
		passMove.setResult(passAssign);
		RegisterArg passUse = phiAssign.duplicate();
		phiVar.use(passUse);
		passMove.addArg(passUse);

		ArgType stringArray = ArgType.array(ArgType.STRING);
		RegisterArg arrayAssign = InsnArg.reg(2, stringArray);
		SSAVar arrayVar = new SSAVar(2, 0, arrayAssign);
		CodeVar arrayCodeVar = new CodeVar();
		arrayCodeVar.setType(stringArray);
		arrayVar.setCodeVar(arrayCodeVar);
		InsnNode arrayMove = new InsnNode(InsnType.MOVE, 1);
		arrayMove.setResult(arrayAssign);
		RegisterArg arrayUse = passAssign.duplicate();
		passVar.use(arrayUse);
		arrayMove.addArg(arrayUse);

		assertThat(FinishTypeInference.replaceNullOnlyPhiUses(null, List.of(phiVar))).isTrue();
		assertThat(phiVar.getUseList()).isEmpty();
		assertThat(passVar.getUseList()).isEmpty();
		assertThat(phiInsn.contains(AFlag.DONT_GENERATE)).isTrue();
		assertThat(passMove.contains(AFlag.DONT_GENERATE)).isTrue();
		assertThat(arrayMove.getArg(0).isZeroConst()).isTrue();
		assertThat(arrayMove.getArg(0).getType()).isEqualTo(stringArray);
		assertThat(sharedCodeVar.getType()).isEqualTo(ArgType.OBJECT);
	}

	@Test
	void replaceGroundedNullPhiCycleAcrossCodeVars() {
		RegisterArg nullAssign = InsnArg.reg(0, ArgType.UNKNOWN_OBJECT);
		SSAVar nullVar = new SSAVar(0, 0, nullAssign);
		nullVar.setCodeVar(new CodeVar());
		InsnNode nullInsn = new InsnNode(InsnType.CONST, 1);
		nullInsn.setResult(nullAssign);
		nullInsn.addArg(InsnArg.lit(0, ArgType.UNKNOWN_OBJECT));

		RegisterArg firstAssign = InsnArg.reg(0, ArgType.UNKNOWN_OBJECT);
		SSAVar firstVar = new SSAVar(0, 1, firstAssign);
		firstVar.setCodeVar(new CodeVar());
		PhiInsn firstPhi = new PhiInsn(0, 2);
		firstPhi.setResult(firstAssign);

		RegisterArg secondAssign = InsnArg.reg(1, ArgType.UNKNOWN_OBJECT);
		SSAVar secondVar = new SSAVar(1, 0, secondAssign);
		secondVar.setCodeVar(new CodeVar());
		InsnNode secondMove = new InsnNode(InsnType.MOVE, 1);
		secondMove.setResult(secondAssign);

		RegisterArg nullUse = nullAssign.duplicate();
		nullVar.use(nullUse);
		firstPhi.bindArg(nullUse, new BlockNode(0, 0, 0));
		RegisterArg secondUse = secondAssign.duplicate();
		secondVar.use(secondUse);
		firstPhi.bindArg(secondUse, new BlockNode(1, 0, 0));
		RegisterArg firstUse = firstAssign.duplicate();
		firstVar.use(firstUse);
		secondMove.addArg(firstUse);

		ArgType concreteGenericTarget = ArgType.generic("java.util.Map", ArgType.STRING, ArgType.STRING);
		RegisterArg genericReturnUse = firstAssign.duplicate();
		genericReturnUse.forceSetInitType(concreteGenericTarget);
		firstVar.use(genericReturnUse);
		InsnNode genericReturn = new InsnNode(InsnType.RETURN, 1);
		genericReturn.addArg(genericReturnUse);
		addReferenceUse(secondAssign, ArgType.object("org.example.SecondTarget"));
		ArgType stateType = ArgType.object("org.example.State");
		RegisterArg stateAssign = InsnArg.reg(2, stateType);
		SSAVar stateVar = new SSAVar(2, 0, stateAssign);
		CodeVar stateCodeVar = new CodeVar();
		stateCodeVar.setType(stateType);
		stateCodeVar.setSsaVars(List.of(stateVar));
		stateVar.setCodeVar(stateCodeVar);
		InsnNode stateMove = new InsnNode(InsnType.MOVE, 1);
		stateMove.setResult(stateAssign);
		RegisterArg stateSource = firstAssign.duplicate();
		firstVar.use(stateSource);
		stateMove.addArg(stateSource);
		RegisterArg stateUse = stateAssign.duplicate();
		stateVar.use(stateUse);
		IfNode stateCheck = new IfNode(IfOp.EQ, -1, stateUse, LiteralArg.make(0, ArgType.INT));

		assertThat(FinishTypeInference.replaceNullOnlyPhiUses(null, List.of(firstVar))).isTrue();
		assertThat(nullInsn.contains(AFlag.DONT_GENERATE)).isTrue();
		assertThat(firstPhi.contains(AFlag.DONT_GENERATE)).isTrue();
		assertThat(secondMove.contains(AFlag.DONT_GENERATE)).isTrue();
		assertThat(firstVar.getUseList()).hasSize(1);
		assertThat(secondVar.getUseList()).hasSize(1);
		assertThat(genericReturn.getArg(0).isZeroConst()).isTrue();
		assertThat(genericReturn.getArg(0).getType()).isEqualTo(concreteGenericTarget);
		assertThat(stateMove.getArg(0).isZeroConst()).isTrue();
		assertThat(stateMove.getArg(0).getType()).isEqualTo(stateType);
		assertThat(stateCheck.getArg(1).getType()).isEqualTo(stateType);
	}

	@Test
	void rejectGroundedNullPhiCycleAtUnresolvedGenericBoundary() {
		RegisterArg nullAssign = InsnArg.reg(0, ArgType.UNKNOWN_OBJECT);
		SSAVar nullVar = new SSAVar(0, 0, nullAssign);
		nullVar.setCodeVar(new CodeVar());
		InsnNode nullInsn = new InsnNode(InsnType.CONST, 1);
		nullInsn.setResult(nullAssign);
		nullInsn.addArg(InsnArg.lit(0, ArgType.UNKNOWN_OBJECT));

		RegisterArg phiAssign = InsnArg.reg(0, ArgType.UNKNOWN_OBJECT);
		SSAVar phiVar = new SSAVar(0, 1, phiAssign);
		phiVar.setCodeVar(new CodeVar());
		PhiInsn phiInsn = new PhiInsn(0, 2);
		phiInsn.setResult(phiAssign);

		RegisterArg moveAssign = InsnArg.reg(1, ArgType.UNKNOWN_OBJECT);
		SSAVar moveVar = new SSAVar(1, 0, moveAssign);
		moveVar.setCodeVar(new CodeVar());
		InsnNode moveInsn = new InsnNode(InsnType.MOVE, 1);
		moveInsn.setResult(moveAssign);

		RegisterArg nullUse = nullAssign.duplicate();
		nullVar.use(nullUse);
		phiInsn.bindArg(nullUse, new BlockNode(0, 0, 0));
		RegisterArg moveUse = moveAssign.duplicate();
		moveVar.use(moveUse);
		phiInsn.bindArg(moveUse, new BlockNode(1, 0, 0));
		RegisterArg phiUse = phiAssign.duplicate();
		phiVar.use(phiUse);
		moveInsn.addArg(phiUse);

		ArgType unresolvedMap = ArgType.generic(
				"java.util.Map", ArgType.genericType("T"), ArgType.STRING);
		RegisterArg unresolvedReturnUse = phiAssign.duplicate();
		unresolvedReturnUse.forceSetInitType(unresolvedMap);
		phiVar.use(unresolvedReturnUse);
		InsnNode unresolvedReturn = new InsnNode(InsnType.RETURN, 1);
		unresolvedReturn.addArg(unresolvedReturnUse);
		addReferenceUse(moveAssign, ArgType.THROWABLE);

		assertThat(FinishTypeInference.replaceNullOnlyPhiUses(null, List.of(phiVar))).isFalse();
		assertThat(moveInsn.contains(AFlag.DONT_GENERATE)).isFalse();
	}

	@Test
	void repairClosedMixedPrimitiveReferencePhiRelay() {
		MixedPrimitiveReferencePhiRelayFixture fixture = new MixedPrimitiveReferencePhiRelayFixture(true);

		FinishTypeInference.repairMixedPrimitiveReferencePhiRelays(null, fixture.groups);

		assertThat(fixture.sharedCodeVar.getType()).isEqualTo(ArgType.OBJECT);
		assertThat(fixture.longOutputMove.getArg(0).isInsnWrap()).isTrue();
		assertThat(fixture.longOutputMove.getArg(0).getType()).isEqualTo(ArgType.LONG);
		assertThat(fixture.stringOutputMove.getArg(0).isInsnWrap()).isTrue();
		assertThat(fixture.stringOutputMove.getArg(0).getType()).isEqualTo(ArgType.STRING);
		assertThat(fixture.longInput.getAssignInsn().getArg(0).isRegister()).isTrue();
		assertThat(fixture.stringInput.getAssignInsn().getArg(0).isRegister()).isTrue();
	}

	@Test
	void rejectMixedPrimitiveReferencePhiRelayWithGeneratedBoundary() {
		MixedPrimitiveReferencePhiRelayFixture fixture = new MixedPrimitiveReferencePhiRelayFixture(false);

		FinishTypeInference.repairMixedPrimitiveReferencePhiRelays(null, fixture.groups);

		assertThat(fixture.sharedCodeVar.getType()).isNull();
		assertThat(fixture.longOutputMove.getArg(0).isRegister()).isTrue();
		assertThat(fixture.stringOutputMove.getArg(0).isRegister()).isTrue();
	}

	@Test
	void repairDeclaredNullableCoroutineObjectCarrierWithReferenceOnlyOutput() {
		NullableCoroutineObjectCarrierFixture fixture =
				new NullableCoroutineObjectCarrierFixture(ArgType.object("test.PointerInputChange"));

		FinishTypeInference.repairNullableCoroutineObjectCarriers(null, fixture.groups);

		assertThat(fixture.sharedCodeVar.getType()).isEqualTo(ArgType.OBJECT);
		assertThat(fixture.sharedCodeVar.isDeclared()).isTrue();
		assertThat(fixture.zeroInput.getAssignInsn().getArg(0).getType()).isEqualTo(ArgType.OBJECT);
		assertThat(fixture.outputMove.getArg(0).isInsnWrap()).isTrue();
		assertThat(fixture.outputMove.getArg(0).getType()).isEqualTo(fixture.outputType);
	}

	@Test
	void rejectNullableCoroutineObjectCarrierWithPrimitiveOutput() {
		NullableCoroutineObjectCarrierFixture fixture =
				new NullableCoroutineObjectCarrierFixture(ArgType.INT);

		FinishTypeInference.repairNullableCoroutineObjectCarriers(null, fixture.groups);

		assertThat(fixture.sharedCodeVar.getType()).isNull();
		assertThat(fixture.zeroInput.getAssignInsn().getArg(0).getType()).isEqualTo(ArgType.UNKNOWN);
		assertThat(fixture.outputMove.getArg(0).isRegister()).isTrue();
	}

	@Test
	void repairZeroInitializedPrimitivePhiRelay() {
		ZeroPrimitivePhiRelayFixture fixture = new ZeroPrimitivePhiRelayFixture(ArgType.FLOAT);

		FinishTypeInference.repairZeroPrimitivePhiRelays(fixture.groups);

		assertThat(fixture.sharedCodeVar.getType()).isEqualTo(ArgType.FLOAT);
		assertThat(fixture.zeroInput.getAssignInsn().getArg(0).getType()).isEqualTo(ArgType.FLOAT);
		assertThat(fixture.outputMove.getArg(0).isRegister()).isTrue();
	}

	@Test
	void rejectZeroInitializedPrimitivePhiRelayWithConflictingOutput() {
		ZeroPrimitivePhiRelayFixture fixture = new ZeroPrimitivePhiRelayFixture(ArgType.INT);

		FinishTypeInference.repairZeroPrimitivePhiRelays(fixture.groups);

		assertThat(fixture.sharedCodeVar.getType()).isNull();
		assertThat(fixture.zeroInput.getAssignInsn().getArg(0).getType()).isEqualTo(ArgType.UNKNOWN);
	}

	@Test
	void detectCoroutineFloatIntResumeCarrier() {
		CoroutineFloatIntCarrierFixture fixture = new CoroutineFloatIntCarrierFixture(false);

		FinishTypeInference.LateCoroutineFloatIntCarrier carrier =
				FinishTypeInference.collectLateCoroutineFloatIntCarrier(
						fixture.allVars, fixture.sharedCodeVar, fixture.group);

		assertThat(carrier).isNotNull();
		assertThat(carrier.floatOutput).isSameAs(fixture.floatOutput);
		assertThat(carrier.floatLoopBack).isSameAs(fixture.floatLoopBack);
		assertThat(carrier.intOutputs).hasSize(1);
	}

	@Test
	void rejectCoroutineFloatIntResumeCarrierWithExtraGeneratedUse() {
		CoroutineFloatIntCarrierFixture fixture = new CoroutineFloatIntCarrierFixture(true);

		assertThat(FinishTypeInference.collectLateCoroutineFloatIntCarrier(
				fixture.allVars, fixture.sharedCodeVar, fixture.group)).isNull();
	}

	@Test
	void repairNullableReferencePhiRelayWithObjectWidening() {
		NullableReferencePhiRelayFixture fixture = new NullableReferencePhiRelayFixture(ArgType.OBJECT);

		FinishTypeInference.repairNullableReferencePhiRelays(fixture.groups);

		assertThat(fixture.sharedCodeVar.getType()).isEqualTo(fixture.referenceType);
		assertThat(fixture.referenceOutputMove.getArg(0).isRegister()).isTrue();
		assertThat(fixture.wideningOutputMove.getArg(0).isRegister()).isTrue();
	}

	@Test
	void rejectNullableReferencePhiRelayWithConflictingOutput() {
		NullableReferencePhiRelayFixture fixture = new NullableReferencePhiRelayFixture(ArgType.STRING);

		FinishTypeInference.repairNullableReferencePhiRelays(fixture.groups);

		assertThat(fixture.sharedCodeVar.getType()).isNull();
		assertThat(fixture.referenceOutputMove.getArg(0).isRegister()).isTrue();
		assertThat(fixture.wideningOutputMove.getArg(0).isRegister()).isTrue();
	}

	@Test
	void replaceDirectNullWithTypedLiteralsAtDifferentReferenceUses() {
		RegisterArg nullAssign = InsnArg.reg(0, ArgType.UNKNOWN_OBJECT);
		SSAVar nullVar = new SSAVar(0, 0, nullAssign);
		InsnNode nullInsn = new InsnNode(InsnType.CONST, 1);
		nullInsn.setResult(nullAssign);
		nullInsn.addArg(InsnArg.lit(0, ArgType.UNKNOWN_OBJECT));

		addReferenceUse(nullAssign, ArgType.STRING);
		addReferenceUse(nullAssign, ArgType.object("kotlin.jvm.internal.DefaultConstructorMarker"));

		assertThat(FinishTypeInference.replaceProvenNullMultiTypeUses(null, nullVar)).isTrue();
		assertThat(nullVar.getUseList()).isEmpty();
	}

	@Test
	void selectNullablePhiTypeAcrossNullCheck() {
		CodeVar sharedCodeVar = new CodeVar();
		RegisterArg nullAssign = InsnArg.reg(0, ArgType.UNKNOWN_OBJECT);
		SSAVar nullVar = new SSAVar(0, 0, nullAssign);
		nullVar.setCodeVar(sharedCodeVar);
		InsnNode nullInsn = new InsnNode(InsnType.CONST, 1);
		nullInsn.setResult(nullAssign);
		nullInsn.addArg(InsnArg.lit(0, ArgType.UNKNOWN_OBJECT));

		RegisterArg stringSource = makeKnownSource(1, ArgType.STRING);
		RegisterArg stringAssign = InsnArg.reg(0, ArgType.UNKNOWN_OBJECT);
		SSAVar stringVar = new SSAVar(0, 1, stringAssign);
		stringVar.setCodeVar(sharedCodeVar);
		InsnNode stringMove = new InsnNode(InsnType.MOVE, 1);
		stringMove.setResult(stringAssign);
		stringMove.addArg(stringSource.duplicate());

		RegisterArg phiAssign = InsnArg.reg(0, ArgType.UNKNOWN_OBJECT);
		SSAVar phiVar = new SSAVar(0, 2, phiAssign);
		phiVar.setCodeVar(sharedCodeVar);
		PhiInsn phiInsn = new PhiInsn(0, 2);
		phiInsn.setResult(phiAssign);
		phiInsn.bindArg(nullAssign.duplicate(), new BlockNode(0, 0, 0));
		phiInsn.bindArg(stringAssign.duplicate(), new BlockNode(1, 0, 0));
		new IfNode(IfOp.EQ, -1, phiAssign.duplicate(), LiteralArg.make(0, ArgType.INT));

		RegisterArg targetAssign = InsnArg.reg(2, ArgType.STRING);
		SSAVar targetVar = new SSAVar(2, 0, targetAssign);
		CodeVar targetCodeVar = new CodeVar();
		targetCodeVar.setType(ArgType.STRING);
		targetVar.setCodeVar(targetCodeVar);
		InsnNode targetMove = new InsnNode(InsnType.MOVE, 1);
		targetMove.setResult(targetAssign);
		targetMove.addArg(phiAssign.duplicate());
		RegisterArg returnUse = targetAssign.duplicate();
		targetVar.use(returnUse);
		InsnNode returnInsn = new InsnNode(InsnType.RETURN, 1);
		returnInsn.addArg(returnUse);

		assertThat(FinishTypeInference.selectNullablePhiUseType(List.of(nullVar, stringVar, phiVar)))
				.isEqualTo(ArgType.STRING);
	}

	@Test
	void selectMoveTargetTypeAcrossInternalPhiEdge() {
		ArgType byteBufferType = ArgType.object("java.nio.ByteBuffer");
		CodeVar sharedCodeVar = new CodeVar();

		RegisterArg inputAssign = InsnArg.reg(0, ArgType.UNKNOWN);
		SSAVar inputVar = new SSAVar(0, 0, inputAssign);
		inputVar.setCodeVar(sharedCodeVar);

		RegisterArg phiAssign = InsnArg.reg(0, ArgType.UNKNOWN);
		SSAVar phiVar = new SSAVar(0, 1, phiAssign);
		phiVar.setCodeVar(sharedCodeVar);
		PhiInsn phiInsn = new PhiInsn(0, 1);
		phiInsn.setResult(phiAssign);
		phiInsn.bindArg(inputAssign.duplicate(), new BlockNode(0, 0, 0));

		RegisterArg targetAssign = InsnArg.reg(1, byteBufferType);
		SSAVar targetVar = new SSAVar(1, 0, targetAssign);
		CodeVar targetCodeVar = new CodeVar();
		targetCodeVar.setType(byteBufferType);
		targetVar.setCodeVar(targetCodeVar);
		InsnNode move = new InsnNode(InsnType.MOVE, 1);
		move.setResult(targetAssign);
		move.addArg(phiAssign.duplicate());

		assertThat(FinishTypeInference.selectMoveTargetType(List.of(inputVar, phiVar), sharedCodeVar))
				.isEqualTo(byteBufferType);
	}

	@Test
	void rejectConflictingMoveTargetTypesAcrossInternalPhiEdge() {
		ArgType byteBufferType = ArgType.object("java.nio.ByteBuffer");
		ArgType objectPoolType = ArgType.object("io.ktor.utils.io.pool.ObjectPool");
		CodeVar sharedCodeVar = new CodeVar();

		RegisterArg inputAssign = InsnArg.reg(0, ArgType.UNKNOWN);
		SSAVar inputVar = new SSAVar(0, 0, inputAssign);
		inputVar.setCodeVar(sharedCodeVar);

		RegisterArg phiAssign = InsnArg.reg(0, ArgType.UNKNOWN);
		SSAVar phiVar = new SSAVar(0, 1, phiAssign);
		phiVar.setCodeVar(sharedCodeVar);
		PhiInsn phiInsn = new PhiInsn(0, 1);
		phiInsn.setResult(phiAssign);
		phiInsn.bindArg(inputAssign.duplicate(), new BlockNode(0, 0, 0));

		addMoveUse(phiAssign, 1, byteBufferType);
		addMoveUse(phiAssign, 2, objectPoolType);

		assertThat(FinishTypeInference.selectMoveTargetType(List.of(inputVar, phiVar), sharedCodeVar)).isNull();
	}

	@Test
	void inferIntFromSyntheticMoveTargetAcrossInternalPhi() {
		CodeVar sharedCodeVar = new CodeVar();

		RegisterArg constAssign = InsnArg.reg(0, ArgType.NARROW);
		SSAVar constVar = new SSAVar(0, 0, constAssign);
		constVar.setCodeVar(sharedCodeVar);
		InsnNode constInsn = new InsnNode(InsnType.CONST, 1);
		constInsn.setResult(constAssign);
		constInsn.addArg(LiteralArg.make(6, ArgType.NARROW));

		RegisterArg phiAssign = InsnArg.reg(0, ArgType.UNKNOWN);
		SSAVar phiVar = new SSAVar(0, 1, phiAssign);
		phiVar.setCodeVar(sharedCodeVar);
		PhiInsn phiInsn = new PhiInsn(0, 1);
		phiInsn.setResult(phiAssign);
		phiInsn.bindArg(constAssign.duplicate(), new BlockNode(0, 0, 0));

		RegisterArg targetAssign = InsnArg.reg(1, ArgType.INT);
		SSAVar targetVar = new SSAVar(1, 0, targetAssign);
		CodeVar targetCodeVar = new CodeVar();
		targetCodeVar.setType(ArgType.INT);
		targetVar.setCodeVar(targetCodeVar);
		InsnNode move = new InsnNode(InsnType.MOVE, 1);
		move.add(AFlag.SYNTHETIC);
		move.setResult(targetAssign);
		move.addArg(phiAssign.duplicate());

		assertThat(FinishTypeInference.selectSyntheticIntMoveTargetType(
				List.of(constVar, phiVar), sharedCodeVar)).isEqualTo(ArgType.INT);
	}

	@Test
	void rejectNonIntSourceForSyntheticIntMoveTarget() {
		CodeVar sharedCodeVar = new CodeVar();

		RegisterArg stringAssign = InsnArg.reg(2, ArgType.STRING);
		SSAVar stringVar = new SSAVar(2, 0, stringAssign);
		CodeVar stringCodeVar = new CodeVar();
		stringCodeVar.setType(ArgType.STRING);
		stringVar.setCodeVar(stringCodeVar);

		RegisterArg inputAssign = InsnArg.reg(0, ArgType.UNKNOWN);
		SSAVar inputVar = new SSAVar(0, 0, inputAssign);
		inputVar.setCodeVar(sharedCodeVar);
		InsnNode inputMove = new InsnNode(InsnType.MOVE, 1);
		inputMove.setResult(inputAssign);
		inputMove.addArg(stringAssign.duplicate());

		RegisterArg targetAssign = InsnArg.reg(1, ArgType.INT);
		SSAVar targetVar = new SSAVar(1, 0, targetAssign);
		CodeVar targetCodeVar = new CodeVar();
		targetCodeVar.setType(ArgType.INT);
		targetVar.setCodeVar(targetCodeVar);
		InsnNode targetMove = new InsnNode(InsnType.MOVE, 1);
		targetMove.add(AFlag.SYNTHETIC);
		targetMove.setResult(targetAssign);
		targetMove.addArg(inputAssign.duplicate());

		assertThat(FinishTypeInference.selectSyntheticIntMoveTargetType(
				List.of(inputVar), sharedCodeVar)).isNull();
	}

	@Test
	void inferClosedReferenceMoveFlowAcrossNullablePhi() {
		ArgType jsonType = ArgType.object("org.json.JSONObject");
		CodeVar flowCodeVar = new CodeVar();
		flowCodeVar.setDeclared(true);

		RegisterArg jsonAssign = InsnArg.reg(1, jsonType);
		SSAVar jsonVar = new SSAVar(1, 0, jsonAssign);
		CodeVar jsonCodeVar = new CodeVar();
		jsonCodeVar.setType(jsonType);
		jsonVar.setCodeVar(jsonCodeVar);

		RegisterArg nullAssign = InsnArg.reg(2, ArgType.UNKNOWN_OBJECT);
		SSAVar nullVar = new SSAVar(2, 0, nullAssign);
		CodeVar objectCodeVar = new CodeVar();
		objectCodeVar.setType(ArgType.OBJECT);
		nullVar.setCodeVar(objectCodeVar);
		InsnNode nullInsn = new InsnNode(InsnType.MOVE, 1);
		nullInsn.setResult(nullAssign);
		nullInsn.addArg(LiteralArg.make(0, ArgType.UNKNOWN_OBJECT));

		RegisterArg concreteMoveAssign = InsnArg.reg(0, ArgType.UNKNOWN_OBJECT);
		SSAVar concreteMoveVar = new SSAVar(0, 0, concreteMoveAssign);
		concreteMoveVar.setCodeVar(flowCodeVar);
		InsnNode concreteMove = new InsnNode(InsnType.MOVE, 1);
		concreteMove.setResult(concreteMoveAssign);
		concreteMove.addArg(jsonAssign.duplicate());

		RegisterArg nullMoveAssign = InsnArg.reg(0, ArgType.UNKNOWN_OBJECT);
		SSAVar nullMoveVar = new SSAVar(0, 1, nullMoveAssign);
		nullMoveVar.setCodeVar(flowCodeVar);
		InsnNode nullMove = new InsnNode(InsnType.MOVE, 1);
		nullMove.setResult(nullMoveAssign);
		nullMove.addArg(nullAssign.duplicate());

		RegisterArg phiAssign = InsnArg.reg(0, ArgType.UNKNOWN_OBJECT);
		SSAVar phiVar = new SSAVar(0, 2, phiAssign);
		phiVar.setCodeVar(flowCodeVar);
		PhiInsn phiInsn = new PhiInsn(0, 2);
		phiInsn.setResult(phiAssign);
		phiInsn.bindArg(concreteMoveAssign.duplicate(), new BlockNode(0, 0, 0));
		phiInsn.bindArg(nullMoveAssign.duplicate(), new BlockNode(1, 0, 0));

		RegisterArg internalMoveAssign = InsnArg.reg(0, ArgType.UNKNOWN_OBJECT);
		SSAVar internalMoveVar = new SSAVar(0, 3, internalMoveAssign);
		internalMoveVar.setCodeVar(flowCodeVar);
		InsnNode internalMove = new InsnNode(InsnType.MOVE, 1);
		internalMove.add(AFlag.SYNTHETIC);
		internalMove.setResult(internalMoveAssign);
		internalMove.addArg(phiAssign.duplicate());

		RegisterArg targetAssign = InsnArg.reg(3, jsonType);
		SSAVar targetVar = new SSAVar(3, 0, targetAssign);
		CodeVar targetCodeVar = new CodeVar();
		targetCodeVar.setType(jsonType);
		targetVar.setCodeVar(targetCodeVar);
		InsnNode targetMove = new InsnNode(InsnType.MOVE, 1);
		targetMove.setResult(targetAssign);
		targetMove.addArg(internalMoveAssign.duplicate());

		assertThat(FinishTypeInference.selectClosedReferenceMoveType(
				List.of(concreteMoveVar, nullMoveVar, phiVar, internalMoveVar), flowCodeVar)).isEqualTo(jsonType);
	}

	@Test
	void inferClosedGenericReferenceFlowAtMatchingInvokeArgument() {
		ClosedGenericReferenceInvokeFixture fixture = new ClosedGenericReferenceInvokeFixture(
				ArgType.generic("test.Parser", ArgType.genericType("UT"), ArgType.genericType("UB")));

		assertThat(FinishTypeInference.selectClosedReferenceMoveType(fixture.group, fixture.flowCodeVar))
				.isEqualTo(ArgType.object("test.Parser"));
		assertThat(FinishTypeInference.isReferenceOnlyPhiFlow(fixture.group, fixture.flowCodeVar)).isTrue();
	}

	@Test
	void rejectClosedGenericReferenceFlowAtDifferentInvokeArgument() {
		ClosedGenericReferenceInvokeFixture fixture = new ClosedGenericReferenceInvokeFixture(
				ArgType.generic("test.Other", ArgType.genericType("UT"), ArgType.genericType("UB")));

		assertThat(FinishTypeInference.selectClosedReferenceMoveType(fixture.group, fixture.flowCodeVar)).isNull();
		FinishTypeInference.repairLateReferenceOnlyPhiFlows(null, fixture.group);
		assertThat(fixture.flowCodeVar.getType()).isEqualTo(ArgType.OBJECT);
		assertThat(fixture.invoke.getArg(0).isInsnWrap()).isTrue();
	}

	@Test
	void rejectPrimitiveCapablePhiAsReferenceOnlyFlow() {
		CodeVar intCodeVar = typedCodeVar(ArgType.INT);
		SSAVar intSource = makeSsaVar(1, 0, ArgType.INT, intCodeVar);
		bindConst(intSource, 1, ArgType.INT);

		CodeVar flowCodeVar = new CodeVar();
		SSAVar input = makeSsaVar(0, 0, ArgType.UNKNOWN, flowCodeVar);
		bindMove(input, intSource);
		SSAVar phiResult = makeSsaVar(0, 1, ArgType.UNKNOWN, flowCodeVar);
		PhiInsn phi = makePhi(phiResult);
		bindPhiArg(phi, input, 0);
		bindPhiArg(phi, input, 1);

		SSAVar objectTarget = makeSsaVar(2, 0, ArgType.OBJECT, typedCodeVar(ArgType.OBJECT));
		InsnNode targetMove = new InsnNode(InsnType.MOVE, 1);
		targetMove.setResult(objectTarget.getAssign());
		RegisterArg targetUse = phiResult.getAssign().duplicate();
		phiResult.use(targetUse);
		targetMove.addArg(targetUse);

		List<SSAVar> group = List.of(input, phiResult);
		flowCodeVar.setSsaVars(group);
		assertThat(FinishTypeInference.isReferenceOnlyPhiFlow(group, flowCodeVar)).isFalse();
		FinishTypeInference.repairLateReferenceOnlyPhiFlows(null, group);
		assertThat(flowCodeVar.getType()).isNull();
	}

	@Test
	void replaceStaleStringHandlerPhiInputWithThis() {
		StaleStringHandlerPhiFixture fixture = new StaleStringHandlerPhiFixture();

		assertThat(FinishTypeInference.repairStaleConstStringHandlerPhi(
				fixture.group, fixture.thisVar, fixture.ownerType, insn -> insn == fixture.phi)).isTrue();
		assertThat(fixture.phi.getArguments()).extracting(arg -> ((RegisterArg) arg).getSVar())
				.containsExactly(fixture.thisVar, fixture.receiverInput);
		assertThat(fixture.sharedCodeVar.getType()).isEqualTo(fixture.ownerType);
		assertThat(fixture.sharedCodeVar.getName()).isEqualTo("ownerVar");
		assertThat(fixture.staleString.getCodeVar().getType()).isEqualTo(ArgType.STRING);
		assertThat(fixture.staleString.getAssignInsn().contains(AFlag.DONT_GENERATE)).isTrue();
	}

	@Test
	void rejectStaleStringPhiOutsideExceptionHandler() {
		StaleStringHandlerPhiFixture fixture = new StaleStringHandlerPhiFixture();

		assertThat(FinishTypeInference.repairStaleConstStringHandlerPhi(
				fixture.group, fixture.thisVar, fixture.ownerType, insn -> false)).isFalse();
		assertThat(fixture.phi.getArguments()).extracting(arg -> ((RegisterArg) arg).getSVar())
				.containsExactly(fixture.staleString, fixture.receiverInput);
	}

	@Test
	void splitStaleStringHandlerInputFromClosedIntFlow() {
		StaleStringHandlerIntFlowFixture fixture = new StaleStringHandlerIntFlowFixture();

		assertThat(FinishTypeInference.repairStaleConstStringHandlerIntFlow(
				fixture.groups, insn -> insn == fixture.handlerPhi)).isTrue();
		assertThat(fixture.handlerPhi.getArguments()).extracting(arg -> ((RegisterArg) arg).getSVar())
				.containsExactly(fixture.intInput, fixture.intInput);
		assertThat(fixture.stringCodeVar.getSsaVars()).containsExactly(fixture.staleString);
		assertThat(fixture.staleString.getCodeVar().getType()).isEqualTo(ArgType.STRING);
		assertThat(fixture.statusResult.getCodeVar().getType()).isEqualTo(ArgType.INT);
		assertThat(fixture.statusResult.getCodeVar().getName()).isNull();
		assertThat(fixture.intInput.getCodeVar().getType()).isEqualTo(ArgType.INT);
		assertThat(fixture.intInput.getCodeVar().getName()).isNull();
		assertThat(fixture.intInput.getCodeVar()).isNotSameAs(fixture.statusResult.getCodeVar());
	}

	@Test
	void rejectStaleStringIntFlowOutsideExceptionHandler() {
		StaleStringHandlerIntFlowFixture fixture = new StaleStringHandlerIntFlowFixture();

		assertThat(FinishTypeInference.repairStaleConstStringHandlerIntFlow(
				fixture.groups, insn -> false)).isFalse();
		assertThat(fixture.handlerPhi.getArguments()).extracting(arg -> ((RegisterArg) arg).getSVar())
				.containsExactly(fixture.staleString, fixture.intInput);
	}

	@Test
	void rejectClosedReferenceMoveFlowWithConflictingInput() {
		ArgType jsonType = ArgType.object("org.json.JSONObject");
		CodeVar flowCodeVar = new CodeVar();

		RegisterArg jsonAssign = InsnArg.reg(1, jsonType);
		SSAVar jsonVar = new SSAVar(1, 0, jsonAssign);
		CodeVar jsonCodeVar = new CodeVar();
		jsonCodeVar.setType(jsonType);
		jsonVar.setCodeVar(jsonCodeVar);

		RegisterArg stringAssign = InsnArg.reg(2, ArgType.STRING);
		SSAVar stringVar = new SSAVar(2, 0, stringAssign);
		CodeVar stringCodeVar = new CodeVar();
		stringCodeVar.setType(ArgType.STRING);
		stringVar.setCodeVar(stringCodeVar);

		RegisterArg jsonMoveAssign = InsnArg.reg(0, ArgType.UNKNOWN_OBJECT);
		SSAVar jsonMoveVar = new SSAVar(0, 0, jsonMoveAssign);
		jsonMoveVar.setCodeVar(flowCodeVar);
		InsnNode jsonMove = new InsnNode(InsnType.MOVE, 1);
		jsonMove.setResult(jsonMoveAssign);
		jsonMove.addArg(jsonAssign.duplicate());

		RegisterArg stringMoveAssign = InsnArg.reg(0, ArgType.UNKNOWN_OBJECT);
		SSAVar stringMoveVar = new SSAVar(0, 1, stringMoveAssign);
		stringMoveVar.setCodeVar(flowCodeVar);
		InsnNode stringMove = new InsnNode(InsnType.MOVE, 1);
		stringMove.setResult(stringMoveAssign);
		stringMove.addArg(stringAssign.duplicate());

		RegisterArg phiAssign = InsnArg.reg(0, ArgType.UNKNOWN_OBJECT);
		SSAVar phiVar = new SSAVar(0, 2, phiAssign);
		phiVar.setCodeVar(flowCodeVar);
		PhiInsn phiInsn = new PhiInsn(0, 2);
		phiInsn.setResult(phiAssign);
		phiInsn.bindArg(jsonMoveAssign.duplicate(), new BlockNode(0, 0, 0));
		phiInsn.bindArg(stringMoveAssign.duplicate(), new BlockNode(1, 0, 0));

		assertThat(FinishTypeInference.selectClosedReferenceMoveType(
				List.of(jsonMoveVar, stringMoveVar, phiVar), flowCodeVar)).isNull();
	}

	private static final class ClosedGenericReferenceInvokeFixture {
		private final CodeVar flowCodeVar = new CodeVar();
		private final List<SSAVar> group;
		private final InvokeNode invoke;

		private ClosedGenericReferenceInvokeFixture(ArgType formalType) {
			ArgType genericType = ArgType.generic(
					"test.Parser", ArgType.genericType("UT"), ArgType.genericType("UB"));
			CodeVar genericCodeVar = typedCodeVar(genericType);
			SSAVar genericSource = makeSsaVar(1, 0, genericType, genericCodeVar);

			CodeVar objectCodeVar = typedCodeVar(ArgType.OBJECT);
			SSAVar nullSource = makeSsaVar(2, 0, ArgType.UNKNOWN_OBJECT, objectCodeVar);
			bindConst(nullSource, 0, ArgType.UNKNOWN_OBJECT);

			SSAVar concreteMove = makeSsaVar(0, 0, ArgType.UNKNOWN_OBJECT, flowCodeVar);
			bindMove(concreteMove, genericSource);
			SSAVar nullMove = makeSsaVar(0, 1, ArgType.UNKNOWN_OBJECT, flowCodeVar);
			bindMove(nullMove, nullSource);
			SSAVar phiResult = makeSsaVar(0, 2, ArgType.UNKNOWN_OBJECT, flowCodeVar);
			PhiInsn phi = makePhi(phiResult);
			bindPhiArg(phi, concreteMove, 0);
			bindPhiArg(phi, nullMove, 1);

			RootNode root = new RootNode(new JadxArgs());
			MethodInfo consumeMethod = MethodInfo.fromDetails(
					root, ClassInfo.fromName(root, "test.Consumer"), "consume", List.of(formalType), ArgType.VOID);
			invoke = new InvokeNode(consumeMethod, InvokeType.STATIC, 1);
			RegisterArg invokeUse = phiResult.getAssign().duplicate();
			phiResult.use(invokeUse);
			invoke.addArg(invokeUse);

			group = List.of(concreteMove, nullMove, phiResult);
			flowCodeVar.setSsaVars(group);
			genericCodeVar.setSsaVars(List.of(genericSource));
			objectCodeVar.setSsaVars(List.of(nullSource));
		}
	}

	private static final class StaleStringHandlerPhiFixture {
		private final ArgType ownerType = ArgType.object("test.Owner");
		private final CodeVar sharedCodeVar = new CodeVar();
		private final SSAVar thisVar;
		private final SSAVar staleString;
		private final SSAVar receiverInput;
		private final PhiInsn phi;
		private final List<SSAVar> group;

		private StaleStringHandlerPhiFixture() {
			CodeVar thisCodeVar = typedCodeVar(ownerType);
			thisVar = makeSsaVar(4, 0, ownerType, thisCodeVar);

			staleString = makeSsaVar(0, 0, ArgType.UNKNOWN_OBJECT, sharedCodeVar);
			IndexInsnNode stringConst = new IndexInsnNode(InsnType.CONST_STR, "_ai", 0);
			stringConst.setResult(staleString.getAssign());

			receiverInput = makeSsaVar(0, 1, ArgType.UNKNOWN_OBJECT, sharedCodeVar);
			bindMove(receiverInput, thisVar);
			SSAVar phiResult = makeSsaVar(0, 2, ArgType.UNKNOWN_OBJECT, sharedCodeVar);
			phi = makePhi(phiResult);
			bindPhiArg(phi, staleString, 0);
			bindPhiArg(phi, receiverInput, 1);

			RootNode root = new RootNode(new JadxArgs());
			MethodInfo receiverMethod = MethodInfo.fromDetails(
					root, ClassInfo.fromType(root, ownerType), "finish", List.of(), ArgType.VOID);
			InvokeNode invoke = new InvokeNode(receiverMethod, InvokeType.VIRTUAL, 1);
			RegisterArg receiverUse = phiResult.getAssign().duplicate();
			phiResult.use(receiverUse);
			invoke.addArg(receiverUse);

			CodeVar boundaryCodeVar = typedCodeVar(ownerType);
			boundaryCodeVar.setName("ownerVar");
			SSAVar boundaryTarget = makeSsaVar(1, 0, ownerType, boundaryCodeVar);
			bindMove(boundaryTarget, phiResult);
			boundaryCodeVar.setSsaVars(List.of(boundaryTarget));

			group = List.of(staleString, receiverInput, phiResult);
			sharedCodeVar.setSsaVars(group);
			thisCodeVar.setSsaVars(List.of(thisVar));
		}
	}

	private static final class StaleStringHandlerIntFlowFixture {
		private final CodeVar stringCodeVar = typedCodeVar(ArgType.STRING);
		private final CodeVar flowCodeVar = new CodeVar();
		private final SSAVar staleString;
		private final SSAVar intInput;
		private final SSAVar statusResult;
		private final PhiInsn handlerPhi;
		private final Map<CodeVar, List<SSAVar>> groups;

		private StaleStringHandlerIntFlowFixture() {
			stringCodeVar.setName("str");
			CodeVar intRootCodeVar = typedCodeVar(ArgType.INT);
			intRootCodeVar.setName("i");
			SSAVar intRoot = makeSsaVar(3, 0, ArgType.INT, intRootCodeVar);
			bindConst(intRoot, 7, ArgType.INT);
			intRootCodeVar.setSsaVars(List.of(intRoot));

			staleString = makeSsaVar(3, 1, ArgType.STRING, stringCodeVar);
			IndexInsnNode stringConst = new IndexInsnNode(InsnType.CONST_STR, "methodName", 0);
			stringConst.setResult(staleString.getAssign());

			intInput = makeSsaVar(3, 2, ArgType.UNKNOWN, stringCodeVar);
			bindMove(intInput, intRoot);
			SSAVar handlerResult = makeSsaVar(3, 3, ArgType.UNKNOWN, stringCodeVar);
			handlerPhi = makePhi(handlerResult);
			bindPhiArg(handlerPhi, staleString, 0);
			bindPhiArg(handlerPhi, intInput, 1);

			SSAVar bridge = makeSsaVar(5, 0, ArgType.UNKNOWN, flowCodeVar);
			bindMove(bridge, handlerResult);
			SSAVar zero = makeSsaVar(5, 1, ArgType.UNKNOWN, flowCodeVar);
			bindConst(zero, 0, ArgType.UNKNOWN);
			statusResult = makeSsaVar(5, 2, ArgType.UNKNOWN, flowCodeVar);
			PhiInsn statusPhi = makePhi(statusResult);
			bindPhiArg(statusPhi, bridge, 2);
			bindPhiArg(statusPhi, zero, 3);
			RegisterArg statusUse = statusResult.getAssign().duplicate();
			statusResult.use(statusUse);
			new IfNode(IfOp.GT, -1, statusUse, LiteralArg.make(0, ArgType.INT));

			List<SSAVar> stringGroup = List.of(staleString, intInput, handlerResult);
			List<SSAVar> flowGroup = List.of(bridge, zero, statusResult);
			stringCodeVar.setSsaVars(stringGroup);
			flowCodeVar.setSsaVars(flowGroup);
			groups = new IdentityHashMap<>();
			groups.put(stringCodeVar, stringGroup);
			groups.put(flowCodeVar, flowGroup);
			groups.put(intRootCodeVar, List.of(intRoot));
		}
	}

	private static final class MixedPrimitiveReferencePhiRelayFixture {
		private final CodeVar sharedCodeVar = new CodeVar();
		private final SSAVar longInput;
		private final SSAVar stringInput;
		private final InsnNode longOutputMove;
		private final InsnNode stringOutputMove;
		private final Map<CodeVar, List<SSAVar>> groups = new IdentityHashMap<>();

		private MixedPrimitiveReferencePhiRelayFixture(boolean syntheticOutputs) {
			CodeVar longSourceCodeVar = typedCodeVar(ArgType.LONG);
			SSAVar longSource = makeSsaVar(1, 0, ArgType.LONG, longSourceCodeVar);
			bindConst(longSource, 7, ArgType.LONG);
			longSourceCodeVar.setSsaVars(List.of(longSource));

			CodeVar stringSourceCodeVar = typedCodeVar(ArgType.STRING);
			SSAVar stringSource = makeSsaVar(2, 0, ArgType.STRING, stringSourceCodeVar);
			IndexInsnNode stringConst = new IndexInsnNode(InsnType.CONST_STR, "value", 0);
			stringConst.setResult(stringSource.getAssign());
			stringSourceCodeVar.setSsaVars(List.of(stringSource));

			longInput = makeSsaVar(0, 0, ArgType.UNKNOWN, sharedCodeVar);
			bindMove(longInput, longSource);
			stringInput = makeSsaVar(0, 1, ArgType.UNKNOWN, sharedCodeVar);
			bindMove(stringInput, stringSource);
			SSAVar phiResult = makeSsaVar(0, 2, ArgType.UNKNOWN, sharedCodeVar);
			PhiInsn phi = makePhi(phiResult);
			bindPhiArg(phi, longInput, 0);
			bindPhiArg(phi, stringInput, 1);

			CodeVar longOutputCodeVar = typedCodeVar(ArgType.LONG);
			SSAVar longOutput = makeSsaVar(3, 0, ArgType.LONG, longOutputCodeVar);
			bindMove(longOutput, phiResult);
			longOutputMove = longOutput.getAssignInsn();
			if (syntheticOutputs) {
				longOutputMove.add(AFlag.SYNTHETIC);
			}
			longOutputCodeVar.setSsaVars(List.of(longOutput));

			CodeVar stringOutputCodeVar = typedCodeVar(ArgType.STRING);
			SSAVar stringOutput = makeSsaVar(4, 0, ArgType.STRING, stringOutputCodeVar);
			bindMove(stringOutput, phiResult);
			stringOutputMove = stringOutput.getAssignInsn();
			if (syntheticOutputs) {
				stringOutputMove.add(AFlag.SYNTHETIC);
			}
			stringOutputCodeVar.setSsaVars(List.of(stringOutput));

			List<SSAVar> sharedGroup = List.of(longInput, stringInput, phiResult);
			sharedCodeVar.setSsaVars(sharedGroup);
			groups.put(sharedCodeVar, sharedGroup);
			groups.put(longSourceCodeVar, List.of(longSource));
			groups.put(stringSourceCodeVar, List.of(stringSource));
			groups.put(longOutputCodeVar, List.of(longOutput));
			groups.put(stringOutputCodeVar, List.of(stringOutput));
		}
	}

	private static final class NullableCoroutineObjectCarrierFixture {
		private final CodeVar sharedCodeVar = new CodeVar();
		private final SSAVar zeroInput;
		private final ArgType outputType;
		private final InsnNode outputMove;
		private final Map<CodeVar, List<SSAVar>> groups = new IdentityHashMap<>();

		private NullableCoroutineObjectCarrierFixture(ArgType outputType) {
			this.outputType = outputType;
			sharedCodeVar.setDeclared(true);

			CodeVar objectSourceCodeVar = typedCodeVar(ArgType.OBJECT);
			SSAVar objectSource = makeSsaVar(1, 0, ArgType.OBJECT, objectSourceCodeVar);
			objectSourceCodeVar.setSsaVars(List.of(objectSource));
			CodeVar floatSourceCodeVar = typedCodeVar(ArgType.FLOAT);
			SSAVar floatSource = makeSsaVar(2, 0, ArgType.FLOAT, floatSourceCodeVar);
			floatSourceCodeVar.setSsaVars(List.of(floatSource));

			zeroInput = makeSsaVar(0, 0, ArgType.UNKNOWN, sharedCodeVar);
			bindConst(zeroInput, 0, ArgType.UNKNOWN);
			SSAVar objectInput = makeSsaVar(0, 1, ArgType.UNKNOWN_OBJECT, sharedCodeVar);
			bindMove(objectInput, objectSource);
			SSAVar nullablePhiResult = makeSsaVar(0, 2, ArgType.UNKNOWN_OBJECT, sharedCodeVar);
			PhiInsn nullablePhi = makePhi(nullablePhiResult);
			bindPhiArg(nullablePhi, zeroInput, 0);
			bindPhiArg(nullablePhi, objectInput, 1);

			SSAVar floatInput = makeSsaVar(0, 3, ArgType.UNKNOWN, sharedCodeVar);
			bindMove(floatInput, floatSource);
			SSAVar carrierPhiResult = makeSsaVar(0, 4, ArgType.UNKNOWN, sharedCodeVar);
			PhiInsn carrierPhi = makePhi(carrierPhiResult);
			bindPhiArg(carrierPhi, nullablePhiResult, 2);
			bindPhiArg(carrierPhi, floatInput, 3);

			CodeVar outputCodeVar = typedCodeVar(outputType);
			SSAVar output = makeSsaVar(3, 0, outputType, outputCodeVar);
			bindMove(output, carrierPhiResult);
			outputMove = output.getAssignInsn();
			outputCodeVar.setSsaVars(List.of(output));

			List<SSAVar> sharedGroup = List.of(
					zeroInput, objectInput, nullablePhiResult, floatInput, carrierPhiResult);
			sharedCodeVar.setSsaVars(sharedGroup);
			groups.put(sharedCodeVar, sharedGroup);
			groups.put(objectSourceCodeVar, List.of(objectSource));
			groups.put(floatSourceCodeVar, List.of(floatSource));
			groups.put(outputCodeVar, List.of(output));
		}
	}

	private static final class CoroutineFloatIntCarrierFixture {
		private final CodeVar sharedCodeVar = new CodeVar();
		private final List<SSAVar> group;
		private final List<SSAVar> allVars;
		private final TernaryInsn floatOutput;
		private final TernaryInsn floatLoopBack;

		private CoroutineFloatIntCarrierFixture(boolean extraGeneratedUse) {
			CodeVar floatRootCodeVar = typedCodeVar(ArgType.FLOAT);
			SSAVar floatRootSource = makeSsaVar(3, 0, ArgType.FLOAT, floatRootCodeVar);
			floatRootCodeVar.setSsaVars(List.of(floatRootSource));
			SSAVar floatRoot = makeSsaVar(0, 0, ArgType.UNKNOWN, sharedCodeVar);
			bindMove(floatRoot, floatRootSource);

			RootNode root = new RootNode(new JadxArgs());
			FieldInfo intSpillField = FieldInfo.from(
					root, ClassInfo.fromName(root, "test.Continuation"), "I$0", ArgType.INT);
			SSAVar intSpill = makeSsaVar(0, 1, ArgType.UNKNOWN, sharedCodeVar);
			intSpill.getAssign().forceSetInitType(ArgType.INT);
			IndexInsnNode intSpillGet = new IndexInsnNode(InsnType.IGET, intSpillField, 0);
			intSpillGet.setResult(intSpill.getAssign());

			CodeVar intSourceCodeVar = typedCodeVar(ArgType.INT);
			SSAVar intSource = makeSsaVar(2, 0, ArgType.INT, intSourceCodeVar);
			intSourceCodeVar.setSsaVars(List.of(intSource));
			SSAVar intInput = makeSsaVar(0, 2, ArgType.UNKNOWN, sharedCodeVar);
			bindMove(intInput, intSource);

			SSAVar firstPhiResult = makeSsaVar(0, 3, ArgType.UNKNOWN, sharedCodeVar);
			PhiInsn firstPhi = makePhi(firstPhiResult);
			bindPhiArg(firstPhi, floatRoot, 0);
			bindPhiArg(firstPhi, intSpill, 1);

			SSAVar loopBack = makeSsaVar(0, 4, ArgType.UNKNOWN, sharedCodeVar);
			RegisterArg loopBackSource = floatRootSource.getAssign().duplicate();
			IfNode loopBackIf = new IfNode(IfOp.EQ, -1, loopBackSource, LiteralArg.litTrue());
			floatLoopBack = new TernaryInsn(
					IfCondition.fromIfNode(loopBackIf), loopBack.getAssign(),
					LiteralArg.make(1, ArgType.UNKNOWN), LiteralArg.make(0, ArgType.UNKNOWN));

			SSAVar carrierPhiResult = makeSsaVar(0, 5, ArgType.UNKNOWN, sharedCodeVar);
			PhiInsn carrierPhi = makePhi(carrierPhiResult);
			bindPhiArg(carrierPhi, firstPhiResult, 2);
			bindPhiArg(carrierPhi, loopBack, 3);

			SSAVar intExitPhiResult = makeSsaVar(0, 6, ArgType.UNKNOWN, sharedCodeVar);
			PhiInsn intExitPhi = makePhi(intExitPhiResult);
			bindPhiArg(intExitPhi, carrierPhiResult, 4);
			bindPhiArg(intExitPhi, intInput, 5);

			CodeVar floatOutputCodeVar = typedCodeVar(ArgType.FLOAT);
			SSAVar floatOutputVar = makeSsaVar(4, 0, ArgType.FLOAT, floatOutputCodeVar);
			RegisterArg carrierUse = carrierPhiResult.getAssign().duplicate();
			IfNode floatOutputIf = new IfNode(IfOp.EQ, -1, carrierUse, LiteralArg.litTrue());
			floatOutput = new TernaryInsn(
					IfCondition.fromIfNode(floatOutputIf), floatOutputVar.getAssign(),
					LiteralArg.make(1, ArgType.UNKNOWN), LiteralArg.make(0, ArgType.UNKNOWN));
			floatOutputCodeVar.setSsaVars(List.of(floatOutputVar));

			CodeVar intOutputCodeVar = typedCodeVar(ArgType.INT);
			SSAVar intOutput = makeSsaVar(5, 0, ArgType.INT, intOutputCodeVar);
			bindMove(intOutput, intExitPhiResult);
			intOutputCodeVar.setSsaVars(List.of(intOutput));

			if (extraGeneratedUse) {
				InsnNode returnInsn = new InsnNode(InsnType.RETURN, 1);
				returnInsn.addArg(intExitPhiResult.getAssign().duplicate());
			}

			group = List.of(
					floatRoot, intSpill, intInput, firstPhiResult, loopBack, carrierPhiResult, intExitPhiResult);
			sharedCodeVar.setSsaVars(group);
			allVars = List.of(
					floatRoot, intSpill, intInput, firstPhiResult, loopBack, carrierPhiResult, intExitPhiResult,
					floatOutputVar, intOutput, floatRootSource, intSource);
		}
	}

	private static final class NullableReferencePhiRelayFixture {
		private final ArgType referenceType = ArgType.object("test.Headers");
		private final CodeVar sharedCodeVar = new CodeVar();
		private final InsnNode referenceOutputMove;
		private final InsnNode wideningOutputMove;
		private final Map<CodeVar, List<SSAVar>> groups = new IdentityHashMap<>();

		private NullableReferencePhiRelayFixture(ArgType wideningType) {
			CodeVar sourceCodeVar = typedCodeVar(referenceType);
			SSAVar source = makeSsaVar(1, 0, referenceType, sourceCodeVar);
			sourceCodeVar.setSsaVars(List.of(source));

			SSAVar nullInput = makeSsaVar(0, 0, ArgType.UNKNOWN_OBJECT, sharedCodeVar);
			bindConst(nullInput, 0, ArgType.UNKNOWN_OBJECT);
			SSAVar referenceInput = makeSsaVar(0, 1, ArgType.UNKNOWN_OBJECT, sharedCodeVar);
			bindMove(referenceInput, source);
			referenceInput.getAssignInsn().add(AFlag.SYNTHETIC);
			SSAVar phiResult = makeSsaVar(0, 2, ArgType.UNKNOWN_OBJECT, sharedCodeVar);
			PhiInsn phi = makePhi(phiResult);
			bindPhiArg(phi, nullInput, 0);
			bindPhiArg(phi, referenceInput, 1);

			CodeVar referenceOutputCodeVar = typedCodeVar(referenceType);
			SSAVar referenceOutput = makeSsaVar(2, 0, referenceType, referenceOutputCodeVar);
			bindMove(referenceOutput, phiResult);
			referenceOutputMove = referenceOutput.getAssignInsn();
			referenceOutputMove.add(AFlag.SYNTHETIC);
			referenceOutputCodeVar.setSsaVars(List.of(referenceOutput));

			CodeVar wideningOutputCodeVar = typedCodeVar(wideningType);
			SSAVar wideningOutput = makeSsaVar(3, 0, wideningType, wideningOutputCodeVar);
			bindMove(wideningOutput, phiResult);
			wideningOutputMove = wideningOutput.getAssignInsn();
			wideningOutputCodeVar.setSsaVars(List.of(wideningOutput));

			List<SSAVar> sharedGroup = List.of(phiResult, nullInput, referenceInput);
			sharedCodeVar.setSsaVars(sharedGroup);
			groups.put(sharedCodeVar, sharedGroup);
			groups.put(sourceCodeVar, List.of(source));
			groups.put(referenceOutputCodeVar, List.of(referenceOutput));
			groups.put(wideningOutputCodeVar, List.of(wideningOutput));
		}
	}

	private static final class ZeroPrimitivePhiRelayFixture {
		private final CodeVar sharedCodeVar = new CodeVar();
		private final SSAVar zeroInput;
		private final InsnNode outputMove;
		private final Map<CodeVar, List<SSAVar>> groups = new IdentityHashMap<>();

		private ZeroPrimitivePhiRelayFixture(ArgType outputType) {
			CodeVar sourceCodeVar = typedCodeVar(ArgType.FLOAT);
			SSAVar source = makeSsaVar(1, 0, ArgType.FLOAT, sourceCodeVar);
			sourceCodeVar.setSsaVars(List.of(source));

			zeroInput = makeSsaVar(0, 0, ArgType.UNKNOWN, sharedCodeVar);
			bindConst(zeroInput, 0, ArgType.UNKNOWN);
			SSAVar primitiveInput = makeSsaVar(0, 1, ArgType.UNKNOWN, sharedCodeVar);
			bindMove(primitiveInput, source);
			primitiveInput.getAssignInsn().add(AFlag.SYNTHETIC);
			SSAVar phiResult = makeSsaVar(0, 2, ArgType.UNKNOWN, sharedCodeVar);
			PhiInsn phi = makePhi(phiResult);
			bindPhiArg(phi, zeroInput, 0);
			bindPhiArg(phi, primitiveInput, 1);

			CodeVar outputCodeVar = typedCodeVar(outputType);
			SSAVar output = makeSsaVar(2, 0, outputType, outputCodeVar);
			bindMove(output, phiResult);
			outputMove = output.getAssignInsn();
			outputMove.add(AFlag.SYNTHETIC);
			outputCodeVar.setSsaVars(List.of(output));

			List<SSAVar> sharedGroup = List.of(phiResult, zeroInput, primitiveInput);
			sharedCodeVar.setSsaVars(sharedGroup);
			groups.put(sharedCodeVar, sharedGroup);
			groups.put(sourceCodeVar, List.of(source));
			groups.put(outputCodeVar, List.of(output));
		}
	}

	private static void addMoveUse(RegisterArg sourceAssign, int regNum, ArgType targetType) {
		RegisterArg targetAssign = InsnArg.reg(regNum, targetType);
		SSAVar targetVar = new SSAVar(regNum, 0, targetAssign);
		CodeVar targetCodeVar = new CodeVar();
		targetCodeVar.setType(targetType);
		targetVar.setCodeVar(targetCodeVar);
		InsnNode move = new InsnNode(InsnType.MOVE, 1);
		move.setResult(targetAssign);
		move.addArg(sourceAssign.duplicate());
	}

	private static void addReferenceUse(RegisterArg sourceAssign, ArgType useType) {
		RegisterArg use = sourceAssign.duplicate();
		use.forceSetInitType(useType);
		sourceAssign.getSVar().use(use);
		InsnNode useInsn = new InsnNode(InsnType.IPUT, 1);
		useInsn.addArg(use);
	}

	private static RegisterArg makeKnownSource(int regNum, ArgType type) {
		RegisterArg assign = InsnArg.reg(regNum, type);
		SSAVar var = new SSAVar(regNum, 0, assign);
		CodeVar codeVar = new CodeVar();
		codeVar.setType(type);
		var.setCodeVar(codeVar);
		return assign;
	}

	@Test
	void selectTypePresentInAssignmentAndUseBounds() {
		ArgType serviceType = ArgType.object("test.ServiceImpl");
		RegisterArg assign = InsnArg.reg(0, ArgType.UNKNOWN);
		SSAVar var = new SSAVar(0, 0, assign);
		var.getTypeInfo().getBounds().add(new TypeBoundConst(BoundEnum.ASSIGN, serviceType));
		var.getTypeInfo().getBounds().add(new TypeBoundConst(BoundEnum.USE, serviceType));
		var.getTypeInfo().getBounds().add(new TypeBoundConst(BoundEnum.USE, ArgType.object("android.content.Context")));

		assertThat(FinishTypeInference.selectAssignUseType(List.of(var))).isEqualTo(serviceType);
	}

	@Test
	void selectSingleConcreteArrayType() {
		ArgType stringArray = ArgType.array(ArgType.STRING);
		RegisterArg assign = InsnArg.reg(0, ArgType.UNKNOWN);
		SSAVar var = new SSAVar(0, 0, assign);
		var.getTypeInfo().getBounds().add(new TypeBoundConst(BoundEnum.ASSIGN, stringArray));

		assertThat(FinishTypeInference.selectSingleArrayType(List.of(var))).isEqualTo(stringArray);
	}

	@Test
	void rejectArrayTypeMixedWithObjectLifetime() {
		ArgType stringArray = ArgType.array(ArgType.STRING);
		SSAVar arrayVar = new SSAVar(0, 0, InsnArg.reg(0, ArgType.UNKNOWN));
		arrayVar.getTypeInfo().getBounds().add(new TypeBoundConst(BoundEnum.ASSIGN, stringArray));
		SSAVar cursorVar = new SSAVar(0, 1, InsnArg.reg(0, ArgType.UNKNOWN));
		cursorVar.getTypeInfo().getBounds().add(
				new TypeBoundConst(BoundEnum.ASSIGN, ArgType.object("android.database.Cursor")));

		assertThat(FinishTypeInference.selectSingleArrayType(List.of(arrayVar, cursorVar))).isNull();
	}

	@Test
	void repairThreeTypeExceptionParallelMoveCycle() {
		MixedPhiCycle fixture = new MixedPhiCycle();

		assertThat(FinishTypeInference.repairMixedReferencePrimitivePhiLifetimes(
				fixture.groups, (root, input) -> true)).isOne();
		assertThat(fixture.stringPhi.getArguments()).extracting(arg -> ((RegisterArg) arg).getSVar())
				.containsExactly(fixture.stringRoot, fixture.stringCopy);
		assertThat(fixture.arrayExceptionPhi.getArguments()).extracting(arg -> ((RegisterArg) arg).getSVar())
				.containsExactly(fixture.arrayRelay, fixture.arrayBridge);
		assertThat(fixture.intExceptionPhi.getArguments()).extracting(arg -> ((RegisterArg) arg).getSVar())
				.containsExactly(fixture.intRoot, fixture.updatedInt);
		assertThat(fixture.arrayRoot.getCodeVar().getType()).isEqualTo(fixture.arrayType);
		assertThat(fixture.arrayRoot.getCodeVar().getSsaVars()).containsExactly(
				fixture.arrayRoot, fixture.arrayPhiResult, fixture.arrayRelay,
				fixture.arrayBridge, fixture.arrayExceptionResult);
		assertThat(fixture.stringRoot.getCodeVar().getType()).isEqualTo(ArgType.STRING);
		assertThat(fixture.stringRoot.getCodeVar().getSsaVars())
				.containsExactly(fixture.stringRoot, fixture.stringPhiResult);
		assertThat(fixture.intRoot.getCodeVar().getType()).isEqualTo(ArgType.INT);
		assertThat(fixture.intRoot.getCodeVar()).isSameAs(fixture.intExceptionResult.getCodeVar());
		assertThat(fixture.stringCopy.getCodeVar().getType()).isEqualTo(ArgType.STRING);
		assertThat(fixture.updatedInt.getCodeVar().getType()).isEqualTo(ArgType.INT);
	}

	@Test
	void rejectThreeTypeExceptionParallelMoveCycleWithoutDominance() {
		MixedPhiCycle fixture = new MixedPhiCycle();

		assertThat(FinishTypeInference.repairMixedReferencePrimitivePhiLifetimes(
				fixture.groups, (root, input) -> false)).isZero();
		assertThat(fixture.stringPhi.getArguments()).extracting(arg -> ((RegisterArg) arg).getSVar())
				.containsExactly(fixture.stringRoot, fixture.arrayBridge);
		assertThat(fixture.arrayRoot.getCodeVar().getType()).isNull();
	}

	private static final class ClosedBooleanIntPhiFixture {
		private final CodeVar mergedCodeVar = new CodeVar();
		private final SSAVar intLoopResult;
		private final InsnNode intInputMove;

		private ClosedBooleanIntPhiFixture(long rootLiteral) {
			CodeVar intLoopCodeVar = typedCodeVar(ArgType.INT);
			SSAVar intRoot = makeSsaVar(2, 0, ArgType.INT, intLoopCodeVar);
			bindConst(intRoot, rootLiteral, ArgType.INT);
			intLoopResult = makeSsaVar(2, 1, ArgType.INT, intLoopCodeVar);
			PhiInsn intLoopPhi = makePhi(intLoopResult);
			SSAVar intLoopBack = makeSsaVar(2, 2, ArgType.INT, intLoopCodeVar);
			bindMove(intLoopBack, intLoopResult);
			bindPhiArg(intLoopPhi, intRoot, 0);
			bindPhiArg(intLoopPhi, intLoopBack, 1);
			intLoopCodeVar.setSsaVars(List.of(intRoot, intLoopResult, intLoopBack));

			SSAVar intInput = makeSsaVar(0, 0, ArgType.UNKNOWN, mergedCodeVar);
			bindMove(intInput, intLoopResult);
			intInputMove = intInput.getAssignInsn();

			SSAVar booleanProducer = makeSsaVar(0, 1, ArgType.UNKNOWN, mergedCodeVar);
			RootNode root = new RootNode(new JadxArgs());
			MethodInfo booleanMethod = MethodInfo.fromDetails(
					root, ClassInfo.fromName(root, "test.BooleanSource"), "read", List.of(), ArgType.BOOLEAN);
			InvokeNode invoke = new InvokeNode(booleanMethod, InvokeType.STATIC, 0);
			invoke.setResult(booleanProducer.getAssign());

			SSAVar phiResult = makeSsaVar(0, 2, ArgType.UNKNOWN, mergedCodeVar);
			PhiInsn mergedPhi = makePhi(phiResult);
			bindPhiArg(mergedPhi, intInput, 2);
			bindPhiArg(mergedPhi, booleanProducer, 3);
			RegisterArg conditionUse = phiResult.getAssign().duplicate();
			phiResult.use(conditionUse);
			new IfNode(IfOp.NE, -1, conditionUse, LiteralArg.make(0, ArgType.INT));

			mergedCodeVar.setSsaVars(List.of(intInput, booleanProducer, phiResult));
		}
	}

	private static final class MixedPhiCycle {
		private final ArgType arrayType = ArgType.object("org.json.JSONArray");
		private final Map<CodeVar, List<SSAVar>> groups = new IdentityHashMap<>();
		private final SSAVar arrayRoot;
		private final SSAVar arrayPhiResult;
		private final SSAVar stringRoot;
		private final SSAVar stringPhiResult;
		private final SSAVar arrayBridge;
		private final SSAVar arrayRelay;
		private final SSAVar updatedInt;
		private final SSAVar arrayExceptionResult;
		private final SSAVar stringCopy;
		private final SSAVar intRoot;
		private final SSAVar intExceptionResult;
		private final PhiInsn stringPhi;
		private final PhiInsn arrayExceptionPhi;
		private final PhiInsn intExceptionPhi;

		private MixedPhiCycle() {
			CodeVar arraySourceCodeVar = typedCodeVar(arrayType);
			SSAVar arraySource = makeSsaVar(12, 0, arrayType, arraySourceCodeVar);
			CodeVar mixedCodeVar = new CodeVar();
			arrayRoot = makeSsaVar(16, 5, ArgType.UNKNOWN_OBJECT, mixedCodeVar);
			bindMove(arrayRoot, arraySource);

			arrayPhiResult = makeSsaVar(16, 7, ArgType.UNKNOWN, mixedCodeVar);
			PhiInsn arrayPhi = makePhi(arrayPhiResult);

			CodeVar stringFlowCodeVar = typedCodeVar(ArgType.STRING);
			SSAVar stringSource = makeSsaVar(4, 12, ArgType.STRING, stringFlowCodeVar);
			stringRoot = makeSsaVar(16, 10, ArgType.UNKNOWN_OBJECT, mixedCodeVar);
			bindMove(stringRoot, stringSource);

			stringPhiResult = makeSsaVar(16, 11, ArgType.UNKNOWN, mixedCodeVar);
			stringPhi = makePhi(stringPhiResult);

			CodeVar intFlowCodeVar = typedCodeVar(ArgType.INT);
			arrayRelay = makeSsaVar(2, 16, ArgType.UNKNOWN, intFlowCodeVar);
			bindMove(arrayRelay, arrayPhiResult);
			arrayBridge = makeSsaVar(16, 12, ArgType.UNKNOWN_OBJECT, mixedCodeVar);
			bindMove(arrayBridge, arrayRelay);

			bindPhiArg(arrayPhi, arrayRoot, 1);
			bindPhiArg(arrayPhi, arrayBridge, 2);
			bindPhiArg(stringPhi, stringRoot, 3);
			bindPhiArg(stringPhi, arrayBridge, 4);

			updatedInt = makeSsaVar(2, 18, ArgType.INT, intFlowCodeVar);
			bindConst(updatedInt, 2, ArgType.INT);
			arrayExceptionResult = makeSsaVar(2, 17, ArgType.UNKNOWN, intFlowCodeVar);
			arrayExceptionPhi = makePhi(arrayExceptionResult);
			bindPhiArg(arrayExceptionPhi, arrayRelay, 5);
			bindPhiArg(arrayExceptionPhi, updatedInt, 6);

			stringCopy = makeSsaVar(4, 17, ArgType.UNKNOWN_OBJECT, stringFlowCodeVar);
			bindMove(stringCopy, stringRoot);
			intRoot = makeSsaVar(4, 15, ArgType.UNKNOWN, stringFlowCodeVar);
			bindConst(intRoot, 6, ArgType.UNKNOWN);
			intExceptionResult = makeSsaVar(4, 16, ArgType.UNKNOWN, stringFlowCodeVar);
			intExceptionPhi = makePhi(intExceptionResult);
			bindPhiArg(intExceptionPhi, intRoot, 7);
			bindPhiArg(intExceptionPhi, stringCopy, 8);

			mixedCodeVar.setSsaVars(List.of(
					arrayRoot, arrayPhiResult, stringRoot, stringPhiResult, arrayBridge));
			arraySourceCodeVar.setSsaVars(List.of(arraySource));
			intFlowCodeVar.setSsaVars(List.of(arrayRelay, updatedInt, arrayExceptionResult));
			stringFlowCodeVar.setSsaVars(List.of(
					stringSource, stringCopy, intRoot, intExceptionResult));
			groups.put(arraySourceCodeVar, arraySourceCodeVar.getSsaVars());
			groups.put(mixedCodeVar, mixedCodeVar.getSsaVars());
			groups.put(intFlowCodeVar, intFlowCodeVar.getSsaVars());
			groups.put(stringFlowCodeVar, stringFlowCodeVar.getSsaVars());
		}
	}

	private static CodeVar typedCodeVar(ArgType type) {
		CodeVar codeVar = new CodeVar();
		codeVar.setType(type);
		return codeVar;
	}

	private static SSAVar makeSsaVar(int reg, int version, ArgType type, CodeVar codeVar) {
		RegisterArg assign = InsnArg.reg(reg, type);
		SSAVar var = new SSAVar(reg, version, assign);
		var.setCodeVar(codeVar);
		return var;
	}

	private static void bindMove(SSAVar result, SSAVar source) {
		InsnNode move = new InsnNode(InsnType.MOVE, 1);
		move.setResult(result.getAssign());
		RegisterArg use = source.getAssign().duplicate();
		source.use(use);
		move.addArg(use);
	}

	private static void bindConst(SSAVar result, long literal, ArgType type) {
		InsnNode constant = new InsnNode(InsnType.CONST, 1);
		constant.setResult(result.getAssign());
		constant.addArg(LiteralArg.make(literal, type));
	}

	private static PhiInsn makePhi(SSAVar result) {
		PhiInsn phi = new PhiInsn(result.getRegNum(), 2);
		phi.setResult(result.getAssign());
		phi.add(AFlag.DONT_GENERATE);
		return phi;
	}

	private static void bindPhiArg(PhiInsn phi, SSAVar source, int blockId) {
		RegisterArg use = source.getAssign().duplicate();
		source.use(use);
		phi.bindArg(use, new BlockNode(blockId, 0, 0));
	}

	private static SSAVar makeVarWithUse(boolean dontGenerate) {
		RegisterArg assign = InsnArg.reg(0, ArgType.UNKNOWN);
		SSAVar var = new SSAVar(0, 0, assign);
		RegisterArg use = InsnArg.reg(0, ArgType.UNKNOWN);
		var.use(use);
		InsnNode useInsn = new InsnNode(InsnType.MOVE, 1);
		useInsn.addArg(use);
		if (dontGenerate) {
			useInsn.add(AFlag.DONT_GENERATE);
		}
		return var;
	}
}
