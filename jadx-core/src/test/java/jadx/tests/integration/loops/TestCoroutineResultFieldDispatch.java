package jadx.tests.integration.loops;

import org.junit.jupiter.api.Test;

import jadx.api.CommentsLevel;
import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestCoroutineResultFieldDispatch extends SmaliTest {

	@Test
	public void testImmediateResultUsesContinuationFieldAtStateDispatch() {
		disableCompilation();
		allowWarnInCode();
		getArgs().setCommentsLevel(CommentsLevel.DEBUG);
		ClassNode cls = getClassNodeFromSmali();
		assertEquals(2, cls.getCode().getCodeStr().split("this.result = ", -1).length - 1);
		assertThat(cls)
				.code()
				.contains("result-field dispatch")
				.contains("this.result = ")
				.containsPattern("this\\.result = objSuspendCall;\\s*continue;")
				.containsPattern("this\\.result = objSuspendCall2;\\s*continue;")
				.doesNotContain("Method not decompiled")
				.doesNotContain("JADX ERROR");
	}
}
