package jadx.tests.integration.types;

import java.util.List;

import org.junit.jupiter.api.Test;

import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestInvalidReferenceTestSource extends SmaliTest {
	@Override
	protected void runChecks(List<ClassNode> classes) {
		org.assertj.core.api.Assertions.assertThat(classes.get(0).root().getErrorsCounter().getErrorCount()).isEqualTo(5);
	}

	@Test
	public void test() {
		disableCompilation();
		assertThat(getClassNodeFromSmali()).code()
				.contains("Method not decompiled: types.TestInvalidReferenceTestSource.intInstanceOf(int):boolean")
				.contains("Method not decompiled: types.TestInvalidReferenceTestSource.intCheckCast(int):java.lang.String")
				.contains("Method not decompiled: types.TestInvalidReferenceTestSource.intArrayLength(int):int")
				.contains("Method not decompiled: types.TestInvalidReferenceTestSource.intThrow(int):void")
				.contains("Method not decompiled: types.TestInvalidReferenceTestSource.intMonitor(int):void")
				.doesNotContain("Method not decompiled: types.TestInvalidReferenceTestSource.valid");
	}
}
