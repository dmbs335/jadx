package jadx.tests.integration.switches;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestSwitchOverStringsSharedJoin extends SmaliTest {

	@Test
	public void test() {
		assertThat(getClassNodeFromSmali())
				.code()
				.containsOne("switch (str)")
				.containsOne("case \"a\":")
				.containsOne("case \"b\":")
				.containsOne("consume(str3);")
				.containsOne("return str3;")
				.doesNotContain("Failed to restore switch over string")
				.doesNotContain("Code duplicated in");
	}
}
