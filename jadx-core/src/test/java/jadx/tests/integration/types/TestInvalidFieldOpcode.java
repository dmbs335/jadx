package jadx.tests.integration.types;

import java.util.List;

import org.junit.jupiter.api.Test;

import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestInvalidFieldOpcode extends SmaliTest {
	@Override
	protected void runChecks(List<ClassNode> classes) {
		// A failed class can be reprocessed while producing the fallback source, so
		// assert the failing methods below instead of relying on the error count.
		org.assertj.core.api.Assertions.assertThat(classes.get(0).root().getErrorsCounter().getErrorCount()).isGreaterThanOrEqualTo(7);
	}

	@Test
	public void test() {
		disableCompilation();
		assertThat(getClassNodeFromSmali()).code()
				.contains("does not match declared field staticness")
				.contains("Method not decompiled: types.TestInvalidFieldOpcode.objectGetIntField():int")
				.contains("Method not decompiled: types.TestInvalidFieldOpcode.objectPutIntField(int):void")
				.contains("Method not decompiled: types.TestInvalidFieldOpcode.wideGetIntField():int")
				.contains("Method not decompiled: types.TestInvalidFieldOpcode.instanceGetStatic():int")
				.contains("Method not decompiled: types.TestInvalidFieldOpcode.instancePutStatic(int):void")
				.contains("Method not decompiled: types.TestInvalidFieldOpcode.staticGetInstance():int")
				.contains("Method not decompiled: types.TestInvalidFieldOpcode.staticPutInstance(int):void")
				.doesNotContain("Method not decompiled: types.TestInvalidFieldOpcode.valid");
	}
}
