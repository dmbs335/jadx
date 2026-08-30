package jadx.tests.integration.conditions;

import org.junit.jupiter.api.Test;

import jadx.api.CommentsLevel;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestCoroutineResumeCounterLoopTail extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		getArgs().setCommentsLevel(CommentsLevel.DEBUG);
		assertThat(getClassNodeFromSmaliFiles())
				.code()
				.containsOne("Split coroutine resume atomic-counter loop tail")
				.containsOne("do {")
				.containsOne("} while (decrementAndGet() != 0);")
				.countString(2, "receive(")
				.countString(3, "consume(")
				.countString(3, "decrementAndGet(")
				.doesNotContain("Unsupported multi-entry loop")
				.doesNotContain("Region traversal cycle")
				.doesNotContain("Code duplicated")
				.doesNotContain("Method not decompiled")
				.doesNotContain("JADX ERROR");
	}
}
