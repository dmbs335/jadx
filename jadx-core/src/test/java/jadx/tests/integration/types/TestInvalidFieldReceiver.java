package jadx.tests.integration.types;

import java.util.List;

import org.junit.jupiter.api.Test;

import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestInvalidFieldReceiver extends SmaliTest {
	@Override
	protected void runChecks(List<ClassNode> classes) {
		org.assertj.core.api.Assertions.assertThat(classes.get(0).root().getErrorsCounter().getErrorCount()).isEqualTo(8);
	}

	@Test
	public void test() {
		disableCompilation();
		assertThat(getClassNodeFromSmali())
				.code()
				.contains("JADX ERROR")
				.contains("Primitive value used as field receiver")
				.contains("Incompatible field receiver: java.lang.String for types.TestInvalidFieldReceiver")
				.contains("Method not decompiled: types.TestInvalidFieldReceiver.getString(java.lang.String):int")
				.contains("Method not decompiled: types.TestInvalidFieldReceiver.getInvokeReceiver():int")
				.contains("Incompatible field receiver")
				.contains("Method not decompiled:")
				.contains("getValid");
	}
}
