package jadx.tests.integration.coroutines;

import java.util.List;

import org.junit.jupiter.api.Test;

import jadx.core.dex.attributes.AType;
import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestEnrollmentViewModelCoroutine extends SmaliTest {
	@Override
	protected void runChecks(List<ClassNode> classes) {
		// Kotlin argument checks precede the super call in dex. Reordering them
		// would change which exception is observed, so only that constructor fails.
		assertThat(classes.stream().flatMap(cls -> cls.getMethods().stream())
				.filter(mth -> mth.contains(AType.JADX_ERROR))
				.toList())
						.hasSize(1)
						.allMatch(mth -> mth.isConstructor());
	}

	@Test
	public void test() {
		disableCompilation();
		allowWarnInCode(); // constructor ordering and shared-tail duplication are tracked separately
		ClassNode cls = getClassNodeFromSmali(
				"coroutines/TestEnrollmentViewModelCoroutine",
				"sample.app.enrollment.EnrollmentViewModel");
		assertThat(cls)
				.code()
				// The state dispatcher has no backward branch in smali; a synthetic loop is not a fix.
				.doesNotContain("while (true)")
				.containsOne("getAccountInfo(")
				.containsOne("getSelectedDevice(")
				.contains("getUserName(")
				.contains("accountInfo != null &&")
				.contains("accountInfo == null ||")
				.contains("getPrimary(")
				.contains("getSecondary(")
				.contains("transToCommonHead(")
				.contains("sendEnrollmentRequest(")
				.contains("Method not decompiled: sample.app.enrollment.EnrollmentViewModel.<init>")
				.doesNotContain("accountInfo == null &&");
		String code = cls.getCode().toString();
		int stateOneRestore = code.indexOf("if (i2 != 1)");
		int selectedDevice = code.indexOf("getSelectedDevice(", stateOneRestore);
		assertTrue(stateOneRestore >= 0 && selectedDevice > closingBrace(code, stateOneRestore),
				"the first resume state must rejoin before fetching the selected device");
		int stateThreeRestore = code.indexOf("enrollRequest3 = (EnrollRequest) r2.f;");
		assertTrue(stateThreeRestore >= 0, "missing restored request in the third state");
		int commonHead = code.indexOf("transToCommonHead(", stateThreeRestore);
		int headContinueGuard = code.indexOf("if (accountInfo2 != coroutineSingletons)", commonHead);
		int requestAfterHead = code.indexOf("sendEnrollmentRequest(", headContinueGuard);
		assertTrue(commonHead > stateThreeRestore && headContinueGuard > commonHead
				&& requestAfterHead > headContinueGuard
				&& requestAfterHead < closingBrace(code, headContinueGuard),
				"the third state must send the request when common-head creation does not suspend");
		int resumedHead = code.indexOf("mVar = (e) r2.f;");
		assertTrue(resumedHead >= 0, "missing restored request repository in the fourth state");
		int requestCall = code.indexOf("sendEnrollmentRequest(", resumedHead);
		int stateExit = code.indexOf("return ", resumedHead);
		assertTrue(requestCall > resumedHead && requestCall < stateExit,
				"the fourth state must send the request before returning");
		assertTrue(code.substring(requestCall, stateExit).contains("if (accountInfo2 == coroutineSingletons)"),
				"the fourth state may return the suspension marker only when the request suspends");
		int fallback = code.indexOf("if (secondary == null)");
		assertTrue(fallback >= 0, "missing primary-phone fallback");
		while (fallback >= 0) {
			int closeBrace = closingBrace(code, fallback);
			assertTrue(code.substring(fallback, closeBrace).contains("getSecondary("),
					"primary-phone null path must try the secondary phone");
			fallback = code.indexOf("if (secondary == null)", closeBrace);
		}
	}

	private static int closingBrace(String code, int condition) {
		int openBrace = code.indexOf('{', condition);
		assertTrue(condition >= 0 && openBrace >= 0, "missing branch body");
		int depth = 0;
		for (int i = openBrace; i < code.length(); i++) {
			char ch = code.charAt(i);
			if (ch == '{') {
				depth++;
			} else if (ch == '}' && --depth == 0) {
				return i;
			}
		}
		throw new AssertionError("unclosed branch body");
	}
}
