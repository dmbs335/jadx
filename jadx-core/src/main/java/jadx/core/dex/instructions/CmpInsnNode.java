package jadx.core.dex.instructions;

import org.jetbrains.annotations.Nullable;

import jadx.core.dex.instructions.args.ArgType;
import jadx.core.dex.nodes.InsnNode;

/** A DEX comparison with the operand type specified by its opcode. */
public final class CmpInsnNode extends InsnNode {
	private final ArgType operandType;
	private @Nullable ArgType invalidSourceType;

	public CmpInsnNode(InsnType insnType, ArgType operandType) {
		super(insnType, 2);
		this.operandType = operandType;
	}

	public ArgType getOperandType() {
		return operandType;
	}

	public @Nullable ArgType getInvalidSourceType() {
		return invalidSourceType;
	}

	public void setInvalidSourceType(ArgType invalidSourceType) {
		this.invalidSourceType = invalidSourceType;
	}

	@Override
	public CmpInsnNode copy() {
		CmpInsnNode copy = new CmpInsnNode(insnType, operandType);
		copy.invalidSourceType = invalidSourceType;
		return copyCommonParams(copy);
	}

	@Override
	public boolean isSame(InsnNode obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof CmpInsnNode) || !super.isSame(obj)) {
			return false;
		}
		CmpInsnNode other = (CmpInsnNode) obj;
		return operandType.equals(other.operandType);
	}
}
