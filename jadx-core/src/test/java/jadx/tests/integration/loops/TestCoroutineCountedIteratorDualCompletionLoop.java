package jadx.tests.integration.loops;

import org.junit.jupiter.api.Test;

import jadx.api.CommentsLevel;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestCoroutineCountedIteratorDualCompletionLoop extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		getArgs().setCommentsLevel(CommentsLevel.DEBUG);
		assertThat(getClassNodeFromSmali())
				.code()
				.doesNotContain("Unsupported multi-entry loop pattern")
				.doesNotContain("Region traversal cycle prevented")
				.doesNotContain("Recursive region processing prevented")
				.doesNotContain("Code duplicated in")
				.doesNotContain("Split coroutine first-resume two-suspend result tail")
				.contains("Normalize counted iterator states")
				.containsOne("while (true) {")
				.contains("hasNext(continuation)")
				.contains(" - 1;")
				.contains("send(");
	}
}
