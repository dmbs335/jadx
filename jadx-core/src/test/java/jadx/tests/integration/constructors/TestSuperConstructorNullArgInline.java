package jadx.tests.integration.constructors;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestSuperConstructorNullArgInline extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		assertThat(getClassNodeFromSmali())
				.code()
				.doesNotContain("Illegal instructions before constructor call")
				.doesNotContain("call moved to the top of the method")
				.contains("super((Object) null);");
	}
}
