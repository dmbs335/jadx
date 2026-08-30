package jadx.tests.integration.loops;

import org.junit.jupiter.api.Test;

import jadx.api.CommentsLevel;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestCoroutineObfuscatedOuterSwitchCompleteStateMap extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		getArgs().setCommentsLevel(CommentsLevel.DEBUG);
		assertThat(getClassNodeFromSmali())
				.code()
				.containsOne(
						"Normalize complete local obfuscated coroutine state map"
								+ " through dispatcher")
				.containsOne("while (true) {")
				.contains("refresh(")
				.contains("fetch(")
				.contains("publish(")
				.contains("delay(")
				.countString(4, "continue;")
				.doesNotContain("Unsupported multi-entry loop pattern")
				.doesNotContain("Region traversal cycle prevented")
				.doesNotContain("Code duplicated in")
				.doesNotContain("Found unreachable blocks")
				.doesNotContain("Method not decompiled")
				.doesNotContain("JADX ERROR");
	}
}
