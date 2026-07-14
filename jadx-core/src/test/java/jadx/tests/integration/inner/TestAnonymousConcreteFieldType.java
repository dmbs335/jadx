package jadx.tests.integration.inner;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestAnonymousConcreteFieldType extends SmaliTest {

	@Test
	public void test() {
		assertThat(getClassNodeFromSmaliFiles())
				.code()
				.doesNotContain("Type inference failed", "??")
				.containsOne("AnonymousClass1 anonymousClass1 = new AnonymousClass1();")
				.containsOne("this.callback = anonymousClass1;")
				.containsOne("use(anonymousClass1);");
	}
}
