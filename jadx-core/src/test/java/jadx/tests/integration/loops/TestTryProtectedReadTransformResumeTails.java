package jadx.tests.integration.loops;

import org.junit.jupiter.api.Test;

import jadx.api.CommentsLevel;
import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestTryProtectedReadTransformResumeTails extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		allowWarnInCode();
		getArgs().setCommentsLevel(CommentsLevel.DEBUG);

		ClassNode cls = getClassNodeFromSmaliFiles(
				"io.ktor.util",
				"TestTryProtectedReadTransformResumeTails",
				"DeflaterKt");
		cls.getCode();

		assertThat(cls.searchMethodByShortName("c"))
				.code()
				.containsOne("while (!byteReadChannel.isClosedForRead())")
				.containsOne("readAvailable(")
				.containsOne("intValue() <= 0")
				.containsOne("continue;")
				.doesNotContain("Region traversal cycle prevented")
				.doesNotContain("Recursive region processing prevented")
				.doesNotContain("Unsupported multi-entry loop pattern")
				.doesNotContain("Method not decompiled")
				.doesNotContain("JADX ERROR");
	}
}
