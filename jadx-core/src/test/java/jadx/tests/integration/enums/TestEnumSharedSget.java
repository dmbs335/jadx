package jadx.tests.integration.enums;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestEnumSharedSget extends SmaliTest {

	@Test
	public void test() {
		assertThat(getClassNodeFromSmali())
				.code()
				.doesNotContain("Enum visitor error")
				.doesNotContain("Failed to restore enum class")
				.contains("public enum TestEnumSharedSget")
				.contains("ONE(Integer.TYPE, 7)")
				.contains("TWO(Integer.TYPE, 7)")
				.contains("THREE(Integer.TYPE, ONE.getValue() | TWO.getValue())");
	}
}
