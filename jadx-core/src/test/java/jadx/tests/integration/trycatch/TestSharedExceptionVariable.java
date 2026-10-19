package jadx.tests.integration.trycatch;

import org.junit.jupiter.api.Test;

import jadx.tests.api.IntegrationTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestSharedExceptionVariable extends IntegrationTest {

	public static class TestCls {
		public RuntimeException parameter(RuntimeException original, boolean replace) {
			try {
				if (replace) {
					throw new IllegalStateException("replacement");
				}
			} catch (RuntimeException e) {
				original = e;
			}
			return original;
		}

		public String split(int value) {
			try {
				if (value-- > 0) {
					throw new IllegalArgumentException("first");
				}
				if (value++ < 0) {
					throw new IllegalStateException("second");
				}
				return "ok";
			} catch (RuntimeException e) {
				return e.getMessage() + value;
			}
		}

		public String test(boolean first) {
			RuntimeException error;
			try {
				if (first) {
					throw new IllegalArgumentException("first");
				}
				throw new IllegalStateException("second");
			} catch (IllegalArgumentException e) {
				error = e;
			} catch (IllegalStateException e) {
				error = e;
			}
			return error.getMessage();
		}

		public void check() {
			RuntimeException original = new RuntimeException("original");
			assertThat(parameter(original, false)).isSameAs(original);
			assertThat(parameter(original, true).getMessage()).isEqualTo("replacement");
			assertThat(split(1)).isEqualTo("first0");
			assertThat(split(0)).isEqualTo("second0");
			assertThat(split(-1)).isEqualTo("second-1");
			assertThat(split(Integer.MIN_VALUE)).isEqualTo("ok");
			assertThat(split(Integer.MAX_VALUE)).isEqualTo("first2147483646");
			assertThat(test(true)).isEqualTo("first");
			assertThat(test(false)).isEqualTo("second");
		}
	}

	@Test
	public void test() {
		getClassNode(TestCls.class);
	}

	@Test
	public void testNoDebug() {
		noDebugInfo();
		getClassNode(TestCls.class);
	}

	@Test
	public void testJavaInput() {
		useJavaInput();
		getClassNode(TestCls.class);
	}

	@Test
	public void testJavaInputNoDebug() {
		useJavaInput();
		noDebugInfo();
		getClassNode(TestCls.class);
	}
}
