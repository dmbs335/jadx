package jadx.core.dex.visitors;

import org.junit.jupiter.api.Test;

import jadx.core.dex.instructions.args.ArgType;
import jadx.core.dex.instructions.args.RegisterArg;
import jadx.core.dex.instructions.args.SSAVar;

import static org.assertj.core.api.Assertions.assertThat;

class GenericTypesVisitorTest {

	@Test
	void initCodeVariableForLateConstructorResult() {
		ArgType genericType = ArgType.generic("java.util.ArrayList", ArgType.STRING);
		RegisterArg resultArg = new RegisterArg(17, genericType);
		SSAVar ssaVar = new SSAVar(17, 0, resultArg);
		ssaVar.markAsImmutable(genericType);

		assertThat(ssaVar.isCodeVarSet()).isFalse();
		assertThat(GenericTypesVisitor.initAndGetCodeVarType(resultArg)).isEqualTo(genericType);
		assertThat(ssaVar.isCodeVarSet()).isTrue();
	}
}
