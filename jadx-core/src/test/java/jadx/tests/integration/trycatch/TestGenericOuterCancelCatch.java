package jadx.tests.integration.trycatch;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestGenericOuterCancelCatch extends SmaliTest {
	@Test
	public void keepsProtectedGenericCallInItsTry() {
		disableCompilation();
		assertThat(getClassNodeFromSmaliFiles())
				.code()
				.contains("catch (IllegalStateException")
				.contains("onLoadInBackground()")
				.doesNotContain("Incorrect try/catch scope")
				.doesNotContain("Method not decompiled:");
	}
}
