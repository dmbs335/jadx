package jadx.tests.integration.types;

import java.util.List;

import org.junit.jupiter.api.Test;

import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestInvalidPrimitiveReturn extends SmaliTest {
	@Override
	protected void runChecks(List<ClassNode> classes) {
		org.assertj.core.api.Assertions.assertThat(classes.get(0).root().getErrorsCounter().getErrorCount()).isEqualTo(4);
	}

	@Test
	public void test() {
		disableCompilation();
		assertThat(getClassNodeFromSmali()).code()
				.contains("Return operand type int does not match method return type float")
				.contains("Return operand type long does not match method return type double")
				.contains("Method not decompiled: types.TestInvalidPrimitiveReturn.intToFloat(int):float")
				.contains("Method not decompiled: types.TestInvalidPrimitiveReturn.longToDouble(long):double")
				.contains("Method not decompiled: types.TestInvalidPrimitiveReturn.floatToInt(float):int")
				.contains("Method not decompiled: types.TestInvalidPrimitiveReturn.doubleToLong(double):long")
				.doesNotContain("Method not decompiled: types.TestInvalidPrimitiveReturn.valid");
	}
}
