package jadx.tests.integration.coroutines;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestCoroutineSharedFinally extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		allowWarnInCode(); // coroutine state dispatch can retain a duplicated setup block
		assertThat(getClassNodeFromSmaliFiles("TestCoroutineSharedFinally"))
				.code()
				.contains("finally")
				.countString(2, "cleanup(")
				.countString(2, "use(")
				.doesNotContain("Method not decompiled")
				.doesNotContain("Code restructure failed")
				.doesNotContain("JADX ERROR");
	}
}
