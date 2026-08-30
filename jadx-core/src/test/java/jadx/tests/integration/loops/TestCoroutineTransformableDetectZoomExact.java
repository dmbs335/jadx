package jadx.tests.integration.loops;

import org.junit.jupiter.api.Test;

import jadx.api.CommentsLevel;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestCoroutineTransformableDetectZoomExact extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		getArgs().setCommentsLevel(CommentsLevel.DEBUG);

		assertThat(getClassNodeFromSmaliFiles())
				.code()
				.contains("awaitPointerEvent")
				.contains("calculateZoom")
				.contains("calculateRotation")
				.contains("calculatePan")
				.contains("Normalize three-state pointer-input completions through shared state dispatch")
				.containsOne("PointerEventKt.positionChanged")
				.containsOne("pointerInputChange.consume();")
				.doesNotContain("Code duplicated in")
				.doesNotContain("TRACE three-state pointer coroutine")
				.doesNotContain("TRACE many-state entry")
				.doesNotContain("TRACE process")
				.doesNotContain("Region traversal cycle prevented")
				.doesNotContain("Recursive region processing prevented")
				.doesNotContain("Type inference failed")
				.doesNotContain("Unsupported multi-entry loop pattern")
				.doesNotContain("Method not decompiled")
				.doesNotContain("JADX ERROR")
				.doesNotContain("??");
	}
}
