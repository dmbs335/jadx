package jadx.tests.integration.types;

import java.util.List;

import org.junit.jupiter.api.Test;

import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestInvalidThrowOperand extends SmaliTest {
	@Override
	protected void runChecks(List<ClassNode> classes) {
		org.assertj.core.api.Assertions.assertThat(classes.get(0).root().getErrorsCounter().getErrorCount()).isEqualTo(5);
	}

	@Test
	public void test() {
		disableCompilation();
		assertThat(getClassNodeFromSmali())
				.code()
				.contains("Invalid throw operand type: int")
				.contains("Invalid throw operand type: java.lang.Object")
				.contains("Invalid throw operand type: java.lang.String")
				.contains("Invalid throw operand type: java.lang.Throwable[]")
				.contains("Method not decompiled: types.TestInvalidThrowOperand.invoke():void")
				.doesNotContain("Failed to analyze thrown exceptions")
				.contains("throw th;")
				.contains("throw null;");
	}
}
