package jadx.tests.integration.loops;

import org.junit.jupiter.api.Test;

import jadx.api.CommentsLevel;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestCoroutineTwoStateAwaitPointerEventBackEdge extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		getArgs().setCommentsLevel(CommentsLevel.DEBUG);
		assertThat(getClassNodeFromSmali())
				.code()
				.contains("Normalize direct coroutine completion through typed restore")
				.contains("firstDown")
				.contains("awaitPointerEvent")
				.contains("while (true)")
				.doesNotContain("Code duplicated in")
				.contains("Region traversal cycle prevented")
				.contains("Recursive region processing prevented")
				.doesNotContain("Unsupported multi-entry loop pattern")
				.doesNotContain("Method not decompiled")
				.doesNotContain("??");
	}
}
