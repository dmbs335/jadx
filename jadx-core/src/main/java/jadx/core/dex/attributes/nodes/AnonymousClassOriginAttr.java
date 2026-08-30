package jadx.core.dex.attributes.nodes;

import jadx.api.plugins.input.data.attributes.PinnedAttribute;
import jadx.core.dex.attributes.AType;

/** Stable marker for a class that was originally selected for anonymous-class inlining. */
public final class AnonymousClassOriginAttr extends PinnedAttribute {
	public static final AnonymousClassOriginAttr INSTANCE = new AnonymousClassOriginAttr();

	private AnonymousClassOriginAttr() {
	}

	@Override
	public AType<AnonymousClassOriginAttr> getAttrType() {
		return AType.ANONYMOUS_CLASS_ORIGIN;
	}
}
