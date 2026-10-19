package jadx.tests.integration.loops;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestProtectedResumeSingleLatch extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		assertThat(getClassNodeFromSmali())
				.code()
				.countString(4, "i++;")
				.countString(2, "protectedProbe();")
				.countString(2, "if (suspend(obj) == obj2)")
				.doesNotContain("continue;")
				.doesNotContain("Code restructure failed: missing block")
				.doesNotContain("Code decompiled incorrectly")
				.doesNotContain("Method not decompiled");
	}
}
