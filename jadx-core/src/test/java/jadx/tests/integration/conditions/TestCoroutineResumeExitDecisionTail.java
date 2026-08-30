package jadx.tests.integration.conditions;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestCoroutineResumeExitDecisionTail extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		assertThat(getClassNodeFromSmaliFiles())
				.code()
				.containsOne("while (")
				.contains("isSuccess()")
				.contains("getLastIndex(")
				.contains("return coroutine_suspended;")
				.doesNotContain("Unsupported multi-entry loop")
				.doesNotContain("Region traversal cycle")
				.doesNotContain("Code duplicated")
				.doesNotContain("Method code generation error");
	}
}
