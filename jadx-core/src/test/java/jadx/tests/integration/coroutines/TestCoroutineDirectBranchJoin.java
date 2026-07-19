package jadx.tests.integration.coroutines;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestCoroutineDirectBranchJoin extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		assertThat(getClassNodeFromSmali())
				.code()
				.doesNotContain("Code duplicated")
				.containsOne("sideEffect(99);");
	}
}
