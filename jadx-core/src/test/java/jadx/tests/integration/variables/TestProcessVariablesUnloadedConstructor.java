package jadx.tests.integration.variables;

import java.util.List;

import org.junit.jupiter.api.Test;

import jadx.core.dex.attributes.AFlag;
import jadx.core.dex.attributes.AType;
import jadx.core.dex.attributes.nodes.SkipMethodArgsAttr;
import jadx.core.dex.instructions.args.ArgType;
import jadx.core.dex.nodes.ClassNode;
import jadx.core.dex.nodes.MethodNode;
import jadx.core.utils.exceptions.JadxRuntimeException;
import jadx.tests.api.SmaliTest;

import static jadx.core.dex.nodes.ProcessState.GENERATED_AND_UNLOADED;
import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class TestProcessVariablesUnloadedConstructor extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		List<ClassNode> classes = loadFromSmaliFiles();
		ClassNode target = searchCls(classes, "variables.pv.Target");
		ClassNode caller = searchCls(classes, "variables.pv.Caller");

		MethodNode syntheticConstructor = target.searchMethodByShortId("<init>(Lvariables/pv/Marker;)V");
		assertThat(syntheticConstructor).isNotNull();
		target.remove(AFlag.DONT_UNLOAD_CLASS);
		target.unload();
		target.setState(GENERATED_AND_UNLOADED);

		SkipMethodArgsAttr skipArgs = syntheticConstructor.get(AType.SKIP_MTH_ARGS);
		assertThat(skipArgs).isNotNull();
		assertThat(skipArgs.isRemovedArg(0, ArgType.object("variables.pv.Marker"))).isTrue();
		assertThat(target.getState()).isEqualTo(GENERATED_AND_UNLOADED);
		assertThatThrownBy(syntheticConstructor::getArgRegs)
				.isInstanceOf(JadxRuntimeException.class)
				.hasMessageContaining("Method arg registers not loaded");

		assertThat(caller).reloadCode(this)
				.containsOne("return new Target();")
				.doesNotContain("Marker");
	}
}
