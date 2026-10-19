package jadx.tests.integration.types;

import java.util.List;

import org.junit.jupiter.api.Test;

import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestInvalidInvokeReceiver extends SmaliTest {
	@Override
	protected void runChecks(List<ClassNode> classes) {
		org.assertj.core.api.Assertions.assertThat(classes.get(0).root().getErrorsCounter().getErrorCount()).isEqualTo(5);
	}

	@Test
	public void test() {
		disableCompilation();
		assertThat(getClassNodeFromSmali())
				.code()
				.contains("JADX ERROR")
				.contains("Primitive value used as invoke receiver")
				.contains("Incompatible invoke receiver: java.lang.String for java.lang.Number")
				.contains("Method not decompiled: types.TestInvalidInvokeReceiver.stringToNumber(java.lang.String):int")
				.contains("Method not decompiled: types.TestInvalidInvokeReceiver.invokeReceiver():int")
				.contains("Incompatible invoke receiver")
				.contains("num.intValue()")
				.contains("Method not decompiled:")
				.contains("valid");
	}
}
