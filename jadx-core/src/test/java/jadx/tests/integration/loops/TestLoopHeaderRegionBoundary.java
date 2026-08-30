package jadx.tests.integration.loops;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestLoopHeaderRegionBoundary extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		assertThat(getClassNodeFromSmali())
				.code()
				.containsOne("while (")
				.doesNotContain("Recursive region processing prevented")
				.doesNotContain("while (false) {")
				.doesNotContain("while (true) {\n            }");
	}
}
