package jadx.tests.integration.enums;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import jadx.api.CommentsLevel;
import jadx.core.dex.attributes.AType;
import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestEnumObfuscated extends SmaliTest {
	@Override
	protected void runChecks(List<ClassNode> classes) {
		org.assertj.core.api.Assertions.assertThat(classes)
				.anyMatch(cls -> cls.contains(AType.JADX_ERROR));
	}

	// @formatter:off
	/*
		public enum TestEnumObfuscated {
			private static final synthetic TestEnumObfuscated[] $VLS = {ONE, TWO};
			public static final TestEnumObfuscated ONE = new TestEnumObfuscated("ONE", 0, 1);
			public static final TestEnumObfuscated TWO = new TestEnumObfuscated("TWO", 1, 2);
			private final int num;

			private TestEnumObfuscated(String str, int i, int i2) {
				super(str, i);
				this.num = i2;
			}

			public static TestEnumObfuscated vo(String str) {
				return (TestEnumObfuscated) Enum.valueOf(TestEnumObfuscated.class, str);
			}

			public static TestEnumObfuscated[] vs() {
				return (TestEnumObfuscated[]) $VLS.clone();
			}

			public synthetic int getNum() {
				return this.num;
			}

			// custom values method returns the backing array directly instead of a clone
			public static TestEnumObfuscated[] values() {
				return $VLS;
			}

			public static int valuesCount() {
				return vs().length;
			}

			public static int valuesFieldUse() {
				return $VLS.length;
			}
		}
	*/
	// @formatter:on

	@Test
	public void test() {
		disableCompilation();
		getArgs().setCommentsLevel(CommentsLevel.WARN);
		getArgs().setRenameFlags(Collections.emptySet());
		assertThat(getClassNodeFromSmali())
				.code()
				.containsOne("Enum values() method does not match the $VALUES array")
				.containsOne("JADX ERROR: Failed to restore enum class")
				.containsOne("return $VLS;")
				.containsOne("$VLS.clone()");
	}
}
