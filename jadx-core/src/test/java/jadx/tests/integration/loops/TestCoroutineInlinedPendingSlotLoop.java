package jadx.tests.integration.loops;

import java.util.List;

import org.junit.jupiter.api.Test;

import jadx.api.CommentsLevel;
import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestCoroutineInlinedPendingSlotLoop extends SmaliTest {

	@Override
	protected void runChecks(List<ClassNode> classes) {
		assertThat(classes).hasSize(1);
		assertThat(classes.get(0).root().getErrorsCounter().getErrorCount()).isZero();
		assertThat(classes.get(0).root().getErrorsCounter().getWarnsCount()).isZero();
	}

	@Test
	public void test() {
		disableCompilation();
		getArgs().setCommentsLevel(CommentsLevel.DEBUG);
		ClassNode cls = getClassNodeFromSmali();
		assertThat(cls)
				.code()
				.doesNotContain("Incomplete try/catch coverage: protected fragments remain outside handler")
				.containsOne(
						"Normalize inlined pending-slot emit completion through state 2")
				.containsOne(
						"Normalize initial inlined pending-slot entry through state 3")
				.containsOne(
						"Normalize inlined pending-slot loop through coroutine state 3")
				.containsOne("while (true) {")
				.containsOne("try {")
				.containsOne("catch (Throwable th)")
				.containsOne("cleanup();")
				.containsOne("atomicBoolean != null && !atomicBoolean.get()")
				.contains("getAndSet(")
				.contains("compareAndSet(")
				.contains("this.y |= Integer.MIN_VALUE;")
				.contains("return collect(this);")
				.contains("continue;")
				.doesNotContain("Unsupported multi-entry loop pattern")
				.doesNotContain("Region traversal cycle prevented")
				.doesNotContain("Recursive region processing prevented")
				.doesNotContain("Failed to fix multi-entry loops")
				.doesNotContain("Method not decompiled")
				.doesNotContain("JADX ERROR");
	}
}
