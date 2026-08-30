package jadx.tests.integration.loops;

import org.junit.jupiter.api.Test;

import jadx.api.CommentsLevel;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestCoroutineDiscardResumeLoopJoin extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		getArgs().setCommentsLevel(CommentsLevel.DEBUG);
		assertThat(getClassNodeFromSmaliWithClsName("io.ktor.utils.io.ByteReadChannelOperationsKt"))
				.code()
				.contains("Split coroutine resume buffer-consume tail before loop header")
				.contains("while (")
				.contains("isClosedForRead()")
				.countString(1, "awaitContent$default(")
				.countString(1, "getAvailableForRead(")
				.doesNotContain("while (true)")
				.doesNotContain("Unsupported multi-entry loop pattern")
				.doesNotContain("Code duplicated")
				.doesNotContain("Method not decompiled")
				.doesNotContain("JADX ERROR");
	}
}
