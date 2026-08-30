package jadx.tests.integration.trycatch;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestCropImageSaveOutputExact extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		assertThat(getClassNodeFromSmali())
				.code()
				.contains("store image fail, continue anyway")
				.contains("catch (Exception")
				.doesNotContain("missing block")
				.doesNotContain("Method not decompiled")
				.doesNotContain("JADX ERROR");
	}
}
