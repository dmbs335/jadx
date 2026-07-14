package jadx.tests.integration.trycatch;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestExceptionHandlerPhi extends SmaliTest {

	@Test
	public void test() {
		assertThat(getClassNodeFromSmali())
				.code()
				.doesNotContain("Not initialized variable")
				.doesNotContain("Incomplete exception PHI")
				.contains("long j = 0;")
				.contains("this.J$0 =")
				.doesNotContain("Method not decompiled");
	}
}
