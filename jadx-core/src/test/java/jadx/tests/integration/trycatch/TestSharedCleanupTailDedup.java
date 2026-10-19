package jadx.tests.integration.trycatch;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static org.assertj.core.api.Assertions.assertThat;

public class TestSharedCleanupTailDedup extends SmaliTest {
	private static final RuntimeException FAILURE = new RuntimeException("probe");
	private static int cleanupCalls;

	public static void probe(int mode, int failMode) {
		if (mode == failMode) {
			throw FAILURE;
		}
	}

	public static void cleanup() {
		cleanupCalls++;
	}

	public static void check(Class<?> generated) throws Exception {
		Method run = generated.getMethod("run", int.class, int.class);
		cleanupCalls = 0;
		assertThat(run.invoke(null, 1, -1)).isEqualTo(1);
		assertThat(run.invoke(null, 0, -1)).isEqualTo(2);
		assertThat(cleanupCalls).isZero();
		assertThat(thrown(run, 1, 1)).isSameAs(FAILURE);
		assertThat(cleanupCalls).isEqualTo(1);
		assertThat(thrown(run, 0, 0)).isSameAs(FAILURE);
		assertThat(cleanupCalls).isEqualTo(2);
	}

	private static Throwable thrown(Method run, int mode, int failMode) throws Exception {
		try {
			run.invoke(null, mode, failMode);
			return null;
		} catch (InvocationTargetException e) {
			return e.getCause();
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
