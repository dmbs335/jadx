package jadx.tests.integration.loops;

import org.junit.jupiter.api.Test;

import jadx.api.CommentsLevel;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestSplittiesPermissionSuspendCompletions extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		allowWarnInCode();
		getArgs().setRunDebugChecks(false);
		getArgs().setCommentsLevel(CommentsLevel.DEBUG);
		assertThat(getClassNodeFromSmaliWithClsName("splitties.permissions.EnsureAllPermissionsKt"))
				.code()
				.containsOne("Normalize permission request/settings completions through state dispatch")
				.contains("while (true)")
				.contains("hasPermission")
				.contains("shouldShowRequestPermissionRationale")
				.contains("requestPermissions")
				.contains("openApplicationDetailsSettingsAndAwaitResumed")
				.contains("result = objRequestPermissions")
				.contains("result = objOpenApplicationDetailsSettingsAndAwaitResumed")
				.doesNotContain("Region traversal cycle prevented")
				.doesNotContain("Recursive region processing prevented")
				.doesNotContain("Unsupported multi-entry loop pattern")
				.doesNotContain("Method not decompiled")
				.doesNotContain("Type inference failed")
				.doesNotContain("JADX ERROR")
				.doesNotContain("??");
	}
}
