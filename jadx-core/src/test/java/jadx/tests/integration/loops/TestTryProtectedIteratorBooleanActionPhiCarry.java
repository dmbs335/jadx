package jadx.tests.integration.loops;

import org.junit.jupiter.api.Test;

import jadx.api.CommentsLevel;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestTryProtectedIteratorBooleanActionPhiCarry extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		getArgs().setCommentsLevel(CommentsLevel.DEBUG);

		assertThat(searchCls(
				loadFromSmaliFiles(),
				"loops.TestTryProtectedIteratorBooleanActionPhiCarry"))
						.code()
						.containsOne(
								"Split try-protected iterator Boolean action resume tail before loop")
						.contains("while (true)")
						.contains("hasNext()")
						.contains("next()")
						.contains("action(")
						.contains("booleanValue()")
						.contains("cleanupSuspend(")
						.contains("catch (Throwable")
						.doesNotContain("Unsupported multi-entry loop pattern")
						.doesNotContain("Recursive region processing prevented")
						.doesNotContain("Method not decompiled")
						.doesNotContain("JADX ERROR");
	}
}
