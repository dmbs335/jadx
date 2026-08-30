package jadx.tests.integration.loops;

import org.junit.jupiter.api.Test;

import jadx.api.CommentsLevel;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestCoroutineTwoStateIteratorActionLoop extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		getArgs().setCommentsLevel(CommentsLevel.DEBUG);
		assertThat(getClassNodeFromSmali())
				.code()
				.containsOne("Normalize two-state iterator/action completions through state dispatch")
				.containsOne("while (true)")
				.contains("hasNext(this)")
				.contains("animate(")
				.doesNotContain("Unsupported multi-entry loop pattern")
				.doesNotContain("Region traversal cycle prevented")
				.doesNotContain("Recursive region processing prevented")
				.doesNotContain("Code duplicated in")
				.doesNotContain("Type inference failed")
				.doesNotContain("Failed to insert additional move")
				.doesNotContain("??")
				.doesNotContain("Method not decompiled")
				.doesNotContain("JADX ERROR");
	}
}
