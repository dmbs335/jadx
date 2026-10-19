package jadx.tests.integration.trycatch;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static org.assertj.core.api.Assertions.assertThat;

public class TestSharedSwitchProtectedResultUse extends SmaliTest {
	public static final class Timeout extends RuntimeException {
		private static final long serialVersionUID = 1L;
	}

	public static int protectedValue(boolean timeout, boolean other) {
		if (timeout) {
			throw new Timeout();
		}
		if (other) {
			throw new IllegalStateException("other failure");
		}
		return 1;
	}

	public static void check(Class<?> generated) throws Exception {
		Method run = generated.getMethod("run", int.class, boolean.class, boolean.class);
		for (int state = 0; state < 2; state++) {
			assertThat(run.invoke(null, state, false, false)).isEqualTo(1);
			assertThat(run.invoke(null, state, true, false)).isEqualTo(2);
			assertThat(run.invoke(null, state, false, true)).isEqualTo(3);
		}
		assertThat(run.invoke(null, 2, false, false)).isEqualTo(4);
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
