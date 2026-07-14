package jadx.tests.integration.inner;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestAnonymousConcreteMethodType extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		assertThat(getClassNodeFromSmaliFiles())
				.code()
				.doesNotContain("Type inference failed", "??")
				.containsOne("capture(new AnonymousClass1());");
	}
}
