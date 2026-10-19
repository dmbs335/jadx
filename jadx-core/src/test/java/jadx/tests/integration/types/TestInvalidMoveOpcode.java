package jadx.tests.integration.types;

import java.util.List;

import org.junit.jupiter.api.Test;

import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestInvalidMoveOpcode extends SmaliTest {
	@Override
	protected void runChecks(List<ClassNode> classes) {
		org.assertj.core.api.Assertions.assertThat(classes.get(0).root().getErrorsCounter().getErrorCount()).isEqualTo(5);
	}

	@Test
	public void test() {
		disableCompilation();
		assertThat(getClassNodeFromSmali()).code()
				.contains("Method not decompiled: types.TestInvalidMoveOpcode.intViaObject(int):int")
				.contains("Method not decompiled: types.TestInvalidMoveOpcode.objectViaNarrow(java.lang.Object):java.lang.Object")
				.contains("Method not decompiled: types.TestInvalidMoveOpcode.intViaWide(int):int")
				.contains("Method not decompiled: types.TestInvalidMoveOpcode.objectViaIntInvoke():java.lang.Object")
				.contains("Method not decompiled: types.TestInvalidMoveOpcode.unusedInvalidMove(int):void")
				.doesNotContain("Method not decompiled: types.TestInvalidMoveOpcode.valid");
	}
}
