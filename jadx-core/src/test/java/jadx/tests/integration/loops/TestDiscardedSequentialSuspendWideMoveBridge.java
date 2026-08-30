package jadx.tests.integration.loops;

import org.junit.jupiter.api.Test;

import jadx.api.CommentsLevel;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestDiscardedSequentialSuspendWideMoveBridge extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		getArgs().setCommentsLevel(CommentsLevel.DEBUG);

		assertThat(getClassNodeFromSmali())
				.code()
				.countString(
						1,
						"Route discarded sequential suspend move bridge through state dispatch: 1 -> 2")
				.countString(1, "this.label = 2;")
				.contains("case 1:")
				.contains("ResultKt.throwOnFailure(obj);")
				.doesNotContain("move-bridge probe")
				.doesNotContain("Code duplicated")
				.doesNotContain("Recursive region processing")
				.doesNotContain("Type inference failed")
				.doesNotContain("Method not decompiled")
				.doesNotContain("JADX ERROR");
	}
}
