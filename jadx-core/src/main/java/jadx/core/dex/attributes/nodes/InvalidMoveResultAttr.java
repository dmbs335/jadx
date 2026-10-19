package jadx.core.dex.attributes.nodes;

import jadx.api.plugins.input.data.attributes.PinnedAttribute;
import jadx.api.plugins.input.insns.Opcode;
import jadx.core.dex.attributes.AType;
import jadx.core.dex.instructions.args.ArgType;

/**
 * Keeps an invalid move-result opcode visible after its instruction is merged into the producer.
 */
public final class InvalidMoveResultAttr extends PinnedAttribute {
	private final Opcode opcode;
	private final ArgType resultType;

	public InvalidMoveResultAttr(Opcode opcode, ArgType resultType) {
		this.opcode = opcode;
		this.resultType = resultType;
	}

	public Opcode getOpcode() {
		return opcode;
	}

	public ArgType getResultType() {
		return resultType;
	}

	@Override
	public AType<InvalidMoveResultAttr> getAttrType() {
		return AType.INVALID_MOVE_RESULT;
	}
}
