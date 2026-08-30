package jadx.core.dex.visitors;

import org.junit.jupiter.api.Test;

import jadx.core.dex.attributes.AFlag;
import jadx.core.dex.attributes.nodes.BooleanNumericConversionAttr;
import jadx.core.dex.instructions.InsnType;
import jadx.core.dex.instructions.args.ArgType;
import jadx.core.dex.instructions.args.CodeVar;
import jadx.core.dex.instructions.args.InsnArg;
import jadx.core.dex.instructions.args.RegisterArg;
import jadx.core.dex.instructions.args.SSAVar;
import jadx.core.dex.instructions.mods.TernaryInsn;
import jadx.core.dex.nodes.InsnNode;

import static org.assertj.core.api.Assertions.assertThat;

class PrepareForCodeGenTest {

	@Test
	void detectMarkedStaleBooleanToFloatIdentity() {
		RegisterArg source = makeTypedSource(0, ArgType.FLOAT);
		TernaryInsn conversion = ModVisitor.makeBooleanConvertInsn(
				InsnArg.reg(1, ArgType.FLOAT), source.duplicate(), ArgType.FLOAT);
		conversion.add(AFlag.SYNTHETIC);
		conversion.getCondition().getCompare().getInsn().addAttr(BooleanNumericConversionAttr.INSTANCE);

		assertThat(PrepareForCodeGen.getMarkedFloatIdentitySource(conversion).getSVar())
				.isSameAs(source.getSVar());
	}

	@Test
	void rejectUnmarkedBooleanToFloatConversion() {
		RegisterArg source = makeTypedSource(0, ArgType.FLOAT);
		TernaryInsn conversion = ModVisitor.makeBooleanConvertInsn(
				InsnArg.reg(1, ArgType.FLOAT), source.duplicate(), ArgType.FLOAT);
		conversion.add(AFlag.SYNTHETIC);

		assertThat(PrepareForCodeGen.getMarkedFloatIdentitySource(conversion)).isNull();
	}

	@Test
	void allowInvokeInOrderedConstructorExpression() {
		assertThat(PrepareForCodeGen.isOrderedConstructorExpression(new InsnNode(InsnType.INVOKE, 0))).isTrue();
	}

	@Test
	void rejectFieldReadInOrderedConstructorExpression() {
		assertThat(PrepareForCodeGen.isOrderedConstructorExpression(new InsnNode(InsnType.SGET, 0))).isFalse();
	}

	private static RegisterArg makeTypedSource(int regNum, ArgType type) {
		RegisterArg assign = InsnArg.reg(regNum, type);
		SSAVar var = new SSAVar(regNum, 0, assign);
		CodeVar codeVar = new CodeVar();
		codeVar.setType(type);
		codeVar.setSsaVars(java.util.List.of(var));
		var.setCodeVar(codeVar);
		return assign;
	}
}
