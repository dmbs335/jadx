package jadx.tests.integration.loops;

import org.junit.jupiter.api.Test;

import jadx.api.CommentsLevel;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestCoroutineCancellableChannelEventLoop extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		getArgs().setCommentsLevel(CommentsLevel.DEBUG);

		assertThat(searchCls(
				loadFromSmaliFiles(),
				"androidx.compose.foundation.gestures.TransformableNode$eventLoop$1"))
						.code()
						.containsOne("Normalize two-state cancellable channel event-loop completions")
						.containsOne("while (true)")
						.contains(".receive(this)")
						.contains("transform(")
						.contains("catch (CancellationException")
						.contains("return coroutine_suspended")
						.doesNotContain("Unsupported multi-entry loop pattern")
						.doesNotContain("Region traversal cycle")
						.doesNotContain("Method not decompiled")
						.doesNotContain("JADX WARN")
						.doesNotContain("JADX ERROR");
	}
}
