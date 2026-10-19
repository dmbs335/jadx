package jadx.tests.integration.loops;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestProtectedGestureResumeLoop extends SmaliTest {

	// State 1 enters the protected await tail directly. The generated loop must keep its
	// catch and continue the source loop, while switch cases legitimately use break.
	@Test
	public void test() {
		// The Compose and kotlinx.coroutines classes referenced by this fixture are not on the
		// isolated Java compiler classpath; the companion execution fixture checks semantics.
		disableCompilation();
		allowWarnInCode();
		String code = getClassNodeFromSmali().getCode().getCodeStr();
		assertThat(code)
				.contains("awaitAllPointersUp", "while (JobKt.isActive", "continue;")
				.doesNotContain("JADX WARN")
				.doesNotContain("non-void coroutine has a fall-through path")
				.doesNotContain("Code decompiled incorrectly")
				.doesNotContain("Method not decompiled");
		assertTrue(code.indexOf("while (JobKt.isActive") == code.lastIndexOf("while (JobKt.isActive"),
				"The protected loop was duplicated across the catch path");
		String loopCode = code.substring(code.indexOf("while (JobKt.isActive"));
		assertThat(loopCode)
				.contains("catch (CancellationException", "continue;")
				.doesNotContain("break;");
	}
}
