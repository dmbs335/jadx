package jadx.tests.integration.trycatch;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestSharedConditionalCleanupThrowTail extends SmaliTest {

	private static int failAt;
	private static int cleanupCount;

	public static void probeA() {
		if (failAt == 1) {
			throw new IllegalStateException("A");
		}
	}

	public static void probeB() {
		if (failAt == 2) {
			throw new IllegalStateException("B");
		}
	}

	public static boolean active() {
		return true;
	}

	public static void cleanup(boolean active) {
		cleanupCount++;
	}

	public static void check(Class<?> generated) throws Exception {
		Method run = generated.getMethod("run", int.class);
		failAt = 0;
		cleanupCount = 0;
		assertEquals(2, run.invoke(null, 2));
		assertEquals(0, cleanupCount);
		failAt = 1;
		assertThatThrownBy(() -> run.invoke(null, 1)).hasCauseInstanceOf(IllegalStateException.class);
		assertEquals(1, cleanupCount);
		failAt = 2;
		assertThatThrownBy(() -> run.invoke(null, 2)).hasCauseInstanceOf(IllegalStateException.class);
		assertEquals(2, cleanupCount);
		failAt = 0;
	}

	@Test
	public void test() {
		forceDecompiledCheck();
		ClassNode cls = getClassNodeFromSmali();
		assertThat(cls).code().contains("cleanup(").doesNotContain("Code decompiled incorrectly");
	}
}
