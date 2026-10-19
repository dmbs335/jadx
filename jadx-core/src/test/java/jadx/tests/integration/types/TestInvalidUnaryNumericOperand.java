package jadx.tests.integration.types;

import java.util.List;

import org.junit.jupiter.api.Test;

import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestInvalidUnaryNumericOperand extends SmaliTest {
	@Override
	protected void runChecks(List<ClassNode> classes) {
		org.assertj.core.api.Assertions.assertThat(classes.get(0).root().getErrorsCounter().getErrorCount()).isEqualTo(5);
	}

	@Test
	public void test() {
		disableCompilation();
		assertThat(getClassNodeFromSmali()).code()
				.contains("Method not decompiled: types.TestInvalidUnaryNumericOperand.negIntFromFloat(float):int")
				.contains("Method not decompiled: types.TestInvalidUnaryNumericOperand.negFloatFromInt(int):float")
				.contains("Method not decompiled: types.TestInvalidUnaryNumericOperand.notLongFromInt(int):long")
				.contains("Method not decompiled: types.TestInvalidUnaryNumericOperand.negIntFromFloatArray(float[]):int")
				.contains("Method not decompiled: types.TestInvalidUnaryNumericOperand.notLongFromIntArray(int[]):long")
				.contains("return -i;")
				.contains("return -f;")
				.contains("return ~j;")
				.doesNotContain("Method not decompiled: types.TestInvalidUnaryNumericOperand.valid");
	}
}
