package jadx.tests.integration.constructors;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestConstructorFinalSingletonConditionalReadBlocked extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		allowWarnInCode();
		assertThat(getClassNodeFromSmaliFiles())
				.code()
				.contains("Illegal instructions before constructor call")
				.contains("= ConstructorFinalSingletonConditionalRead.INSTANCE;");
	}
}
