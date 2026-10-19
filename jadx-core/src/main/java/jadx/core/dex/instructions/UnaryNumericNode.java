package jadx.core.dex.instructions;

import org.jetbrains.annotations.Nullable;

import jadx.core.dex.instructions.args.ArgType;
import jadx.core.dex.nodes.InsnNode;

/** A DEX unary numeric operation with the operand type specified by its opcode. */
public final class UnaryNumericNode extends InsnNode {
	private final ArgType operandType;
	private @Nullable ArgType invalidSourceType;

	public UnaryNumericNode(InsnType insnType, ArgType operandType) {
		super(insnType, 1);
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
	public UnaryNumericNode copy() {
		UnaryNumericNode copy = new UnaryNumericNode(insnType, operandType);
		copy.invalidSourceType = invalidSourceType;
		return copyCommonParams(copy);
	}

	@Override
	public boolean isSame(InsnNode obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof UnaryNumericNode) || !super.isSame(obj)) {
			return false;
		}
		UnaryNumericNode other = (UnaryNumericNode) obj;
		return operandType.equals(other.operandType);
	}
}
