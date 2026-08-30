package jadx.tests.integration.loops;

import org.junit.jupiter.api.Test;

import jadx.api.CommentsLevel;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestCoroutineSharedLoopEntryPreHeader extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		getArgs().setCommentsLevel(CommentsLevel.DEBUG);

		assertThat(searchCls(
				loadFromSmaliFiles(),
				"io.ktor.network.sockets.CIOWriterKt$attachForWritingDirectImpl$1"))
						.code()
						.containsOne("Merge 2 coroutine entries through shared loop pre-header")
						.contains("this.$channel.isClosedForRead()")
						.contains("ByteReadChannelOperationsKt.getAvailableForRead(this.$channel)")
						.contains("selectorManager.select(selectable2, selectInterest, this)")
						.doesNotContain("JADX WARN:")
						.doesNotContain("Unsupported multi-entry loop pattern")
						.doesNotContain("Method not decompiled")
						.doesNotContain("JADX ERROR")
						.doesNotContain("Type inference failed");
	}
}
