package jadx.tests.integration.loops;

import jadx.tests.api.IntegrationTest;
import jadx.tests.api.extensions.profiles.TestProfile;
import jadx.tests.api.extensions.profiles.TestWithProfiles;

public class TestLoopSharedReturnAfterCandidate extends IntegrationTest {

	public static class TestCls {
		private final boolean[] tested = new boolean[1];
		private final int columns;
		private final int candidate;
		private final int rows;

		public TestCls() {
			this(4, -1, 0);
		}

		public TestCls(int columns, int candidate, int rows) {
			this.columns = columns;
			this.candidate = candidate;
			this.rows = rows;
		}

		public int run() {
			boolean done = false;
			int iterations = 0;
			while (!done) {
				iterations++;
				if (iterations >= columns * 2) {
					return iterations;
				}
				int selected = candidate;
				if (selected >= 0) {
					if (tested[selected]) {
						return iterations;
					}
					tested[selected] = true;
				}
				if (selected >= 0) {
					int pivot = -1;
					for (int row = 0; row < rows; row++) {
						if (row == 0) {
							continue;
						}
						pivot = row;
					}
					if (pivot < 0) {
						continue;
					}
				} else {
					done = true;
				}
			}
			return iterations;
		}

		public void check() {
			TestCls alreadyTested = new TestCls(4, 0, 2);
			alreadyTested.tested[0] = true;
			if (new TestCls(4, -1, 0).run() != 1
					|| new TestCls(4, 0, 2).run() != 2
					|| alreadyTested.run() != 1
					|| new TestCls(1, 0, 0).run() != 2) {
				throw new AssertionError("shared loop return or continuation was lost");
			}
		}
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void test() {
		getClassNode(TestCls.class);
	}
}
