package jadx.tests.integration.types;

import java.util.List;

import org.junit.jupiter.api.Test;

import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestInvalidNumericCastOperand extends SmaliTest {
	@Override
	protected void runChecks(List<ClassNode> classes) {
		org.assertj.core.api.Assertions.assertThat(classes.get(0).root().getErrorsCounter().getErrorCount()).isEqualTo(6);
	}

	@Test
	public void test() {
		disableCompilation();
		assertThat(getClassNodeFromSmali()).code()
				.contains("Method not decompiled: types.TestInvalidNumericCastOperand.intToFloatFromFloat(float):float")
				.contains("Method not decompiled: types.TestInvalidNumericCastOperand.floatToIntFromInt(int):int")
				.contains("Method not decompiled: types.TestInvalidNumericCastOperand.longToDoubleFromDouble(double):double")
				.contains("Method not decompiled: types.TestInvalidNumericCastOperand.intToFloatFromInvoke():float")
				.contains("Method not decompiled: types.TestInvalidNumericCastOperand.floatToIntFromArray(int[]):int")
				.contains("Method not decompiled: types.TestInvalidNumericCastOperand.longToDoubleFromField():double")
				.doesNotContain("Method not decompiled: types.TestInvalidNumericCastOperand.valid");
	}
}
