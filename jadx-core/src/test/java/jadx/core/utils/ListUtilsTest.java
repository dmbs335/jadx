package jadx.core.utils;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ListUtilsTest {

	@Test
	void safeReplaceCopyDoesNotMutateSharedList() {
		List<String> shared = new ArrayList<>(List.of("old", "keep"));

		List<String> replaced = ListUtils.safeReplaceCopy(shared, "old", "new");

		assertThat(shared).containsExactly("old", "keep");
		assertThat(replaced).containsExactly("new", "keep");
	}

	@Test
	void compactListPreservesContentsAndReusesLargerList() {
		List<String> empty = ListUtils.compactList(new ArrayList<>());
		List<String> single = ListUtils.compactList(new ArrayList<>(List.of("a")));
		List<String> pair = ListUtils.compactList(new ArrayList<>(List.of("a", "b")));
		List<String> larger = new ArrayList<>(List.of("a", "b", "c"));

		assertThat(empty).isEmpty();
		assertThat(single).containsExactly("a");
		assertThat(pair).containsExactly("a", "b");
		assertThat(ListUtils.compactList(larger)).isSameAs(larger);
	}

	@Test
	void safeMutationCopiesCompactList() {
		List<String> compact = ListUtils.compactList(new ArrayList<>(List.of("a", "b")));

		List<String> added = ListUtils.safeAdd(compact, "c");
		List<String> removed = ListUtils.safeRemoveAndTrim(compact, "a");

		assertThat(compact).containsExactly("a", "b");
		assertThat(added).containsExactly("a", "b", "c");
		assertThat(removed).containsExactly("b");
	}

	@Test
	void distinctMergeResultIsMutableWhenOneSideIsEmpty() {
		List<String> compact = ListUtils.compactList(new ArrayList<>(List.of("a")));

		List<String> firstEmpty = ListUtils.distinctMergeSortedLists(List.of(), compact);
		List<String> secondEmpty = ListUtils.distinctMergeSortedLists(compact, List.of());
		firstEmpty.remove("a");
		secondEmpty.add("b");

		assertThat(firstEmpty).isEmpty();
		assertThat(secondEmpty).containsExactly("a", "b");
	}
}
