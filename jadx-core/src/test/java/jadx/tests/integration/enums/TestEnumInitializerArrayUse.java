package jadx.tests.integration.enums;

import java.util.Collections;

import org.junit.jupiter.api.Test;

import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.SmaliTest;
import jadx.tests.api.compiler.TestCompiler;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestEnumInitializerArrayUse extends SmaliTest {
	@Test
	public void testFilledArray() throws Exception {
		checkArray(getClassNodeFromSmali());
	}

	@Test
	public void testAputArray() throws Exception {
		checkArray(getClassNodeFromSmaliWithPkg("enums", "TestEnumInitializerAputUse"));
	}

	private void checkArray(ClassNode cls) throws Exception {
		assertThat(cls)
				.code()
				.containsOne("enum " + cls.getName() + " {")
				.doesNotContain("JADX ERROR");
		try (TestCompiler compiler = new TestCompiler(getCompilerOptions())) {
			compiler.compileNodes(Collections.singletonList(cls));
			Class<?> compiledCls = compiler.getClass(cls.getFullName());
			Object[] seenArray = (Object[]) compiledCls.getField("seenArray").get(null);
			org.assertj.core.api.Assertions.assertThat(seenArray)
					.hasSize(1)
					.containsExactly(compiledCls.getField("ONE").get(null));
		}
	}
}
