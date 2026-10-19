package jadx.tests.integration.loops;

import java.util.List;

import org.junit.jupiter.api.Test;

import jadx.api.CommentsLevel;
import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestCoroutineTwoStateAwaitPointerEventBackEdge extends SmaliTest {
	@Override
	protected void runChecks(List<ClassNode> classes) {
		org.assertj.core.api.Assertions.assertThat(classes).hasSize(1);
		org.assertj.core.api.Assertions.assertThat(classes.get(0).root().getErrorsCounter().getErrorCount()).isEqualTo(1);
	}

	@Test
	public void reportsUnresolvedRegionTraversal() {
		disableCompilation();
		getArgs().setCommentsLevel(CommentsLevel.DEBUG);
		assertThat(getClassNodeFromSmali())
				.code()
				.contains("Region traversal cycle prevented")
				.contains("Recursive region processing prevented")
				.containsOne("Unresolved control-flow restructuring: recursive region traversal, region traversal cycle")
				.contains("JADX ERROR")
				.contains("Method not decompiled:");
	}
}
