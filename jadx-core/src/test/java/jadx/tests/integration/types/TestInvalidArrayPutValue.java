package jadx.tests.integration.types;

import java.util.List;

import org.junit.jupiter.api.Test;

import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestInvalidArrayPutValue extends SmaliTest {
	@Override
	protected void runChecks(List<ClassNode> classes) {
		org.assertj.core.api.Assertions.assertThat(classes.get(0).root().getErrorsCounter().getErrorCount()).isEqualTo(2);
	}

	@Test
	public void test() {
		disableCompilation();
		assertThat(getClassNodeFromSmali())
				.code()
				.contains("Incompatible array assignment type: java.lang.String for int")
				.contains("Incompatible array assignment type: int for java.lang.String")
				.contains("Method not decompiled: types.TestInvalidArrayPutValue.stringToIntArray(int[]):void")
				.contains("Method not decompiled: types.TestInvalidArrayPutValue.intToStringArray(java.lang.String[]):void")
				.contains("((Object[]) strArr)[0] = getObject();")
				.contains("objArr[0] = getInt();")
				.contains("objArr[0] = getString();")
				.contains("strArr[0] = getString();");
	}
}
