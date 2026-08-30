package jadx.tests.integration.conditions;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestInlinedBranchMoveCodegenNoOp extends SmaliTest {

	@Test
	public void test() {
		assertThat(getClassNodeFromSmali())
				.code()
				.doesNotContain("Code restructure failed")
				.doesNotContain("r0 = r0")
				.doesNotContain("UnsupportedOperationException")
				.containsOne("if (!new File(this.orgPath).exists() || isEmpty(this.newPath))")
				.containsOne("consume(\".mp4\");");
	}
}
