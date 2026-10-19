package jadx.tests.integration.loops;

import org.junit.jupiter.api.Test;

import jadx.api.CommentsLevel;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestCoroutineProtectedDirectResultJoin extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		getArgs().setCommentsLevel(CommentsLevel.DEBUG);
		assertThat(getClassNodeFromSmali())
				.code()
				.containsOne("Normalize try-protected coroutine suspend completion through state dispatch")
				.containsOne("continue;")
				.contains("access$awaitCtrlMouseScrollOrNull(")
				.contains("throwOnFailure(")
				.doesNotContain("obj.trySend")
				.doesNotContain("might not have been initialized")
				.doesNotContain("Unsupported multi-entry loop pattern")
				.doesNotContain("Method not decompiled")
				.doesNotContain("JADX ERROR");
	}
}
