package jadx.tests.integration.loops;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.Test;

import kotlin.coroutines.Continuation;

import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.SmaliTest;
import jadx.tests.api.compiler.TestCompiler;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

public class TestCoroutineProtectedResumeLatch extends SmaliTest {

	// The original smali has three paths into the arithmetic latch: the skipped odd branch,
	// direct completion of the second suspend, and resume state 2. Resume state 1 must
	// execute the second suspend before that latch. Splitting only the state-2 latch or
	// suppressing a generated continue leaves the state-1 completion or increment lost.
	@Test
	public void test() {
		ClassNode cls = getClassNodeFromSmali();
		assertThat(cls)
				.code()
				.doesNotContain("Code restructure failed: missing block")
				.doesNotContain("Unsupported multi-entry loop pattern")
				.doesNotContain("Recursive region processing prevented")
				.doesNotContain("Region traversal cycle prevented")
				.doesNotContain("Code decompiled incorrectly")
				.doesNotContain("continue;");
		String code = cls.getCode().getCodeStr();
		org.assertj.core.api.Assertions.assertThat(code)
				.containsPattern("else \\{\\s*\\w+\\+\\+;")
				.containsPattern("this\\.label = 2;\\s*if \\(suspend\\(");
		org.assertj.core.api.Assertions.assertThat(code.split("if \\(suspend\\(", -1).length - 1)
				.as("The first resume state must retain the second suspend call")
				.isGreaterThanOrEqualTo(3);
		try (TestCompiler compiler = new TestCompiler(getCompilerOptions())) {
			compiler.compileNodes(List.of(cls));
			Class<?> generated = compiler.getClass("loops.TestCoroutineProtectedResumeLatch");
			checkRun(generated, 0, 0, 4);
			checkRun(generated, 1, 3, 1);
			checkRun(generated, 2, 3, 0);
			checkRun(generated, 0, 0, 1, true);
			checkRun(generated, 1, 3, 1, true);
		} catch (Exception e) {
			throw new AssertionError("Protected resume behavior differs from smali", e);
		}
	}

	private static void checkRun(Class<?> generated, int label, int index, int expectedCalls) throws Exception {
		checkRun(generated, label, index, expectedCalls, false);
	}

	private static void checkRun(Class<?> generated, int label, int index,
			int expectedCalls, boolean suspended) throws Exception {
		Object instance = generated.getConstructor().newInstance();
		Field labelField = generated.getDeclaredField("label");
		Field indexField = generated.getDeclaredField("index");
		Field callsField = generated.getDeclaredField("suspendCalls");
		labelField.setAccessible(true);
		indexField.setAccessible(true);
		callsField.setAccessible(true);
		labelField.setInt(instance, label);
		indexField.setInt(instance, index);
		callsField.setInt(null, 0);
		Method run = generated.getMethod("run", Object.class, Object.class, Continuation.class);
		Object value = new Object();
		Object sentinel = suspended ? value : new Object();
		Object result = assertTimeoutPreemptively(Duration.ofSeconds(2),
				() -> run.invoke(instance, value, sentinel, null));
		assertSame(value, result);
		assertEquals(expectedCalls, callsField.getInt(null));
	}
}
