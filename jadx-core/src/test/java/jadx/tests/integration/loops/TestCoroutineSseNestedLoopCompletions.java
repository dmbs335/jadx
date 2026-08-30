package jadx.tests.integration.loops;

import org.junit.jupiter.api.Test;

import jadx.api.CommentsLevel;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestCoroutineSseNestedLoopCompletions extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		allowWarnInCode();
		getArgs().setCommentsLevel(CommentsLevel.DEBUG);
		assertThat(searchCls(loadFromSmaliFiles(),
				"io.ktor.client.plugins.sse.DefaultClientSSESession$_incoming$1"))
						.code()
						.containsOne("state SSE nested-loop completions through state dispatch")
						.containsOne("Route SSE close reentry through reconnect state dispatch")
						.contains("while (true)")
						.doesNotContain("Unsupported multi-entry loop")
						.doesNotContain("Code duplicated")
						.doesNotContain("Recursive region processing prevented")
						.doesNotContain("Method not decompiled")
						.doesNotContain("JADX ERROR");
	}
}
