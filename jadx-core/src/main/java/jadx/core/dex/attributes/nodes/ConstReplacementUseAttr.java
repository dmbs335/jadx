package jadx.core.dex.attributes.nodes;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jadx.api.plugins.input.data.attributes.PinnedAttribute;
import jadx.core.dex.attributes.AType;
import jadx.core.dex.nodes.FieldNode;
import jadx.core.dex.nodes.MethodNode;

/** Distinguishes field usages inserted by constant restoration from usages present in the input. */
public final class ConstReplacementUseAttr extends PinnedAttribute {
	public static void mark(FieldNode field, MethodNode method) {
		ConstReplacementUseAttr attr = field.get(AType.CONST_REPLACEMENT_USE);
		if (attr == null) {
			attr = new ConstReplacementUseAttr();
			field.addAttr(attr);
		}
		attr.methods.add(method);
	}

	private final Set<MethodNode> methods = new HashSet<>();

	private ConstReplacementUseAttr() {
	}

	public boolean containsAll(List<MethodNode> useIn) {
		return methods.containsAll(useIn);
	}

	@Override
	public AType<ConstReplacementUseAttr> getAttrType() {
		return AType.CONST_REPLACEMENT_USE;
	}
}
