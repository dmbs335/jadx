package jadx.tests.integration.trycatch;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestSequentialExecutorLoop extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		assertThat(getClassNodeFromSmali())
				.code()
				.containsOne("while (true) {")
				.containsOne("Thread.interrupted()")
				.containsOne("catch (RuntimeException e)")
				.contains("Thread.currentThread().interrupt()")
				.doesNotContain("break;\n                    break;")
				.doesNotContain("Method not decompiled")
				.doesNotContain("Code restructure failed")
				.doesNotContain("JADX ERROR");
	}
}
