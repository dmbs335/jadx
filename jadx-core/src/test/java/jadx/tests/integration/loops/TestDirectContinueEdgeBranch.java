package jadx.tests.integration.loops;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestDirectContinueEdgeBranch extends SmaliTest {

	@Test
	public void test() {
		assertThat(getClassNodeFromSmali())
				.code()
				.countString(2, "continue;")
				.doesNotContain("if (replacement == null) {\n        }")
				.doesNotContain("Code duplicated")
				.doesNotContain("Code restructure failed");
	}
}
