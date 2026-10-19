package jadx.tests.integration.types;

import java.util.List;

import org.junit.jupiter.api.Test;

import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestInvalidInvokeArgument extends SmaliTest {
	@Override
	protected void runChecks(List<ClassNode> classes) {
		org.assertj.core.api.Assertions.assertThat(classes.get(0).root().getErrorsCounter().getErrorCount()).isEqualTo(2);
	}

	@Test
	public void test() {
		disableCompilation();
		assertThat(getClassNodeFromSmali()).code()
				.contains("Incompatible method argument type: java.lang.String for int")
				.contains("Incompatible method argument type: int for java.lang.String")
				.contains("Method not decompiled: types.TestInvalidInvokeArgument.stringToInt():void")
				.contains("Method not decompiled: types.TestInvalidInvokeArgument.intToString():void")
				.contains("consumeObject(getString());")
				.contains("consumeObject(getInt());");
	}
}
