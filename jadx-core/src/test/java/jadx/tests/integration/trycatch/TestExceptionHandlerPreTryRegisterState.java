package jadx.tests.integration.trycatch;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestExceptionHandlerPreTryRegisterState extends SmaliTest {

	@Test
	public void test() {
		assertThat(getClassNodeFromSmali())
				.code()
				.doesNotContain("Type inference failed")
				.doesNotContain("Not initialized variable")
				.doesNotContain("Incomplete exception PHI")
				.doesNotContain("??")
				.doesNotContain("Method not decompiled")
				.contains("Object obj = null;")
				.contains("long j = VALUE;")
				.contains("useLong(j);")
				.contains("useObject(obj);");
	}
}
