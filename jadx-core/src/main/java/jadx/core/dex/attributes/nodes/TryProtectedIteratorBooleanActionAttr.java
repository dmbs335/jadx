package jadx.core.dex.attributes.nodes;

import jadx.api.plugins.input.data.attributes.PinnedAttribute;
import jadx.core.dex.attributes.AType;

/**
 * Marks a verified try-protected iterator loop whose suspended Boolean action resumes before the
 * source-level loop. Region building can then join the obfuscated state branch at the loop
 * pre-header without changing unrelated normalized coroutines.
 */
public final class TryProtectedIteratorBooleanActionAttr extends PinnedAttribute {
	public static final TryProtectedIteratorBooleanActionAttr INSTANCE =
			new TryProtectedIteratorBooleanActionAttr();

	private TryProtectedIteratorBooleanActionAttr() {
	}

	@Override
	public AType<TryProtectedIteratorBooleanActionAttr> getAttrType() {
		return AType.TRY_PROTECTED_ITERATOR_BOOLEAN_ACTION;
	}

	@Override
	public String toString() {
		return "TRY_PROTECTED_ITERATOR_BOOLEAN_ACTION";
	}
}
