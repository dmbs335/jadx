package jadx.tests.integration.trycatch;

import org.junit.jupiter.api.Test;

import jadx.tests.api.RaungTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestJavaPartialSupertypeCatchPrecedence extends RaungTest {
	@Test
	public void test() {
		assertThat(getClassNodeFromRaung()).code()
				.contains("catch (RuntimeException")
				.contains("catch (IllegalStateException")
				.containsPattern("(?s)try \\{\\s*second\\(\\);\\s*\\} catch \\(IllegalStateException[^)]*\\) \\{[^}]*typed\\(\\);");
	}
}
