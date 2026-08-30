package jadx.tests.integration.loops;

import org.junit.jupiter.api.Test;

import jadx.api.CommentsLevel;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestCoroutinePooledByteChannelFanOut extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		getArgs().setCommentsLevel(CommentsLevel.DEBUG);

		assertThat(searchCls(
				loadFromSmaliFiles(),
				"io.ktor.util.ByteChannelsKt$split$1"))
						.code()
						.containsOne("Normalize pooled byte-channel fan-out completions through state dispatch")
						.contains("while (true)")
						.contains("if (this.$this_split.isClosedForRead())")
						.contains("if (iIntValue > 0)")
						.contains("Object objAwaitAll = AwaitKt.awaitAll")
						.containsOne("obj = objAwaitAll;")
						.containsOne("continue;")
						.doesNotContain("JADX WARN:")
						.doesNotContain("Unsupported multi-entry loop pattern")
						.doesNotContain("Method not decompiled")
						.doesNotContain("JADX ERROR")
						.doesNotContain("Type inference failed");
	}
}
