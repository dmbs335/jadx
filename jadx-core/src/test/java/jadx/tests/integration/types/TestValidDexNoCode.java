package jadx.tests.integration.types;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestValidDexNoCode extends SmaliTest {
	@Test
	public void test() {
		assertThat(getClassNodeFromSmali()).code()
				.contains("abstract void run();")
				.contains("native int value();")
				.doesNotContain("Method not decompiled");
	}
}
