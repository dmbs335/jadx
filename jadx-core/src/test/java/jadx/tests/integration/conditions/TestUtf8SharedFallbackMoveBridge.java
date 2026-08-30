package jadx.tests.integration.conditions;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestUtf8SharedFallbackMoveBridge extends SmaliTest {

	@Test
	public void test() {
		assertThat(getClassNodeFromSmali())
				.code()
				.doesNotContain("Method not decompiled")
				.doesNotContain("Code restructure failed")
				.doesNotContain("JADX ERROR")
				.contains("& 192")
				.containsOne("} else if ((b >> 4) == -2) {")
				.containsOne("} else if ((b >> 3) == -2) {")
				.countString(4, "i += i4;")
				.countString(4, "i4 = 1;")
				.countString(4, "i4 = 2;")
				.countString(2, "i4 = 3;")
				.containsOne("i4 = 4;")
				.containsOne("i++;")
				.containsLines(3,
						"} else {",
						indent() + "cArr[i5] = 65533;",
						indent() + "i++;",
						indent() + "i5++;",
						"}")
				.containsOne("return");
	}
}
