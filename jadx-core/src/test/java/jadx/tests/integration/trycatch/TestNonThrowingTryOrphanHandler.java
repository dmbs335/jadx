package jadx.tests.integration.trycatch;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestNonThrowingTryOrphanHandler extends SmaliTest {

	@Test
	public void test() {
		assertThat(getClassNodeFromSmali())
				.code()
				.containsOne("catch (NumberFormatException")
				.doesNotContain("Unreachable blocks removed")
				.doesNotContain("Method not decompiled")
				.doesNotContain("JADX ERROR");
	}
}
