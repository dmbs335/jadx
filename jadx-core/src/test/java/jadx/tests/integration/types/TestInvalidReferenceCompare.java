package jadx.tests.integration.types;

import java.util.List;

import org.junit.jupiter.api.Test;

import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestInvalidReferenceCompare extends SmaliTest {
	@Override
	protected void runChecks(List<ClassNode> classes) {
		org.assertj.core.api.Assertions.assertThat(classes.get(0).root().getErrorsCounter().getErrorCount()).isEqualTo(3);
	}

	@Test
	public void test() {
		ClassNode cls = getClassNodeFromSmali();
		assertThat(cls).code()
				.contains("Invalid reference relational comparison: java.lang.String < java.lang.String")
				.contains("Incompatible reference and primitive comparison: java.lang.String and int")
				.contains(
						"Method not decompiled: types.TestInvalidReferenceCompare.relationalStrings(java.lang.String, java.lang.String):boolean")
				.contains("Method not decompiled: types.TestInvalidReferenceCompare.relationalStringZero(java.lang.String):boolean")
				.contains("Method not decompiled: types.TestInvalidReferenceCompare.referenceAndPrimitive(java.lang.String, int):boolean")
				.contains("return ((Object) str) == num;")
				.contains("return ((Object) getString()) != getInteger();")
				.contains("return ((Object) number) != thread;")
				.contains("return ((Object) iArr) == jArr;")
				.contains("== obj")
				.doesNotContain("Method not decompiled: types.TestInvalidReferenceCompare.unrelatedFinalClasses")
				.doesNotContain("Method not decompiled: types.TestInvalidReferenceCompare.unrelatedInvokeResults")
				.doesNotContain("Method not decompiled: types.TestInvalidReferenceCompare.unrelatedClasses")
				.doesNotContain("Method not decompiled: types.TestInvalidReferenceCompare.unrelatedArrays");
	}
}
