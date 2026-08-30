package jadx.tests.integration.loops;

import org.junit.jupiter.api.Test;

import jadx.api.CommentsLevel;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestJsonTreeReaderDeepRecursiveObjectLoop extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		allowWarnInCode();
		getArgs().setRunDebugChecks(false);
		getArgs().setCommentsLevel(CommentsLevel.DEBUG);
		assertThat(getClassNodeFromSmaliWithClsName(
				"loops.DeepRecursiveObjectReader"))
						.code()
						.containsOne("Normalize single-state direct completion bridge through state dispatch")
						.containsOne("while (true)")
						.contains("callRecursive")
						.contains("result = objCallRecursive")
						.contains("linkedHashMap.put")
						.contains("canConsumeValue")
						.doesNotContain("Region traversal cycle prevented")
						.doesNotContain("Recursive region processing prevented")
						.doesNotContain("Unsupported multi-entry loop pattern")
						.doesNotContain("Method not decompiled")
						.doesNotContain("JADX ERROR")
						.doesNotContain("??");
	}
}
