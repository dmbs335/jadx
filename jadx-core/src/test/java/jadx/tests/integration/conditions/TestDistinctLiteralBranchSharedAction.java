package jadx.tests.integration.conditions;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestDistinctLiteralBranchSharedAction extends SmaliTest {

	@Test
	public void test() {
		assertThat(getClassNodeFromSmali())
				.code()
				.countString(3, "sharedAction(")
				.countString(3, "moveAction(")
				.doesNotContain("Code duplicated")
				.doesNotContain("Method not decompiled");
	}
}
