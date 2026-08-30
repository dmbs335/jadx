package jadx.tests.integration.loops;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestArrayMaxLoop extends SmaliTest {

	@Test
	public void test() {
		assertThat(getClassNodeFromSmali())
				.code()
				.containsOne("while (true)")
				.doesNotContain("Method not decompiled")
				.doesNotContain("Recursive region processing prevented")
				.doesNotContain("Code restructure failed")
				.doesNotContain("JADX ERROR");
	}
}
