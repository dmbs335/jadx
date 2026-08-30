package jadx.tests.integration.types;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestInheritedGenericField extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		assertThat(getClassNodeFromSmaliFiles("types", "TestInheritedGenericField", "Child"))
				.code()
				.doesNotContain("Type inference incomplete")
				.doesNotContain("Consumer<A>")
				.contains("this.callback.accept(this.value)");
	}
}
