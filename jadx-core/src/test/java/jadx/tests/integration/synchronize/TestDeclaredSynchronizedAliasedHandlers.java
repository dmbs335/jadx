package jadx.tests.integration.synchronize;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestDeclaredSynchronizedAliasedHandlers extends SmaliTest {

	@Test
	public void test() {
		assertThat(getClassNodeFromSmali())
				.code()
				.containsOne("public synchronized void test(")
				.contains(".g--;")
				.containsOne("if (this.j >= 2000 || this.k >= 524288) {")
				.containsOne("this.l = 42L;")
				.containsOne("throw th;")
				.containsOne("throw th2;")
				.doesNotContain("th = th;", "finally");
	}
}
