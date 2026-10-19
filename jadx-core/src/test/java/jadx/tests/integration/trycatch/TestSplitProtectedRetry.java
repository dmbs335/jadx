package jadx.tests.integration.trycatch;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static org.assertj.core.api.Assertions.assertThat;

public class TestSplitProtectedRetry extends SmaliTest {
	private static final RuntimeException FAILURE = new RuntimeException("operation");
	private static final RuntimeException HANDLER_FAILURE = new RuntimeException("handler");
	private static final Error UNHANDLED = new Error("not caught by RuntimeException handler");
	private static final StringBuilder TRACE = new StringBuilder();
	private static int remainingFailures;
	private static int retryBudget;
	private static int catches;
	private static boolean failHandler;
	private static boolean throwError;

	public static void probe(int location, int failureLocation) {
		TRACE.append(location);
		if (location == failureLocation && remainingFailures-- > 0) {
			if (throwError) {
				throw UNHANDLED;
			}
			throw FAILURE;
		}
	}

	public static boolean retry(Throwable failure) {
		assertThat(failure).isSameAs(FAILURE);
		TRACE.append('H');
		catches++;
		if (failHandler) {
			throw HANDLER_FAILURE;
		}
		return catches <= retryBudget;
	}

	public static void check(Class<?> generated) throws Exception {
		Method run = generated.getMethod("run", int.class, int.class);
		for (int mode = 0; mode < 2; mode++) {
			for (int location = -1; location <= 2; location++) {
				for (int failures = 0; failures <= 3; failures++) {
					for (int budget = 0; budget <= 3; budget++) {
						for (int handler = 0; handler < 2; handler++) {
							checkScenario(run, mode, location, failures, budget, handler != 0);
						}
					}
				}
			}
		}
	}

	public static void checkTyped(Class<?> generated) throws Exception {
		check(generated);
		Method run = generated.getMethod("run", int.class, int.class);
		try {
			throwError = true;
			checkUnhandled(run, 0, 0, "0");
			checkUnhandled(run, 0, 1, "01");
			checkUnhandled(run, 1, 1, "21");
			checkUnhandled(run, 1, 2, "2");
		} finally {
			throwError = false;
		}
	}

	private static void checkUnhandled(Method run, int mode, int location, String expectedTrace) throws Exception {
		TRACE.setLength(0);
		catches = 0;
		remainingFailures = 1;
		retryBudget = 3;
		failHandler = false;
		Throwable thrown = null;
		try {
			run.invoke(null, mode, location);
		} catch (InvocationTargetException e) {
			thrown = e.getCause();
		}
		assertThat(thrown).isSameAs(UNHANDLED);
		assertThat(catches).isZero();
		assertThat(TRACE.toString()).isEqualTo(expectedTrace);
	}

	private static void checkScenario(Method run, int mode, int location, int failures, int budget,
			boolean handlerFailure) throws Exception {
		StringBuilder expected = new StringBuilder();
		int expectedCatches = 0;
		int left = failures;
		Throwable expectedFailure = null;
		if (mode == 0) {
			expected.append('0');
			if (location == 0 && left-- > 0) {
				expectedFailure = FAILURE;
			}
		}
		int call = mode == 0 ? 1 : 2;
		int completed = 0;
		while (expectedFailure == null && completed < 2) {
			expected.append(call);
			if (call == location && left-- > 0) {
				expected.append('H');
				expectedCatches++;
				if (handlerFailure || expectedCatches > budget) {
					expectedFailure = handlerFailure ? HANDLER_FAILURE : FAILURE;
				}
			} else {
				completed++;
			}
			call = 1;
		}
		TRACE.setLength(0);
		catches = 0;
		remainingFailures = failures;
		retryBudget = budget;
		failHandler = handlerFailure;
		Throwable thrown = null;
		try {
			assertThat(run.invoke(null, mode, location)).isEqualTo(2);
		} catch (InvocationTargetException e) {
			thrown = e.getCause();
		}
		assertThat(thrown).isSameAs(expectedFailure);
		assertThat(catches).isEqualTo(expectedCatches);
		assertThat(TRACE.toString()).isEqualTo(expected.toString());
	}

	@Test
	public void test() {
		forceDecompiledCheck();
		getArgs().setShowInconsistentCode(false);
		jadx.tests.api.utils.assertj.JadxAssertions.assertThat(getClassNodeFromSmali()).code()
				.contains("Normalize protected retry entries through a common dispatch")
				.doesNotContain("JADX WARN:")
				.doesNotContain("Method not decompiled:");
	}

	@Test
	public void testWithoutDebugInfo() {
		noDebugInfo();
		test();
	}
}
