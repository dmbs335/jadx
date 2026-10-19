package jadx.tests.integration.trycatch;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static org.assertj.core.api.Assertions.assertThat;

public class TestSequentialParseExceptionCatch extends SmaliTest {
	private static final StringBuilder TRACE = new StringBuilder();

	public static int parse(int step, int accepted) throws ParseException {
		TRACE.append(step);
		if (step == accepted) {
			return step + 1;
		}
		throw new ParseException("step " + step, step);
	}

	public static void check(Class<?> generated) throws Exception {
		Method run = generated.getMethod("run", int.class);
		Object instance = generated.getConstructor().newInstance();
		for (int step = 0; step <= 3; step++) {
			TRACE.setLength(0);
			try {
				Object result = run.invoke(instance, step);
				assertThat(step).isLessThan(3);
				assertThat(result).isEqualTo(step + 1);
			} catch (InvocationTargetException e) {
				assertThat(step).isEqualTo(3);
				assertThat(e.getCause()).isInstanceOf(IllegalArgumentException.class)
						.hasCauseInstanceOf(ParseException.class);
			}
			assertThat(TRACE.toString()).isEqualTo("012".substring(0, Math.min(step + 1, 3)));
		}
	}

	@Test
	public void test() {
		getClassNodeFromSmali();
	}
}
