package jadx.tests.integration.constructors;

import java.util.List;

import jadx.core.dex.attributes.AType;
import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.SmaliTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Diagnostic fixtures for constructors whose original instruction order cannot be
 * represented as valid Java without changing the order of side effects.
 */
abstract class ExpectedConstructorErrorTest extends SmaliTest {
	@Override
	protected void runChecks(List<ClassNode> classes) {
		assertThat(classes.stream()
				.flatMap(cls -> cls.getMethods().stream())
				.anyMatch(mth -> mth.contains(AType.JADX_ERROR)))
				.as("expected an explicit constructor decompilation error")
				.isTrue();
	}
}
