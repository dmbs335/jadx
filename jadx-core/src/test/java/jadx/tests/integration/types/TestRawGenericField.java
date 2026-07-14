package jadx.tests.integration.types;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestRawGenericField extends SmaliTest {

	@Test
	public void test() {
		// T is declared by the enclosing generic method in the original anonymous class.
		disableCompilation();
		assertThat(getClassNodeFromSmali())
				.code()
				.doesNotContain("Type inference incomplete")
				.contains("this.callback.apply((T) obj)");
	}
}
