package jadx.tests.integration.loops;

import org.junit.jupiter.api.Test;

import jadx.api.CommentsLevel;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestCoroutineIteratorSuspendTryLoop extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		allowWarnInCode();
		getArgs().setCommentsLevel(CommentsLevel.DEBUG);

		assertThat(getClassNodeFromSmaliFiles())
				.code()
				.contains("while (true) {")
				.contains("hasNext()")
				.contains("await(")
				// The minimized resume join still has one separate traversal cycle.
				.containsOne("Region traversal cycle prevented at block")
				.doesNotContain("Method not decompiled")
				.doesNotContain("JADX ERROR");
	}
}
