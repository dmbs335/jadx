package jadx.tests.integration.others;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestNonJavaArrayBoundSignature extends SmaliTest {

	@Test
	public void test() {
		assertThat(getClassNodeFromSmali())
				.code()
				.containsOne("public static <R> R forwardArrayTypeVariableBound(")
				.containsOne("Object[] objArr, Supplier<? extends R> supplier) {")
				.containsOne("Removed non-Java array-bound type parameter from method signature: C : java.lang.Object[]")
				.doesNotContain("JADX WARN");
	}
}
