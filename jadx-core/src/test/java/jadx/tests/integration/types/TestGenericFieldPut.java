package jadx.tests.integration.types;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestGenericFieldPut extends SmaliTest {

	@Test
	public void test() {
		assertThat(getClassNodeFromSmaliFiles())
				.code()
				.doesNotContain("Type inference failed")
				.doesNotContain("??")
				.contains("box.value = String.valueOf(obj);");
	}
}
