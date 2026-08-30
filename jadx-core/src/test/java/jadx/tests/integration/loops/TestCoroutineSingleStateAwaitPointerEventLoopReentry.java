package jadx.tests.integration.loops;

import org.junit.jupiter.api.Test;

import jadx.api.CommentsLevel;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestCoroutineSingleStateAwaitPointerEventLoopReentry extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		allowWarnInCode();
		getArgs().setCommentsLevel(CommentsLevel.DEBUG);
		assertThat(getClassNodeFromSmali(
				"loops/TestCoroutineSingleStateAwaitPointerEventLoopReentry",
				"androidx.compose.foundation.gestures.TapGestureDetectorKt"))
						.code()
						.containsOne("Normalize 1 awaitPointerEvent completions through label dispatch")
						.containsOne("while (true)")
						.containsOne(".getPressed()) {")
						.containsOne("label = 0;")
						.containsOne("continue;")
						.doesNotContain("Type inference failed")
						.doesNotContain("Recursive region processing prevented")
						.doesNotContain("Region traversal cycle prevented")
						.doesNotContain("Unsupported multi-entry loop pattern")
						.doesNotContain("Method not decompiled")
						.doesNotContain("JADX ERROR");
	}
}
