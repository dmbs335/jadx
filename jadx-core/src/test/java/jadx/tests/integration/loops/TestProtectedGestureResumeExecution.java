package jadx.tests.integration.loops;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestProtectedGestureResumeExecution extends SmaliTest {

	// Resume states enter two bounded protected tails below the source loop header. The second
	// block() call throws: the loop catch must await once and continue, while switch-case catches
	// must retain their own exits even though they share an exception-handler descriptor.
	@Test
	public void test() {
		// check() exercises all four resume labels. The second block() call throws,
		// and the catch must await once before continuing to the third iteration.
		forceDecompiledCheck();
		String code = getClassNodeFromSmali().getCode().getCodeStr();
		assertThat(code)
				.contains("while (")
				.contains("catch (CancellationException")
				.contains("continue;")
				.doesNotContain("JADX WARN", "Method not decompiled");
		assertThat(code.substring(code.indexOf("while (")))
				.contains("catch (CancellationException", "continue;")
				.doesNotContain("break;");
	}
}
