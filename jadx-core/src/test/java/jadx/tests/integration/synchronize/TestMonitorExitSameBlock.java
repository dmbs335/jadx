package jadx.tests.integration.synchronize;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestMonitorExitSameBlock extends SmaliTest {
	@Test
	public void test() {
		forceDecompiledCheck();
		assertThat(getClassNodeFromSmali())
				.code()
				.contains("trace = 1;")
				.contains("trace = 2;");
	}
}
