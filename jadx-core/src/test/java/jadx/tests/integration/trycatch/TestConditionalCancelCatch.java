package jadx.tests.integration.trycatch;

import java.util.List;

import org.junit.jupiter.api.Test;

import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestConditionalCancelCatch extends SmaliTest {
	@Override
	protected void runChecks(List<ClassNode> classes) {
		org.assertj.core.api.Assertions.assertThat(classes).hasSize(1);
		org.assertj.core.api.Assertions.assertThat(classes.get(0).root().getErrorsCounter().getErrorCount()).isZero();
	}

	@Test
	public void keepsConditionalCatchOutsideProtectedCall() {
		disableCompilation();
		assertThat(getClassNodeFromSmali()).code()
				.contains("catch (RuntimeException")
				.contains("isCancelled()")
				.doesNotContain("Incorrect try/catch scope")
				.doesNotContain("Method not decompiled:");
	}
}
