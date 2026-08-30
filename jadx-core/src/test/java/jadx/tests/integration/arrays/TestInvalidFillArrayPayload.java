package jadx.tests.integration.arrays;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestInvalidFillArrayPayload extends SmaliTest {

	@Test
	public void test() {
		allowWarnInCode();
		assertThat(getClassNodeFromSmali())
				.code()
				.contains("{1, 2, 3}")
				.contains("Repaired invalid fill-array reference")
				.doesNotContain("Method not decompiled")
				.doesNotContain("JADX ERROR");
	}
}
