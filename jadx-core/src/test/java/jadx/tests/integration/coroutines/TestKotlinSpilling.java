package jadx.tests.integration.coroutines;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestKotlinSpilling extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		allowWarnInCode();
		assertThat(getClassNodeFromSmaliFiles("TestKotlinSpilling"))
				.code()
				.doesNotContain("Not initialized variable")
				.contains("SpillingKt.nullOutSpilledVariable(null)")
				.contains("identity(");
	}
}
