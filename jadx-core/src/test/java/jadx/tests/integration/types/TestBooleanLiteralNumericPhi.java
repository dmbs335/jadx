package jadx.tests.integration.types;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestBooleanLiteralNumericPhi extends SmaliTest {

	@Test
	public void test() {
		assertThat(getClassNodeFromSmali())
				.code()
				.doesNotContain("Type inference failed")
				.doesNotContain("??")
				.contains("num != null ? num.intValue() : 0")
				.contains("> 0");
	}
}
