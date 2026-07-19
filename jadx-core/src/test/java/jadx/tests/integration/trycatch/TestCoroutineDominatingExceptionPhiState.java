package jadx.tests.integration.trycatch;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestCoroutineDominatingExceptionPhiState extends SmaliTest {

	@Test
	public void test() {
		assertThat(getClassNodeFromSmali())
				.code()
				.doesNotContain("Type inference failed")
				.doesNotContain("??")
				.doesNotContain("Method not decompiled")
				.contains("close(closeable);");
	}
}
