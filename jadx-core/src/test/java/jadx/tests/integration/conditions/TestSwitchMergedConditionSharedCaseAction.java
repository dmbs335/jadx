package jadx.tests.integration.conditions;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestSwitchMergedConditionSharedCaseAction extends SmaliTest {

	@Test
	public void test() {
		assertThat(getClassNodeFromSmali())
				.code()
				.containsOne("case '-':")
				.containsOne("&&")
				.doesNotContain("Code restructure failed")
				.doesNotContain("Method not decompiled")
				.doesNotContain("JADX ERROR");
	}
}
