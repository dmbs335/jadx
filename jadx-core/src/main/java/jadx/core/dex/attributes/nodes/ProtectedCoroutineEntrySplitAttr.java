package jadx.core.dex.attributes.nodes;

import jadx.api.plugins.input.data.attributes.PinnedAttribute;
import jadx.core.dex.attributes.AType;

/** Marks a loop whose bounded protected resume entry tails were copied before region building. */
public final class ProtectedCoroutineEntrySplitAttr extends PinnedAttribute {
	public static final ProtectedCoroutineEntrySplitAttr INSTANCE = new ProtectedCoroutineEntrySplitAttr();

	private ProtectedCoroutineEntrySplitAttr() {
	}

	@Override
	public AType<ProtectedCoroutineEntrySplitAttr> getAttrType() {
		return AType.PROTECTED_COROUTINE_ENTRY_SPLIT;
	}
}
