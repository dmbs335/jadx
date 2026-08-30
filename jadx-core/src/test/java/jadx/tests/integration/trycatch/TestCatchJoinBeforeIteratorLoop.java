package jadx.tests.integration.trycatch;

import org.junit.jupiter.api.Test;

import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestCatchJoinBeforeIteratorLoop extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		ClassNode cls = getClassNodeFromSmali();
		String code = cls.getCode().toString();
		assertThat(cls)
				.code()
				.doesNotContain("JADX WARN")
				.doesNotContain("Method not decompiled");
		org.assertj.core.api.Assertions.assertThat(code.indexOf("catch (ClassCastException"))
				.isGreaterThanOrEqualTo(0)
				.isLessThan(code.indexOf("while ("));
	}
}
