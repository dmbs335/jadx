package jadx.tests.integration.loops;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestSharedIncrementLoopContinuation extends SmaliTest {

	@Test
	public void test() {
		assertThat(getClassNodeFromSmali())
				.code()
				.containsOne("while (i2 < size)")
				.containsOne("if (obj instanceof Integer)")
				.containsOne("if (num.intValue() == i)")
				.containsOne("list2.add(num);")
				.containsOne("break;")
				.countString(1, "i2++;")
				.doesNotContain("continue;")
				.doesNotContain("Method not decompiled")
				.doesNotContain("JADX ERROR");
	}
}
