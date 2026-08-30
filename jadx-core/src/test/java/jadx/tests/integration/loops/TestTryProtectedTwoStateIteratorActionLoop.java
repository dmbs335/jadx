package jadx.tests.integration.loops;

import org.junit.jupiter.api.Test;

import jadx.api.CommentsLevel;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestTryProtectedTwoStateIteratorActionLoop extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		getArgs().setCommentsLevel(CommentsLevel.DEBUG);

		assertThat(searchCls(
				loadFromSmaliFiles(),
						"loops.TestTryProtectedTwoStateIteratorActionLoop"))
						.code()
						.containsOne(
								"Normalize try-protected two-state iterator/action completions"
										+ " through state dispatch")
						.containsOne("while (true) {")
						.contains("try {")
						.contains("catch (Throwable")
						.contains("hasNext(")
						.contains("action(")
						.contains("cleanup()")
						.doesNotContain("Unsupported multi-entry loop pattern")
						.doesNotContain("Region traversal cycle prevented")
						.doesNotContain("Recursive region processing prevented")
						.doesNotContain("Code duplicated in")
						.doesNotContain("Method not decompiled")
						.doesNotContain("JADX ERROR");
	}
}
