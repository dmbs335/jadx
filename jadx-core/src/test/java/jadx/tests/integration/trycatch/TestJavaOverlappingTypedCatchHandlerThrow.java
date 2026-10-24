package jadx.tests.integration.trycatch;

import java.util.List;

import org.junit.jupiter.api.Test;

import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.RaungTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestJavaOverlappingTypedCatchHandlerThrow extends RaungTest {
	@Override
	protected void runChecks(List<ClassNode> classes) {
		org.assertj.core.api.Assertions.assertThat(classes.get(0).root().getErrorsCounter().getErrorCount()).isGreaterThanOrEqualTo(1);
	}

	@Test
	public void test() {
		disableCompilation();
		assertThat(getClassNodeFromRaung()).code()
				.contains("Incorrect JVM typed catch scope: unprotected throwing code was included in a narrower range")
				.contains("Method not decompiled: trycatch.TestJavaOverlappingTypedCatchHandlerThrow.test():int");
	}
}
