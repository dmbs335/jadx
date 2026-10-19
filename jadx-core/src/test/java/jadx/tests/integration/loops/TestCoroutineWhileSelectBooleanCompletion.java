package jadx.tests.integration.loops;

import org.junit.jupiter.api.Test;

import jadx.api.CommentsLevel;
import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestCoroutineWhileSelectBooleanCompletion extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		getArgs().setCommentsLevel(CommentsLevel.DEBUG);
		ClassNode cls = getClassNodeFromSmali();
		assertThat(cls)
				.code()
				.containsOne("Normalize whileSelect Boolean completion through state dispatch")
				.containsOne("this.result = obj;")
				.containsOne("a.getCOROUTINE_SUSPENDED()")
				.contains("while (true)")
				.contains("selectImplementation.g(this)")
				.doesNotContain("this =")
				.doesNotContain("Unsupported multi-entry loop")
				.doesNotContain("Region traversal cycle")
				.doesNotContain("Method not decompiled")
				.doesNotContain("JADX ERROR");
		String code = cls.getCode().getCodeStr();
		org.assertj.core.api.Assertions.assertThat(code.indexOf("this.result = obj;"))
				.isLessThan(code.indexOf("while (true)"));
		org.assertj.core.api.Assertions.assertThat(code.indexOf("a.getCOROUTINE_SUSPENDED()"))
				.isLessThan(code.indexOf("while (true)"));
	}
}
