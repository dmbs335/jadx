package jadx.tests.integration.loops;

import org.junit.jupiter.api.Test;

import jadx.api.CommentsLevel;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestCoroutineChannelElementAtOrNullDecisionBody extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		getArgs().setCommentsLevel(CommentsLevel.DEBUG);
		assertThat(searchCls(loadFromSmaliFiles(),
				"kotlinx.coroutines.channels.ChannelsKt__DeprecatedKt"))
						.code()
						.containsOne("Split coroutine resume Boolean projection/branch before loop header")
						.containsOne("Split iterator cleanup from coroutine loop try body")
						.containsOne("Split shared consume cleanup for try-protected coroutine loop")
						.containsOne("do {")
						.countString(8, "catch (Throwable")
						.countString(8, "cancelConsumed")
						.doesNotContain("Object anonymousClass1")
						.doesNotContain("Unsupported multi-entry loop")
						.doesNotContain("Region traversal cycle")
						.doesNotContain("Recursive region processing")
						.doesNotContain("Method not decompiled")
						.doesNotContain("JADX ERROR");
	}
}
