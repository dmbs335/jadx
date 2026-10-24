package jadx.tests.integration.others;

import org.junit.jupiter.api.Test;

import jadx.tests.api.IntegrationTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestFieldInitDelegatingConstructor extends IntegrationTest {
	public static class TestCls {
		static int calls;
		int value;

		public TestCls() {
			this(1);
			this.value = next();
		}

		public TestCls(int ignored) {
			this.value = next();
		}

		private int next() {
			return ++calls;
		}

		public void check() {
			calls = 0;
			TestCls instance = new TestCls();
			if (instance.value != 2 || calls != 2) {
				throw new AssertionError();
			}
		}
	}

	@Test
	public void test() {
		assertThat(getClassNode(TestCls.class))
				.code()
				.contains("next()");
	}
}
