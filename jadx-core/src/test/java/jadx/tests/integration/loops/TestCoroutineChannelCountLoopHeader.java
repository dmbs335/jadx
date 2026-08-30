package jadx.tests.integration.loops;

import org.junit.jupiter.api.Test;

import jadx.api.CommentsLevel;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestCoroutineChannelCountLoopHeader extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		getArgs().setCommentsLevel(CommentsLevel.DEBUG);
		assertThat(getClassNodeFromSmaliWithClsName(
				"kotlinx.coroutines.channels.ChannelsKt__DeprecatedKt"))
						.code()
						.contains("Split coroutine resume Boolean projection/branch before loop header")
						.contains("while (true)")
						.countString(1, "hasNext(")
						.doesNotContain("Unsupported multi-entry loop pattern")
						.doesNotContain("Region traversal cycle prevented")
						.doesNotContain("Recursive region processing prevented")
						.doesNotContain("Method not decompiled")
						.doesNotContain("JADX ERROR");
	}
}
