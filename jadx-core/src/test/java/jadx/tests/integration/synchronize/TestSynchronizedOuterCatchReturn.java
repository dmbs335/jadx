package jadx.tests.integration.synchronize;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestSynchronizedOuterCatchReturn extends SmaliTest {

	@Test
	public void test() {
		assertThat(getClassNodeFromSmali())
				.code()
				.containsOne("synchronized (this.lock) {")
				.contains("work();")
				.contains("report(th")
				.doesNotContain("Method not decompiled")
				.doesNotContain("missing block")
				.doesNotContain("JADX ERROR");
	}
}
