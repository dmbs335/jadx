package jadx.tests.integration.conditions;

import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestSharedBooleanCarrierColorGate extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		String code = getClassNodeFromSmali().getCode().getCodeStr();
		assertThat(code).doesNotContain("Method not decompiled", "JADX ERROR", "JADX WARN")
				.contains("getTextForegroundStyle$ui_text()", "getFontStyle", "getFontWeight()",
						"getFontFamily()", "getLetterSpacing", "getTextDecoration()", "getFontSynthesis",
						"getFontFeatureSettings()", "getBaselineShift", "getTextGeometricTransform()",
						"getLocaleList()", "getBackground", "getShadow()", "getPlatformStyle()", "getDrawStyle()");
		assertFalse(Pattern.compile("if \\([^\\n]*\\) \\{\\s*\\}").matcher(code).find(),
				"A guard was emitted with an empty body instead of preserving its conditional action");
		assertTrue(code.length() < 100_000, "Shared terminal tail expanded excessively: " + code.length());
	}
}
