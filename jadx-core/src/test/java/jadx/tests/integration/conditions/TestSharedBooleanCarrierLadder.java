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

public class TestSharedBooleanCarrierLadder extends SmaliTest {

	@Test
	public void test() {
		ClassNode cls = getClassNodeFromSmali();
		String code = cls.getCode().getCodeStr();
		assertThat(code).doesNotContain("Method not decompiled", "JADX ERROR");
		assertTrue(code.length() < 10_000, "Shared boolean carriers duplicated a guard ladder");
		try (TestCompiler compiler = new TestCompiler(getCompilerOptions())) {
			compiler.compileNodes(List.of(cls));
			Class<?> generated = compiler.getClass("conditions.TestSharedBooleanCarrierLadder");
			Class<?>[] parameterTypes = new Class<?>[17];
			Arrays.fill(parameterTypes, Object.class);
			Method select = generated.getMethod("select", parameterTypes);
			Object original = new Object();
			Object replacement = new Object();
			Object[] args = new Object[17];
			args[0] = original;
			args[16] = replacement;
			assertSame(replacement, select.invoke(null, args), "Null guard must select replacement");
			Arrays.fill(args, 1, 16, original);
			assertSame(original, select.invoke(null, args), "All equal guards must keep the original");
			for (int guard = 1; guard <= 15; guard++) {
				args[guard] = new Object();
				assertSame(replacement, select.invoke(null, args), "Changed guard " + guard + " must select replacement");
				args[guard] = original;
				assertSame(original, select.invoke(null, args), "Equal guard " + guard + " must keep the original");
			}
		} catch (Exception e) {
			throw new AssertionError("Shared carrier ladder differs from smali", e);
		}
	}
}
