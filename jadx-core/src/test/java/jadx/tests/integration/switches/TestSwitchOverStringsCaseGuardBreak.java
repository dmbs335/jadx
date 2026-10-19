package jadx.tests.integration.switches;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestSwitchOverStringsCaseGuardBreak extends SmaliTest {

	@Test
	public void test() {
		assertThat(getClassNodeFromSmali())
				.code()
				.containsOne("case \"concat\":")
				.containsOne("case \"filter\":")
				.containsOne("break;")
				.containsOne("throw new IllegalArgumentException")
				.doesNotContain("Code decompiled incorrectly");
	}
}
