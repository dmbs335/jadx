package jadx.tests.integration.conditions;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestComplexIf4 extends SmaliTest {
	@Test
	void test() {
		assertThat(getClassNodeFromSmali()).code().contains("if (0 >= 0) {");
	}
}
