package jadx.tests.integration.types;

import java.util.List;

import org.junit.jupiter.api.Test;

import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestInvalidNewInstanceType extends SmaliTest {
	@Override
	protected void runChecks(List<ClassNode> classes) {
		org.assertj.core.api.Assertions.assertThat(classes.get(0).root().getErrorsCounter().getErrorCount()).isEqualTo(4);
	}

	@Test
	public void test() {
		disableCompilation();
		assertThat(getClassNodeFromSmali()).code()
				.contains("new-instance type must be an object class: java.lang.String[]")
				.contains("new-instance type must be an object class: int")
				.contains("new-instance has no matching constructor invocation: java.lang.Object")
				.contains("Method not decompiled: types.TestInvalidNewInstanceType.invalidArray():java.lang.Object")
				.contains("Method not decompiled: types.TestInvalidNewInstanceType.invalidPrimitive():java.lang.Object")
				.contains("Method not decompiled: types.TestInvalidNewInstanceType.invalidUninitialized():java.lang.Object")
				.contains("Method not decompiled: types.TestInvalidNewInstanceType.invalidUnused():void")
				.doesNotContain("Method not decompiled: types.TestInvalidNewInstanceType.valid")
				.doesNotContain("Debug check failed");
	}
}
