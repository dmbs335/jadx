package jadx.tests.integration.constructors;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestConstructorFluentBuilderBranchInline extends SmaliTest {

	@Test
	public void test() {
		assertThat(getClassNodeFromSmaliFiles())
				.code()
				.doesNotContain("Illegal instructions before constructor call")
				.contains("super(ConstructorFluentBuilderBranchHelper.make(i).append("
						+ "i > 1 ? \"s\" : \"\").toString());");
	}
}
