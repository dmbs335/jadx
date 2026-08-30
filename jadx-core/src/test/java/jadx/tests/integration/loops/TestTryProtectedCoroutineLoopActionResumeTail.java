package jadx.tests.integration.loops;

import org.junit.jupiter.api.Test;

import jadx.api.CommentsLevel;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestTryProtectedCoroutineLoopActionResumeTail extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		getArgs().setCommentsLevel(CommentsLevel.DEBUG);

		assertThat(searchCls(loadFromSmaliFiles(),
				"jadx.tests.integration.loops.TestTryProtectedCoroutineLoopActionResumeTail"))
						.code()
						.containsOne(
								"Split try-protected coroutine loop-action resume tail before loop")
						.contains("while (")
						.contains("awaitStep(")
						.contains("touch(")
						.contains("return coroutine_suspended")
						.doesNotContain("Unsupported multi-entry loop pattern")
						.doesNotContain("Method not decompiled")
						.doesNotContain("JADX ERROR");
	}
}
