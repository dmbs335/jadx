package jadx.tests.integration.loops;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestEndlessGuardedScanLoop extends SmaliTest {

	@Test
	public void test() {
		assertThat(getClassNodeFromSmali())
				.code()
				.doesNotContain("Code duplicated")
				.countString(1, ".charAt(")
				.containsOne("break;")
				.contains("return z2 ? -i2 : i2;");
	}
}
