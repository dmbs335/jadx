package jadx.tests.integration.loops;

import org.junit.jupiter.api.Test;

import jadx.api.CommentsLevel;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestOptionalCoroutineProjectionFallbackJoin extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		getArgs().setCommentsLevel(CommentsLevel.DEBUG);

		assertThat(getClassNodeFromSmali())
				.code()
				.countString(1, "Merge optional coroutine projection fallback through nullable result decision")
				.countString(2, "fallback(")
				.contains("objInvoke = null;")
				.contains("return objInvoke == null ? fallback(obj2) : objInvoke;")
				.doesNotContain("Code duplicated")
				.doesNotContain("Recursive region processing")
				.doesNotContain("Type inference failed")
				.doesNotContain("Method not decompiled")
				.doesNotContain("JADX ERROR");
	}
}
