package jadx.tests.integration.trycatch;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestExceptionHandlerTypeSplit extends SmaliTest {

	@Test
	public void test() {
		assertThat(getClassNodeFromSmali())
				.code()
				.contains("catch (Exception unused2) {")
				.contains("catch (Throwable th2) {")
				.contains("close(fileOutputStream);")
				.contains("throw th2;")
				.doesNotContain("th = th2;")
				.doesNotContain("Type inference failed")
				.doesNotContain("Failed to insert an additional move")
				.doesNotContain("??")
				.doesNotContain("Method not decompiled");
	}
}
