package jadx.tests.integration.conditions;

import org.junit.jupiter.api.Test;

import jadx.api.DecompilationMode;
import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestSimpleModeTargetedVoidReturn extends SmaliTest {
	@Test
	public void test() {
		disableCompilation();
		getArgs().setDecompilationMode(DecompilationMode.SIMPLE);
		ClassNode cls = getClassNodeFromSmali();
		assertThat(cls).code()
				.contains("record();")
				.containsPattern("(?s)if \\(r0 == false\\) goto L(\\d+);.*?L\\1:\\s+return;");
	}
}
