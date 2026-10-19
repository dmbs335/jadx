package jadx.tests.integration.types;

import java.util.List;

import org.junit.jupiter.api.Test;

import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestInvalidIfReferenceSource extends SmaliTest {
	@Override
	protected void runChecks(List<ClassNode> classes) {
		org.assertj.core.api.Assertions.assertThat(classes.get(0).root().getErrorsCounter().getErrorCount()).isEqualTo(2);
	}

	@Test
	public void test() {
		disableCompilation();
		assertThat(getClassNodeFromSmali()).code()
				.contains(
						"Method not decompiled: types.TestInvalidIfReferenceSource.objectLtObject(java.lang.Object, java.lang.Object):int")
				.contains("Method not decompiled: types.TestInvalidIfReferenceSource.objectGeZero(java.lang.Object):int")
				.doesNotContain("Method not decompiled: types.TestInvalidIfReferenceSource.validObjectEq");
	}
}
