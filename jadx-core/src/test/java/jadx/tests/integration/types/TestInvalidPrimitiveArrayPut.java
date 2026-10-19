package jadx.tests.integration.types;

import java.util.List;

import org.junit.jupiter.api.Test;

import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestInvalidPrimitiveArrayPut extends SmaliTest {
	@Override
	protected void runChecks(List<ClassNode> classes) {
		org.assertj.core.api.Assertions.assertThat(classes.get(0).root().getErrorsCounter().getErrorCount()).isEqualTo(3);
	}

	@Test
	public void test() {
		disableCompilation();
		assertThat(getClassNodeFromSmali()).code()
				.contains("Method not decompiled: types.TestInvalidPrimitiveArrayPut.floatToIntArray(int[], float):void")
				.contains("Method not decompiled: types.TestInvalidPrimitiveArrayPut.intToFloatArray(float[], int):void")
				.contains("Method not decompiled: types.TestInvalidPrimitiveArrayPut.longToDoubleArray(double[], long):void")
				.contains("iArr[0] = i;")
				.contains("jArr[0] = j;")
				.doesNotContain("Method not decompiled: types.TestInvalidPrimitiveArrayPut.valid");
	}
}
