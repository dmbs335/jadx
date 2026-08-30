package jadx.tests.integration.conditions;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestSharedTerminalOut extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		assertThat(getClassNodeFromSmali())
				.code()
				.countString(2, "Selection.removeSelection(spannable);")
				.containsOne("super.onTouchEvent(textView, spannable, motionEvent)")
				.doesNotContain("Code duplicated");
	}
}
