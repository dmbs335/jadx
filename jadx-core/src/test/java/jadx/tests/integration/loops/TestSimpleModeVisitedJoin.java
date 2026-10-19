package jadx.tests.integration.loops;

import org.junit.jupiter.api.Test;

import jadx.api.DecompilationMode;
import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestSimpleModeVisitedJoin extends SmaliTest {
	@Test
	public void test() {
		disableCompilation();
		getArgs().setDecompilationMode(DecompilationMode.SIMPLE);
		ClassNode cls = getClassNodeFromSmali();
		assertThat(cls).code().containsPattern(
				"(?s)L(\\d+):\\s+record\\(r0\\);.*?r0 = 0 \\+ 2;\\s+goto L\\1\\s+L\\d+:\\s+r0 = r0 \\+ 1;");
	}
}
