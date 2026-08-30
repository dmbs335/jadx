package jadx.tests.integration.synchronize;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestSynchronizedDuplicatedFinallyCleanup extends SmaliTest {

	@Test
	public void test() {
		String code = getClassNodeFromSmali().getCode().toString();
		assertThat(code)
				.doesNotContain("Method not decompiled")
				.doesNotContain("missing block")
				.doesNotContain("Code duplicated")
				.doesNotContain("JADX ERROR")
				.doesNotContain("th = th;")
				.containsOne("synchronized (this.lock) {")
				.containsOne("obj2 = obj;")
				.countString(3, "throw th");

		int synchronizedStart = code.indexOf("synchronized (this.lock) {");
		int synchronizedEnd = findBlockEnd(code, synchronizedStart);
		org.assertj.core.api.Assertions.assertThat(synchronizedEnd)
				.as("synchronized block end")
				.isGreaterThan(synchronizedStart);
		org.assertj.core.api.Assertions.assertThat(code.substring(synchronizedStart, synchronizedEnd))
				.as("cleanup must not widen the synchronized section")
				.doesNotContain("release(");
		org.assertj.core.api.Assertions.assertThat(code.indexOf("release(", synchronizedEnd))
				.as("normal cleanup after the synchronized section")
				.isGreaterThan(synchronizedEnd);
	}

	private static int findBlockEnd(String code, int blockStart) {
		int openBrace = code.indexOf('{', blockStart);
		int depth = 0;
		for (int i = openBrace; i < code.length(); i++) {
			char ch = code.charAt(i);
			if (ch == '{') {
				depth++;
			} else if (ch == '}' && --depth == 0) {
				return i;
			}
		}
		return -1;
	}
}
