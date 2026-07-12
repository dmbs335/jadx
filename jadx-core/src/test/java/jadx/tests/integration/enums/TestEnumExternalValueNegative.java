package jadx.tests.integration.enums;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestEnumExternalValueNegative extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		allowWarnInCode();
		assertThat(getClassNodeFromSmali())
				.code()
				.containsOne("Enum visitor error")
				.containsOne("Failed to restore enum class");
	}
}
