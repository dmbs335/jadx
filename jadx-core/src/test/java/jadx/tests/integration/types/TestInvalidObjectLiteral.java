package jadx.tests.integration.types;

import java.util.List;

import org.junit.jupiter.api.Test;

import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestInvalidObjectLiteral extends SmaliTest {
	@Override
	protected void runChecks(List<ClassNode> classes) {
		org.assertj.core.api.Assertions.assertThat(classes.get(0).root().getErrorsCounter().getErrorCount()).isEqualTo(5);
	}

	@Test
	public void test() {
		disableCompilation();
		assertThat(getClassNodeFromSmali())
				.code()
				.contains("JADX ERROR")
				.contains("Non-null numeric literal used as reference type")
				.contains("Method not decompiled:")
				.contains("arrayValue")
				.contains("invokeValue")
				.contains("storeValue")
				.contains("throwValue")
				.contains("return null;")
				.contains("throw null;")
				.doesNotContain("return 1;");
	}
}
