package jadx.tests.integration.others;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestStaticConstantIndirectRead extends SmaliTest {
	@Test
	public void test() {
		assertThat(getClassNodeFromSmali())
				.runDecompiledAutoCheck(this)
				.code()
				.contains("observed = readInitial()");
	}
}
