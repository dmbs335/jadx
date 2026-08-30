package jadx.tests.integration.loops;

import org.junit.jupiter.api.Test;

import jadx.api.CommentsLevel;
import jadx.api.JadxInternalAccess;
import jadx.core.dex.nodes.ClassNode;
import jadx.core.dex.nodes.RootNode;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestCoroutineTwoStateFrameRequestLoop extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		getArgs().setCommentsLevel(CommentsLevel.DEBUG);

		ClassNode cls = getClassNodeFromSmaliFiles(
				getTestPkg(), getTestName(), getTestName());
		assertThat(cls)
				.code()
				.containsOne("Normalize two-state frame-request loop completions"
						+ " through a single-entry loop pre-header")
				.contains("while (true)")
				.contains("awaitFrameRequest(")
				.contains("withFrameNanos(")
				.contains("new Callback(this, list")
				.doesNotContain("new Callback()")
				.doesNotContain("JADX WARN:")
				.doesNotContain("Unsupported multi-entry loop pattern")
				.doesNotContain("Region traversal cycle prevented")
				.doesNotContain("Recursive region processing prevented")
				.doesNotContain("Code duplicated")
				.doesNotContain("Method not decompiled")
				.doesNotContain("JADX ERROR")
				.doesNotContain("??");

		RootNode root = JadxInternalAccess.getRoot(jadxDecompiler);
		ClassNode callback = root.resolveClass(getTestPkg() + ".Callback");
		assertThat(callback)
				.code()
				.contains("class Callback")
				.contains("Callback(TestCoroutineTwoStateFrameRequestLoop")
				.contains("this.recomposer = testCoroutineTwoStateFrameRequestLoop;")
				.contains("this.toRecompose = list;")
				.contains("this.toApply = list2;")
				.contains("this.signal = produceFrameSignal;");
	}
}
