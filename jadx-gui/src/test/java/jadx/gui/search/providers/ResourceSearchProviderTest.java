package jadx.gui.search.providers;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ResourceSearchProviderTest {

	@Test
	void resourceSizeLimitConversionDoesNotOverflowAtTwoGigabytes() {
		assertThat(ResourceSearchProvider.sizeLimitBytes(2_048))
				.isEqualTo(2_147_483_648L);
	}

	@Test
	void resourceSizeLimitConversionSupportsLargestSetting() {
		assertThat(ResourceSearchProvider.sizeLimitBytes(Integer.MAX_VALUE))
				.isEqualTo(2_251_799_812_636_672L);
	}
}
