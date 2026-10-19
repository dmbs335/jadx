package jadx.tests.integration.loops;

import org.junit.jupiter.api.Test;

import jadx.api.CommentsLevel;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestTryProtectedSingleIteratorPredicateCompletion extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		getArgs().setCommentsLevel(CommentsLevel.DEBUG);
		assertThat(getClassNodeFromSmali())
				.code()
				.contains("Normalize try-protected single iterator predicate completion"
						+ " through state dispatch")
				.containsOne("do {")
				.containsOne("while (obj != coroutine_suspended);")
				.contains("consume(")
				.doesNotContain("Unsupported multi-entry loop pattern")
				.doesNotContain("Region traversal cycle prevented")
				.doesNotContain("Recursive region processing prevented")
				.doesNotContain("Method not decompiled")
				.doesNotContain("JADX ERROR")
				.doesNotContain("??");
	}
}
