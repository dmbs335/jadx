package jadx.tests.integration.trycatch;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestBranchHandlerFinallyReturn extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		assertThat(getClassNodeFromSmali())
				.code()
				.containsOne("cleanup();")
				.contains("catch (UndeclaredThrowableException")
				.contains("if (consumer != null)")
				.doesNotContain("missing block")
				.doesNotContain("Method not decompiled")
				.doesNotContain("JADX ERROR");
	}
}
