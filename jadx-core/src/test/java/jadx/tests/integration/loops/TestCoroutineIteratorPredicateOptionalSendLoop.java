package jadx.tests.integration.loops;

import org.junit.jupiter.api.Test;

import jadx.api.CommentsLevel;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestCoroutineIteratorPredicateOptionalSendLoop extends SmaliTest {

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
				.contains("Normalize counted iterator states [3, 2]")
				.containsOne("while (true) {")
				.contains("obj = hasNext(obj3, continuation)")
				.contains("obj = invoke(i, ")
				.contains("obj = send(obj2, ");
	}
}
