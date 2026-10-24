package jadx.tests.integration.others;

import org.junit.jupiter.api.Test;

import jadx.tests.api.IntegrationTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestFieldInitForwardRead extends IntegrationTest {
	public static class TestCls {
		int first;
		int second;

		public TestCls() {
			this.first = this.second;
			this.second = 9;
		}

		public void check() {
			if (new TestCls().first != 0) {
				throw new AssertionError();
			}
		}
	}

	public static class TestStaticCls {
		static int first;
		static int second;

		static {
			first = second;
			second = 9;
		}

		public void check() {
			if (first != 0) {
				throw new AssertionError();
			}
		}
	}

	public static class TestUnassignedStaticCls {
		static int first;
		static int second;

		static {
			first = second;
		}

		public void check() {
			if (first != 0) {
				throw new AssertionError();
			}
		}
	}

	@Test
	public void test() {
		assertThat(getClassNode(TestCls.class))
				.code()
				.containsLines(1, "int first = this.second;", "int second = 9;");
	}

	@Test
	public void testStatic() {
		assertThat(getClassNode(TestStaticCls.class))
				.code()
				.containsLines(1, "static {", "    first = second;", "    second = 9;", "}");
	}

	@Test
	public void testUnassignedStatic() {
		assertThat(getClassNode(TestUnassignedStaticCls.class))
				.code()
				.containsLines(1, "static int second;", "static int first = second;");
	}
}
