package jadx.tests.integration.loops;

import org.junit.jupiter.api.Test;

import jadx.api.CommentsLevel;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestSharedArithmeticLoopReset extends SmaliTest {

	@Test
	public void test() {
		getArgs().setCommentsLevel(CommentsLevel.DEBUG);
		assertThat(getClassNodeFromSmali())
				.code()
				.contains("Split shared loop entry/reset block for 2 loop branches")
				.contains("while (")
				.contains("continue;")
				.countString(1, "i++")
				.doesNotContain("Unsupported multi-entry loop pattern")
				.doesNotContain("Recursive region processing prevented")
				.doesNotContain("Region traversal cycle prevented")
				.doesNotContain("Method not decompiled")
				.doesNotContain("JADX ERROR");
	}
}
