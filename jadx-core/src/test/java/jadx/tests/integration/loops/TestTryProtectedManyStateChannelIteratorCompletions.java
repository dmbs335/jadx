package jadx.tests.integration.loops;

import org.junit.jupiter.api.Test;

import jadx.api.CommentsLevel;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestTryProtectedManyStateChannelIteratorCompletions extends SmaliTest {

	@Test
	public void testSideEffectingDispatchPrefixAndSharedResultJoins() {
		disableCompilation();
		allowWarnInCode();
		getArgs().setCommentsLevel(CommentsLevel.DEBUG);
		assertThat(getClassNodeFromSmali())
				.code()
				.contains("Normalize 4-state try-protected channel iterator completions through label dispatch")
				.contains("while (true)")
				.contains(".hasNext(")
				.contains(".next()")
				.contains(".send(")
				.contains("cancelConsumed(")
				.doesNotContain("Unsupported multi-entry loop pattern")
				.doesNotContain("Recursive region processing prevented")
				.doesNotContain("Region traversal cycle prevented")
				.doesNotContain("Code duplicated in")
				.doesNotContain("Method not decompiled")
				.doesNotContain("Found unreachable blocks")
				.doesNotContain("JADX ERROR");
	}
}
