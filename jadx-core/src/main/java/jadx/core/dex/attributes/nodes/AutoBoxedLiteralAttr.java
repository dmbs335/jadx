package jadx.core.dex.attributes.nodes;

import jadx.api.plugins.input.data.attributes.IJadxAttribute;
import jadx.core.dex.attributes.AType;

/** Marks a literal preserved from a removed primitive boxing call. */
public final class AutoBoxedLiteralAttr implements IJadxAttribute {
	public static final AutoBoxedLiteralAttr INSTANCE = new AutoBoxedLiteralAttr();

	private AutoBoxedLiteralAttr() {
	}

	@Override
	public AType<AutoBoxedLiteralAttr> getAttrType() {
		return AType.AUTO_BOXED_LITERAL;
	}

	@Override
	public String toString() {
		return "AUTO_BOXED_LITERAL";
	}
}
