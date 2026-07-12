package jadx.tests.integration.enums;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestEnumNamedConstantClasses extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		assertThat(getClassNodeFromSmaliFiles())
				.code()
				.containsOne("public enum TestEnumNamedConstantClasses")
				.contains("ALPHA")
				.contains("BETA")
				.doesNotContain("class ALPHA")
				.doesNotContain("class BETA")
				.doesNotContain("Unknown enum class pattern")
				.doesNotContain("Failed to restore enum");
	}
}
