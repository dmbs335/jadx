package jadx.tests.integration.loops;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestCompleteSmallIfChainCoroutineCompletions extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		assertThat(getClassNodeFromSmali())
				.code()
				.contains("Normalize object-register states")
				.contains("complete 2-state if-chain coroutine")
				.countString(1, "while (true)")
				.doesNotContain("Unsupported multi-entry loop pattern")
				.doesNotContain("Region traversal cycle prevented")
				.doesNotContain("Recursive region processing prevented")
				.doesNotContain("Method not decompiled")
				.doesNotContain("JADX WARN")
				.doesNotContain("JADX ERROR");
	}
}
