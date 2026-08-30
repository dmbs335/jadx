package jadx.tests.integration.loops;

import org.junit.jupiter.api.Test;

import jadx.api.CommentsLevel;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestCoroutineReadPacketResumeDecisionJoin extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		getArgs().setCommentsLevel(CommentsLevel.DEBUG);
		assertThat(getClassNodeFromSmaliWithClsName("io.ktor.utils.io.ByteReadChannelOperationsKt"))
				.code()
				.contains("Split coroutine readPacket resume decision before loop header")
				.countString(1, "while (")
				.countString(1, "awaitContent$default(")
				.countString(2, "readTo(")
				.countString(2, "transferTo(")
				.countString(1, "long j")
				.contains("getRemaining(byteReadChannel.getReadBuffer()) > ((long) i2) - buffer.getSize()")
				.contains("readTo(buffer, ((long) i2) - buffer.getSize())")
				.contains("throw new EOFException")
				.doesNotContain("while (true)")
				.doesNotContain("Unsupported multi-entry loop pattern")
				.doesNotContain("Region traversal cycle prevented")
				.doesNotContain("Code duplicated")
				.doesNotContain("Method not decompiled")
				.doesNotContain("JADX ERROR");
	}
}
