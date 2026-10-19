package jadx.tests.integration.types;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestMixedBooleanBitOp extends SmaliTest {
	@Test
	public void test() {
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
				.contains("new TestMixedBooleanBitOp(i | (z ? 1 : 0))");
	}
}
