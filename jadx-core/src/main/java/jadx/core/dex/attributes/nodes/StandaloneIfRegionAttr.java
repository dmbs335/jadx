package jadx.core.dex.attributes.nodes;

import jadx.api.plugins.input.data.attributes.PinnedAttribute;
import jadx.core.dex.attributes.AType;

/**
 * Keeps an if block as a standalone condition region instead of merging it into its predecessor.
 *
 * Shared state-machine tails can be rendered from several entry regions. If one entry merges a
 * shared guard first, the guard is marked as already consumed and can disappear from later
 * entries. Keeping the guard standalone allows every entry to render the same decision.
 */
public final class StandaloneIfRegionAttr extends PinnedAttribute {
	public static final StandaloneIfRegionAttr INSTANCE = new StandaloneIfRegionAttr();

	private StandaloneIfRegionAttr() {
	}

	@Override
	public AType<StandaloneIfRegionAttr> getAttrType() {
		return AType.STANDALONE_IF_REGION;
	}

	@Override
	public String toString() {
		return "STANDALONE_IF_REGION";
	}
}
