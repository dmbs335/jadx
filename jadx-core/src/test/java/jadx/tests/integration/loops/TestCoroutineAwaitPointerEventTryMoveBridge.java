package jadx.tests.integration.loops;

import org.junit.jupiter.api.Test;

import jadx.api.CommentsLevel;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestCoroutineAwaitPointerEventTryMoveBridge extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		allowWarnInCode();
		getArgs().setCommentsLevel(CommentsLevel.DEBUG);
		assertThat(getClassNodeFromSmali())
				.code()
				.containsOne("while (true)")
				.contains("awaitPointerEvent(")
				.countString(4, "cleanup()")
				.doesNotContain("this =")
				.doesNotContain("Unsupported multi-entry loop pattern")
				.doesNotContain("Region traversal cycle prevented")
				.doesNotContain("Method not decompiled")
				.doesNotContain("JADX ERROR");
	}
}
