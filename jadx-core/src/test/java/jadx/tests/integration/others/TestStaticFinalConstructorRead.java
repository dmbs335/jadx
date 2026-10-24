package jadx.tests.integration.others;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestStaticFinalConstructorRead extends SmaliTest {
	@Test
	public void test() {
		assertThat(getClassNodeFromSmali())
				.runDecompiledAutoCheck(this)
				.code()
				.containsOne("JADX WARN: Final modifier removed: static field is read through a call before assignment")
				.contains("observed = INITIAL");
	}
}
