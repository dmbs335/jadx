package jadx.tests.integration.loops;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestCoroutineResumeLatch extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		allowWarnInCode();
		assertThat(getClassNodeFromSmali())
				.code()
				.doesNotContain("Recursive region processing prevented")
				.doesNotContain("Unsupported multi-entry loop pattern")
				.doesNotContain("Method not decompiled")
				.doesNotContain("JADX ERROR");
	}
}
