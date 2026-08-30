package jadx.tests.integration.trycatch;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestNullableCastCatch extends SmaliTest {

	@Test
	public void test() {
		disableCompilation(); // Android Bundle and Log are not on the test compiler classpath
		assertThat(getClassNodeFromSmali())
				.code()
				.containsOne("bundle.get(\"r\")")
				.containsOne("cls.cast(obj)")
				.containsOne("catch (ClassCastException e)")
				.doesNotContain("Method not decompiled")
				.doesNotContain("Code restructure failed")
				.doesNotContain("JADX ERROR");
	}
}
