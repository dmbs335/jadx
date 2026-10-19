package jadx.tests.integration.trycatch;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class TestSingleSiteCatchHandlerScope extends SmaliTest {
	public static final class Timeout extends RuntimeException {
		private static final long serialVersionUID = 1L;
	}

	private static int innerCleanupCalls;
	private static int outerCleanupCalls;

	public static void probe(boolean timeout) {
		if (timeout) {
			throw new Timeout();
		}
	}

	public static void inner(boolean fail) {
		if (fail) {
			throw new IllegalStateException("inner failed");
		}
	}

	public static void innerCleanup() {
		innerCleanupCalls++;
	}

	public static void outerCleanup() {
		outerCleanupCalls++;
	}

	public static void check(Class<?> generated) throws Exception {
		Method run = generated.getMethod("run", boolean.class, boolean.class);
		innerCleanupCalls = 0;
		outerCleanupCalls = 0;
		assertThat(run.invoke(null, false, false)).isEqualTo(0);
		assertThat(run.invoke(null, true, false)).isEqualTo(1);
		assertThatThrownBy(() -> run.invoke(null, true, true))
				.hasCauseInstanceOf(IllegalStateException.class);
		assertThat(innerCleanupCalls).isEqualTo(1);
		assertThat(outerCleanupCalls).isZero();
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
