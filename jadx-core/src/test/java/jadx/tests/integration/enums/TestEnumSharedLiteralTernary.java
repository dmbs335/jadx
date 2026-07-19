package jadx.tests.integration.enums;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestEnumSharedLiteralTernary extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		assertThat(getClassNodeFromSmali())
				.code()
				.containsOne("public enum TestEnumSharedLiteralTernary")
				.doesNotContain("Enum visitor error")
				.doesNotContain("Failed to restore enum class");
	}
}
