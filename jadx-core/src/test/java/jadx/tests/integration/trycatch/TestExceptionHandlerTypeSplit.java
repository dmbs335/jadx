package jadx.tests.integration.trycatch;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestExceptionHandlerTypeSplit extends SmaliTest {

	@Test
	public void test() {
		assertThat(getClassNodeFromSmali())
				.code()
				.contains("FileOutputStream fileOutputStream = null;")
				.contains("catch (Exception unused) {")
				.contains("} finally {")
				.contains("close(fileOutputStream);")
				.doesNotContain("catch (Throwable")
				.doesNotContain("throw th")
				.doesNotContain("th = th2;")
				.doesNotContain("Type inference failed")
				.doesNotContain("Failed to insert an additional move")
				.doesNotContain("??")
				.doesNotContain("Method not decompiled");
	}
}
