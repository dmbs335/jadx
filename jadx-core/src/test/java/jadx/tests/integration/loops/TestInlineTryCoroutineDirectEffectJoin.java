package jadx.tests.integration.loops;

import java.util.List;

import org.junit.jupiter.api.Test;

import jadx.api.CommentsLevel;
import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestInlineTryCoroutineDirectEffectJoin extends SmaliTest {
	@Override
	protected void runChecks(List<ClassNode> classes) {
		org.assertj.core.api.Assertions.assertThat(classes.get(0).root().getErrorsCounter().getErrorCount()).isEqualTo(1);
	}

	@Test
	public void test() {
		disableCompilation();
		getArgs().setCommentsLevel(CommentsLevel.DEBUG);
		assertThat(getClassNodeFromSmali())
				.code()
				.contains("JADX ERROR")
				.contains("Unsupported mixed boolean condition")
				.contains("Method not decompiled:")
				.doesNotContain("obj13 == true");
	}
}
