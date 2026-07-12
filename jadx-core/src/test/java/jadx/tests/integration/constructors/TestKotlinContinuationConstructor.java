package jadx.tests.integration.constructors;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestKotlinContinuationConstructor extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		assertThat(getClassNodeFromSmali())
				.code()
				.doesNotContain("'super' call moved")
				.doesNotContain("Illegal instructions before constructor call")
				.doesNotContain("JADX ERROR");
	}
}
