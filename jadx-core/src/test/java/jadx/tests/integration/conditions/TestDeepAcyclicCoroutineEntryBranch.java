package jadx.tests.integration.conditions;

import java.util.List;

import org.junit.jupiter.api.Test;

import jadx.api.CommentsLevel;
import jadx.core.dex.attributes.AType;
import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestDeepAcyclicCoroutineEntryBranch extends SmaliTest {
	@Override
	protected void runChecks(List<ClassNode> classes) {
		// The synthetic constructor stores captured fields before invoking super.
		// Java cannot preserve that order, but the coroutine body must still decompile.
		assertThat(classes.stream().flatMap(cls -> cls.getMethods().stream())
				.filter(mth -> mth.contains(AType.JADX_ERROR))
				.toList())
				.hasSize(1)
				.allMatch(mth -> mth.isConstructor());
	}

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
				.contains("Method not decompiled: o4.b.<init>")
				.doesNotContain("Code restructure failed")
				.doesNotContain("missing block")
				.contains("JADX ERROR");
	}
}
