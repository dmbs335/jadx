package jadx.tests.integration.loops;

import org.junit.jupiter.api.Test;

import jadx.api.CommentsLevel;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestCoroutineThreeStateSelectorProcess extends SmaliTest {

	@Test
	public void testSideEffectingIfChainDispatch() {
		disableCompilation();
		allowWarnInCode();
		getArgs().setCommentsLevel(CommentsLevel.DEBUG);
		assertThat(getClassNodeFromSmaliFiles())
				.code()
				.contains("Normalize three-state selector process completions through label if-chain")
				.contains("while (true)")
				.contains("selectedKeys()")
				.contains("removeFirstOrNull()")
				.contains("YieldKt.yield(")
				.doesNotContain("Unsupported multi-entry loop pattern")
				.doesNotContain("Recursive region processing prevented")
				.doesNotContain("Region traversal cycle prevented")
				.doesNotContain("Code duplicated in")
				.doesNotContain("Method not decompiled")
				.doesNotContain("JADX ERROR");
	}
}
