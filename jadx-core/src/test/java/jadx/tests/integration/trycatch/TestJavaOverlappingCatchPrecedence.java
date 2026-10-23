package jadx.tests.integration.trycatch;

import org.junit.jupiter.api.Test;

import jadx.tests.api.RaungTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestJavaOverlappingCatchPrecedence extends RaungTest {
	@Test
	public void test() {
		allowWarnInCode(); // the shadowed handler bytecode is unreachable
		assertThat(getClassNodeFromRaung()).code()
				.contains("catch (Throwable")
				.doesNotContain("catch (IllegalStateException")
				.containsPattern("(?s)catch \\(Throwable[^)]*\\) \\{[^}]*handled\\(\\);");
	}
}
