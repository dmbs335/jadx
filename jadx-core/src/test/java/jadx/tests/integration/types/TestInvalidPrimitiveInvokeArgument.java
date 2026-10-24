package jadx.tests.integration.types;

import java.util.List;

import org.junit.jupiter.api.Test;

import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestInvalidPrimitiveInvokeArgument extends SmaliTest {
	@Override
	protected void runChecks(List<ClassNode> classes) {
		org.assertj.core.api.Assertions.assertThat(classes.get(0).root().getErrorsCounter().getErrorCount()).isEqualTo(4);
	}

	@Test
	public void test() {
		disableCompilation();
		assertThat(getClassNodeFromSmali()).code()
				.contains("Method not decompiled: types.TestInvalidPrimitiveInvokeArgument.intToFloat(int):void")
				.contains("Method not decompiled: types.TestInvalidPrimitiveInvokeArgument.longToDouble(long):void")
				.contains(
						"Method not decompiled: types.TestInvalidPrimitiveInvokeArgument.intToFloatVirtual(types.TestInvalidPrimitiveInvokeArgument, int):void")
				.contains(
						"Method not decompiled: types.TestInvalidPrimitiveInvokeArgument.intToFloatConstructor(int):types.TestInvalidPrimitiveInvokeArgument")
				.contains("consumeFloat((float) i);")
				.contains("consumeFloat(f);")
				.doesNotContain("Method not decompiled: types.TestInvalidPrimitiveInvokeArgument.valid");
	}
}
