package jadx.tests.integration.loops;

import org.junit.jupiter.api.Test;

import jadx.api.CommentsLevel;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestCoroutineDirectBridgeTypedRestore extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		getArgs().setCommentsLevel(CommentsLevel.DEBUG);

		assertThat(searchCls(
				loadFromSmaliFiles(),
				"io.ktor.client.plugins.sse.DefaultClientSSESession$doReconnection$2"))
						.code()
						.containsOne("Normalize coroutine direct bridge through typed restore input")
						.contains("if (i != 3)")
						.contains("objCheckResponse = SSEKt.checkResponse")
						.contains("if (objCheckResponse != coroutine_suspended)")
						.contains("obj = objCheckResponse")
						.contains("httpResponse = (HttpResponse) this.L$2;")
						.contains("ResultKt.throwOnFailure(obj);")
						.doesNotContain("JADX WARN:")
						.doesNotContain("Unsupported multi-entry loop pattern")
						.doesNotContain("Method not decompiled")
						.doesNotContain("JADX ERROR")
						.doesNotContain("Type inference failed");
	}
}
