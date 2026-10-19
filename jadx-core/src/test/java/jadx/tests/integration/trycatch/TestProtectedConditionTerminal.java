package jadx.tests.integration.trycatch;

import org.junit.jupiter.api.Test;

import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.SmaliTest;

public class TestProtectedConditionTerminal extends SmaliTest {
	public static Integer parse(int mode) {
		if (mode == 2) {
			throw new IllegalArgumentException("protected parse");
		}
		return mode == 1 ? null : 42;
	}

	public static void after(int mode) {
		if (mode == 3) {
			throw new IllegalArgumentException("unprotected continuation");
		}
	}

	public static void missing() {
		throw new IllegalArgumentException("protected missing value");
	}

	public static int recover() {
		return 99;
	}

	@Test
	public void test() {
		forceDecompiledCheck();
		ClassNode cls = getClassNodeFromSmali();
		jadx.tests.api.utils.assertj.JadxAssertions.assertThat(cls).code()
				.doesNotContain("Incorrect try/catch scope")
				.doesNotContain("Incomplete try/catch coverage")
				.doesNotContain("Code decompiled incorrectly");
	}
}
