package jadx.core.dex.visitors.regions.maker;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import jadx.api.JadxArgs;
import jadx.core.dex.attributes.AFlag;
import jadx.core.dex.attributes.AType;
import jadx.core.dex.attributes.nodes.BooleanNumericConversionAttr;
import jadx.core.dex.attributes.nodes.LoopInfo;
import jadx.core.dex.info.ClassInfo;
import jadx.core.dex.info.MethodInfo;
import jadx.core.dex.instructions.InsnType;
import jadx.core.dex.instructions.InvokeNode;
import jadx.core.dex.instructions.InvokeType;
import jadx.core.dex.instructions.args.ArgType;
import jadx.core.dex.instructions.args.InsnArg;
import jadx.core.dex.instructions.mods.ConstructorInsn;
import jadx.core.dex.instructions.mods.ConstructorInsn.CallType;
import jadx.core.dex.instructions.mods.TernaryInsn;
import jadx.core.dex.nodes.BlockNode;
import jadx.core.dex.nodes.InsnNode;
import jadx.core.dex.nodes.RootNode;
import jadx.core.dex.visitors.ModVisitor;

import static org.assertj.core.api.Assertions.assertThat;

class RegionMakerTest {

	@Test
	void rejectUnprocessedLinearWrapper() {
		assertThat(RegionMaker.collectLinearWrapperInvokes(null)).isNull();
		assertThat(RegionMaker.collectLinearWrapperInvokes(List.of())).isEmpty();
	}

	@Test
	void classifyOnlyLoopStartAsExpectedTraversalCycle() {
		BlockNode start = new BlockNode(1, 0, 0);
		BlockNode end = new BlockNode(2, 0, 0);
		LoopInfo loop = new LoopInfo(start, end, Set.of(start, end));

		assertThat(RegionMaker.isExpectedLoopStartCycle(start, loop)).isTrue();
		assertThat(RegionMaker.isExpectedLoopStartCycle(end, loop)).isFalse();
		assertThat(RegionMaker.isExpectedLoopStartCycle(start, null)).isFalse();
	}

	@Test
	void classifyBlocksWithoutGeneratedCodeAsSafeDuplication() {
		BlockNode emptyBlock = new BlockNode(1, 0, 0);
		assertThat(RegionMaker.hasNoGeneratedCode(emptyBlock)).isTrue();

		InsnNode hiddenInsn = new InsnNode(InsnType.NOP, 0);
		hiddenInsn.add(AFlag.DONT_GENERATE);
		emptyBlock.getInstructions().add(hiddenInsn);
		assertThat(RegionMaker.hasNoGeneratedCode(emptyBlock)).isTrue();
	}

	@Test
	void rejectGeneratedCodeAsEmptyDuplication() {
		assertThat(RegionMaker.hasNoGeneratedCode(makeConstBlock(ArgType.INT, ArgType.INT))).isFalse();
	}

	@Test
	void classifyLocalConstAndMoveAssignmentsAsSafeDuplication() {
		BlockNode block = makeConstBlock(ArgType.INT, ArgType.INT);
		InsnNode moveInsn = new InsnNode(InsnType.MOVE, 1);
		moveInsn.setResult(InsnArg.reg(1, ArgType.OBJECT));
		moveInsn.addArg(InsnArg.reg(2, ArgType.OBJECT));
		block.getInstructions().add(moveInsn);

		assertThat(RegionMaker.isSafeLocalAssignmentDuplication(block)).isTrue();
	}

	@Test
	void classifyLocalArithmeticAsSafeDuplication() {
		BlockNode block = new BlockNode(1, 0, 0);
		InsnNode arithInsn = new InsnNode(InsnType.ARITH, 2);
		arithInsn.setResult(InsnArg.reg(0, ArgType.INT));
		arithInsn.addArg(InsnArg.reg(1, ArgType.INT));
		arithInsn.addArg(InsnArg.lit(1, ArgType.INT));
		block.getInstructions().add(arithInsn);

		assertThat(RegionMaker.isSafeLocalAssignmentDuplication(block)).isTrue();
	}

	@Test
	void rejectNonLocalAssignmentDuplication() {
		InsnNode fieldPutInsn = new InsnNode(InsnType.IPUT, 2);
		fieldPutInsn.addArg(InsnArg.reg(0, ArgType.INT));
		fieldPutInsn.addArg(InsnArg.reg(1, ArgType.OBJECT));
		BlockNode block = new BlockNode(1, 0, 0);
		block.getInstructions().add(fieldPutInsn);

		assertThat(RegionMaker.isSafeLocalAssignmentDuplication(block)).isFalse();
	}

	@Test
	void classifyBooleanToIntConversionAsSafeDuplication() {
		TernaryInsn conversion = ModVisitor.makeBooleanConvertInsn(
				InsnArg.reg(0, ArgType.INT), InsnArg.reg(1, ArgType.BOOLEAN), ArgType.INT);
		conversion.addAttr(BooleanNumericConversionAttr.INSTANCE);
		BlockNode block = new BlockNode(1, 0, 0);
		block.getInstructions().add(conversion);

		assertThat(RegionMaker.isSafeBooleanNumericConversionDuplication(block)).isTrue();

		conversion.remove(AType.BOOLEAN_NUMERIC_CONVERSION);
		assertThat(RegionMaker.isSafeBooleanNumericConversionDuplication(block)).isFalse();
	}

	@Test
	void classifyArrayCreationAsReadOnlyValue() {
		assertThat(RegionMaker.isReadOnlyValueInsnType(InsnType.NEW_ARRAY)).isTrue();
		assertThat(RegionMaker.isReadOnlyValueInsnType(InsnType.FILLED_NEW_ARRAY)).isTrue();
	}

	@Test
	void rejectFieldWriteAsReadOnlyValue() {
		assertThat(RegionMaker.isReadOnlyValueInsnType(InsnType.IPUT)).isFalse();
	}

	@Test
	void classifyOnlyFinalNewInstanceAsSingletonInitializer() {
		RootNode root = new RootNode(new JadxArgs());
		MethodInfo constructor = MethodInfo.fromDetails(
				root, ClassInfo.fromName(root, "test.Singleton"), "<init>", List.of(), ArgType.VOID);
		ConstructorInsn newInstance = new ConstructorInsn(constructor, CallType.CONSTRUCTOR);
		ConstructorInsn superCall = new ConstructorInsn(constructor, CallType.SUPER);

		assertThat(RegionMaker.isFinalSingletonInit(true, newInstance)).isTrue();
		assertThat(RegionMaker.isFinalSingletonInit(false, newInstance)).isFalse();
		assertThat(RegionMaker.isFinalSingletonInit(true, superCall)).isFalse();
		assertThat(RegionMaker.isFinalSingletonInit(true, new InsnNode(InsnType.CONST, 0))).isFalse();
		assertThat(RegionMaker.isFinalSingletonInit(true, null)).isFalse();
	}

	@Test
	void classifyOnlyLiteralAssignmentsAsSafeLoopDuplication() {
		assertThat(RegionMaker.isSafeLoopConstantDuplication(makeConstBlock(ArgType.INT, ArgType.INT))).isTrue();

		BlockNode stringBlock = new BlockNode(1, 0, 0);
		stringBlock.getInstructions().add(new InsnNode(InsnType.CONST_STR, 0));
		assertThat(RegionMaker.isSafeLoopConstantDuplication(stringBlock)).isTrue();

		BlockNode arrayReadBlock = new BlockNode(1, 0, 0);
		arrayReadBlock.getInstructions().add(new InsnNode(InsnType.AGET, 2));
		assertThat(RegionMaker.isSafeLoopConstantDuplication(arrayReadBlock)).isFalse();
	}

	@Test
	void classifyOnlyKnownPureValueCalls() {
		assertThat(RegionMaker.isPureValueCall("java.lang.Boolean", "booleanValue", 1)).isTrue();
		assertThat(RegionMaker.isPureValueCall("kotlin.coroutines.jvm.internal.Boxing", "boxBoolean", 1)).isTrue();
		assertThat(RegionMaker.isPureValueCall("java.lang.Integer", "intValue", 1)).isTrue();
		assertThat(RegionMaker.isPureValueCall("java.lang.Double", "doubleValue", 1)).isTrue();
		assertThat(RegionMaker.isPureValueCall("kotlinx.coroutines.flow.StateFlow", "getValue", 1)).isTrue();
		assertThat(RegionMaker.isPureValueCall("kotlinx.coroutines.flow.MutableStateFlow", "getValue", 1)).isTrue();
		assertThat(RegionMaker.isPureValueCall("java.util.List", "size", 1)).isTrue();
		assertThat(RegionMaker.isPureValueCall("java.util.List", "get", 2)).isTrue();
		assertThat(RegionMaker.isPureValueCall(
				"androidx.compose.ui.Alignment.Companion", "getStart", 1)).isTrue();
		assertThat(RegionMaker.isPureValueCall(
				"androidx.compose.ui.unit.Dp.Companion", "getUnspecified-D9Ej5fM", 1)).isTrue();
		assertThat(RegionMaker.isPureValueCall(
				"androidx.compose.ui.unit.Velocity.Companion", "getZero-9UxMQ8M", 1)).isTrue();
		assertThat(RegionMaker.isPureValueCall(
				"androidx.compose.ui.text.style.TextAlign.Companion", "getRight-e0LSkKk", 1)).isTrue();
		assertThat(RegionMaker.isPureValueCall("kotlin.coroutines.jvm.internal.Boxing", "boxInt", 1)).isTrue();
		assertThat(RegionMaker.isPureValueCall("kotlin.coroutines.jvm.internal.Boxing", "boxFloat", 1)).isTrue();
		assertThat(RegionMaker.isPureValueCall("kotlin.Result", "constructor-impl", 1)).isTrue();
		assertThat(RegionMaker.isPureValueCall("androidx.compose.ui.unit.Dp", "constructor-impl", 1)).isTrue();
		assertThat(RegionMaker.isPureValueCall("kotlin.collections.CollectionsKt", "emptyList", 0)).isTrue();
		assertThat(RegionMaker.isPureValueCall("kotlin.collections.CollectionsKt", "arrayListOf", 1)).isTrue();

		assertThat(RegionMaker.isPureValueCall("java.lang.Boolean", "booleanValue", 0)).isFalse();
		assertThat(RegionMaker.isPureValueCall("java.lang.Integer", "longValue", 1)).isFalse();
		assertThat(RegionMaker.isPureValueCall("kotlinx.coroutines.flow.MutableStateFlow", "setValue", 2)).isFalse();
		assertThat(RegionMaker.isPureValueCall("java.util.List", "add", 1)).isFalse();
		assertThat(RegionMaker.isPureValueCall("java.util.List", "get", 1)).isFalse();
		assertThat(RegionMaker.isPureValueCall(
				"androidx.compose.ui.Alignment.Companion", "getEnd", 1)).isFalse();
		assertThat(RegionMaker.isPureValueCall(
				"androidx.compose.ui.unit.Dp.Companion", "getUnspecified-D9Ej5fM", 0)).isFalse();
		assertThat(RegionMaker.isPureValueCall(
				"androidx.compose.ui.unit.Velocity.Companion", "getZero-9UxMQ8M", 0)).isFalse();
		assertThat(RegionMaker.isPureValueCall(
				"androidx.compose.ui.text.style.TextAlign.Companion", "getRight-e0LSkKk", 0)).isFalse();
		assertThat(RegionMaker.isPureValueCall("kotlin.coroutines.jvm.internal.Boxing", "boxObject", 1)).isFalse();
		assertThat(RegionMaker.isPureValueCall("java.util.Iterator", "next", 1)).isFalse();
		assertThat(RegionMaker.isPureValueCall("kotlin.collections.CollectionsKt", "emptyList", 1)).isFalse();
		assertThat(RegionMaker.isPureValueCall("kotlin.collections.CollectionsKt", "arrayListOf", 0)).isFalse();
	}

	@Test
	void classifyOnlyPrimitiveStringValueOfCalls() {
		assertThat(RegionMaker.isPrimitiveStringValueOf(
				"java.lang.String", "valueOf", List.of(ArgType.FLOAT))).isTrue();
		assertThat(RegionMaker.isPrimitiveStringValueOf(
				"java.lang.String", "valueOf", List.of(ArgType.OBJECT))).isFalse();
		assertThat(RegionMaker.isPrimitiveStringValueOf(
				"java.lang.String", "valueOf", List.of(ArgType.array(ArgType.CHAR)))).isFalse();
		assertThat(RegionMaker.isPrimitiveStringValueOf(
				"java.lang.StringBuilder", "valueOf", List.of(ArgType.FLOAT))).isFalse();
	}

	@Test
	void classifyOnlyKnownLoopValueCalls() {
		assertThat(RegionMaker.isSafeLoopValueCall("java.util.List", "size", 1)).isTrue();
		assertThat(RegionMaker.isSafeLoopValueCall("java.lang.Math", "min", 2)).isTrue();

		assertThat(RegionMaker.isSafeLoopValueCall("java.util.Iterator", "next", 1)).isFalse();
		assertThat(RegionMaker.isSafeLoopValueCall("java.lang.Math", "random", 0)).isFalse();
	}

	@Test
	void classifyOnlyKnownTerminalThrowCalls() {
		assertThat(RegionMaker.isKnownTerminalThrowCall(
				"kotlin.jvm.internal.Intrinsics", "throwUninitializedPropertyAccessException")).isTrue();
		assertThat(RegionMaker.isKnownTerminalThrowCall(
				"kotlin.collections.CollectionsKt", "throwIndexOverflow")).isTrue();

		assertThat(RegionMaker.isKnownTerminalThrowCall(
				"kotlin.jvm.internal.Intrinsics", "checkNotNullParameter")).isFalse();
		assertThat(RegionMaker.isKnownTerminalThrowCall("java.lang.IllegalStateException", "<init>")).isFalse();
	}

	@Test
	void classifyOnlyKnownComposeProtocolCalls() {
		assertThat(RegionMaker.isKnownComposeProtocolCall(
				"androidx.compose.runtime.ComposerKt", "traceEventEnd", 0)).isTrue();
		assertThat(RegionMaker.isKnownComposeProtocolCall(
				"androidx.compose.runtime.ComposablesKt", "invalidApplier", 0)).isTrue();
		assertThat(RegionMaker.isKnownComposeProtocolCall(
				"androidx.compose.runtime.Composer", "useNode", 1)).isTrue();
		assertThat(RegionMaker.isKnownComposeProtocolCall(
				"androidx.compose.runtime.Composer", "createNode", 2)).isTrue();
		assertThat(RegionMaker.isKnownComposeProtocolCall(
				"androidx.compose.runtime.Composer", "rememberedValue", 1)).isTrue();
		assertThat(RegionMaker.isKnownComposeProtocolCall(
				"androidx.compose.runtime.Composer", "skipToGroupEnd", 1)).isTrue();
		assertThat(RegionMaker.isKnownComposeProtocolCall(
				"androidx.compose.runtime.Composer", "startReplaceGroup", 2)).isTrue();
		assertThat(RegionMaker.isKnownComposeProtocolCall(
				"androidx.compose.runtime.Composer", "endReplaceGroup", 1)).isTrue();
		assertThat(RegionMaker.isKnownComposeProtocolCall(
				"androidx.compose.runtime.ScopeUpdateScope", "updateScope", 2)).isTrue();

		assertThat(RegionMaker.isKnownComposeProtocolCall(
				"androidx.compose.runtime.Composer", "createNode", 1)).isFalse();
		assertThat(RegionMaker.isKnownComposeProtocolCall(
				"androidx.compose.runtime.Composer", "updateRememberedValue", 2)).isFalse();
		assertThat(RegionMaker.isKnownComposeProtocolCall(
				"androidx.compose.runtime.ComposerKt", "traceEventStart", 4)).isFalse();
		assertThat(RegionMaker.isKnownComposeProtocolCall(
				"androidx.compose.runtime.ScopeUpdateScope", "updateScope", 1)).isFalse();
		assertThat(RegionMaker.isKnownComposeProtocolCall(
				"com.example.ComposerKt", "traceEventEnd", 0)).isFalse();
	}

	@Test
	void requireEveryCleanPathToReachBalancedProtocolGuard() {
		BlockNode start = new BlockNode(1, 0, 0);
		BlockNode left = new BlockNode(2, 1, 1);
		BlockNode right = new BlockNode(3, 2, 2);
		BlockNode target = new BlockNode(4, 3, 3);
		connect(start, left);
		connect(start, right);
		connect(left, target);
		connect(right, target);

		assertThat(RegionMaker.allCleanPathsReach(start, target, Set.of())).isTrue();

		BlockNode bypass = new BlockNode(5, 4, 4);
		connect(right, bypass);
		assertThat(RegionMaker.allCleanPathsReach(start, target, Set.of())).isFalse();
		assertThat(RegionMaker.allCleanPathsReach(start, target, Set.of(left))).isFalse();
	}

	@Test
	void rejectCycleBeforeBalancedProtocolGuard() {
		BlockNode start = new BlockNode(1, 0, 0);
		BlockNode loop = new BlockNode(2, 1, 1);
		BlockNode target = new BlockNode(3, 2, 2);
		connect(start, loop);
		connect(loop, start);
		connect(loop, target);

		assertThat(RegionMaker.allCleanPathsReach(start, target, Set.of())).isFalse();
	}

	@Test
	void classifyOnlyKnownComposeRememberUpdateCall() {
		assertThat(RegionMaker.isKnownComposeRememberUpdateCall(
				"androidx.compose.runtime.Composer", "updateRememberedValue", 2)).isTrue();

		assertThat(RegionMaker.isKnownComposeRememberUpdateCall(
				"androidx.compose.runtime.Composer", "updateRememberedValue", 1)).isFalse();
		assertThat(RegionMaker.isKnownComposeRememberUpdateCall(
				"androidx.compose.runtime.Composer", "changed", 2)).isFalse();
		assertThat(RegionMaker.isKnownComposeRememberUpdateCall(
				"com.example.Composer", "updateRememberedValue", 2)).isFalse();
	}

	@Test
	void classifyComposeGroupedColorValueProtocolCalls() {
		RootNode root = new RootNode(new JadxArgs());
		InvokeNode start = makeInvoke(root, "androidx.compose.runtime.Composer", "startReplaceGroup", ArgType.VOID,
				ArgType.object("androidx.compose.runtime.Composer"), ArgType.INT);
		InvokeNode end = makeInvoke(root, "androidx.compose.runtime.Composer", "endReplaceGroup", ArgType.VOID,
				ArgType.object("androidx.compose.runtime.Composer"));
		InvokeNode box = makeInvoke(root, "androidx.compose.ui.graphics.Color", "box-impl",
				ArgType.object("androidx.compose.ui.graphics.Color"), ArgType.LONG);
		InvokeNode unbox = makeInvoke(root, "androidx.compose.ui.graphics.Color", "unbox-impl", ArgType.LONG,
				ArgType.object("androidx.compose.ui.graphics.Color"));

		assertThat(RegionMaker.isComposeGroupBoundary(start, "startReplaceGroup", 2)).isTrue();
		assertThat(RegionMaker.isComposeGroupBoundary(end, "endReplaceGroup", 1)).isTrue();
		assertThat(RegionMaker.isComposeColorBoxCall(box)).isTrue();
		assertThat(RegionMaker.isComposeColorUnboxCall(unbox)).isTrue();
		assertThat(RegionMaker.isComposeGroupBoundary(start, "endReplaceGroup", 2)).isFalse();
		assertThat(RegionMaker.isComposeColorBoxCall(start)).isFalse();
		assertThat(RegionMaker.isComposeColorUnboxCall(box)).isFalse();
	}

	private static BlockNode makeConstBlock(ArgType resultType, ArgType literalType) {
		InsnNode constInsn = new InsnNode(InsnType.CONST, 1);
		constInsn.setResult(InsnArg.reg(0, resultType));
		constInsn.addArg(InsnArg.lit(0, literalType));
		BlockNode block = new BlockNode(1, 0, 0);
		block.getInstructions().add(constInsn);
		return block;
	}

	private static InvokeNode makeInvoke(RootNode root, String clsName, String name, ArgType returnType,
			ArgType... argTypes) {
		MethodInfo method = MethodInfo.fromDetails(
				root, ClassInfo.fromName(root, clsName), name, List.of(argTypes), returnType);
		InvokeNode invoke = new InvokeNode(method, InvokeType.STATIC, argTypes.length);
		for (int i = 0; i < argTypes.length; i++) {
			invoke.addArg(InsnArg.reg(i, argTypes[i]));
		}
		return invoke;
	}

	private static void connect(BlockNode source, BlockNode target) {
		source.getSuccessors().add(target);
		source.updateCleanSuccessors();
		target.getPredecessors().add(source);
		target.updateCleanSuccessors();
	}

}
