package jadx.tests.integration.conditions;

import org.junit.jupiter.api.Test;

import jadx.api.CommentsLevel;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestDeepAcyclicCoroutineEntryBranch extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		allowWarnInCode();
		getArgs().setCommentsLevel(CommentsLevel.DEBUG);

		assertThat(searchCls(loadFromSmaliFiles(), "o4.b"))
				.code()
				.containsOne("if (i == 0) {")
				.containsOne("if (i2 == 0) {")
				.containsOne("return bVar.invoke(((t) yVar7).c());")
				.containsOne("return bVar2.invoke(((t) yVar8).c());")
				.containsOne("if (i == 1) {")
				.containsOne("if (i2 == 1) {")
				.containsOne("if (i == 2) {")
				.containsOne("if (i2 == 2) {")
				.containsOne("if (i == 3) {")
				.containsOne("if (i2 == 3) {")
				.doesNotContain("Method not decompiled")
				.doesNotContain("Code restructure failed")
				.doesNotContain("missing block")
				.doesNotContain("JADX ERROR");
	}
}
