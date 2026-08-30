package jadx.tests.integration.coroutines;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestVehicleApiFlowSuspendPipeline extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		assertThat(getClassNodeFromSmali(
				"coroutines/TestVehicleApiFlowSuspendPipeline",
				"coroutines.ExecuteBodyEmitPipeline"))
						.code()
						.doesNotContain("Code duplicated")
						.containsOne("Normalize execute/body/emit flow completions through state dispatch")
						.containsOne("execute(this)")
						.containsOne("bodyNullable(this)")
						.containsOne("emit(this)");
	}
}
