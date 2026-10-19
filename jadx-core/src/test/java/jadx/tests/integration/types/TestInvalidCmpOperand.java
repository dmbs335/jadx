package jadx.tests.integration.types;

import java.util.List;

import org.junit.jupiter.api.Test;

import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestInvalidCmpOperand extends SmaliTest {
	@Override
	protected void runChecks(List<ClassNode> classes) {
		org.assertj.core.api.Assertions.assertThat(classes.get(0).root().getErrorsCounter().getErrorCount()).isEqualTo(25);
	}

	@Test
	public void test() {
		disableCompilation();
		assertThat(getClassNodeFromSmali())
				.code()
				.contains("Non-numeric CMP operand: java.lang.Object")
				.contains("Non-numeric CMP operand: java.lang.String")
				.contains("Method not decompiled: types.TestInvalidCmpOperand.badInvoke(long):int")
				.contains("Method not decompiled: types.TestInvalidCmpOperand.badField(long):int")
				.contains("Method not decompiled: types.TestInvalidCmpOperand.badMoveAlias(long):int")
				.contains("Method not decompiled: types.TestInvalidCmpOperand.badPhiAlias(long, boolean):int")
				.contains("Method not decompiled: types.TestInvalidCmpOperand.badWidth(long):int")
				.contains("Method not decompiled: types.TestInvalidCmpOperand.badWidthBranch(long):int")
				.contains("Method not decompiled: types.TestInvalidCmpOperand.badParamWidthBranch(int, long):int")
				.contains("Method not decompiled: types.TestInvalidCmpOperand.badDoubleWidth(double):int")
				.contains("Method not decompiled: types.TestInvalidCmpOperand.badArithWidth(long):int")
				.contains("Method not decompiled: types.TestInvalidCmpOperand.badNegWidth(long):int")
				.contains("Method not decompiled: types.TestInvalidCmpOperand.badConstWidth(long):int")
				.contains("Method not decompiled: types.TestInvalidCmpOperand.badConstStringBranch(long):int")
				.contains("Method not decompiled: types.TestInvalidCmpOperand.badNewArrayBranch(long):int")
				.contains("Method not decompiled: types.TestInvalidCmpOperand.badFloatInt(int, float):int")
				.contains("Method not decompiled: types.TestInvalidCmpOperand.badConstClassBranch(long):int")
				.contains("Method not decompiled: types.TestInvalidCmpOperand.badFilledArrayBranch(long):int")
				.contains("Method not decompiled: types.TestInvalidCmpOperand.badNewInstanceBranch(long):int")
				.contains("Method not decompiled: types.TestInvalidCmpOperand.badArrayLengthBranch(int[], long):int")
				.contains("Method not decompiled: types.TestInvalidCmpOperand.badInstanceOfBranch(java.lang.Object, long):int")
				.contains("Method not decompiled: types.TestInvalidCmpOperand.badNotWidthBranch(int, long):int")
				.contains("Method not decompiled: types.TestInvalidCmpOperand.badCmpResultWidth(long, long, long):int")
				.contains("Method not decompiled: types.TestInvalidCmpOperand.badMoveExceptionBranch(long):int")
				.contains("return Long.compare(j, j2);");
	}
}
