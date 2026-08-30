package jadx.tests.integration.types;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestBoundedGenericField extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		assertThat(getClassNodeFromSmaliFiles("types", "TestBoundedGenericField", "User"))
				.code()
				.doesNotContain("Type inference incomplete")
				.doesNotContain("Consumer<C>")
				.contains("this.box.callback.accept(this.value)");
	}
}
