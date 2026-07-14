package jadx.tests.integration.coroutines;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestCoroutineGenericField extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		allowWarnInCode();
		assertThat(getClassNodeFromSmaliFiles("TestCoroutineGenericField"))
				.code()
				.containsOne("!= null")
				.doesNotContain("Type inference incomplete")
				.doesNotContain("JADX ERROR");
	}
}
