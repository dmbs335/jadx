package jadx.tests.integration.enums;

import java.util.List;

import org.junit.jupiter.api.Test;

import jadx.core.dex.attributes.AType;
import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestEnumExternalValueNegative extends SmaliTest {
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
				.containsOne("JADX ERROR: Failed to restore enum class")
				.containsOne("class TestEnumExternalValueNegative extends Enum")
				.containsOne("long jNanoTime = System.nanoTime();")
				.containsOne("ONE = new TestEnumExternalValueNegative(\"ONE\", 0, cls, jNanoTime);")
				.containsOne("TWO = new TestEnumExternalValueNegative(\"TWO\", 1, cls, jNanoTime);");
	}
}
