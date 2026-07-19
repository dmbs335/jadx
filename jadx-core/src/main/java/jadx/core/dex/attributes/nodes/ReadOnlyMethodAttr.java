package jadx.core.dex.attributes.nodes;

import jadx.api.plugins.input.data.attributes.PinnedAttribute;
import jadx.core.dex.attributes.AType;

public final class ReadOnlyMethodAttr extends PinnedAttribute {
	public static final ReadOnlyMethodAttr INSTANCE = new ReadOnlyMethodAttr();

	private ReadOnlyMethodAttr() {
	}

	@Override
	public AType<ReadOnlyMethodAttr> getAttrType() {
		return AType.READ_ONLY_METHOD;
	}

	@Override
	public String toString() {
		return "READ_ONLY_METHOD";
	}
}
