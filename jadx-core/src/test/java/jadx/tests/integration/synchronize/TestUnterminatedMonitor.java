package jadx.tests.integration.synchronize;

import java.util.List;

import org.junit.jupiter.api.Test;

import jadx.core.dex.attributes.AType;
import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestUnterminatedMonitor extends SmaliTest {
	@Override
	protected void runChecks(List<ClassNode> classes) {
		assertThat(classes.get(0).getMethods().stream()
				.filter(method -> method.contains(AType.JADX_ERROR))
				.count()).isEqualTo(2);
	}

	@Test
	public void test() {
		disableCompilation();
		assertThat(getClassNodeFromSmali())
				.code()
				.contains("Synchronized block reaches method exit without monitor-exit")
				.contains("JADX ERROR")
				.contains("Method not decompiled: synchronize.TestUnterminatedMonitor.lock")
				.contains("Method not decompiled: synchronize.TestUnterminatedMonitor.conditionalLock");
	}
}
