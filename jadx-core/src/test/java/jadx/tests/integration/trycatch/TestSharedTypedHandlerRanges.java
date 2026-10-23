package jadx.tests.integration.trycatch;

import java.util.List;

import org.junit.jupiter.api.Test;

import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestSharedTypedHandlerRanges extends SmaliTest {
	@Override
	protected void runChecks(List<ClassNode> classes) {
		org.assertj.core.api.Assertions.assertThat(classes.get(0).root().getErrorsCounter().getErrorCount()).isGreaterThanOrEqualTo(1);
	}

	@Test
	public void test() {
		disableCompilation();
		assertThat(getClassNodeFromSmali()).code()
				.contains("Exception handler shared by try ranges with different catch types")
				.contains("Method not decompiled: trycatch.TestSharedTypedHandlerRanges.test(boolean):void")
				.doesNotContain("Method not decompiled: trycatch.TestSharedTypedHandlerRanges.sameTypes(boolean):void")
				.doesNotContain("Method not decompiled: trycatch.TestSharedTypedHandlerRanges.sameMultiTypes(boolean):void");
	}
}
