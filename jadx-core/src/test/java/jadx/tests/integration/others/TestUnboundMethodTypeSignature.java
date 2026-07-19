package jadx.tests.integration.others;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestUnboundMethodTypeSignature extends SmaliTest {

	@Test
	public void test() {
		assertThat(getClassNodeFromSmali())
				.code()
				.containsOne("class TestUnboundMethodTypeSignature<B>")
				.containsOne("public void accept(String str,"
						+ " Consumer<? super TestUnboundMethodTypeSignature> consumer) {")
				.containsOne("public void acceptKnown(String str) {")
				.containsOne("public String returnValue() {")
				.containsOne("JADX INFO: Replace unresolved generic type in method signature: T -> java.lang.String")
				.containsOne("JADX INFO: Replace unresolved generic type in method signature: B -> java.lang.String")
				.containsOne("JADX INFO: Replace unresolved generic return type in method signature: T -> java.lang.String")
				.doesNotContain("JADX WARN: Incorrect types in method signature");
	}
}
