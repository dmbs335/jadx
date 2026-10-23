package jadx.tests.integration.types;

import java.util.List;

import org.junit.jupiter.api.Test;

import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestInvalidMoveException extends SmaliTest {
	@Override
	protected void runChecks(List<ClassNode> classes) {
		org.assertj.core.api.Assertions.assertThat(classes.get(0).root().getErrorsCounter().getErrorCount()).isGreaterThanOrEqualTo(4);
	}

	@Test
	public void test() {
		disableCompilation();
		assertThat(getClassNodeFromSmali()).code()
				.contains("move-exception is not the first instruction of an exception handler")
				.contains("Branch targets move-exception")
				.contains("Control flow falls through to move-exception")
				.contains("Method not decompiled: types.TestInvalidMoveException.orphan():java.lang.Object")
				.contains("Method not decompiled: types.TestInvalidMoveException.delayedHandler():java.lang.Object")
				.contains("Method not decompiled: types.TestInvalidMoveException.branchToHandler():java.lang.Object")
				.contains("Method not decompiled: types.TestInvalidMoveException.fallthroughIntoHandler():java.lang.Object")
				.doesNotContain("Method not decompiled: types.TestInvalidMoveException.validHandler");
	}
}
