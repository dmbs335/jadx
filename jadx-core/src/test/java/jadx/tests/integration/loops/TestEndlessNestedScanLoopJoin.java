package jadx.tests.integration.loops;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestEndlessNestedScanLoopJoin extends SmaliTest {

	@Test
	public void test() {
		assertThat(getClassNodeFromSmali())
				.code()
				.doesNotContain("Code duplicated")
				.doesNotContain("variable i might not have been initialized")
				.contains("break loop0;")
				.countString(1, "return string;");
	}
}
