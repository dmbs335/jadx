package jadx.tests.integration.others;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestInstanceFinalIndirectRead extends SmaliTest {
	@Test
	public void test() {
		assertThat(getClassNodeFromSmali())
				.runDecompiledAutoCheck(this)
				.code()
				.containsOne("JADX WARN: Final modifier removed: instance field is read before assignment in constructor")
				.contains("observed = readInitial()");
	}
}
