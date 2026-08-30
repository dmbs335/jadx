package jadx.core.dex.attributes.nodes;

import jadx.api.plugins.input.data.attributes.PinnedAttribute;
import jadx.core.dex.attributes.AType;

public final class ConstantReturnMethodAttr extends PinnedAttribute {
	public static final ConstantReturnMethodAttr INSTANCE = new ConstantReturnMethodAttr();

	private ConstantReturnMethodAttr() {
	}

	@Override
	public AType<ConstantReturnMethodAttr> getAttrType() {
		return AType.CONSTANT_RETURN_METHOD;
	}

	@Override
	public String toString() {
		return "CONSTANT_RETURN_METHOD";
	}
}
