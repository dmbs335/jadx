package jadx.tests.integration.types;

import java.util.List;

import org.junit.jupiter.api.Test;

import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestInvalidInstanceOfOperand extends SmaliTest {
	@Override
	protected void runChecks(List<ClassNode> classes) {
		org.assertj.core.api.Assertions.assertThat(classes.get(0).root().getErrorsCounter().getErrorCount()).isEqualTo(20);
	}

	@Test
	public void test() {
		disableCompilation();
		assertThat(getClassNodeFromSmali())
				.code()
				.contains("Primitive value used as instanceof operand: int")
				.contains("Method not decompiled: types.TestInvalidInstanceOfOperand.invalidPrimitive(int):boolean")
				.contains("Method not decompiled: types.TestInvalidInstanceOfOperand.invalidInvoke():boolean")
				.contains("Method not decompiled: types.TestInvalidInstanceOfOperand.invalidUnrelatedFinal(java.lang.Integer):boolean")
				.contains("Method not decompiled: types.TestInvalidInstanceOfOperand.invalidUnrelatedClasses(java.lang.Number):boolean")
				.contains("Method not decompiled: types.TestInvalidInstanceOfOperand.invalidObjectArrays(java.lang.String[]):boolean")
				.contains("Method not decompiled: types.TestInvalidInstanceOfOperand.invalidPrimitiveArrays(int[]):boolean")
				.contains(
						"Method not decompiled: types.TestInvalidInstanceOfOperand.invalidNestedObjectArrays(java.lang.String[][]):boolean")
				.contains("Method not decompiled: types.TestInvalidInstanceOfOperand.invalidNestedPrimitiveArray(int[][]):boolean")
				.contains("Method not decompiled: types.TestInvalidInstanceOfOperand.invalidFinalToInterface(java.lang.Integer):boolean")
				.contains(
						"Method not decompiled: types.TestInvalidInstanceOfOperand.invalidInterfaceToFinal(java.lang.CharSequence):boolean")
				.contains(
						"Method not decompiled: types.TestInvalidInstanceOfOperand.invalidFinalToInterfaceArray(java.lang.Integer[]):boolean")
				.contains(
						"Method not decompiled: types.TestInvalidInstanceOfOperand.invalidInterfaceToFinalArray(java.lang.CharSequence[]):boolean")
				.contains("Method not decompiled: types.TestInvalidInstanceOfOperand.invalidPrimitiveToNestedArray(int[]):boolean")
				.contains(
						"Method not decompiled: types.TestInvalidInstanceOfOperand.invalidObjectToNestedArray(java.lang.String[]):boolean")
				.contains(
						"Method not decompiled: types.TestInvalidInstanceOfOperand.invalidNestedArrayToFinalObject(java.lang.Object[][]):boolean")
				.contains(
						"Method not decompiled: types.TestInvalidInstanceOfOperand.invalidClassArrayToNestedPrimitive(java.lang.Number[]):boolean")
				.contains(
						"Method not decompiled: types.TestInvalidInstanceOfOperand.invalidObjectToPrimitiveArray(java.lang.String):boolean")
				.contains("Method not decompiled: types.TestInvalidInstanceOfOperand.invalidPrimitiveArrayToObject(int[]):boolean")
				.contains("Method not decompiled: types.TestInvalidInstanceOfOperand.invalidNonFinalClassToArray(java.lang.Number):boolean")
				.contains("Method not decompiled: types.TestInvalidInstanceOfOperand.invalidArrayToInterface(int[]):boolean")
				.contains("return strArr instanceof CharSequence[];")
				.contains("return serializableArr instanceof CharSequence[];")
				.contains("return iArr instanceof Object[];")
				.contains("return serializable instanceof CharSequence;")
				.contains("return str instanceof CharSequence;")
				.contains("Object obj = null;")
				.contains("return obj instanceof String;");
	}
}
