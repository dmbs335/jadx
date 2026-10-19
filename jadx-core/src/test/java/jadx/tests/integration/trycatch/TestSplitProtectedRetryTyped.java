package jadx.tests.integration.trycatch;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestSplitProtectedRetryTyped extends SmaliTest {
	@Test
	public void test() {
		forceDecompiledCheck();
		getArgs().setShowInconsistentCode(false);
		assertThat(getClassNodeFromSmali()).code()
				.contains("Normalize protected retry entries through a common dispatch")
				.contains("catch (RuntimeException")
				.doesNotContain("JADX WARN:")
				.doesNotContain("Method not decompiled:");
	}

	@Test
	public void testWithoutDebugInfo() {
		noDebugInfo();
		test();
	}
}
