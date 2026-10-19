package jadx.tests.integration.loops;

import org.junit.jupiter.api.Test;

import jadx.api.CommentsLevel;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestCoroutineInjectedRetryDelayResultJoin extends SmaliTest {

	@Test
	public void test() {
		forceDecompiledCheck();
		getArgs().setCommentsLevel(CommentsLevel.DEBUG);
		assertThat(getClassNodeFromSmali())
				.code()
				.containsOne("while (true)")
				.containsOne("this.delay.invoke(1L, this)")
				.doesNotContain("Code duplicated in")
				.doesNotContain("Region traversal cycle prevented")
				.doesNotContain("Unsupported multi-entry loop pattern")
				.doesNotContain("Method not decompiled")
				.doesNotContain("JADX ERROR");
	}
}
