package jadx.tests.integration.loops;

import org.junit.jupiter.api.Test;

import jadx.api.CommentsLevel;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestObfuscatedThreeStateIteratorTransformAction extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		getArgs().setCommentsLevel(CommentsLevel.DEBUG);
		// Shape/coverage regression only: external Kotlin dependencies are not compiled here.
		assertThat(getClassNodeFromSmali())
				.code()
				.doesNotContain("Incomplete try/catch coverage")
				.countString(4, "catch (Throwable ")
				.contains("Normalize three-state iterator transform/action completions"
						+ " through state dispatch")
				.containsOne("while (true) {")
				.contains(".a(")
				.contains(".M(")
				.contains(".F(")
				.doesNotContain("Unsupported multi-entry loop pattern")
				.doesNotContain("Region traversal cycle prevented")
				.doesNotContain("Recursive region processing prevented")
				.doesNotContain("Method not decompiled")
				.doesNotContain("JADX ERROR")
				.doesNotContain("??");
	}
}
