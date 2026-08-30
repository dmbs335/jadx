package jadx.tests.integration.loops;

import org.junit.jupiter.api.Test;

import jadx.api.CommentsLevel;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestCoroutineChannelLastLoop extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		getArgs().setCommentsLevel(CommentsLevel.DEBUG);
		assertThat(getClassNodeFromSmaliWithClsName(
				"kotlinx.coroutines.channels.ChannelsKt__DeprecatedKt"))
						.code()
						.containsOne("Split coroutine resume Boolean projection/branch before loop header")
						.containsOne("while (true)")
						.countString(2, "hasNext(")
						.countString(2, "NoSuchElementException(\"ReceiveChannel is empty.\")")
						.contains("cancelConsumed")
						.doesNotContain("Unsupported multi-entry loop")
						.doesNotContain("Region traversal cycle")
						.doesNotContain("Recursive region processing")
						.doesNotContain("Code duplicated")
						.doesNotContain("Method not decompiled")
						.doesNotContain("JADX ERROR");
	}
}
