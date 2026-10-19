package jadx.tests.integration.loops;

import org.junit.jupiter.api.Test;

import jadx.api.CommentsLevel;
import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestCoroutineInlinedComparatorIteratorLoop extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		getArgs().setCommentsLevel(CommentsLevel.DEBUG);
		ClassNode cls = getClassNodeFromSmali();
		assertThat(cls)
				.code()
				.contains("Normalize counted iterator states [2] through state dispatch")
				.containsOne("do {")
				.contains("comparator.compare")
				.containsOne("this.result = obj;")
				.doesNotContain("Unsupported multi-entry loop")
				.doesNotContain("Region traversal cycle")
				.doesNotContain("Code duplicated")
				.doesNotContain("Method not decompiled")
				.doesNotContain("JADX ERROR");
		String code = cls.getCode().getCodeStr();
		org.assertj.core.api.Assertions.assertThat(code.indexOf("this.result = obj;"))
				.isLessThan(code.indexOf("do {"));
	}
}
