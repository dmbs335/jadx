package jadx.tests.integration.others;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestAllNops extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		assertThat(getClassNodeFromSmali())
				.code()
				.contains("private boolean test() {")
				.contains("private boolean testWithTryCatch() {")
				.contains("return false;")
				.doesNotContain("nop;");
	}
}
