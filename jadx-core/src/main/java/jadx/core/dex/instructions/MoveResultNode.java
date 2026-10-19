package jadx.core.dex.instructions;

import jadx.api.plugins.input.insns.Opcode;
import jadx.core.dex.instructions.args.ArgType;
import jadx.core.dex.nodes.InsnNode;

/** Preserve the input move-result variant until its producer return type is known. */
public final class MoveResultNode extends InsnNode {
	private final Opcode moveResultOpcode;

	public MoveResultNode(Opcode moveResultOpcode) {
		super(InsnType.MOVE_RESULT, 0);
		this.moveResultOpcode = moveResultOpcode;
	}

	public Opcode getMoveResultOpcode() {
		return moveResultOpcode;
	}

	public boolean isOpcodeCompatible(ArgType resultType) {
		if (ArgType.VOID.equals(resultType)) {
			return false;
		}
		if (!resultType.isTypeKnown()) {
			return true;
		}
		switch (moveResultOpcode) {
			case MOVE_RESULT:
				// Legacy input plugins may not distinguish move-result variants.
				return true;
			case MOVE_RESULT_NARROW:
				return resultType.isPrimitive() && !ArgType.LONG.equals(resultType) && !ArgType.DOUBLE.equals(resultType);
			case MOVE_RESULT_WIDE:
				return ArgType.LONG.equals(resultType) || ArgType.DOUBLE.equals(resultType);
			case MOVE_RESULT_OBJECT:
				return resultType.isObject() || resultType.isArray();
			default:
				throw new IllegalStateException("Unexpected move-result opcode: " + moveResultOpcode);
		}
	}

	@Override
	public MoveResultNode copy() {
		return copyCommonParams(new MoveResultNode(moveResultOpcode));
	}

	@Override
	public boolean isSame(InsnNode obj) {
		return obj instanceof MoveResultNode && moveResultOpcode == ((MoveResultNode) obj).moveResultOpcode && super.isSame(obj);
	}
}
