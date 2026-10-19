package jadx.core.dex.visitors;

import java.util.function.Predicate;

import jadx.core.dex.instructions.ArithNode;
import jadx.core.dex.instructions.CmpInsnNode;
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
				}
			}
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
}
