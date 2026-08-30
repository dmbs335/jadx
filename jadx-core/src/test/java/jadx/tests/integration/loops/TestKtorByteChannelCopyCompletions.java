package jadx.tests.integration.loops;

import org.junit.jupiter.api.Test;

import jadx.api.CommentsLevel;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestKtorByteChannelCopyCompletions extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		allowWarnInCode();
		getArgs().setRunDebugChecks(false);
		getArgs().setCommentsLevel(CommentsLevel.DEBUG);
		assertThat(getClassNodeFromSmaliWithClsName("io.ktor.utils.io.ByteReadChannelOperationsKt"))
				.code()
				.countString(2, "Normalize single coroutine completion through state dispatch")
				.countString(2, "while (true)")
				.contains("awaitContent")
				.contains("transferTo")
				.contains("readTo")
				.contains("flush")
				.doesNotContain("Region traversal cycle prevented")
				.doesNotContain("Recursive region processing prevented")
				.doesNotContain("Unsupported multi-entry loop pattern")
				.doesNotContain("Method not decompiled")
				.doesNotContain("JADX ERROR")
				.doesNotContain("??");
	}
}
