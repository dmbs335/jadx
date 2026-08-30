package jadx.tests.integration.conditions;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestCoroutineFirstResumeJoin extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		allowWarnInCode(); // continuation allocation retains one harmless duplicated setup block
		assertThat(getClassNodeFromSmaliFiles())
				.code()
				.containsOne("load(obj, anonymousClass1)")
				.contains("objLoad.hashCode()")
				.contains("delete(anonymousClass1)")
				.containsOne("save(obj, anonymousClass1)")
				.doesNotContain("Code restructure failed")
				.doesNotContain("Method not decompiled");
	}
}
