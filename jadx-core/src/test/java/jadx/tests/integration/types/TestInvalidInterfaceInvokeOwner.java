package jadx.tests.integration.types;

import java.util.List;

import org.junit.jupiter.api.Test;

import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestInvalidInterfaceInvokeOwner extends SmaliTest {
	@Override
	protected void runChecks(List<ClassNode> classes) {
		org.assertj.core.api.Assertions.assertThat(classes.get(0).root().getErrorsCounter().getErrorCount()).isGreaterThanOrEqualTo(2);
	}

	@Test
	public void test() {
		disableCompilation();
		assertThat(getClassNodeFromSmaliFiles()).code()
				.contains("Invoke opcode DIRECT cannot target a private method outside its declaring class")
				.contains("Invoke opcode VIRTUAL requires a class method owner")
				.contains("Method not decompiled: types.TestInvalidInterfaceInvokeOwner.callAsVirtual")
				.contains("Method not decompiled: types.TestInvalidInterfaceInvokeOwner.callAsDirect")
				.doesNotContain("Method not decompiled: types.TestInvalidInterfaceInvokeOwner.callAsInterface")
				.doesNotContain("Method not decompiled: types.TestInvalidInterfaceInvokeOwner.I.callOwnPrivate");
	}
}
