package jadx.tests.integration.loops;

import org.junit.jupiter.api.Test;

import jadx.api.CommentsLevel;
import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestTryProtectedIteratorMappingCompletion extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		allowWarnInCode();
		getArgs().setCommentsLevel(CommentsLevel.DEBUG);
		ClassNode cls = getClassNodeFromSmali();
		assertThat(cls)
				.code()
				.contains("Split try-protected iterator resume mapping and unify nullable loop entry")
				.containsOne("while (objMapResult == null)")
				.containsOne("if (!it.hasNext())")
				.containsOne("it = this.iterator;")
				.containsOne("objMapResult = mapResult(obj2);")
				.containsOne("objMapResult = mapResult(objSuspendMap);")
				.countString(2, "catch (Exception")
				.doesNotContain("Recursive region processing prevented")
				.doesNotContain("Region traversal cycle prevented")
				.doesNotContain("Unsupported multi-entry loop pattern")
				.doesNotContain("Code duplicated")
				.doesNotContain("Method not decompiled")
				.doesNotContain("JADX ERROR");
		String code = cls.getCode().getCodeStr();
		int restore = code.indexOf("it = this.iterator;");
		int resumeTry = code.indexOf("try {", restore);
		int resumeMap = code.indexOf("objMapResult = mapResult(obj2);", resumeTry);
		org.assertj.core.api.Assertions.assertThat(restore)
				.isGreaterThanOrEqualTo(0)
				.isLessThan(resumeTry);
		org.assertj.core.api.Assertions.assertThat(resumeTry)
				.isLessThan(resumeMap);
	}
}
