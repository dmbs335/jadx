package jadx.tests.integration.enums;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestEnumRepeatedSelectorExpression extends SmaliTest {

	@Test
	public void test() {
		assertThat(getClassNodeFromSmaliFiles())
				.code()
				.containsOne("public enum TestEnumRepeatedSelectorExpression")
				.containsOne("ONE($enumArg$ONE())")
				.containsOne("private static String $enumArg$ONE()")
				.containsOne("int selector = TestEnumRepeatedSelectorHelper.MAPPING[TestEnumRepeatedSelectorHelper.getIndex()]")
				.countString(1, "getIndex()")
				.doesNotContain("Enum visitor error")
				.doesNotContain("Failed to restore enum class");
	}
}
