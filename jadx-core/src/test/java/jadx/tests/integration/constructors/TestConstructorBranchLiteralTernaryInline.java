package jadx.tests.integration.constructors;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestConstructorBranchLiteralTernaryInline extends SmaliTest {

	@Test
	public void test() {
		assertThat(getClassNodeFromSmali())
				.code()
				.doesNotContain("Illegal instructions before constructor call")
				.doesNotContain("call moved to the top of the method")
				.contains("this((i & 1) != 0 ? box((((long) 0) & 4294967295L) | (((long) 0) << 32)) : j);");
	}
}
