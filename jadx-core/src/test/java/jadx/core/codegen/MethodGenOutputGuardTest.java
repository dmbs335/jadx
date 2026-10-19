package jadx.core.codegen;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MethodGenOutputGuardTest {

	@Test
	void rejectsUnreachableTransferAfterDuplicatedContinue() {
		assertThat(MethodGen.hasUnreachableStatementAfterAbrupt(String.join("\n",
				"try {",
				"  work();",
				"} catch (Exception e) {",
				"  continue;",
				"  continue;",
				"}"))).isTrue();
	}

	@Test
	void skipsCommentsBetweenUnreachableTransfers() {
		assertThat(MethodGen.hasUnreachableStatementAfterAbrupt(String.join("\n",
				"continue; // generated retry",
				"/* duplicate transfer */",
				"break;"))).isTrue();
	}

	@Test
	void keepsSeparateBranchesAndCommentText() {
		assertThat(MethodGen.hasUnreachableStatementAfterAbrupt(String.join("\n",
				"if (first) {",
				"  continue;",
				"} else {",
				"  continue;",
				"}",
				"/*",
				"continue;",
				"continue;",
				"*/"))).isFalse();
	}

	@Test
	void permitsUnbracedConditionalTransfer() {
		assertThat(MethodGen.hasUnreachableStatementAfterAbrupt(String.join("\n",
				"if (done)",
				"    return;",
				"return;"))).isFalse();
	}

	@Test
	void rejectsOrdinaryStatementAfterAbruptReturnOrThrow() {
		assertThat(MethodGen.hasUnreachableStatementAfterAbrupt(String.join("\n",
				"if (ready) {",
				"  return result;",
				"  updateState();",
				"}"))).isTrue();
		assertThat(MethodGen.hasUnreachableStatementAfterAbrupt(String.join("\n",
				"throw failure;",
				"return fallback;"))).isTrue();
	}

	@Test
	void keepsFollowingScopeAndSwitchCaseReachable() {
		assertThat(MethodGen.hasUnreachableStatementAfterAbrupt(String.join("\n",
				"if (done) {",
				"  return;",
				"}",
				"work();"))).isFalse();
		assertThat(MethodGen.hasUnreachableStatementAfterAbrupt(String.join("\n",
				"case 1:",
				"  return;",
				"case 2:",
				"  work();"))).isFalse();
	}
}
