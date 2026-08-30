package jadx.tests.integration.loops;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestCoroutineChannelSelectLaunchLoop extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		assertThat(getClassNodeFromSmaliFiles())
				.code()
				.containsOne("while (true)")
				.countString(3, "launch(")
				.countString(2, "getOrNull(tryReceive(")
				.countString(2, "!= null) {")
				.doesNotContain("Unsupported multi-entry loop")
				.doesNotContain("Region traversal cycle")
				.doesNotContain("return Unit.INSTANCE;\n        launch(");
	}
}
