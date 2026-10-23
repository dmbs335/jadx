package jadx.tests.integration.trycatch;

import org.junit.jupiter.api.Test;

import jadx.tests.api.RaungTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestJavaSharedThrowingTail extends RaungTest {
	@Test
	public void test() {
		assertThat(getClassNodeFromRaung()).code()
				.containsPattern("(?s)catch \\(IllegalStateException[^)]*\\) \\{[^}]*handle\\(\\);[^}]*\\}")
				.containsPattern("(?s)catch \\(Throwable[^)]*\\) \\{[^}]*\\}\\s*second\\(\\);");
	}
}
