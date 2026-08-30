package jadx.tests.integration.conditions;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestSwitchMergedShortCircuitSharedPhi extends SmaliTest {

	@Test
	public void test() {
		assertThat(getClassNodeFromSmali())
				.code()
				.countString(2, "i2 == i3 || i2 == i4")
				.containsOne("i2 == i3 || classMatcher(i2, i4)")
				.doesNotContain("Code restructure failed")
				.doesNotContain("JADX ERROR");
	}
}
