package jadx.core.dex.attributes.nodes;

import jadx.api.plugins.input.data.attributes.PinnedAttribute;
import jadx.core.dex.attributes.AType;

/**
 * Marks a block whose repeated region rendering was proven equivalent by a CFG normalization.
 */
public final class SafeDuplicationAttr extends PinnedAttribute {
	public static final SafeDuplicationAttr INSTANCE = new SafeDuplicationAttr();

	private SafeDuplicationAttr() {
	}

	@Override
	public AType<SafeDuplicationAttr> getAttrType() {
		return AType.SAFE_DUPLICATION;
	}

	@Override
	public String toString() {
		return "SAFE_DUPLICATION";
	}
}
