package jadx.tests.integration.types;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestTypeUpdateLimit extends SmaliTest {

	@Test
	public void test() {
		// Force every non-empty type propagation transaction over the limit.
		getArgs().setTypeUpdatesLimitCount(0);

		assertThat(getClassNodeFromSmali())
				.code()
				.containsOne("return i + 1")
				.doesNotContain("Method not decompiled")
				.doesNotContain("Failed to set immutable type")
				.doesNotContain("Types fix failed")
				.doesNotContain("JADX WARN")
				.doesNotContain("JADX ERROR");
	}
}
