package jadx.tests.integration.loops;

import org.junit.jupiter.api.Test;

import jadx.api.CommentsLevel;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestCoroutineKtorJvmByteReadLoops extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		getArgs().setCommentsLevel(CommentsLevel.DEBUG);
		assertThat(getClassNodeFromSmaliWithClsName("io.ktor.utils.io.ByteReadChannelOperations_jvmKt"))
				.code()
				.containsOne("Normalize reported coroutine loop completion through state dispatch")
				.containsOne("Split coroutine resume result decision before loop header")
				.contains("do {")
				.contains("while (i < i2)")
				.countString(1, "awaitContent$default(")
				.countString(1, "readByte(")
				.doesNotContain("Failed to insert an additional move for type inference")
				.doesNotContain("Unsupported multi-entry loop pattern")
				.doesNotContain("Region traversal cycle prevented")
				.doesNotContain("Code duplicated")
				.doesNotContain("Recursive region processing")
				.doesNotContain("Method not decompiled")
				.doesNotContain("JADX ERROR");
	}
}
