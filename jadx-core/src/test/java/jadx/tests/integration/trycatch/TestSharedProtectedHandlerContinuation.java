package jadx.tests.integration.trycatch;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class TestSharedProtectedHandlerContinuation extends SmaliTest {
	public static final class Timeout extends RuntimeException {
		private static final long serialVersionUID = 1L;
	}

	private static int cleanupCalls;

	public static void probe(boolean timeout) {
		if (timeout) {
			throw new Timeout();
		}
	}

	public static void work(boolean fail) {
		if (fail) {
			throw new IllegalStateException("work failed");
		}
	}

	public static void workCleanup() {
		cleanupCalls++;
	}

	public static void check(Class<?> generated) throws Exception {
		Method run = generated.getMethod("run", int.class, boolean.class, boolean.class);
		cleanupCalls = 0;
		assertThat(run.invoke(null, 0, false, false)).isEqualTo(0);
		assertThat(run.invoke(null, 1, false, false)).isEqualTo(0);
		assertThat(run.invoke(null, 0, true, false)).isEqualTo(1);
		assertThat(run.invoke(null, 1, true, false)).isEqualTo(1);
		assertThatThrownBy(() -> run.invoke(null, 0, true, true))
				.hasCauseInstanceOf(IllegalStateException.class);
		assertThatThrownBy(() -> run.invoke(null, 1, true, true))
				.hasCauseInstanceOf(IllegalStateException.class);
		assertThat(cleanupCalls).isEqualTo(2);
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
