package jadx.tests.integration.synchronize;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestGlideSingleRequestResourceReadyExact extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		assertThat(getClassNodeFromSmali())
				.code()
				.contains("synchronized (this.requestLock)")
				.countString(2, "this.engine.release(")
				.countString(2, "if (resource == null)")
				.doesNotContain("missing block")
				.doesNotContain("Method not decompiled")
				.doesNotContain("JADX ERROR");
	}
}
