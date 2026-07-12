package jadx.core.dex.visitors.regions.variables;

import java.util.Collections;

import org.junit.jupiter.api.Test;

import jadx.core.dex.nodes.IBlock;
import jadx.core.dex.nodes.IRegion;
import jadx.core.dex.nodes.InsnContainer;
import jadx.core.dex.regions.Region;

import static org.assertj.core.api.Assertions.assertThat;

class UsePlaceTest {

	@Test
	void hashCodeMustMatchObjectsHashWithoutAllocatingVarargs() {
		IRegion region = new Region(null);
		IBlock block = new InsnContainer(Collections.emptyList());

		UsePlace usePlace = new UsePlace(region, block);

		assertThat(usePlace.hashCode()).isEqualTo(31 * (31 + region.hashCode()) + block.hashCode());
	}
}
