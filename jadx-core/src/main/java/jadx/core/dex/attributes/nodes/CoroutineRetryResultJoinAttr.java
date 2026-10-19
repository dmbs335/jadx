package jadx.core.dex.attributes.nodes;

import jadx.api.plugins.input.data.attributes.IJadxAttribute;
import jadx.core.dex.attributes.AType;
import jadx.core.dex.nodes.BlockNode;

/**
 * Identifies the shared request and result-processing joins in a normalized coroutine retry loop.
 */
public final class CoroutineRetryResultJoinAttr implements IJadxAttribute {
	private final BlockNode requestEntry;
	private final BlockNode resultJoin;

	public CoroutineRetryResultJoinAttr(BlockNode requestEntry, BlockNode resultJoin) {
		this.requestEntry = requestEntry;
		this.resultJoin = resultJoin;
	}

	public BlockNode getRequestEntry() {
		return requestEntry;
	}

	public BlockNode getResultJoin() {
		return resultJoin;
	}

	@Override
	public AType<CoroutineRetryResultJoinAttr> getAttrType() {
		return AType.COROUTINE_RETRY_RESULT_JOIN;
	}

	@Override
	public String toString() {
		return "COROUTINE_RETRY_RESULT_JOIN: " + requestEntry + " -> " + resultJoin;
	}
}
