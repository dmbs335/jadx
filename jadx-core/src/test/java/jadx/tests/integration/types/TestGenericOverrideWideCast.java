package jadx.tests.integration.types;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestGenericOverrideWideCast extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();

		assertThat(getClassNodeFromSmaliFiles())
				.code()
				.doesNotContain("??")
				.doesNotContain("Type inference failed")
				.containsOne("Model get() {")
				.containsOne("(Model) ((Parcelable) Factory.create())")
				.containsOne("(Model) Factory.createParcelable()");
	}
}
