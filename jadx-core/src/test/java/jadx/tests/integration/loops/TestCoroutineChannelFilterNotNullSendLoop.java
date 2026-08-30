package jadx.tests.integration.loops;

import org.junit.jupiter.api.Test;

import jadx.api.CommentsLevel;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestCoroutineChannelFilterNotNullSendLoop extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		getArgs().setCommentsLevel(CommentsLevel.DEBUG);

		assertThat(searchCls(
				loadFromSmaliFiles(),
				"loops.TestCoroutineChannelFilterNotNullSendLoop"))
						.code()
						.containsOne(
								"Normalize protected optional-action has-next completion"
										+ " through state dispatch")
						.containsOne(
								"Split shared protected optional-action latch"
										+ " for coroutine resume")
						.containsOne("if (next != null)")
						.containsOne(".send(next,")
						.containsOne("while (it.hasNext(")
						.contains("cancelConsumed(")
						.doesNotContain("Unsupported multi-entry loop pattern")
						.doesNotContain("Region traversal cycle prevented")
						.doesNotContain("Recursive region processing prevented")
						.doesNotContain("Code duplicated in")
						.doesNotContain("Method not decompiled")
						.doesNotContain("JADX ERROR");
	}
}
