package jadx.tests.integration.loops;

import org.junit.jupiter.api.Test;

import jadx.api.CommentsLevel;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestTryProtectedChannelIteratorTopSplitter extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		getArgs().setCommentsLevel(CommentsLevel.DEBUG);

		assertThat(getClassNodeFromSmali())
				.code()
				.containsOne("Normalize single coroutine completion through state dispatch")
				.contains("while (true)")
				.contains(".hasNext(")
				.contains(".next()")
				.contains("cancelConsumed(")
				.doesNotContain("Unsupported multi-entry loop pattern")
				.doesNotContain("Region traversal cycle prevented")
				.doesNotContain("Recursive region processing prevented")
				.doesNotContain("Code duplicated")
				.doesNotContain("Code restructure failed")
				.doesNotContain("Method not decompiled")
				.doesNotContain("JADX ERROR");
	}
}
