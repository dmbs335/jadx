package jadx.tests.integration.others;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestStaticDefaultFinalRead extends SmaliTest {
	@Test
	public void test() {
		assertThat(getClassNodeFromSmali())
				.runDecompiledAutoCheck(this)
				.code()
				.containsOne("JADX WARN: Final modifier removed: static field is read before assignment in class initializer")
				.containsOne("public static int INITIAL = 0;")
				.containsOne("observed = INITIAL;");
	}
}
