package jadx.tests.integration.loops;

import org.junit.jupiter.api.Test;

import jadx.api.CommentsLevel;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestCoroutineIteratorCollectionSelectorLoop extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		getArgs().setCommentsLevel(CommentsLevel.DEBUG);
		assertThat(getClassNodeFromSmali())
				.code()
				.doesNotContain("Unsupported multi-entry loop pattern")
				.doesNotContain("Region traversal cycle prevented")
				.doesNotContain("Recursive region detected")
				.doesNotContain("Method not decompiled")
				.doesNotContain("??")
				.contains("Normalize iterator collection selector completion through state dispatch")
				.contains("Split coroutine resume collection-add tail before iterator loop header")
				.containsOne("while (true) {")
				.containsOne(".contains(obj)")
				.containsOne(".send(")
				.containsOne("return Unit.INSTANCE;");
	}
}
