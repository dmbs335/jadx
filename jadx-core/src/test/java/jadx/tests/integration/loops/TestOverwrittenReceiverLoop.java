package jadx.tests.integration.loops;

import java.util.List;

import org.junit.jupiter.api.Test;

import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestOverwrittenReceiverLoop extends SmaliTest {
	@Override
	protected void runChecks(List<ClassNode> classes) {
		super.runChecks(classes);
		org.assertj.core.api.Assertions.assertThat(classes).hasSize(1);
		org.assertj.core.api.Assertions.assertThat(classes.get(0).root().getErrorsCounter().getErrorCount()).isZero();
		org.assertj.core.api.Assertions.assertThat(classes.get(0).root().getErrorsCounter().getWarnsCount()).isZero();
	}

	@Test
	public void test() {
		ClassNode cls = getClassNodeFromSmali();
		assertThat(cls)
				.code()
				.countString(9, "TestOverwrittenReceiverLoop testOverwrittenReceiverLoop = this;")
				.countString(14, "Object obj = this;")
				.contains("= null;")
				.contains("public final int checkAfterBranch(boolean z)")
				.contains("public final int checkAfterCast(boolean z)")
				.contains("obj = (Cloneable) this;")
				.contains("public final int checkAfterArrayGet(String[] strArr, boolean z)")
				.contains("obj = strArr[0];")
				.contains("public final int checkAfterFilledArray(boolean z)")
				.contains("obj = new int[]{1};")
				.contains("public final int checkAfterArrayMove(String[] strArr, boolean z)")
				.contains("public final int checkAfterNullMove(boolean z)")
				.contains("public final int checkAfterSelfMove(boolean z)")
				.contains("public final int checkAfterLoopSelfMove(int i)")
				.contains("public final int checkAfterCaughtCast(Object obj, boolean z)")
				.contains("public final int checkAfterThrowBeforeMove(Object obj, boolean z)")
				.contains("public final int checkAfterThrowAfterMove(Object obj, boolean z)")
				.contains("public final int checkAfterTwoThrowSites(Object obj, boolean z)")
				.contains("public final int checkAfterThreeThrowStates(Object obj, String str, boolean z)")
				.contains("public final int checkAfterBranchedThrow(Object obj, boolean z)")
				.contains("public final int checkAfterBranchedThrowCatchReturn(Object obj, boolean z)")
				.contains("public final int checkAfterBranchedThrowWithHandler(Object obj, boolean z)")
				.contains("public final int checkAfterBranchedThrowWithHandlerReplacement(Object obj, Object obj2, boolean z)")
				.contains("public final int checkAfterBranchedThrowWithOutsideCall(Object obj, boolean z)")
				.contains("public final int checkAfterBranchedTwoProtectedCallsCatchReturn(Object obj, boolean z)")
				.contains("public final int checkAfterSplitProtectedCallsWithUnprotectedPrelude(Object obj, boolean z)")
				.contains("public final int checkAfterNestedSplitProtectedCalls(Object obj, boolean z, boolean z2)")
				.contains("public final int checkLoopProtectedCallWithUnprotectedPrelude(Object obj, int i)")
				.contains("public final int checkLoopSplitProtectedCallsWithUnprotectedMiddle(Object obj, int i)")
				.contains("public final int checkDoWhileProtectedCallWithUnprotectedTail(Object obj, int i)")
				.contains("public final int checkDoWhileSplitProtectedCalls(Object obj, int i)")
				.contains("public final int checkAfterProtectedBranchCondition(Object[] objArr, Object obj)")
				.contains("public final int checkAfterTwoThrowSitesWithHandler(Object obj, Object obj2, boolean z)")
				.contains("public final int checkAfterBranchReplacement(TestOverwrittenReceiverLoop testOverwrittenReceiverLoop, boolean z)")
				.contains("public final int checkAfterFreshReceiver(boolean z)")
				.contains("public final int checkAfterFreshArray(boolean z)")
				.contains("public final int checkAfterStringReplacement(boolean z)")
				.contains("public final int checkAfterClassReplacement(boolean z)")
				.contains("public final int checkAfterOtherReference(String str, boolean z)")
				.contains("public final int checkAfterOtherField(boolean z)")
				.contains("public final int checkAfterOtherCall(boolean z)")
				.contains("public final int checkAfterOtherConstruction(boolean z)")
				.contains("public final int checkAfterThreeWay(TestOverwrittenReceiverLoop testOverwrittenReceiverLoop, TestOverwrittenReceiverLoop testOverwrittenReceiverLoop2, int i)")
				.contains("testOverwrittenReceiverLoop3 = i == 1 ? testOverwrittenReceiverLoop : testOverwrittenReceiverLoop2;")
				.contains("public final int checkAfterMixedThreeWay(String str, TestOverwrittenReceiverLoop testOverwrittenReceiverLoop, int i)")
				.contains("obj = i == 1 ? str : testOverwrittenReceiverLoop;")
				.contains("public final int checkAfterNullThreeWay(String str, int i)")
				.contains("obj = i == 1 ? null : str;")
				.contains("public final int checkAfterOptionalReplacement(String str, int i)")
				.contains("public final int checkAfterNestedOptionalReplacement(String str, int i)")
				.contains("public final int checkAfterNestedSameType(TestOverwrittenReceiverLoop testOverwrittenReceiverLoop, int i)")
				.contains("public final int checkAfterConditionalLoop(String str, int i)")
				.contains("while (i > 0) {")
				.contains("testOverwrittenReceiverLoop2.hashCode();")
				.contains("obj.hashCode();")
				.contains("if (i != 0 && i == 1) {")
				.contains("obj = str;")
				.contains("Object obj = this;")
				.contains("obj = new int[1];")
				.contains("obj = \"replacement\";")
				.contains("obj = String.class;")
				.contains("obj = str;")
				.contains("obj = textReplacement;")
				.contains("otherReplacement = getOtherReplacement();")
				.contains("sb = new StringBuilder();")
				.contains("public final int checkAfterCallReplacement(boolean z)")
				.contains("replacement2 = getReplacement();")
				.contains("public final int checkAfterFieldReplacement(boolean z)")
				.contains("public final int checkAfterInstanceFieldReplacement(boolean z)")
				.contains("testOverwrittenReceiverLoop = replacement;")
				.contains("testOverwrittenReceiverLoop = this.instanceReplacement;")
				.contains("testOverwrittenReceiverLoop = new TestOverwrittenReceiverLoop();")
				.contains("if (z) {")
				.contains("return testOverwrittenReceiverLoop != null ? 1 : 2;")
				.contains("int iHashCode = hashCode();")
				.contains("return iHashCode;")
				.contains("testOverwrittenReceiverLoop2 = testOverwrittenReceiverLoop;")
				.contains("return testOverwrittenReceiverLoop2 != null ? 1 : 2;")
				.doesNotContain("Method not decompiled")
				.doesNotContain("this = ")
				.doesNotContain("??");
		String code = cls.getCode().getCodeStr();
		String argumentOrBranchMethod = methodCode(code, "checkSwitchCatchArgumentOrBranchValue");
		org.assertj.core.api.Assertions.assertThat(argumentOrBranchMethod)
				.containsPattern("(?s)case 0:\\s*obj.hashCode\\(\\);\\s*try \\{\\s*mayThrow\\(\\);\\s*\\} catch \\(RuntimeException.*?recordCatch\\(\\);\\s*break;\\s*\\}\\s*obj.toString\\(\\);\\s*i2 = 0;\\s*break;")
				.containsPattern("(?s)case 1:\\s*i2 = 5;\\s*obj.hashCode\\(\\);\\s*try \\{\\s*mayThrow\\(\\);\\s*\\} catch \\(RuntimeException.*?recordCatch\\(\\);\\s*break;\\s*\\}\\s*obj.toString\\(\\);\\s*i2 = 1;\\s*break;")
				.containsPattern("(?s)\\}\\s*return i2;\\s*\\}")
				.doesNotContain("try {\n            switch");
		String throwingAssignmentMethod = methodCode(code, "checkSwitchCatchThrowingAssignment");
		org.assertj.core.api.Assertions.assertThat(throwingAssignmentMethod)
				.containsPattern("(?s)case 0:\\s*iHashCode = 4;\\s*obj.toString\\(\\);\\s*try \\{\\s*iHashCode = obj.hashCode\\(\\);\\s*mayThrow\\(\\);\\s*\\} catch \\(RuntimeException.*?recordCatch\\(\\);\\s*return iHashCode;\\s*\\}\\s*obj.toString\\(\\);")
				.containsPattern("(?s)case 1:\\s*iHashCode = 5;\\s*obj.toString\\(\\);\\s*try \\{\\s*iHashCode = obj.hashCode\\(\\);\\s*mayThrow\\(\\);\\s*\\} catch \\(RuntimeException.*?recordCatch\\(\\);\\s*return iHashCode;\\s*\\}\\s*obj.toString\\(\\);")
				.doesNotContain("try {\n            switch");
		String insideTryMethod = methodCode(code, "checkSwitchCatchUpdatedInsideTry");
		org.assertj.core.api.Assertions.assertThat(insideTryMethod)
				.containsPattern("(?s)case 0:\\s*i2 = 4;\\s*obj.hashCode\\(\\);\\s*try \\{\\s*mayThrow\\(\\);\\s*i2 = 6;\\s*mayThrow\\(\\);\\s*\\} catch \\(RuntimeException.*?recordCatch\\(\\);\\s*return i2;\\s*\\}\\s*obj.toString\\(\\);")
				.containsPattern("(?s)case 1:\\s*i2 = 5;\\s*obj.hashCode\\(\\);\\s*try \\{\\s*mayThrow\\(\\);\\s*i2 = 7;\\s*mayThrow\\(\\);\\s*\\} catch \\(RuntimeException.*?recordCatch\\(\\);\\s*return i2;\\s*\\}\\s*obj.toString\\(\\);")
				.doesNotContain("try {\n            switch");
		String updatedValueMethod = methodCode(code, "checkSwitchSplitCatchUpdatedValue");
		org.assertj.core.api.Assertions.assertThat(updatedValueMethod)
				.containsPattern("(?s)case 0:\\s*i2 = 4;\\s*obj.hashCode\\(\\);\\s*try \\{\\s*mayThrow\\(\\);\\s*\\} catch \\(RuntimeException.*?recordCatch\\(\\);\\s*return i2;\\s*\\}\\s*obj.toString\\(\\);\\s*i2 = 6;\\s*try \\{\\s*mayThrow\\(\\);\\s*\\} catch \\(RuntimeException.*?recordCatch\\(\\);\\s*return i2;\\s*\\}\\s*obj.toString\\(\\);")
				.containsPattern("(?s)case 1:\\s*i2 = 5;\\s*obj.hashCode\\(\\);\\s*try \\{\\s*mayThrow\\(\\);\\s*\\} catch \\(RuntimeException.*?recordCatch\\(\\);\\s*return i2;\\s*\\}\\s*obj.toString\\(\\);\\s*i2 = 7;\\s*try \\{\\s*mayThrow\\(\\);\\s*\\} catch \\(RuntimeException.*?recordCatch\\(\\);\\s*return i2;\\s*\\}\\s*obj.toString\\(\\);")
				.doesNotContain("try {\n            switch");
		String computedValueMethod = methodCode(code, "checkSwitchCasesCatchComputedValue");
		org.assertj.core.api.Assertions.assertThat(computedValueMethod)
				.containsPattern("(?s)case 0:\\s*iHashCode = obj.hashCode\\(\\);\\s*try \\{\\s*mayThrow\\(\\);\\s*\\} catch \\(RuntimeException.*?recordCatch\\(\\);\\s*return iHashCode;\\s*\\}\\s*obj.toString\\(\\);.*?case 1:\\s*iHashCode = obj.hashCode\\(\\);\\s*try \\{\\s*mayThrow\\(\\);\\s*\\} catch \\(RuntimeException.*?recordCatch\\(\\);\\s*return iHashCode;\\s*\\}\\s*obj.toString\\(\\);")
				.doesNotContain("try {\n            switch");
		String branchValueMethod = methodCode(code, "checkSwitchCasesCatchBranchValue");
		org.assertj.core.api.Assertions.assertThat(branchValueMethod)
				.containsPattern("(?s)case 0:\\s*i2 = 4;\\s*obj.hashCode\\(\\);\\s*try \\{\\s*mayThrow\\(\\);\\s*\\} catch \\(RuntimeException.*?recordCatch\\(\\);\\s*return i2;\\s*\\}\\s*obj.toString\\(\\);.*?case 1:\\s*i2 = 5;\\s*obj.hashCode\\(\\);\\s*try \\{\\s*mayThrow\\(\\);\\s*\\} catch \\(RuntimeException.*?recordCatch\\(\\);\\s*return i2;\\s*\\}\\s*obj.toString\\(\\);")
				.doesNotContain("try {\n            switch");
		String catchArgumentMethod = methodCode(code, "checkSwitchCasesCatchArgumentResult");
		org.assertj.core.api.Assertions.assertThat(catchArgumentMethod)
				.containsPattern("(?s)case 0:\\s*obj.hashCode\\(\\);\\s*try \\{\\s*mayThrow\\(\\);\\s*\\} catch \\(RuntimeException.*?recordCatch\\(\\);\\s*return i2;\\s*\\}\\s*obj.toString\\(\\);.*?case 1:\\s*obj.hashCode\\(\\);\\s*try \\{\\s*mayThrow\\(\\);\\s*\\} catch \\(RuntimeException.*?recordCatch\\(\\);\\s*return i2;\\s*\\}\\s*obj.toString\\(\\);")
				.doesNotContain("try {\n            switch");
		String catchResultMethod = methodCode(code, "checkSwitchCasesCatchResult");
		org.assertj.core.api.Assertions.assertThat(catchResultMethod)
				.containsPattern("(?s)case 0:\\s*obj.hashCode\\(\\);\\s*try \\{\\s*mayThrow\\(\\);\\s*\\} catch \\(RuntimeException.*?recordCatch\\(\\);\\s*return -1;\\s*\\}\\s*obj.toString\\(\\);.*?case 1:\\s*obj.hashCode\\(\\);\\s*try \\{\\s*mayThrow\\(\\);\\s*\\} catch \\(RuntimeException.*?recordCatch\\(\\);\\s*return -1;\\s*\\}\\s*obj.toString\\(\\);")
				.doesNotContain("try {\n            switch");
		String sharedThrowMethod = methodCode(code, "checkSwitchCasesCatchSharedThrow");
		org.assertj.core.api.Assertions.assertThat(sharedThrowMethod)
				.containsPattern("(?s)case 0:\\s*runtimeException.hashCode\\(\\);\\s*try \\{\\s*mayThrow\\(\\);\\s*\\} catch \\(RuntimeException.*?recordCatch\\(\\);\\s*throw runtimeException;\\s*\\}\\s*runtimeException.toString\\(\\);\\s*break;\\s*case 1:\\s*runtimeException.hashCode\\(\\);\\s*try \\{\\s*mayThrow\\(\\);\\s*\\} catch \\(RuntimeException.*?recordCatch\\(\\);\\s*throw runtimeException;\\s*\\}\\s*runtimeException.toString\\(\\);\\s*break;");
		String directReturnSwitchCatchMethod = methodCode(code, "checkSwitchCasesCatchDirectReturn");
		org.assertj.core.api.Assertions.assertThat(directReturnSwitchCatchMethod)
				.containsPattern("(?s)case 0:\\s*obj.hashCode\\(\\);\\s*try \\{\\s*mayThrow\\(\\);\\s*\\} catch \\(RuntimeException.*?recordCatch\\(\\);\\s*break;\\s*\\}\\s*obj.toString\\(\\);\\s*break;\\s*case 1:\\s*obj.hashCode\\(\\);\\s*try \\{\\s*mayThrow\\(\\);\\s*\\} catch \\(RuntimeException.*?recordCatch\\(\\);\\s*break;\\s*\\}\\s*obj.toString\\(\\);\\s*return i;.*?\\}\\s*return i;");
		String nonLoopSwitchCatchMethod = methodCode(code, "checkSwitchCasesCatchToReturn");
		org.assertj.core.api.Assertions.assertThat(nonLoopSwitchCatchMethod)
				.containsPattern("(?s)case 0:\\s*obj.hashCode\\(\\);\\s*try \\{\\s*mayThrow\\(\\);\\s*\\} catch \\(RuntimeException.*?recordCatch\\(\\);\\s*break;\\s*\\}\\s*obj.toString\\(\\);\\s*break;\\s*case 1:\\s*obj.hashCode\\(\\);\\s*try \\{\\s*mayThrow\\(\\);\\s*\\} catch \\(RuntimeException.*?recordCatch\\(\\);\\s*break;\\s*\\}\\s*obj.toString\\(\\);\\s*break;.*?\\}\\s*obj.hashCode\\(\\);\\s*return i;");
		String casesCatchUpdateMethod = methodCode(code, "checkLoopSwitchCasesCatchToUpdate");
		org.assertj.core.api.Assertions.assertThat(casesCatchUpdateMethod)
				.containsPattern("(?s)case 0:\\s*obj.hashCode\\(\\);\\s*try \\{\\s*mayThrow\\(\\);\\s*\\} catch \\(RuntimeException.*?recordCatch\\(\\);\\s*break;\\s*\\}\\s*obj.toString\\(\\);\\s*break;\\s*case 1:\\s*obj.hashCode\\(\\);\\s*try \\{\\s*mayThrow\\(\\);\\s*\\} catch \\(RuntimeException.*?recordCatch\\(\\);\\s*break;\\s*\\}\\s*obj.toString\\(\\);\\s*break;");
		String splitCatchUpdateMethod = methodCode(code, "checkLoopSwitchSplitCatchToUpdate");
		org.assertj.core.api.Assertions.assertThat(splitCatchUpdateMethod)
				.containsPattern("(?s)case 0:\\s*obj.hashCode\\(\\);\\s*try \\{\\s*mayThrow\\(\\);\\s*\\} catch \\(RuntimeException.*?recordCatch\\(\\);\\s*break;\\s*\\}\\s*obj.toString\\(\\);\\s*try \\{\\s*mayThrow\\(\\);\\s*\\} catch \\(RuntimeException.*?recordCatch\\(\\);\\s*break;\\s*\\}\\s*obj.toString\\(\\);");
		String catchContinuesMethod = methodCode(code, "checkLoopSwitchCatchContinues");
		org.assertj.core.api.Assertions.assertThat(catchContinuesMethod)
				.containsPattern("(?s)case 0:\\s*obj.hashCode\\(\\);\\s*try \\{\\s*mayThrow\\(\\);\\s*\\} catch \\(RuntimeException.*?recordCatch\\(\\);\\s*\\}\\s*obj.toString\\(\\);\\s*break;\\s*default:.*?\\}\\s*i--;");
		String outerFallThroughMethod = methodCode(code, "checkLoopNestedSwitchOuterFallThrough");
		org.assertj.core.api.Assertions.assertThat(outerFallThroughMethod)
				.containsPattern("(?s)case 0:\\s*switch \\(i3\\) \\{.*?case 8:\\s*obj.toString\\(\\);\\s*break;.*?\\}\\s*break;\\s*\\}\\s*obj.hashCode\\(\\);\\s*i--;");
		String nestedTerminalMethod = methodCode(code, "checkLoopNestedSwitchTerminal");
		org.assertj.core.api.Assertions.assertThat(nestedTerminalMethod)
				.containsPattern("(?s)case 0:\\s*switch \\(i3\\) \\{\\s*case 7:\\s*return i;\\s*default:\\s*obj.hashCode\\(\\);\\s*break;\\s*\\}\\s*break;\\s*default:.*?\\}\\s*i--;");
		String nestedContinueMethod = methodCode(code, "checkLoopNestedSwitchContinue");
		org.assertj.core.api.Assertions.assertThat(nestedContinueMethod)
				.containsPattern("(?s)case 0:\\s*switch \\(i3\\) \\{\\s*case 6:\\s*obj.hashCode\\(\\);\\s*continue;\\s*default:\\s*obj.hashCode\\(\\);\\s*break;\\s*\\}\\s*i--;\\s*break;\\s*default:");
		String conditionalNestedSwitchMethod = methodCode(code, "checkLoopConditionalNestedSwitch");
		String normalConditionalSwitch = "if \\(z\\) \\{\\s*switch \\(i3\\) \\{.*?case 5:.*?mayThrow\\(\\);"
				+ ".*?obj.toString\\(\\);\\s*break;.*?\\}\\s*\\} else \\{\\s*obj.hashCode\\(\\);\\s*break;\\s*\\}";
		String invertedConditionalSwitch = "if \\(!z\\) \\{\\s*obj.hashCode\\(\\);\\s*break;\\s*\\} else \\{"
				+ "\\s*switch \\(i3\\) \\{.*?case 5:.*?mayThrow\\(\\);.*?obj.toString\\(\\);\\s*break;.*?\\}\\s*\\}";
		org.assertj.core.api.Assertions.assertThat(conditionalNestedSwitchMethod)
				.containsPattern("(?s)case 0:\\s*(?:" + normalConditionalSwitch + "|" + invertedConditionalSwitch
						+ ")\\s*break;\\s*default:");
		String nestedSwitchMethod = methodCode(code, "checkLoopNestedSwitchSplitProtectedCases");
		org.assertj.core.api.Assertions.assertThat(nestedSwitchMethod)
				.containsPattern("(?s)case 0:.*?switch \\(i3\\) \\{.*?case 4:.*?mayThrow\\(\\);.*?obj.toString\\(\\);\\s*break;.*?default:.*?mayThrow\\(\\);.*?obj.toString\\(\\);\\s*break;\\s*\\}\\s*break;\\s*default:");
		String conditionalSwitchMethod = methodCode(code, "checkLoopSwitchConditionalProtectedCall");
		org.assertj.core.api.Assertions.assertThat(conditionalSwitchMethod)
				.containsPattern("(?s)case 0:\\s*obj.hashCode\\(\\);\\s*if \\(z\\) \\{\\s*try \\{\\s*mayThrow\\(\\);\\s*\\} catch \\(RuntimeException.*?return -1;\\s*\\}\\s*\\}\\s*obj.toString\\(\\);\\s*break;.*?\\}\\s*i--;")
				.doesNotContain("continue;");
		String doWhileSwitchMethod = methodCode(code, "checkDoWhileProtectedSwitchWithUpdate");
		org.assertj.core.api.Assertions.assertThat(doWhileSwitchMethod)
				.containsPattern("(?s)do \\{\\s*switch \\(i2\\) \\{.*?case 0:.*?try \\{\\s*mayThrow\\(\\);.*?obj.toString\\(\\);\\s*break;.*?\\}\\s*i--;\\s*\\} while \\(i > 0\\);")
				.doesNotContain("continue;");
		String directContinueSwitchMethod = methodCode(code, "checkLoopProtectedSwitchWithDirectContinue");
		org.assertj.core.api.Assertions.assertThat(directContinueSwitchMethod)
				.containsPattern("(?s)case 0:\\s*obj.hashCode\\(\\);\\s*continue;\\s*case 1:.*?obj.toString\\(\\);\\s*break;.*?\\}\\s*i--;");
		String terminalSwitchMethod = methodCode(code, "checkLoopProtectedSwitchWithTerminalCase");
		org.assertj.core.api.Assertions.assertThat(terminalSwitchMethod)
				.containsPattern("(?s)case 0:.*?obj.toString\\(\\);\\s*break;\\s*case 1:\\s*return i;\\s*default:.*?\\}\\s*i--;")
				.doesNotContain("continue;");
		String sparseSwitchMethod = methodCode(code, "checkLoopSplitProtectedSparseSwitchCases");
		org.assertj.core.api.Assertions.assertThat(sparseSwitchMethod)
				.containsPattern("(?s)case 2:.*?mayThrow\\(\\);.*?obj.toString\\(\\);\\s*break;\\s*case 16:.*?mayThrow\\(\\);.*?obj.toString\\(\\);\\s*break;.*?\\}\\s*i--;")
				.doesNotContain("continue;");
		org.assertj.core.api.Assertions.assertThat(code)
				.containsPattern("(?s)checkAfterThrowBeforeMove\\(Object obj, boolean z\\).*?try \\{\\s*mayThrow\\(\\);\\s*obj2 = obj;")
				.containsPattern("(?s)checkAfterThrowAfterMove\\(Object obj, boolean z\\).*?try \\{\\s*obj2 = obj;\\s*mayThrow\\(\\);")
				.containsPattern("(?s)checkAfterTwoThrowSites\\(Object obj, boolean z\\).*?try \\{\\s*obj2 = \\(String\\) obj;\\s*mayThrow\\(\\);")
				.containsPattern("(?s)checkAfterThreeThrowStates\\(Object obj, String str, boolean z\\).*?mayThrow\\(\\);\\s*obj2 = obj;\\s*mayThrow\\(\\);\\s*obj2 = str;\\s*mayThrow\\(\\);");
		String branchMethod = code.substring(code.indexOf("public final int checkAfterBranchedThrow("),
				code.indexOf("public final int checkAfterBranchedThrowCatchReturn("));
		org.assertj.core.api.Assertions.assertThat(branchMethod)
				.containsPattern("(?s)if \\(z\\) \\{\\s*obj2 = obj;\\s*\\}\\s*try \\{\\s*mayThrow\\(\\);\\s*\\} catch \\(RuntimeException.*?return obj2.hashCode\\(\\);");
		String catchReturnMethod = code.substring(code.indexOf("public final int checkAfterBranchedThrowCatchReturn("),
				code.indexOf("public final int checkAfterBranchedThrowWithHandler("));
		org.assertj.core.api.Assertions.assertThat(catchReturnMethod)
				.containsPattern("(?s)try \\{\\s*mayThrow\\(\\);\\s*\\} catch \\(RuntimeException.*?return -1;\\s*\\}\\s*return obj2.hashCode\\(\\);");
		String handlerMethod = code.substring(code.indexOf("public final int checkAfterBranchedThrowWithHandler("),
				code.indexOf("public final int checkAfterBranchedThrowWithHandlerReplacement("));
		org.assertj.core.api.Assertions.assertThat(handlerMethod)
				.containsPattern("(?s)if \\(z\\) \\{\\s*obj2 = obj;\\s*\\}\\s*try \\{\\s*mayThrow\\(\\);\\s*\\} catch \\(RuntimeException.*?recordCatch\\(\\);.*?return obj2.hashCode\\(\\);");
		String replacementMethod = code.substring(code.indexOf("public final int checkAfterBranchedThrowWithHandlerReplacement("),
				code.indexOf("public final int checkAfterBranchedThrowWithOutsideCall("));
		org.assertj.core.api.Assertions.assertThat(replacementMethod)
				.containsPattern("(?s)if \\(z\\) \\{\\s*obj3 = obj;\\s*\\}\\s*try \\{\\s*mayThrow\\(\\);\\s*\\} catch \\(RuntimeException.*?obj3 = obj2;.*?return obj3.hashCode\\(\\);");
		String outsideCallMethod = code.substring(code.indexOf("public final int checkAfterBranchedThrowWithOutsideCall("),
				code.indexOf("public final int checkAfterBranchedTwoProtectedCallsCatchReturn("));
		org.assertj.core.api.Assertions.assertThat(outsideCallMethod)
				.containsPattern("(?s)if \\(z\\) \\{\\s*mayThrow\\(\\);\\s*obj2 = obj;\\s*\\}\\s*try \\{\\s*mayThrow\\(\\);\\s*\\} catch \\(RuntimeException.*?return obj2.hashCode\\(\\);");
		String sharedProtectedTailMethod = code.substring(
				code.indexOf("public final int checkAfterBranchedTwoProtectedCallsCatchReturn("),
				code.indexOf("public final int checkAfterCallReplacement("));
		org.assertj.core.api.Assertions.assertThat(sharedProtectedTailMethod)
				.containsPattern("(?s)try \\{\\s*if \\(z\\) \\{\\s*obj2 = obj;\\s*mayThrow\\(\\);\\s*\\}\\s*mayThrow\\(\\);\\s*\\} catch \\(RuntimeException.*?return -1;\\s*\\}\\s*return obj2.hashCode\\(\\);");
		String splitPreludeMethod = code.substring(
				code.indexOf("public final int checkAfterSplitProtectedCallsWithUnprotectedPrelude("),
				code.indexOf("public final int checkAfterStringReplacement("));
		org.assertj.core.api.Assertions.assertThat(splitPreludeMethod)
				.containsPattern("(?s)if \\(z\\) \\{\\s*iHashCode = obj.hashCode\\(\\);\\s*try \\{\\s*mayThrow\\(\\);\\s*\\} catch \\(RuntimeException.*?return -1;\\s*\\}\\s*\\} else \\{\\s*iHashCode = obj.hashCode\\(\\);\\s*try \\{\\s*mayThrow\\(\\);\\s*\\} catch \\(RuntimeException.*?return -1;\\s*\\}\\s*\\}\\s*return iHashCode;");
		String nestedSplitMethod = code.substring(
				code.indexOf("public final int checkAfterNestedSplitProtectedCalls("),
				code.indexOf("public final int checkAfterNullMove("));
		org.assertj.core.api.Assertions.assertThat(nestedSplitMethod)
				.containsPattern("(?s)if \\(z\\) \\{\\s*iHashCode = obj.hashCode\\(\\);\\s*if \\(z2\\) \\{\\s*try \\{\\s*mayThrow\\(\\);\\s*\\} catch \\(RuntimeException.*?return -1;\\s*\\}\\s*\\}\\s*\\} else \\{\\s*iHashCode = obj.hashCode\\(\\);\\s*try \\{\\s*mayThrow\\(\\);\\s*\\} catch \\(RuntimeException.*?return -1;\\s*\\}\\s*\\}\\s*return iHashCode;");
		String loopSplitMethod = code.substring(
				code.indexOf("public final int checkLoopProtectedCallWithUnprotectedPrelude("),
				code.indexOf("public final int checkLoopSplitProtectedCallsWithUnprotectedMiddle("));
		org.assertj.core.api.Assertions.assertThat(loopSplitMethod)
				.containsPattern("(?s)while \\(i > 0\\) \\{\\s*obj.hashCode\\(\\);\\s*try \\{\\s*mayThrow\\(\\);\\s*\\} catch \\(RuntimeException.*?return -1;\\s*\\}\\s*obj.toString\\(\\);\\s*i--;\\s*\\}\\s*return i;");
		String twoLoopTriesMethod = code.substring(code.indexOf("public final int checkLoopSplitProtectedCallsWithUnprotectedMiddle("));
		org.assertj.core.api.Assertions.assertThat(twoLoopTriesMethod)
				.containsPattern("(?s)while \\(i > 0\\) \\{\\s*obj.hashCode\\(\\);\\s*try \\{\\s*mayThrow\\(\\);\\s*\\} catch \\(RuntimeException.*?return -1;\\s*\\}\\s*obj.toString\\(\\);\\s*try \\{\\s*mayThrow\\(\\);\\s*\\} catch \\(RuntimeException.*?return -1;\\s*\\}\\s*i--;\\s*\\}\\s*return i;");
		String doWhileOneTryMethod = code.substring(
				code.indexOf("public final int checkDoWhileProtectedCallWithUnprotectedTail("),
				code.indexOf("public final int checkDoWhileSplitProtectedCalls("));
		org.assertj.core.api.Assertions.assertThat(doWhileOneTryMethod)
				.containsPattern("(?s)do \\{\\s*obj.hashCode\\(\\);\\s*try \\{\\s*mayThrow\\(\\);\\s*\\} catch \\(RuntimeException.*?return -1;\\s*\\}\\s*obj.toString\\(\\);\\s*i--;\\s*\\} while \\(i > 0\\);\\s*return i;");
		String doWhileTwoTriesMethod = code.substring(
				code.indexOf("public final int checkDoWhileSplitProtectedCalls("),
				code.indexOf("public final int checkLoopProtectedCallWithUnprotectedPrelude("));
		org.assertj.core.api.Assertions.assertThat(doWhileTwoTriesMethod)
				.containsPattern("(?s)do \\{\\s*obj.hashCode\\(\\);\\s*try \\{\\s*mayThrow\\(\\);\\s*\\} catch \\(RuntimeException.*?return -1;\\s*\\}\\s*obj.toString\\(\\);\\s*try \\{\\s*mayThrow\\(\\);\\s*\\} catch \\(RuntimeException.*?return -1;\\s*\\}\\s*i--;\\s*\\} while \\(i > 0\\);\\s*return i;");
		String nestedLoopMethod = code.substring(code.indexOf("public final int checkNestedLoopProtectedCallWithUnprotectedTail("));
		org.assertj.core.api.Assertions.assertThat(nestedLoopMethod)
				.containsPattern("(?s)while \\(i > 0\\) \\{\\s*for \\(int i3 = i2; i3 > 0; i3--\\) \\{\\s*obj.hashCode\\(\\);\\s*try \\{\\s*mayThrow\\(\\);\\s*\\} catch \\(RuntimeException.*?return -1;\\s*\\}\\s*obj.toString\\(\\);\\s*\\}\\s*i--;\\s*\\}\\s*return i;");
		String nestedLoopSplitMethod = code.substring(code.indexOf("public final int checkNestedLoopSplitProtectedCalls("));
		org.assertj.core.api.Assertions.assertThat(nestedLoopSplitMethod)
				.containsPattern("(?s)while \\(i > 0\\) \\{\\s*for \\(int i3 = i2; i3 > 0; i3--\\) \\{\\s*obj.hashCode\\(\\);\\s*try \\{\\s*mayThrow\\(\\);\\s*\\} catch \\(RuntimeException.*?return -1;\\s*\\}\\s*obj.toString\\(\\);\\s*try \\{\\s*mayThrow\\(\\);\\s*\\} catch \\(RuntimeException.*?return -1;\\s*\\}\\s*\\}\\s*i--;\\s*\\}\\s*return i;");
		String outerInnerLoopMethod = code.substring(code.indexOf("public final int checkOuterAndInnerLoopProtectedCalls("));
		org.assertj.core.api.Assertions.assertThat(outerInnerLoopMethod)
				.containsPattern("(?s)while \\(i > 0\\) \\{\\s*obj.hashCode\\(\\);\\s*try \\{\\s*mayThrow\\(\\);\\s*\\} catch \\(RuntimeException.*?return -1;\\s*\\}\\s*obj.toString\\(\\);\\s*for \\(int i3 = i2; i3 > 0; i3--\\) \\{\\s*obj.hashCode\\(\\);\\s*try \\{\\s*mayThrow\\(\\);\\s*\\} catch \\(RuntimeException.*?return -1;\\s*\\}\\s*obj.toString\\(\\);\\s*\\}\\s*i--;\\s*\\}\\s*return i;");
		String siblingLoopMethod = code.substring(code.indexOf("public final int checkSiblingInnerLoopProtectedCalls("));
		org.assertj.core.api.Assertions.assertThat(siblingLoopMethod)
				.containsPattern("(?s)for \\(int i3 = i2; i3 > 0; i3--\\) \\{\\s*obj.hashCode\\(\\);\\s*try \\{\\s*mayThrow\\(\\);\\s*\\} catch \\(RuntimeException.*?return -1;\\s*\\}\\s*obj.toString\\(\\);\\s*\\}\\s*obj.toString\\(\\);\\s*for \\(int i4 = i2; i4 > 0; i4--\\) \\{\\s*obj.hashCode\\(\\);\\s*try \\{\\s*mayThrow\\(\\);\\s*\\} catch \\(RuntimeException.*?return -1;\\s*\\}\\s*obj.toString\\(\\);");
		String conditionalLoopMethod = code.substring(code.indexOf("public final int checkConditionalLoopProtectedCall("),
				code.indexOf("public final int checkDoWhileProtectedCallWithUnprotectedTail("));
		org.assertj.core.api.Assertions.assertThat(conditionalLoopMethod)
				.containsPattern("(?s)while \\(i > 0\\) \\{\\s*if \\(z\\) \\{\\s*obj.hashCode\\(\\);\\s*try \\{\\s*mayThrow\\(\\);\\s*\\} catch \\(RuntimeException.*?return -1;\\s*\\}\\s*obj.toString\\(\\);\\s*\\} else \\{\\s*obj.hashCode\\(\\);\\s*\\}\\s*i--;\\s*\\}\\s*return i;");
		String splitBranchLoopMethod = code.substring(code.indexOf("public final int checkLoopSplitProtectedBranches("),
				code.indexOf("public final int checkLoopSplitProtectedCallsWithUnprotectedMiddle("));
		org.assertj.core.api.Assertions.assertThat(splitBranchLoopMethod)
				.containsPattern("(?s)while \\(i > 0\\) \\{\\s*if \\(z\\) \\{\\s*obj.hashCode\\(\\);\\s*try \\{\\s*mayThrow\\(\\);\\s*\\} catch \\(RuntimeException.*?return -1;\\s*\\}\\s*obj.toString\\(\\);\\s*\\} else \\{\\s*obj.hashCode\\(\\);\\s*try \\{\\s*mayThrow\\(\\);\\s*\\} catch \\(RuntimeException.*?return -1;\\s*\\}\\s*obj.toString\\(\\);\\s*\\}\\s*i--;\\s*\\}\\s*return i;");
		String switchLoopMethod = code.substring(code.indexOf("public final int checkLoopProtectedSwitchCase("),
				code.indexOf("public final int checkLoopSplitProtectedBranches("));
		org.assertj.core.api.Assertions.assertThat(switchLoopMethod)
				.containsPattern("(?s)while \\(i > 0\\) \\{\\s*switch \\(i2\\) \\{\\s*case 0:\\s*obj.hashCode\\(\\);\\s*try \\{\\s*mayThrow\\(\\);\\s*\\} catch \\(RuntimeException.*?return -1;\\s*\\}\\s*obj.toString\\(\\);\\s*break;\\s*case 1:\\s*obj.hashCode\\(\\);\\s*break;\\s*default:\\s*obj.hashCode\\(\\);\\s*break;\\s*\\}\\s*i--;\\s*\\}\\s*return i;");
		String splitSwitchLoopMethod = code.substring(code.indexOf("public final int checkLoopSplitProtectedSwitchCases("),
				code.indexOf("public final int checkNestedLoopProtectedCallWithUnprotectedTail("));
		org.assertj.core.api.Assertions.assertThat(splitSwitchLoopMethod)
				.containsPattern("(?s)while \\(i > 0\\) \\{\\s*switch \\(i2\\) \\{\\s*case 0:\\s*obj.hashCode\\(\\);\\s*try \\{\\s*mayThrow\\(\\);\\s*\\} catch \\(RuntimeException.*?return -1;\\s*\\}\\s*obj.toString\\(\\);\\s*break;\\s*case 1:\\s*obj.hashCode\\(\\);\\s*try \\{\\s*mayThrow\\(\\);\\s*\\} catch \\(RuntimeException.*?return -1;\\s*\\}\\s*obj.toString\\(\\);\\s*break;\\s*default:\\s*obj.hashCode\\(\\);\\s*break;\\s*\\}\\s*i--;\\s*\\}\\s*return i;")
				.doesNotContain("continue;");
		String protectedConditionMethod = code.substring(code.indexOf("public final int checkAfterProtectedBranchCondition("),
				code.indexOf("public final int checkAfterReceiverUse("));
		org.assertj.core.api.Assertions.assertThat(protectedConditionMethod)
				.containsPattern("(?s)try \\{\\s*if \\(objArr.length != 0\\) \\{\\s*obj2 = obj;\\s*\\}\\s*mayThrow\\(\\);\\s*\\} catch \\(Exception.*?obj2 = obj;.*?return obj2.hashCode\\(\\);");
		String twoThrowSitesMethod = code.substring(code.indexOf("public final int checkAfterTwoThrowSitesWithHandler("));
		org.assertj.core.api.Assertions.assertThat(twoThrowSitesMethod)
				.containsPattern("(?s)if \\(z\\) \\{\\s*try \\{\\s*obj3 = \\(String\\) obj;\\s*mayThrow\\(\\);\\s*\\} catch \\(Exception.*?obj3 = obj2;.*?return obj3.hashCode\\(\\);");
	}

	private static String methodCode(String code, String methodName) {
		int start = code.indexOf("public final int " + methodName + "(");
		if (start == -1) {
			throw new AssertionError("Method not found: " + methodName);
		}
		int end = code.indexOf("\n    public ", start + 1);
		return code.substring(start, end == -1 ? code.length() : end);
	}
}
