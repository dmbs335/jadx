package jadx.tests.integration.loops;

import org.junit.jupiter.api.Test;

import jadx.api.CommentsLevel;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestCoroutineDirectArithmeticBridgeJoin extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		getArgs().setCommentsLevel(CommentsLevel.DEBUG);
		assertThat(getClassNodeFromSmali())
				.code()
				.contains("Split coroutine direct move/result tail before resume join")
				.contains("Verified coroutine iterator/array fixed-stride yield loop")
				.countString(2, "while (true)")
				.contains("hasNext()")
				.contains(".next()")
				.countString(2, ".yield(")
				.contains("+= 6")
				.doesNotContain("Unsupported multi-entry loop pattern")
				.doesNotContain("Code duplicated")
				.doesNotContain("Method not decompiled")
				.doesNotContain("JADX ERROR");
	}
}
