package jadx.tests.integration.switches;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class TestSharedCoroutineCatchTerminalCleanup extends SmaliTest {
	public static final class Timeout extends RuntimeException {
		private static final long serialVersionUID = 1L;
	}

	private static int cleanupCalls;
	private static int consumeCalls;
	private static int consumeCleanupCalls;
	private static boolean failConsume;
	private static boolean failProbeOther;

	public static void probe(boolean fail) {
		if (failProbeOther) {
			throw new IllegalStateException("probe failed");
		}
		if (fail) {
			throw new Timeout();
		}
	}

	public static int waitForUp(boolean suspend) {
		return suspend ? 1 : 0;
	}

	public static void consume() {
		consumeCalls++;
		if (failConsume) {
			throw new IllegalStateException("consume failed");
		}
	}

	public static void cleanup() {
		cleanupCalls++;
	}

	public static void consumeFailureCleanup() {
		consumeCleanupCalls++;
	}

	public static void check(Class<?> generated) throws Exception {
		Method run = generated.getMethod("run", int.class, boolean.class, boolean.class);
		cleanupCalls = 0;
		consumeCalls = 0;
		consumeCleanupCalls = 0;
		failConsume = false;
		failProbeOther = false;
		assertThat(run.invoke(null, 0, false, false)).isEqualTo(0);
		assertThat(cleanupCalls).isEqualTo(1);
		assertThat(run.invoke(null, 2, true, false)).isEqualTo(0);
		assertThat(cleanupCalls).isEqualTo(2);
		assertThat(consumeCalls).isEqualTo(1);
		assertThat(run.invoke(null, 2, true, true)).isEqualTo(1);
		assertThat(cleanupCalls).isEqualTo(2);
		assertThat(run.invoke(null, 3, false, false)).isEqualTo(0);
		assertThat(cleanupCalls).isEqualTo(3);
		assertThat(consumeCalls).isEqualTo(2);
		failProbeOther = true;
		assertThatThrownBy(() -> run.invoke(null, 2, false, false))
				.hasCauseInstanceOf(IllegalStateException.class);
		assertThat(cleanupCalls).isEqualTo(4);
		failProbeOther = false;
		failConsume = true;
		assertThatThrownBy(() -> run.invoke(null, 2, true, false))
				.hasCauseInstanceOf(IllegalStateException.class);
		assertThat(consumeCalls).isEqualTo(3);
		assertThat(consumeCleanupCalls).isEqualTo(1);
		assertThat(cleanupCalls).isEqualTo(4);
		failConsume = false;
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
