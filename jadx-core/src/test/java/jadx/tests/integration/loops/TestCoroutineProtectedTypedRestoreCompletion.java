package jadx.tests.integration.loops;

import org.junit.jupiter.api.Test;

import jadx.api.CommentsLevel;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestCoroutineProtectedTypedRestoreCompletion extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		getArgs().setCommentsLevel(CommentsLevel.DEBUG);
		assertThat(searchCls(loadFromSmaliFiles(),
				"loops.TestCoroutineProtectedTypedRestoreCompletion"))
						.code()
						.containsOne("Normalize protected direct coroutine completion through typed restore")
						.contains("throwOnFailure")
						.countString(3, "if (next instanceof Frame)")
						.countString(3, "if (!(next instanceof WebSocketWriter.FlushRequest))")
						.doesNotContainPattern(
								"if \\(next instanceof Frame\\) \\{\\s+if \\(!\\(next instanceof WebSocketWriter\\.FlushRequest\\)\\)")
						.doesNotContain("Unsupported multi-entry loop pattern")
						.doesNotContain("??")
						.doesNotContain("Method not decompiled")
						.doesNotContain("JADX ERROR");
	}
}
