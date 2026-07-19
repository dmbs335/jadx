package jadx.tests.integration.constructors;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestConstructorMoveLiteralArgInline extends SmaliTest {

	@Test
	public void test() {
		assertThat(getClassNodeFromSmali())
				.code()
				.doesNotContain("Illegal instructions before constructor call")
				.doesNotContain("call moved to the top of the method")
				.contains("this(20);");
	}
}
