package jadx.core.dex.visitors;

import java.util.function.Predicate;

import jadx.core.dex.instructions.CmpInsnNode;
import jadx.core.dex.instructions.args.ArgType;
import jadx.core.dex.nodes.BlockNode;
import jadx.core.dex.nodes.InsnNode;
import jadx.core.dex.nodes.MethodNode;
import jadx.core.dex.visitors.ssa.SSATransform;
import jadx.core.utils.InsnUtils;

/** Record invalid DEX comparison sources before casts and aliases are optimized away. */
@JadxVisitor(
		name = "ValidateCmpTypes",
		desc = "Preserve DEX comparison operand type errors",
		runAfter = SSATransform.class,
		runBefore = MoveInlineVisitor.class
)
public class ValidateCmpTypes extends AbstractVisitor {
	@Override
	public void visit(MethodNode mth) {
		if (mth.isNoCode()) {
			return;
		}
		for (BlockNode block : mth.getBasicBlocks()) {
			for (InsnNode insn : block.getInstructions()) {
				if (insn instanceof CmpInsnNode) {
					validate((CmpInsnNode) insn);
				}
			}
		}
	}

	private static void validate(CmpInsnNode insn) {
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
}
