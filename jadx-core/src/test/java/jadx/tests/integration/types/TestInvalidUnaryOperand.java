package jadx.tests.integration.types;

import java.util.List;

import org.junit.jupiter.api.Test;

import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestInvalidUnaryOperand extends SmaliTest {
	@Override
	protected void runChecks(List<ClassNode> classes) {
		org.assertj.core.api.Assertions.assertThat(classes.get(0).root().getErrorsCounter().getErrorCount()).isEqualTo(5);
	}

	@Test
	public void test() {
		disableCompilation();
		assertThat(getClassNodeFromSmali())
				.code()
				.contains("Invalid unary operand type for '-': java.lang.Object")
				.contains("Invalid unary operand type for '~': java.lang.Object")
				.contains("Invalid unary operand type for '~': float")
				.contains("Method not decompiled: types.TestInvalidUnaryOperand.negInvoke():int")
				.contains("Method not decompiled: types.TestInvalidUnaryOperand.notInvoke():int")
				.contains("return -i;")
				.contains("return ~i;");
	}
}
