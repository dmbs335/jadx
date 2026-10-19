package jadx.tests.integration.trycatch;

import org.junit.jupiter.api.Test;

import jadx.tests.api.IntegrationTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestRetryHandlerContinuation extends IntegrationTest {

	public static class TestCls {
		private final StringBuilder trace = new StringBuilder();
		private final RuntimeException failure = new RuntimeException("retry");
		private final RuntimeException handlerFailure = new RuntimeException("handler");
		private final RuntimeException delayFailure = new RuntimeException("delay");
		private int entryFailures;
		private boolean failInHandler;
		private boolean failInDelay;

		private void enter() {
			trace.append('m');
			if (entryFailures > 0) {
				entryFailures--;
				throw failure;
			}
		}

		private void recordFailure() {
			trace.append('e');
			if (failInHandler) {
				throw handlerFailure;
			}
		}

		private void delay() {
			trace.append('d');
			if (failInDelay) {
				throw delayFailure;
			}
		}

		private int request(int modifyFailures, int requestFailures, int maxRetries) {
			int retries = 0;
			while (true) {
				int response;
				try {
					enter();
					if (modifyFailures-- > 0) {
						throw failure;
					}
					trace.append('q');
					if (requestFailures-- > 0) {
						throw failure;
					}
					response = 7;
				} catch (RuntimeException e) {
					recordFailure();
					if (retries++ >= maxRetries) {
						trace.append('x');
						throw e;
					}
					delay();
					continue;
				}
				trace.append('s');
				return response;
			}
		}

		public void check() {
			assertThat(request(0, 0, 2)).isEqualTo(7);
			assertThat(trace.toString()).isEqualTo("mqs");
			trace.setLength(0);
			assertThat(request(1, 0, 2)).isEqualTo(7);
			assertThat(trace.toString()).isEqualTo("medmqs");
			trace.setLength(0);
			assertThat(request(0, 2, 2)).isEqualTo(7);
			assertThat(trace.toString()).isEqualTo("mqedmqedmqs");
			trace.setLength(0);
			try {
				request(0, 2, 1);
				throw new AssertionError("Expected the original exception");
			} catch (RuntimeException e) {
				assertThat(e).isSameAs(failure);
			}
			assertThat(trace.toString()).isEqualTo("mqedmqex");
			for (int modify = -1; modify <= 3; modify++) {
				for (int request = -1; request <= 3; request++) {
					for (int budget = -1; budget <= 6; budget++) {
						checkScenario(modify, request, budget);
					}
				}
			}
			trace.setLength(0);
			entryFailures = 2;
			assertThat(request(0, 0, 2)).isEqualTo(7);
			assertThat(trace.toString()).isEqualTo("medmedmqs");
			trace.setLength(0);
			entryFailures = 1;
			try {
				request(0, 0, 0);
				throw new AssertionError("Expected entry failure");
			} catch (RuntimeException e) {
				assertThat(e).isSameAs(failure);
			}
			assertThat(trace.toString()).isEqualTo("mex");
			trace.setLength(0);
			failInHandler = true;
			try {
				request(0, 1, 3);
				throw new AssertionError("Expected handler failure");
			} catch (RuntimeException e) {
				assertThat(e).isSameAs(handlerFailure);
			}
			assertThat(trace.toString()).isEqualTo("mqe");
			trace.setLength(0);
			failInHandler = false;
			failInDelay = true;
			try {
				request(0, 1, 3);
				throw new AssertionError("Expected delay failure outside the request catch");
			} catch (RuntimeException e) {
				assertThat(e).isSameAs(delayFailure);
			}
			assertThat(trace.toString()).isEqualTo("mqed");
		}

		private void checkScenario(int modify, int request, int budget) {
			int modifications = Math.max(modify, 0);
			int failures = modifications + Math.max(request, 0);
			int allowed = Math.max(budget, 0);
			boolean success = failures <= allowed;
			StringBuilder expected = new StringBuilder();
			for (int attempt = 0; attempt < Math.min(failures, allowed + 1); attempt++) {
				expected.append(attempt < modifications ? "me" : "mqe");
				expected.append(attempt == allowed ? 'x' : 'd');
			}
			if (success) {
				expected.append("mqs");
			}
			trace.setLength(0);
			boolean returned = false;
			try {
				assertThat(request(modify, request, budget)).isEqualTo(7);
				returned = true;
			} catch (RuntimeException e) {
				assertThat(e).isSameAs(failure);
			}
			assertThat(returned).isEqualTo(success);
			assertThat(trace.toString()).isEqualTo(expected.toString());
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
