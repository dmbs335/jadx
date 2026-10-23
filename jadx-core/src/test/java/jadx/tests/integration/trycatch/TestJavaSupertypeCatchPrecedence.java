package jadx.tests.integration.trycatch;

import org.junit.jupiter.api.Test;

import jadx.tests.api.RaungTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestJavaSupertypeCatchPrecedence extends RaungTest {
	@Test
	public void test() {
		allowWarnInCode(); // the shadowed handler bytecode is unreachable
		assertThat(getClassNodeFromRaung()).code()
				.contains("catch (RuntimeException")
				.doesNotContain("catch (IllegalStateException")
				.containsPattern("(?s)catch \\(RuntimeException[^)]*\\) \\{[^}]*handled\\(\\);");
	}
}
