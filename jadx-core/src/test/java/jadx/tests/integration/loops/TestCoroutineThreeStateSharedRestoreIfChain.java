package jadx.tests.integration.loops;

import org.junit.jupiter.api.Test;

import jadx.api.CommentsLevel;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestCoroutineThreeStateSharedRestoreIfChain extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		getArgs().setCommentsLevel(CommentsLevel.DEBUG);

		assertThat(getClassNodeFromSmali())
				.code()
				.contains("emit")
				.contains("receive")
				.contains("while (true)")
				.contains("Normalize 3-state")
				.doesNotContain("Code duplicated in")
				.doesNotContain("Region traversal cycle prevented")
				.doesNotContain("Recursive region processing prevented")
				.doesNotContain("Unsupported multi-entry loop pattern")
				.doesNotContain("Method not decompiled")
				.doesNotContain("JADX WARN")
				.doesNotContain("JADX ERROR")
				.doesNotContain("??");
	}
}
