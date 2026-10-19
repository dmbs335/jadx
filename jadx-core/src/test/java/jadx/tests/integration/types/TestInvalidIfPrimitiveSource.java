package jadx.tests.integration.types;

import java.util.List;

import org.junit.jupiter.api.Test;

import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestInvalidIfPrimitiveSource extends SmaliTest {
	@Override
	protected void runChecks(List<ClassNode> classes) {
		org.assertj.core.api.Assertions.assertThat(classes.get(0).root().getErrorsCounter().getErrorCount()).isEqualTo(3);
	}

	@Test
	public void test() {
		disableCompilation();
		assertThat(getClassNodeFromSmali()).code()
				.contains("If operand 0 type float is not valid for a DEX branch comparison")
				.contains("Method not decompiled: types.TestInvalidIfPrimitiveSource.floatEqZero(float):int")
				.contains("Method not decompiled: types.TestInvalidIfPrimitiveSource.floatEqFloat(float, float):int")
				.contains("Method not decompiled: types.TestInvalidIfPrimitiveSource.floatLtFloat(float, float):int")
				.doesNotContain("Method not decompiled: types.TestInvalidIfPrimitiveSource.validIntEqZero")
				.doesNotContain("Method not decompiled: types.TestInvalidIfPrimitiveSource.validFloatCmp");
	}
}
