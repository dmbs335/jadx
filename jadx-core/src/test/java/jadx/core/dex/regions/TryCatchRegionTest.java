package jadx.core.dex.regions;

import java.util.List;

import org.junit.jupiter.api.Test;

import jadx.core.dex.trycatch.ExceptionHandler;
import jadx.core.dex.trycatch.TryCatchBlockAttr;

import static org.assertj.core.api.Assertions.assertThat;

class TryCatchRegionTest {

	@Test
	void detectsMultipleCatchAllHandlersThatCannotBeEmitted() {
		ExceptionHandler first = ExceptionHandler.build(null, 1, null);
		first.setHandlerRegion(new Region(null));
		ExceptionHandler second = ExceptionHandler.build(null, 2, null);
		second.setHandlerRegion(new Region(null));

		TryCatchRegion region = new TryCatchRegion(null, new Region(null));
		region.setTryCatchBlock(new TryCatchBlockAttr(1, List.of(first), List.of()));
		assertThat(region.hasMultipleCatchAllHandlers()).isFalse();

		region.setTryCatchBlock(new TryCatchBlockAttr(2, List.of(first, second), List.of()));
		assertThat(region.hasMultipleCatchAllHandlers()).isTrue();
	}
}
