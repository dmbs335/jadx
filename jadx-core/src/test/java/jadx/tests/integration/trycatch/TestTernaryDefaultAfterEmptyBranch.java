package jadx.tests.integration.trycatch;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestTernaryDefaultAfterEmptyBranch extends SmaliTest {
	@Test
	public void keepsFallbackAssignmentAfterEarlierTernaryRemovesEmptyBlock() {
		disableCompilation();
		assertThat(getClassNodeFromSmali())
				.code()
				.contains("getCause()")
				.contains("cause = th;")
				.contains("atomicReference.set(cause)")
				.doesNotContain("Code restructure failed: missing block")
				.doesNotContain("Method not decompiled:");
	}
}
