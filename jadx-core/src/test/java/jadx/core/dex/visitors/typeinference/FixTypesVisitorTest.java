package jadx.core.dex.visitors.typeinference;

import org.junit.jupiter.api.Test;

import jadx.core.dex.instructions.IfNode;
import jadx.core.dex.instructions.IfOp;
import jadx.core.dex.instructions.InsnType;
import jadx.core.dex.instructions.args.ArgType;
import jadx.core.dex.instructions.args.InsnArg;
import jadx.core.dex.instructions.args.PrimitiveType;
import jadx.core.dex.instructions.args.RegisterArg;
import jadx.core.dex.nodes.InsnNode;

import static org.assertj.core.api.Assertions.assertThat;

class FixTypesVisitorTest {

	@Test
	void shouldAcceptObjectNullCheck() {
		ArgType wideType = wideType();
		RegisterArg arg = InsnArg.reg(0, wideType);
		new IfNode(IfOp.EQ, -1, arg, InsnArg.lit(0, wideType));
		ITypeBound bound = new TypeBoundConst(BoundEnum.USE, wideType, arg);

		assertThat(FixTypesVisitor.isCompatibleUnknownObjectUse(bound)).isTrue();
	}

	@Test
	void shouldAcceptObjectOnlyMove() {
		ArgType.UNKNOWN.getPossibleTypes();
		ArgType objectType = ArgType.unknown(PrimitiveType.OBJECT, PrimitiveType.ARRAY);
		RegisterArg arg = InsnArg.reg(0, objectType);
		InsnNode move = new InsnNode(InsnType.MOVE, 1);
		move.addArg(arg);
		ITypeBound bound = new TypeBoundConst(BoundEnum.USE, objectType, arg);

		assertThat(FixTypesVisitor.isCompatibleUnknownObjectUse(bound)).isTrue();
	}

	@Test
	void shouldRejectNumericComparison() {
		ArgType wideType = wideType();
		RegisterArg arg = InsnArg.reg(0, wideType);
		new IfNode(IfOp.GT, -1, arg, InsnArg.lit(0, wideType));
		ITypeBound bound = new TypeBoundConst(BoundEnum.USE, wideType, arg);

		assertThat(FixTypesVisitor.isCompatibleUnknownObjectUse(bound)).isFalse();
	}

	@Test
	void shouldRejectUnknownAssignment() {
		ITypeBound bound = new TypeBoundConst(BoundEnum.ASSIGN, wideType());

		assertThat(FixTypesVisitor.isCompatibleUnknownObjectUse(bound)).isFalse();
	}

	@Test
	void shouldAcceptUnboundedWildcardCaptureForSameRawType() {
		ArgType fieldType = ArgType.generic("test.Parser", ArgType.wildcard(), ArgType.wildcard());
		ArgType useType = ArgType.generic("test.Parser", ArgType.genericType("UT"), ArgType.genericType("UB"));

		assertThat(FixTypesVisitor.isUnboundedWildcardCapture(fieldType, useType)).isTrue();
	}

	@Test
	void shouldRejectWildcardCaptureForDifferentRawType() {
		ArgType fieldType = ArgType.generic("test.Parser", ArgType.wildcard(), ArgType.wildcard());
		ArgType useType = ArgType.generic("test.Other", ArgType.genericType("UT"), ArgType.genericType("UB"));

		assertThat(FixTypesVisitor.isUnboundedWildcardCapture(fieldType, useType)).isFalse();
	}

	@Test
	void shouldRejectBoundedWildcardCapture() {
		ArgType fieldType = ArgType.generic(
				"test.Parser",
				ArgType.wildcard(ArgType.STRING, ArgType.WildcardBound.EXTENDS),
				ArgType.wildcard());
		ArgType useType = ArgType.generic("test.Parser", ArgType.genericType("UT"), ArgType.genericType("UB"));

		assertThat(FixTypesVisitor.isUnboundedWildcardCapture(fieldType, useType)).isFalse();
	}

	private static ArgType wideType() {
		ArgType.UNKNOWN.getPossibleTypes();
		return ArgType.unknown(
				PrimitiveType.INT, PrimitiveType.BOOLEAN,
				PrimitiveType.OBJECT, PrimitiveType.ARRAY,
				PrimitiveType.BYTE, PrimitiveType.SHORT, PrimitiveType.CHAR);
	}
}
