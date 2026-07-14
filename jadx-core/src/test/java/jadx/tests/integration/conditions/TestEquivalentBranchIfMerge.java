package jadx.tests.integration.conditions;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestEquivalentBranchIfMerge extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		assertThat(getClassNodeFromSmali())
				.code()
				.contains("first(i)")
				.contains("second(i)")
				.contains("prepare(i)")
				.contains("update()")
				.contains("suspended()")
				.contains("try {")
				.contains("catch (Exception")
				.doesNotContain("Method not decompiled")
				.doesNotContain("Code restructure failed")
				.doesNotContain("Code duplicated")
				.doesNotContain("JADX ERROR");
	}
}
