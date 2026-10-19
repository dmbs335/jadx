package jadx.tests.integration.loops;

import java.util.List;

import org.junit.jupiter.api.Test;

import jadx.api.DecompilationMode;
import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestProtectedCoroutineThreeStateRetry extends SmaliTest {
	@Override
	protected void runChecks(List<ClassNode> classes) {
		org.assertj.core.api.Assertions.assertThat(classes).hasSize(1);
		int expectedErrors = getArgs().getDecompilationMode() == DecompilationMode.SIMPLE ? 0 : 1;
		org.assertj.core.api.Assertions.assertThat(classes.get(0).root().getErrorsCounter().getErrorCount())
				.isEqualTo(expectedErrors);
		org.assertj.core.api.Assertions.assertThat(classes.get(0).root().getErrorsCounter().getWarnsCount()).isZero();
	}

	/**
	 * Minimal three-state retry coroutine: both suspend calls and their resume failure checks
	 * share a catch whose nonterminal branch returns to the retry guard. The handler is reachable
	 * only through the exception splitter in the normal CFG, so no loop is registered. Repeating
	 * the catch without restoring that loop either loses the retry or encloses the unprotected
	 * initial failure check. Keep an explicit error until the full protected state family is restored.
	 * Redirecting both the initial and catch edges through a synthetic label state alone is also
	 * insufficient: region construction can emit a continue outside its loop and lose the input
	 * result variable. Exception-aware structuring must precede source emission.
	 */
	@Test
	public void reportsUnsupportedProtectedRetryScope() {
		disableCompilation();
		getArgs().setShowInconsistentCode(false);
		ClassNode cls = getClassNodeFromSmali();
		assertThat(cls).code()
				.containsOne("Incorrect try/catch scope: unprotected throwing code was included in handler scope")
				.contains("JADX ERROR")
				.contains("Method not decompiled:");
	}

	@Test
	public void simpleModePreservesDirectDelayCompletion() {
		disableCompilation();
		getArgs().setDecompilationMode(DecompilationMode.SIMPLE);
		ClassNode cls = getClassNodeFromSmali();
		assertThat(cls).code()
				.contains("this.call.invoke")
				.containsPattern("if \\(this\\.delay\\.invoke\\(.*?\\) == r0\\) goto L\\d+;\\s+goto L\\d+");
	}
}
