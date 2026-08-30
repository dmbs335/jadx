package jadx.tests.integration.enums;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestEnumPhiRegionExpression extends SmaliTest {

	@Test
	public void test() {
		assertThat(getClassNodeFromSmaliFiles())
				.code()
				.containsOne("public enum TestEnumPhiRegionExpression")
				.containsOne("ONE($enumArg$ONE())")
				.containsOne("private static String $enumArg$ONE()")
				.countString(1, "getPrimary()")
				.countString(1, "getFallback()")
				.doesNotContain("static {")
				.doesNotContain("Enum visitor error")
				.doesNotContain("Failed to restore enum class");
	}
}
