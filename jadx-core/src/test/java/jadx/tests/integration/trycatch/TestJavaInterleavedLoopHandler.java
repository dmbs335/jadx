package jadx.tests.integration.trycatch;

import org.junit.jupiter.api.Test;

import jadx.tests.api.RaungTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestJavaInterleavedLoopHandler extends RaungTest {
	@Test
	public void test() {
		forceDecompiledCheck();
		assertThat(getClassNodeFromRaung()).code()
				.contains("catch (IllegalStateException")
				.contains("continue;")
				.doesNotContain("JADX ERROR");
	}
}
