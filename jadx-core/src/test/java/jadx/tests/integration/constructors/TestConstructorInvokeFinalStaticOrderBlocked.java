package jadx.tests.integration.constructors;

import org.junit.jupiter.api.Test;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestConstructorInvokeFinalStaticOrderBlocked extends ExpectedConstructorErrorTest {

	@Test
	public void test() {
		disableCompilation();
		assertThat(getClassNodeFromSmaliFiles())
				.code()
				.contains("Illegal instructions before constructor call")
				.contains("JADX ERROR")
				.contains("Method not decompiled:");
	}
}
