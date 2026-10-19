package jadx.core.dex.attributes.nodes;

import jadx.api.plugins.input.data.attributes.PinnedAttribute;
import jadx.core.dex.attributes.AType;

/** Marks a retry completion normalized through the coroutine entry-state dispatcher. */
public final class CoroutineRetryCatchDispatchAttr extends PinnedAttribute {
	public static final CoroutineRetryCatchDispatchAttr INSTANCE = new CoroutineRetryCatchDispatchAttr();

	private CoroutineRetryCatchDispatchAttr() {
	}

	@Override
	public AType<CoroutineRetryCatchDispatchAttr> getAttrType() {
		return AType.COROUTINE_RETRY_CATCH_DISPATCH;
	}
}
