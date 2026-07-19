package jadx.tests.integration.enums;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestEnumNestedSingleUseExpression extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		assertThat(getClassNodeFromSmaliFiles())
				.code()
				.containsOne("public enum TestEnumNestedSingleUseExpression")
				.containsOne("ONE(TestEnumNestedExpressionFactory.listOf(new TestEnumNestedExpressionValue[]{new TestEnumNestedExpressionValue(7)}))")
				.doesNotContain("Enum visitor error")
				.doesNotContain("Failed to restore enum class");
	}
}
