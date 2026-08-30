package jadx.tests.integration.conditions;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestLoopSharedMoveContinuation extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		assertThat(getClassNodeFromSmali())
				.code()
				.countString(3, "i2++")
				.contains("f <= 0.0f || (z && f != 0.0f)")
				.contains("f >= 0.0f || (z && f != 0.0f)")
				.doesNotContain("Method not decompiled")
				.doesNotContain("Code restructure failed")
				.doesNotContain("JADX ERROR");
	}
}
