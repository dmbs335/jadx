package jadx.tests.integration.types;

import java.util.List;

import org.junit.jupiter.api.Test;

import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestInvalidFilledNewArrayValue extends SmaliTest {
	@Override
	protected void runChecks(List<ClassNode> classes) {
		org.assertj.core.api.Assertions.assertThat(classes.get(0).root().getErrorsCounter().getErrorCount()).isEqualTo(3);
	}

	@Test
	public void test() {
		disableCompilation();
		assertThat(getClassNodeFromSmali())
				.code()
				.contains("Incompatible array initializer type: java.lang.String for int")
				.contains("Incompatible array initializer type: int for java.lang.String")
				.contains("Method not decompiled: types.TestInvalidFilledNewArrayValue.stringToIntArray():int[]")
				.contains("Method not decompiled: types.TestInvalidFilledNewArrayValue.intToStringArray():java.lang.String[]")
				.contains("Method not decompiled: types.TestInvalidFilledNewArrayValue.intToStringVarargs():void")
				.contains("new String[]{getString()}");
	}
}
