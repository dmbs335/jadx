package jadx.tests.integration.conditions;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestInheritedScopeExitDeepTerminal extends SmaliTest {

	@Test
	public void test() {
		assertThat(getClassNodeFromSmali())
				.code()
				.contains("if (i <= 0 || i >= 100) {")
				.contains("if (i2 < 0 || i2 >= 100) {")
				.countString(2, "return false;")
				.containsOne("touch();")
				.doesNotContain("if (i2 < 0 || i2 >= 100) {\n        }")
				.doesNotContain("Code duplicated")
				.doesNotContain("Method not decompiled")
				.doesNotContain("JADX ERROR");
	}
}
