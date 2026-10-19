package jadx.tests.integration.types;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestMixedBooleanBitOp extends SmaliTest {
	@Test
	public void test() {
		forceDecompiledCheck();
		assertThat(getClassNodeFromSmali()).code()
				.contains("return (z ? 1 : 0) & i;")
				.contains("return i | (z ? 1 : 0);")
				.contains("return (z ? 1 : 0) ^ i;")
				.contains("return (z ? 1 : 0) & (z2 ? 1 : 0);")
				.contains("bits = i | (z ? 1 : 0);")
				.contains("value = i | (z ? 1 : 0);")
				.contains("iArr[i] = i | (z ? 1 : 0);")
				.contains("consume(i | (z ? 1 : 0));")
				.contains("consumeInstance(i | (z ? 1 : 0));")
				.contains("consumePair(z, i | (z ? 1 : 0));")
				.contains("new TestMixedBooleanBitOp(i | (z ? 1 : 0))")
				.contains("public static long widenOrIntBoolean(int i, boolean z) {\n        return i | (z ? 1 : 0);")
				.contains("public static byte narrowOrIntBoolean(int i, boolean z) {\n        return (byte) (i | (z ? 1 : 0));")
				.contains("public static float floatOrIntBoolean(int i, boolean z) {\n        return i | (z ? 1 : 0);")
				.contains("public static long storeAndWidenOrIntBoolean(int i, boolean z, int[] iArr) {")
				.contains("iArr[i] = i2;\n        return i2;")
				.contains("public static long branchAndWidenOrIntBoolean(int i, boolean z) {")
				.contains("if (i2 != 0) {\n            return i2;")
				.contains("public static long compareAndWidenOrIntBoolean(int i, boolean z) {")
				.contains("if (i2 != i) {\n            return i2;")
				.contains("public static long reverseCompareAndWidenOrIntBoolean(int i, boolean z) {")
				.contains("public static long booleanCompareAndWidenOrIntBoolean(int i, boolean z) {")
				.contains("if (i2 != (z ? 1 : 0)) {\n            return i2;")
				.contains("public static long reverseBooleanCompareAndWidenOrIntBoolean(int i, boolean z) {")
				.contains("if ((z ? 1 : 0) != i2) {\n            return i2;")
				.contains("public static long relationalCompareAndWidenOrIntBoolean(int i, boolean z) {")
				.contains("if (i2 >= i) {\n            return i2;")
				.contains("public static char charOrIntBoolean(int i, boolean z) {\n        return (char) (i | (z ? 1 : 0));")
				.contains("public static double doubleOrIntBoolean(int i, boolean z) {\n        return i | (z ? 1 : 0);")
				.doesNotContain("== 1 ? 1L : 0L");
	}
}
