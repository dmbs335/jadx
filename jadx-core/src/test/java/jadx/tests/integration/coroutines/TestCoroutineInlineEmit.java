package jadx.tests.integration.coroutines;

import java.util.List;

import org.junit.jupiter.api.Test;

import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestCoroutineInlineEmit extends SmaliTest {
	@Override
	protected void runChecks(List<ClassNode> classes) {
		org.assertj.core.api.Assertions.assertThat(classes.get(0).root().getErrorsCounter().getErrorCount()).isEqualTo(1);
	}

	@Test
	public void test() {
		disableCompilation();
		assertThat(getClassNodeFromSmaliFiles("TestCoroutineInlineEmit"))
				.code()
				.contains("JADX ERROR")
				.contains("Unreachable code after abrupt statement in duplicated region")
				.contains("Method not decompiled:");
	}
}
