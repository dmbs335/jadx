package jadx.tests.integration.conditions;

import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestSharedBooleanCarrierFromDex extends SmaliTest {

	// The guard-prelude MOVE feeds both the comparison and a later PHI. Normalize the
	// PHI only when every incoming MOVE has the same method-argument source, without
	// consuming the MOVE needed by the comparison or duplicating the constructor tail.
	@Test
	public void test() {
		disableCompilation();
		String code = getClassNodeFromSmali().getCode().getCodeStr();
		assertThat(code)
				.doesNotContain("Method not decompiled", "JADX ERROR", "JADX WARN")
				.contains("requiresAlloc", "getDrawStyle()", "return new SpanStyle(");
		assertFalse(Pattern.compile("if \\([^\\n]*\\) \\{\\s*\\}").matcher(code).find(),
				"A guard was emitted with an empty body instead of preserving its conditional action");
		assertTrue(code.length() < 100_000, "Shared terminal tail expanded excessively");
		assertTrue(code.indexOf("return new SpanStyle(") == code.lastIndexOf("return new SpanStyle("),
				"The shared constructor tail was emitted more than once");
	}
}
