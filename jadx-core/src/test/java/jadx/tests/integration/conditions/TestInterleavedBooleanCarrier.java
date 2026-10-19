package jadx.tests.integration.conditions;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.SmaliTest;
import jadx.tests.api.compiler.TestCompiler;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestInterleavedBooleanCarrier extends SmaliTest {

	@Test
	public void test() {
		ClassNode cls = getClassNodeFromSmali();
		String code = cls.getCode().getCodeStr();
		assertThat(code).doesNotContain("Method not decompiled", "JADX ERROR", "JADX WARN");
		assertTrue(code.length() < 10_000, "Shared boolean carrier duplicated the guard ladder");
		try (TestCompiler compiler = new TestCompiler(getCompilerOptions())) {
			compiler.compileNodes(List.of(cls));
			Class<?> generated = compiler.getClass("conditions.TestInterleavedBooleanCarrier");
			Class<?>[] parameterTypes = new Class<?>[9];
			Arrays.fill(parameterTypes, Object.class);
			Method select = generated.getMethod("select", parameterTypes);
			Object original = new Object();
			Object[] args = new Object[9];
			args[0] = original;
			Arrays.fill(args, 1, 9, original);
			assertSame(original, select.invoke(null, args));
			for (int guard = 1; guard <= 8; guard++) {
				args[guard] = null;
				assertSame(original, select.invoke(null, args), "Null guard " + guard);
				Object changed = new Object();
				args[guard] = changed;
				assertSame(changed, select.invoke(null, args), "Changed guard " + guard);
				args[guard] = original;
				assertSame(original, select.invoke(null, args), "Equal guard " + guard);
			}
			Object firstChange = new Object();
			args[1] = firstChange;
			args[8] = new Object();
			assertSame(firstChange, select.invoke(null, args), "First changed guard must win");
		} catch (Exception e) {
			throw new AssertionError("Interleaved carrier behavior differs from smali", e);
		}
	}
}
