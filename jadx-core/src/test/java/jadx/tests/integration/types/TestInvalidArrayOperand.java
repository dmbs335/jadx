package jadx.tests.integration.types;

import java.util.List;

import org.junit.jupiter.api.Test;

import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestInvalidArrayOperand extends SmaliTest {
	@Override
	protected void runChecks(List<ClassNode> classes) {
		org.assertj.core.api.Assertions.assertThat(classes.get(0).root().getErrorsCounter().getErrorCount()).isEqualTo(13);
	}

	@Test
	public void test() {
		disableCompilation();
		assertThat(getClassNodeFromSmali())
				.code()
				.contains("JADX ERROR")
				.contains("Method not decompiled:")
				.contains("Non-array value used as array operand")
				.contains("Method not decompiled: types.TestInvalidArrayOperand.lengthInvoke():int")
				.contains("Method not decompiled: types.TestInvalidArrayOperand.getInvoke():int")
				.contains("Method not decompiled: types.TestInvalidArrayOperand.putInvoke():void")
				.contains("Method not decompiled: types.TestInvalidArrayOperand.lengthField():int")
				.doesNotContain("return 1.length;", "return 1[0];");
	}
}
