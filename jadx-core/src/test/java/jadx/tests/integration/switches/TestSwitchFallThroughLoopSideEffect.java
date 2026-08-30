package jadx.tests.integration.switches;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestSwitchFallThroughLoopSideEffect extends SmaliTest {

	@Test
	public void test() {
		assertThat(getClassNodeFromSmali())
				.code()
				.containsOne("for (int i = 0; i < str.length(); i++)")
				.doesNotContain("Code duplicated in")
				.doesNotContain("Method not decompiled");
	}
}
