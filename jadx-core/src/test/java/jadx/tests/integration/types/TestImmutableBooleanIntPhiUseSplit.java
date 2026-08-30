package jadx.tests.integration.types;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestImmutableBooleanIntPhiUseSplit extends SmaliTest {

	@Test
	public void test() {
		assertThat(getClassNodeFromSmali())
				.code()
				.doesNotContain("Types fix failed")
				.doesNotContain("Method not decompiled")
				.doesNotContain("??")
				.contains("objArr[i]")
				.contains("zArr[0] = i != 0");
	}
}
