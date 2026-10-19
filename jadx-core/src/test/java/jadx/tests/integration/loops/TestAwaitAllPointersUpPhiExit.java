package jadx.tests.integration.loops;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestAwaitAllPointersUpPhiExit extends SmaliTest {

	@Test
	public void test() {
		disableCompilation(); // The coroutine's framework types are outside this isolated CFG fixture.
		allowWarnInCode();
		assertThat(getClassNodeFromSmali())
				.code()
				.contains("z = true;\n                                break;")
				.contains("z = false;\n                            break;")
				.doesNotContain("Method not decompiled")
				.doesNotContain("JADX ERROR");
	}
}
