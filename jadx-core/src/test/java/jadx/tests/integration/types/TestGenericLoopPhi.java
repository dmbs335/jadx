package jadx.tests.integration.types;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestGenericLoopPhi extends SmaliTest {

	@Test
	public void test() {
		assertThat(getClassNodeFromSmaliFiles())
				.code()
				.doesNotContain("??")
				.doesNotContain("Type inference failed")
				.contains(" = (T) (Object) it.next();")
				.containsOne("public static <T, R extends Comparable<? super R>> T maxByOrNull(");
	}
}
