package jadx.tests.integration.trycatch;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static org.assertj.core.api.Assertions.assertThat;

public class TestSharedCoroutineCatchRethrow extends SmaliTest {
	private static final RuntimeException FAILURE = new RuntimeException("probe");
	private static final Object SUSPENDED = new Object();
	private static final Object DONE = new Object();
	private static int cleanupCalls;
	private static RuntimeException saved;

	public static void probe(int location, int failLocation) {
		if (location == failLocation) {
			throw FAILURE;
		}
	}

	public static Object cleanup(boolean suspend) {
		cleanupCalls++;
		return suspend ? SUSPENDED : DONE;
	}

	public static Object suspended() {
		return SUSPENDED;
	}

	public static Object done() {
		return DONE;
	}

	public static void save(RuntimeException th) {
		saved = th;
	}

	public static RuntimeException saved() {
		return saved;
	}

	public static void check(Class<?> generated) throws Exception {
		Constructor<?> ctor = generated.getConstructor(int.class, int.class, boolean.class);
		Method run = generated.getMethod("invokeSuspend", Object.class);
		cleanupCalls = 0;
		saved = null;
		assertThat(invoke(ctor, run, 0, -1, false)).isSameAs(DONE);
		assertThat(cleanupCalls).isZero();
		assertThat(thrown(ctor, run, 0, 0, false)).isSameAs(FAILURE);
		assertThat(cleanupCalls).isEqualTo(1);
		assertThat(saved).isSameAs(FAILURE);
		assertThat(invoke(ctor, run, 1, 1, true)).isSameAs(SUSPENDED);
		assertThat(cleanupCalls).isEqualTo(2);
		assertThat(thrown(ctor, run, 2, -1, false)).isSameAs(FAILURE);
		assertThat(cleanupCalls).isEqualTo(2);
		assertThat(thrown(ctor, run, 0, 2, false)).isSameAs(FAILURE);
		assertThat(cleanupCalls).isEqualTo(2);
	}

	private static Object invoke(Constructor<?> ctor, Method run,
			int state, int failLocation, boolean suspend) throws Exception {
		return run.invoke(ctor.newInstance(state, failLocation, suspend), DONE);
	}

	private static Throwable thrown(Constructor<?> ctor, Method run,
			int state, int failLocation, boolean suspend) throws Exception {
		try {
			invoke(ctor, run, state, failLocation, suspend);
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
