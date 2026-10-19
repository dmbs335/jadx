package jadx.tests.integration.conditions;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestSharedBooleanCarrierThreeGuards extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		String code = getClassNodeFromSmali().getCode().getCodeStr();
		assertThat(code).doesNotContain("Method not decompiled", "JADX ERROR", "JADX WARN")
				.contains("getShadow()", "getPlatformStyle()", "getDrawStyle()");
		assertTrue(code.length() < 10_000, "Guard tail expanded excessively");
	}
}
