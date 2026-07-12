package jadx.tests.integration.enums;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestEnumSingleUseGetter extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		assertThat(getClassNodeFromSmaliFiles())
				.code()
				.containsOne("public enum TestEnumSingleUseGetter")
				.contains("ONE(Helper.INSTANCE.getType() == Mode.TYPE24 ? \"A\" : \"B\")")
				.contains("TWO(Helper.INSTANCE.getType() == Mode.TYPE24 ? \"C\" : \"D\")")
				.doesNotContain("Enum visitor error")
				.doesNotContain("Failed to restore enum class");
	}
}
