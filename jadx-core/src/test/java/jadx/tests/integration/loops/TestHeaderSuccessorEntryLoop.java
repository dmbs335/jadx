package jadx.tests.integration.loops;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestHeaderSuccessorEntryLoop extends SmaliTest {

	@Test
	public void test() {
		assertThat(getClassNodeFromSmali())
				.code()
				.doesNotContain("Method not decompiled")
				.doesNotContain("Unsupported multi-entry loop pattern")
				.doesNotContain("Code restructure failed")
				.doesNotContain("JADX ERROR");
	}
}
