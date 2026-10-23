package jadx.tests.integration.types;

import java.util.List;

import org.junit.jupiter.api.Test;

import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestInvalidMethodEnd extends SmaliTest {
	@Override
	protected void runChecks(List<ClassNode> classes) {
		org.assertj.core.api.Assertions.assertThat(classes.get(0).root().getErrorsCounter().getErrorCount()).isGreaterThanOrEqualTo(2);
	}

	@Test
	public void test() {
		disableCompilation();
		assertThat(getClassNodeFromSmali()).code()
				.contains("Control flow exits method without return or throw")
				.contains("Method not decompiled: types.TestInvalidMethodEnd.noReturn():int")
				.contains("Method not decompiled: types.TestInvalidMethodEnd.conditionalFallOff(boolean):int")
				.doesNotContain("Method not decompiled: types.TestInvalidMethodEnd.validReturn")
				.doesNotContain("Method not decompiled: types.TestInvalidMethodEnd.infiniteLoop")
				.doesNotContain("Method not decompiled: types.TestInvalidMethodEnd.unreachableTail");
	}
}
