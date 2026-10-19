package jadx.tests.integration.types;

import java.util.List;

import org.junit.jupiter.api.Test;

import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestInvalidMoveResultOpcode extends SmaliTest {
	@Override
	protected void runChecks(List<ClassNode> classes) {
		org.assertj.core.api.Assertions.assertThat(classes.get(0).root().getErrorsCounter().getErrorCount()).isEqualTo(6);
	}

	@Test
	public void test() {
		disableCompilation();
		assertThat(getClassNodeFromSmali()).code()
				.contains("Move-result opcode MOVE_RESULT_OBJECT does not match result type int")
				.contains("Move-result opcode MOVE_RESULT_NARROW does not match result type java.lang.Object")
				.contains("Move-result opcode MOVE_RESULT_WIDE does not match result type int")
				.contains("Move-result opcode MOVE_RESULT_NARROW does not match result type int[]")
				.contains("Move-result opcode MOVE_RESULT_NARROW does not match result type void")
				.contains("Method not decompiled: types.TestInvalidMoveResultOpcode.objectFromInt():int")
				.contains("Method not decompiled: types.TestInvalidMoveResultOpcode.narrowFromObject():java.lang.Object")
				.contains("Method not decompiled: types.TestInvalidMoveResultOpcode.wideFromInt():long")
				.contains("Method not decompiled: types.TestInvalidMoveResultOpcode.narrowFromFilledArray(int):int[]")
				.contains("Method not decompiled: types.TestInvalidMoveResultOpcode.resultFromVoid():int")
				.contains("Method not decompiled: types.TestInvalidMoveResultOpcode.unusedObjectResult():void")
				.doesNotContain("Method not decompiled: types.TestInvalidMoveResultOpcode.valid");
	}
}
