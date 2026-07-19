package jadx.tests.integration.switches;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestSwitchOverStringsLoopSharedAction extends SmaliTest {

	@Test
	public void test() {
		assertThat(getClassNodeFromSmali())
				.code()
				.containsOne("switch (str)")
				.containsOne("case \"constraint\":")
				.containsOne("case \"guideline\":")
				.containsOne("case \"stop\":")
				.doesNotContain("Failed to restore switch over string");
	}
}
