package jadx.tests.integration.loops;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestTryCatchLoopSharedRethrow extends SmaliTest {

	@Test
	public void test() {
		assertThat(getClassNodeFromSmali())
				.code()
				.containsOne("catch (Throwable th)")
				.containsOne("throw th;")
				.doesNotContain("break;")
				.doesNotContain("JADX WARN")
				.doesNotContain("JADX ERROR");
	}
}
