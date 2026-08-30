package jadx.tests.integration.loops;

import org.junit.jupiter.api.Test;

import jadx.api.CommentsLevel;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestCoroutineIteratorMapPutResumeTail extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		getArgs().setCommentsLevel(CommentsLevel.DEBUG);

		assertThat(getClassNodeFromSmali())
				.code()
				.containsOne("Split single-suspend iterator resume result tail before loop header")
				.containsOne("while (it.hasNext())")
				.contains(".hasNext()")
				.contains(".next()")
				.contains(".put(")
				.contains("insert(")
				.contains("return coroutine_suspended")
				.doesNotContain("Unsupported multi-entry loop pattern")
				.doesNotContain("Region traversal cycle")
				.doesNotContain("Method not decompiled")
				.doesNotContain("??")
				.doesNotContain("JADX WARN")
				.doesNotContain("JADX ERROR");
	}
}
