package jadx.tests.integration.loops;

import org.junit.jupiter.api.Test;

import jadx.api.CommentsLevel;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestCoroutineMergedTwoStateTransformLoop extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		allowWarnInCode();
		getArgs().setCommentsLevel(CommentsLevel.DEBUG);

		assertThat(getClassNodeFromSmali())
				.code()
				.containsOne("Normalize merged suspend states [2, 3] with preceding state 1 through state dispatch")
				.containsOne("while (true)")
				.contains("receive(this)")
				.contains("transformCopy(")
				.contains("transformDirect(")
				.doesNotContain("Code duplicated")
				.doesNotContain("Region traversal cycle prevented")
				.doesNotContain("Unsupported multi-entry loop pattern")
				.doesNotContain("Method not decompiled")
				.doesNotContain("JADX WARN")
				.doesNotContain("JADX ERROR");
	}
}
