package jadx.tests.integration.arith;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestNaNPayload extends SmaliTest {

	@Test
	public void test() {
		forceDecompiledCheck();
		assertThat(getClassNodeFromSmaliWithPath("arith", "TestNaNPayload"))
				.code()
				.contains("public static final double NAN = Double.longBitsToDouble(0x7ff8000000000001L);")
				.contains("public static final float NAN_FLOAT = Float.intBitsToFloat(0x7fc00001);")
				.contains("return Double.doubleToRawLongBits(Double.longBitsToDouble(0x7ff8000000000001L));");
	}
}
