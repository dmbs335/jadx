package jadx.core.dex.attributes.nodes;

import jadx.api.plugins.input.data.attributes.PinnedAttribute;
import jadx.core.dex.attributes.AType;

/**
 * Marks a coroutine loop whose multi-entry state edge was structurally normalized.
 *
 * Region fallback diagnostics can remain useful for these large state machines, but they no
 * longer indicate an unresolved multi-entry loop and should not be promoted to warnings.
 */
public final class NormalizedCoroutineLoopAttr extends PinnedAttribute {
	public static final NormalizedCoroutineLoopAttr INSTANCE = new NormalizedCoroutineLoopAttr();

	private NormalizedCoroutineLoopAttr() {
	}

	@Override
	public AType<NormalizedCoroutineLoopAttr> getAttrType() {
		return AType.NORMALIZED_COROUTINE_LOOP;
	}

	@Override
	public String toString() {
		return "NORMALIZED_COROUTINE_LOOP";
	}
}
