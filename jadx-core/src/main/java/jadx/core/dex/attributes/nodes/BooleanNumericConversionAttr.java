package jadx.core.dex.attributes.nodes;

import jadx.api.plugins.input.data.attributes.IJadxAttribute;
import jadx.core.dex.attributes.AType;

public final class BooleanNumericConversionAttr implements IJadxAttribute {
	public static final BooleanNumericConversionAttr INSTANCE = new BooleanNumericConversionAttr();

	private BooleanNumericConversionAttr() {
	}

	@Override
	public AType<BooleanNumericConversionAttr> getAttrType() {
		return AType.BOOLEAN_NUMERIC_CONVERSION;
	}

	@Override
	public String toString() {
		return "BOOLEAN_NUMERIC_CONVERSION";
	}
}
