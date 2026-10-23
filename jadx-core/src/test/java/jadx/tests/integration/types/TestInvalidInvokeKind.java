package jadx.tests.integration.types;

import java.util.List;

import org.junit.jupiter.api.Test;

import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestInvalidInvokeKind extends SmaliTest {
	@Override
	protected void runChecks(List<ClassNode> classes) {
		org.assertj.core.api.Assertions.assertThat(classes.get(0).root().getErrorsCounter().getErrorCount()).isGreaterThanOrEqualTo(5);
	}

	@Test
	public void test() {
		disableCompilation();
		assertThat(getClassNodeFromSmali()).code()
				.contains("Invoke opcode STATIC does not match declared method staticness")
				.contains("Invoke opcode VIRTUAL does not match declared method staticness")
				.contains("Invoke opcode DIRECT requires a private method or constructor")
				.contains("Invoke opcode VIRTUAL cannot target a private method or constructor")
				.contains("Invoke opcode INTERFACE requires an interface method owner")
				.contains("Method not decompiled: types.TestInvalidInvokeKind.invokeStaticOnInstance():int")
				.contains("Method not decompiled: types.TestInvalidInvokeKind.invokeVirtualOnStatic():int")
				.contains("Method not decompiled: types.TestInvalidInvokeKind.invokeDirectOnVirtual():int")
				.contains("Method not decompiled: types.TestInvalidInvokeKind.invokeVirtualOnPrivate():int")
				.contains("Method not decompiled: types.TestInvalidInvokeKind.invokeInterfaceOnClass():int")
				.doesNotContain("Method not decompiled: types.TestInvalidInvokeKind.valid");
	}
}
