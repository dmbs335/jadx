package jadx.tests.integration.enums;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestEnumSingleUsePrimitiveBoxing extends SmaliTest {

	@Test
	public void test() {
		assertThat(getClassNodeFromSmali())
				.code()
				.containsOne("public enum TestEnumSingleUsePrimitiveBoxing")
				.containsOne("ONE(7)")
				.doesNotContain("Enum visitor error")
				.doesNotContain("Failed to restore enum class");
	}
}
