package jadx.tests.integration.loops;

import org.junit.jupiter.api.Test;

import jadx.api.CommentsLevel;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestCoroutineShortCircuitProjectionJoin extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		allowWarnInCode();
		getArgs().setCommentsLevel(CommentsLevel.DEBUG);
		assertThat(getClassNodeFromSmali())
				.code()
				.containsOne("Split 3 coroutine short-circuit branches from projection join")
				.contains("first() || second() || third()")
				.containsOne("Unsupported multi-entry loop pattern")
				.doesNotContain("??")
				.doesNotContain("Method not decompiled")
				.doesNotContain("JADX ERROR");
	}
}
