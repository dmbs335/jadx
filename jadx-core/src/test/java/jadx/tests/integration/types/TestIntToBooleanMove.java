package jadx.tests.integration.types;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestIntToBooleanMove extends SmaliTest {

	@Test
	public void test() {
		assertThat(getClassNodeFromSmali())
				.code()
				.doesNotContain("??")
				.doesNotContain("Type inference failed", "JADX WARN", "JADX ERROR")
				.contains("loop", ": while (true)", "break loop")
				.contains("!= 0");
	}
}
