package jadx.tests.integration.loops;

import java.util.List;

import org.junit.jupiter.api.Test;

import jadx.api.CommentsLevel;
import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestCoroutineDirectBridgeTypedRestore extends SmaliTest {
	private static final String SCOPE_WARNING =
			"Incorrect try/catch scope: unprotected throwing code was included in handler scope";

	@Override
	protected void runChecks(List<ClassNode> classes) {
		// Diagnostic regression, not a successful round trip. The original initial
		// throwOnFailure and restored-value casts are outside the catch ranges, but
		// the normalized multi-entry region still encloses them in the retry catch.
		// The inconsistent output also loses the direct execute suspension check
		// and the catch-to-retry loop, so narrowing the catch alone is insufficient.
		// CFG inspection after region building found zero registered loops: the real
		// increment at 0x1a0 returns to the retry guard at 0x55, but the handler is
		// reachable only via its synthetic top-splitter edge, not explicit edges from
		// protected calls. DFS therefore classifies 0x1a0 -> 0x55 as a cross edge,
		// while the guard cannot dominate the handler's increment path.
		// The generic suspend-completion matcher finds states 1, 2 and 3. Redirecting
		// only state 2 through either the label dispatcher or its typed resume block
		// still misplaces the catch and loses the retry. The three protected states
		// and catch-to-guard edge need one exception-aware normalization. Until then,
		// the scope proof must reject source output even with show-bad-code enabled.
		org.assertj.core.api.Assertions.assertThat(classes).hasSize(1);
		org.assertj.core.api.Assertions.assertThat(classes.get(0).root().getErrorsCounter().getErrorCount()).isEqualTo(1);
		org.assertj.core.api.Assertions.assertThat(classes.get(0).root().getErrorsCounter().getWarnsCount()).isZero();
	}

	@Test
	public void reportsUnsupportedScopeByDefault() {
		getArgs().setShowInconsistentCode(false);
		assertThat(searchCls(loadFromSmaliFiles(),
				"io.ktor.client.plugins.sse.DefaultClientSSESession$doReconnection$2"))
						.code()
						.containsOne(SCOPE_WARNING)
						.contains("JADX ERROR")
						.contains("Method not decompiled:");
	}

	@Test
	public void rejectsBadCodeDisplay() {
		disableCompilation();
		getArgs().setShowInconsistentCode(true);
		getArgs().setCommentsLevel(CommentsLevel.DEBUG);
		assertThat(searchCls(
				loadFromSmaliFiles(),
				"io.ktor.client.plugins.sse.DefaultClientSSESession$doReconnection$2"))
						.code()
						.containsOne(SCOPE_WARNING)
						.contains("JADX ERROR")
						.contains("Method not decompiled:")
						.doesNotContain("Type inference failed");
	}
}
