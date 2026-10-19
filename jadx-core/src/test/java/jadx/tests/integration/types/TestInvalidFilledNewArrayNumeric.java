package jadx.tests.integration.types;

import java.util.List;

import org.junit.jupiter.api.Test;

import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestInvalidFilledNewArrayNumeric extends SmaliTest {
	@Override
	protected void runChecks(List<ClassNode> classes) {
		org.assertj.core.api.Assertions.assertThat(classes.get(0).root().getErrorsCounter().getErrorCount()).isEqualTo(3);
	}

	@Test
	public void test() {
		disableCompilation();
		assertThat(getClassNodeFromSmali()).code()
				.contains("Array initializer operand 0 type int does not match element type float")
				.contains("filled-new-array does not support wide element type: long")
				.contains("filled-new-array does not support wide element type: double")
				.contains("Method not decompiled: types.TestInvalidFilledNewArrayNumeric.intToFloatArray(int):float[]")
				.contains("Method not decompiled: types.TestInvalidFilledNewArrayNumeric.wideLongArray(long):long[]")
				.contains("Method not decompiled: types.TestInvalidFilledNewArrayNumeric.wideDoubleArrayRange(double):double[]")
				.doesNotContain("Method not decompiled: types.TestInvalidFilledNewArrayNumeric.valid");
	}
}
