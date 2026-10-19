package jadx.tests.integration.switches;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestSwitchOverStringsNestedCaseLoops extends SmaliTest {

	@Test
	public void test() {
		assertThat(getClassNodeFromSmali())
				.code()
				.containsOne("switch (str)")
				.containsOne("case \"concat\":")
				.containsOne("case \"filter\":")
				.countString(3, "for (")
				.doesNotContain("break;")
				.doesNotContain("Code decompiled incorrectly");
	}
}
