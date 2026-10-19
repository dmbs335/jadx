package jadx.tests.integration.loops;

import java.util.List;

import org.junit.jupiter.api.Test;

import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.SmaliTest;

import static org.assertj.core.api.Assertions.assertThat;

public class TestDuplicatedIndexedLoopInit extends SmaliTest {
	@Override
	protected void runChecks(List<ClassNode> classes) {
		assertThat(classes).hasSize(1);
		assertThat(classes.get(0).root().getErrorsCounter().getErrorCount()).isZero();
	}

	@Test
	public void testSharedLoopInitializer() {
		disableCompilation();
		String code = getClassNodeFromSmali().getCode().getCodeStr();
		// Both source branches include the same loop. It may be reconstructed as an
		// indexed or for-each loop, but each copy must retain its own iteration.
		assertThat(code).doesNotContain("Method not decompiled");
		assertThat(code).contains("this.start == Long.MIN_VALUE", "this.scale != f");
		assertThat(code.split("for \\(", -1)).hasSize(3);
		assertThat(code.split("visit\\(", -1)).hasSize(4);
	}
}
