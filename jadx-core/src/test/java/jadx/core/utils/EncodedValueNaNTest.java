package jadx.core.utils;

import org.junit.jupiter.api.Test;

import jadx.api.plugins.input.data.annotations.EncodedType;
import jadx.api.plugins.input.data.annotations.EncodedValue;
import jadx.core.codegen.TypeGen;
import jadx.core.dex.instructions.args.ArgType;
import jadx.core.dex.instructions.args.LiteralArg;

import static org.assertj.core.api.Assertions.assertThat;

class EncodedValueNaNTest {
	@Test
	void preserveNonCanonicalFloatNaNPayload() {
		int bits = 0x7fc00001;
		EncodedValue value = new EncodedValue(EncodedType.ENCODED_FLOAT, Float.intBitsToFloat(bits));
		LiteralArg literal = (LiteralArg) EncodedValueUtils.convertToConstValue(value);
		assertThat((int) literal.getLiteral()).isEqualTo(bits);
		assertThat((int) literal.negate().getLiteral()).isEqualTo(bits ^ Integer.MIN_VALUE);
		assertThat(TypeGen.literalToString(literal.getLiteral(), ArgType.FLOAT, StringUtils.getInstance(), false, false))
				.isEqualTo("Float.intBitsToFloat(0x7fc00001)");
	}

	@Test
	void preserveNonCanonicalDoubleNaNPayload() {
		long bits = 0x7ff8000000000001L;
		EncodedValue value = new EncodedValue(EncodedType.ENCODED_DOUBLE, Double.longBitsToDouble(bits));
		LiteralArg literal = (LiteralArg) EncodedValueUtils.convertToConstValue(value);
		assertThat(literal.getLiteral()).isEqualTo(bits);
		assertThat(literal.negate().getLiteral()).isEqualTo(bits ^ Long.MIN_VALUE);
		assertThat(TypeGen.literalToString(literal.getLiteral(), ArgType.DOUBLE, StringUtils.getInstance(), false, false))
				.isEqualTo("Double.longBitsToDouble(0x7ff8000000000001L)");
	}
}
