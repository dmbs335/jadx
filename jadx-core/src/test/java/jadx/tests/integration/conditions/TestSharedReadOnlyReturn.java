package jadx.tests.integration.conditions;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestSharedReadOnlyReturn extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		assertThat(getClassNodeFromSmali())
				.code()
				.doesNotContain("Code duplicated")
				.countString(2, "return Unit.INSTANCE;")
				.countString(2, "return objSuspended;");
	}
}
