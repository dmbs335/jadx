package jadx.core.dex.instructions;

import jadx.api.plugins.input.insns.Opcode;
import jadx.core.dex.instructions.args.ArgType;
import jadx.core.dex.nodes.InsnNode;

/** Preserve the source move variant before SSA inlines the instruction. */
public final class MoveNode extends InsnNode {
	private final Opcode moveOpcode;

	public MoveNode(Opcode moveOpcode) {
		super(InsnType.MOVE, 1);
		this.moveOpcode = moveOpcode;
	}

	public Opcode getMoveOpcode() {
		return moveOpcode;
	}

	public boolean isOpcodeCompatible(ArgType sourceType) {
		if (sourceType == null || !sourceType.isTypeKnown()) {
			return true;
		}
		switch (moveOpcode) {
			case MOVE:
				// Generic input (including JVM aload) does not specify a DEX move category.
				return true;
			case MOVE_NARROW:
				return sourceType.isPrimitive() && !ArgType.LONG.equals(sourceType) && !ArgType.DOUBLE.equals(sourceType);
			case MOVE_WIDE:
				return ArgType.LONG.equals(sourceType) || ArgType.DOUBLE.equals(sourceType);
			case MOVE_OBJECT:
				return sourceType.isObject() || sourceType.isArray();
			default:
				throw new IllegalStateException("Unexpected move opcode: " + moveOpcode);
		}
	}

	@Override
	public MoveNode copy() {
		return copyCommonParams(new MoveNode(moveOpcode));
	}

	@Override
	public boolean isSame(InsnNode obj) {
		return obj instanceof MoveNode && moveOpcode == ((MoveNode) obj).moveOpcode && super.isSame(obj);
	}
}
