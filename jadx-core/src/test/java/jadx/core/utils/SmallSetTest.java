package jadx.core.utils;

import java.util.Iterator;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

class SmallSetTest {

	@Test
	void storesSmallSetsInline() {
		SmallSet<String> set = new SmallSet<>();

		for (int i = 0; i < 9; i++) {
			assertThat(set.add("v" + i)).isTrue();
		}
		assertThat(set.add("v0")).isFalse();

		assertThat(set).containsExactly("v0", "v1", "v2", "v3", "v4", "v5", "v6", "v7", "v8");
	}

	@Test
	void promotesWithoutChangingSetSemantics() {
		SmallSet<Integer> set = new SmallSet<>();
		for (int i = 0; i < 10; i++) {
			assertThat(set.add(i)).isTrue();
		}

		assertThat(set).containsExactlyInAnyOrder(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
		assertThat(set.remove(5)).isTrue();
		assertThat(set).doesNotContain(5).hasSize(9);
	}

	@Test
	void inlineIteratorSupportsRemoval() {
		SmallSet<String> set = new SmallSet<>();
		set.add("a");
		set.add("b");
		set.add("c");
		Iterator<String> iterator = set.iterator();

		assertThat(iterator.next()).isEqualTo("a");
		iterator.remove();
		assertThatIllegalStateException().isThrownBy(iterator::remove);

		assertThat(set).containsExactlyInAnyOrder("b", "c");
		assertThat(iterator).toIterable().containsExactlyInAnyOrder("b", "c");
	}

	@Test
	void supportsRemoveIfClearAndReuse() {
		SmallSet<Integer> set = new SmallSet<>();
		set.add(1);
		set.add(2);
		set.add(3);
		set.add(4);

		assertThat(set.removeIf(value -> value % 2 == 0)).isTrue();
		assertThat(set).containsExactlyInAnyOrder(1, 3);

		set.clear();
		assertThat(set).isEmpty();
		assertThat(set.add(9)).isTrue();
		assertThat(set).containsExactly(9);
	}
}
