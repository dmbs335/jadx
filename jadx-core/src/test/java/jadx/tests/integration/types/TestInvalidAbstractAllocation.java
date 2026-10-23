package jadx.tests.integration.types;

import java.util.List;

import org.junit.jupiter.api.Test;

import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestInvalidAbstractAllocation extends SmaliTest {
	@Override
	protected void runChecks(List<ClassNode> classes) {
		org.assertj.core.api.Assertions.assertThat(classes.get(0).root().getErrorsCounter().getErrorCount()).isGreaterThanOrEqualTo(2);
	}

	@Test
	public void test() {
		disableCompilation();
		assertThat(getClassNodeFromSmaliFiles()).code()
				.contains("new-instance cannot instantiate abstract or interface class: types.TestInvalidAbstractAllocation$AbstractTarget")
				.contains("Method not decompiled: types.TestInvalidAbstractAllocation.createAbstract():java.lang.Object")
				.contains(
						"new-instance cannot instantiate abstract or interface class: types.TestInvalidAbstractAllocation$InterfaceTarget")
				.contains("Method not decompiled: types.TestInvalidAbstractAllocation.createInterface():java.lang.Object")
				.doesNotContain("Method not decompiled: types.TestInvalidAbstractAllocation.createConcrete");
	}
}
