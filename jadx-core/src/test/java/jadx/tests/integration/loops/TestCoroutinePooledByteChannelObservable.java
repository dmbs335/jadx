package jadx.tests.integration.loops;

import org.junit.jupiter.api.Test;

import jadx.api.CommentsLevel;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestCoroutinePooledByteChannelObservable extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		getArgs().setCommentsLevel(CommentsLevel.DEBUG);

		assertThat(searchCls(
				loadFromSmaliFiles(),
				"io.ktor.client.utils.ByteChannelUtilsKt$observable$1"))
						.code()
						.containsOne("Normalize pooled byte-channel observable completions through state dispatch")
						.contains("while (true)")
						.contains("if (iIntValue > 0)")
						.countString(3, "if (closedCause == null)")
						.countString(3, "if (j3 == j)")
						.countString(4, ".onProgress(")
						.containsOne("continue;")
						.doesNotContain("JADX WARN:")
						.doesNotContain("Unsupported multi-entry loop pattern")
						.doesNotContain("Method not decompiled")
						.doesNotContain("JADX ERROR")
						.doesNotContain("Type inference failed");
	}
}
