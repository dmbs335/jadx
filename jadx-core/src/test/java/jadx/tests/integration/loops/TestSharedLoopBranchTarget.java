package jadx.tests.integration.loops;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestSharedLoopBranchTarget extends SmaliTest {

	@Test
	public void test() {
		assertThat(getClassNodeFromSmali())
				.code()
				.contains("list.add(\"\");")
				.contains("continue;")
				.doesNotContain("Unsupported multi-entry loop pattern")
				.doesNotContain("Recursive region processing prevented")
				.doesNotContain("Region traversal cycle prevented")
				.doesNotContain("Method not decompiled")
				.doesNotContain("JADX ERROR");
	}
}
