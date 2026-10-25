package jadx.tests.integration.enums;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestEnumValueOfAlias extends SmaliTest {

	@Test
	public void testAliasIsPreserved() {
		assertThat(getClassNodeFromSmali())
				.code()
				.containsOne("public static TestEnumValueOfAlias lookup(String")
				.doesNotContain("JADX ERROR: Failed to restore enum class");
	}
}
