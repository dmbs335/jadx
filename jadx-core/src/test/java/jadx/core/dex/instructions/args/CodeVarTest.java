package jadx.core.dex.instructions.args;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CodeVarTest {

	@Test
	void addToImmutableMultiValueListPreservesExistingVariables() {
		SSAVar first = makeVar(0);
		SSAVar second = makeVar(1);
		SSAVar third = makeVar(2);
		CodeVar codeVar = new CodeVar();
		codeVar.setSsaVars(List.of(first, second));

		codeVar.addSsaVar(third);

		assertThat(codeVar.getSsaVars()).containsExactly(first, second, third);
	}

	private static SSAVar makeVar(int version) {
		return new SSAVar(0, version, InsnArg.reg(0, ArgType.INT));
	}
}
