package jadx.tests.integration.loops;

import org.junit.jupiter.api.Test;

import jadx.api.CommentsLevel;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestProtectedPollingDelayStateDispatch extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		allowWarnInCode();
		getArgs().setCommentsLevel(CommentsLevel.DEBUG);

		assertThat(getClassNodeFromSmali())
				.code()
				.countString(1, "Normalize protected polling delay completion through state dispatch")
				.countString(1, "DelayKt.delay")
				.contains("if (jMax <= 0) {")
				.contains("continue;")
				.doesNotContain("if (jMax <= 0 && DelayKt.delay")
				.doesNotContain("Recursive region processing")
				.doesNotContain("Type inference failed")
				.doesNotContain("Unsupported multi-entry loop pattern")
				.doesNotContain("Method not decompiled")
				.doesNotContain("JADX ERROR");
	}
}
