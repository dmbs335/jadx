package jadx.tests.integration.inline;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestSyntheticBridgeSwappedArgs extends SmaliTest {

	@Test
	public void testSwappedArgumentsAreNotMerged() {
		disableCompilation();
		assertThat(getClassNodeFromSmali())
				.code()
				.containsOne("Object call(Object")
				.containsOne("return target((String) obj2, (String) obj);")
				.containsOne("target(String")
				.doesNotContain("merged with bridge method");
	}
}
