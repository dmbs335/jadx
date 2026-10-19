package jadx.tests.integration.switches;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static org.assertj.core.api.Assertions.assertThat;

public class TestCatchContinuationSkipsNormalCleanup extends SmaliTest {
	private static int normalCalls;
	private static int sharedCalls;

	public static void probe(boolean fail) {
		if (fail) {
			throw new IllegalStateException("probe");
		}
	}

	public static void normalCleanup() {
		normalCalls++;
	}

	public static void catchAction() {
		// Separate protected action in the catch continuation.
	}

	public static void sharedCleanup() {
		sharedCalls++;
	}

	public static void check(Class<?> generated) throws Exception {
		Method run = generated.getMethod("run", int.class, boolean.class, boolean.class);
		normalCalls = 0;
		sharedCalls = 0;
		assertThat(run.invoke(null, 0, false, false)).isEqualTo(10);
		assertThat(normalCalls).isEqualTo(1);
		assertThat(sharedCalls).isZero();
		assertThat(run.invoke(null, 0, true, false)).isEqualTo(20);
		assertThat(normalCalls).isEqualTo(1);
		assertThat(sharedCalls).isEqualTo(1);
		assertThat(run.invoke(null, 0, true, true)).isEqualTo(99);
		assertThat(normalCalls).isEqualTo(1);
		assertThat(sharedCalls).isEqualTo(1);
		assertThat(run.invoke(null, 1, false, false)).isEqualTo(20);
		assertThat(normalCalls).isEqualTo(1);
		assertThat(sharedCalls).isEqualTo(2);
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
