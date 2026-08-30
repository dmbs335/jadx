package jadx.tests.integration.loops;

import org.junit.jupiter.api.Test;

import jadx.api.CommentsLevel;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestCoroutineInlinedPendingSlotLoop extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		getArgs().setCommentsLevel(CommentsLevel.DEBUG);

		assertThat(getClassNodeFromSmali())
				.code()
				.containsOne(
						"Normalize inlined pending-slot emit completion through state 2")
				.containsOne(
						"Normalize initial inlined pending-slot entry through state 3")
				.containsOne(
						"Normalize inlined pending-slot loop through coroutine state 3")
				.containsOne("while (true) {")
				.containsOne("atomicBoolean != null && !atomicBoolean.get()")
				.contains("getAndSet(")
				.contains("compareAndSet(")
				.contains("continue;")
				.doesNotContain("Unsupported multi-entry loop pattern")
				.doesNotContain("Region traversal cycle prevented")
				.doesNotContain("Recursive region processing prevented")
				.doesNotContain("Failed to fix multi-entry loops")
				.doesNotContain("Method not decompiled")
				.doesNotContain("JADX ERROR");
	}
}
