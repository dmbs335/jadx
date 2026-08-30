package jadx.core.dex.attributes.nodes;

import jadx.api.plugins.input.data.attributes.PinnedAttribute;
import jadx.core.dex.attributes.AType;

/**
 * Marks a field read which forced an early type fallback after use casts were inserted.
 * A final type-inference pass can remove the coarse warning only after this exact conflict
 * becomes compatible.
 */
public final class IncompleteFieldTypeConflictAttr extends PinnedAttribute {
	public static final IncompleteFieldTypeConflictAttr INSTANCE = new IncompleteFieldTypeConflictAttr();

	private IncompleteFieldTypeConflictAttr() {
	}

	@Override
	public AType<IncompleteFieldTypeConflictAttr> getAttrType() {
		return AType.INCOMPLETE_FIELD_TYPE_CONFLICT;
	}

	@Override
	public String toString() {
		return "INCOMPLETE_FIELD_TYPE_CONFLICT";
	}
}
