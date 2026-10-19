package jadx.tests.integration.types;

import java.util.List;

import org.junit.jupiter.api.Test;

import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestInvalidArrayIndexSource extends SmaliTest {
	@Override
	protected void runChecks(List<ClassNode> classes) {
		org.assertj.core.api.Assertions.assertThat(classes.get(0).root().getErrorsCounter().getErrorCount()).isEqualTo(3);
	}

	@Test
	public void test() {
		disableCompilation();
		assertThat(getClassNodeFromSmali()).code()
				.contains("Invalid array dimension type: float")
				.contains("Invalid array index type: float")
				.contains("Method not decompiled: types.TestInvalidArrayIndexSource.floatSize(float):int[]")
				.contains("Method not decompiled: types.TestInvalidArrayIndexSource.floatIndexGet(int[], float):int")
				.contains("Method not decompiled: types.TestInvalidArrayIndexSource.floatIndexPut(int[], float, int):void")
				.doesNotContain("Method not decompiled: types.TestInvalidArrayIndexSource.validIndex");
	}
}
