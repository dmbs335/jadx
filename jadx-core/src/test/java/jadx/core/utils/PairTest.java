package jadx.core.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PairTest {

	@Test
	void supportsNullValuesInHashCollections() {
		Pair<String> first = new Pair<>("value", null);
		Pair<String> second = new Pair<>("value", null);

		assertThat(first).isEqualTo(second);
		assertThat(first.hashCode()).isEqualTo(second.hashCode());
	}
}
