package jadx.tests.integration.types;

import java.util.List;

import org.junit.jupiter.api.Test;

import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestInvalidReferenceCompare extends SmaliTest {
	@Override
	protected void runChecks(List<ClassNode> classes) {
		org.assertj.core.api.Assertions.assertThat(classes.get(0).root().getErrorsCounter().getErrorCount()).isEqualTo(2);
	}

	@Test
	public void test() {
		disableCompilation();
		assertThat(getClassNodeFromSmali()).code()
				.contains("Incompatible reference comparison: java.lang.String and java.lang.Integer")
				.contains("Method not decompiled: types.TestInvalidReferenceCompare.unrelatedFinalClasses(java.lang.String, java.lang.Integer):boolean")
				.contains("Method not decompiled: types.TestInvalidReferenceCompare.unrelatedInvokeResults():boolean")
				.contains("== obj");
	}
}
