package jadx.tests.integration.loops;

import org.junit.jupiter.api.Test;

import jadx.api.CommentsLevel;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestCoroutineUnconditionalYieldReferenceAdvance extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		getArgs().setCommentsLevel(CommentsLevel.DEBUG);
		assertThat(getClassNodeFromSmaliFiles("TestCoroutineUnconditionalYieldReferenceAdvance"))
				.code()
				.containsOne("Split unconditional sequence-yield reference advance before loop")
				.contains("while (")
				.countString(2, "getCallerFrame()")
				.doesNotContain("Unsupported multi-entry loop")
				.doesNotContain("Region traversal cycle")
				.doesNotContain("Recursive region processing")
				.doesNotContain("Code duplicated")
				.doesNotContain("Method not decompiled")
				.doesNotContain("JADX ERROR");
	}
}
