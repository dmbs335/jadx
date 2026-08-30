package jadx.core.dex.attributes.nodes;

import jadx.api.plugins.input.data.attributes.PinnedAttribute;
import jadx.core.dex.attributes.AType;

/**
 * Marks an irreducible source-loop header validated by a CFG normalization pass.
 */
public final class ExpectedTraversalCycleAttr extends PinnedAttribute {
	public static final ExpectedTraversalCycleAttr INSTANCE = new ExpectedTraversalCycleAttr();

	private ExpectedTraversalCycleAttr() {
	}

	@Override
	public AType<ExpectedTraversalCycleAttr> getAttrType() {
		return AType.EXPECTED_TRAVERSAL_CYCLE;
	}

	@Override
	public String toString() {
		return "EXPECTED_TRAVERSAL_CYCLE";
	}
}
