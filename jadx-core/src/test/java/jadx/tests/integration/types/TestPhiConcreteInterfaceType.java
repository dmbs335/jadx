package jadx.tests.integration.types;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestPhiConcreteInterfaceType extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();

		assertThat(getClassNodeFromSmaliFiles())
				.code()
				.doesNotContain("Type inference failed")
				.doesNotContain("??")
				.contains("void testUnknown(")
				.contains("Sink.useOwner(both);")
				.contains("both.getExtras();");
	}
}
