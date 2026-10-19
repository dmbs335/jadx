package jadx.tests.integration.types;

import java.util.List;

import org.junit.jupiter.api.Test;

import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestInvalidReturnOpcode extends SmaliTest {
	@Override
	protected void runChecks(List<ClassNode> classes) {
		org.assertj.core.api.Assertions.assertThat(classes.get(0).root().getErrorsCounter().getErrorCount()).isEqualTo(4);
	}

	@Test
	public void test() {
		disableCompilation();
		assertThat(getClassNodeFromSmali()).code()
				.contains("Return opcode RETURN_OBJECT does not match method return type int")
				.contains("Return opcode RETURN_NARROW does not match method return type java.lang.Object")
				.contains("Return opcode RETURN_NARROW does not match method return type long")
				.contains("Return opcode RETURN_OBJECT does not match method return type float")
				.contains("Method not decompiled: types.TestInvalidReturnOpcode.intViaObject(int):int")
				.contains("Method not decompiled: types.TestInvalidReturnOpcode.objectViaNarrow(java.lang.Object):java.lang.Object")
				.contains("Method not decompiled: types.TestInvalidReturnOpcode.longViaNarrow(long):long")
				.contains("Method not decompiled: types.TestInvalidReturnOpcode.floatViaObject(float):float")
				.doesNotContain("Method not decompiled: types.TestInvalidReturnOpcode.valid");
	}
}
