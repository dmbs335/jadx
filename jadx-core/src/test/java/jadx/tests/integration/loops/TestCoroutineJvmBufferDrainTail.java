package jadx.tests.integration.loops;

import org.junit.jupiter.api.Test;

import jadx.api.CommentsLevel;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestCoroutineJvmBufferDrainTail extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		getArgs().setCommentsLevel(CommentsLevel.DEBUG);
		assertThat(getClassNodeFromSmaliWithClsName("io.ktor.utils.io.jvm.javaio.WritingKt"))
				.code()
				.contains("Preserved coroutine buffer-drain resume entry")
				.countString(1, "while (")
				.countString(1, "awaitContent$default(")
				.contains("readTo$default(")
				.doesNotContain("Unsupported multi-entry loop pattern")
				.doesNotContain("Method not decompiled")
				.doesNotContain("JADX ERROR");
	}
}
