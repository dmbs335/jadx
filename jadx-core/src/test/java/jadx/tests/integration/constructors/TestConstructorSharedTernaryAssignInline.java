package jadx.tests.integration.constructors;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestConstructorSharedTernaryAssignInline extends SmaliTest {

	@Test
	public void test() {
		assertThat(getClassNodeFromSmali())
				.code()
				.doesNotContain("Illegal instructions before constructor call")
				.doesNotContain("call moved to the top of the method")
				.contains("this((z = (i & 1) != 0 ? false : z), (i & 2) != 0 ? z : z2);");
	}
}
