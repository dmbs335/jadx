package jadx.tests.integration.loops;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestEndlessLoopSharedReturn extends SmaliTest {

	@Test
	public void test() {
		assertThat(getClassNodeFromSmali())
				.code()
				.contains("while (true) {")
				.containsLines(5,
						"if (i >= i2) {",
						indent() + "break;",
						"}",
						"int i6 = bArr[i];")
				.containsLines(5,
						"if (i6 != i3) {",
						indent() + "break;",
						"}",
						"i++;")
				.countString(3, "break;")
				.doesNotContain("Method not decompiled")
				.doesNotContain("JADX ERROR");
	}
}
