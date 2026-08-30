package jadx.core.dex.attributes.nodes;

import jadx.api.plugins.input.data.attributes.PinnedAttribute;
import jadx.core.dex.attributes.AType;

public final class UnsupportedMultiEntryLoopAttr extends PinnedAttribute {
	public static final UnsupportedMultiEntryLoopAttr INSTANCE = new UnsupportedMultiEntryLoopAttr();

	private UnsupportedMultiEntryLoopAttr() {
	}

	@Override
	public AType<UnsupportedMultiEntryLoopAttr> getAttrType() {
		return AType.UNSUPPORTED_MULTI_ENTRY_LOOP;
	}

	@Override
	public String toString() {
		return "UNSUPPORTED_MULTI_ENTRY_LOOP";
	}
}
