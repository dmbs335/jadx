package jadx.tests.integration.loops;

import java.util.List;

import org.junit.jupiter.api.Test;

import jadx.api.CommentsLevel;
import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestCoroutineDragNestedScansExact extends SmaliTest {
	@Override
	protected void runChecks(List<ClassNode> classes) {
		org.assertj.core.api.Assertions.assertThat(classes.get(0).root().getErrorsCounter().getErrorCount()).isEqualTo(1);
	}

	@Test
	public void test() {
		disableCompilation();
		allowWarnInCode();
		getArgs().setCommentsLevel(CommentsLevel.DEBUG);
		assertThat(getClassNodeFromSmali())
				.code()
				.contains("Incompatible local assignment type: androidx.compose.ui.input.pointer.AwaitPointerEventScope for long")
				.contains("Method not decompiled: loops.TestCoroutineDragNestedScansExact.m0awaitHorizontalPointerSlopOrCancellation");
	}
}
