package jadx.tests.integration.inline;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestTernaryInlineSkip extends SmaliTest {

	@Test
	public void test() {
		assertThat(getClassNodeFromSmali())
				.code()
				.containsOne("return wrapper(i) + wrapper(i);")
				.doesNotContain("Failed to inline method")
				.doesNotContain("Failed to replace arg");
	}
}
