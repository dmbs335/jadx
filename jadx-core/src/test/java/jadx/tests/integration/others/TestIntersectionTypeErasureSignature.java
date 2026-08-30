package jadx.tests.integration.others;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestIntersectionTypeErasureSignature extends SmaliTest {

	@Test
	public void test() {
		assertThat(getClassNodeFromSmali())
				.code()
				.containsOne("class TestIntersectionTypeErasureSignature<T, B extends ArrayList<T> & Serializable, C extends B>")
				.containsOne("public void acceptClass(B b) {")
				.containsOne("public static <E, R extends List<E> & Serializable> void acceptMethod(R r) {")
				.containsOne("public B returnClass() {")
				.containsOne("public C returnChained() {")
				.containsOne("public static <E, R extends List<E> & Serializable> R returnMethod() {")
				.containsOne("public static <M extends Map<?, ?>, R> R forwardTypeVariableBound(")
				.containsOne("M m, Supplier<? extends R> supplier) {")
				.containsOne("Removed non-Java type-variable bound from method signature: M : R")
				.doesNotContain("JADX WARN");
	}
}
