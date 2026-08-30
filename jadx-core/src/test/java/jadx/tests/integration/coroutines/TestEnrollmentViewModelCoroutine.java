package jadx.tests.integration.coroutines;

import org.junit.jupiter.api.Test;

import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestEnrollmentViewModelCoroutine extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		allowWarnInCode(); // unrelated constructor ordering warning in the captured control-flow shape
		ClassNode cls = getClassNodeFromSmali(
				"coroutines/TestEnrollmentViewModelCoroutine",
				"sample.app.enrollment.EnrollmentViewModel");
		assertThat(cls)
				.code()
				.containsOne("while (true)")
				.containsOne("getAccountInfo(")
				.containsOne("getSelectedDevice(")
				.containsOne("getUserName(")
				.containsOne("transToCommonHead(")
				.containsOne("sendEnrollmentRequest(")
				.doesNotContain("Method not decompiled")
				.doesNotContain("Code duplicated")
				.doesNotContain("accountInfo == null &&");
	}
}
