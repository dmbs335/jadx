package jadx.core.dex.visitors.regions;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DepthRegionTraversalTest {

	@Test
	void useRegionTreeSizeForDuplicatedControlFlow() {
		assertThat(DepthRegionTraversal.calcIterativeLimit(193, 700)).isEqualTo(1400);
	}

	@Test
	void keepBlockBasedFloorForRegularMethods() {
		assertThat(DepthRegionTraversal.calcIterativeLimit(100, 20)).isEqualTo(500);
	}
}
