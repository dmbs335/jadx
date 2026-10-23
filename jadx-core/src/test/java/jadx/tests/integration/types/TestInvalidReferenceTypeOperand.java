package jadx.tests.integration.types;

import java.util.List;

import org.junit.jupiter.api.Test;

import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestInvalidReferenceTypeOperand extends SmaliTest {
	@Override
	protected void runChecks(List<ClassNode> classes) {
		org.assertj.core.api.Assertions.assertThat(classes.get(0).root().getErrorsCounter().getErrorCount()).isGreaterThanOrEqualTo(2);
	}

	@Test
	public void test() {
		disableCompilation();
		assertThat(getClassNodeFromSmali()).code()
				.contains("check-cast type must be a reference: int")
				.contains("instance-of type must be a reference: int")
				.contains(
						"Method not decompiled: types.TestInvalidReferenceTypeOperand.checkCastPrimitive(java.lang.Object):java.lang.Object")
				.contains(
						"Method not decompiled: types.TestInvalidReferenceTypeOperand.instanceOfPrimitive(java.lang.Object):boolean")
				.doesNotContain("Method not decompiled: types.TestInvalidReferenceTypeOperand.valid");
	}
}
