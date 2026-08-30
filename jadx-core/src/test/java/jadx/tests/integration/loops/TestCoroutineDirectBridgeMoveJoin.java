package jadx.tests.integration.loops;

import org.junit.jupiter.api.Test;

import jadx.api.CommentsLevel;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestCoroutineDirectBridgeMoveJoin extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		getArgs().setCommentsLevel(CommentsLevel.DEBUG);

		assertThat(searchCls(
				loadFromSmaliFiles(),
				"fixtures.sdk.domain.mediaframe"
						+ ".MediaFrameUploadUseCase$invoke$1"))
								.code()
								.contains("case 10:")
								.contains("ResultKt.throwOnFailure(obj);")
								.contains("if (!it")
								.doesNotContain("JADX WARN:")
								.doesNotContain("Unsupported multi-entry loop pattern")
								.doesNotContain("Method not decompiled")
								.doesNotContain("JADX ERROR");
	}
}
