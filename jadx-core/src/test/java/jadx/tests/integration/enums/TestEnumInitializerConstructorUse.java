package jadx.tests.integration.enums;

import java.util.List;

import org.junit.jupiter.api.Test;

import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.SmaliTest;
import jadx.tests.api.compiler.TestCompiler;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestEnumInitializerConstructorUse extends SmaliTest {
	@Test
	public void test() throws Exception {
		List<ClassNode> classes = loadFromSmaliFiles();
		ClassNode enumCls = classes.stream()
				.filter(cls -> cls.getName().equals("TestEnumInitializerConstructorUse"))
				.findFirst()
				.orElseThrow();
		assertThat(enumCls)
				.code()
				.containsOne("enum TestEnumInitializerConstructorUse {")
				.doesNotContain("JADX ERROR");
		try (TestCompiler compiler = new TestCompiler(getCompilerOptions())) {
			compiler.compileNodes(classes);
			Class<?> compiledEnum = compiler.getClass(enumCls.getFullName());
			Object holder = compiledEnum.getField("holder").get(null);
			Object seen = holder.getClass().getField("seen").get(holder);
			org.assertj.core.api.Assertions.assertThat(seen)
					.isSameAs(compiledEnum.getField("ONE").get(null));
		}
	}
}
