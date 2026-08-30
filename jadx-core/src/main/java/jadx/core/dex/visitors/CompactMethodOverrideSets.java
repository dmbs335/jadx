package jadx.core.dex.visitors;

import java.util.SortedSet;

import jadx.core.dex.attributes.AType;
import jadx.core.dex.attributes.nodes.MethodOverrideAttr;
import jadx.core.dex.nodes.MethodNode;
import jadx.core.utils.ImmutableSortedSet;

@JadxVisitor(
		name = "CompactMethodOverrideSets",
		desc = "Freeze shared method override sets into compact sorted lists",
		runAfter = OverrideMethodVisitor.class
)
public class CompactMethodOverrideSets extends AbstractVisitor {
	@Override
	public void visit(MethodNode mth) {
		MethodOverrideAttr attr = mth.get(AType.METHOD_OVERRIDE);
		if (attr == null) {
			return;
		}
		SortedSet<MethodNode> relatedMethods = attr.getRelatedMthNodes();
		if (relatedMethods instanceof ImmutableSortedSet) {
			return;
		}
		SortedSet<MethodNode> compact = ImmutableSortedSet.copyOf(relatedMethods);
		for (MethodNode relatedMth : relatedMethods) {
			MethodOverrideAttr relatedAttr = relatedMth.get(AType.METHOD_OVERRIDE);
			if (relatedAttr != null && relatedAttr.getRelatedMthNodes() == relatedMethods) {
				relatedAttr.setRelatedMthNodes(compact);
			}
		}
	}
}
