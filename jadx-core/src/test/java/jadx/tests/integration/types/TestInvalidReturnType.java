package jadx.tests.integration.types;

import java.util.List;

import org.junit.jupiter.api.Test;

import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestInvalidReturnType extends SmaliTest {
	@Override
	protected void runChecks(List<ClassNode> classes) {
		org.assertj.core.api.Assertions.assertThat(classes.get(0).root().getErrorsCounter().getErrorCount()).isEqualTo(9);
	}

	@Test
	public void test() {
		disableCompilation();
		assertThat(getClassNodeFromSmali())
				.code()
				.contains("Incompatible return type: java.lang.Object for int")
				.contains("Incompatible return type: java.lang.String for int")
				.contains("Incompatible return type: int for java.lang.String")
				.contains("Incompatible return type: int for java.lang.Object")
				.contains("Method not decompiled: types.TestInvalidReturnType.stringInvokeToInt():int")
				.contains("Method not decompiled: types.TestInvalidReturnType.intInvokeToString():java.lang.String")
				.contains("Method not decompiled: types.TestInvalidReturnType.objectInvokeToString():java.lang.String")
				.contains("Missing return value for int")
				.contains("Method not decompiled: types.TestInvalidReturnType.valueInVoid")
				.contains("return i;")
				.contains("return str;")
				.contains("return getString();")
				.contains("return getInt();");
	}
}
