package jadx.tests.integration.loops;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestCoroutineYieldFrameResumeLoop extends SmaliTest {

	@Test
	public void test() {
		assertThat(getClassNodeFromSmaliFiles("TestCoroutineYieldFrameResumeLoop"))
				.code()
				.contains("getStackTraceElement()")
				.contains("getCallerFrame()")
				.contains("while (")
				.doesNotContain("Unsupported multi-entry loop")
				.doesNotContain("Unsafe code duplication")
				.doesNotContain("Code decompiled incorrectly")
				.doesNotContain("Method not decompiled");
	}
}
