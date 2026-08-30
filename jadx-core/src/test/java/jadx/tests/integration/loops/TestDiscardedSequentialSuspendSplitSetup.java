package jadx.tests.integration.loops;

import org.junit.jupiter.api.Test;

import jadx.api.CommentsLevel;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestDiscardedSequentialSuspendSplitSetup extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		getArgs().setCommentsLevel(CommentsLevel.DEBUG);

		assertThat(getClassNodeFromSmali())
				.code()
				.countString(
						1,
						"Route discarded sequential suspend completion through state dispatch: 1 -> 2")
				.countString(1, "function1.invoke(this)")
				.contains("Function1 function1 = this.second;")
				.contains("case 1:")
				.contains("ResultKt.throwOnFailure(obj);")
				.doesNotContain("Code duplicated")
				.doesNotContain("Recursive region processing")
				.doesNotContain("Method not decompiled")
				.doesNotContain("JADX ERROR");
	}
}
