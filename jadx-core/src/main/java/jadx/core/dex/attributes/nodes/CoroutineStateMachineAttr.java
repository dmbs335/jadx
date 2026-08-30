package jadx.core.dex.attributes.nodes;

import jadx.api.plugins.input.data.attributes.PinnedAttribute;
import jadx.core.dex.attributes.AType;

/**
 * Marks methods recognized as compiler-generated coroutine state-machine bodies.
 *
 * Downstream generic passes must depend on this semantic marker instead of matching
 * compiler-specific method names directly.
 */
public final class CoroutineStateMachineAttr extends PinnedAttribute {
	public static final CoroutineStateMachineAttr INSTANCE = new CoroutineStateMachineAttr();

	private CoroutineStateMachineAttr() {
	}

	@Override
	public AType<CoroutineStateMachineAttr> getAttrType() {
		return AType.COROUTINE_STATE_MACHINE;
	}

	@Override
	public String toString() {
		return "COROUTINE_STATE_MACHINE";
	}
}
