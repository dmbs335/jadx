package jadx.tests.integration.loops;

import org.junit.jupiter.api.Test;

import jadx.api.CommentsLevel;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestProtectedCoroutineMultiSuspendDispatch extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		allowWarnInCode();
		getArgs().setCommentsLevel(CommentsLevel.DEBUG);
		assertThat(getClassNodeFromSmali())
				.code()
				.contains("Normalize coroutine delay result before preserved restore bridge")
				.contains("Normalize protected multi-suspend loop completion through state dispatch")
				.contains("while (")
				.contains("delay(1L, this)")
				.contains("suspendClock(this)")
				.contains("send(this)")
				.doesNotContain("Unsupported multi-entry loop pattern")
				.doesNotContain("Found unreachable blocks")
				.doesNotContain("Failed to insert an additional move for type inference")
				.doesNotContain("Method not decompiled")
				.doesNotContain("JADX ERROR");
	}
}
