package jadx.tests.integration.constructors;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestConstructorInvokeFinalStaticPrefixInline extends SmaliTest {

	@Test
	public void test() {
		assertThat(getClassNodeFromSmaliFiles())
				.code()
				.doesNotContain("Illegal instructions before constructor call")
				.contains("super(factory().first(CodingErrorAction.REPLACE).second(CodingErrorAction.REPLACE));")
				.countString(2, "CodingErrorAction.REPLACE");
	}
}
