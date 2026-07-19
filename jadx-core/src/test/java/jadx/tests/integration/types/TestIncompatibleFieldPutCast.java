package jadx.tests.integration.types;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestIncompatibleFieldPutCast extends SmaliTest {

	@Test
	public void test() {
		assertThat(getClassNodeFromSmaliFiles())
				.code()
				.contains("Object incompatibleFieldPutConcrete = new IncompatibleFieldPutConcrete();")
				.contains("sink = (IncompatibleFieldPutMarker) incompatibleFieldPutConcrete;")
				.contains("return incompatibleFieldPutConcrete;")
				.doesNotContain("Type inference failed")
				.doesNotContain("??")
				.doesNotContain("Method not decompiled");
	}
}
