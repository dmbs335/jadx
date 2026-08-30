package jadx.tests.integration.switches;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestSwitchFallThroughChainOrder extends SmaliTest {

	@Test
	public void test() {
		assertThat(getClassNodeFromSmali())
				.code()
				.containsOne("case 1:")
				.containsOne("case 2:")
				.containsOne("case 3:")
				.containsOne("case 4:")
				.doesNotContain("Can't fix incorrect switch cases order")
				.doesNotContain("Code duplicated in");
	}
}
