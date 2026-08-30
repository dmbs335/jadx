package jadx.tests.integration.loops;

import org.junit.jupiter.api.Test;

import jadx.api.CommentsLevel;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestCoroutineObfuscatedTwoStateLocalTryLoop extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		getArgs().setCommentsLevel(CommentsLevel.DEBUG);
		assertThat(getClassNodeFromSmali())
				.code()
				.containsOne(
						"Normalize complete unprotected obfuscated coroutine completions"
								+ " through state dispatch")
				.containsOne("while (true) {")
				.contains("firstSuspend(")
				.contains("secondSuspend(")
				.containsOne("write(obj);")
				.contains("try {")
				.contains("catch (Throwable")
				.doesNotContain("Unsupported multi-entry loop pattern")
				.doesNotContain("Region traversal cycle prevented")
				.doesNotContain("Code duplicated in")
				.doesNotContain("Found unreachable blocks")
				.doesNotContain("Method not decompiled")
				.doesNotContain("JADX ERROR");
	}
}
