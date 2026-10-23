package jadx.tests.integration.types;

import java.util.List;

import org.junit.jupiter.api.Test;

import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestInvalidPayloadControlFlow extends SmaliTest {
	@Override
	protected void runChecks(List<ClassNode> classes) {
		org.assertj.core.api.Assertions.assertThat(classes.get(0).root().getErrorsCounter().getErrorCount()).isGreaterThanOrEqualTo(4);
	}

	@Test
	public void test() {
		disableCompilation();
		assertThat(getClassNodeFromSmali()).code()
				.contains("Branch targets data payload")
				.contains("Control flow reaches data payload")
				.contains("Method not decompiled: types.TestInvalidPayloadControlFlow.branchToArrayPayload():int[]")
				.contains("Method not decompiled: types.TestInvalidPayloadControlFlow.branchToSwitchPayload(int):int")
				.contains("Method not decompiled: types.TestInvalidPayloadControlFlow.fallthroughIntoArrayPayload():int[]")
				.contains("Method not decompiled: types.TestInvalidPayloadControlFlow.fallthroughIntoSwitchPayload(int):int")
				.doesNotContain("Method not decompiled: types.TestInvalidPayloadControlFlow.validArray")
				.doesNotContain("Failed to set jump");
	}
}
