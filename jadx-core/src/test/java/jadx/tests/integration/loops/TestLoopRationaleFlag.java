package jadx.tests.integration.loops;

import java.util.Arrays;
import java.util.List;

import jadx.tests.api.IntegrationTest;
import jadx.tests.api.extensions.profiles.TestProfile;
import jadx.tests.api.extensions.profiles.TestWithProfiles;

public class TestLoopRationaleFlag extends IntegrationTest {

	public static class TestCls {
		private static boolean granted(String permission) {
			return "granted".equals(permission);
		}

		private static boolean rationale(String permission) {
			return "rationale".equals(permission);
		}

		public int run(List<String> permissions) {
			int deniedCount = 0;
			int noRationaleCount = 0;
			boolean allNoRationale = true;
			for (String permission : permissions) {
				if (granted(permission)) {
					continue;
				}
				deniedCount++;
				if (rationale(permission)) {
					allNoRationale = false;
					continue;
				}
				noRationaleCount++;
			}
			if (deniedCount == 0) {
				return 0;
			}
			if (allNoRationale) {
				return noRationaleCount;
			}
			return -noRationaleCount - 1;
		}

		public void check() {
			if (run(Arrays.asList("granted")) != 0
					|| run(Arrays.asList("denied")) != 1
					|| run(Arrays.asList("rationale")) != -1
					|| run(Arrays.asList("denied", "rationale")) != -2) {
				throw new AssertionError("rationale flag update was lost");
			}
		}
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void test() {
		getClassNode(TestCls.class);
	}
}
