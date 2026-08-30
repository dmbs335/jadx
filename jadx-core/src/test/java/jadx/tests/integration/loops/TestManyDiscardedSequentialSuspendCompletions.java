package jadx.tests.integration.loops;

import org.junit.jupiter.api.Test;

import jadx.api.CommentsLevel;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestManyDiscardedSequentialSuspendCompletions extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		getArgs().setCommentsLevel(CommentsLevel.DEBUG);

		assertThat(getClassNodeFromSmali())
				.code()
				.countString(
						9,
						"Route discarded sequential suspend completion through state dispatch:")
				.contains("case 10:")
				.doesNotContain("Coroutine region normalization limit reached")
				.doesNotContain("Method not decompiled")
				.doesNotContain("JADX ERROR");
	}
}
