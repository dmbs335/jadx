package jadx.tests.integration.trycatch;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static org.assertj.core.api.Assertions.assertThat;

public class TestSplitProtectedRethrow extends SmaliTest {
	private static final RuntimeException FAILURE = new RuntimeException("operation");
	private static final RuntimeException HANDLER_FAILURE = new RuntimeException("handler");
	private static final StringBuilder TRACE = new StringBuilder();
	private static int catches;
	private static int matchingCalls;
	private static int failureOccurrence;
	private static boolean failHandler;

	public static void probe(int location, int failureLocation) {
		TRACE.append(location);
		if (location == failureLocation && ++matchingCalls == failureOccurrence) {
			throw FAILURE;
		}
	}

	public static void record(Throwable failure) {
		assertThat(failure).isSameAs(FAILURE);
		catches++;
		TRACE.append('H');
		if (failHandler) {
			throw HANDLER_FAILURE;
		}
	}

	public static void check(Class<?> generated) throws Exception {
		Method run = generated.getMethod("run", int.class, int.class);
		for (int occurrence = 1; occurrence <= 2; occurrence++) {
			checkOccurrence(run, occurrence);
		}
	}

	private static void checkOccurrence(Method run, int occurrence) throws Exception {
		for (int mode = 0; mode < 2; mode++) {
			for (int failureLocation = -1; failureLocation <= 2; failureLocation++) {
				for (int handlerMode = 0; handlerMode < 2; handlerMode++) {
					TRACE.setLength(0);
					catches = 0;
					matchingCalls = 0;
					failureOccurrence = occurrence;
					failHandler = handlerMode != 0;
					boolean outsideFailure = mode == 0 && failureLocation == 0 && occurrence == 1;
					boolean protectedFailure = failureLocation == 1 && (mode == 0 || occurrence == 1)
							|| mode == 1 && failureLocation == 2 && occurrence == 1;
					Throwable thrown = null;
					try {
						assertThat(run.invoke(null, mode, failureLocation)).isEqualTo(2);
					} catch (InvocationTargetException e) {
						thrown = e.getCause();
					}
					assertThat(thrown).isSameAs(outsideFailure ? FAILURE
							: protectedFailure ? (failHandler ? HANDLER_FAILURE : FAILURE) : null);
					assertThat(catches).isEqualTo(protectedFailure ? 1 : 0);
					String expected;
					if (outsideFailure) {
						expected = "0";
					} else if (protectedFailure) {
						expected = mode == 0 ? (occurrence == 1 ? "01H" : "011H")
								: failureLocation == 2 ? "2H" : "21H";
					} else {
						expected = mode == 0 ? "011" : "21";
					}
					assertThat(TRACE.toString()).isEqualTo(expected);
				}
			}
		}
	}

	@Test
	public void test() {
		forceDecompiledCheck();
		getClassNodeFromSmali();
	}

	@Test
	public void testWithoutDebugInfo() {
		noDebugInfo();
		forceDecompiledCheck();
		getClassNodeFromSmali();
	}
}
