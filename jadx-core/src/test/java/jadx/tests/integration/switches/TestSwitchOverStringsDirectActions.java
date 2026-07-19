package jadx.tests.integration.switches;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestSwitchOverStringsDirectActions extends SmaliTest {

	@Test
	public void test() {
		assertThat(getClassNodeFromSmali())
				.code()
				.containsOne("switch (str)")
				.containsOne("case \"identity\":")
				.containsOne("case \"gzip\":")
				.containsOne("case \"snappy\":")
				.containsOne("case \"deflate\":")
				.containsOne("throw new IllegalStateException();")
				.doesNotContain("Failed to restore switch over string");
	}
}
