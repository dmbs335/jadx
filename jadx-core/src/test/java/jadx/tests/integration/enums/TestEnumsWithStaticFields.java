package jadx.tests.integration.enums;

import java.util.List;

import org.junit.jupiter.api.Test;

import jadx.core.dex.attributes.AType;
import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestEnumsWithStaticFields extends SmaliTest {
	@Override
	protected void runChecks(List<ClassNode> classes) {
		org.assertj.core.api.Assertions.assertThat(classes)
				.anyMatch(cls -> cls.contains(AType.JADX_ERROR));
	}

	@Test
	public void test() {
		disableCompilation();
		assertThat(getClassNodeFromSmali())
				.code()
				.containsOne("Enum values() method does not match the $VALUES array")
				.containsOne("JADX ERROR: Failed to restore enum class")
				.containsOne("sC.clone()")
				.containsOne("q.i(145655)")
				.containsOne("q.o(145655)");
	}
}
