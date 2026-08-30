package jadx.tests.integration.enums;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestEnumSharedSingletonInvokeArgs extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		assertThat(getClassNodeFromSmaliFiles())
				.code()
				.containsOne("public enum TestEnumSharedSingletonInvokeArgs")
				.containsOne(
						"ONE(new Holder(Helper.format(1), \"\"), new Holder(Helper.format(2), Helper.format(3, new Object[]{Helper.INSTANCE.getName()})), 4, 5)")
				.containsOne(
						"TWO(new Holder(Helper.format(6), \"\"), new Holder(Helper.format(7), Helper.format(8, new Object[]{Helper.INSTANCE.getName()})), 9, 10)")
				.doesNotContain("Enum visitor error")
				.doesNotContain("Failed to restore enum class");
	}
}
