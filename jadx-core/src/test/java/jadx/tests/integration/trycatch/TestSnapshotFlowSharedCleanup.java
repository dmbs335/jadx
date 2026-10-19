package jadx.tests.integration.trycatch;

import java.util.List;

import org.junit.jupiter.api.Test;

import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.SmaliTest;

import static org.assertj.core.api.Assertions.assertThat;

public class TestSnapshotFlowSharedCleanup extends SmaliTest {
	@Override
	protected void runChecks(List<ClassNode> classes) {
		assertThat(classes).hasSize(1);
		assertThat(classes.get(0).root().getErrorsCounter().getErrorCount()).isZero();
	}

	@Test
	public void testCatchRethrowsAfterOptionalCleanup() {
		disableCompilation();
		String code = getClassNodeFromSmali().getCode().getCodeStr();
		int lastCatch = code.lastIndexOf("catch (Throwable");
		assertThat(lastCatch).isPositive();
		String tail = code.substring(lastCatch);
		assertThat(tail)
				.contains("reportSnapshotFlowCancellation")
				.contains("dispose()")
				.contains("throw th;")
				.doesNotContain("break;");
		String initialState = code.substring(code.indexOf("case 0:"), code.indexOf("case 1:"));
		assertThat(initialState.indexOf("ResultKt.throwOnFailure"))
				.isLessThan(initialState.indexOf("try {"));
		for (int state = 1; state <= 3; state++) {
			String stateCode = code.substring(code.indexOf("case " + state + ":"),
					state == 3 ? code.indexOf("default:") : code.indexOf("case " + (state + 1) + ":"));
			assertThat(stateCode).containsPattern("try \\{[\\s\\S]*?ResultKt\\.throwOnFailure\\([^)]*\\);[\\s\\S]*?catch \\(Throwable");
		}
	}
}
