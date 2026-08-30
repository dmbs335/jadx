package jadx.tests.integration.loops;

import org.junit.jupiter.api.Test;

import jadx.api.CommentsLevel;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestTryProtectedChannelForwardCompletion extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		getArgs().setCommentsLevel(CommentsLevel.DEBUG);

		assertThat(searchCls(
				loadFromSmaliFiles(),
				"loops.TestTryProtectedChannelForwardCompletion"))
						.code()
						.containsOne(
								"Normalize try-protected channel forward completion"
										+ " through state dispatch")
						.contains("hasNext(")
						.contains("send(")
						.contains("cancelConsumed(")
						.doesNotContain("??")
						.doesNotContain("Unsupported multi-entry loop pattern")
						.doesNotContain("Region traversal cycle prevented")
						.doesNotContain("Recursive region processing prevented")
						.doesNotContain("Code duplicated in")
						.doesNotContain("Method not decompiled")
						.doesNotContain("JADX ERROR");
	}
}
