package jadx.core.dex.attributes.nodes;

import jadx.api.plugins.input.data.attributes.PinnedAttribute;
import jadx.core.dex.attributes.AType;

/**
 * Marks a register-only back-edge tail owned by a normalized coroutine loop.
 */
public final class CoroutineLoopCarryTailAttr extends PinnedAttribute {
	public static final CoroutineLoopCarryTailAttr INSTANCE = new CoroutineLoopCarryTailAttr();

	private CoroutineLoopCarryTailAttr() {
	}

	@Override
	public AType<CoroutineLoopCarryTailAttr> getAttrType() {
		return AType.COROUTINE_LOOP_CARRY_TAIL;
	}

	@Override
	public String toString() {
		return "COROUTINE_LOOP_CARRY_TAIL";
	}
}
