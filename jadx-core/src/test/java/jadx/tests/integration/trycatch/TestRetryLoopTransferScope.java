package jadx.tests.integration.trycatch;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

import jadx.tests.api.IntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;

public class TestRetryLoopTransferScope extends IntegrationTest {
	public static class TestCls {
		private final RuntimeException operationFailure = new RuntimeException("operation");
		private final RuntimeException outsideFailure = new RuntimeException("outside");
		private final RuntimeException handlerFailure = new RuntimeException("handler");
		private final StringBuilder trace = new StringBuilder();
		private int remainingFailures;
		private int catches;
		private int budget;
		private boolean failOutside;
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

		private void outside(char marker) {
			trace.append(marker);
			if (failOutside) {
				throw outsideFailure;
			}
		}

		private int request(boolean stopAfterFailure) {
			requestLoop: while (true) {
				try {
					operation();
				} catch (RuntimeException e) {
					if (!retry()) {
						throw e;
					}
					if (stopAfterFailure) {
						break requestLoop;
					}
					continue requestLoop;
				}
				outside('S');
				return 7;
			}
			outside('A');
			return 9;
		}

		private int requestReturningOnExhaustion(boolean stopAfterFailure) {
			while (true) {
				try {
					operation();
				} catch (RuntimeException e) {
					if (!retry()) {
						return -1;
					}
					if (stopAfterFailure) {
						break;
					}
					continue;
				}
				outside('S');
				return 7;
			}
			outside('A');
			return 9;
		}

		public void check() throws Exception {
			TestRetryLoopTransferScope.verify(this);
		}
	}

	// Run the same oracle against the original and recompiled classes. Keeping it outside
	// TestCls prevents transformations of the checker from masking a change in the subject.
	public static void verify(Object target) throws Exception {
		for (String name : new String[] { "request", "requestReturningOnExhaustion" }) {
			Method request = target.getClass().getDeclaredMethod(name, boolean.class);
			request.setAccessible(true);
			for (int stop = 0; stop < 2; stop++) {
				for (int failures = 0; failures < 4; failures++) {
					for (int retries = 0; retries < 4; retries++) {
						for (int outside = 0; outside < 2; outside++) {
							for (int handler = 0; handler < 2; handler++) {
								checkScenario(target, request, stop != 0, failures, retries, outside != 0, handler != 0,
										name.equals("requestReturningOnExhaustion"));
							}
						}
					}
				}
			}
		}
	}

	private static Field field(Object target, String name) throws Exception {
		Field field = target.getClass().getDeclaredField(name);
		field.setAccessible(true);
		return field;
	}

	private static void checkScenario(Object target, Method request, boolean stop, int failures, int retries,
			boolean outsideThrows, boolean handlerThrows, boolean returnOnExhaustion) throws Exception {
		field(target, "remainingFailures").setInt(target, failures);
		field(target, "catches").setInt(target, 0);
		field(target, "budget").setInt(target, retries);
		field(target, "failOutside").setBoolean(target, outsideThrows);
		field(target, "failHandler").setBoolean(target, handlerThrows);
		StringBuilder trace = (StringBuilder) field(target, "trace").get(target);
		trace.setLength(0);
		int expectedCatches = failures == 0 ? 0
				: stop || handlerThrows ? 1 : Math.min(failures, retries + 1);
		boolean handlerStops = failures > 0 && (handlerThrows || expectedCatches > retries);
		boolean abandoned = stop && failures > 0 && !handlerStops;
		StringBuilder expected = new StringBuilder();
		for (int i = 0; i < expectedCatches; i++) {
			expected.append("OH");
		}
		if (!handlerStops) {
			expected.append(abandoned ? "A" : "OS");
		}
		Object expectedFailure = handlerStops
				? field(target, handlerThrows ? "handlerFailure" : "operationFailure").get(target)
				: outsideThrows ? field(target, "outsideFailure").get(target) : null;
		boolean exhausted = returnOnExhaustion && handlerStops && !handlerThrows;
		if (exhausted) {
			expectedFailure = null;
		}
		Throwable thrown = null;
		try {
			int result = (Integer) request.invoke(target, stop);
			assertThat(result).isEqualTo(exhausted ? -1 : abandoned ? 9 : 7);
		} catch (InvocationTargetException e) {
			thrown = e.getCause();
		}
		assertThat(thrown).isSameAs(expectedFailure);
		assertThat(field(target, "catches").getInt(target)).isEqualTo(expectedCatches);
		assertThat(trace.toString()).isEqualTo(expected.toString());
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
