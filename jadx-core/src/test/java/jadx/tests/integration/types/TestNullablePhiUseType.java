package jadx.tests.integration.types;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestNullablePhiUseType extends SmaliTest {

	@Test
	public void test() {
		allowWarnInCode(); // the shared null intentionally has two incompatible upstream interface uses
		disableCompilation();

		assertThat(getClassNodeFromSmaliFiles())
				.code()
				.doesNotContain("Type inference failed for: r1")
				.doesNotContain("?? r1")
				.contains("useText(");
	}
}
