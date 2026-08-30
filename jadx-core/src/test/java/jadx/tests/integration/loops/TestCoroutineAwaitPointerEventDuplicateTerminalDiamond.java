package jadx.tests.integration.loops;

import org.junit.jupiter.api.Test;

import jadx.api.CommentsLevel;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestCoroutineAwaitPointerEventDuplicateTerminalDiamond extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		getArgs().setCommentsLevel(CommentsLevel.DEBUG);
		assertThat(getClassNodeFromSmali())
				.code()
				.contains("awaitPointerEvent")
				.countString(2, "setTouchDown(false)")
				.countString(2, "onInteracting(false)")
				.countString(2, "setStarted(false)")
				.countString(3, "onStop()")
				.countString(3, "warning()")
				.doesNotContain("Code duplicated in")
				.doesNotContain("Unsupported multi-entry loop pattern")
				.doesNotContain("Method not decompiled")
				.doesNotContain("??");
	}
}
