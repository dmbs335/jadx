package jadx.tests.integration.types;

import java.util.List;

import org.junit.jupiter.api.Test;

import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestInvalidArrayIndex extends SmaliTest {
	@Override
	protected void runChecks(List<ClassNode> classes) {
		org.assertj.core.api.Assertions.assertThat(classes.get(0).root().getErrorsCounter().getErrorCount()).isEqualTo(7);
	}

	@Test
	public void test() {
		disableCompilation();
		assertThat(getClassNodeFromSmali())
				.code()
				.contains("Invalid array dimension type: java.lang.Object")
				.contains("Invalid array dimension type: long")
				.contains("Invalid array index type: java.lang.Object")
				.contains("Method not decompiled: types.TestInvalidArrayIndex.getIndexInvoke(int[]):int")
				.contains("Method not decompiled: types.TestInvalidArrayIndex.putIndexInvoke(int[]):void")
				.contains("Method not decompiled: types.TestInvalidArrayIndex.newArrayIndexInvoke():int[]")
				.contains("return iArr[z ? 1 : 0];")
				.contains("return iArr[b];")
				.contains("return iArr[i];");
	}
}
