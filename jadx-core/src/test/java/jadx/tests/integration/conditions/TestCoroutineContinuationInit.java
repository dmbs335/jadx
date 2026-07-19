package jadx.tests.integration.conditions;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestCoroutineContinuationInit extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		assertThat(getClassNodeFromSmaliFiles())
				.code()
				.doesNotContain("Code duplicated")
				.countString(2, "new AnonymousClass1(continuation)")
				.countString(2, "new DetachedContinuation(continuation)")
				.countString(2, "new OwnerContinuation")
				.containsOne("consume(anonymousClass1);");
	}
}
