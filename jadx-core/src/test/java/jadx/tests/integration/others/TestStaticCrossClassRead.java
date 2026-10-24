package jadx.tests.integration.others;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestStaticCrossClassRead extends SmaliTest {
	@Test
	public void test() {
		assertThat(getClassNodeFromSmaliFiles())
				.runDecompiledAutoCheck(this)
				.code()
				.containsOne("JADX WARN: Final modifier removed: static field is read indirectly before assignment")
				.contains("observed = StaticReadHelper.read()");
	}
}
