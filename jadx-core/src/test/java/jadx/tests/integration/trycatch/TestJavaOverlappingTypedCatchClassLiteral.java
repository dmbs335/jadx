package jadx.tests.integration.trycatch;

import org.junit.jupiter.api.Test;

import jadx.tests.api.RaungTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestJavaOverlappingTypedCatchClassLiteral extends RaungTest {
	@Test
	public void test() {
		disableCompilation();
		assertThat(getClassNodeFromRaung()).code()
				.containsPattern(
						"catch \\(IllegalArgumentException[^)]*\\) \\{\\s*return MissingClassLiteralTarget\\.class;\\s*\\} catch \\(LinkageError")
				.doesNotContain("JADX ERROR");
	}
}
