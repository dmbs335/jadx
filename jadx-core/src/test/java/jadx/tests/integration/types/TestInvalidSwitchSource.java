package jadx.tests.integration.types;

import java.util.List;

import org.junit.jupiter.api.Test;

import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestInvalidSwitchSource extends SmaliTest {
	@Override
	protected void runChecks(List<ClassNode> classes) {
		org.assertj.core.api.Assertions.assertThat(classes.get(0).root().getErrorsCounter().getErrorCount()).isEqualTo(1);
	}

	@Test
	public void test() {
		disableCompilation();
		assertThat(getClassNodeFromSmali()).code()
				.contains("Switch operand type float is not an integral type")
				.contains("Method not decompiled: types.TestInvalidSwitchSource.floatSwitch(float):int")
				.contains("? 1 : 0")
				.doesNotContain("Method not decompiled: types.TestInvalidSwitchSource.validIntSwitch");
	}
}
