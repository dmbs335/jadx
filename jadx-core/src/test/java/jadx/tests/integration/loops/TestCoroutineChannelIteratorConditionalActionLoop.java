package jadx.tests.integration.loops;

import java.util.List;

import org.junit.jupiter.api.Test;

import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestCoroutineChannelIteratorConditionalActionLoop extends SmaliTest {
	@Override
	protected void runChecks(List<ClassNode> classes) {
		// Both immediate suspend completions must use their corresponding resume states.
		// Normalizing just one state duplicates the action tail and uses an out-of-scope next2.
		org.assertj.core.api.Assertions.assertThat(classes).hasSize(1);
		org.assertj.core.api.Assertions.assertThat(classes.get(0).root().getErrorsCounter().getErrorCount()).isZero();
		org.assertj.core.api.Assertions.assertThat(classes.get(0).root().getErrorsCounter().getWarnsCount()).isZero();
	}

	@Test
	public void preservesPredicateAndActionPaths() {
		disableCompilation(); // Android coroutine library classes are not available to javac.
		getArgs().setShowInconsistentCode(false);
		assertThat(getClassNodeFromSmali())
				.code()
				.containsOne("shouldSkip(next)")
				.containsOne("action(next, this)")
				.contains("this.label = 1", "this.label = 2")
				.doesNotContain("next2")
				.doesNotContain("Unsafe code duplication")
				.doesNotContain("Unsupported multi-entry loop pattern")
				.doesNotContain("Region traversal cycle prevented")
				.doesNotContain("Recursive region processing prevented")
				.doesNotContain("Method not decompiled")
				.doesNotContain("JADX ERROR");
	}
}
