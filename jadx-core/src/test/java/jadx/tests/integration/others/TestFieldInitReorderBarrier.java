package jadx.tests.integration.others;

import org.junit.jupiter.api.Test;

import jadx.tests.api.IntegrationTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestFieldInitReorderBarrier extends IntegrationTest {
	public static class TestCls {
		int first;
		int second;

		public TestCls(int value) {
			this.first = observe(value);
			this.second = 9;
		}

		private int observe(int value) {
			return second + value;
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
