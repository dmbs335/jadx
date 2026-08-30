package jadx.tests.integration.loops;

import org.junit.jupiter.api.Test;

import jadx.api.CommentsLevel;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestTryProtectedDeferredBooleanResumeTail extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		getArgs().setCommentsLevel(CommentsLevel.DEBUG);

		assertThat(getClassNodeFromSmaliFiles())
				.code()
				.contains("Split try-protected Deferred Boolean resume tail before iterator loop")
				.containsOne("while (it.hasNext())")
				.containsOne(".await(")
				.contains(" || ")
				.doesNotContain("JADX WARN")
				.doesNotContain("Method not decompiled")
				.doesNotContain("JADX ERROR");
	}
}
