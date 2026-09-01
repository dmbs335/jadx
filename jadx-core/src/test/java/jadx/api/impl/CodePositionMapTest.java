package jadx.api.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

class CodePositionMapTest {
	@Test
	void keepsKeysSortedAndReplacesValues() {
		CodePositionMap<String> map = new CodePositionMap<>();
		map.putValue(10, "ten");
		map.putValue(30, "old");
		map.putValue(20, "twenty");
		map.putValue(30, "thirty");
		map.putValue(5, "five");

		assertThat(map).hasSize(4);
		assertThat(map.get(20)).isEqualTo("twenty");
		assertThat(map.get(99)).isNull();
		assertThat(map.containsKey(30)).isTrue();
		assertThat(map.entrySet())
				.extracting(Map.Entry::getKey, Map.Entry::getValue)
				.containsExactly(
						tuple(5, "five"),
						tuple(10, "ten"),
						tuple(20, "twenty"),
						tuple(30, "thirty"));
	}

	@Test
	void mergesWithPositionShift() {
		CodePositionMap<String> first = new CodePositionMap<>();
		first.putValue(1, "prefix");
		first.putValue(12, "replaced");
		CodePositionMap<String> second = new CodePositionMap<>();
		second.putValue(2, "body-start");
		second.putValue(8, "body-end");

		first.putAllShifted(second, 10);

		List<String> entries = new ArrayList<>();
		first.forEach((key, value) -> entries.add(key + "=" + value));
		assertThat(entries).containsExactly("1=prefix", "12=body-start", "18=body-end");
	}

	@Test
	void copiesUnorderedMapAndSupportsDirectionalIndexes() {
		Map<Integer, String> source = new HashMap<>();
		source.put(30, "thirty");
		source.put(5, "five");
		source.put(20, "twenty");
		source.put(10, "ten");

		CodePositionMap<String> map = CodePositionMap.copyOf(source);

		assertThat(map.entrySet())
				.extracting(Map.Entry::getKey, Map.Entry::getValue)
				.containsExactly(
						tuple(5, "five"),
						tuple(10, "ten"),
						tuple(20, "twenty"),
						tuple(30, "thirty"));
		assertThat(map.floorIndex(20)).isEqualTo(2);
		assertThat(map.floorIndex(19)).isEqualTo(1);
		assertThat(map.lowerIndex(20)).isEqualTo(1);
		assertThat(map.ceilingIndex(21)).isEqualTo(3);
	}

	@Test
	void removesEntriesThroughValuesView() {
		CodePositionMap<String> map = new CodePositionMap<>();
		map.putValue(5, "keep");
		map.putValue(10, "remove");
		map.putValue(20, "keep-too");

		map.values().removeIf("remove"::equals);

		assertThat(map.entrySet())
				.extracting(Map.Entry::getKey, Map.Entry::getValue)
				.containsExactly(tuple(5, "keep"), tuple(20, "keep-too"));
	}

}
