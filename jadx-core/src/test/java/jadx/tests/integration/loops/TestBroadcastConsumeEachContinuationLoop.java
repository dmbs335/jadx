package jadx.tests.integration.loops;

import org.junit.jupiter.api.Test;

import jadx.api.CommentsLevel;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestBroadcastConsumeEachContinuationLoop extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		getArgs().setCommentsLevel(CommentsLevel.DEBUG);
		assertThat(getClassNodeFromSmaliFiles(
				"kotlinx.coroutines.channels",
				"TestBroadcastConsumeEachContinuationLoop",
				"ChannelsKt__DeprecatedKt"))
						.code()
						.doesNotContain("Unsupported multi-entry loop pattern")
						.doesNotContain("Region traversal cycle prevented")
						.doesNotContain("Recursive region processing prevented")
						.doesNotContain("Code duplicated in")
						.doesNotContain("Failed to insert an additional move for type inference")
						.doesNotContain("Type inference failed")
						.contains("Normalize BroadcastChannel consumeEach completion through state dispatch")
						.contains("Split BroadcastChannel method argument from iterator resume lifetime")
						.containsOne("do {")
						.containsOne("} while (")
						.contains("hasNext(")
						.contains("cancel$default(");
	}
}
