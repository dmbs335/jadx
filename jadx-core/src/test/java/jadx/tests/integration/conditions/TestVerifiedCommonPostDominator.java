package jadx.tests.integration.conditions;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestVerifiedCommonPostDominator extends SmaliTest {

	@Test
	public void test() {
		allowWarnInCode();
		disableCompilation();
		assertThat(getClassNodeFromSmali())
				.code()
				.containsOne("tailA();")
				.containsOne("tailB();")
				.containsOne("tailC();")
				.doesNotContain("Method not decompiled")
				.doesNotContain("JADX ERROR");
	}
}
