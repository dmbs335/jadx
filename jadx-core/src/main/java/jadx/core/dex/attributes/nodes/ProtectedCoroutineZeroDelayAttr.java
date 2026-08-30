package jadx.core.dex.attributes.nodes;

import jadx.api.plugins.input.data.attributes.PinnedAttribute;
import jadx.core.dex.attributes.AType;

/**
 * Marks the zero-delay bridge after the complete protected polling decision has been verified.
 * The companion immediate-completion edge can then reuse that classification without mutating
 * the CFG.
 */
public final class ProtectedCoroutineZeroDelayAttr extends PinnedAttribute {
	public static final ProtectedCoroutineZeroDelayAttr INSTANCE =
			new ProtectedCoroutineZeroDelayAttr();

	private ProtectedCoroutineZeroDelayAttr() {
	}

	@Override
	public AType<ProtectedCoroutineZeroDelayAttr> getAttrType() {
		return AType.PROTECTED_COROUTINE_ZERO_DELAY;
	}

	@Override
	public String toString() {
		return "PROTECTED_COROUTINE_ZERO_DELAY";
	}
}
