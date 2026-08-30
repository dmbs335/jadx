package jadx.core.dex.visitors.usage;

import java.util.Set;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UseSetTest {

	@Test
	void preservesSetSemanticsAcrossPromotion() {
		UseSet<String, Integer> useSet = new UseSet<>();
		for (int i = 0; i < 10; i++) {
			useSet.add("key", i);
		}
		useSet.add("key", 5);

		assertThat(useSet.get("key")).containsExactlyInAnyOrder(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
		assertThat(useSet.getOrDefault("missing", Set.of(42))).containsExactly(42);
	}

	@Test
	void excludesIdentitySelfUsage() {
		Object node = new Object();
		UseSet<Object, Object> useSet = new UseSet<>();

		useSet.add(node, node);

		assertThat(useSet.get(node)).isNull();
	}

	@Test
	void singletonSupportsDuplicateVisitAndNullPromotion() {
		UseSet<String, String> useSet = new UseSet<>();
		useSet.add("single", "value");
		useSet.add("single", "value");
		useSet.add("nullable", null);

		assertThat(useSet.get("single")).containsExactly("value");
		assertThat(useSet.get("nullable")).containsExactly((String) null);

		useSet.add("nullable", "value");
		assertThat(useSet.get("nullable")).containsExactlyInAnyOrder(null, "value");
		assertThat(useSet.getOrDefault("missing", Set.of("default"))).containsExactly("default");

		useSet.visit((key, values) -> assertThat(values).isEqualTo(useSet.get(key)));
	}

	@Test
	void freezesAndReusesSortedMultiValueLists() {
		UseSet<String, Integer> useSet = new UseSet<>();
		useSet.add("key", 3);
		useSet.add("key", 1);
		useSet.add("key", 2);

		assertThat(useSet.getSortedList("key")).containsExactly(1, 2, 3);
		assertThat(useSet.getSortedList("key")).isSameAs(useSet.getSortedList("key"));
		assertThat(useSet.get("key")).containsExactlyInAnyOrder(1, 2, 3);

		useSet.add("key", 0);
		assertThat(useSet.getSortedList("key")).containsExactly(0, 1, 2, 3);
	}
}
