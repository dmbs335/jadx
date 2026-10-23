package jadx.tests.integration.types;

import java.util.List;

import org.junit.jupiter.api.Test;

import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestInvalidMoveResultPlacement extends SmaliTest {
	@Override
	protected void runChecks(List<ClassNode> classes) {
		org.assertj.core.api.Assertions.assertThat(classes.get(0).root().getErrorsCounter().getErrorCount()).isGreaterThanOrEqualTo(8);
	}

	@Test
	public void test() {
		disableCompilation();
		assertThat(getClassNodeFromSmali()).code()
				.contains("move-result must immediately follow invoke or filled-new-array")
				.contains("Branch targets move-result")
				.contains("Exception handler targets move-result")
				.contains("Method not decompiled: types.TestInvalidMoveResultPlacement.orphan():int")
				.contains("Method not decompiled: types.TestInvalidMoveResultPlacement.interrupted():int")
				.contains("Method not decompiled: types.TestInvalidMoveResultPlacement.interruptedFilledArray():int[]")
				.contains("Method not decompiled: types.TestInvalidMoveResultPlacement.branchToResult():int")
				.contains("Method not decompiled: types.TestInvalidMoveResultPlacement.conditionalBranchToResult(boolean):int")
				.contains("Method not decompiled: types.TestInvalidMoveResultPlacement.switchToResult(int):int")
				.contains("Method not decompiled: types.TestInvalidMoveResultPlacement.backwardBranchToResult(boolean):int")
				.contains("Method not decompiled: types.TestInvalidMoveResultPlacement.handlerToResult():int")
				.doesNotContain("Method not decompiled: types.TestInvalidMoveResultPlacement.valid");
	}
}
