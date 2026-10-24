package jadx.tests.integration.enums;

import java.util.List;

import org.junit.jupiter.api.Test;

import jadx.core.dex.attributes.AType;
import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestEnumDuplicateValuesNegative extends SmaliTest {
	@Override
	protected void runChecks(List<ClassNode> classes) {
		org.assertj.core.api.Assertions.assertThat(classes)
				.anyMatch(cls -> cls.contains(AType.JADX_ERROR));
	}

	@Test
	public void testDuplicateField() {
		disableCompilation();
		assertThat(getClassNodeFromSmali())
				.code()
				.containsOne("$VALUES array contains a duplicate enum field")
				.containsOne("JADX ERROR: Failed to restore enum class")
				.containsOne("class TestEnumDuplicateValuesNegative extends Enum")
				.containsOne("$VALUES = new TestEnumDuplicateValuesNegative[]{");
	}

	@Test
	public void testDuplicateNames() {
		disableCompilation();
		assertThat(getClassNodeFromSmaliWithPkg("enums", "TestEnumDuplicateNamesNegative"))
				.code()
				.containsOne("Enum constants resolve to duplicate names")
				.containsOne("JADX ERROR: Failed to restore enum class")
				.containsOne("class TestEnumDuplicateNamesNegative extends Enum");
	}
}
