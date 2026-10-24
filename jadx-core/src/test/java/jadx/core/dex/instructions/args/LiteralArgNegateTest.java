package jadx.core.dex.instructions.args;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LiteralArgNegateTest {
	@Test
	void doesNotNegateOutsidePrimitiveRange() {
		assertThat(LiteralArg.make(Byte.MIN_VALUE, ArgType.BYTE).negate()).isNull();
		assertThat(LiteralArg.make(Short.MIN_VALUE, ArgType.SHORT).negate()).isNull();
		assertThat(LiteralArg.make(Integer.MIN_VALUE, ArgType.INT).negate()).isNull();
		assertThat(LiteralArg.make(Long.MIN_VALUE, ArgType.LONG).negate()).isNull();
	}

	@Test
	void negatesRepresentableIntegerLiteral() {
		assertThat(LiteralArg.make(-7, ArgType.INT).negate().getLiteral()).isEqualTo(7);
		assertThat(LiteralArg.make(-7, ArgType.LONG).negate().getLiteral()).isEqualTo(7);
	}
}
