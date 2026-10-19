package jadx.tests.integration.types;

import java.util.List;

import org.junit.jupiter.api.Test;

import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestInvalidPrimitiveFieldPut extends SmaliTest {
	@Override
	protected void runChecks(List<ClassNode> classes) {
		org.assertj.core.api.Assertions.assertThat(classes.get(0).root().getErrorsCounter().getErrorCount()).isEqualTo(3);
	}

	@Test
	public void test() {
		disableCompilation();
		assertThat(getClassNodeFromSmali()).code()
				.contains("Method not decompiled: types.TestInvalidPrimitiveFieldPut.floatToStaticInt(float):void")
				.contains("Method not decompiled: types.TestInvalidPrimitiveFieldPut.intToStaticFloat(int):void")
				.contains(
						"Method not decompiled: types.TestInvalidPrimitiveFieldPut.longToInstanceDouble(types.TestInvalidPrimitiveFieldPut, long):void")
				.contains("floatValue = f;")
				.contains("doubleValue = d;")
				.contains("floatValue = i;")
				.doesNotContain("Method not decompiled: types.TestInvalidPrimitiveFieldPut.valid");
	}
}
