package jadx.tests.integration.trycatch;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestExceptionHandlerStableLiteral extends SmaliTest {

	@Test
	public void test() {
		assertThat(getClassNodeFromSmali())
				.code()
				.doesNotContain("Not initialized variable")
				.doesNotContain("Incomplete exception PHI")
				.doesNotContain("Method not decompiled")
				.contains("Object obj = null;")
				.contains("return obj;");
	}
}
