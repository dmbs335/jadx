package jadx.tests.integration.others;

import org.junit.jupiter.api.Test;

import jadx.tests.api.IntegrationTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestFieldInitRepeatedWriteBarrier extends IntegrationTest {
	public static class TestCls {
		static int seen;
		int first;
		int second;

		public TestCls() {
			this.first = observe();
			this.first = 3;
			this.second = 9;
		}

		private int observe() {
			seen = second;
			return 1;
		}

		public void check() {
			new TestCls();
			if (seen != 0) {
				throw new AssertionError();
			}
		}
	}

	@Test
	public void test() {
		assertThat(getClassNode(TestCls.class))
				.code()
				.containsOne("this.second = 9;")
				.doesNotContain("int second = 9;");
	}
}
