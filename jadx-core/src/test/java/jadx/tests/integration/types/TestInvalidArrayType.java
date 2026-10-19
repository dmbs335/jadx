package jadx.tests.integration.types;

import java.util.List;

import org.junit.jupiter.api.Test;

import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestInvalidArrayType extends SmaliTest {
	@Override
	protected void runChecks(List<ClassNode> classes) {
		org.assertj.core.api.Assertions.assertThat(classes.get(0).root().getErrorsCounter().getErrorCount()).isEqualTo(2);
	}

	@Test
	public void test() {
		disableCompilation();
		assertThat(getClassNodeFromSmali()).code()
				.contains("new-array type is not an array: java.lang.String")
				.contains("filled-new-array type is not an array: java.lang.String")
				.contains("Method not decompiled: types.TestInvalidArrayType.invalidNewArray(int):java.lang.String")
				.contains("Method not decompiled: types.TestInvalidArrayType.invalidFilledNewArray(java.lang.String):java.lang.String")
				.doesNotContain("Method not decompiled: types.TestInvalidArrayType.valid");
	}
}
