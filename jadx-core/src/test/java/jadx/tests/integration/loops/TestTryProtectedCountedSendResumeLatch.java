package jadx.tests.integration.loops;

import org.junit.jupiter.api.Test;

import jadx.api.CommentsLevel;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestTryProtectedCountedSendResumeLatch extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		getArgs().setCommentsLevel(CommentsLevel.DEBUG);

		assertThat(getClassNodeFromSmali())
				.code()
				.containsOne("Split try-protected counted send resume latch through PHI pre-header")
				.countString(1, "while (true) {")
				.countString(2, "i = 0;")
				.countString(2, "while (i < length) {")
				.countString(2, "i++;")
				.contains(".send(obj2, this)")
				.doesNotContain("Unsupported multi-entry loop pattern")
				.doesNotContain("Recursive region processing prevented")
				.doesNotContain("Region traversal cycle prevented")
				.doesNotContain("Code restructure failed")
				.doesNotContain("Method not decompiled")
				.doesNotContain("JADX ERROR");
	}
}
