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
}
