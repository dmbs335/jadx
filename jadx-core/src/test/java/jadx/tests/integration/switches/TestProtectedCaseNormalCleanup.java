package jadx.tests.integration.switches;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static org.assertj.core.api.Assertions.assertThat;

public class TestProtectedCaseNormalCleanup extends SmaliTest {
	private static int cleanupCalls;

	public static void probe(boolean fail) {
		if (fail) {
			throw new IllegalStateException("probe");
		}
	}

	public static void cleanup() {
		cleanupCalls++;
	}

	public static void check(Class<?> generated) throws Exception {
		Method run = generated.getMethod("run", int.class, boolean.class);
		cleanupCalls = 0;
		assertThat(run.invoke(null, 0, false)).isEqualTo(10);
		assertThat(cleanupCalls).isEqualTo(1);
		assertThat(run.invoke(null, 0, true)).isEqualTo(-1);
		assertThat(cleanupCalls).isEqualTo(2);
		assertThat(run.invoke(null, 1, false)).isEqualTo(20);
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
