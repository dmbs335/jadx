package jadx.core.dex.attributes.nodes;

import jadx.api.plugins.input.data.attributes.IJadxAttribute;
import jadx.core.dex.attributes.AType;
import jadx.core.dex.nodes.BlockNode;

/**
 * Marks a synthetic join created to keep coroutine state restoration outside a source loop.
 */
public final class CoroutineLoopPreHeaderAttr implements IJadxAttribute {
	private final BlockNode block;

	public CoroutineLoopPreHeaderAttr(BlockNode block) {
		this.block = block;
	}

	public BlockNode getBlock() {
		return block;
	}

	@Override
	public AType<CoroutineLoopPreHeaderAttr> getAttrType() {
		return AType.COROUTINE_LOOP_PRE_HEADER;
	}

	@Override
	public String toString() {
		return "COROUTINE_LOOP_PRE_HEADER: " + block;
	}
}
