package jadx.tests.integration.others;

import org.junit.jupiter.api.Test;

import jadx.tests.api.RaungTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestJavaDup2x2WideForms extends RaungTest {
	@Test
	public void test() {
		forceDecompiledCheck();
		assertThat(getClassNodeFromRaung()).code()
				.doesNotContain("JADX ERROR", "Method not decompiled");
	}
}
