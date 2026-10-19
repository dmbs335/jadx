package jadx.tests.integration.loops;

import org.junit.jupiter.api.Test;

import jadx.api.CommentsLevel;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestCoroutinePostSuspendLoopCondition extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		getArgs().setCommentsLevel(CommentsLevel.DEBUG);
		assertThat(getClassNodeFromSmaliFiles("TestCoroutinePostSuspendLoopCondition"))
				.code()
				.containsOne("Split coroutine resume post-suspend loop condition tail before body")
				.contains("while (true)")
				.doesNotContain("Unsupported multi-entry loop")
				.doesNotContain("Region traversal cycle")
				.doesNotContain("Recursive region processing")
				.doesNotContain("Method not decompiled")
				.doesNotContain("JADX ERROR");
	}
}
