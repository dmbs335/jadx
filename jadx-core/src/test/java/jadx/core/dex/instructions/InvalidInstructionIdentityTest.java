package jadx.core.dex.instructions;

import java.util.List;

import org.junit.jupiter.api.Test;

import jadx.api.JadxArgs;
import jadx.api.plugins.input.insns.Opcode;
import jadx.core.dex.info.ClassInfo;
import jadx.core.dex.info.FieldInfo;
import jadx.core.dex.info.MethodInfo;
import jadx.core.dex.instructions.args.ArgType;
import jadx.core.dex.instructions.args.InsnArg;
import jadx.core.dex.nodes.InsnNode;
import jadx.core.dex.nodes.RootNode;

import static org.assertj.core.api.Assertions.assertThat;

class InvalidInstructionIdentityTest {
	@Test
	void arithmeticErrorMustNotMergeWithValidInstruction() {
		ArithNode valid = arith();
		ArithNode invalid = arith();
		invalid.setInvalidSourceType(ArgType.FLOAT, ArgType.INT);

		assertDifferent(valid, invalid);
	}

	@Test
	void arrayWriteErrorMustNotMergeWithValidInstruction() {
		ArrayPutNode valid = new ArrayPutNode();
		ArrayPutNode invalid = new ArrayPutNode();
		invalid.setInvalidSourceType(ArgType.INT, ArgType.FLOAT);

		assertDifferent(valid, invalid);
	}

	@Test
	void methodArgumentErrorMustNotMergeWithValidInstruction() {
		RootNode root = new RootNode(new JadxArgs());
		ClassInfo cls = ClassInfo.fromName(root, "test.Target");
		MethodInfo method = MethodInfo.fromDetails(root, cls, "accept", List.of(ArgType.FLOAT), ArgType.VOID);
		InvokeNode valid = new InvokeNode(method, InvokeType.STATIC, 1);
		InvokeNode invalid = new InvokeNode(method, InvokeType.STATIC, 1);
		invalid.setInvalidArgType(0, ArgType.INT, ArgType.FLOAT);

		assertDifferent(valid, invalid);
	}

	@Test
	void fieldWriteErrorMustNotMergeWithValidInstruction() {
		RootNode root = new RootNode(new JadxArgs());
		ClassInfo cls = ClassInfo.fromName(root, "test.Target");
		FieldInfo field = FieldInfo.from(root, cls, "value", ArgType.FLOAT);
		FieldPutNode valid = new FieldPutNode(InsnType.SPUT, field, 1);
		FieldPutNode invalid = new FieldPutNode(InsnType.SPUT, field, 1);
		invalid.setInvalidSourceType(ArgType.INT);

		assertDifferent(valid, invalid);
	}

	@Test
	void compareErrorMustNotMergeWithValidInstruction() {
		CmpInsnNode valid = new CmpInsnNode(InsnType.CMP_L, ArgType.LONG);
		CmpInsnNode invalid = new CmpInsnNode(InsnType.CMP_L, ArgType.LONG);
		invalid.setInvalidSourceType(ArgType.FLOAT);
		assertDifferent(valid, invalid);
	}

	@Test
	void castErrorMustNotMergeWithValidInstruction() {
		NumericCastNode valid = new NumericCastNode(ArgType.FLOAT, ArgType.INT);
		NumericCastNode invalid = new NumericCastNode(ArgType.FLOAT, ArgType.INT);
		invalid.setInvalidSourceType(ArgType.LONG);
		assertDifferent(valid, invalid);
	}

	@Test
	void unaryErrorMustNotMergeWithValidInstruction() {
		UnaryNumericNode valid = new UnaryNumericNode(InsnType.NEG, ArgType.INT);
		UnaryNumericNode invalid = new UnaryNumericNode(InsnType.NEG, ArgType.INT);
		invalid.setInvalidSourceType(ArgType.FLOAT);
		assertDifferent(valid, invalid);
	}

	@Test
	void filledArrayErrorMustNotMergeWithValidInstruction() {
		FilledNewArrayNode valid = new FilledNewArrayNode(ArgType.FLOAT, 1);
		FilledNewArrayNode invalid = new FilledNewArrayNode(ArgType.FLOAT, 1);
		valid.addArg(InsnArg.lit(0, ArgType.FLOAT));
		invalid.addArg(InsnArg.lit(0, ArgType.FLOAT));
		invalid.setInvalidArgType(0, ArgType.INT);
		assertDifferent(valid, invalid);
	}

	@Test
	void returnErrorMustNotMergeWithValidInstruction() {
		ReturnNode valid = new ReturnNode(Opcode.RETURN_FLOAT);
		ReturnNode invalid = new ReturnNode(Opcode.RETURN_FLOAT);
		valid.addArg(InsnArg.reg(0, ArgType.FLOAT));
		invalid.addArg(InsnArg.reg(0, ArgType.FLOAT));
		invalid.setInvalidSourceType(ArgType.INT);
		assertDifferent(valid, invalid);
	}

	@Test
	void conditionErrorMustNotMergeWithValidInstruction() {
		IfNode valid = new IfNode(IfOp.EQ, 0, InsnArg.reg(0, ArgType.INT), InsnArg.lit(0, ArgType.INT));
		IfNode invalid = new IfNode(IfOp.EQ, 0, InsnArg.reg(0, ArgType.INT), InsnArg.lit(0, ArgType.INT));
		invalid.setInvalidArgType(0, ArgType.FLOAT);
		assertDifferent(valid, invalid);
	}

	@Test
	void switchErrorMustNotMergeWithValidInstruction() {
		SwitchInsn valid = new SwitchInsn(InsnArg.reg(0, ArgType.INT), 1, true);
		SwitchInsn invalid = new SwitchInsn(InsnArg.reg(0, ArgType.INT), 1, true);
		invalid.setInvalidSourceType(ArgType.FLOAT);
		assertDifferent(valid, invalid);
	}

	private static ArithNode arith() {
		return new ArithNode(ArithOp.ADD, InsnArg.reg(0, ArgType.INT),
				InsnArg.reg(1, ArgType.INT), InsnArg.reg(2, ArgType.INT), ArgType.INT);
	}

	private static void assertDifferent(InsnNode valid, InsnNode invalid) {
		assertThat(valid.isSame(invalid)).isFalse();
		assertThat(invalid.isSame(valid)).isFalse();
	}
}
