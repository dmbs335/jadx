package jadx.tests.integration.loops;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestRegionTraversalCycle extends SmaliTest {

	@Test
	public void test() {
		allowWarnInCode();
		assertThat(getClassNodeFromSmali())
				.code()
				.containsOne("while (true)")
				.doesNotContain("Region traversal cycle prevented")
				.doesNotContain("Unsupported multi-entry loop pattern")
				.doesNotContain("Method not decompiled")
				.doesNotContain("JADX ERROR");
	}
}
