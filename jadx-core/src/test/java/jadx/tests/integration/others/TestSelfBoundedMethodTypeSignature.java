package jadx.tests.integration.others;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestSelfBoundedMethodTypeSignature extends SmaliTest {

	@Test
	public void test() {
		assertThat(getClassNodeFromSmali())
				.code()
				.containsOne("public static <K extends Comparable<? super K>, V> V of(K k, V v) {")
				.doesNotContain("Incorrect types in method signature")
				.doesNotContain("JADX WARN");
	}
}
