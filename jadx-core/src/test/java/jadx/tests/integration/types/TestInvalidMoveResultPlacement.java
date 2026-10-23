package jadx.tests.integration.types;

import java.util.List;

import org.junit.jupiter.api.Test;

import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestInvalidMoveResultPlacement extends SmaliTest {
	@Override
	protected void runChecks(List<ClassNode> classes) {
		org.assertj.core.api.Assertions.assertThat(classes.get(0).root().getErrorsCounter().getErrorCount()).isGreaterThanOrEqualTo(5);
	}

	@Test
	public void test() {
		disableCompilation();
		assertThat(getClassNodeFromSmali()).code()
				.contains("move-result must immediately follow invoke or filled-new-array")
				.contains("Method not decompiled: types.TestInvalidMoveResultPlacement.orphan():int")
				.contains("Method not decompiled: types.TestInvalidMoveResultPlacement.interrupted():int")
				.contains("Method not decompiled: types.TestInvalidMoveResultPlacement.interruptedFilledArray():int[]")
				.contains("Method not decompiled: types.TestInvalidMoveResultPlacement.branchToResult():int")
				.contains("Method not decompiled: types.TestInvalidMoveResultPlacement.conditionalBranchToResult(boolean):int")
				.doesNotContain("Method not decompiled: types.TestInvalidMoveResultPlacement.valid");
	}
}
