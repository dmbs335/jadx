package jadx.tests.integration.others;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestStaticConstantOverwrite extends SmaliTest {
	@Test
	public void test() {
		assertThat(getClassNodeFromSmali())
				.runDecompiledAutoCheck(this)
				.code()
				.containsOne("JADX WARN: Final modifier removed: encoded static value is overwritten in class initializer")
				.containsOne("public static int INITIAL = 5;")
				.containsOne("observed = INITIAL;");
	}
}
