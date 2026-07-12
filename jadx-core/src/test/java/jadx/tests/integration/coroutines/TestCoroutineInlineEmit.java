package jadx.tests.integration.coroutines;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestCoroutineInlineEmit extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		allowWarnInCode();
		assertThat(getClassNodeFromSmaliFiles("TestCoroutineInlineEmit"))
				.code()
				.doesNotContain("Method not decompiled")
				.doesNotContain("Code restructure failed")
				.doesNotContain("Recursive region processing prevented")
				.doesNotContain("JADX ERROR");
	}
}
