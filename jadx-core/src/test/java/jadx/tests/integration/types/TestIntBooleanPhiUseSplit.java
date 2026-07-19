package jadx.tests.integration.types;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestIntBooleanPhiUseSplit extends SmaliTest {

	@Test
	public void test() {
		assertThat(getClassNodeFromSmali())
				.code()
				.doesNotContain("Type inference failed")
				.doesNotContain("??")
				.contains("acceptBoolean(i2 != 0);")
				.contains("acceptInt(i2 | 8);")
				.contains("objArr[i2] = obj;");
	}
}
