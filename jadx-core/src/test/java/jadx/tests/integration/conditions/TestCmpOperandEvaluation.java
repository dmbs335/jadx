package jadx.tests.integration.conditions;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

public class TestCmpOperandEvaluation extends SmaliTest {
	@Test
	public void test() {
		forceDecompiledCheck();
		getClassNodeFromSmali();
	}
}
