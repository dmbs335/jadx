package jadx.tests.integration.conditions;

import org.junit.jupiter.api.Test;

import jadx.api.CommentsLevel;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestCoroutineResumeMoveNullableAddTail extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		allowWarnInCode();
		getArgs().setCommentsLevel(CommentsLevel.DEBUG);
		assertThat(getClassNodeFromSmaliFiles())
				.code()
				.contains("Split coroutine resume move/nullable-add tail")
				.contains("while (it.hasNext())")
				.contains("GraphItemResponseKt.mapToEntity(")
				.contains("arrayList.add(")
				.doesNotContain("Unsupported multi-entry loop")
				.doesNotContain("Region traversal cycle")
				.doesNotContain("Method not decompiled")
				.doesNotContain("JADX ERROR");
	}
}
