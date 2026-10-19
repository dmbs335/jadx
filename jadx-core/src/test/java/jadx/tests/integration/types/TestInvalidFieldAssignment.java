package jadx.tests.integration.types;

import java.util.List;

import org.junit.jupiter.api.Test;

import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestInvalidFieldAssignment extends SmaliTest {
	@Override
	protected void runChecks(List<ClassNode> classes) {
		org.assertj.core.api.Assertions.assertThat(classes.get(0).root().getErrorsCounter().getErrorCount()).isEqualTo(5);
	}

	@Test
	public void test() {
		disableCompilation();
		assertThat(getClassNodeFromSmali())
				.code()
				.contains("Incompatible field assignment type: java.lang.String for int")
				.contains("Incompatible field assignment type: int for java.lang.String")
				.contains("Incompatible field assignment type: java.lang.Object for java.lang.String")
				.contains("Method not decompiled: types.TestInvalidFieldAssignment.stringToNumber():void")
				.contains("Method not decompiled: types.TestInvalidFieldAssignment.intToString():void")
				.contains("Method not decompiled: types.TestInvalidFieldAssignment.objectToString():void")
				.contains(
						"Method not decompiled: types.TestInvalidFieldAssignment.stringToInstanceNumber(types.TestInvalidFieldAssignment):void")
				.contains(
						"Method not decompiled: types.TestInvalidFieldAssignment.intToInstanceString(types.TestInvalidFieldAssignment):void")
				.contains("any = getString();")
				.contains("any = getInt();")
				.contains("text = getString();");
	}
}
