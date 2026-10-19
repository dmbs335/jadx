package jadx.tests.integration.synchronize;

import java.util.List;

import org.junit.jupiter.api.Test;

import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestInvalidMonitorOperand extends SmaliTest {
	@Override
	protected void runChecks(List<ClassNode> classes) {
		org.assertj.core.api.Assertions.assertThat(classes.get(0).root().getErrorsCounter().getErrorCount()).isEqualTo(2);
	}

	@Test
	public void test() {
		disableCompilation();
		assertThat(getClassNodeFromSmali())
				.code()
				.contains("JADX ERROR")
				.contains("Primitive value used as monitor operand")
				.contains("Method not decompiled: synchronize.TestInvalidMonitorOperand.invalidInvoke():void")
				.contains("synchronized (Integer.valueOf(1))")
				.contains("Method not decompiled:");
	}
}
