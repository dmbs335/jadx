package jadx.tests.integration.conditions;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestCoroutineMoveJoin extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		assertThat(getClassNodeFromSmaliFiles())
				.code()
				.containsOne("while (true)")
				.countString(2, "consume(obj);")
				.doesNotContain("Unsupported multi-entry loop")
				.doesNotContain("Region traversal cycle");
	}
}
