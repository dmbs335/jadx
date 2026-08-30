package jadx.core.utils;

import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ImmutableSortedSetTest {
	@Test
	void preservesNaturalOrderAndRanges() {
		SortedSet<Integer> source = new TreeSet<>();
		source.add(4);
		source.add(1);
		source.add(3);
		source.add(2);

		SortedSet<Integer> result = ImmutableSortedSet.copyOf(source);

		assertThat(result).containsExactly(1, 2, 3, 4);
		assertThat(result.first()).isEqualTo(1);
		assertThat(result.last()).isEqualTo(4);
		assertThat(result.headSet(3)).containsExactly(1, 2);
		assertThat(result.tailSet(3)).containsExactly(3, 4);
		assertThat(result.subSet(2, 4)).containsExactly(2, 3);
		assertThatThrownBy(() -> result.subSet(5, 4)).isInstanceOf(IllegalArgumentException.class);
		assertThat(result).contains(3).doesNotContain(5);
		assertThatThrownBy(() -> result.add(5)).isInstanceOf(UnsupportedOperationException.class);
	}

	@Test
	void preservesCustomComparator() {
		SortedSet<Integer> source = new TreeSet<>(Comparator.reverseOrder());
		source.add(1);
		source.add(3);
		source.add(2);

		SortedSet<Integer> result = ImmutableSortedSet.copyOf(source);

		assertThat(result).containsExactly(3, 2, 1);
		assertThat(result.comparator()).isSameAs(source.comparator());
		assertThat(result.headSet(2)).containsExactly(3);
	}
}
