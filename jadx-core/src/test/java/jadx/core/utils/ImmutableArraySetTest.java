package jadx.core.utils;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ImmutableArraySetTest {
	@Test
	void testSetContractAndOrder() {
		Set<String> source = new LinkedHashSet<>(List.of("first", "second", "third"));
		Set<String> set = new ImmutableArraySet<>(source);

		assertThat(set).containsExactly("first", "second", "third");
		assertThat(set).contains("second");
		assertThat(set).doesNotContain("missing");
		assertThat(set).isEqualTo(source);
		assertThat(set.hashCode()).isEqualTo(source.hashCode());
		assertThatThrownBy(() -> set.add("fourth")).isInstanceOf(UnsupportedOperationException.class);
	}
}
