package jadx.tests.integration.loops;

import org.junit.jupiter.api.Test;

import jadx.api.CommentsLevel;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestCoroutineNullableSerializeResultLoopTail extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		getArgs().setCommentsLevel(CommentsLevel.DEBUG);

		assertThat(getClassNodeFromSmaliFiles())
				.code()
				.containsOne("Split coroutine resume nullable result tail before loop header")
				.containsOne("while (it.hasNext())")
				// Obfuscated call names must not be part of the CFG matcher contract.
				.containsOne("contentConverter.a(")
				.contains("logger.i(")
				.doesNotContain("Unsupported multi-entry loop pattern")
				.doesNotContain("Region traversal cycle prevented")
				.doesNotContain("Recursive region processing prevented")
				.doesNotContain("Code duplicated")
				.doesNotContain("Method not decompiled")
				.doesNotContain("JADX ERROR")
				.doesNotContain("??");
	}
}
