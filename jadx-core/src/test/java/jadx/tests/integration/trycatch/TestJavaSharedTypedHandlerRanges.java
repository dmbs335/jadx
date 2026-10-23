package jadx.tests.integration.trycatch;

import java.util.List;

import org.junit.jupiter.api.Test;

import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.RaungTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestJavaSharedTypedHandlerRanges extends RaungTest {
	@Override
	protected void runChecks(List<ClassNode> classes) {
		org.assertj.core.api.Assertions.assertThat(classes.get(0).root().getErrorsCounter().getErrorCount()).isGreaterThanOrEqualTo(1);
	}

	@Test
	public void test() {
		disableCompilation();
		assertThat(getClassNodeFromRaung()).code()
				.contains("Exception handler shared by try ranges with different catch types")
				.contains("Method not decompiled: trycatch.TestJavaSharedTypedHandlerRanges.test(boolean):void")
				.doesNotContain("Method not decompiled: trycatch.TestJavaSharedTypedHandlerRanges.sameTypes(boolean):void");
	}
}
