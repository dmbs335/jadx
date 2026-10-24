package jadx.core.dex.instructions;

import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import jadx.api.plugins.input.insns.Opcode;
import jadx.core.dex.instructions.args.ArgType;
import jadx.core.dex.nodes.InsnNode;

/**
 * A value return whose source type and input opcode must survive later inference and
 * simplification.
 */
public final class ReturnNode extends InsnNode {
	private final Opcode returnOpcode;
	private @Nullable ArgType invalidSourceType;

	public ReturnNode(Opcode returnOpcode) {
		super(InsnType.RETURN, 1);
		this.returnOpcode = returnOpcode;
	}

	public Opcode getReturnOpcode() {
		return returnOpcode;
	}

	public boolean isOpcodeCompatible(ArgType returnType) {
		switch (returnOpcode) {
			case RETURN:
				// Legacy input plugins may not distinguish return opcode variants.
				return true;
			case RETURN_NARROW:
				return returnType.isPrimitive() && !ArgType.VOID.equals(returnType)
						&& !ArgType.LONG.equals(returnType) && !ArgType.DOUBLE.equals(returnType);
			case RETURN_INT:
				return returnType.isPrimitive() && !ArgType.VOID.equals(returnType)
						&& !ArgType.LONG.equals(returnType) && !ArgType.FLOAT.equals(returnType) && !ArgType.DOUBLE.equals(returnType);
			case RETURN_WIDE:
				return ArgType.LONG.equals(returnType) || ArgType.DOUBLE.equals(returnType);
			case RETURN_OBJECT:
				return returnType.isObject() || returnType.isArray();
			case RETURN_LONG:
				return ArgType.LONG.equals(returnType);
			case RETURN_FLOAT:
				return ArgType.FLOAT.equals(returnType);
			case RETURN_DOUBLE:
				return ArgType.DOUBLE.equals(returnType);
			default:
				throw new IllegalStateException("Unexpected return opcode: " + returnOpcode);
		}
	}

	public @Nullable ArgType getInvalidSourceType() {
		return invalidSourceType;
	}

	public void setInvalidSourceType(ArgType invalidSourceType) {
		this.invalidSourceType = invalidSourceType;
	}

	@Override
	public ReturnNode copy() {
		ReturnNode copy = new ReturnNode(returnOpcode);
		copy.invalidSourceType = invalidSourceType;
		return copyCommonParams(copy);
	}

	@Override
	public boolean isSame(InsnNode obj) {
		return obj instanceof ReturnNode
				&& returnOpcode == ((ReturnNode) obj).returnOpcode
				&& Objects.equals(invalidSourceType, ((ReturnNode) obj).invalidSourceType)
				&& super.isSame(obj);
	}
}
