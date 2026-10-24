package jadx.tests.integration.enums;

import java.util.Collections;

import org.junit.jupiter.api.Test;

import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.SmaliTest;
import jadx.tests.api.compiler.TestCompiler;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestEnumInitializerUse extends SmaliTest {
	@Test
	public void test() throws Exception {
		ClassNode cls = getClassNodeFromSmali();
		assertThat(cls)
				.code()
				.containsOne("ONE;")
				.containsOne("remember(ONE);")
				.doesNotContain("JADX ERROR");
		try (TestCompiler compiler = new TestCompiler(getCompilerOptions())) {
			compiler.compileNodes(Collections.singletonList(cls));
			Class<?> compiledCls = compiler.getClass(cls.getFullName());
			org.assertj.core.api.Assertions.assertThat(compiledCls.getField("seen").get(null))
					.isSameAs(compiledCls.getField("ONE").get(null));
		}
	}
}
