package jadx.tests.integration.types;

import java.util.List;

import org.junit.jupiter.api.Test;

import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestInvalidArithOperand extends SmaliTest {
	@Override
	protected void runChecks(List<ClassNode> classes) {
		org.assertj.core.api.Assertions.assertThat(classes.get(0).root().getErrorsCounter().getErrorCount()).isEqualTo(13);
	}

	@Test
	public void test() {
		disableCompilation();
		assertThat(getClassNodeFromSmali())
				.code()
				.contains("Invalid ADD operand type: java.lang.Object")
				.contains("Invalid ADD operand type: java.lang.String")
				.contains("Method not decompiled: types.TestInvalidArithOperand.addStringField(int):void")
				.contains("Method not decompiled: types.TestInvalidArithOperand.addInvokeObject(int):int")
				.contains("Method not decompiled: types.TestInvalidArithOperand.addInvokeString(int):int")
				.contains("Method not decompiled: types.TestInvalidArithOperand.addArrayObject(java.lang.Object[], int, int):int")
				.contains("Method not decompiled: types.TestInvalidArithOperand.addArrayString(java.lang.String[], int, int):int")
				.contains("Method not decompiled: types.TestInvalidArithOperand.addCastString(java.lang.Object, int):int")
				.contains("Method not decompiled: types.TestInvalidArithOperand.addConstString(int):int")
				.contains("Method not decompiled: types.TestInvalidArithOperand.addInvokeInteger(int):int")
				.contains("Method not decompiled: types.TestInvalidArithOperand.addBoxedParam(java.lang.Integer, int):int")
				.contains("Method not decompiled: types.TestInvalidArithOperand.addMovedBoxedParam(java.lang.Integer, int):int")
				.contains("return num.intValue() + i;")
				.contains("return getInteger().intValue() + i;")
				.contains("return iArr[i] + i2;")
				.contains("Invalid SUB operand type: java.lang.Object")
				.contains("return i + i2;");
	}
}
