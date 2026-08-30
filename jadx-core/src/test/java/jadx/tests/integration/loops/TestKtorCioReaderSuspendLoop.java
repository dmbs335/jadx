package jadx.tests.integration.loops;

import org.junit.jupiter.api.Test;

import jadx.api.CommentsLevel;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestKtorCioReaderSuspendLoop extends SmaliTest {

	@Test
	public void testNormalizesIfChainSuspendCompletions() {
		disableCompilation();
		allowWarnInCode();
		getArgs().setCommentsLevel(CommentsLevel.DEBUG);
		assertThat(getClassNodeFromSmaliFiles(
				"io.ktor.network.sockets",
				"TestKtorCioReaderSuspendLoop",
				"CIOReaderKt$attachForReadingImpl$1"))
						.code()
						.containsOne("Normalize Ktor CIO reader 5-state suspend completions through state dispatch")
						.containsOne("while (true)")
						.doesNotContain("Unreachable blocks")
						.doesNotContain("Recursive region processing prevented")
						.doesNotContain("Region traversal cycle prevented")
						.doesNotContain("Unsupported multi-entry loop pattern")
						.doesNotContain("Code duplicated")
						.doesNotContain("Method not decompiled")
						.doesNotContain("JADX ERROR");
	}

	@Test
	public void testNormalizesSwitchSuspendCompletionsAcrossRestorePath() {
		disableCompilation();
		allowWarnInCode();
		getArgs().setCommentsLevel(CommentsLevel.DEBUG);
		assertThat(getClassNodeFromSmaliFiles(
				"io.ktor.network.sockets",
				"TestKtorCioReaderSuspendLoop",
				"CIOReaderKt$attachForReadingDirectImpl$1"))
						.code()
						.containsOne("Normalize Ktor CIO reader 8-state suspend completions through state dispatch")
						.containsOne("while (true)")
						.doesNotContain("Unreachable blocks")
						.doesNotContain("Recursive region processing prevented")
						.doesNotContain("Region traversal cycle prevented")
						.doesNotContain("Unsupported multi-entry loop pattern")
						.doesNotContain("Code duplicated")
						.doesNotContain("Method not decompiled")
						.doesNotContain("JADX ERROR");
	}

	@Test
	public void testSplitsReadRetryResultTailForResumeEntry() {
		disableCompilation();
		allowWarnInCode();
		getArgs().setCommentsLevel(CommentsLevel.DEBUG);
		assertThat(getClassNodeFromSmaliFiles(
				"io.ktor.network.sockets",
				"TestKtorCioReaderRetryResumeTail",
				"CIOReaderKt$attachForReadingImpl$1"))
						.code()
						.containsOne("Split Ktor CIO read retry result tail for resume entry")
						.containsOne("while (true)")
						.doesNotContain("Unsupported multi-entry loop pattern")
						.doesNotContain("Recursive region processing prevented")
						.doesNotContain("Region traversal cycle prevented")
						.doesNotContain("Method not decompiled")
						.doesNotContain("JADX ERROR");
	}

	@Test
	public void testSharesCleanupTailAcrossCoroutineStates() {
		disableCompilation();
		allowWarnInCode();
		getArgs().setCommentsLevel(CommentsLevel.DEBUG);
		assertThat(getClassNodeFromSmaliFiles(
				"io.ktor.network.sockets",
				"TestKtorCioReaderSharedCleanupTail",
				"CIOReaderKt$attachForReadingImpl$1"))
						.code()
						.containsOne(".finish()")
						.containsOne("recordMode(i);")
						.containsOne("recycle();")
						.containsOne("shutdownInput();")
						.doesNotContain("Method not decompiled")
						.doesNotContain("JADX ERROR");
	}

	@Test
	public void testSharesDirectReaderCloseTailAcrossActiveSwitchStates() {
		disableCompilation();
		allowWarnInCode();
		getArgs().setCommentsLevel(CommentsLevel.DEBUG);
		assertThat(getClassNodeFromSmaliFiles(
				"io.ktor.network.sockets",
				"TestKtorCioDirectSharedCloseTail",
				"CIOReaderKt$attachForReadingDirectImpl$1"))
						.code()
						.containsOne("Share Ktor CIO direct-reader close tail")
						.containsOne(".finish();")
						.containsOne(".close();")
						.doesNotContain("Code duplicated")
						.doesNotContain("Method not decompiled")
						.doesNotContain("JADX ERROR");
	}
}
