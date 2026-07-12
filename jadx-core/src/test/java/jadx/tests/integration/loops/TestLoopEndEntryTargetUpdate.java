package jadx.tests.integration.loops;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestLoopEndEntryTargetUpdate extends SmaliTest {

	@Test
	public void test() {
		assertThat(getClassNodeFromSmali())
				.code()
				.contains("(i & 1) == 0 ? i - 1 : i - 2")
				.contains("i2 -= 2;")
				.doesNotContain("Method not decompiled")
				.doesNotContain("JADX ERROR");
	}
}
