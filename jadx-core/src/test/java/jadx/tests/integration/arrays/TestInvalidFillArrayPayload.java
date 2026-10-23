package jadx.tests.integration.arrays;

import java.util.List;

import org.junit.jupiter.api.Test;

import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestInvalidFillArrayPayload extends SmaliTest {
	@Override
	protected void runChecks(List<ClassNode> classes) {
		org.assertj.core.api.Assertions.assertThat(classes.get(0).root().getErrorsCounter().getErrorCount()).isGreaterThanOrEqualTo(2);
	}

	@Test
	public void test() {
		disableCompilation();
		assertThat(getClassNodeFromSmali())
				.code()
				.contains("Payload for fill-array not found")
				.contains("Method not decompiled: arrays.TestInvalidFillArrayPayload.test():int[]")
				.contains("Method not decompiled: arrays.TestInvalidFillArrayPayload.beforePayload():int[]")
				.contains("{1, 2, 3}")
				.doesNotContain("Repaired invalid fill-array reference")
				.doesNotContain("Method not decompiled: arrays.TestInvalidFillArrayPayload.valid():int[]");
	}
}
