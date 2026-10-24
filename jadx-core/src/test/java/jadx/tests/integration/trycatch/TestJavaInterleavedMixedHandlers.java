package jadx.tests.integration.trycatch;

import org.junit.jupiter.api.Test;

import jadx.tests.api.RaungTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestJavaInterleavedMixedHandlers extends RaungTest {
	@Test
	public void test() {
		forceDecompiledCheck();
		assertThat(getClassNodeFromRaung()).code()
				.contains("catch (IllegalArgumentException")
				.contains("catch (IllegalStateException")
				.contains("catch (RuntimeException")
				.contains("catch (Throwable")
				.doesNotContain("JADX ERROR");
	}
}
