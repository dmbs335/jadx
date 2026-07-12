package jadx.tests.integration.trycatch;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestSynchronizedHandlerLoop extends SmaliTest {

	@Test
	public void test() {
		assertThat(getClassNodeFromSmali())
				.code()
				.containsOne("synchronized (")
				.doesNotContain("Method not decompiled")
				.doesNotContain("Bottom block not found for handler")
				.doesNotContain("JADX ERROR");
	}
}
