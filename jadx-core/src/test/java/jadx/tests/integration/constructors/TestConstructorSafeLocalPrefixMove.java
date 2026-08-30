package jadx.tests.integration.constructors;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestConstructorSafeLocalPrefixMove extends SmaliTest {

	@Test
	public void test() {
		assertThat(getClassNodeFromSmaliFiles())
				.code()
				.doesNotContain("call moved to the top of the method")
				.contains("super(i);")
				.contains("this.first = i3;")
				.contains("this.second = i3;");
	}
}
