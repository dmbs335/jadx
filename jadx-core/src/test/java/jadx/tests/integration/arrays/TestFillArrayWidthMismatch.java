package jadx.tests.integration.arrays;

import java.util.List;

import org.junit.jupiter.api.Test;

import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestFillArrayWidthMismatch extends SmaliTest {
	@Override
	protected void runChecks(List<ClassNode> classes) {
		org.assertj.core.api.Assertions.assertThat(classes.get(0).root().getErrorsCounter().getErrorCount()).isEqualTo(3);
	}

	@Test
	public void test() {
		disableCompilation();
		assertThat(getClassNodeFromSmali()).code()
				.contains("Fill-array payload element width 1 does not match array element type int")
				.contains("Fill-array payload size 2 exceeds array length 1")
				.contains("Method not decompiled: arrays.TestFillArrayWidthMismatch.bytePayloadIntoIntArray():int[]")
				.contains("Method not decompiled: arrays.TestFillArrayWidthMismatch.bytePayloadIntoIntParam(int[]):void")
				.contains("Method not decompiled: arrays.TestFillArrayWidthMismatch.tooManyPayloadValues():int[]")
				.doesNotContain("Method not decompiled: arrays.TestFillArrayWidthMismatch.validIntPayload")
				.doesNotContain("Method not decompiled: arrays.TestFillArrayWidthMismatch.validShortPayload")
				.doesNotContain("Method not decompiled: arrays.TestFillArrayWidthMismatch.validIntParam")
				.doesNotContain("Method not decompiled: arrays.TestFillArrayWidthMismatch.validBytePayload")
				.doesNotContain("Method not decompiled: arrays.TestFillArrayWidthMismatch.validFloatPayload");
	}
}
