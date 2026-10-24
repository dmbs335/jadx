package jadx.core.dex.nodes;

import org.junit.jupiter.api.Test;

import jadx.api.plugins.input.insns.Opcode;
import jadx.core.dex.instructions.ArithNode;
import jadx.core.dex.instructions.ArithOp;
import jadx.core.dex.instructions.InsnType;
import jadx.core.dex.instructions.ReturnNode;
import jadx.core.dex.instructions.args.ArgType;
import jadx.core.dex.instructions.args.InsnArg;

import static org.assertj.core.api.Assertions.assertThat;

class InsnNodeSameTest {
	@Test
	void specializedInstructionMustNotMatchGenericInstructionInEitherOrder() {
		InsnNode generic = new InsnNode(InsnType.RETURN, 1);
		generic.addArg(InsnArg.reg(0, ArgType.INT));
		ReturnNode specialized = new ReturnNode(Opcode.RETURN_INT);
		specialized.addArg(InsnArg.reg(0, ArgType.INT));

		assertThat(generic.isSame(specialized)).isFalse();
		assertThat(specialized.isSame(generic)).isFalse();
	}

	@Test
	void wrappedArgumentMustNotMatchPlainArgumentInEitherOrder() {
		InsnNode plain = new InsnNode(InsnType.RETURN, 1);
		plain.addArg(InsnArg.reg(0, ArgType.INT));

		InsnNode wrapped = new InsnNode(InsnType.RETURN, 1);
		wrapped.addArg(InsnArg.wrapArg(new ArithNode(ArithOp.ADD, null,
				InsnArg.reg(0, ArgType.INT), InsnArg.lit(1, ArgType.INT))));

		assertThat(plain.isSame(wrapped)).isFalse();
		assertThat(wrapped.isSame(plain)).isFalse();
	}

	@Test
	void arithmeticOpcodeTypeIsPartOfInstructionIdentity() {
		ArithNode intAdd = new ArithNode(ArithOp.ADD, InsnArg.reg(0, ArgType.INT),
				InsnArg.reg(1, ArgType.INT), InsnArg.reg(2, ArgType.INT), ArgType.INT);
		ArithNode longAdd = new ArithNode(ArithOp.ADD, InsnArg.reg(0, ArgType.LONG),
				InsnArg.reg(1, ArgType.LONG), InsnArg.reg(2, ArgType.LONG), ArgType.LONG);

		assertThat(intAdd.isSame(longAdd)).isFalse();
		assertThat(longAdd.isSame(intAdd)).isFalse();
	}
}
