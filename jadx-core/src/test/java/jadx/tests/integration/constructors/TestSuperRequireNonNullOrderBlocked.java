package jadx.tests.integration.constructors;

import org.junit.jupiter.api.Test;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestSuperRequireNonNullOrderBlocked extends ExpectedConstructorErrorTest {

	@Test
	public void test() {
		disableCompilation();
		assertThat(getClassNodeFromSmaliFiles())
				.code()
				.contains("Cannot move 'super' call to the top without changing code semantics")
				.contains("JADX ERROR")
				.contains("Method not decompiled:");
	}
}
