package jadx.tests.integration.loops;

import org.junit.jupiter.api.Test;

import jadx.api.CommentsLevel;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestCoroutineIndexedIteratorSendLoop extends SmaliTest {

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
				.contains("Normalize counted iterator states [2]")
				.containsOne("do {")
				.contains("obj = hasNext(obj3, continuation)")
				.contains("this.I$0 = i + 1")
				.contains("obj = send(obj2, next, continuation)")
				.containsOne("} while (obj != coroutine_suspended);");
	}
}
