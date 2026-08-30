package jadx.tests.integration.loops;

import org.junit.jupiter.api.Test;

import jadx.api.CommentsLevel;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestCoroutineTypedRestoreCompletion extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		getArgs().setCommentsLevel(CommentsLevel.DEBUG);
		assertThat(getClassNodeFromSmali())
				.code()
				.containsOne("Normalize direct coroutine completion through typed restore")
				.contains("throwOnFailure")
				.doesNotContain("Unsupported multi-entry loop pattern")
				.doesNotContain("??")
				.doesNotContain("Method not decompiled")
				.doesNotContain("JADX ERROR");
	}
}
