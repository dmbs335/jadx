package jadx.core.dex.visitors;

import java.util.List;
import java.util.function.Predicate;

import jadx.api.plugins.input.insns.Opcode;
import jadx.core.dex.info.FieldInfo;
import jadx.core.dex.instructions.ArithNode;
import jadx.core.dex.instructions.ArrayPutNode;
import jadx.core.dex.instructions.CmpInsnNode;
import jadx.core.dex.instructions.FieldPutNode;
import jadx.core.dex.instructions.FilledNewArrayNode;
import jadx.core.dex.instructions.IfNode;
import jadx.core.dex.instructions.InvokeNode;
import jadx.core.dex.instructions.MoveNode;
import jadx.core.dex.instructions.NumericCastNode;
import jadx.core.dex.instructions.ReturnNode;
import jadx.core.dex.instructions.SwitchInsn;
import jadx.core.dex.instructions.UnaryNumericNode;
import jadx.core.dex.instructions.args.ArgType;
import jadx.core.dex.nodes.BlockNode;
import jadx.core.dex.nodes.InsnNode;
import jadx.core.dex.nodes.MethodNode;
import jadx.core.dex.visitors.ssa.SSATransform;
import jadx.core.utils.InsnUtils;

/** Record invalid DEX numeric opcode sources before casts and aliases are optimized away. */
@JadxVisitor(
		name = "ValidateNumericOpcodeTypes",
		desc = "Preserve DEX numeric operand type errors",
		runAfter = SSATransform.class,
		runBefore = MoveInlineVisitor.class
)
public class ValidateNumericOpcodeTypes extends AbstractVisitor {
	@Override
	public void visit(MethodNode mth) {
		if (mth.isNoCode()) {
			return;
		}
		for (BlockNode block : mth.getBasicBlocks()) {
			for (InsnNode insn : block.getInstructions()) {
				if (insn instanceof CmpInsnNode) {
					validateCmp((CmpInsnNode) insn);
				} else if (insn instanceof ArithNode) {
					validateArith((ArithNode) insn);
				} else if (insn instanceof UnaryNumericNode) {
					validateUnary((UnaryNumericNode) insn);
				} else if (insn instanceof NumericCastNode) {
					validateCast((NumericCastNode) insn);
				} else if (insn instanceof ArrayPutNode) {
					validateArrayPut((ArrayPutNode) insn);
				} else if (insn instanceof FieldPutNode) {
					validateFieldPut((FieldPutNode) insn);
				} else if (insn instanceof InvokeNode) {
					validateInvoke((InvokeNode) insn);
				} else if (insn instanceof ReturnNode) {
					validateReturn(mth, (ReturnNode) insn);
				} else if (insn instanceof MoveNode) {
					validateMove(mth, (MoveNode) insn);
				} else if (insn instanceof FilledNewArrayNode) {
					validateFilledNewArray((FilledNewArrayNode) insn);
				} else if (insn instanceof SwitchInsn) {
					validateSwitch((SwitchInsn) insn);
				} else if (insn instanceof IfNode) {
					validateIf((IfNode) insn);
				}
			}
		}
	}

	private static void validateMove(MethodNode mth, MoveNode insn) {
		if (insn.getMoveOpcode() == Opcode.MOVE) {
			return; // Generic input carries no DEX move category to validate.
		}
		ArgType invalidType = InsnUtils.findUnambiguousDeclaredSourceType(insn.getArg(0));
		if (invalidType != null && !insn.isOpcodeCompatible(invalidType)) {
			mth.addError("Move opcode " + insn.getMoveOpcode() + " does not match source type " + invalidType
					+ " at " + InsnUtils.formatOffset(insn.getOffset()), null);
		}
	}

	private static void validateCmp(CmpInsnNode insn) {
		ArgType expectedType = insn.getOperandType();
		Predicate<ArgType> mismatch = type -> type != null
				&& (type.isTypeKnown() && !type.isGenericType() && !expectedType.equals(type)
						|| !type.isTypeKnown() && !type.contains(expectedType.getPrimitiveType()));
		for (int i = 0; i < insn.getArgsCount(); i++) {
			ArgType invalidType = InsnUtils.findDeclaredSourceType(insn.getArg(i), mismatch);
			if (invalidType != null) {
				insn.setInvalidSourceType(invalidType);
				return;
			}
		}
	}

	private static void validateArith(ArithNode insn) {
		if (insn.getOpcodeType() == null) {
			return;
		}
		for (int i = 0; i < insn.getArgsCount(); i++) {
			ArgType expectedType = InsnUtils.getArithOpcodeOperandType(insn, i);
			if (expectedType == null) {
				continue;
			}
			ArgType invalidType = InsnUtils.findDeclaredSourceType(insn.getArg(i),
					type -> InsnUtils.isIncompatibleArithPrimitive(type, expectedType));
			if (invalidType != null) {
				insn.setInvalidSourceType(invalidType, expectedType);
				return;
			}
		}
	}

	private static void validateUnary(UnaryNumericNode insn) {
		ArgType expectedType = insn.getOperandType();
		ArgType invalidType = InsnUtils.findDeclaredSourceType(insn.getArg(0),
				type -> InsnUtils.isIncompatibleUnaryPrimitive(type, expectedType));
		if (invalidType != null) {
			insn.setInvalidSourceType(invalidType);
		}
	}

	private static void validateCast(NumericCastNode insn) {
		ArgType expectedType = insn.getSourceType();
		ArgType invalidType = InsnUtils.findDeclaredSourceType(insn.getArg(0),
				type -> InsnUtils.isIncompatibleArithPrimitive(type, expectedType));
		if (invalidType != null) {
			insn.setInvalidSourceType(invalidType);
		}
	}

	private static void validateArrayPut(ArrayPutNode insn) {
		ArgType arrayType = InsnUtils.getDeclaredArrayType(insn.getArg(0));
		if (!arrayType.isArray()) {
			return;
		}
		ArgType elementType = arrayType.getArrayElement();
		if (!ArgType.FLOAT.equals(elementType) && !ArgType.DOUBLE.equals(elementType)) {
			return;
		}
		ArgType invalidType = InsnUtils.findDeclaredSourceType(insn.getArg(2),
				type -> InsnUtils.isIncompatibleArithPrimitive(type, elementType));
		if (invalidType != null) {
			insn.setInvalidSourceType(invalidType, elementType);
		}
	}

	private static void validateFieldPut(FieldPutNode insn) {
		ArgType fieldType = ((FieldInfo) insn.getIndex()).getType();
		if (!ArgType.FLOAT.equals(fieldType) && !ArgType.DOUBLE.equals(fieldType)) {
			return;
		}
		ArgType invalidType = InsnUtils.findDeclaredSourceType(insn.getArg(0),
				type -> InsnUtils.isIncompatibleArithPrimitive(type, fieldType));
		if (invalidType != null) {
			insn.setInvalidSourceType(invalidType);
		}
	}

	private static void validateInvoke(InvokeNode insn) {
		int offset = insn.getFirstArgOffset();
		List<ArgType> parameterTypes = insn.getCallMth().getArgumentsTypes();
		for (int i = 0; i < parameterTypes.size() && i + offset < insn.getArgsCount(); i++) {
			ArgType expectedType = parameterTypes.get(i);
			if (!ArgType.FLOAT.equals(expectedType) && !ArgType.DOUBLE.equals(expectedType)) {
				continue;
			}
			ArgType invalidType = InsnUtils.findDeclaredSourceType(insn.getArg(i + offset),
					type -> InsnUtils.isIncompatibleArithPrimitive(type, expectedType));
			if (invalidType != null) {
				insn.setInvalidArgType(i, invalidType, expectedType);
				return;
			}
		}
	}

	private static void validateReturn(MethodNode mth, ReturnNode insn) {
		ArgType expectedType = mth.getReturnType();
		if (!ArgType.FLOAT.equals(expectedType) && !ArgType.DOUBLE.equals(expectedType)) {
			return;
		}
		ArgType invalidType = InsnUtils.findDeclaredSourceType(insn.getArg(0),
				type -> InsnUtils.isIncompatibleArithPrimitive(type, expectedType));
		if (invalidType != null) {
			insn.setInvalidSourceType(invalidType);
		}
	}

	private static void validateFilledNewArray(FilledNewArrayNode insn) {
		ArgType expectedType = insn.getElemType();
		if (!ArgType.FLOAT.equals(expectedType) && !ArgType.DOUBLE.equals(expectedType)) {
			return;
		}
		for (int i = 0; i < insn.getArgsCount(); i++) {
			ArgType invalidType = InsnUtils.findDeclaredSourceType(insn.getArg(i),
					type -> InsnUtils.isIncompatibleArithPrimitive(type, expectedType));
			if (invalidType != null) {
				insn.setInvalidArgType(i, invalidType);
				return;
			}
		}
	}

	private static void validateSwitch(SwitchInsn insn) {
		ArgType invalidType = InsnUtils.findDeclaredSourceType(insn.getArg(0),
				type -> InsnUtils.isIncompatibleArithPrimitive(type, ArgType.INT));
		if (invalidType != null) {
			insn.setInvalidSourceType(invalidType);
		}
	}

	private static void validateIf(IfNode insn) {
		for (int i = 0; i < insn.getArgsCount(); i++) {
			ArgType invalidType = InsnUtils.findDeclaredSourceType(insn.getArg(i),
					type -> InsnUtils.isIncompatibleArithPrimitive(type, ArgType.INT));
			if (invalidType != null) {
				insn.setInvalidArgType(i, invalidType);
				return;
			}
		}
	}
}
