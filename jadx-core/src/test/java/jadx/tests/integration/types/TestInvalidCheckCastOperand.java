package jadx.tests.integration.types;

import java.util.List;

import org.junit.jupiter.api.Test;

import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestInvalidCheckCastOperand extends SmaliTest {
	@Override
	protected void runChecks(List<ClassNode> classes) {
		org.assertj.core.api.Assertions.assertThat(classes.get(0).root().getErrorsCounter().getErrorCount()).isEqualTo(7);
	}

	@Test
	public void test() {
		disableCompilation();
		assertThat(getClassNodeFromSmali())
				.code()
				.contains("Primitive value used as check-cast operand: int")
				.contains("Method not decompiled: types.TestInvalidCheckCastOperand.invalidUnrelatedClasses(java.lang.Integer):java.lang.String")
				.contains("Method not decompiled: types.TestInvalidCheckCastOperand.invalidFinalToInterface(java.lang.Integer):java.lang.CharSequence")
				.contains("Method not decompiled: types.TestInvalidCheckCastOperand.invalidInterfaceToFinal(java.lang.CharSequence):java.lang.Integer")
				.contains("Method not decompiled: types.TestInvalidCheckCastOperand.invalidArrayToObject(int[]):java.lang.String")
				.contains("Method not decompiled: types.TestInvalidCheckCastOperand.invalidObjectToArray(java.lang.String):int[]")
				.contains("Method not decompiled: types.TestInvalidCheckCastOperand.invalidCrossDimension(int[]):java.lang.Object[][]")
				.contains("Method not decompiled: types.TestInvalidCheckCastOperand.invalidPrimitive(int):java.lang.String")
				.contains("return (CharSequence) number;")
				.contains("return (int[]) serializable;")
				.contains("return iArr;")
				.contains("return (String) null;");
	}
}
