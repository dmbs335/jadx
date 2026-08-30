package jadx.tests.integration.enums;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestEnumNestedWrappedRegisterExpression extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		assertThat(getClassNodeFromSmaliFiles())
				.code()
				.containsOne("public enum TestEnumNestedWrappedRegisterExpression")
				.contains("NestedRegisterFactory.listOf(")
				.contains("NestedRegisterFactory.listOfArray(")
				.contains("NestedRegisterFactory.base()")
				.contains("NestedRegisterFactory.SHARED")
				.doesNotContain("Enum visitor error")
				.doesNotContain("Failed to restore enum class");
	}
}
