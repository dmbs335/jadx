package jadx.tests.integration.conditions;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestShortCircuitFinalFieldPrelude extends SmaliTest {

	@Test
	public void test() {
		assertThat(getClassNodeFromSmali())
				.code()
				.contains("Float.intBitsToFloat(i) < 10.0f"
						+ " && Float.intBitsToFloat(i2) < this.top")
				// cmpl/if-lez and cmpg/if-gez both enter this branch for NaN, so direct
				// <=/>= relations would silently exclude valid bytecode paths.
				.contains("!(Float.intBitsToFloat(i) > 20.0f)"
						+ " || !(Float.intBitsToFloat(i2) < this.top)")
				.doesNotContain("Code duplicated")
				.doesNotContain("if (Float.intBitsToFloat(yBits) < this.top) {\n        }")
				.doesNotContain("(r0 =")
				.doesNotContain("Method not decompiled")
				.doesNotContain("JADX ERROR");
	}
}
