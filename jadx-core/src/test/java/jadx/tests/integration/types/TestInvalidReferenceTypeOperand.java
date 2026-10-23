package jadx.tests.integration.types;

import java.util.List;

import org.junit.jupiter.api.Test;

import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestInvalidReferenceTypeOperand extends SmaliTest {
	@Override
	protected void runChecks(List<ClassNode> classes) {
		org.assertj.core.api.Assertions.assertThat(classes.get(0).root().getErrorsCounter().getErrorCount()).isGreaterThanOrEqualTo(1);
	}

	@Test
	public void test() {
		disableCompilation();
		assertThat(getClassNodeFromSmali()).code()
				.contains("check-cast to primitive cannot be represented as a Java cast: int")
				.contains(
						"Method not decompiled: types.TestInvalidReferenceTypeOperand.checkCastPrimitive(java.lang.Object):java.lang.Object")
				.contains("return false;")
				.doesNotContain("Method not decompiled: types.TestInvalidReferenceTypeOperand.instanceOfPrimitive")
				.doesNotContain("Method not decompiled: types.TestInvalidReferenceTypeOperand.valid");
	}
}
