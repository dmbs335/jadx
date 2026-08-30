package jadx.tests.integration.switches;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestSwitchTerminalCaseSharedNormalTail extends SmaliTest {

	@Test
	public void test() {
		assertThat(getClassNodeFromSmali())
				.code()
				.containsOne("switch (i2)")
				.containsOne("consume(i5);")
				.contains("throw new IllegalArgumentException();")
				.doesNotContain("Code duplicated in")
				.doesNotContain("Method not decompiled");
	}
}
