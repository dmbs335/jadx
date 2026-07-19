package jadx.tests.integration.types;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestBooleanCharSwitchPhi extends SmaliTest {

	@Test
	public void test() {
		assertThat(getClassNodeFromSmali())
				.code()
				.doesNotContain("Type inference failed")
				.doesNotContain("??")
				.contains("? 1 : 0")
				.contains("switch (");
	}
}
