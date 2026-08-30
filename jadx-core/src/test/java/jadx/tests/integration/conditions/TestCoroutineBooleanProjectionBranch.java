package jadx.tests.integration.conditions;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestCoroutineBooleanProjectionBranch extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		assertThat(getClassNodeFromSmaliFiles())
				.code()
				.containsOne("while (true)")
				.oneOf(
						c -> c.countString(2, "if (!((Boolean)"),
						c -> c.containsOne("if (!((Boolean)")
								.containsOne("while (((Boolean)"))
				.containsOne("consume(bool);")
				.containsOne("consume(obj);")
				.doesNotContain("Unsupported multi-entry loop")
				.doesNotContain("Region traversal cycle");
	}
}
