package jadx.tests.integration.conditions;

import java.util.List;

import org.junit.jupiter.api.Test;

import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestInvalidBooleanRelationalCompare extends SmaliTest {
	@Override
	protected void runChecks(List<ClassNode> classes) {
		org.assertj.core.api.Assertions.assertThat(classes.get(0).root().getErrorsCounter().getErrorCount()).isEqualTo(4);
	}

	@Test
	public void test() {
		disableCompilation();
		assertThat(getClassNodeFromSmali())
				.code()
				.contains("JADX ERROR")
				.contains("Unsupported relational boolean condition")
				.contains("Unsupported mixed boolean condition")
				.contains("Method not decompiled:")
				.contains("checkNonBooleanLiteral")
				.contains("checkTwoBooleans")
				.contains("checkEqualBooleans")
				.contains("checkLiteralLeft")
				.doesNotContain("return z >= 1 ? 2 : 3;");
	}
}
