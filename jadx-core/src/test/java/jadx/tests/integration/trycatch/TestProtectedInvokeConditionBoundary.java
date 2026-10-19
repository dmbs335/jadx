package jadx.tests.integration.trycatch;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class TestProtectedInvokeConditionBoundary extends SmaliTest {
	public static int protectedValue(boolean fail) {
		if (fail) {
			throw new IllegalStateException("protected failure");
		}
		return 7;
	}

	public static int unprotectedValue(boolean fail) {
		if (fail) {
			throw new IllegalStateException("unprotected failure");
		}
		return 7;
	}

	public static void check(Class<?> generated) throws Exception {
		Method run = generated.getMethod("run", boolean.class, boolean.class);
		assertThat(run.invoke(null, false, false)).isEqualTo(1);
		assertThat(run.invoke(null, true, false)).isEqualTo(2);
		assertThatThrownBy(() -> run.invoke(null, false, true))
				.hasCauseInstanceOf(IllegalStateException.class);
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
