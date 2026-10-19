package jadx.tests.integration.loops;

import org.junit.jupiter.api.Test;

import jadx.api.CommentsLevel;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestTryProtectedCoroutineReceiveFlushLoop extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		getArgs().setCommentsLevel(CommentsLevel.DEBUG);

		var cls = getClassNodeFromSmali();
		assertThat(cls)
				.code()
				.containsOne("Normalize try-protected coroutine suspend completion through state dispatch")
				.contains("while (true) {")
				.contains("this.label = 0;")
				.containsLines(4,
						"if (byteWriteChannelAccess$getOutput$p4.flushAndClose(this) != coroutine_suspended) {",
						"    throw th2;",
						"}",
						"return coroutine_suspended;")
				.containsLines(3,
						"if (byteWriteChannelAccess$getOutput$p5.flushAndClose(this) == coroutine_suspended) {",
						"    return coroutine_suspended;",
						"}")
				.doesNotContain("Unsupported multi-entry loop pattern")
				.doesNotContain("Recursive region processing prevented")
				.doesNotContain("Code duplicated")
				.doesNotContain("Method not decompiled")
				.doesNotContain("JADX ERROR");
	}
}
