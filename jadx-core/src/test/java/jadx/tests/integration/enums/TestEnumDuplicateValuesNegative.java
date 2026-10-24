package jadx.tests.integration.enums;

import java.util.Collections;
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

	@Test
	public void testOrdinalMismatch() {
		disableCompilation();
		assertThat(getClassNodeFromSmaliWithPkg("enums", "TestEnumOrdinalMismatchNegative"))
				.code()
				.containsOne("Enum constructor ordinal differs from $VALUES array position")
				.containsOne("JADX ERROR: Failed to restore enum class")
				.containsOne("class TestEnumOrdinalMismatchNegative extends Enum");
	}

	@Test
	public void testDynamicOrdinal() {
		disableCompilation();
		assertThat(getClassNodeFromSmaliWithPkg("enums", "TestEnumDynamicOrdinalNegative"))
				.code()
				.containsOne("Enum constructor ordinal is not a known constant")
				.containsOne("JADX ERROR: Failed to restore enum class")
				.containsOne("class TestEnumDynamicOrdinalNegative extends Enum");
	}

	@Test
	public void testDynamicName() {
		disableCompilation();
		assertThat(getClassNodeFromSmaliWithPkg("enums", "TestEnumDynamicNameNegative"))
				.code()
				.containsOne("Enum constructor name is not a known constant")
				.containsOne("JADX ERROR: Failed to restore enum class")
				.containsOne("class TestEnumDynamicNameNegative extends Enum");
	}

	@Test
	public void testEncodedNameOverriddenAtRuntime() {
		disableCompilation();
		assertThat(getClassNodeFromSmaliWithPkg("enums", "TestEnumEncodedNameOverrideNegative"))
				.code()
				.containsOne("Enum constructor name is not a known constant")
				.containsOne("JADX ERROR: Failed to restore enum class")
				.containsOne("class TestEnumEncodedNameOverrideNegative extends Enum");
	}

	@Test
	public void testNameMismatchWithRenameDisabled() {
		disableCompilation();
		getArgs().setRenameFlags(Collections.emptySet());
		assertThat(getClassNodeFromSmaliWithPkg("enums", "TestEnumNameMismatchNegative"))
				.code()
				.containsOne("Enum constant name differs from constructor name")
				.containsOne("JADX ERROR: Failed to restore enum class")
				.containsOne("class TestEnumNameMismatchNegative extends Enum");
	}

	@Test
	public void testCustomValuesArray() {
		disableCompilation();
		assertThat(getClassNodeFromSmaliWithPkg("enums", "TestEnumCustomValuesNegative"))
				.code()
				.containsOne("Enum values() method does not match the $VALUES array")
				.containsOne("JADX ERROR: Failed to restore enum class")
				.containsOne("class TestEnumCustomValuesNegative extends Enum")
				.containsOne("SPECIAL.clone()");
	}

	@Test
	public void testCustomValueOfArgument() {
		disableCompilation();
		assertThat(getClassNodeFromSmaliWithPkg("enums", "TestEnumCustomValueOfNegative"))
				.code()
				.containsOne("Enum valueOf(String) method is not the default implementation")
				.containsOne("JADX ERROR: Failed to restore enum class")
				.containsOne("class TestEnumCustomValueOfNegative extends Enum")
				.containsOne("Enum.valueOf(TestEnumCustomValueOfNegative.class, \"ONE\")");
	}

	@Test
	public void testValuesOverwrite() {
		disableCompilation();
		assertThat(getClassNodeFromSmaliWithPkg("enums", "TestEnumValuesOverwriteNegative"))
				.code()
				.containsOne("Enum $VALUES field is assigned more than once")
				.containsOne("JADX ERROR: Failed to restore enum class")
				.containsOne("class TestEnumValuesOverwriteNegative extends Enum");
	}

	@Test
	public void testConstantOverwrite() {
		disableCompilation();
		assertThat(getClassNodeFromSmaliWithPkg("enums", "TestEnumConstantOverwriteNegative"))
				.code()
				.containsOne("Enum constant field is assigned more than once")
				.containsOne("JADX ERROR: Failed to restore enum class")
				.containsOne("class TestEnumConstantOverwriteNegative extends Enum");
	}

	@Test
	public void testValuesHelperSideEffect() {
		disableCompilation();
		assertThat(getClassNodeFromSmaliWithPkg("enums", "TestEnumValuesHelperSideEffectNegative"))
				.code()
				.containsOne("Enum values helper has side effects")
				.containsOne("JADX ERROR: Failed to restore enum class")
				.containsOne("class TestEnumValuesHelperSideEffectNegative extends Enum")
				.containsOne("SIDE_EFFECT = true;");
	}
}
