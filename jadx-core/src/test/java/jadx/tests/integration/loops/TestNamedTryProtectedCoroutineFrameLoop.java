package jadx.tests.integration.loops;

import org.junit.jupiter.api.Test;

import jadx.api.CommentsLevel;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestNamedTryProtectedCoroutineFrameLoop extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		getArgs().setCommentsLevel(CommentsLevel.DEBUG);

		assertThat(searchCls(loadFromSmaliFiles(),
				"androidx.compose.foundation.gestures.UpdatableAnimationState"))
						.code()
						.containsOne(
								"Split named try-protected coroutine resume effect before loop")
						.contains("while (!Companion.isZeroish(this.e))")
						.contains("withFrameNanos(")
						.doesNotContain("Region traversal cycle prevented")
						.doesNotContain("Unsupported multi-entry loop pattern")
						.doesNotContain("Method not decompiled")
						.doesNotContain("JADX ERROR");
	}
}
