package jadx.tests.integration.trycatch;

import org.junit.jupiter.api.Test;

import jadx.tests.api.IntegrationTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestRetryUnprotectedSuccess extends IntegrationTest {
	public static class TestCls {
		private final RuntimeException operationFailure = new RuntimeException("operation");
		private final RuntimeException successFailure = new RuntimeException("success");
		private final RuntimeException handlerFailure = new RuntimeException("handler");
		private final StringBuilder trace = new StringBuilder();
		private int remainingFailures;
		private int catches;
		private int budget;
		private boolean failSuccess;
		private boolean failHandler;

		private void operation() {
			trace.append('O');
			if (remainingFailures-- > 0) {
				throw operationFailure;
			}
		}

		private boolean retry() {
			trace.append('H');
			catches++;
			if (failHandler) {
				throw handlerFailure;
			}
			return catches <= budget;
		}

		private void success() {
			trace.append('S');
			if (failSuccess) {
				throw successFailure;
			}
		}

		private int request() {
			while (true) {
				try {
					operation();
				} catch (RuntimeException e) {
					if (!retry()) {
						throw e;
					}
					continue;
				}
				success();
				return 7;
			}
		}

		public void check() {
			for (int failures = 0; failures < 4; failures++) {
				for (int retries = 0; retries < 4; retries++) {
					for (int success = 0; success < 2; success++) {
						for (int handler = 0; handler < 2; handler++) {
							checkScenario(failures, retries, success != 0, handler != 0);
						}
					}
				}
			}
		}

		private void checkScenario(int failures, int retries, boolean successThrows, boolean handlerThrows) {
			remainingFailures = failures;
			catches = 0;
			budget = retries;
			failSuccess = successThrows;
			failHandler = handlerThrows;
			trace.setLength(0);
			int expectedCatches = handlerThrows && failures != 0 ? 1 : Math.min(failures, retries + 1);
			boolean reachesSuccess = failures <= retries && (!handlerThrows || failures == 0);
			StringBuilder expected = new StringBuilder();
			for (int i = 0; i < expectedCatches; i++) {
				expected.append("OH");
			}
			if (reachesSuccess) {
				expected.append("OS");
			}
			RuntimeException expectedFailure = reachesSuccess ? (successThrows ? successFailure : null)
					: handlerThrows ? handlerFailure : operationFailure;
			RuntimeException thrown = null;
			try {
				assertThat(request()).isEqualTo(7);
			} catch (RuntimeException e) {
				thrown = e;
			}
			assertThat(thrown).isSameAs(expectedFailure);
			assertThat(catches).isEqualTo(expectedCatches);
			assertThat(trace.toString()).isEqualTo(expected.toString());
		}
	}

	@Test
	public void test() {
		getClassNode(TestCls.class);
	}

	@Test
	public void testWithoutDebugInfo() {
		noDebugInfo();
		getClassNode(TestCls.class);
	}

	@Test
	public void testJavaInput() {
		useJavaInput();
		getClassNode(TestCls.class);
	}

	@Test
	public void testJavaInputWithoutDebugInfo() {
		useJavaInput();
		noDebugInfo();
		getClassNode(TestCls.class);
	}
}
