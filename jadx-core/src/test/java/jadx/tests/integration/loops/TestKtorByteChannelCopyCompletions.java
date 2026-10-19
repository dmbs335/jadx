package jadx.tests.integration.loops;

import java.util.List;

import org.junit.jupiter.api.Test;

import jadx.api.CommentsLevel;
import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestKtorByteChannelCopyCompletions extends SmaliTest {
	@Override
	protected void runChecks(List<ClassNode> classes) {
		// Both overloads currently merge primitive values into reference-typed Java variables.
		// Fail explicitly until those SSA lifetimes can be separated without changing control flow.
		org.assertj.core.api.Assertions.assertThat(classes.get(0).root().getErrorsCounter().getErrorCount()).isEqualTo(2);
	}

	@Test
	public void test() {
		disableCompilation();
		allowWarnInCode();
		getArgs().setRunDebugChecks(false);
		getArgs().setCommentsLevel(CommentsLevel.DEBUG);
		assertThat(getClassNodeFromSmaliWithClsName("io.ktor.utils.io.ByteReadChannelOperationsKt"))
				.code()
				.contains("Incompatible local assignment type: int for io.ktor.utils.io.ByteWriteChannel")
				.contains("Incompatible local assignment type: long for io.ktor.utils.io.ByteReadChannel")
				.contains(
						"Method not decompiled: io.ktor.utils.io.ByteReadChannelOperationsKt.copyTo(io.ktor.utils.io.ByteReadChannel, io.ktor.utils.io.ByteWriteChannel, kotlin.coroutines.Continuation")
				.contains(
						"Method not decompiled: io.ktor.utils.io.ByteReadChannelOperationsKt.copyTo(io.ktor.utils.io.ByteReadChannel, io.ktor.utils.io.ByteWriteChannel, long, kotlin.coroutines.Continuation");
	}
}
