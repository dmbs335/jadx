package jadx.tests.integration.loops;

import org.junit.jupiter.api.Test;

import jadx.api.CommentsLevel;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestCoroutineDelaySharedInitialResume extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		getArgs().setCommentsLevel(CommentsLevel.DEBUG);
		assertThat(getClassNodeFromSmali())
				.code()
				.contains("Normalize coroutine delay completion through state dispatch")
				.contains("Object coroutine_suspended = getCOROUTINE_SUSPENDED();")
				.countString(1, "getCOROUTINE_SUSPENDED();")
				.contains("if (i == 4)")
				.countString(2, "throw new IllegalStateException();")
				.contains("while (true)")
				.countString(4, "throwOnFailure(obj);")
				.countString(3, "obj = delay(this);")
				.countString(3, "if (obj == coroutine_suspended)")
				.countString(1, "receive(this)")
				.doesNotContain("Code duplicated")
				.doesNotContain("Region traversal cycle prevented")
				.doesNotContain("Unsupported multi-entry loop pattern")
				.doesNotContain("??");
	}
}
